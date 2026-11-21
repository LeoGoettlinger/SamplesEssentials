package com.sk89q.worldedit.command.util.annotation;

import com.fastasyncworldedit.core.regions.FaweMaskManager;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.enginehub.piston.inject.InjectAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@InjectAnnotation
public @interface AllowedRegion {
   FaweMaskManager.MaskType value() default FaweMaskManager.MaskType.OWNER;
}
