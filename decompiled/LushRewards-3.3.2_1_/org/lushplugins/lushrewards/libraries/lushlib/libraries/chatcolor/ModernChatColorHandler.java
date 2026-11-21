package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers.MiniMessageMessenger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Resolver;

public class ModernChatColorHandler {
   public static Component translate(@Nullable String string) {
      return translate(string, ChatColorHandler.settings().defaultParsers());
   }

   public static Component translate(@Nullable String string, @NotNull List<Parser> parsers) {
      return translate(string, null, parsers);
   }

   public static Component translate(@Nullable String string, Player player) {
      return translate(string, player, ChatColorHandler.settings().defaultParsers());
   }

   public static Component translate(@Nullable String string, Player player, @NotNull List<Parser> parsers) {
      ChatColorHandler.ensureInitialised();
      if (string != null && !string.isBlank()) {
         String legacyParsed = ChatColorHandler.parsers().parseString(string, Parser.OutputType.MINI_MESSAGE, player, parsers);
         TagResolver tagResolver = Resolver.combineResolvers(
            (Audience)player, parsers.stream().map(parser -> parser instanceof Resolver resolver ? resolver : null).filter(Objects::nonNull).toList()
         );
         return MiniMessageMessenger.MINI_MESSAGE.deserialize(legacyParsed, tagResolver);
      } else {
         return Component.empty();
      }
   }

   public static List<Component> translate(@NotNull Collection<String> strings) {
      return translate(strings, ChatColorHandler.settings().defaultParsers());
   }

   public static List<Component> translate(@NotNull Collection<String> strings, List<Parser> parsers) {
      return translate(strings, null, parsers);
   }

   public static List<Component> translate(@NotNull Collection<String> strings, Player player) {
      return translate(strings, player, ChatColorHandler.settings().defaultParsers());
   }

   public static List<Component> translate(@NotNull Collection<String> strings, Player player, List<Parser> parsers) {
      return strings.stream().map(string -> translate(string, player, parsers)).toList();
   }

   private ModernChatColorHandler() {
   }
}
