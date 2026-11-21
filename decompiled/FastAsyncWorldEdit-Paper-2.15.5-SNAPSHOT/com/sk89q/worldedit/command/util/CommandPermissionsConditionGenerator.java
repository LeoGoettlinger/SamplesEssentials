package com.sk89q.worldedit.command.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.Set;
import org.enginehub.piston.Command;
import org.enginehub.piston.gen.CommandConditionGenerator;
import org.enginehub.piston.util.NonnullByDefault;

@NonnullByDefault
public final class CommandPermissionsConditionGenerator implements CommandConditionGenerator {
   @Override
   public Command.Condition generateCondition(Method commandMethod) {
      CommandPermissions annotation = commandMethod.getAnnotation(CommandPermissions.class);
      Preconditions.checkNotNull(annotation, "Annotation is missing from commandMethod");
      Set<String> permissions = ImmutableSet.copyOf(annotation.value());
      return new PermissionCondition(permissions, annotation.queued());
   }

   public interface Registration {
      CommandPermissionsConditionGenerator.Registration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator generator);
   }
}
