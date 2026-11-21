package de.oliver.fancynpcs.api.actions;

import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NpcAction {
   private final String name;
   private final boolean requiresValue;

   public NpcAction(String name, boolean requiresValue) {
      this.name = name;
      this.requiresValue = requiresValue;
   }

   public abstract void execute(@NotNull ActionExecutionContext var1, @Nullable String var2);

   public String getName() {
      return this.name;
   }

   public boolean requiresValue() {
      return this.requiresValue;
   }

   public record NpcActionData(int order, NpcAction action, String value) {
   }
}
