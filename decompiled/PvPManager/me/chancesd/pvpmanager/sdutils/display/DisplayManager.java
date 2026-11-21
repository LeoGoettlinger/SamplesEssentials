package me.chancesd.pvpmanager.sdutils.display;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class DisplayManager implements Runnable {
   private static final String COUNTDOWN_PREFIX = "countdown_";
   private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(
      new ThreadFactoryBuilder().setNameFormat("Display Thread - %d").build()
   );
   protected final Map<Player, List<CountdownData>> countdowns = new ConcurrentHashMap<>();
   protected final Map<Player, Map<String, BossBar>> allBossBars = new ConcurrentHashMap<>();
   private ScheduledFuture<?> timer;

   public void createCountdown(Player player, CountdownData countdownData) {
      this.countdowns.computeIfAbsent(player, k -> new CopyOnWriteArrayList<>()).add(countdownData);
      if (countdownData.bossBar != null) {
         this.showBossBar(player, "countdown_" + countdownData.hashCode(), countdownData.bossBar);
      }

      if (this.timer == null) {
         this.timer = EXECUTOR.scheduleAtFixedRate(new ScheduleUtils.ExceptionRunnable(this), 0L, 100L, TimeUnit.MILLISECONDS);
      }
   }

   public void showBossBar(Player player, String key, BossBar bossBar) {
      Map<String, BossBar> playerBars = this.allBossBars.computeIfAbsent(player, k -> new ConcurrentHashMap<>());
      BossBar existing = playerBars.remove(key);
      this.removePlayerFromBossBar(existing, player);
      bossBar.addPlayer(player);
      playerBars.put(key, bossBar);
   }

   public void hideBossBar(Player player, String key) {
      Map<String, BossBar> playerBars = this.allBossBars.get(player);
      if (playerBars != null) {
         BossBar bossBar = playerBars.remove(key);
         this.removePlayerFromBossBar(bossBar, player);
         if (playerBars.isEmpty()) {
            this.allBossBars.remove(player);
         }
      }
   }

   public void hideAllBossBars(Player player) {
      Map<String, BossBar> playerBars = this.allBossBars.remove(player);
      if (playerBars != null) {
         playerBars.values().forEach(bar -> bar.removePlayer(player));
      }
   }

   public void cancelCountdown(Player player, CountdownData countdownData) {
      List<CountdownData> dataList = this.countdowns.get(player);
      if (dataList != null && dataList.remove(countdownData)) {
         if (countdownData.bossBar != null) {
            this.hideBossBar(player, "countdown_" + countdownData.hashCode());
         }

         if (dataList.isEmpty()) {
            this.countdowns.remove(player);
         }
      }
   }

   public void cancelAllCountdown(Player player) {
      List<CountdownData> dataList = this.countdowns.remove(player);
      if (dataList == null) {
         throw new IllegalStateException("Tried to discard countdowns that don't exist!");
      } else {
         for (CountdownData data : dataList) {
            if (data.bossBar != null) {
               this.hideBossBar(player, "countdown_" + data.hashCode());
            }
         }
      }
   }

   @Override
   public void run() {
      Iterator<Entry<Player, List<CountdownData>>> it = this.countdowns.entrySet().iterator();

      while (it.hasNext()) {
         Entry<Player, List<CountdownData>> entry = it.next();
         List<CountdownData> dataList = entry.getValue();
         Player player = entry.getKey();
         dataList.removeIf(data -> {
            boolean shouldRemove = data.update();
            if (shouldRemove && data.bossBar != null) {
               this.hideBossBar(player, "countdown_" + data.hashCode());
            }

            return shouldRemove;
         });
         if (dataList.isEmpty()) {
            it.remove();
         }
      }

      if (this.countdowns.isEmpty() && this.timer != null) {
         this.timer.cancel(false);
         this.timer = null;
      }
   }

   private void removePlayerFromBossBar(BossBar bossBar, Player player) {
      if (bossBar != null) {
         bossBar.removePlayer(player);
      }
   }

   public void cleanup() {
      this.allBossBars.forEach((player, bars) -> bars.values().forEach(bar -> bar.removePlayer(player)));
      this.allBossBars.clear();
      this.countdowns.clear();
   }

   public interface MessageSource {
      String getMessage(DisplayManager.TimeProgressSource var1);
   }

   public interface TimeProgressSource {
      long getProgress();

      long getGoal();
   }
}
