package ch.njol.skript.update;

import java.util.concurrent.CompletableFuture;

public interface UpdateChecker {
   CompletableFuture<UpdateManifest> check(ReleaseManifest var1, ReleaseChannel var2);
}
