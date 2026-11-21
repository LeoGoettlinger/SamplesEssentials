package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Resolver;

public class MiniPlaceholdersParser implements Resolver {
   public static final MiniPlaceholdersParser INSTANCE = new MiniPlaceholdersParser();

   private MiniPlaceholdersParser() {
   }

   @Override
   public String getType() {
      return "placeholder";
   }

   @NotNull
   @Override
   public TagResolver getResolver() {
      return MiniPlaceholders.getGlobalPlaceholders();
   }

   @NotNull
   @Override
   public TagResolver getResolver(Audience audience) {
      return MiniPlaceholders.getAudienceGlobalPlaceholders(audience);
   }
}
