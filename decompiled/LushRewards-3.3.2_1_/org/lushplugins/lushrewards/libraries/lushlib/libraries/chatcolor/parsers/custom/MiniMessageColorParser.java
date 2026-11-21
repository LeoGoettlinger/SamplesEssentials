package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers.MiniMessageMessenger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Resolver;

public class MiniMessageColorParser implements Resolver {
   public static final MiniMessageColorParser INSTANCE = new MiniMessageColorParser();
   private static final TagResolver BASIC_COLORS = TagResolver.builder()
      .resolvers(
         new TagResolver[]{
            StandardTags.color(), StandardTags.decorations(), StandardTags.gradient(), StandardTags.rainbow(), StandardTags.reset(), StandardTags.transition()
         }
      )
      .build();

   private MiniMessageColorParser() {
   }

   @Override
   public String getType() {
      return "color";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return switch (outputType) {
         case SPIGOT -> {
            string = string.replace('§', '&');
            yield MiniMessageMessenger.LEGACY_COMPONENT_SERIALIZER.serialize(MiniMessageMessenger.MINI_MESSAGE.deserialize(string, BASIC_COLORS));
         }
         case MINI_MESSAGE -> string;
      };
   }

   @NotNull
   @Override
   public TagResolver getResolver() {
      return BASIC_COLORS;
   }
}
