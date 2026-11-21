package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers.MiniMessageMessenger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Resolver;

public class MiniMessagePlaceholderParser implements Resolver {
   public static final MiniMessagePlaceholderParser INSTANCE = new MiniMessagePlaceholderParser();
   private static final TagResolver VANILLA_PLACEHOLDERS = TagResolver.builder()
      .resolvers(
         new TagResolver[]{
            StandardTags.keybind(),
            StandardTags.translatable(),
            StandardTags.translatableFallback(),
            StandardTags.selector(),
            StandardTags.score(),
            StandardTags.nbt()
         }
      )
      .build();

   private MiniMessagePlaceholderParser() {
   }

   @Override
   public String getType() {
      return "placeholder";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return switch (outputType) {
         case SPIGOT -> {
            string = string.replace('§', '&');
            yield MiniMessageMessenger.LEGACY_COMPONENT_SERIALIZER.serialize(MiniMessageMessenger.MINI_MESSAGE.deserialize(string, VANILLA_PLACEHOLDERS));
         }
         case MINI_MESSAGE -> string;
      };
   }

   @NotNull
   @Override
   public TagResolver getResolver() {
      return VANILLA_PLACEHOLDERS;
   }
}
