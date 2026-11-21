package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.caption;

import de.theredend2000.advancedhunt.lib.cloud.caption.Caption;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionFormatter;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.minimessage.MiniMessage;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE,
   since = "2.0.0"
)
public interface ComponentCaptionFormatter<C> extends CaptionFormatter<C, Component> {
   @NonNull
   static <C> ComponentCaptionFormatter<C> miniMessage() {
      return miniMessage(MiniMessage.miniMessage());
   }

   @NonNull
   static <C> ComponentCaptionFormatter<C> miniMessage(@NonNull final MiniMessage miniMessage) {
      return miniMessage(miniMessage);
   }

   @NonNull
   static <C> ComponentCaptionFormatter<C> miniMessage(@NonNull final MiniMessage miniMessage, @NonNull final TagResolver... resolvers) {
      return new MiniMessageComponentCaptionFormatter<>(miniMessage, Arrays.asList(resolvers));
   }

   @API(
      status = API.Status.EXPERIMENTAL
   )
   @NonNull
   static <C> ComponentCaptionFormatter<C> translatable() {
      return new ComponentCaptionFormatter<C>() {
         @NonNull
         public Component formatCaption(
            @NonNull final Caption captionKey, @NonNull final C recipient, @NonNull final String caption, @NonNull final List<CaptionVariable> variables
         ) {
            return Component.translatable(
               captionKey.key(),
               variables.stream()
                  .map(variable -> (ComponentLike)(variable instanceof RichVariable ? (RichVariable)variable : Component.text(variable.value())))
                  .collect(Collectors.toList())
            );
         }
      };
   }

   @NonNull
   static <C> ComponentCaptionFormatter<C> patternReplacing(@NonNull final Pattern pattern) {
      return new PatternReplacingComponentCaptionFormatter<>(pattern);
   }

   @NonNull
   static <C> ComponentCaptionFormatter<C> placeholderReplacing() {
      return new PatternReplacingComponentCaptionFormatter<>(CaptionFormatter.placeholderPattern());
   }
}
