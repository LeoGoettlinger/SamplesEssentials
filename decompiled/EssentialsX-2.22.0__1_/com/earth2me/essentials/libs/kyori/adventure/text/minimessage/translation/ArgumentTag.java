package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.translation;

import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.Context;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.ParsingException;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.Tag;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ArgumentTag implements TagResolver {
   private static final String NAME = "argument";
   private static final String NAME_1 = "arg";
   private final List<Tag> arguments;
   private final TagResolver tagResolver;

   ArgumentTag(@NotNull final List<Tag> arguments, @NotNull final TagResolver tagResolver) {
      this.arguments = arguments;
      this.tagResolver = tagResolver;
   }

   @Nullable
   @Override
   public Tag resolve(@NotNull final String name, @NotNull final ArgumentQueue arguments, @NotNull final Context ctx) throws ParsingException {
      if (!name.equals("argument") && !name.equals("arg")) {
         return this.tagResolver.resolve(name, arguments, ctx);
      } else {
         int index = arguments.popOr("No argument number provided").asInt().orElseThrow(() -> ctx.newException("Invalid argument number", arguments));
         if (index >= 0 && index < this.arguments.size()) {
            return this.arguments.get(index);
         } else {
            throw ctx.newException("Invalid argument number", arguments);
         }
      }
   }

   @Override
   public boolean has(@NotNull final String name) {
      return name.equals("argument") || name.equals("arg") || this.tagResolver.has(name);
   }
}
