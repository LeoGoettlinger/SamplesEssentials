package de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.ParserType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiniMessageInteractionResolver implements Resolver {
   public static final MiniMessageInteractionResolver INSTANCE = new MiniMessageInteractionResolver();
   private static final TagResolver INTERACTION = TagResolver.builder()
      .resolvers(new TagResolver[]{StandardTags.hoverEvent(), StandardTags.clickEvent(), StandardTags.insertion()})
      .build();

   @Override
   public ParserType getType() {
      return ParserType.INTERACTION;
   }

   @NotNull
   @Override
   public TagResolver getResolver(@Nullable Audience audience) {
      return INTERACTION;
   }
}
