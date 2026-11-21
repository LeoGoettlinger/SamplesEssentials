package net.thenextlvl.version;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;

public interface VersionChecker<N, V extends Version> {
   @Contract(
      pure = true
   )
   V getVersionRunning();

   @Contract(
      value = "_ -> new",
      pure = true
   )
   V parseVersion(N var1);

   @Contract(
      value = "_ -> new",
      pure = true
   )
   V parseVersion(String var1);

   @Contract(
      pure = true
   )
   boolean isSupported(N var1);

   @CheckReturnValue
   CompletableFuture<V> retrieveLatestVersion();

   @CheckReturnValue
   CompletableFuture<Optional<V>> retrieveLatestSupportedVersion();

   @CheckReturnValue
   CompletableFuture<Set<V>> retrieveVersions();

   @CheckReturnValue
   Set<V> getSupportedVersions();

   @CheckReturnValue
   Set<V> getVersions();

   @CheckReturnValue
   Optional<V> getLatestSupportedVersion();

   @CheckReturnValue
   Optional<V> getLatestVersion();
}
