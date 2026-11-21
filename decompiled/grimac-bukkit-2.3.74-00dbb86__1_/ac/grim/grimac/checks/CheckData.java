package ac.grim.grimac.checks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CheckData {
   String name();

   String alternativeName() default "UNKNOWN";

   String configName() default "DEFAULT";

   String description() default "No description provided";

   String stableKey();

   double decay() default 0.05;

   double setback() default 25.0;

   boolean experimental() default false;
}
