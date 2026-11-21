package ch.njol.skript.update;

import java.util.concurrent.CompletableFuture;

public class NoUpdateChecker implements UpdateChecker {
   @Override
   public CompletableFuture<UpdateManifest> check(ReleaseManifest manifest, ReleaseChannel channel) {
      return CompletableFuture.completedFuture(null);
   }
}
