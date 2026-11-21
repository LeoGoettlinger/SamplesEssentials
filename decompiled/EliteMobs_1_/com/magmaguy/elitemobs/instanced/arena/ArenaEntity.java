package com.magmaguy.elitemobs.instanced.arena;

import javax.xml.stream.Location;
import lombok.Generated;

public class ArenaEntity {
   private String spawnPointName;
   private Location spawnLocation;
   private int wave;
   private String bossfile;
   private boolean mythicMob = false;
   private int level = -1;

   public ArenaEntity(String spawnPointName, int wave, String bossfile) {
      this.spawnPointName = spawnPointName;
      this.wave = wave;
      this.bossfile = bossfile;
   }

   @Generated
   public String getSpawnPointName() {
      return this.spawnPointName;
   }

   @Generated
   public void setSpawnPointName(String spawnPointName) {
      this.spawnPointName = spawnPointName;
   }

   @Generated
   public Location getSpawnLocation() {
      return this.spawnLocation;
   }

   @Generated
   public void setSpawnLocation(Location spawnLocation) {
      this.spawnLocation = spawnLocation;
   }

   @Generated
   public int getWave() {
      return this.wave;
   }

   @Generated
   public void setWave(int wave) {
      this.wave = wave;
   }

   @Generated
   public String getBossfile() {
      return this.bossfile;
   }

   @Generated
   public void setBossfile(String bossfile) {
      this.bossfile = bossfile;
   }

   @Generated
   public boolean isMythicMob() {
      return this.mythicMob;
   }

   @Generated
   public void setMythicMob(boolean mythicMob) {
      this.mythicMob = mythicMob;
   }

   @Generated
   public int getLevel() {
      return this.level;
   }

   @Generated
   public void setLevel(int level) {
      this.level = level;
   }
}
