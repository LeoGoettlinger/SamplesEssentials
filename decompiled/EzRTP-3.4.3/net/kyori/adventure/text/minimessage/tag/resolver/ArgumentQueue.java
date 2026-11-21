package net.kyori.adventure.text.minimessage.tag.resolver;

import java.util.function.Supplier;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ArgumentQueue {
   @NotNull
   Tag.Argument pop();

   @NotNull
   Tag.Argument popOr(@NotNull final String errorMessage);

   @NotNull
   Tag.Argument popOr(@NotNull final Supplier<String> errorMessage);

   @Nullable
   Tag.Argument peek();

   boolean hasNext();

   void reset();
}
