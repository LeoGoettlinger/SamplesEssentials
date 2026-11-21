package com.sk89q.worldedit.blocks;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.Collection;
import java.util.Map;

public final class Blocks {
   private Blocks() {
   }

   public static <B extends BlockStateHolder<B>> boolean containsFuzzy(Collection<? extends BlockStateHolder<?>> collection, B o) {
      for (BlockStateHolder<?> b : collection) {
         if (b.equalsFuzzy(o)) {
            return true;
         }
      }

      return false;
   }

   public static Map<Property<Object>, Object> resolveProperties(Map<String, String> states, BlockType type) {
      Map<String, ? extends Property<?>> existing = type.getPropertyMap();
      Map<Property<Object>, Object> newMap = Maps.newHashMap();
      states.forEach((key, value) -> {
         Property<Object> prop = (Property<Object>)existing.get(key);
         if (prop != null) {
            Object val = null;

            try {
               val = prop.getValueFor(value);
            } catch (IllegalArgumentException var7) {
            }

            if (val != null) {
               newMap.put(prop, val);
            }
         }
      });
      return newMap;
   }
}
