package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

public interface PlayerToggleStore {
   String KEY_ALERTS = "alerts";
   String KEY_VERBOSE = "verbose";
   String KEY_BRANDS = "brands";
   PlayerToggleStore NOOP = new PlayerToggleStore() {
      @Override
      public void prefetch(@NotNull UUID uuid) {
      }

      @Nullable
      @Override
      public Boolean current(@NotNull UUID uuid, @NotNull String key) {
         return null;
      }

      @Override
      public void applyPermissionDefault(@NotNull UUID uuid, @NotNull String key, boolean value) {
      }

      @Override
      public void applyUserToggle(@NotNull UUID uuid, @NotNull String key, boolean value) {
      }

      @Override
      public void evict(@NotNull UUID uuid) {
      }

      @Override
      public void shutdown() {
      }
   };

   void prefetch(@NotNull UUID var1);

   @Nullable
   Boolean current(@NotNull UUID var1, @NotNull String var2);

   void applyPermissionDefault(@NotNull UUID var1, @NotNull String var2, boolean var3);

   void applyUserToggle(@NotNull UUID var1, @NotNull String var2, boolean var3);

   void evict(@NotNull UUID var1);

   void shutdown();
}
