package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers.MiniMessageMessenger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Resolver;

public class MiniMessageTextFormattingParser implements Resolver {
   public static final MiniMessageTextFormattingParser INSTANCE = new MiniMessageTextFormattingParser();
   private static final TagResolver TEXT_FORMATTING = TagResolver.builder().resolvers(new TagResolver[]{StandardTags.font(), StandardTags.newline()}).build();

   private MiniMessageTextFormattingParser() {
   }

   @Override
   public String getType() {
      return "text-formatting";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return switch (outputType) {
         case SPIGOT -> {
            string = string.replace('§', '&');
            yield MiniMessageMessenger.LEGACY_COMPONENT_SERIALIZER.serialize(MiniMessageMessenger.MINI_MESSAGE.deserialize(string, TEXT_FORMATTING));
         }
         case MINI_MESSAGE -> string;
      };
   }

   @NotNull
   @Override
   public TagResolver getResolver() {
      return TEXT_FORMATTING;
   }
}
