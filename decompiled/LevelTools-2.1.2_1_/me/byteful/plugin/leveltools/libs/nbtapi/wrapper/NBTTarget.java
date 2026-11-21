package me.byteful.plugin.leveltools.libs.nbtapi.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface NBTTarget {
   String value();

   NBTTarget.Type type() default NBTTarget.Type.AUTOMATIC;

   public static enum Type {
      AUTOMATIC,
      GET,
      SET,
      HAS;
   }
}
