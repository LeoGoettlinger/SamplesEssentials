package com.skyblockexp.ezrtp.config.teleport;

public final class RtpLimitSettings {
   private final int cooldownSeconds;
   private final int dailyLimit;
   private final int weeklyLimit;
   private final Double cost;
   private final boolean disableDailyLimit;

   public RtpLimitSettings(int cooldownSeconds, int dailyLimit, int weeklyLimit, Double cost) {
      this(cooldownSeconds, dailyLimit, weeklyLimit, cost, false);
   }

   public RtpLimitSettings(int cooldownSeconds, int dailyLimit, int weeklyLimit, Double cost, boolean disableDailyLimit) {
      this.cooldownSeconds = cooldownSeconds;
      this.dailyLimit = dailyLimit;
      this.weeklyLimit = weeklyLimit;
      this.cost = cost;
      this.disableDailyLimit = disableDailyLimit;
   }

   public int getCooldownSeconds() {
      return this.cooldownSeconds;
   }

   public int getDailyLimit() {
      return this.dailyLimit;
   }

   public int getWeeklyLimit() {
      return this.weeklyLimit;
   }

   public Double getCost() {
      return this.cost;
   }

   public boolean isDisableDailyLimit() {
      return this.disableDailyLimit;
   }
}
