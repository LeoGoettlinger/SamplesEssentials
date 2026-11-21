package me.chancesd.pvpmanager.tasks;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import me.chancesd.pvpmanager.event.PlayerTagEvent;
import me.chancesd.pvpmanager.event.PlayerUntagEvent;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import me.chancesd.pvpmanager.sdutils.display.BossBarBuilder;
import me.chancesd.pvpmanager.sdutils.display.CountdownData;
import me.chancesd.pvpmanager.sdutils.display.DisplayManager;
import me.chancesd.pvpmanager.sdutils.display.ProgressBar;
import me.chancesd.pvpmanager.sdutils.utils.Utils;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TagTask implements Listener {
   private final Map<CombatPlayer, CountdownData> taggedCountdowns = new ConcurrentHashMap<>();
   private final DisplayManager display;
   private BossBarBuilder bossBar;

   public TagTask(DisplayManager display) {
      this.display = display;
      if (Conf.BOSS_BAR_ENABLED.asBool()) {
         this.bossBar = BossBarBuilder.create().barColor(Conf.BOSS_BAR_COLOR.asEnum(BarColor.class)).barStyle(Conf.BOSS_BAR_STYLE.asEnum(BarStyle.class));
      }
   }

   public final boolean cancel() {
      for (Entry<CombatPlayer, CountdownData> entry : this.taggedCountdowns.entrySet()) {
         CombatPlayer combatPlayer = entry.getKey();
         if (combatPlayer.isInCombat()) {
            combatPlayer.untag(UntagReason.PLUGIN_DISABLE);
         }
      }

      return false;
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.MONITOR
   )
   public final void onPlayerTag(PlayerTagEvent event) {
      this.startTracking(event.getCombatPlayer());
   }

   @EventHandler
   public final void onPlayerUntag(PlayerUntagEvent event) {
      this.stopTracking(event.getCombatPlayer(), event.getReason());
   }

   private final void startTracking(final CombatPlayer combatPlayer) {
      ProgressBar progressBar = new ProgressBar(
         Conf.ACTION_BAR_MESSAGE.asString(), Conf.ACTION_BAR_BARS.asInt(), combatPlayer.getTotalTagTime(), Conf.ACTION_BAR_SYMBOL.asString()
      );
      CountdownData.Builder builder = new CountdownData.Builder();
      if (Conf.ACTION_BAR_ENABLED.asBool()) {
         builder.withActionBar(progressBar, timeSource -> {
            String message = CombatUtils.processPlaceholders(combatPlayer.getPlayer(), progressBar.getMessage());
            combatPlayer.sendActionBar(message);
            return message;
         });
      }

      if (Conf.BOSS_BAR_ENABLED.asBool()) {
         builder.withBossBar(this.bossBar.build(), timeSource -> {
            double secondsRemaining = (timeSource.getGoal() - timeSource.getProgress()) / 1000.0;
            String message = Conf.BOSS_BAR_MESSAGE.asString().replace("<time>", Double.toString(Utils.roundTo1Decimal(secondsRemaining)));
            return CombatUtils.processPlaceholders(combatPlayer.getPlayer(), message);
         });
      }

      DisplayManager.TimeProgressSource timeProgressSource = new DisplayManager.TimeProgressSource() {
         @Override
         public long getGoal() {
            return combatPlayer.getTotalTagTime();
         }

         @Override
         public long getProgress() {
            return System.currentTimeMillis() - combatPlayer.getTaggedTime();
         }
      };
      CountdownData countdownData = builder.withTimeSource(timeProgressSource)
         .onFinish(() -> combatPlayer.untag(UntagReason.TIME_EXPIRED))
         .build(combatPlayer.getPlayer());
      this.display.createCountdown(combatPlayer.getPlayer(), countdownData);
      this.taggedCountdowns.put(combatPlayer, countdownData);
   }

   private final void stopTracking(CombatPlayer combatPlayer, UntagReason reason) {
      CountdownData countdownData = this.taggedCountdowns.remove(combatPlayer);
      if (reason != UntagReason.TIME_EXPIRED) {
         this.display.cancelCountdown(combatPlayer.getPlayer(), countdownData);
      }
   }

   public Set<CombatPlayer> getTaggedPlayers() {
      return this.taggedCountdowns.keySet();
   }
}
