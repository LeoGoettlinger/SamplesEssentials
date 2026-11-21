package com.sk89q.worldedit.util.formatting.text.serializer.legacy;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import com.sk89q.worldedit.util.formatting.text.format.TextFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractLegacyComponentSerializer implements LegacyComponentSerializer {
   private static final TextDecoration[] DECORATIONS = TextDecoration.values();
   private static final String LEGACY_CHARS = "0123456789abcdefklmnor";
   private static final List<TextFormat> FORMATS;

   @Nullable
   private static TextFormat formatByLegacyChar(final char legacy) {
      int index = "0123456789abcdefklmnor".indexOf(legacy);
      return index == -1 ? null : FORMATS.get(index);
   }

   private static char getLegacyChar(final TextFormat legacy) {
      int index = FORMATS.indexOf(legacy);
      return "0123456789abcdefklmnor".charAt(index);
   }

   @NonNull
   @Override
   public TextComponent deserialize(@NonNull final String input, final char character) {
      int next = input.lastIndexOf(character, input.length() - 2);
      if (next == -1) {
         return this.finish(TextComponent.of(input));
      } else {
         List<TextComponent> parts = new ArrayList<>();
         TextComponent.Builder current = null;
         boolean reset = false;
         int pos = input.length();

         do {
            TextFormat format = formatByLegacyChar(input.charAt(next + 1));
            if (format != null) {
               int from = next + 2;
               if (from != pos) {
                  if (current != null) {
                     if (reset) {
                        parts.add(current.build());
                        reset = false;
                        current = TextComponent.builder();
                     } else {
                        current = TextComponent.builder().append(current.build());
                     }
                  } else {
                     current = TextComponent.builder();
                  }

                  current.content(input.substring(from, pos));
               } else if (current == null) {
                  current = TextComponent.builder();
               }

               reset |= applyFormat(current, format);
               pos = next;
            }

            next = input.lastIndexOf(character, next - 1);
         } while (next != -1);

         if (current != null) {
            parts.add(current.build());
         }

         Collections.reverse(parts);
         return this.finish(TextComponent.builder(pos > 0 ? input.substring(0, pos) : "").append(parts).build());
      }
   }

   @NonNull
   protected abstract TextComponent finish(final TextComponent component);

   @NonNull
   @Override
   public String serialize(@NonNull final Component component, final char character) {
      AbstractLegacyComponentSerializer.Cereal state = new AbstractLegacyComponentSerializer.Cereal(character);
      state.append(component);
      return state.toString();
   }

   private static boolean applyFormat(@NonNull final TextComponent.Builder builder, @NonNull final TextFormat format) {
      if (format instanceof TextColor) {
         builder.colorIfAbsent((TextColor)format);
         return true;
      } else if (format instanceof TextDecoration) {
         builder.decoration((TextDecoration)format, TextDecoration.State.TRUE);
         return false;
      } else if (!(format instanceof AbstractLegacyComponentSerializer.Reset)) {
         throw new IllegalArgumentException(String.format("unknown format '%s'", format.getClass()));
      } else {
         builder.colorIfAbsent(null);
         int i = 0;

         for (int length = DECORATIONS.length; i < length; i++) {
            TextDecoration decoration = DECORATIONS[i];
            builder.decoration(decoration, TextDecoration.State.NOT_SET);
         }

         return true;
      }
   }

   static {
      List<TextFormat> formats = new ArrayList<>();
      Collections.addAll(formats, TextColor.values());
      Collections.addAll(formats, DECORATIONS);
      formats.add(AbstractLegacyComponentSerializer.Reset.INSTANCE);
      FORMATS = Collections.unmodifiableList(formats);
      if (FORMATS.size() != "0123456789abcdefklmnor".length()) {
         throw new IllegalStateException("FORMATS length differs from LEGACY_CHARS length");
      }
   }

   private static final class Cereal {
      private final StringBuilder sb = new StringBuilder();
      private final AbstractLegacyComponentSerializer.Cereal.Style style = new AbstractLegacyComponentSerializer.Cereal.Style();
      private final char character;

      Cereal(final char character) {
         this.character = character;
      }

      void append(@NonNull final Component component) {
         this.append(component, new AbstractLegacyComponentSerializer.Cereal.Style());
      }

      private void append(@NonNull final Component component, final AbstractLegacyComponentSerializer.Cereal.Style style) {
         style.apply(component);
         if (component instanceof TextComponent) {
            String content = ((TextComponent)component).content();
            if (!content.isEmpty()) {
               style.applyFormat();
               this.sb.append(content);
            }
         }

         List<Component> children = component.children();
         if (!children.isEmpty()) {
            AbstractLegacyComponentSerializer.Cereal.Style childrenStyle = new AbstractLegacyComponentSerializer.Cereal.Style(style);

            for (Component child : children) {
               this.append(child, childrenStyle);
               childrenStyle.set(style);
            }
         }
      }

      private void append(@NonNull final TextFormat format) {
         this.sb.append(this.character).append(AbstractLegacyComponentSerializer.getLegacyChar(format));
      }

      @Override
      public String toString() {
         return this.sb.toString();
      }

      private final class Style {
         @Nullable
         private TextColor color;
         private final Set<TextDecoration> decorations;

         Style() {
            this.decorations = EnumSet.noneOf(TextDecoration.class);
         }

         Style(final AbstractLegacyComponentSerializer.Cereal.Style that) {
            this.color = that.color;
            this.decorations = EnumSet.copyOf(that.decorations);
         }

         void set(final AbstractLegacyComponentSerializer.Cereal.Style that) {
            this.color = that.color;
            this.decorations.clear();
            this.decorations.addAll(that.decorations);
         }

         void apply(@NonNull final Component component) {
            TextColor color = component.color();
            if (color != null) {
               this.color = color;
            }

            int i = 0;

            for (int length = AbstractLegacyComponentSerializer.DECORATIONS.length; i < length; i++) {
               TextDecoration decoration = AbstractLegacyComponentSerializer.DECORATIONS[i];
               switch (component.decoration(decoration)) {
                  case TRUE:
                     this.decorations.add(decoration);
                     break;
                  case FALSE:
                     this.decorations.remove(decoration);
               }
            }
         }

         void applyFormat() {
            if (this.color != Cereal.this.style.color) {
               this.applyFullFormat();
            } else if (!this.decorations.containsAll(Cereal.this.style.decorations)) {
               this.applyFullFormat();
            } else {
               for (TextDecoration decoration : this.decorations) {
                  if (Cereal.this.style.decorations.add(decoration)) {
                     Cereal.this.append(decoration);
                  }
               }
            }
         }

         private void applyFullFormat() {
            if (this.color != null) {
               Cereal.this.append(this.color);
            } else {
               Cereal.this.append(AbstractLegacyComponentSerializer.Reset.INSTANCE);
            }

            Cereal.this.style.color = this.color;

            for (TextDecoration decoration : this.decorations) {
               Cereal.this.append(decoration);
            }

            Cereal.this.style.decorations.clear();
            Cereal.this.style.decorations.addAll(this.decorations);
         }
      }
   }

   private static enum Reset implements TextFormat {
      INSTANCE;
   }
}
