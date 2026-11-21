package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

public interface SessionTracker {
   SessionTracker NOOP = new SessionTracker() {
      @NotNull
      @Override
      public UUID observeActivity(@NotNull UUID p, long n, @NotNull SessionTracker.ClientMeta m) {
         return UUID.randomUUID();
      }

      @Override
      public void pollHeartbeat(@NotNull UUID p, long n) {
      }

      @Override
      public void close(@NotNull UUID p, long n, @NotNull SessionTracker.ClientMeta m) {
      }

      @Nullable
      @Override
      public UUID currentSessionId(@NotNull UUID p) {
         return null;
      }
   };

   @NotNull
   UUID observeActivity(@NotNull UUID var1, long var2, @NotNull SessionTracker.ClientMeta var4);

   void pollHeartbeat(@NotNull UUID var1, long var2);

   void close(@NotNull UUID var1, long var2, @NotNull SessionTracker.ClientMeta var4);

   @Nullable
   UUID currentSessionId(@NotNull UUID var1);

   public record ClientMeta(String grimVersion, String clientBrand, int clientVersion, String serverVersion) {
      public static SessionTracker.ClientMeta empty() {
         return new SessionTracker.ClientMeta(null, null, -1, null);
      }
   }
}
