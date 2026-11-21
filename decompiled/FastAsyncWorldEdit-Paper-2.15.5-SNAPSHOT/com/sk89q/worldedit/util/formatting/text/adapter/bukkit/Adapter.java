package com.sk89q.worldedit.util.formatting.text.adapter.bukkit;

import com.sk89q.worldedit.util.formatting.text.Component;
import java.util.List;
import org.bukkit.command.CommandSender;

interface Adapter {
   void sendMessage(final List<? extends CommandSender> viewers, final Component component);

   void sendActionBar(final List<? extends CommandSender> viewers, final Component component);
}
