package com.magmaguy.elitemobs.quests;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Generated;

public class QuestLockout implements Serializable {
   private static final long serialVersionUID = 1L;
   private final Map<String, Long> lockouts = new HashMap<>();

   public void addLockout(String questFilename, int lockoutMinutes) {
      long expirationTime = System.currentTimeMillis() + lockoutMinutes * 60L * 1000L;
      this.lockouts.put(questFilename, expirationTime);
   }

   public boolean isLockedOut(String questFilename) {
      Long expirationTime = this.lockouts.get(questFilename);
      if (expirationTime == null) {
         return false;
      } else if (System.currentTimeMillis() >= expirationTime) {
         this.lockouts.remove(questFilename);
         return false;
      } else {
         return true;
      }
   }

   public long getRemainingLockoutMillis(String questFilename) {
      Long expirationTime = this.lockouts.get(questFilename);
      if (expirationTime == null) {
         return 0L;
      } else {
         long remaining = expirationTime - System.currentTimeMillis();
         if (remaining <= 0L) {
            this.lockouts.remove(questFilename);
            return 0L;
         } else {
            return remaining;
         }
      }
   }

   public String getFormattedRemainingTime(String questFilename) {
      long remainingMillis = this.getRemainingLockoutMillis(questFilename);
      if (remainingMillis <= 0L) {
         return "0m";
      } else {
         long totalMinutes = remainingMillis / 60000L;
         long hours = totalMinutes / 60L;
         long minutes = totalMinutes % 60L;
         return hours > 0L ? hours + "h " + minutes + "m" : minutes + "m";
      }
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
