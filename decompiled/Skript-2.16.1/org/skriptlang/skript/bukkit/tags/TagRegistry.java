package org.skriptlang.skript.bukkit.tags;

import ch.njol.util.coll.iterator.CheckedIterator;
import com.destroystokyo.paper.MaterialTags;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterators;
import io.papermc.paper.tag.EntityTags;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.sources.BukkitTagSource;
import org.skriptlang.skript.bukkit.tags.sources.PaperTagSource;
import org.skriptlang.skript.bukkit.tags.sources.SkriptTagSource;
import org.skriptlang.skript.bukkit.tags.sources.TagOrigin;
import org.skriptlang.skript.bukkit.tags.sources.TagSource;

public class TagRegistry {
   private final TagRegistry.TagSourceMap tagSourceMap = new TagRegistry.TagSourceMap();

   TagRegistry() {
      this.tagSourceMap.put(TagType.ITEMS, new BukkitTagSource("items", TagType.ITEMS));
      this.tagSourceMap.put(TagType.BLOCKS, new BukkitTagSource("blocks", TagType.BLOCKS));
      this.tagSourceMap.put(TagType.ENTITIES, new BukkitTagSource("entity_types", TagType.ENTITIES));
      if (TagModule.PAPER_TAGS_EXIST) {
         try {
            List<Tag<Material>> itemTags = new ArrayList<>();
            List<Tag<Material>> blockTags = new ArrayList<>();
            List<Tag<Material>> blockAndItemTag = new ArrayList<>();

            for (Field field : MaterialTags.class.getDeclaredFields()) {
               if (field.canAccess(null)) {
                  Tag<Material> tag = (Tag<Material>)field.get(null);
                  boolean hasItem = false;
                  boolean hasBlock = false;

                  for (Material material : tag.getValues()) {
                     if (!hasBlock && material.isBlock()) {
                        blockTags.add(tag);
                        hasBlock = true;
                     }

                     if (!hasItem && material.isItem()) {
                        itemTags.add(tag);
                        hasItem = true;
                     }

                     if (hasItem && hasBlock) {
                        blockAndItemTag.add(tag);
                        break;
                     }
                  }
               }
            }

            PaperTagSource<Material> paperMaterialTags = new PaperTagSource(blockAndItemTag, TagType.BLOCKS, TagType.ITEMS);
            PaperTagSource<Material> paperItemTags = new PaperTagSource(itemTags, TagType.ITEMS);
            PaperTagSource<Material> paperBlockTags = new PaperTagSource(blockTags, TagType.BLOCKS);
            this.tagSourceMap.put(TagType.BLOCKS, paperMaterialTags);
            this.tagSourceMap.put(TagType.ITEMS, paperMaterialTags);
            this.tagSourceMap.put(TagType.BLOCKS, paperBlockTags);
            this.tagSourceMap.put(TagType.ITEMS, paperItemTags);
            List<Tag<EntityType>> entityTags = new ArrayList<>();

            for (Field fieldx : EntityTags.class.getDeclaredFields()) {
               if (fieldx.canAccess(null)) {
                  entityTags.add((Tag<EntityType>)fieldx.get(null));
               }
            }

            PaperTagSource<EntityType> paperEntityTags = new PaperTagSource(entityTags, TagType.ENTITIES);
            this.tagSourceMap.put(TagType.ENTITIES, paperEntityTags);
         } catch (IllegalAccessException var13) {
         }
      }

      SkriptTagSource.makeDefaultSources();
      this.tagSourceMap.put(TagType.ITEMS, SkriptTagSource.ITEMS());
      this.tagSourceMap.put(TagType.BLOCKS, SkriptTagSource.BLOCKS());
      this.tagSourceMap.put(TagType.ENTITIES, SkriptTagSource.ENTITIES());
   }

   public <T extends Keyed> Iterable<Tag<T>> getTags(TagOrigin origin, Class<T> typeClass, TagType<?>... types) {
      final List<Iterator<Tag<T>>> tagIterators = new ArrayList<>();
      if (types == null) {
         types = (TagType<?>[])this.tagSourceMap.map.keys().toArray(new TagType[0]);
      }

      for (TagType<?> type : types) {
         if (typeClass.isAssignableFrom(type.type())) {
            Iterator<Tag<T>> iterator = (Iterator<Tag<T>>)this.getTags(origin, type).iterator();
            if (iterator.hasNext()) {
               tagIterators.add(iterator);
            }
         }
      }

      return new Iterable<Tag<T>>() {
         @NotNull
         @Override
         public Iterator<Tag<T>> iterator() {
            return Iterators.concat(tagIterators.iterator());
         }
      };
   }

   public <T extends Keyed> Iterable<Tag<T>> getTags(TagOrigin origin, TagType<T> type) {
      if (!this.tagSourceMap.containsKey(type)) {
         return List.of();
      } else {
         final Iterator<TagSource<T>> tagSources = this.tagSourceMap.get(origin, type).iterator();
         return (Iterable<Tag<T>>)(!tagSources.hasNext() ? List.of() : new Iterable<Tag<T>>() {
            @NotNull
            @Override
            public Iterator<Tag<T>> iterator() {
               return Iterators.concat(new Iterator<Iterator<Tag<T>>>() {
                  @Override
                  public boolean hasNext() {
                     return tagSources.hasNext();
                  }

                  public Iterator<Tag<T>> next() {
                     return tagSources.next().getAllTags().iterator();
                  }
               });
            }
         });
      }
   }

   public <T extends Keyed> Iterable<Tag<T>> getMatchingTags(TagOrigin origin, TagType<T> type, final Predicate<Tag<T>> predicate) {
      final Iterator<Tag<T>> tagIterator = this.getTags(origin, type).iterator();
      return new Iterable<Tag<T>>() {
         @NotNull
         @Override
         public Iterator<Tag<T>> iterator() {
            return new CheckedIterator<>(tagIterator, predicate::test);
         }
      };
   }

   @Nullable
   public <T extends Keyed> Tag<T> getTag(TagOrigin origin, TagType<T> type, NamespacedKey key) {
      for (TagSource<T> source : this.tagSourceMap.get(origin, type)) {
         Tag<T> tag = source.getTag(key);
         if (tag != null) {
            return tag;
         }
      }

      return null;
   }

   private static class TagSourceMap {
      private final ArrayListMultimap<TagType<?>, TagSource<?>> map = ArrayListMultimap.create();

      public <T extends Keyed> void put(TagType<T> key, TagSource<T> value) {
         this.map.put(key, value);
      }

      @NotNull
      public <T extends Keyed> List<TagSource<T>> get(TagOrigin origin, TagType<T> key) {
         List<TagSource<T>> sources = new ArrayList<>();

         for (TagSource<?> source : this.map.get(key)) {
            if (source.getOrigin().matches(origin)) {
               sources.add((TagSource<T>)source);
            }
         }

         return sources;
      }

      public <T extends Keyed> boolean containsKey(TagType<T> type) {
         return this.map.containsKey(type);
      }
   }
}
