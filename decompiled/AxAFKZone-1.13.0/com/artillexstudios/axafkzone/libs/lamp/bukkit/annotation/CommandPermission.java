package com.artillexstudios.axafkzone.libs.lamp.bukkit.annotation;

import com.artillexstudios.axafkzone.libs.lamp.annotation.DistributeOnMethods;
import com.artillexstudios.axafkzone.libs.lamp.annotation.NotSender;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bukkit.permissions.PermissionDefault;

@DistributeOnMethods
@NotSender.ImpliesNotSender
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandPermission {
   String value();

   PermissionDefault defaultAccess() default PermissionDefault.OP;
}
