package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers;

import java.util.List;
import java.util.logging.Level;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.Builder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Resolver extends Parser {
   @Override
   default String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return string;
   }

   @NotNull
   TagResolver getResolver();

   @NotNull
   default TagResolver getResolver(Audience audience) {
      return this.getResolver();
   }

   static TagResolver combineResolvers(@Nullable Audience audience, @NotNull List<Resolver> resolvers) {
      Builder tagResolver = TagResolver.builder();

      for (Resolver resolver : resolvers) {
         try {
            tagResolver.resolver(audience != null ? resolver.getResolver(audience) : resolver.getResolver());
         } catch (Throwable var6) {
            Bukkit.getLogger().log(Level.WARNING, "[ChatColorHandler] Failed to combine resolver: ", var6);
         }
      }

      return tagResolver.build();
   }
}
