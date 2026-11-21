package com.sk89q.worldedit.internal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.enginehub.piston.inject.InjectAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@InjectAnnotation
public @interface ClipboardMask {
}
