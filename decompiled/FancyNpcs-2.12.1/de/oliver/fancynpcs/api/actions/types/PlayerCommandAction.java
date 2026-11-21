package de.oliver.fancynpcs.api.actions.types;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parsers;
import de.oliver.fancynpcs.libs.chatcolorhandler.paper.PaperColor;
import org.jetbrains.annotations.NotNull;

public class PlayerCommandAction extends NpcAction {
   public PlayerCommandAction() {
      super("player_command", true);
   }

   @Override
   public void execute(@NotNull ActionExecutionContext context, String value) {
      if (value != null && !value.isEmpty()) {
         if (context.getPlayer() != null) {
            String command = PaperColor.handler().translateRaw(value, context.getPlayer(), Parsers::placeholder);
            if (command.toLowerCase().startsWith("server")) {
               String[] args = value.split(" ");
               if (args.length >= 2) {
                  String server = args[1];
                  ByteArrayDataOutput out = ByteStreams.newDataOutput();
                  out.writeUTF("Connect");
                  out.writeUTF(server);
                  context.getPlayer().sendPluginMessage(FancyNpcsPlugin.get().getPlugin(), "BungeeCord", out.toByteArray());
               }
            } else {
               FancyNpcsPlugin.get().getScheduler().runTask(context.getPlayer().getLocation(), () -> {
                  try {
                     context.getPlayer().chat("/" + command);
                  } catch (Exception var3x) {
                     FancyNpcsPlugin.get().getFancyLogger().warn("Failed to execute command: " + command);
                  }
               });
            }
         }
      }
   }
}
