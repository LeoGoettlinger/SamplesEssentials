package de.theredend2000.advancedhunt.lib.cronutils.validation;

import de.theredend2000.advancedhunt.lib.cronutils.model.CronType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
   validatedBy = {CronValidator.class}
)
@Inherited
@Documented
public @interface Cron {
   String message() default "UNUSED";

   Class<?>[] groups() default {};

   Class<? extends Payload>[] payload() default {};

   CronType type();
}
