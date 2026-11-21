package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.gui.GuiSettings;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GuiSessionManager {
   private final Map<UUID, GuiSessionManager.GuiSession> openMenus = new ConcurrentHashMap<>();

   public void startSession(Player player, Inventory inventory, Map<Integer, GuiOption> optionMap, GuiSettings settings) {
      GuiSessionManager.GuiSession session = new GuiSessionManager.GuiSession(inventory, optionMap, settings);
      this.openMenus.put(player.getUniqueId(), session);
   }

   public GuiSessionManager.GuiSession getSession(Player player) {
      return this.openMenus.get(player.getUniqueId());
   }

   public GuiSessionManager.GuiSession endSession(Player player) {
      return this.openMenus.remove(player.getUniqueId());
   }

   public boolean hasSession(Player player) {
      return this.openMenus.containsKey(player.getUniqueId());
   }

   public void clearAllSessions() {
      this.openMenus.clear();
   }

   public Map<UUID, GuiSessionManager.GuiSession> getAllSessions() {
      return new ConcurrentHashMap<>(this.openMenus);
   }

   public static class GuiSession {
      private final Inventory inventory;
      private final Map<Integer, GuiOption> options;
      private final GuiSettings settings;

      public GuiSession(Inventory inventory, Map<Integer, GuiOption> options, GuiSettings settings) {
         this.inventory = inventory;
         this.options = options;
         this.settings = settings;
      }

      public Inventory inventory() {
         return this.inventory;
      }

      public Map<Integer, GuiOption> options() {
         return this.options;
      }

      public GuiSettings settings() {
         return this.settings;
      }
   }
}
