package de.theredend2000.advancedhunt.lib.cloud.services.annotation;

import de.theredend2000.advancedhunt.lib.cloud.services.type.Service;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceImplementation {
   Class<? extends Service<?, ?>> value();
}
