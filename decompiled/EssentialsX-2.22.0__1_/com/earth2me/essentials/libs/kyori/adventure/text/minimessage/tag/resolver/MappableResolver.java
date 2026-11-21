package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver;

import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.Tag;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

interface MappableResolver {
   boolean contributeToMap(@NotNull final Map<String, Tag> map);
}
