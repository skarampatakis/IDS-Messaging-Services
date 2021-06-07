package de.fraunhofer.ids.messaging.core.daps.customvalidation;

import io.jsonwebtoken.Claims;

@FunctionalInterface
public interface DatValidationRule {
    ValidationRuleResult checkRule(Claims toVerify) throws ValidationRuleException;
}
