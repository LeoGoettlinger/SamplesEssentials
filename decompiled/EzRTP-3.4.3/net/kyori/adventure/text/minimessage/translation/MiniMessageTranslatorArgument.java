package net.kyori.adventure.text.minimessage.translation;

import java.util.Objects;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.VirtualComponentRenderer;
import net.kyori.adventure.text.minimessage.internal.TagInternals;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

final class MiniMessageTranslatorArgument<T> implements VirtualComponentRenderer<Void> {
   @NotNull
   private final String name;
   @NotNull
   private final T data;

   MiniMessageTranslatorArgument(@TagPattern @NotNull final String name, @NotNull final T data) {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(data, "data");
      TagInternals.assertValidTagName(name);
      this.name = name;
      this.data = data;
   }

   @NotNull
   public String name() {
      return this.name;
   }

   @NotNull
   public T data() {
      return this.data;
   }

   @UnknownNullability
   public ComponentLike apply(@NotNull final Void context) {
      return this.data instanceof ComponentLike ? (ComponentLike)this.data : null;
   }
}
