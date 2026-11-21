package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers.MiniMessageMessenger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.ParserTypes;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Resolver;

public class MiniMessageResolverParser implements Parser {
   public static final MiniMessageResolverParser INSTANCE = new MiniMessageResolverParser();

   private MiniMessageResolverParser() {
   }

   @Override
   public String getType() {
      return "placeholder";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return this.parseString(string, outputType, null);
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType, Player player) {
      return switch (outputType) {
         case SPIGOT -> {
            string = string.replace('§', '&');
            TagResolver resolver = Resolver.combineResolvers(player instanceof Audience audience ? audience : null, getPlaceholderResolvers());
            yield MiniMessageMessenger.LEGACY_COMPONENT_SERIALIZER.serialize(MiniMessageMessenger.MINI_MESSAGE.deserialize(string, resolver));
         }
         case MINI_MESSAGE -> string;
      };
   }

   private static List<Resolver> getPlaceholderResolvers() {
      return ParserTypes.placeholder()
         .stream()
         .map(parser -> parser instanceof Resolver parserResolver ? parserResolver : null)
         .filter(Objects::nonNull)
         .toList();
   }
}
