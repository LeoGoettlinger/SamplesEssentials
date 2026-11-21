package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.messengers.MiniMessageMessenger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Resolver;

public class MiniMessageInteractionParser implements Resolver {
   public static final MiniMessageInteractionParser INSTANCE = new MiniMessageInteractionParser();
   private static final TagResolver INTERACTION = TagResolver.builder()
      .resolvers(new TagResolver[]{StandardTags.hoverEvent(), StandardTags.clickEvent(), StandardTags.insertion()})
      .build();

   private MiniMessageInteractionParser() {
   }

   @Override
   public String getType() {
      return "interaction";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return switch (outputType) {
         case SPIGOT -> {
            string = string.replace('§', '&');
            yield MiniMessageMessenger.LEGACY_COMPONENT_SERIALIZER.serialize(MiniMessageMessenger.MINI_MESSAGE.deserialize(string, INTERACTION));
         }
         case MINI_MESSAGE -> string;
      };
   }

   @NotNull
   @Override
   public TagResolver getResolver() {
      return INTERACTION;
   }
}
