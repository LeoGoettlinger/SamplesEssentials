package de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface Resolver extends Parser {
   @Override
   default int getPriority() {
      return -1;
   }

   @Override
   default String parseString(String string, @Nullable Player player) {
      return string;
   }

   TagResolver getResolver(@Nullable Audience var1);
}
