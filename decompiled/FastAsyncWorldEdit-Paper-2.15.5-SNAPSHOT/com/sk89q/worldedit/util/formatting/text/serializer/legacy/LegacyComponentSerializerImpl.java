package com.sk89q.worldedit.util.formatting.text.serializer.legacy;

import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

final class LegacyComponentSerializerImpl extends AbstractLegacyComponentSerializer {
   static final LegacyComponentSerializer INSTANCE = new LegacyComponentSerializerImpl();

   @NonNull
   @Override
   protected TextComponent finish(@NonNull final TextComponent component) {
      return component;
   }
}
