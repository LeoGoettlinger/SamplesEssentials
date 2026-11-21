package me.clip.placeholderapi.commands.impl.local;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.commands.PlaceholderCommand;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public final class CommandParse extends PlaceholderCommand {
   public CommandParse() {
      super("parse", "bcparse", "parserel", "cmdparse");
   }

   @Override
   public void evaluate(@NotNull PlaceholderAPIPlugin plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
      String var5 = alias.toLowerCase(Locale.ROOT);
      switch (var5) {
         case "parserel":
            this.evaluateParseRelation(sender, params);
            break;
         case "parse":
            this.evaluateParseSingular(sender, params, false, false);
            break;
         case "bcparse":
            this.evaluateParseSingular(sender, params, true, false);
            break;
         case "cmdparse":
            this.evaluateParseSingular(sender, params, false, true);
      }
   }

   @Override
   public void complete(
      @NotNull PlaceholderAPIPlugin plugin,
      @NotNull CommandSender sender,
      @NotNull String alias,
      @NotNull @Unmodifiable List<String> params,
      @NotNull List<String> suggestions
   ) {
      String var6 = alias.toLowerCase(Locale.ROOT);
      switch (var6) {
         case "parserel":
            this.completeParseRelation(params, suggestions);
            break;
         case "parse":
         case "bcparse":
         case "cmdparse":
            this.completeParseSingular(sender, params, suggestions);
      }
   }

   private void evaluateParseSingular(@NotNull CommandSender sender, @NotNull @Unmodifiable List<String> params, boolean broadcast, boolean command) {
      if (params.size() < 2) {
         Msg.msg(
            sender,
            "&cYou must provide a target and message: &b/papi " + (command ? "cmdparse" : (broadcast ? "bcparse" : "parse")) + " &7{target} &a{message}"
         );
      } else {
         OfflinePlayer player;
         if ("me".equalsIgnoreCase(params.get(0))) {
            if (!(sender instanceof Player)) {
               Msg.msg(sender, "&cYou must be a player to use &7me&c as a target!");
               return;
            }

            player = (Player)sender;
         } else if ("--null".equalsIgnoreCase(params.get(0))) {
            player = null;
         } else {
            OfflinePlayer target = this.resolvePlayer(params.get(0));
            if (target == null) {
               Msg.msg(sender, "&cFailed to find player: &7" + params.get(0));
               return;
            }

            player = target;
         }

         String message = PlaceholderAPI.setPlaceholders(player, String.join(" ", params.subList(1, params.size())));
         if (command) {
            Bukkit.dispatchCommand(sender, message);
         } else {
            if (broadcast) {
               Bukkit.broadcastMessage(message);
            } else {
               sender.sendMessage(message);
            }
         }
      }
   }

   private void evaluateParseRelation(@NotNull CommandSender sender, @NotNull @Unmodifiable List<String> params) {
      if (params.size() < 3) {
         Msg.msg(sender, "&cYou must supply two targets, and a message: &b/papi parserel &7{target one} {target two} &a{message}");
      } else {
         OfflinePlayer playerOne;
         if ("me".equalsIgnoreCase(params.get(0))) {
            if (!(sender instanceof Player)) {
               Msg.msg(sender, "&cYou must be a player to use &7me&c as a target!");
               return;
            }

            playerOne = (Player)sender;
         } else {
            playerOne = this.resolvePlayer(params.get(0));
         }

         if (playerOne != null && playerOne.isOnline()) {
            OfflinePlayer playerTwo;
            if ("me".equalsIgnoreCase(params.get(1))) {
               if (!(sender instanceof Player)) {
                  Msg.msg(sender, "&cYou must be a player to use &7me&c as a target!");
                  return;
               }

               playerTwo = (Player)sender;
            } else {
               playerTwo = this.resolvePlayer(params.get(1));
            }

            if (playerTwo != null && playerTwo.isOnline()) {
               String message = PlaceholderAPI.setRelationalPlaceholders(
                  (Player)playerOne, (Player)playerTwo, String.join(" ", params.subList(2, params.size()))
               );
               sender.sendMessage(message);
            } else {
               Msg.msg(sender, "&cFailed to find player: &f" + params.get(1));
            }
         } else {
            Msg.msg(sender, "&cFailed to find player: &f" + params.get(0));
         }
      }
   }

   private void completeParseSingular(@NotNull CommandSender sender, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
      if (params.size() > 1) {
         String name = params.get(params.size() - 1);
         if (name.startsWith("%") && !name.endsWith("%")) {
            int index = name.indexOf(95);
            if (index != -1) {
               PlaceholderExpansion expansion = PlaceholderAPIPlugin.getInstance()
                  .getLocalExpansionManager()
                  .findExpansionByIdentifier(name.substring(1, index))
                  .orElse(null);
               if (expansion != null) {
                  Set<String> possible = new HashSet<>(expansion.getPlaceholders());
                  PlaceholderAPIPlugin.getInstance()
                     .getCloudExpansionManager()
                     .findCloudExpansionByName(expansion.getName())
                     .ifPresent(cloud -> possible.addAll(cloud.getPlaceholders()));
                  suggestByParameter(possible.stream(), suggestions, params.get(params.size() - 1));
               }
            }
         }
      } else {
         if (sender instanceof Player && (params.isEmpty() || "me".startsWith(params.get(0).toLowerCase(Locale.ROOT)))) {
            suggestions.add("me");
         }

         if ("--null".startsWith(params.get(0).toLowerCase(Locale.ROOT))) {
            suggestions.add("--null");
         }

         Stream<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName);
         suggestByParameter(names, suggestions, params.isEmpty() ? null : params.get(0));
      }
   }

   private void completeParseRelation(@NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
      if (params.size() <= 2) {
         Stream<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName);
         suggestByParameter(names, suggestions, params.isEmpty() ? null : params.get(params.size() - 1));
      }
   }

   @Nullable
   private OfflinePlayer resolvePlayer(@NotNull String name) {
      OfflinePlayer target = Bukkit.getPlayerExact(name);
      if (target == null) {
         target = Bukkit.getOfflinePlayer(name);
         return target.hasPlayedBefore() ? target : null;
      } else {
         return target;
      }
   }
}
