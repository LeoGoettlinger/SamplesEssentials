package de.theredend2000.advancedhunt.lib.cloud.annotation.specifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@API(
   status = API.Status.STABLE
)
public @interface Completions {
   String value() default "";
}
