package de.oliver.fancynpcs.libs.chatcolorhandler.paper.resolver;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.ParserType;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResolverRegistry {
   private final Set<Resolver> resolvers = new HashSet<>();

   public void register(Resolver resolver) {
      this.resolvers.add(resolver);
   }

   public void register(Resolver... resolvers) {
      for (Resolver resolver : resolvers) {
         this.register(resolver);
      }
   }

   public TagResolver asTagResolver(@Nullable Audience audience, @NotNull List<Resolver> resolvers) {
      return TagResolver.builder().resolvers(resolvers.stream().map(resolver -> {
         try {
            return resolver.getResolver(audience);
         } catch (Throwable var3) {
            Bukkit.getLogger().log(Level.WARNING, "[ChatColorHandler] Failed to combine resolver: ", var3);
            return null;
         }
      }).filter(Objects::nonNull).toList()).build();
   }

   public Set<Resolver> values() {
      return this.resolvers;
   }

   public List<Resolver> ofTypes(@NotNull String... types) {
      return this.values().stream().filter(resolver -> {
         ParserType resolverType = resolver.getType();
         if (resolverType != null) {
            for (String type : types) {
               if (resolver.getType().equals(type)) {
                  return true;
               }
            }
         }

         return false;
      }).toList();
   }

   public List<Resolver> ofType(@NotNull String type) {
      return this.ofTypes(type);
   }
}
