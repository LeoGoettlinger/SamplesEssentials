package org.evenmorefish.fish.libs.daisylib.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CommandUtils {
   public static final SimpleCommandExceptionType PLAYER_REQUIRED = new SimpleCommandExceptionType(new LiteralMessage("Only players can use this command."));
   public static final SimpleCommandExceptionType ONLY_ONE_TARGET = new SimpleCommandExceptionType(
      new LiteralMessage("Multiple targets selected when only one is required.")
   );
   public static final SimpleCommandExceptionType INVALID_TARGET = new SimpleCommandExceptionType(new LiteralMessage("Invalid target."));

   @NonNull
   public static Player requirePlayer(@Nullable CommandSourceStack source) throws CommandSyntaxException {
      if (source == null) {
         throw PLAYER_REQUIRED.create();
      } else if (source.getSender() instanceof Player player) {
         return player;
      } else {
         throw PLAYER_REQUIRED.create();
      }
   }

   @NonNull
   public static Player requirePlayer(@Nullable CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      if (context == null) {
         throw PLAYER_REQUIRED.create();
      } else {
         return requirePlayer((CommandSourceStack)context.getSource());
      }
   }

   public static Predicate<CommandSourceStack> playerPredicate(@NonNull Predicate<Player> playerPredicate) {
      return sender -> sender.getSender() instanceof Player player ? playerPredicate.test(player) : false;
   }

   @NonNull
   public static List<OfflinePlayer> parsePlayerProfilesArgument(@NonNull CommandSourceStack stack, @Nullable PlayerProfileListResolver resolver) throws CommandSyntaxException {
      return resolver == null
         ? List.of()
         : ((Collection)resolver.resolve(stack))
            .stream()
            .<UUID>map(PlayerProfile::getId)
            .filter(Objects::nonNull)
            .<OfflinePlayer>map(Bukkit::getOfflinePlayer)
            .filter(player -> player.getFirstPlayed() != 0L)
            .toList();
   }

   @Nullable
   public static OfflinePlayer parsePlayerProfileArgument(@NonNull CommandSourceStack stack, @Nullable PlayerProfileListResolver resolver) throws CommandSyntaxException {
      if (resolver == null) {
         return null;
      } else {
         List<PlayerProfile> profiles = List.copyOf((Collection<? extends PlayerProfile>)resolver.resolve(stack));
         if (profiles.size() != 1) {
            throw ONLY_ONE_TARGET.create();
         } else {
            UUID uuid = profiles.getFirst().getId();
            if (uuid == null) {
               throw INVALID_TARGET.create();
            } else {
               OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
               if (player.getFirstPlayed() == 0L) {
                  throw INVALID_TARGET.create();
               } else {
                  return player;
               }
            }
         }
      }
   }

   public static List<Player> parsePlayersArgument(@NonNull CommandSourceStack stack, @Nullable PlayerSelectorArgumentResolver resolver) throws CommandSyntaxException {
      return resolver == null ? List.of() : (List)resolver.resolve(stack);
   }

   public static Player parsePlayerArgument(@NonNull CommandSourceStack stack, @Nullable PlayerSelectorArgumentResolver resolver) throws CommandSyntaxException {
      return resolver == null ? null : (Player)((List)resolver.resolve(stack)).getFirst();
   }

   @NonNull
   public static <T> T getArgumentOrDefault(@NonNull CommandContext<CommandSourceStack> ctx, @NonNull String name, @NonNull Class<T> clazz, @NonNull T def) {
      try {
         return (T)ctx.getArgument(name, clazz);
      } catch (Exception var5) {
         return def;
      }
   }

   @Nullable
   public static <T> T getArgumentOrNull(@NonNull CommandContext<CommandSourceStack> ctx, @NonNull String name, @NonNull Class<T> clazz) {
      try {
         return (T)ctx.getArgument(name, clazz);
      } catch (Exception var4) {
         return null;
      }
   }
}
