package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeleportPreCheckHandler {
   private final JavaPlugin plugin;
   private final PlatformScheduler scheduler;
   private final MessageProvider messageProvider;
   private final TeleportCostCalculator costCalculator;

   public TeleportPreCheckHandler(JavaPlugin plugin, PlatformScheduler scheduler, MessageProvider messageProvider, TeleportCostCalculator costCalculator) {
      this.plugin = plugin;
      this.scheduler = scheduler;
      this.messageProvider = messageProvider;
      this.costCalculator = costCalculator;
   }

   public boolean checkMemorySafety(Player player, Consumer<Boolean> callback) {
      Runtime runtime = Runtime.getRuntime();
      long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1048576L;
      long totalMemoryMb = runtime.totalMemory() / 1048576L;
      if (freeMemoryMb < 128L) {
         MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORTING, player));
         this.scheduler.executeGlobalDelayed(() -> {
            MessageUtil.send(player, this.messageProvider.format(MessageKey.WORLD_MISSING, Map.of("world", "Server memory too low for teleport"), player));
            if (callback != null) {
               callback.accept(false);
            }
         }, 20L);
         this.plugin.getLogger().warning(String.format("Teleport cancelled due to critically low memory: %dMB free / %dMB total", freeMemoryMb, totalMemoryMb));
         return false;
      } else {
         return true;
      }
   }

   public boolean checkPayment(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason) {
      double resolvedCost = this.costCalculator.calculateCost(player, teleportSettings);
      if (this.costCalculator.requiresPayment(reason, resolvedCost) && !this.costCalculator.hasBalance(player, resolvedCost)) {
         if (!teleportSettings.isSuppressPlayerMessages()) {
            MessageUtil.send(
               player, this.messageProvider.format(MessageKey.INSUFFICIENT_FUNDS, Map.of("cost", String.format(Locale.US, "%.2f", resolvedCost)), player)
            );
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean processPayment(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason) {
      double resolvedCost = this.costCalculator.calculateCost(player, teleportSettings);
      if (this.costCalculator.requiresPayment(reason, resolvedCost) && !this.costCalculator.withdraw(player, resolvedCost)) {
         MessageUtil.send(
            player, this.messageProvider.format(MessageKey.INSUFFICIENT_FUNDS, Map.of("cost", String.format(Locale.US, "%.2f", resolvedCost)), player)
         );
         return false;
      } else {
         return true;
      }
   }
}
