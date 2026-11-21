package com.samplecraft.darkdrop.model;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

public class PendingChest {
   private final Location location;
   private final DropSettings settings;
   private final boolean goodOutcome;
   private final UUID originId;
   private boolean consumed;
   private BukkitTask despawnTask;

   public PendingChest(Location location, DropSettings settings, boolean goodOutcome, UUID originId) {
      this.location = location;
      this.settings = settings;
      this.goodOutcome = goodOutcome;
      this.originId = originId;
      this.consumed = false;
   }

   public Location getLocation() {
      return this.location;
   }

   public DropSettings getSettings() {
      return this.settings;
   }

   public boolean isGoodOutcome() {
      return this.goodOutcome;
   }

   public UUID getOriginId() {
      return this.originId;
   }

   public boolean isConsumed() {
      return this.consumed;
   }

   public void setConsumed(boolean consumed) {
      this.consumed = consumed;
   }

   public BukkitTask getDespawnTask() {
      return this.despawnTask;
   }

   public void setDespawnTask(BukkitTask despawnTask) {
      this.despawnTask = despawnTask;
   }
}
