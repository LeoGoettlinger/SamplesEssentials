package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.caption;

import de.theredend2000.advancedhunt.lib.cloud.caption.Caption;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.minimessage.MiniMessage;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.minimessage.tag.resolver.TagResolver.Builder;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL
)
final class MiniMessageComponentCaptionFormatter<C> implements ComponentCaptionFormatter<C> {
   private final MiniMessage miniMessage;
   private final List<TagResolver> extraResolvers;

   MiniMessageComponentCaptionFormatter(@NonNull final MiniMessage miniMessage, @NonNull final List<TagResolver> extraResolvers) {
      this.miniMessage = miniMessage;
      this.extraResolvers = extraResolvers;
   }

   @NonNull
   public Component formatCaption(
      @NonNull final Caption captionKey, @NonNull final C recipient, @NonNull final String caption, @NonNull final List<CaptionVariable> variables
   ) {
      Builder builder = TagResolver.builder();
      builder.resolvers(this.extraResolvers);

      for (CaptionVariable variable : variables) {
         String key = variable.key();
         if (variable instanceof RichVariable) {
            builder.resolver(Placeholder.component(key, ((RichVariable)variable).component()));
         } else {
            builder.resolver(Placeholder.parsed(key, variable.value()));
         }
      }

      return this.miniMessage.deserialize(caption, builder.build());
   }
}
