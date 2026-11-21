package org.skriptlang.skript.bukkit.tags.sources;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagType;

public sealed class CustomTagSource<T extends Keyed> extends TagSource<T> permits PaperTagSource, SkriptTagSource {
   final Map<NamespacedKey, Tag<T>> tags = new ConcurrentHashMap<>();

   @SafeVarargs
   CustomTagSource(TagOrigin origin, @NotNull Iterable<Tag<T>> tags, TagType<T>... types) {
      super(origin, types);

      for (Tag<T> tag : tags) {
         this.tags.put(tag.getKey(), tag);
      }
   }

   @Override
   public Iterable<Tag<T>> getAllTags() {
      return Collections.unmodifiableCollection(this.tags.values());
   }

   @Nullable
   @Override
   public Tag<T> getTag(NamespacedKey key) {
      return this.tags.get(key);
   }
}
