package de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.ParserType;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiniPlaceholdersResolver implements Resolver {
   public static final MiniPlaceholdersResolver INSTANCE = new MiniPlaceholdersResolver();

   @Override
   public ParserType getType() {
      return ParserType.PLACEHOLDER;
   }

   @NotNull
   @Override
   public TagResolver getResolver(@Nullable Audience audience) {
      return audience != null ? MiniPlaceholders.audienceGlobalPlaceholders() : MiniPlaceholders.globalPlaceholders();
   }
}
