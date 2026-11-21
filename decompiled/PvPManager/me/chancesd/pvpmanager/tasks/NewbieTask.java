package me.chancesd.pvpmanager.tasks;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.display.BossBarBuilder;
import me.chancesd.pvpmanager.sdutils.display.CountdownData;
import me.chancesd.pvpmanager.sdutils.display.DisplayManager;
import me.chancesd.pvpmanager.sdutils.tasks.PausableTask;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.TimeUtil;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public class NewbieTask extends PausableTask {
   private final CombatPlayer player;
   private CountdownData countdownData;

   public NewbieTask(CombatPlayer player, long time) {
      super(time == 0L ? Conf.NEWBIE_TIME.asInt() * 1000L : time);
      this.player = player;
      if (time == 0L) {
         player.message(Lang.NEWBIE_PROTECTION.msgTimeUntil(this.getFinishTime()));
      } else {
         player.message(Lang.NEWBIE_TIME_CHECK.msgTime(time));
      }

      if (Conf.NEWBIE_BOSS_BAR_ENABLED.asBool()) {
         this.startBossBarDisplay();
      }
   }

   @Override
   protected void onComplete() {
      this.player.setNewbie(false);
      this.player.message(Lang.NEWBIE_PROTECTION_END, new Object[0]);
      this.stopBossBarDisplay();
   }

   @Override
   public synchronized void pause() {
      super.pause();
      Log.info("Paused new player protection for " + this.player.getName() + " because they entered an excluded world.");
      if (Conf.NEWBIE_BOSS_BAR_ENABLED.asBool() && this.countdownData != null) {
         this.stopBossBarDisplay();
      }
   }

   @Override
   public synchronized void resume() {
      super.resume();
      Log.info("Resumed new player protection for " + this.player.getName() + " because they left an excluded world.");
      if (Conf.NEWBIE_BOSS_BAR_ENABLED.asBool() && this.countdownData == null) {
         this.startBossBarDisplay();
      }
   }

   @Override
   public synchronized void cancel() {
      super.cancel();
      this.stopBossBarDisplay();
   }

   private void startBossBarDisplay() {
      DisplayManager displayManager = this.getDisplayManager();
      BossBarBuilder bossBar = BossBarBuilder.create()
         .barColor(Conf.NEWBIE_BOSS_BAR_COLOR.asEnum(BarColor.class))
         .barStyle(Conf.NEWBIE_BOSS_BAR_STYLE.asEnum(BarStyle.class));
      DisplayManager.TimeProgressSource timeProgressSource = new DisplayManager.TimeProgressSource() {
         @Override
         public long getGoal() {
            return NewbieTask.this.getDuration();
         }

         @Override
         public long getProgress() {
            return NewbieTask.this.getDuration() - NewbieTask.this.getTimeleft();
         }
      };
      CountdownData.Builder builder = new CountdownData.Builder().withBossBar(bossBar.build(), timeSource -> {
         long currentTimeLeft = this.getTimeleft();
         String currentFormattedTime = TimeUtil.getDiffDuration(Lang.class, currentTimeLeft);
         String message = Conf.NEWBIE_BOSS_BAR_MESSAGE.asString().replace("<time>", currentFormattedTime);
         return CombatUtils.processPlaceholders(this.player.getPlayer(), message);
      }).withTimeSource(timeProgressSource);
      this.countdownData = builder.build(this.player.getPlayer());
      displayManager.createCountdown(this.player.getPlayer(), this.countdownData);
   }

   private void stopBossBarDisplay() {
      if (this.countdownData != null) {
         DisplayManager displayManager = this.getDisplayManager();
         if (displayManager != null) {
            displayManager.cancelCountdown(this.player.getPlayer(), this.countdownData);
         }

         this.countdownData = null;
      }
   }

   private DisplayManager getDisplayManager() {
      return PvPManager.getInstance().getDisplayManager();
   }
}
