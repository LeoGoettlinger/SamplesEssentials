package de.oliver.fancynpcs.api.actions.types;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnknownActionAction extends NpcAction {
   private final ActionTrigger trigger;
   private final String unknownActionName;
   private final String unknownActionValue;
   private final int order;

   public UnknownActionAction(ActionTrigger trigger, String unknownActionName, String unknownActionValue, int order) {
      super("unknown_action", true);
      this.trigger = trigger;
      this.unknownActionName = unknownActionName;
      this.unknownActionValue = unknownActionValue;
      this.order = order;
   }

   @Override
   public void execute(@NotNull ActionExecutionContext context, @Nullable String value) {
      NpcAction action = FancyNpcsPlugin.get().getActionManager().getActionByName(this.unknownActionName);
      if (action == null) {
         FancyNpcsPlugin.get().getFancyLogger().warn("Could not find action: " + this.unknownActionName);
      } else {
         action.execute(context, value);
      }
   }

   @Override
   public String getName() {
      return this.unknownActionName + " (unknown)";
   }

   public ActionTrigger getTrigger() {
      return this.trigger;
   }

   public String getUnknownActionName() {
      return this.unknownActionName;
   }

   public String getUnknownActionValue() {
      return this.unknownActionValue;
   }

   public int getOrder() {
      return this.order;
   }
}
