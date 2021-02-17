package de.fraunhofer.ids.framework.daps;

import java.io.IOException;
import java.security.Key;
import java.sql.Date;
import java.time.Instant;
import java.util.Objects;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.ids.framework.config.ClientProvider;
import de.fraunhofer.ids.framework.config.ConfigContainer;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Get Daps Tokens and Daps Public Key from specified URLs.
 * Spring Component Wrapper for TokenManagerService
 */
@Slf4j
@Service
public class TokenProviderService implements DapsTokenProvider, DapsPublicKeyProvider {
    private final ConfigContainer configContainer;
    private final ClientProvider  clientProvider;
    private final TokenManagerService tokenManagerService;
    private String currentJwt;
    private Key  publicKey;

    @Value( "${daps.key.url}" )
    private String dapsKeyUrl;

    @Value( "${daps.token.url}" )
    private String dapsTokenUrl;

    @Value( "${daps.kid.url:default}" )
    private String keyKid;

    /**
     * @param configContainer the {@link ConfigContainer} managing the connector configuration
     * @param clientProvider         the {@link ClientProvider} providing HttpClients using the current connector configuration
     * @param tokenManagerService client to get a DAT Token from a DAPS (eg Orbiter/AISEC)
     */
    @Autowired
    public TokenProviderService(ConfigContainer configContainer, ClientProvider clientProvider, TokenManagerService tokenManagerService) {
        this.configContainer = configContainer;
        this.clientProvider = clientProvider;
        this.tokenManagerService = tokenManagerService;
    }

    /**
     * Return the DAT as a Infomodel {@link DynamicAttributeToken}
     *
     * @return acquire a new DAPS Token and return it as a {@link DynamicAttributeToken}
     */
    @Override
    public DynamicAttributeToken getDAT() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        return new DynamicAttributeTokenBuilder()
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_(provideDapsToken())
                .build();
    }

    /**
     * Return the DAT as JWT String
     *
     * @return acquire a new DAPS Token and return the JWT String value
     */
    @Override
    public String provideDapsToken() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
        if(this.currentJwt == null || isExpired(currentJwt)) {
            log.debug(String.format("Get a new DAT Token from %s", dapsTokenUrl));
            currentJwt = tokenManagerService.acquireToken(dapsTokenUrl);
        }
        return currentJwt;
    }

    /**
     * Return the Public Key from the DAPS JWKS
     *
     * @return the Public Key from the DAPS (used for validating Tokens of incoming Messages)
     */
    @Override
    public Key providePublicKey() {
        if( publicKey == null ) {
            log.debug(String.format("Getting public key from %s!", dapsKeyUrl));
            getPublicKey();
        }
        log.debug("Provide public key!");
        return publicKey;
    }

    /**
     * Pull the Public Key from the DAPS and save it in the publicKey variable
     */
    private void getPublicKey() {
        try {
            //request the JWK-Set
            log.debug(String.format("Getting json web keyset from %s", dapsKeyUrl));
            var client = clientProvider.getClient();
            var request = new Request.Builder()
                    .url(dapsKeyUrl)
                    .build();
            var response = client.newCall(request).execute();

            var keySetJSON = Objects.requireNonNull(response.body()).string();
            var jsonWebKeySet = new JsonWebKeySet(keySetJSON);
            var jsonWebKey = jsonWebKeySet.getJsonWebKeys().stream().filter(k -> k.getKeyId().equals(keyKid)).findAny()
                                          .orElse(null);

            if( jsonWebKey != null ) {
                this.publicKey = jsonWebKey.getKey();
            } else {
                log.warn("Could not get JsonWebKey with kid " + keyKid + " from received KeySet! PublicKey is null!");
            }
        } catch( IOException e ) {
            log.warn("Could not get key from " + dapsKeyUrl + "!");
            log.warn(e.getMessage(), e);
        } catch( JoseException e ) {
            log.warn("Could not create JsonWebKeySet from response!");
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * @param jwt the jwt to check expiration
     * @return true if jwt expired
     */
    private boolean isExpired(String jwt){
        if(publicKey == null){
            getPublicKey();
        }
        var claims = Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(jwt)
                .getBody();
        log.debug("Current DAT will expire: " + claims.getExpiration().toString());
        return claims.getExpiration().before(Date.from(Instant.now()));
    }
}