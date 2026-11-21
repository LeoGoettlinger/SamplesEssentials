package com.samplecraft.darkdrop.gui;

import com.samplecraft.darkdrop.DarkDrop;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class CommandInputSession {
   private final DarkDrop plugin;
   private final Map<UUID, CommandInputSession.Session> active = new HashMap<>();

   public CommandInputSession(DarkDrop plugin) {
      this.plugin = plugin;
   }

   public void start(Player player, Integer index, CommandInputGUI gui) {
      this.active.put(player.getUniqueId(), new CommandInputSession.Session(index, gui));
   }

   public boolean isActive(Player player) {
      return this.active.containsKey(player.getUniqueId());
   }

   public CommandInputSession.Session consume(Player player) {
      return this.active.remove(player.getUniqueId());
   }

   public void cancel(Player player) {
      this.active.remove(player.getUniqueId());
   }

   public static class Session {
      public final Integer index;
      public final CommandInputGUI gui;

      public Session(Integer index, CommandInputGUI gui) {
         this.index = index;
         this.gui = gui;
      }
   }
}
