package de.oliver.cloud.services.annotation;

import de.oliver.cloud.services.ExecutionOrder;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
   ExecutionOrder value() default ExecutionOrder.SOON;
}
