package ac.grim.grimac.platform.api.player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformPlayerCache {
   private static final PlatformPlayerCache INSTANCE = new PlatformPlayerCache();
   private final Map<UUID, PlatformPlayer> playerCache = new ConcurrentHashMap<>();

   private PlatformPlayerCache() {
   }

   public static PlatformPlayerCache getInstance() {
      return INSTANCE;
   }

   public PlatformPlayer addOrGetPlayer(UUID uuid, PlatformPlayer player) {
      return this.playerCache.compute(uuid, (key, existing) -> (PlatformPlayer)(existing != null ? existing : player));
   }

   public void removePlayer(UUID uuid) {
      this.playerCache.remove(uuid);
   }

   public PlatformPlayer getPlayer(UUID uuid) {
      return this.playerCache.get(uuid);
   }
}
