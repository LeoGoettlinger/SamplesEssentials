package su.nightexpress.nightcore.integration.permission.impl;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.integration.permission.PermissionProvider;
import su.nightexpress.nightcore.util.LowerCase;

public class LuckPermissionProvider implements PermissionProvider {
   @NonNull
   @Override
   public String getName() {
      return "LuckPerms";
   }

   private LuckPerms api() {
      return LuckPermsProvider.get();
   }

   @Nullable
   private User getUser(@NonNull Player player) {
      return this.api().getUserManager().getUser(player.getUniqueId());
   }

   @NonNull
   private CompletableFuture<User> loadUser(@NonNull UUID playerId) {
      return this.api().getUserManager().loadUser(playerId);
   }

   @Nullable
   @Override
   public String getPrimaryGroup(@NonNull Player player) {
      return this.getPrimaryGroup(this.getUser(player));
   }

   @NonNull
   @Override
   public CompletableFuture<String> getPrimaryGroup(@NonNull UUID playerId) {
      return this.loadUser(playerId).thenApplyAsync(this::getPrimaryGroup);
   }

   @Nullable
   private String getPrimaryGroup(@Nullable User user) {
      if (user == null) {
         return null;
      } else {
         String group = user.getPrimaryGroup();
         return LowerCase.USER_LOCALE.apply(group);
      }
   }

   @NonNull
   @Override
   public Set<String> getPermissionGroups(@NonNull Player player) {
      return this.getPermissionGroups(this.getUser(player));
   }

   @NonNull
   @Override
   public CompletableFuture<Set<String>> getPermissionGroups(@NonNull UUID playerId) {
      return this.loadUser(playerId).thenApplyAsync(this::getPermissionGroups);
   }

   @NonNull
   private Set<String> getPermissionGroups(@Nullable User user) {
      return user == null
         ? Collections.emptySet()
         : user.getNodes(NodeType.INHERITANCE)
            .stream()
            .<String>map(InheritanceNode::getGroupName)
            .map(LowerCase.USER_LOCALE::apply)
            .collect(Collectors.toSet());
   }

   @Nullable
   @Override
   public String getPrefix(@NonNull Player player) {
      return this.getPrefix(this.getUser(player));
   }

   @NonNull
   @Override
   public CompletableFuture<String> getPrefix(@NonNull UUID playerId) {
      return this.loadUser(playerId).thenApplyAsync(this::getPrefix);
   }

   @Nullable
   public String getPrefix(@Nullable User user) {
      return user == null ? null : user.getCachedData().getMetaData().getPrefix();
   }

   @Nullable
   @Override
   public String getSuffix(@NonNull Player player) {
      return this.getSuffix(this.getUser(player));
   }

   @NonNull
   @Override
   public CompletableFuture<String> getSuffix(@NonNull UUID playerId) {
      return this.loadUser(playerId).thenApplyAsync(this::getSuffix);
   }

   @Nullable
   public String getSuffix(@Nullable User user) {
      return user == null ? null : user.getCachedData().getMetaData().getSuffix();
   }
}
