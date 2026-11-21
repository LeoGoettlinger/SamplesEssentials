package me.usainsrht.ujobs.models;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;

public class PlayerJobData {
   private final UUID uuid;
   private final Map<String, PlayerJobData.JobStats> jobStats;

   public PlayerJobData(UUID playerId) {
      this.uuid = playerId;
      this.jobStats = new ConcurrentHashMap<>();
   }

   public PlayerJobData.JobStats getJobStats(String jobId) {
      return this.jobStats.computeIfAbsent(jobId, k -> new PlayerJobData.JobStats());
   }

   public void setJobStats(String jobId, PlayerJobData.JobStats stats) {
      this.jobStats.put(jobId, stats);
   }

   public boolean hasJobStats(String jobId) {
      return this.jobStats.containsKey(jobId);
   }

   public void addExp(String jobId, double exp) {
      PlayerJobData.JobStats stats = this.getJobStats(jobId);
      stats.setExp(stats.getExp() + exp);
   }

   public void addMoney(String jobId, double money) {
      PlayerJobData.JobStats stats = this.getJobStats(jobId);
      stats.setTotalMoney(stats.getTotalMoney() + money);
   }

   public void levelUp(String jobId) {
      PlayerJobData.JobStats stats = this.getJobStats(jobId);
      stats.setLevel(stats.getLevel() + 1);
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   public Map<String, PlayerJobData.JobStats> getJobStats() {
      return this.jobStats;
   }

   public static class JobStats {
      private volatile int level;
      private volatile double exp;
      private volatile double totalMoney;

      public JobStats() {
         this(0, 0.0, 0.0);
      }

      public JobStats(int level, double exp, double totalMoney) {
         this.level = level;
         this.exp = exp;
         this.totalMoney = totalMoney;
      }

      @Generated
      public int getLevel() {
         return this.level;
      }

      @Generated
      public double getExp() {
         return this.exp;
      }

      @Generated
      public double getTotalMoney() {
         return this.totalMoney;
      }

      @Generated
      public void setLevel(int level) {
         this.level = level;
      }

      @Generated
      public void setExp(double exp) {
         this.exp = exp;
      }

      @Generated
      public void setTotalMoney(double totalMoney) {
         this.totalMoney = totalMoney;
      }
   }
}
