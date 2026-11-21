package su.nightexpress.nightcore.util.text.night;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.text.night.tag.TagHandler;
import su.nightexpress.nightcore.util.text.night.tag.TagHandlerRegistry;

public class ParserResolvers {
   private final Map<String, ParserResolvers.Index> indexes = new HashMap<>();

   public void clear() {
      this.indexes.clear();
   }

   @NonNull
   public List<TagHandler> removeAll() {
      List<TagHandler> resolvers = this.indexes.values().stream().flatMap(index -> index.resolvers.stream()).toList();
      this.clear();
      return resolvers;
   }

   @Nullable
   public TagHandler createFor(@NonNull String tagName) {
      String name = LowerCase.INTERNAL.apply(tagName);
      TagHandler resolver = TagHandlerRegistry.create(name);
      if (resolver == null) {
         return null;
      } else {
         ParserResolvers.Index index = this.indexes.computeIfAbsent(name, k -> new ParserResolvers.Index());
         return index.add(resolver);
      }
   }

   @Nullable
   public TagHandler removeFor(@NonNull String tagName) {
      String name = LowerCase.INTERNAL.apply(tagName);
      ParserResolvers.Index index = this.indexes.get(name);
      return index == null ? null : index.removeLast();
   }

   static class Index {
      private final List<TagHandler> resolvers = new ArrayList<>();

      public Index() {
      }

      @NonNull
      public TagHandler add(@NonNull TagHandler resolver) {
         this.resolvers.add(resolver);
         return resolver;
      }

      @Nullable
      public TagHandler removeLast() {
         return this.resolvers.isEmpty() ? null : this.resolvers.removeLast();
      }
   }
}
