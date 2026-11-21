package com.magmaguy.elitemobs.dungeons;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;

public class DungeonBossLockout implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<String, Long> lockouts = new HashMap<>();

   public static String createBossIdentifier(String filename, String spawnLocation) {
      String[] parts = spawnLocation.split(",");
      return parts.length >= 4 ? filename + ":" + parts[1] + "," + parts[2] + "," + parts[3] : filename + ":" + spawnLocation;
   }

   public void addLockout(String bossIdentifier, int lockoutMinutes) {
      long expirationTime = System.currentTimeMillis() + lockoutMinutes * 60L * 1000L;
      this.lockouts.put(bossIdentifier, expirationTime);
   }

   public boolean isLockedOut(String bossIdentifier) {
      Long expirationTime = this.lockouts.get(bossIdentifier);
      if (expirationTime == null) {
         return false;
      } else if (System.currentTimeMillis() >= expirationTime) {
         this.lockouts.remove(bossIdentifier);
         return false;
      } else {
         return true;
      }
   }

   public long getRemainingLockoutMillis(String bossIdentifier) {
      Long expirationTime = this.lockouts.get(bossIdentifier);
      if (expirationTime == null) {
         return 0L;
      } else {
         long remaining = expirationTime - System.currentTimeMillis();
         if (remaining <= 0L) {
            this.lockouts.remove(bossIdentifier);
            return 0L;
         } else {
            return remaining;
         }
      }
   }

   public String getFormattedRemainingTime(String bossIdentifier) {
      long remainingMillis = this.getRemainingLockoutMillis(bossIdentifier);
      if (remainingMillis <= 0L) {
         return "0m";
      } else {
         long totalMinutes = remainingMillis / 60000L;
         long hours = totalMinutes / 60L;
         long minutes = totalMinutes % 60L;
         return hours > 0L ? hours + "h " + minutes + "m" : minutes + "m";
      }
   }

   public boolean removeLockout(String bossIdentifier) {
      return this.lockouts.remove(bossIdentifier) != null;
   }

   public int clearLockouts() {
      int cleared = this.lockouts.size();
      this.lockouts.clear();
      return cleared;
   }

   public void cleanupExpiredLockouts() {
      long currentTime = System.currentTimeMillis();
      Iterator<Entry<String, Long>> iterator = this.lockouts.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<String, Long> entry = iterator.next();
         if (currentTime >= entry.getValue()) {
            iterator.remove();
         }
      }
   }

   @Generated
   public Map<String, Long> getLockouts() {
      return this.lockouts;
   }
}
