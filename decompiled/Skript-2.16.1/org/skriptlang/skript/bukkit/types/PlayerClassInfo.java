package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Internal
public class PlayerClassInfo extends ClassInfo<Player> {
   public PlayerClassInfo() {
      super(Player.class, "player");
      this.user(new String[]{"players?"})
         .name("Player")
         .description(
            "A player. Depending on whether a player is online or offline several actions can be performed with them, though you won't get any errors when using effects that only work if the player is online (e.g. changing their inventory) on an offline player.",
            "You have two possibilities to use players as command arguments: <player> and <offline player>. The first requires that the player is online and also accepts only part of the name, while the latter doesn't require that the player is online, but the player's name has to be entered exactly."
         )
         .usage(
            "Parsing an offline player as a player (online) will return nothing (none), for that case you would need to parse as offlineplayer which only returns nothing (none) if player doesn't exist in Minecraft databases (name not taken) otherwise it will return the player regardless of their online status."
         )
         .examples(
            "set {_p} to \"Notch\" parsed as a player # returns <none> unless Notch is actually online or starts with Notch like Notchan",
            "set {_p} to \"N\" parsed as a player # returns Notch if Notch is online because their name starts with 'N' (case insensitive) however, it would return nothing if no player whose name starts with 'N' is online."
         )
         .since("1.0")
         .defaultExpression(new EventValueExpression<>(Player.class))
         .after("string", "world")
         .parser(new PlayerClassInfo.PlayerParser())
         .changer(new PlayerClassInfo.PlayerChanger())
         .property(
            Property.NAME,
            "A player's account/true name, as text. Cannot be changed.",
            Skript.instance(),
            ExpressionPropertyHandler.of(CommandSender::name, Component.class)
         )
         .property(
            Property.DISPLAY_NAME,
            "The player's display name, as text. Can be set or reset.",
            Skript.instance(),
            new PlayerClassInfo.PlayerDisplayNameHandler()
         )
         .serializeAs(OfflinePlayer.class);
   }

   public static class PlayerChanger implements Changer<Player> {
      private static final Changer<Entity> ENTITY_CHANGER = new EntityClassInfo.EntityChanger();

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return mode == Changer.ChangeMode.DELETE ? null : ENTITY_CHANGER.acceptChange(mode);
      }

      public void change(Player[] players, @Nullable Object[] delta, Changer.ChangeMode mode) {
         ENTITY_CHANGER.change(players, delta, mode);
      }
   }

   public static class PlayerDisplayNameHandler implements ExpressionPropertyHandler<Player, Component> {
      public Component convert(Player player) {
         return player.displayName();
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case SET, RESET -> (Class[])CollectionUtils.array(Component.class);
            default -> null;
         };
      }

      public void change(Player player, @Nullable Object[] delta, Changer.ChangeMode mode) {
         Component name = delta == null ? null : (Component)delta[0];
         player.displayName(name);
      }

      @NotNull
      @Override
      public Class<Component> returnType() {
         return Component.class;
      }
   }

   public static class PlayerParser extends Parser<Player> {
      @Nullable
      public Player parse(String string, ParseContext context) {
         if (context != ParseContext.COMMAND && context != ParseContext.PARSE) {
            assert false;

            return null;
         } else if (string.isEmpty()) {
            return null;
         } else if (Utils.isValidUUID(string)) {
            return Bukkit.getPlayer(UUID.fromString(string));
         } else {
            String name = string.toLowerCase(Locale.ENGLISH);
            int nameLength = name.length();
            List<Player> players = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
               if (player.getName().toLowerCase(Locale.ENGLISH).startsWith(name)) {
                  if (player.getName().length() == nameLength) {
                     return player;
                  }

                  players.add(player);
               }
            }

            if (players.size() == 1) {
               return players.get(0);
            } else {
               if (players.isEmpty()) {
                  Skript.error(String.format(Language.get("commands.no player starts with"), string));
               } else {
                  Skript.error(String.format(Language.get("commands.multiple players start with"), string));
               }

               return null;
            }
         }
      }

      @Override
      public boolean canParse(ParseContext context) {
         return context == ParseContext.COMMAND || context == ParseContext.PARSE;
      }

      public String toString(Player player, int flags) {
         return player.getName();
      }

      public String toVariableNameString(Player player) {
         return SkriptConfig.usePlayerUUIDsInVariableNames.value() ? player.getUniqueId().toString() : player.getName();
      }

      public String getDebugMessage(Player player) {
         return player.getName() + " " + Classes.getDebugMessage(player.getLocation());
      }
   }
}
