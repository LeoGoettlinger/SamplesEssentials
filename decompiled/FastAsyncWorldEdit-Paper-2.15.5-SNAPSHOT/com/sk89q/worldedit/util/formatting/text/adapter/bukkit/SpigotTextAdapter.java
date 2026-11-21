package com.sk89q.worldedit.util.formatting.text.adapter.bukkit;

import com.sk89q.worldedit.util.formatting.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface SpigotTextAdapter {
   static BaseComponent[] toBungeeCord(@NonNull final Component component) {
      return SpigotAdapter.toBungeeCord(component);
   }
}
