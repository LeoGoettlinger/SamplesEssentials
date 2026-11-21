package de.oliver.cloud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
   String[] value() default {};

   @NonNull
   Permission.Mode mode() default Permission.Mode.ANY_OF;

   public static enum Mode {
      ANY_OF,
      ALL_OF;

      @NonNull
      de.oliver.cloud.permission.Permission combine(@NonNull final Stream<de.oliver.cloud.permission.Permission> permissions) {
         List<de.oliver.cloud.permission.Permission> permissionList = permissions.collect(Collectors.toList());
         return this == ANY_OF ? de.oliver.cloud.permission.Permission.anyOf(permissionList) : de.oliver.cloud.permission.Permission.allOf(permissionList);
      }
   }
}
