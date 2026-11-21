package me.byteful.plugin.leveltools.libs.lamp.bukkit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.byteful.plugin.leveltools.libs.lamp.annotation.DistributeOnMethods;
import me.byteful.plugin.leveltools.libs.lamp.annotation.NotSender;
import org.bukkit.permissions.PermissionDefault;

@DistributeOnMethods
@NotSender.ImpliesNotSender
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandPermission {
   String value();

   PermissionDefault defaultAccess() default PermissionDefault.OP;
}
