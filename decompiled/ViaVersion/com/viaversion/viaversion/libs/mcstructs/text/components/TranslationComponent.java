package com.viaversion.viaversion.libs.mcstructs.text.components;

import com.viaversion.viaversion.libs.mcstructs.converter.ConsumerTracking;
import com.viaversion.viaversion.libs.mcstructs.core.utils.ToString;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.translation.Translator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.Generated;

public class TranslationComponent extends TextComponent {
   private static final Pattern ARG_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
   private String key;
   private Object[] args;
   @Nullable
   private String fallback;
   private Translator translator = Translator.GLOBAL;

   public TranslationComponent(String key, List<?> args) {
      this.key = key;
      this.args = args.toArray();
   }

   public TranslationComponent(String key, Object... args) {
      this.key = key;
      this.args = args;
   }

   public String getKey() {
      return this.key;
   }

   public TranslationComponent setKey(String key) {
      this.key = key;
      return this;
   }

   public Object[] getArgs() {
      return this.args;
   }

   public TranslationComponent setArgs(Object[] args) {
      this.args = args;
      return this;
   }

   @Nullable
   public String getFallback() {
      return this.fallback;
   }

   public TranslationComponent setFallback(@Nullable String fallback) {
      this.fallback = fallback;
      return this;
   }

   public TranslationComponent setTranslator(@Nullable Translator translator) {
      if (translator == null) {
         this.translator = Translator.GLOBAL;
      } else {
         this.translator = translator;
      }

      return this;
   }

   public TextComponent resolveIntoComponents() {
      List<TextComponent> components = new ArrayList<>();
      String translated = this.translator.translate(this.key);
      if (translated == null) {
         translated = this.fallback;
      }

      if (translated == null) {
         translated = this.key;
      }

      Matcher matcher = ARG_PATTERN.matcher(translated);
      int argIndex = 0;
      int start = 0;

      while (matcher.find(start)) {
         int matchStart = matcher.start();
         int matchEnd = matcher.end();
         if (matchStart > start) {
            components.add(new StringComponent(String.format(translated.substring(start, matchStart))));
         }

         start = matchEnd;
         String argType = matcher.group(2);
         String match = translated.substring(matchStart, matchEnd);
         if (argType.equals("%") && match.equals("%%")) {
            components.add(new StringComponent("%"));
         } else {
            if (!argType.equals("s")) {
               throw new IllegalStateException("Unsupported format: '" + match + "'");
            }

            String rawIndex = matcher.group(1);
            int index;
            if (rawIndex == null) {
               index = argIndex++;
            } else {
               index = Integer.parseInt(rawIndex) - 1;
            }

            if (index < this.args.length) {
               Object arg = this.args[index];
               if (arg instanceof TextComponent) {
                  components.add((TextComponent)arg);
               } else if (arg == null) {
                  components.add(new StringComponent("null"));
               } else {
                  components.add(new StringComponent(arg.toString()));
               }
            }
         }
      }

      if (start < translated.length()) {
         components.add(new StringComponent(String.format(translated.substring(start))));
      }

      TextComponent out = new StringComponent();
      out.setStyle(this.getStyle());
      components.forEach(out::append);
      return out;
   }

   @Override
   public String asLegacyFormatString() {
      return this.resolveIntoComponents().asLegacyFormatString();
   }

   @Override
   public String asSingleString() {
      return this.resolveIntoComponents().asUnformattedString();
   }

   @Override
   public void asSingleString(ConsumerTracking converter, TextComponent.ComponentConsumer consumer) {
      if (consumer instanceof TranslationComponent.TranslatableContentConsumer) {
         if (converter != null) {
            converter.setCurrentConsumer(consumer);
         }

         this.resolveIntoComponents().visit(converter, consumer);
      } else {
         TranslationComponent.TranslatableContentConsumer translatableConsumer = new TranslationComponent.TranslatableContentConsumer(consumer);
         if (converter != null) {
            converter.setCurrentConsumer(translatableConsumer);
         }

         this.resolveIntoComponents().visit(converter, translatableConsumer);
      }
   }

   @Override
   public TextComponent shallowCopy() {
      Object[] copyArgs = new Object[this.args.length];

      for (int i = 0; i < this.args.length; i++) {
         Object arg = this.args[i];
         if (arg instanceof TextComponent) {
            copyArgs[i] = ((TextComponent)arg).copy();
         } else {
            copyArgs[i] = arg;
         }
      }

      TranslationComponent copy = new TranslationComponent(this.key, copyArgs);
      copy.translator = this.translator;
      return copy.setStyle(this.getStyle().copy());
   }

   @Override
   public String toString() {
      return ToString.of(this)
         .add("siblings", this.getSiblings(), siblings -> !siblings.isEmpty())
         .add("style", this.getStyle(), style -> !style.isEmpty())
         .add("key", this.key)
         .add("args", this.args, args -> args.length > 0, Arrays::toString)
         .add("translator", this.translator, translator -> translator != Translator.GLOBAL)
         .toString();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof TranslationComponent)) {
         return false;
      } else {
         TranslationComponent other = (TranslationComponent)o;
         if (!other.canEqual(this)) {
            return false;
         } else if (!super.equals(o)) {
            return false;
         } else {
            Object this$key = this.getKey();
            Object other$key = other.getKey();
            if (this$key == null ? other$key == null : this$key.equals(other$key)) {
               if (!Arrays.deepEquals(this.getArgs(), other.getArgs())) {
                  return false;
               } else {
                  Object this$fallback = this.getFallback();
                  Object other$fallback = other.getFallback();
                  if (this$fallback == null ? other$fallback == null : this$fallback.equals(other$fallback)) {
                     Object this$translator = this.translator;
                     Object other$translator = other.translator;
                     return this$translator == null ? other$translator == null : this$translator.equals(other$translator);
                  } else {
                     return false;
                  }
               }
            } else {
               return false;
            }
         }
      }
   }

   @Generated
   @Override
   protected boolean canEqual(Object other) {
      return other instanceof TranslationComponent;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = super.hashCode();
      Object $key = this.getKey();
      result = result * 59 + ($key == null ? 43 : $key.hashCode());
      result = result * 59 + Arrays.deepHashCode(this.getArgs());
      Object $fallback = this.getFallback();
      result = result * 59 + ($fallback == null ? 43 : $fallback.hashCode());
      Object $translator = this.translator;
      return result * 59 + ($translator == null ? 43 : $translator.hashCode());
   }

   private static final class TranslatableContentConsumer implements TextComponent.ComponentConsumer {
      private static final IllegalArgumentException EX = new IllegalArgumentException("Too long");
      private final TextComponent.ComponentConsumer runnable;
      private int visited;

      private TranslatableContentConsumer(TextComponent.ComponentConsumer runnable) {
         this.runnable = runnable;
      }

      public void accept(String s) {
         if (this.visited++ > 32) {
            throw EX;
         } else {
            this.runnable.accept(s);
         }
      }

      @Override
      public TextComponent.ComponentConsumer withConsumer(TextComponent.ComponentConsumer consumer) {
         TranslationComponent.TranslatableContentConsumer newConsumer = new TranslationComponent.TranslatableContentConsumer(consumer);
         newConsumer.visited = this.visited;
         return newConsumer;
      }
   }
}
