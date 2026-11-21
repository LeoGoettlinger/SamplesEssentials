package me.usainsrht.ujobs.models;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;

public class PlayerLeaderboardData {
   private final UUID uuid;
   private final Map<Job, PlayerLeaderboardData.LeaderboardStats> leaderboardStats;

   public PlayerLeaderboardData(UUID uuid) {
      this.uuid = uuid;
      this.leaderboardStats = new ConcurrentHashMap<>();
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   public Map<Job, PlayerLeaderboardData.LeaderboardStats> getLeaderboardStats() {
      return this.leaderboardStats;
   }

   public static class LeaderboardStats {
      private int position;
      private int level;

      public LeaderboardStats(int position, int level) {
         this.position = position;
         this.level = level;
      }

      @Generated
      public int getPosition() {
         return this.position;
      }

      @Generated
      public int getLevel() {
         return this.level;
      }

      @Generated
      public void setPosition(int position) {
         this.position = position;
      }

      @Generated
      public void setLevel(int level) {
         this.level = level;
      }
   }
}
