package net.kyori.adventure.text.minimessage.tag.resolver;

import java.util.Map;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jetbrains.annotations.NotNull;

interface MappableResolver {
   boolean contributeToMap(@NotNull final Map<String, Tag> map);
}
