package de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.ParserType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiniMessageTextFormattingResolver implements Resolver {
   public static final MiniMessageTextFormattingResolver INSTANCE = new MiniMessageTextFormattingResolver();
   private static final TagResolver TEXT_FORMATTING = TagResolver.builder().resolvers(new TagResolver[]{StandardTags.font(), StandardTags.newline()}).build();

   @Override
   public ParserType getType() {
      return ParserType.TEXT_FORMATTING;
   }

   @NotNull
   @Override
   public TagResolver getResolver(@Nullable Audience audience) {
      return TEXT_FORMATTING;
   }
}
