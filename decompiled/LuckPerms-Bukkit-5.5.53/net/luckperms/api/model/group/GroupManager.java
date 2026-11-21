package net.luckperms.api.model.group;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.luckperms.api.node.HeldNode;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.matcher.NodeMatcher;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface GroupManager {
   @NonNull
   CompletableFuture<Group> createAndLoadGroup(@NonNull String var1);

   @NonNull
   CompletableFuture<Optional<Group>> loadGroup(@NonNull String var1);

   @NonNull
   CompletableFuture<Void> saveGroup(@NonNull Group var1);

   @NonNull
   CompletableFuture<Void> deleteGroup(@NonNull Group var1);

   @NonNull
   default CompletableFuture<Void> modifyGroup(@NonNull String name, @NonNull Consumer<? super Group> action) {
      return this.createAndLoadGroup(name).thenApplyAsync(group -> {
         action.accept(group);
         return (Group)group;
      }).thenCompose(this::saveGroup);
   }

   @NonNull
   CompletableFuture<Void> loadAllGroups();

   @NonNull
   <T extends Node> CompletableFuture<Map<String, Collection<T>>> searchAll(@NonNull NodeMatcher<? extends T> var1);

   @Deprecated
   @NonNull
   CompletableFuture<List<HeldNode<String>>> getWithPermission(@NonNull String var1);

   @Nullable
   Group getGroup(@NonNull String var1);

   @NonNull
   @Unmodifiable
   Set<Group> getLoadedGroups();

   boolean isLoaded(@NonNull String var1);
}
