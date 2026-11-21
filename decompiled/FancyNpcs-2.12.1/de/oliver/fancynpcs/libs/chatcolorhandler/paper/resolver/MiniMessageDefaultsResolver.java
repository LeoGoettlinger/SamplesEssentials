package de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.ParserType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiniMessageDefaultsResolver implements Resolver {
   public static final MiniMessageDefaultsResolver INSTANCE = new MiniMessageDefaultsResolver();

   @Nullable
   @Override
   public ParserType getType() {
      return null;
   }

   @NotNull
   @Override
   public TagResolver getResolver(@Nullable Audience audience) {
      return StandardTags.defaults();
   }
}
