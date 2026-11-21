package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.caption;

import de.theredend2000.advancedhunt.lib.cloud.caption.Caption;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TextReplacementConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL
)
final class PatternReplacingComponentCaptionFormatter<C> implements ComponentCaptionFormatter<C> {
   private final Pattern pattern;

   PatternReplacingComponentCaptionFormatter(@NonNull final Pattern pattern) {
      this.pattern = pattern;
   }

   @NonNull
   public Component formatCaption(
      @NonNull final Caption captionKey, @NonNull final C recipient, @NonNull final String caption, @NonNull final List<CaptionVariable> variables
   ) {
      Map<String, Component> replacements = new HashMap<>();

      for (CaptionVariable variable : variables) {
         if (variable instanceof RichVariable) {
            replacements.put(variable.key(), ((RichVariable)variable).component());
         } else {
            replacements.put(variable.key(), Component.text(variable.value()));
         }
      }

      TextReplacementConfig replacementConfig = TextReplacementConfig.builder()
         .match(this.pattern)
         .replacement((matcher, builder) -> replacements.getOrDefault(matcher.group(1), Component.text(matcher.group())))
         .build();
      return Component.text(caption).replaceText(replacementConfig);
   }
}
