package com.sk89q.worldedit.world.registry;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.gson.VectorAdapter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public final class BundledItemData {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static BundledItemData INSTANCE;
   private final Map<String, BundledItemData.ItemEntry> idMap = new HashMap<>();

   private BundledItemData() {
      try {
         this.loadFromResource();
      } catch (Throwable var2) {
         LOGGER.warn("Failed to load the built-in item registry", var2);
      }
   }

   private void loadFromResource() throws IOException {
      GsonBuilder gsonBuilder = new GsonBuilder();
      gsonBuilder.registerTypeAdapter(Vector3.class, new VectorAdapter());
      Gson gson = gsonBuilder.create();
      URL url = BundledRegistries.loadRegistry("items");
      LOGGER.debug("Using {} for bundled item data.", url);
      String data = Resources.toString(url, Charset.defaultCharset());

      for (BundledItemData.ItemEntry entry : (List)gson.fromJson(data, (new TypeToken<List<BundledItemData.ItemEntry>>() {}).getType())) {
         this.idMap.put(entry.id, entry);
      }
   }

   @Nullable
   public BundledItemData.ItemEntry findById(String id) {
      if (!id.contains(":")) {
         id = "minecraft:" + id;
      }

      return this.idMap.get(id);
   }

   @Nullable
   public ItemMaterial getMaterialById(String id) {
      BundledItemData.ItemEntry entry = this.findById(id);
      return entry != null ? new SimpleItemMaterial(entry.maxStackSize, entry.maxDamage) : null;
   }

   public static BundledItemData getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new BundledItemData();
      }

      return INSTANCE;
   }

   public static class ItemEntry {
      private String id;
      private String unlocalizedName;
      public String localizedName;
      private int maxDamage;
      private int maxStackSize;
   }
}
