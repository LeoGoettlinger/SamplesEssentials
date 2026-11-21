package de.theredend2000.advancedhunt.lib.xseries.reflection.proxy.annotations;

import de.theredend2000.advancedhunt.lib.xseries.reflection.minecraft.MinecraftPackage;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReflectMinecraftPackage {
   MinecraftPackage type();

   String packageName() default "";

   boolean ignoreCurrentName() default false;
}
