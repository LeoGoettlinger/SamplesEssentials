package com.artillexstudios.axafkzone.libs.axapi.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {
   String value();

   Comment.CommentType type() default Comment.CommentType.BLOCK;

   public static enum CommentType {
      BLOCK,
      INLINE;
   }
}
