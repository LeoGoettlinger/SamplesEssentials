package com.skyblockexp.ezrtp.listener;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.teleport.OnJoinTeleportSettings;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import java.util.Objects;
import java.util.function.Supplier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerJoinTeleportListener implements Listener {
   private final JavaPlugin plugin;
   private final PlatformScheduler scheduler;
   private final Supplier<RandomTeleportService> teleportServiceSupplier;
   private final Supplier<RandomTeleportSettings> settingsSupplier;

   public PlayerJoinTeleportListener(
      JavaPlugin plugin,
      PlatformScheduler scheduler,
      Supplier<RandomTeleportService> teleportServiceSupplier,
      Supplier<RandomTeleportSettings> settingsSupplier
   ) {
      this.plugin = plugin;
      this.scheduler = scheduler;
      this.teleportServiceSupplier = teleportServiceSupplier;
      this.settingsSupplier = settingsSupplier;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      RandomTeleportSettings settings = this.settingsSupplier.get();
      if (settings != null) {
         OnJoinTeleportSettings onJoin = settings.getOnJoinTeleportSettings();
         if (onJoin.isEnabled()) {
            Player player = event.getPlayer();
            if (!onJoin.onlyFirstJoin() || !player.hasPlayedBefore()) {
               if (onJoin.bypassPermission().isEmpty() || !player.hasPermission(onJoin.bypassPermission())) {
                  RandomTeleportService service = Objects.requireNonNull(this.teleportServiceSupplier.get(), "Teleport service not initialised");
                  long delay = Math.max(0L, onJoin.delayTicks());
                  this.scheduler.executeGlobalDelayed(() -> service.teleportPlayer(player, TeleportReason.JOIN), delay);
               }
            }
         }
      }
   }

   public void updateSettings(RandomTeleportSettings newSettings) {
   }
}
