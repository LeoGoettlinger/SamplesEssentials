package com.magmaguy.elitemobs.menus.gambling;

import java.util.UUID;
import org.bukkit.inventory.Inventory;

public abstract class GamblingSession {
   public final UUID playerUUID;
   public int betAmount;
   Inventory inventory;

   protected GamblingSession(UUID playerUUID, int betAmount) {
      this.playerUUID = playerUUID;
      this.betAmount = betAmount;
   }
}
