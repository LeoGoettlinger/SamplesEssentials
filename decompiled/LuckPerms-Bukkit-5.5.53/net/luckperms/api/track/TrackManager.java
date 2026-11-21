package net.luckperms.api.track;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface TrackManager {
   @NonNull
   CompletableFuture<Track> createAndLoadTrack(@NonNull String var1);

   @NonNull
   CompletableFuture<Optional<Track>> loadTrack(@NonNull String var1);

   @NonNull
   CompletableFuture<Void> saveTrack(@NonNull Track var1);

   @NonNull
   CompletableFuture<Void> deleteTrack(@NonNull Track var1);

   @NonNull
   default CompletableFuture<Void> modifyTrack(@NonNull String name, @NonNull Consumer<? super Track> action) {
      return this.createAndLoadTrack(name).thenApplyAsync(track -> {
         action.accept(track);
         return (Track)track;
      }).thenCompose(this::saveTrack);
   }

   @NonNull
   CompletableFuture<Void> loadAllTracks();

   @Nullable
   Track getTrack(@NonNull String var1);

   @NonNull
   @Unmodifiable
   Set<Track> getLoadedTracks();

   boolean isLoaded(@NonNull String var1);
}
