package com.viaversion.viaversion.api.protocol.packet;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.configuration.RateLimitConfig;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.util.MathUtil;
import java.util.Arrays;

public class PacketTracker {
   private static final long SECOND_NANOS = 1000000000L;
   private final PacketTracker.RateTracker packetTracker = new PacketTracker.RateTracker(5);
   private final PacketTracker.RateTracker packetSizeTracker = new PacketTracker.RateTracker(3);
   private long startTime = System.nanoTime();
   private boolean packetLimiterEnabled = true;
   private final UserConnection connection;
   private long sentPacketsTotal;
   private long receivedPacketsTotal;

   public PacketTracker(UserConnection connection) {
      this.connection = connection;
   }

   public void incrementSent() {
      this.sentPacketsTotal++;
   }

   @Deprecated(
      forRemoval = true
   )
   public boolean incrementReceived() {
      return this.incrementReceived(0);
   }

   public boolean incrementReceived(int packetSize) {
      this.receivedPacketsTotal++;
      ViaVersionConfig config = Via.getConfig();
      long currentTime = System.nanoTime();
      long elapsed = currentTime - this.startTime;
      if (elapsed < 1000000000L) {
         if (config.getPacketTrackerConfig().enabled()) {
            this.packetTracker.add(1);
         }

         if (config.getPacketSizeTrackerConfig().enabled()) {
            this.packetSizeTracker.add(packetSize);
         }

         return false;
      } else {
         if (config.getPacketTrackerConfig().enabled()) {
            this.packetTracker.updateRate(elapsed);
            this.packetTracker.add(1);
         }

         if (config.getPacketSizeTrackerConfig().enabled()) {
            this.packetSizeTracker.updateRate(elapsed);
            this.packetSizeTracker.add(packetSize);
         }

         this.startTime = currentTime;
         return true;
      }
   }

   public boolean exceedsLimits() {
      if (this.connection.isClientSide()) {
         return false;
      } else {
         ViaVersionConfig config = Via.getConfig();
         return this.packetTracker.exceedsLimit(this.connection, config.getPacketTrackerConfig())
            || this.packetSizeTracker.exceedsLimit(this.connection, config.getPacketSizeTrackerConfig());
      }
   }

   public long getSentPackets() {
      return this.sentPacketsTotal;
   }

   @Deprecated(
      forRemoval = true
   )
   public void setSentPackets(long sentPackets) {
      this.sentPacketsTotal = sentPackets;
   }

   public long getReceivedPackets() {
      return this.receivedPacketsTotal;
   }

   @Deprecated(
      forRemoval = true
   )
   public void setReceivedPackets(long receivedPackets) {
      this.receivedPacketsTotal = receivedPackets;
   }

   public long getIntervalPackets() {
      return this.packetTracker.count;
   }

   public void setIntervalPackets(long intervalPackets) {
      this.packetTracker.count = intervalPackets;
   }

   public int getPacketsPerSecond() {
      return this.packetTracker.smoothedRate;
   }

   public boolean isPacketLimiterEnabled() {
      ViaVersionConfig config = Via.getConfig();
      return this.packetLimiterEnabled && (config.getPacketTrackerConfig().enabled() || config.getPacketSizeTrackerConfig().enabled());
   }

   public void setPacketLimiterEnabled(boolean packetLimiterEnabled) {
      this.packetLimiterEnabled = packetLimiterEnabled;
   }

   @Deprecated(
      forRemoval = true
   )
   public void setWarnings(int warnings) {
      this.packetTracker.warnings = warnings;
   }

   public void reset() {
      this.sentPacketsTotal = 0L;
      this.receivedPacketsTotal = 0L;
      this.startTime = System.nanoTime();
      this.packetTracker.reset();
      this.packetSizeTracker.reset();
   }

   private static final class RateTracker {
      private final int[] history;
      private int historyIndex;
      private int historyCount;
      private int smoothedRate = -1;
      private long count;
      private long warningPeriodStart = System.nanoTime();
      private long lastWarning = Long.MIN_VALUE;
      private int warnings;

      public RateTracker(int windowSize) {
         this.history = new int[windowSize];
      }

      public void add(int value) {
         this.count += value;
      }

      public void updateRate(long elapsedNanos) {
         long rate = this.count * 1000000000L / elapsedNanos;
         this.history[this.historyIndex] = (int)MathUtil.clamp(rate, 0L, 2147483647L);
         this.historyIndex = (this.historyIndex + 1) % this.history.length;
         if (this.historyCount < this.history.length) {
            this.historyCount++;
         }

         long sum = 0L;

         for (int i = 0; i < this.historyCount; i++) {
            sum += this.history[i];
         }

         this.smoothedRate = (int)(sum / this.historyCount);
         this.count = 0L;
      }

      public boolean exceedsLimit(UserConnection connection, RateLimitConfig limitConfig) {
         if (!limitConfig.enabled()) {
            return false;
         } else if (limitConfig.maxRate() > 0 && this.count >= limitConfig.maxRate()) {
            connection.disconnect(limitConfig.maxRateKickMessage().replace(limitConfig.ratePlaceholder(), Long.toString(this.count)));
            return true;
         } else {
            return limitConfig.maxWarnings() > 0 && limitConfig.trackingPeriodNanos() > 0L && limitConfig.warningRate() > 0
               ? this.checkTrackedInterval(connection, limitConfig)
               : false;
         }
      }

      private boolean checkTrackedInterval(UserConnection connection, RateLimitConfig config) {
         long currentTime = System.nanoTime();
         if (currentTime - this.warningPeriodStart >= config.trackingPeriodNanos()) {
            this.warnings = 0;
            this.warningPeriodStart = currentTime;
         }

         if (this.smoothedRate >= config.warningRate() && currentTime - this.lastWarning >= 1000000000L) {
            this.lastWarning = currentTime;
            if (++this.warnings >= config.maxWarnings()) {
               connection.disconnect(config.warningKickMessage().replace(config.ratePlaceholder(), Integer.toString(this.smoothedRate)));
               return true;
            }
         }

         return false;
      }

      public void reset() {
         this.count = 0L;
         this.smoothedRate = -1;
         this.warnings = 0;
         this.warningPeriodStart = System.nanoTime();
         this.historyIndex = 0;
         this.historyCount = 0;
         Arrays.fill(this.history, 0);
      }
   }
}
