package com.artillexstudios.axtrade.libs.lamp.command.trait;

import java.lang.annotation.Annotation;
import org.jetbrains.annotations.NotNull;

public interface CommandAnnotationHolder {
   <A extends Annotation> A getAnnotation(@NotNull Class<A> var1);

   boolean hasAnnotation(@NotNull Class<? extends Annotation> var1);
}
