package com.sk89q.worldedit.util.gson;

import com.fastasyncworldedit.core.util.gson.BaseItemAdapter;
import com.fastasyncworldedit.core.util.gson.ItemTypeAdapter;
import com.fastasyncworldedit.core.util.gson.RegionSelectorAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.world.item.ItemType;

public final class GsonUtil {
   private static final Gson gson = new Gson();

   private GsonUtil() {
   }

   public static GsonBuilder createBuilder() {
      GsonBuilder gsonBuilder = new GsonBuilder();
      gsonBuilder.registerTypeAdapter(Vector3.class, new VectorAdapter());
      gsonBuilder.registerTypeAdapter(BlockVector3.class, new BlockVectorAdapter());
      gsonBuilder.registerTypeAdapter(RegionSelector.class, new RegionSelectorAdapter());
      gsonBuilder.registerTypeAdapter(ItemType.class, new ItemTypeAdapter());
      gsonBuilder.registerTypeAdapter(BaseItem.class, new BaseItemAdapter());
      return gsonBuilder;
   }

   public static String stringValue(String s) {
      return gson.toJson(s);
   }
}
