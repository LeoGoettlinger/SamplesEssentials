package com.sk89q.worldedit.util.formatting.text.adapter.bukkit;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer;
import java.util.List;
import org.bukkit.command.CommandSender;

final class LegacyAdapter implements Adapter {
   @Override
   public void sendMessage(final List<? extends CommandSender> viewers, final Component component) {
      String legacy = LegacyComponentSerializer.legacy().serialize(component);

      for (CommandSender viewer : viewers) {
         viewer.sendMessage(legacy);
      }

      viewers.clear();
   }

   @Override
   public void sendActionBar(final List<? extends CommandSender> viewers, final Component component) {
   }
}
