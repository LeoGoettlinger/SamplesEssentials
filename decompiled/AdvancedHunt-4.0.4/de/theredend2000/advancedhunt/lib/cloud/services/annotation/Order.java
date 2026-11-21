package de.theredend2000.advancedhunt.lib.cloud.services.annotation;

import de.theredend2000.advancedhunt.lib.cloud.services.ExecutionOrder;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
   ExecutionOrder value() default ExecutionOrder.SOON;
}
