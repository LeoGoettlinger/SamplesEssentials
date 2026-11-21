package net.kyori.adventure.text.minimessage.tag.resolver;

import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.internal.serializer.ClaimConsumer;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class EmptyTagResolver implements TagResolver, MappableResolver, SerializableResolver {
   static final EmptyTagResolver INSTANCE = new EmptyTagResolver();

   private EmptyTagResolver() {
   }

   @Nullable
   @Override
   public Tag resolve(@NotNull final String name, @NotNull final ArgumentQueue arguments, @NotNull final Context ctx) {
      return null;
   }

   @Override
   public boolean has(@NotNull final String name) {
      return false;
   }

   @Override
   public boolean contributeToMap(@NotNull final Map<String, Tag> map) {
      return true;
   }

   @Override
   public void handle(@NotNull final Component serializable, @NotNull final ClaimConsumer consumer) {
   }
}
