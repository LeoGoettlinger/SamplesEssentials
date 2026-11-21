package com.samplecraft.darkdrop.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class MessageUtil {
   private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

   private MessageUtil() {
   }

   public static Component toComponent(String legacy) {
      return legacy == null ? Component.empty() : LEGACY.deserialize(legacy);
   }
}
