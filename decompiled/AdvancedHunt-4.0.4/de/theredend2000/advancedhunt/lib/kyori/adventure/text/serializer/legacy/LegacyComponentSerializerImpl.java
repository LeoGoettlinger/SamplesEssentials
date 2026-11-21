package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.legacy;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TextComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TextReplacementConfig;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.ClickEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.flattener.ComponentFlattener;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.flattener.FlattenerListener;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.NamedTextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextDecoration;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextFormat;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Services;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

final class LegacyComponentSerializerImpl implements LegacyComponentSerializer {
   static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/([A-Za-z0-9\\-._~!$&'()*+,;=:@/]|%[0-9A-Fa-f]{2})*)?");
   static final Pattern URL_SCHEME_PATTERN = Pattern.compile("^[a-z][a-z0-9+\\-.]*:");
   private static final TextDecoration[] DECORATIONS = TextDecoration.values();
   private static final char LEGACY_BUNGEE_HEX_CHAR = 'x';
   private static final Optional<LegacyComponentSerializer.Provider> SERVICE = Services.service(
      ServiceLoader.load(LegacyComponentSerializer.Provider.class, LegacyComponentSerializer.Provider.class.getClassLoader()),
      LegacyComponentSerializer.Provider.class
   );
   static final Consumer<LegacyComponentSerializer.Builder> BUILDER = SERVICE.map(LegacyComponentSerializer.Provider::legacy).orElseGet(() -> builder -> {});
   private final char character;
   private final char hexCharacter;
   @Nullable
   private final TextReplacementConfig urlReplacementConfig;
   private final boolean hexColours;
   private final boolean useTerriblyStupidHexFormat;
   private final ComponentFlattener flattener;
   private final CharacterAndFormatSet formats;

   LegacyComponentSerializerImpl(
      final char character,
      final char hexCharacter,
      @Nullable final TextReplacementConfig urlReplacementConfig,
      final boolean hexColours,
      final boolean useTerriblyStupidHexFormat,
      final ComponentFlattener flattener,
      final CharacterAndFormatSet formats
   ) {
      this.character = character;
      this.hexCharacter = hexCharacter;
      this.urlReplacementConfig = urlReplacementConfig;
      this.hexColours = hexColours;
      this.useTerriblyStupidHexFormat = useTerriblyStupidHexFormat;
      this.flattener = flattener;
      this.formats = formats;
   }

   @Nullable
   private LegacyComponentSerializerImpl.FormatCodeType determineFormatType(final char legacy, final String input, final int pos) {
      if (pos >= 14) {
         int expectedCharacterPosition = pos - 14;
         int expectedIndicatorPosition = pos - 13;
         if (input.charAt(expectedCharacterPosition) == this.character && input.charAt(expectedIndicatorPosition) == 'x') {
            return LegacyComponentSerializerImpl.FormatCodeType.BUNGEECORD_UNUSUAL_HEX;
         }
      }

      if (legacy == this.hexCharacter && input.length() - pos >= 6) {
         return LegacyComponentSerializerImpl.FormatCodeType.KYORI_HEX;
      } else {
         return this.formats.characters().indexOf(legacy) != -1 ? LegacyComponentSerializerImpl.FormatCodeType.MOJANG_LEGACY : null;
      }
   }

   @Nullable
   static LegacyFormat legacyFormat(final char character) {
      int index = CharacterAndFormatSet.DEFAULT.characters().indexOf(character);
      if (index != -1) {
         return switch ((TextFormat)CharacterAndFormatSet.DEFAULT.formats().get(index)) {
            case NamedTextColor namedTextColor -> new LegacyFormat(namedTextColor);
            case TextDecoration textDecoration -> new LegacyFormat(textDecoration);
            case null, default -> LegacyFormat.RESET;
         };
      } else {
         return null;
      }
   }

   @Nullable
   private LegacyComponentSerializerImpl.DecodedFormat decodeTextFormat(final char legacy, final String input, final int pos) {
      LegacyComponentSerializerImpl.FormatCodeType foundFormat = this.determineFormatType(legacy, input, pos);
      if (foundFormat == null) {
         return null;
      } else {
         switch (foundFormat) {
            case MOJANG_LEGACY:
               return new LegacyComponentSerializerImpl.DecodedFormat(foundFormat, this.formats.formats().get(this.formats.characters().indexOf(legacy)));
            case KYORI_HEX:
               TextColor parsed = tryParseHexColor(input.substring(pos, pos + 6));
               if (parsed != null) {
                  return new LegacyComponentSerializerImpl.DecodedFormat(foundFormat, parsed);
               }
               break;
            case BUNGEECORD_UNUSUAL_HEX:
               StringBuilder foundHex = new StringBuilder(6);

               for (int i = pos - 1; i >= pos - 11; i -= 2) {
                  foundHex.append(input.charAt(i));
               }

               TextColor parsedx = tryParseHexColor(foundHex.reverse().toString());
               if (parsedx != null) {
                  return new LegacyComponentSerializerImpl.DecodedFormat(foundFormat, parsedx);
               }
         }

         return null;
      }
   }

   @Nullable
   private static TextColor tryParseHexColor(final String hexDigits) {
      try {
         int color = Integer.parseInt(hexDigits, 16);
         return TextColor.color(color);
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   private static boolean isHexTextColor(final TextFormat format) {
      return format instanceof TextColor && !(format instanceof NamedTextColor);
   }

   @Nullable
   private String toLegacyCode(@Nullable TextFormat format) {
      if (format != null && isHexTextColor(format)) {
         TextColor color = (TextColor)format;
         if (this.hexColours) {
            String hex = String.format("%06x", color.value());
            if (!this.useTerriblyStupidHexFormat) {
               return this.hexCharacter + hex;
            }

            StringBuilder legacy = new StringBuilder(String.valueOf('x'));
            int i = 0;

            for (int length = hex.length(); i < length; i++) {
               legacy.append(this.character).append(hex.charAt(i));
            }

            return legacy.toString();
         }

         if (!(color instanceof NamedTextColor)) {
            format = TextColor.nearestColorTo(this.formats.colors(), color);
         }
      }

      int index = this.formats.formats().indexOf(format);
      return index == -1 ? null : Character.toString(this.formats.characters().charAt(index));
   }

   private TextComponent extractUrl(final TextComponent component) {
      if (this.urlReplacementConfig == null) {
         return component;
      } else {
         Component newComponent = component.replaceText(this.urlReplacementConfig);
         return newComponent instanceof TextComponent tc ? tc : Component.text().append(newComponent).build();
      }
   }

   @Override
   public TextComponent deserialize(final String input) {
      int next = input.lastIndexOf(this.character, input.length() - 2);
      if (next == -1) {
         return this.extractUrl(Component.text(input));
      } else {
         List<TextComponent> parts = new ArrayList<>();
         TextComponent.Builder current = null;
         boolean reset = false;
         int pos = input.length();

         do {
            LegacyComponentSerializerImpl.DecodedFormat decoded = this.decodeTextFormat(input.charAt(next + 1), input, next + 2);
            if (decoded != null) {
               int from = next + (decoded.encodedFormat == LegacyComponentSerializerImpl.FormatCodeType.KYORI_HEX ? 8 : 2);
               if (from != pos) {
                  if (current != null) {
                     if (reset) {
                        parts.add(current.build());
                        reset = false;
                        current = Component.text();
                     } else {
                        current = Component.text().append(current.build());
                     }
                  } else {
                     current = Component.text();
                  }

                  current.content(input.substring(from, pos));
               } else if (current == null) {
                  current = Component.text();
               }

               if (!reset) {
                  reset = applyFormat(current, decoded.format);
               }

               if (decoded.encodedFormat == LegacyComponentSerializerImpl.FormatCodeType.BUNGEECORD_UNUSUAL_HEX) {
                  next -= 12;
               }

               pos = next;
            }

            next = input.lastIndexOf(this.character, next - 1);
         } while (next != -1);

         if (current != null) {
            parts.add(current.build());
         }

         String remaining = pos > 0 ? input.substring(0, pos) : "";
         if (parts.size() == 1 && remaining.isEmpty()) {
            return this.extractUrl(parts.getFirst());
         } else {
            Collections.reverse(parts);
            return this.extractUrl(Component.text().content(remaining).append(parts).build());
         }
      }
   }

   @Override
   public String serialize(final Component component) {
      LegacyComponentSerializerImpl.Cereal state = new LegacyComponentSerializerImpl.Cereal();
      this.flattener.flatten(component, state);
      return state.toString();
   }

   private static boolean applyFormat(final TextComponent.Builder builder, @Nullable final TextFormat format) {
      return switch (format) {
         case null -> true;
         case TextColor textColor -> {
            builder.colorIfAbsent(textColor);
            yield true;
         }
         case TextDecoration textDecoration -> {
            builder.decoration(textDecoration, TextDecoration.State.TRUE);
            yield false;
         }
         default -> throw new MatchException(null, null);
      };
   }

   @Override
   public LegacyComponentSerializer.Builder toBuilder() {
      return new LegacyComponentSerializerImpl.BuilderImpl(this);
   }

   static final class BuilderImpl implements LegacyComponentSerializer.Builder {
      private char character = 167;
      private char hexCharacter = '#';
      private TextReplacementConfig urlReplacementConfig = null;
      private boolean hexColours = false;
      private boolean useTerriblyStupidHexFormat = false;
      private ComponentFlattener flattener = ComponentFlattener.basic();
      private CharacterAndFormatSet formats = CharacterAndFormatSet.DEFAULT;

      BuilderImpl() {
         LegacyComponentSerializerImpl.BUILDER.accept(this);
      }

      BuilderImpl(final LegacyComponentSerializerImpl serializer) {
         this();
         this.character = serializer.character;
         this.hexCharacter = serializer.hexCharacter;
         this.urlReplacementConfig = serializer.urlReplacementConfig;
         this.hexColours = serializer.hexColours;
         this.useTerriblyStupidHexFormat = serializer.useTerriblyStupidHexFormat;
         this.flattener = serializer.flattener;
         this.formats = serializer.formats;
      }

      @Override
      public LegacyComponentSerializer.Builder character(final char legacyCharacter) {
         this.character = legacyCharacter;
         return this;
      }

      @Override
      public LegacyComponentSerializer.Builder hexCharacter(final char legacyHexCharacter) {
         this.hexCharacter = legacyHexCharacter;
         return this;
      }

      @Override
      public LegacyComponentSerializer.Builder extractUrls() {
         return this.extractUrls(LegacyComponentSerializerImpl.DEFAULT_URL_PATTERN, null);
      }

      @Override
      public LegacyComponentSerializer.Builder extractUrls(final Pattern pattern) {
         return this.extractUrls(pattern, null);
      }

      @Override
      public LegacyComponentSerializer.Builder extractUrls(@Nullable final Style style) {
         return this.extractUrls(LegacyComponentSerializerImpl.DEFAULT_URL_PATTERN, style);
      }

      @Override
      public LegacyComponentSerializer.Builder extractUrls(final Pattern pattern, @Nullable final Style style) {
         Objects.requireNonNull(pattern, "pattern");
         this.urlReplacementConfig = TextReplacementConfig.builder().match(pattern).replacement(url -> {
            String clickUrl = url.content();
            if (!LegacyComponentSerializerImpl.URL_SCHEME_PATTERN.matcher(clickUrl).find()) {
               clickUrl = "http://" + clickUrl;
            }

            try {
               new URI(clickUrl);
               return (style == null ? url : url.style(style)).clickEvent(ClickEvent.openUrl(clickUrl));
            } catch (URISyntaxException var4) {
               return url;
            }
         }).build();
         return this;
      }

      @Override
      public LegacyComponentSerializer.Builder hexColors() {
         this.hexColours = true;
         return this;
      }

      @Override
      public LegacyComponentSerializer.Builder useUnusualXRepeatedCharacterHexFormat() {
         this.useTerriblyStupidHexFormat = true;
         return this;
      }

      @Override
      public LegacyComponentSerializer.Builder flattener(final ComponentFlattener flattener) {
         this.flattener = Objects.requireNonNull(flattener, "flattener");
         return this;
      }

      @Override
      public LegacyComponentSerializer.Builder formats(final List<CharacterAndFormat> formats) {
         this.formats = CharacterAndFormatSet.of(formats);
         return this;
      }

      @Override
      public LegacyComponentSerializer build() {
         return new LegacyComponentSerializerImpl(
            this.character, this.hexCharacter, this.urlReplacementConfig, this.hexColours, this.useTerriblyStupidHexFormat, this.flattener, this.formats
         );
      }
   }

   private final class Cereal implements FlattenerListener {
      private final StringBuilder sb = new StringBuilder();
      private final LegacyComponentSerializerImpl.Cereal.StyleState style = new LegacyComponentSerializerImpl.Cereal.StyleState();
      @Nullable
      private TextFormat lastWritten;
      private LegacyComponentSerializerImpl.Cereal.StyleState[] styles = new LegacyComponentSerializerImpl.Cereal.StyleState[8];
      private int head = -1;

      @Override
      public void pushStyle(final Style pushed) {
         int idx = ++this.head;
         if (idx >= this.styles.length) {
            this.styles = Arrays.copyOf(this.styles, this.styles.length * 2);
         }

         LegacyComponentSerializerImpl.Cereal.StyleState state = this.styles[idx];
         if (state == null) {
            this.styles[idx] = state = new LegacyComponentSerializerImpl.Cereal.StyleState();
         }

         if (idx > 0) {
            state.set(this.styles[idx - 1]);
         } else {
            state.clear();
         }

         state.apply(pushed);
      }

      @Override
      public void component(final String text) {
         if (!text.isEmpty()) {
            if (this.head < 0) {
               throw new IllegalStateException("No style has been pushed!");
            }

            this.styles[this.head].applyFormat();
            this.sb.append(text);
         }
      }

      @Override
      public void popStyle(final Style style) {
         if (this.head-- < 0) {
            throw new IllegalStateException("Tried to pop beyond what was pushed!");
         }
      }

      void append(@Nullable final TextFormat format) {
         if (this.lastWritten != format) {
            String legacyCode = LegacyComponentSerializerImpl.this.toLegacyCode(format);
            if (legacyCode == null) {
               return;
            }

            this.sb.append(LegacyComponentSerializerImpl.this.character).append(legacyCode);
         }

         this.lastWritten = format;
      }

      @Override
      public String toString() {
         return this.sb.toString();
      }

      private final class StyleState {
         @Nullable
         private TextColor color;
         private final Set<TextDecoration> decorations = EnumSet.noneOf(TextDecoration.class);
         private boolean needsReset;

         StyleState() {
         }

         void set(final LegacyComponentSerializerImpl.Cereal.StyleState that) {
            this.color = that.color;
            this.decorations.clear();
            this.decorations.addAll(that.decorations);
         }

         private void clear() {
            this.color = null;
            this.decorations.clear();
         }

         void apply(final Style component) {
            TextColor color = component.color();
            if (color != null) {
               this.color = color;
            }

            for (TextDecoration decoration : LegacyComponentSerializerImpl.DECORATIONS) {
               switch (component.decoration(decoration)) {
                  case TRUE:
                     this.decorations.add(decoration);
                     break;
                  case FALSE:
                     if (this.decorations.remove(decoration)) {
                        this.needsReset = true;
                     }
                  case NOT_SET:
               }
            }
         }

         void applyFormat() {
            boolean colorChanged = this.color != Cereal.this.style.color;
            if (this.needsReset) {
               if (!colorChanged) {
                  Cereal.this.append(null);
               }

               this.needsReset = false;
            }

            if (!colorChanged && Cereal.this.lastWritten != null) {
               if (!this.decorations.containsAll(Cereal.this.style.decorations)) {
                  this.applyFullFormat();
               } else {
                  for (TextDecoration decoration : this.decorations) {
                     if (Cereal.this.style.decorations.add(decoration)) {
                        Cereal.this.append(decoration);
                     }
                  }
               }
            } else {
               this.applyFullFormat();
            }
         }

         private void applyFullFormat() {
            Cereal.this.append(this.color);
            Cereal.this.style.color = this.color;

            for (TextDecoration decoration : this.decorations) {
               Cereal.this.append(decoration);
            }

            Cereal.this.style.decorations.clear();
            Cereal.this.style.decorations.addAll(this.decorations);
         }
      }
   }

   record DecodedFormat(LegacyComponentSerializerImpl.FormatCodeType encodedFormat, @Nullable TextFormat format) {
   }

   static enum FormatCodeType {
      MOJANG_LEGACY,
      KYORI_HEX,
      BUNGEECORD_UNUSUAL_HEX;
   }

   static final class Instances {
      static final LegacyComponentSerializer SECTION = LegacyComponentSerializerImpl.SERVICE
         .map(LegacyComponentSerializer.Provider::legacySection)
         .orElseGet(() -> new LegacyComponentSerializerImpl('§', '#', null, false, false, ComponentFlattener.basic(), CharacterAndFormatSet.DEFAULT));
      static final LegacyComponentSerializer AMPERSAND = LegacyComponentSerializerImpl.SERVICE
         .map(LegacyComponentSerializer.Provider::legacyAmpersand)
         .orElseGet(() -> new LegacyComponentSerializerImpl('&', '#', null, false, false, ComponentFlattener.basic(), CharacterAndFormatSet.DEFAULT));
   }
}
