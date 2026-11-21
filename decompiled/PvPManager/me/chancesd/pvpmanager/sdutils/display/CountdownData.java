package me.chancesd.pvpmanager.sdutils.display;

import me.chancesd.pvpmanager.sdutils.utils.Log;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class CountdownData {
   private final DisplayManager.TimeProgressSource progressSource;
   private final DisplayManager.MessageSource bossBarSource;
   private final DisplayManager.MessageSource actionBarSource;
   final BossBar bossBar;
   private final ProgressBar progressBar;
   private final Runnable onFinish;

   private CountdownData(CountdownData.Builder builder) {
      this.bossBar = builder.bossBar;
      this.progressSource = builder.progressSource;
      this.bossBarSource = builder.bossBarSource;
      this.actionBarSource = builder.actionBarSource;
      this.progressBar = builder.progressBar;
      this.onFinish = builder.onFinish;
   }

   boolean update() {
      long timePassedMs = this.progressSource.getProgress();
      long totalTimeMs = this.progressSource.getGoal();
      if (timePassedMs >= totalTimeMs) {
         if (this.onFinish != null) {
            this.onFinish.run();
         }

         return true;
      } else if (timePassedMs < 0L) {
         Log.severe("TimePassed is negative: " + timePassedMs);
         return true;
      } else {
         if (this.actionBarSource != null) {
            this.progressBar.setProgress(timePassedMs).setGoal(totalTimeMs).calculate();
            this.actionBarSource.getMessage(this.progressSource);
         }

         if (this.bossBarSource != null) {
            this.bossBar.setTitle(this.bossBarSource.getMessage(this.progressSource));
            this.bossBar.setProgress((double)(totalTimeMs - timePassedMs) / totalTimeMs);
         }

         return false;
      }
   }

   public static class Builder {
      private DisplayManager.TimeProgressSource progressSource;
      private DisplayManager.MessageSource bossBarSource;
      private DisplayManager.MessageSource actionBarSource;
      private BossBar bossBar;
      private ProgressBar progressBar;
      private Runnable onFinish;

      public CountdownData.Builder withBossBar(BossBar bar, DisplayManager.MessageSource msgSource) {
         this.bossBar = bar;
         this.bossBarSource = msgSource;
         return this;
      }

      public CountdownData.Builder withActionBar(ProgressBar bar, DisplayManager.MessageSource msgSource) {
         this.progressBar = bar;
         this.actionBarSource = msgSource;
         return this;
      }

      public CountdownData.Builder withTimeSource(DisplayManager.TimeProgressSource timeSource) {
         this.progressSource = timeSource;
         return this;
      }

      public CountdownData.Builder onFinish(Runnable finish) {
         this.onFinish = finish;
         return this;
      }

      public CountdownData build(Player player) {
         if (this.bossBar != null) {
            this.bossBar.addPlayer(player);
         }

         return new CountdownData(this);
      }
   }
}
