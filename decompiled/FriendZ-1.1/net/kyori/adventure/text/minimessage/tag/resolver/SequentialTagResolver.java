package net.kyori.adventure.text.minimessage.tag.resolver;

import java.util.Arrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.ClaimConsumer;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SequentialTagResolver implements TagResolver, SerializableResolver {
   final TagResolver[] resolvers;

   SequentialTagResolver(@NotNull final TagResolver[] resolvers) {
      this.resolvers = resolvers;
   }

   @Nullable
   @Override
   public Tag resolve(@NotNull final String name, @NotNull final ArgumentQueue arguments, @NotNull final Context ctx) throws ParsingException {
      ParsingException thrown = null;

      for (TagResolver resolver : this.resolvers) {
         try {
            Tag placeholder = resolver.resolve(name, arguments, ctx);
            if (placeholder != null) {
               return placeholder;
            }
         } catch (ParsingException var11) {
            arguments.reset();
            if (thrown == null) {
               thrown = var11;
            } else {
               thrown.addSuppressed(var11);
            }
         } catch (Exception var12) {
            arguments.reset();
            ParsingException err = ctx.newException("Exception thrown while parsing <" + name + ">", var12, arguments);
            if (thrown == null) {
               thrown = err;
            } else {
               thrown.addSuppressed(err);
            }
         }
      }

      if (thrown != null) {
         throw thrown;
      } else {
         return null;
      }
   }

   @Override
   public boolean has(@NotNull final String name) {
      for (TagResolver resolver : this.resolvers) {
         if (resolver.has(name)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void handle(@NotNull final Component serializable, @NotNull final ClaimConsumer consumer) {
      for (TagResolver resolver : this.resolvers) {
         if (resolver instanceof SerializableResolver) {
            ((SerializableResolver)resolver).handle(serializable, consumer);
         }
      }
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (other == this) {
         return true;
      } else if (!(other instanceof SequentialTagResolver)) {
         return false;
      } else {
         SequentialTagResolver that = (SequentialTagResolver)other;
         return Arrays.equals((Object[])this.resolvers, (Object[])that.resolvers);
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode((Object[])this.resolvers);
   }
}
