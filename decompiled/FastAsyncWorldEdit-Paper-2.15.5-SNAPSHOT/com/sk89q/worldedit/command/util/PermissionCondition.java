package com.sk89q.worldedit.command.util;

import com.sk89q.worldedit.extension.platform.Actor;
import java.util.Set;
import org.enginehub.piston.Command;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

public class PermissionCondition implements Command.Condition {
   private static final Key<Actor> ACTOR_KEY = Key.of(Actor.class);
   private final Set<String> permissions;
   private final boolean queued;

   public PermissionCondition(Set<String> permissions) {
      this(permissions, true);
   }

   public PermissionCondition(Set<String> permissions, boolean queued) {
      this.permissions = permissions;
      this.queued = queued;
   }

   public Set<String> getPermissions() {
      return this.permissions;
   }

   @Override
   public boolean satisfied(InjectedValueAccess context) {
      return this.permissions.isEmpty()
         || context.injectedValue(ACTOR_KEY).map(actor -> this.permissions.stream().anyMatch(actor::hasPermission)).orElse(false);
   }

   public boolean isQueued() {
      return this.queued;
   }
}
