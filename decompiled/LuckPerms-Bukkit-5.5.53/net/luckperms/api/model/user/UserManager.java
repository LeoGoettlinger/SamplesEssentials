package net.luckperms.api.model.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.luckperms.api.model.PlayerSaveResult;
import net.luckperms.api.node.HeldNode;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.matcher.NodeMatcher;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface UserManager {
   @NonNull
   CompletableFuture<User> loadUser(@NonNull UUID var1, @Nullable String var2);

   @NonNull
   default CompletableFuture<User> loadUser(@NonNull UUID uniqueId) {
      return this.loadUser(uniqueId, null);
   }

   @NonNull
   CompletableFuture<UUID> lookupUniqueId(@NonNull String var1);

   @NonNull
   CompletableFuture<String> lookupUsername(@NonNull UUID var1);

   @NonNull
   CompletableFuture<Void> saveUser(@NonNull User var1);

   @NonNull
   default CompletableFuture<Void> modifyUser(@NonNull UUID uniqueId, @NonNull Consumer<? super User> action) {
      return this.loadUser(uniqueId).thenApplyAsync(user -> {
         action.accept(user);
         return (User)user;
      }).thenCompose(this::saveUser);
   }

   @NonNull
   CompletableFuture<PlayerSaveResult> savePlayerData(@NonNull UUID var1, @NonNull String var2);

   @NonNull
   CompletableFuture<Void> deletePlayerData(@NonNull UUID var1);

   @NonNull
   CompletableFuture<Set<UUID>> getUniqueUsers();

   @NonNull
   <T extends Node> CompletableFuture<Map<UUID, Collection<T>>> searchAll(@NonNull NodeMatcher<? extends T> var1);

   @Deprecated
   @NonNull
   CompletableFuture<List<HeldNode<UUID>>> getWithPermission(@NonNull String var1);

   @Nullable
   User getUser(@NonNull UUID var1);

   @Nullable
   User getUser(@NonNull String var1);

   @NonNull
   @Unmodifiable
   Set<User> getLoadedUsers();

   boolean isLoaded(@NonNull UUID var1);

   void cleanupUser(@NonNull User var1);
}
