package de.oliver.fancynpcs.api.actions.types;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.api.actions.executor.ActionExecutionContext;
import org.jetbrains.annotations.NotNull;

public class SendToServerAction extends NpcAction {
   public SendToServerAction() {
      super("send_to_server", true);
   }

   @Override
   public void execute(@NotNull ActionExecutionContext context, String value) {
      if (value != null && !value.isEmpty()) {
         if (context.getPlayer() != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(value);
            context.getPlayer().sendPluginMessage(FancyNpcsPlugin.get().getPlugin(), "BungeeCord", out.toByteArray());
         }
      }
   }
}
