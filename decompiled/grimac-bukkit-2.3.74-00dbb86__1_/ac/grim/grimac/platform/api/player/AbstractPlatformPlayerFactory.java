package ac.grim.grimac.platform.api.player;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractPlatformPlayerFactory<T> implements PlatformPlayerFactory {
   protected final PlatformPlayerCache cache = PlatformPlayerCache.getInstance();

   @Nullable
   @Override
   public final PlatformPlayer getFromUUID(@NotNull UUID uuid) {
      PlatformPlayer cachedPlayer = this.cache.getPlayer(uuid);
      if (cachedPlayer != null) {
         return cachedPlayer;
      } else {
         T nativePlayer = this.getNativePlayer(uuid);
         if (nativePlayer == null) {
            return null;
         } else {
            PlatformPlayer platformPlayer = this.createPlatformPlayer(nativePlayer);
            return this.cache.addOrGetPlayer(uuid, platformPlayer);
         }
      }
   }

   @Nullable
   @Override
   public PlatformPlayer getFromName(@NotNull String name) {
      T nativePlayer = this.getNativePlayer(name);
      if (nativePlayer == null) {
         return null;
      } else {
         PlatformPlayer platformPlayer = this.createPlatformPlayer(nativePlayer);
         return this.cache.addOrGetPlayer(platformPlayer.getUniqueId(), platformPlayer);
      }
   }

   @Override
   public final PlatformPlayer getFromNativePlayerType(@NotNull Object playerObject) {
      T nativePlayer = Objects.requireNonNull((T)playerObject);
      UUID uuid = this.getPlayerUUID(nativePlayer);
      PlatformPlayer cachedPlayer = this.cache.getPlayer(uuid);
      if (cachedPlayer != null) {
         return cachedPlayer;
      } else {
         PlatformPlayer platformPlayer = this.createPlatformPlayer(nativePlayer);
         return this.cache.addOrGetPlayer(uuid, platformPlayer);
      }
   }

   @Override
   public final void invalidatePlayer(@NotNull UUID uuid) {
      this.cache.removePlayer(uuid);
   }

   @Override
   public Collection<PlatformPlayer> getOnlinePlayers() {
      Collection<T> nativePlayers = this.getNativeOnlinePlayers();
      List<PlatformPlayer> platformPlayers = new ArrayList<>(nativePlayers.size());

      for (T nativePlayer : nativePlayers) {
         platformPlayers.add(this.getFromNativePlayerType(nativePlayer));
      }

      return platformPlayers;
   }

   public void replaceNativePlayer(@NotNull UUID uuid, @NotNull T player) {
   }

   protected abstract T getNativePlayer(@NotNull UUID var1);

   protected abstract T getNativePlayer(@NotNull String var1);

   protected abstract PlatformPlayer createPlatformPlayer(@NotNull T var1);

   protected abstract UUID getPlayerUUID(@NotNull T var1);

   protected abstract Collection<T> getNativeOnlinePlayers();

   @Override
   public abstract OfflinePlatformPlayer getOfflineFromUUID(@NotNull UUID var1);

   @Override
   public abstract OfflinePlatformPlayer getOfflineFromName(@NotNull String var1);
}
