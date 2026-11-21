package com.samplecraft.darkdrop.model;

import org.bukkit.Location;

public class DropTarget {
   private final Location startLocation;
   private final Location landLocation;
   private final DropTarget.TargetMode mode;

   public DropTarget(Location startLocation, Location landLocation, DropTarget.TargetMode mode) {
      this.startLocation = startLocation;
      this.landLocation = landLocation;
      this.mode = mode;
   }

   public Location getStartLocation() {
      return this.startLocation;
   }

   public Location getLandLocation() {
      return this.landLocation;
   }

   public DropTarget.TargetMode getMode() {
      return this.mode;
   }

   public static enum TargetMode {
      PLAYER,
      RANDOM,
      COORDS;
   }
}
