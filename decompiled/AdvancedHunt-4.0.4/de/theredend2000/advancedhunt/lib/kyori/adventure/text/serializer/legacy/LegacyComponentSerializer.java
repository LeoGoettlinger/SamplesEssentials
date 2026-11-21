package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.legacy;

import de.theredend2000.advancedhunt.lib.kyori.adventure.builder.AbstractBuilder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TextComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.flattener.ComponentFlattener;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.ComponentSerializer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.PlatformAPI;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

public interface LegacyComponentSerializer extends ComponentSerializer<Component, TextComponent, String> {
   char SECTION_CHAR = '§';
   char AMPERSAND_CHAR = '&';
   char HEX_CHAR = '#';

   static LegacyComponentSerializer legacySection() {
      return LegacyComponentSerializerImpl.Instances.SECTION;
   }

   static LegacyComponentSerializer legacyAmpersand() {
      return LegacyComponentSerializerImpl.Instances.AMPERSAND;
   }

   static LegacyComponentSerializer legacy(final char legacyCharacter) {
      if (legacyCharacter == 167) {
         return legacySection();
      } else {
         return legacyCharacter == '&' ? legacyAmpersand() : builder().character(legacyCharacter).build();
      }
   }

   @Nullable
   static LegacyFormat parseChar(final char character) {
      return LegacyComponentSerializerImpl.legacyFormat(character);
   }

   static LegacyComponentSerializer.Builder builder() {
      return new LegacyComponentSerializerImpl.BuilderImpl();
   }

   TextComponent deserialize(final String input);

   String serialize(final Component component);

   LegacyComponentSerializer.Builder toBuilder();

   public interface Builder extends AbstractBuilder<LegacyComponentSerializer> {
      LegacyComponentSerializer.Builder character(final char legacyCharacter);

      LegacyComponentSerializer.Builder hexCharacter(final char legacyHexCharacter);

      LegacyComponentSerializer.Builder extractUrls();

      LegacyComponentSerializer.Builder extractUrls(final Pattern pattern);

      LegacyComponentSerializer.Builder extractUrls(@Nullable final Style style);

      LegacyComponentSerializer.Builder extractUrls(final Pattern pattern, @Nullable final Style style);

      LegacyComponentSerializer.Builder hexColors();

      LegacyComponentSerializer.Builder useUnusualXRepeatedCharacterHexFormat();

      LegacyComponentSerializer.Builder flattener(final ComponentFlattener flattener);

      LegacyComponentSerializer.Builder formats(final List<CharacterAndFormat> formats);

      LegacyComponentSerializer build();
   }

   @PlatformAPI
   @Internal
   public interface Provider {
      @PlatformAPI
      @Internal
      LegacyComponentSerializer legacyAmpersand();

      @PlatformAPI
      @Internal
      LegacyComponentSerializer legacySection();

      @PlatformAPI
      @Internal
      Consumer<LegacyComponentSerializer.Builder> legacy();
   }
}
