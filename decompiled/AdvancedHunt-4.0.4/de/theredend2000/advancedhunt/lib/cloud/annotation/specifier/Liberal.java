package de.theredend2000.advancedhunt.lib.cloud.annotation.specifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@API(
   status = API.Status.STABLE
)
public @interface Liberal {
}
