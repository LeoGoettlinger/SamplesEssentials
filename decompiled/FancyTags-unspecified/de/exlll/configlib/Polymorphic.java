package de.exlll.configlib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SerializeWith(
   serializer = PolymorphicSerializer.class
)
public @interface Polymorphic {
   String DEFAULT_PROPERTY = "type";

   String property() default "type";
}
