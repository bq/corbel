package io.corbel.iam.model.validation;

import io.corbel.iam.model.Scope;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ScopeValidation implements ConstraintValidator<ValidateScope, Scope> {

    @Override
    public void initialize(ValidateScope constraintAnnotation) {
    }

    @Override
    public boolean isValid(Scope value, ConstraintValidatorContext context) {
        return (value.isComposed()) || value.getAudience() != null && value.getRules() != null && !value.getRules().isEmpty();
    }
}