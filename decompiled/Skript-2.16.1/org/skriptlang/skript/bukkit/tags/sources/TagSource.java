package org.skriptlang.skript.bukkit.tags.sources;

import ch.njol.util.coll.iterator.CheckedIterator;
import java.util.Iterator;
import java.util.function.Predicate;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.TagType;

public abstract class TagSource<T extends Keyed> {
   private final TagType<T>[] types;
   private final TagOrigin origin;

   @SafeVarargs
   protected TagSource(TagOrigin origin, TagType<T>... types) {
      this.types = types;
      this.origin = origin;
   }

   public abstract Iterable<Tag<T>> getAllTags();

   public Iterable<Tag<T>> getAllTagsMatching(final Predicate<Tag<T>> predicate) {
      final Iterator<Tag<T>> tagIterator = this.getAllTags().iterator();
      return new Iterable<Tag<T>>() {
         @NotNull
         @Override
         public Iterator<Tag<T>> iterator() {
            return new CheckedIterator<>(tagIterator, predicate::test);
         }
      };
   }

   @Nullable
   public abstract Tag<T> getTag(NamespacedKey var1);

   public TagType<T>[] getTypes() {
      return this.types;
   }

   public TagOrigin getOrigin() {
      return this.origin;
   }
}
