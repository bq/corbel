package io.corbel.iam.model.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ScopeValidation.class)
@Documented
public @interface ValidateScope {

    String message() default "Both audience and rules fields can't be empty while creating a non-composed scope";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
