package com.sk89q.worldedit.command.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.enginehub.piston.annotation.CommandCondition;

@Retention(RetentionPolicy.RUNTIME)
@CommandCondition(CommandPermissionsConditionGenerator.class)
public @interface CommandPermissions {
   String[] value() default {};

   boolean queued() default true;
}
