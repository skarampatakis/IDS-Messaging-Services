/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.ids.messaging.core.daps;

import de.fraunhofer.iais.eis.DynamicAttributeToken;

/**
 * Implementations of the DapsTokenProvider interface must implement a
 * method that should return a valid JWT Daps token
 * in its String representation.
 */
public interface DapsTokenProvider {
    /**
     * Get the DAPS JWT Token from a DAPS and return its compact String representation.
     *
     * @return the Daps Token of the Connector
     */
    String provideDapsToken() throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException;

    /**
     * Return the DAPS JWT Token in infomodel {@link DynamicAttributeToken} representation.
     *
     * @return DynamicAttributeToken from the DAPS JWT
     */
    DynamicAttributeToken getDAT() throws
            ConnectorMissingCertExtensionException,
            DapsConnectionException,
            DapsEmptyResponseException;
}
