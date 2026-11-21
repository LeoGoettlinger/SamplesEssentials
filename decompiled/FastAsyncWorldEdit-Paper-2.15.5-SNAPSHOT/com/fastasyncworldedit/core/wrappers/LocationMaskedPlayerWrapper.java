package com.fastasyncworldedit.core.wrappers;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;

public class LocationMaskedPlayerWrapper extends AsyncPlayer {
   private final boolean allowTeleport;
   private Location position;

   public LocationMaskedPlayerWrapper(Player parent, Location position) {
      this(parent, position, false);
   }

   public LocationMaskedPlayerWrapper(Player parent, Location position, boolean allowTeleport) {
      super(parent);
      this.position = position;
      this.allowTeleport = allowTeleport;
   }

   @Override
   public Location getLocation() {
      return this.position;
   }

   @Override
   public void setPosition(final Vector3 pos) {
      if (this.allowTeleport) {
         super.setPosition(pos);
         this.update();
      }
   }

   private void update() {
      this.position = super.getLocation();
   }

   @Override
   public void setPosition(Vector3 pos, float pitch, float yaw) {
      if (this.allowTeleport) {
         super.setPosition(pos, pitch, yaw);
         this.update();
      }
   }

   @Override
   public void findFreePosition(Location searchPos) {
      if (this.allowTeleport) {
         super.findFreePosition(searchPos);
         this.update();
      }
   }

   @Override
   public void setOnGround(Location searchPos) {
      if (this.allowTeleport) {
         super.setOnGround(searchPos);
         this.update();
      }
   }

   @Override
   public void findFreePosition() {
      if (this.allowTeleport) {
         super.findFreePosition();
         this.update();
      }
   }

   @Override
   public boolean ascendLevel() {
      if (this.allowTeleport && super.ascendLevel()) {
         this.update();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean descendLevel() {
      if (this.allowTeleport && super.descendLevel()) {
         this.update();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean ascendToCeiling(int clearance) {
      if (this.allowTeleport && super.ascendToCeiling(clearance)) {
         this.update();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean ascendToCeiling(int clearance, boolean alwaysGlass) {
      if (this.allowTeleport && super.ascendToCeiling(clearance, alwaysGlass)) {
         this.update();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean ascendUpwards(int distance) {
      if (this.allowTeleport && super.ascendUpwards(distance)) {
         this.update();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean ascendUpwards(int distance, boolean alwaysGlass) {
      if (this.allowTeleport && super.ascendUpwards(distance, alwaysGlass)) {
         this.update();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void floatAt(int x, int y, int z, boolean alwaysGlass) {
      if (this.allowTeleport) {
         super.floatAt(x, y, z, alwaysGlass);
         this.update();
      }
   }
}
