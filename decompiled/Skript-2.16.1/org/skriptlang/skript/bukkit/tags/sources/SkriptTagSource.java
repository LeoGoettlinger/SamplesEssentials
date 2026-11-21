package org.skriptlang.skript.bukkit.tags.sources;

import ch.njol.util.coll.iterator.EmptyIterable;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.skriptlang.skript.bukkit.tags.TagType;

public final class SkriptTagSource<T extends Keyed> extends CustomTagSource<T> {
   private static SkriptTagSource<Material> ITEMS;
   private static SkriptTagSource<Material> BLOCKS;
   private static SkriptTagSource<EntityType> ENTITIES;

   public static void makeDefaultSources() {
      ITEMS = new SkriptTagSource(TagType.ITEMS);
      BLOCKS = new SkriptTagSource(TagType.BLOCKS);
      ENTITIES = new SkriptTagSource(TagType.ENTITIES);
   }

   @SafeVarargs
   private SkriptTagSource(TagType<T>... types) {
      super(TagOrigin.SKRIPT, new EmptyIterable<>(), types);
   }

   public void addTag(Tag<T> tag) {
      this.tags.put(tag.getKey(), tag);
   }

   public static SkriptTagSource<Material> ITEMS() {
      return ITEMS;
   }

   public static SkriptTagSource<Material> BLOCKS() {
      return BLOCKS;
   }

   public static SkriptTagSource<EntityType> ENTITIES() {
      return ENTITIES;
   }
}
