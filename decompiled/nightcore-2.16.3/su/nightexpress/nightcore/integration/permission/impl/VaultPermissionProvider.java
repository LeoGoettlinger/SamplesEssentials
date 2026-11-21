package su.nightexpress.nightcore.integration.permission.impl;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.integration.permission.PermissionProvider;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.ServerUtils;

public class VaultPermissionProvider implements PermissionProvider {
   @NonNull
   @Override
   public String getName() {
      return "Vault";
   }

   @NonNull
   private Optional<Permission> permission() {
      return ServerUtils.serviceProvider(Permission.class);
   }

   @NonNull
   private Optional<Chat> chat() {
      return ServerUtils.serviceProvider(Chat.class);
   }

   @NonNull
   private Optional<Player> onlinePlayer(@NonNull UUID playerId) {
      return Optional.ofNullable(Players.getPlayer(playerId));
   }

   @Nullable
   @Override
   public String getPrimaryGroup(@NonNull Player player) {
      Permission permission = this.permission().orElse(null);
      if (permission != null && permission.hasGroupSupport()) {
         String group = permission.getPrimaryGroup(player);
         return group == null ? null : LowerCase.USER_LOCALE.apply(group);
      } else {
         return null;
      }
   }

   @NonNull
   @Override
   public CompletableFuture<String> getPrimaryGroup(@NonNull UUID playerId) {
      return CompletableFuture.completedFuture(this.onlinePlayer(playerId).map(this::getPrimaryGroup).orElse(null));
   }

   @NonNull
   @Override
   public Set<String> getPermissionGroups(@NonNull Player player) {
      Permission permission = this.permission().orElse(null);
      if (permission != null && permission.hasGroupSupport()) {
         String[] groups = permission.getPlayerGroups(player);
         if (groups == null) {
            groups = new String[]{this.getPrimaryGroup(player)};
         }

         return Stream.of(groups).map(LowerCase.USER_LOCALE::apply).collect(Collectors.toSet());
      } else {
         return Collections.emptySet();
      }
   }

   @NonNull
   @Override
   public CompletableFuture<Set<String>> getPermissionGroups(@NonNull UUID playerId) {
      return CompletableFuture.completedFuture(this.onlinePlayer(playerId).map(this::getPermissionGroups).orElse(Collections.emptySet()));
   }

   @Nullable
   @Override
   public String getPrefix(@NonNull Player player) {
      return this.chat().map(chat -> chat.getPlayerPrefix(player)).orElse(null);
   }

   @NonNull
   @Override
   public CompletableFuture<String> getPrefix(@NonNull UUID playerId) {
      return CompletableFuture.completedFuture(this.onlinePlayer(playerId).map(this::getPrefix).orElse(null));
   }

   @Nullable
   @Override
   public String getSuffix(@NonNull Player player) {
      return this.chat().map(chat -> chat.getPlayerSuffix(player)).orElse(null);
   }

   @NonNull
   @Override
   public CompletableFuture<String> getSuffix(@NonNull UUID playerId) {
      return CompletableFuture.completedFuture(this.onlinePlayer(playerId).map(this::getSuffix).orElse(null));
   }
}
