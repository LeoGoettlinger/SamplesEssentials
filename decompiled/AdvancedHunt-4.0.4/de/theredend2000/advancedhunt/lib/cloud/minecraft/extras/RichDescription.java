package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras;

import de.theredend2000.advancedhunt.lib.cloud.description.Description;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.translation.GlobalTranslator;
import java.util.Locale;
import java.util.Objects;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class RichDescription implements Description {
   private static final RichDescription EMPTY = new RichDescription(Component.empty());
   private final Component contents;

   RichDescription(@NonNull final Component contents) {
      this.contents = contents;
   }

   @NonNull
   public static RichDescription empty() {
      return EMPTY;
   }

   @NonNull
   public static RichDescription of(@NonNull final ComponentLike contents) {
      Component componentContents = Objects.requireNonNull(contents, "contents").asComponent();
      return Component.empty().equals(componentContents) ? EMPTY : new RichDescription(componentContents);
   }

   @NonNull
   public static RichDescription richDescription(@NonNull final ComponentLike contents) {
      return of(contents);
   }

   @NonNull
   public static RichDescription translatable(@NonNull final String key) {
      Objects.requireNonNull(key, "key");
      return new RichDescription(Component.translatable(key));
   }

   @NonNull
   public static RichDescription translatable(@NonNull final String key, @NonNull final ComponentLike... args) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(args, "args");
      return new RichDescription(Component.translatable(key, args));
   }

   @Deprecated
   @NonNull
   @Override
   public String textDescription() {
      return PlainComponentSerializer.plain().serialize(GlobalTranslator.render(this.contents, Locale.getDefault()));
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public Component contents() {
      return this.contents;
   }

   @Override
   public boolean isEmpty() {
      return Component.empty().equals(this.contents);
   }

   @Override
   public boolean equals(final Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         RichDescription that = (RichDescription)object;
         return Objects.equals(this.contents, that.contents);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.contents);
   }
}
