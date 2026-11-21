package de.oliver.fancynpcs.api.actions.types;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;
import org.jetbrains.annotations.NotNull;

public class WaitAction extends NpcAction {
   public WaitAction() {
      super("wait", true);
   }

   @Override
   public void execute(@NotNull ActionExecutionContext context, String value) {
      if (value != null && !value.isEmpty()) {
         int time;
         try {
            time = Integer.parseInt(value);
         } catch (NumberFormatException var6) {
            FancyNpcsPlugin.get().getFancyLogger().warn("Invalid time value for wait action: " + value);
            return;
         }

         try {
            Thread.sleep(time * 1000L);
         } catch (InterruptedException var5) {
            FancyNpcsPlugin.get().getFancyLogger().warn("Thread was interrupted while waiting");
         }
      }
   }
}
