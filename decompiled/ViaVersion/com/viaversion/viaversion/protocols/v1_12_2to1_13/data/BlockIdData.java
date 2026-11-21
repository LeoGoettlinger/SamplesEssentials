package com.viaversion.viaversion.protocols.v1_12_2to1_13.data;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.gson.reflect.TypeToken;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.util.GsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

public class BlockIdData {
   public static final Map<String, String[]> blockIdMapping = new HashMap<>();
   public static final Map<String, String[]> fallbackReverseMapping = new HashMap<>();
   public static final Int2ObjectMap<String> numberIdToString = new Int2ObjectOpenHashMap<>();

   public static void init() {
      InputStream stream = MappingData1_13.class.getClassLoader().getResourceAsStream("assets/viaversion/data/blockIds1.12to1.13.json");

      try (InputStreamReader reader = new InputStreamReader(stream)) {
         Map<String, String[]> map = GsonUtil.getGson().fromJson(reader, (new TypeToken<Map<String, String[]>>() {}).getType());
         blockIdMapping.putAll(map);
         Map<String, List<String>> reverse = new HashMap<>();

         for (Entry<String, String[]> entry : blockIdMapping.entrySet()) {
            for (String val : entry.getValue()) {
               reverse.computeIfAbsent(val, k -> new ArrayList<>()).add(entry.getKey());
            }
         }

         reverse.forEach((valx, keys) -> fallbackReverseMapping.put(valx, keys.toArray(new String[0])));
      } catch (IOException var15) {
         Protocol1_12_2To1_13.LOGGER.log(Level.SEVERE, "Failed to load block id mappings", var15);
      }

      InputStream blockS = MappingData1_13.class.getClassLoader().getResourceAsStream("assets/viaversion/data/blockNumberToString1.12.json");

      try (InputStreamReader blockR = new InputStreamReader(blockS)) {
         Map<Integer, String> map = GsonUtil.getGson().fromJson(blockR, (new TypeToken<Map<Integer, String>>() {}).getType());
         numberIdToString.putAll(map);
      } catch (IOException var12) {
         Protocol1_12_2To1_13.LOGGER.log(Level.SEVERE, "Failed to load block number to string mappings", var12);
      }
   }
}
