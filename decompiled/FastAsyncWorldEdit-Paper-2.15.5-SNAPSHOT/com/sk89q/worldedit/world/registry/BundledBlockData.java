package com.sk89q.worldedit.world.registry;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
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

public final class BundledBlockData {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static BundledBlockData INSTANCE;
   private final Map<String, BundledBlockData.BlockEntry> idMap = new HashMap<>();

   private BundledBlockData() {
      try {
         this.loadFromResource();
      } catch (Throwable var2) {
         LOGGER.warn("Failed to load the built-in block registry", var2);
      }
   }

   private void loadFromResource() throws IOException {
      GsonBuilder gsonBuilder = new GsonBuilder();
      gsonBuilder.registerTypeAdapter(Vector3.class, new VectorAdapter());
      gsonBuilder.registerTypeAdapter(int.class, (JsonDeserializer)(json, typeOfT, context) -> {
         JsonPrimitive primitive = (JsonPrimitive)json;
         if (primitive.isString()) {
            String value = primitive.getAsString();
            return value.charAt(0) == 35 ? Integer.parseInt(value.substring(1), 16) : Integer.parseInt(value);
         } else {
            return primitive.getAsInt();
         }
      });
      Gson gson = gsonBuilder.create();
      URL url = BundledRegistries.loadRegistry("blocks");
      String data = Resources.toString(url, Charset.defaultCharset());

      for (BundledBlockData.BlockEntry entry : (List)gson.fromJson(data, (new TypeToken<List<BundledBlockData.BlockEntry>>() {}).getType())) {
         this.idMap.put(entry.id, entry);
      }
   }

   @Nullable
   public BundledBlockData.BlockEntry findById(String id) {
      if (!id.contains(":")) {
         id = "minecraft:" + id;
      }

      return this.idMap.get(id);
   }

   @Nullable
   public BlockMaterial getMaterialById(String id) {
      BundledBlockData.BlockEntry entry = this.findById(id);
      return entry != null ? entry.material : null;
   }

   public static BundledBlockData getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new BundledBlockData();
      }

      return INSTANCE;
   }

   public static class BlockEntry {
      public String id;
      public String localizedName;
      public SimpleBlockMaterial material = new SimpleBlockMaterial();
   }
}
