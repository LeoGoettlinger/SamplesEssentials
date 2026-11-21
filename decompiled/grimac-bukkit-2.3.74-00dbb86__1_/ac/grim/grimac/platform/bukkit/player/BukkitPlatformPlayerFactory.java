package ac.grim.grimac.platform.bukkit.player;

import ac.grim.grimac.platform.api.player.AbstractPlatformPlayerFactory;
import ac.grim.grimac.platform.api.player.OfflinePlatformPlayer;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class BukkitPlatformPlayerFactory extends AbstractPlatformPlayerFactory<Player> {
   protected Player getNativePlayer(@NotNull UUID uuid) {
      return Bukkit.getPlayer(uuid);
   }

   protected Player getNativePlayer(@NotNull String name) {
      return Bukkit.getPlayer(name);
   }

   protected PlatformPlayer createPlatformPlayer(@NotNull Player nativePlayer) {
      return new BukkitPlatformPlayer(nativePlayer);
   }

   protected UUID getPlayerUUID(@NotNull Player nativePlayer) {
      return nativePlayer.getUniqueId();
   }

   @Override
   protected Collection<Player> getNativeOnlinePlayers() {
      return Bukkit.getOnlinePlayers();
   }

   @Override
   public OfflinePlatformPlayer getOfflineFromUUID(@NotNull UUID uuid) {
      return new BukkitOfflinePlatformPlayer(Bukkit.getOfflinePlayer(uuid));
   }

   @Override
   public OfflinePlatformPlayer getOfflineFromName(@NotNull String name) {
      return new BukkitOfflinePlatformPlayer(Bukkit.getOfflinePlayer(name));
   }

   @Override
   public Collection<OfflinePlatformPlayer> getOfflinePlayers() {
      OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
      BukkitOfflinePlatformPlayer[] offlinePlatformPlayers = new BukkitOfflinePlatformPlayer[offlinePlayers.length];

      for (int i = 0; i < offlinePlayers.length; i++) {
         offlinePlatformPlayers[i] = new BukkitOfflinePlatformPlayer(offlinePlayers[i]);
      }

      return Arrays.asList(offlinePlatformPlayers);
   }
}
