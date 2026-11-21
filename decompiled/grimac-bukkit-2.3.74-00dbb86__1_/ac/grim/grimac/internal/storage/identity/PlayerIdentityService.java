package ac.grim.grimac.internal.storage.identity;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.UUID;

@ApiStatus.Internal
public final class PlayerIdentityService {
   private final DataStore store;

   public PlayerIdentityService(DataStore store) {
      this.store = store;
   }

   public void observe(UUID uuid, String name, long epochMs) {
      this.store.submit(Categories.PLAYER_IDENTITY, e -> e.uuid(uuid).currentName(name).firstSeenEpochMs(epochMs).lastSeenEpochMs(epochMs));
   }
}
