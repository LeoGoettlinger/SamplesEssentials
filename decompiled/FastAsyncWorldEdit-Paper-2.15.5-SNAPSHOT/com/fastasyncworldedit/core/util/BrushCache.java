package com.fastasyncworldedit.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class BrushCache {
   private static final WeakHashMap<Object, BrushTool> brushCache = new WeakHashMap<>();
   private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private static final ThreadLocal<Boolean> RECURSION = new ThreadLocal<>();

   private static CompoundTag getNBT(BaseItem item) {
      return item.hasNbtData() ? item.getNbtData() : null;
   }

   private static Object getKey(BaseItem item) {
      return item.getNativeItem();
   }

   public static BrushTool getTool(Player player, LocalSession session, BaseItem item) {
      if (!item.hasNbtData()) {
         return null;
      } else {
         Object key = getKey(item);
         if (key == null) {
            return null;
         } else {
            BrushTool cached = brushCache.get(key);
            if (cached != null) {
               return cached;
            } else {
               CompoundTag nbt = item.getNbtData();
               if (nbt == null) {
                  return null;
               } else {
                  StringTag json = (StringTag)nbt.getValue().get("weBrushJson");
                  return null;
               }
            }
         }
      }
   }

   public static BrushTool getCachedTool(BaseItem item) {
      Object key = getKey(item);
      return key != null ? brushCache.get(key) : null;
   }

   public static BrushTool setTool(BaseItem item, BrushTool tool) {
      if (item.getNativeItem() == null) {
         return null;
      } else {
         CompoundTag nbt = item.getNbtData();
         Map<String, Tag<?, ?>> map;
         if (nbt == null) {
            if (tool == null) {
               item.setNbtData(null);
               return tool;
            }

            nbt = new CompoundTag(map = new HashMap<>());
         } else {
            map = nbt.getValue();
         }

         item.setNbtData(nbt);
         brushCache.remove(getKey(item));
         CompoundTag display = (CompoundTag)map.get("display");
         return tool;
      }
   }
}
