package me.clip.placeholderapi.libs.kyori.adventure.platform.facet;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.libs.kyori.adventure.text.Component;
import me.clip.placeholderapi.libs.kyori.adventure.text.TranslatableComponent;
import me.clip.placeholderapi.libs.kyori.adventure.text.flattener.ComponentFlattener;
import me.clip.placeholderapi.libs.kyori.adventure.translation.GlobalTranslator;
import me.clip.placeholderapi.libs.kyori.adventure.translation.TranslationRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class FacetComponentFlattener {
   private static final Pattern LOCALIZATION_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?s");

   private FacetComponentFlattener() {
   }

   public static <V> ComponentFlattener get(final V instance, final Collection<? extends FacetComponentFlattener.Translator<V>> candidates) {
      FacetComponentFlattener.Translator<V> translator = Facet.of((Collection<FacetComponentFlattener.Translator<V>>)candidates, instance);
      ComponentFlattener.Builder flattenerBuilder = ComponentFlattener.basic().toBuilder();
      flattenerBuilder.complexMapper(TranslatableComponent.class, (translatable, consumer) -> {
         String key = translatable.key();

         for (me.clip.placeholderapi.libs.kyori.adventure.translation.Translator registry : GlobalTranslator.translator().sources()) {
            if (registry instanceof TranslationRegistry && ((TranslationRegistry)registry).contains(key)) {
               consumer.accept(GlobalTranslator.render(translatable, Locale.getDefault()));
               return;
            }
         }

         String translated = translator == null ? key : translator.valueOrDefault(instance, key);
         Matcher matcher = LOCALIZATION_PATTERN.matcher(translated);
         List<Component> args = translatable.args();
         int argPosition = 0;
         int lastIdx = 0;

         while (matcher.find()) {
            if (lastIdx < matcher.start()) {
               consumer.accept(Component.text(translated.substring(lastIdx, matcher.start())));
            }

            lastIdx = matcher.end();
            String argIdx = matcher.group(1);
            if (argIdx != null) {
               try {
                  int idx = Integer.parseInt(argIdx) - 1;
                  if (idx < args.size()) {
                     consumer.accept(args.get(idx));
                  }
               } catch (NumberFormatException var12) {
               }
            } else {
               int idx = argPosition++;
               if (idx < args.size()) {
                  consumer.accept(args.get(idx));
               }
            }
         }

         if (lastIdx < translated.length()) {
            consumer.accept(Component.text(translated.substring(lastIdx)));
         }
      });
      return flattenerBuilder.build();
   }

   public interface Translator<V> extends Facet<V> {
      @NotNull
      String valueOrDefault(@NotNull final V game, @NotNull final String key);
   }
}
