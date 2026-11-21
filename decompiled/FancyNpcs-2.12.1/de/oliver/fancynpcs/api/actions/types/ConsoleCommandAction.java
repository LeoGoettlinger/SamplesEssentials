package de.oliver.fancynpcs.api.actions.types;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parsers;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.PaperColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ConsoleCommandAction extends NpcAction {
   public ConsoleCommandAction() {
      super("console_command", true);
   }

   @Override
   public void execute(@NotNull ActionExecutionContext context, String value) {
      if (value != null && !value.isEmpty()) {
         String command = value;
         if (context.getPlayer() != null) {
            command = value.replace("{player}", context.getPlayer().getName());
         }

         String finalCommand = PaperColor.handler().translateRaw(command, context.getPlayer(), Parsers::placeholder);
         FancyNpcsPlugin.get().getScheduler().runTask(null, () -> {
            try {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            } catch (Exception var2x) {
               FancyNpcsPlugin.get().getFancyLogger().warn("Failed to execute command: " + finalCommand);
            }
         });
      }
   }
}
