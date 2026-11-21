package com.artillexstudios.axtrade.libs.lamp.annotation.dynamic;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AnnotationReplacer<T> {
   @Nullable
   Collection<Annotation> replaceAnnotations(@NotNull AnnotatedElement var1, @NotNull T var2);
}
