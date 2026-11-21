package com.viaversion.viaversion.protocols.v1_12_2to1_13.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.CharStreams;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.BiMappings;
import com.viaversion.viaversion.api.data.Int2IntMapBiMappings;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.Int2IntBiHashMap;
import com.viaversion.viaversion.util.Key;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingData1_13 extends MappingDataBase {
   private final Map<String, int[]> blockTags = new HashMap<>();
   private final Map<String, int[]> itemTags = new HashMap<>();
   private final Map<String, int[]> fluidTags = new HashMap<>();
   private final BiMap<Short, String> oldEnchantmentsIds = HashBiMap.create();
   private final Map<String, String> translateMapping = new HashMap<>();
   private final Map<String, String> mojangTranslation = new HashMap<>();
   private final BiMap<String, String> channelMappings = HashBiMap.create();

   public MappingData1_13() {
      super("1.12", "1.13");
   }

   @Override
   protected void loadExtras(CompoundTag data) {
      this.loadTags(this.blockTags, data.getCompoundTag("block_tags"));
      this.loadTags(this.itemTags, data.getCompoundTag("item_tags"));
      this.loadTags(this.fluidTags, data.getCompoundTag("fluid_tags"));
      CompoundTag legacyEnchantments = data.getCompoundTag("legacy_enchantments");
      this.loadEnchantments(this.oldEnchantmentsIds, legacyEnchantments);
      if (Via.getConfig().isSnowCollisionFix()) {
         this.blockMappings.setNewId(1248, 3416);
      }

      if (Via.getConfig().isInfestedBlocksFix()) {
         this.blockMappings.setNewId(1552, 1);
         this.blockMappings.setNewId(1553, 14);
         this.blockMappings.setNewId(1554, 3983);
         this.blockMappings.setNewId(1555, 3984);
         this.blockMappings.setNewId(1556, 3985);
         this.blockMappings.setNewId(1557, 3986);
      }

      JsonObject object = MappingDataLoader.INSTANCE.loadFromDataDir("channelmappings-1.13.json");
      if (object != null) {
         for (Entry<String, JsonElement> entry : object.entrySet()) {
            String oldChannel = entry.getKey();
            String newChannel = entry.getValue().getAsString();
            if (!Key.isValid(newChannel)) {
               this.getLogger().warning("Channel '" + newChannel + "' is not a valid 1.13 plugin channel, please check your configuration!");
            } else {
               this.channelMappings.put(oldChannel, newChannel);
            }
         }
      }

      String[] translationLines;
      try (Reader reader = new InputStreamReader(
            MappingData1_13.class.getClassLoader().getResourceAsStream("assets/viaversion/data/mapping-lang-1.12-1.13.txt"), StandardCharsets.UTF_8
         )) {
         translationLines = CharStreams.toString(reader).split("\n");
      } catch (IOException var15) {
         throw new RuntimeException(var15);
      }

      for (String line : translationLines) {
         if (line.endsWith("\r")) {
            line = line.substring(0, line.length() - 1);
         }

         if (!line.isEmpty() && line.charAt(0) != '#') {
            int keySeparator = line.indexOf(61);
            if (keySeparator != -1) {
               String translation = line.substring(keySeparator + 1);
               if (line.charAt(0) == '-') {
                  this.mojangTranslation.put(line.substring(1, keySeparator), translation);
               } else {
                  int renameSeparator = line.indexOf(62);
                  if (renameSeparator != -1 && renameSeparator <= keySeparator) {
                     String key = line.substring(0, renameSeparator);
                     this.mojangTranslation.put(key, translation);
                     this.translateMapping.put(key, line.substring(renameSeparator + 1, keySeparator));
                  } else {
                     String key = line.substring(0, keySeparator);
                     this.mojangTranslation.put(key, translation);
                     this.translateMapping.put(key, key);
                  }
               }
            }
         }
      }
   }

   @Nullable
   @Override
   protected Mappings loadMappings(CompoundTag data, String key) {
      if (key.equals("blocks")) {
         return super.loadMappings(data, "blockstates");
      } else {
         return key.equals("blockstates") ? null : super.loadMappings(data, key);
      }
   }

   @Nullable
   @Override
   protected BiMappings loadBiMappings(CompoundTag data, String key) {
      return key.equals("items") ? (BiMappings)MappingDataLoader.INSTANCE.loadMappings(data, "items", size -> {
         Int2IntBiHashMap map = new Int2IntBiHashMap(size);
         map.defaultReturnValue(-1);
         return map;
      }, Int2IntBiHashMap::put, (v, mappedSize) -> Int2IntMapBiMappings.of(v)) : super.loadBiMappings(data, key);
   }

   public static String validateNewChannel(String newId) {
      return !Key.isValid(newId) ? null : Key.namespaced(newId);
   }

   private void loadTags(Map<String, int[]> output, CompoundTag newTags) {
      for (Entry<String, Tag> entry : newTags.entrySet()) {
         IntArrayTag ids = (IntArrayTag)entry.getValue();
         output.put(Key.namespaced(entry.getKey()), ids.getValue());
      }
   }

   private void loadEnchantments(Map<Short, String> output, CompoundTag enchantments) {
      for (Entry<String, Tag> enty : enchantments.entrySet()) {
         output.put(Short.parseShort(enty.getKey()), ((StringTag)enty.getValue()).getValue());
      }
   }

   public Map<String, int[]> getBlockTags() {
      return this.blockTags;
   }

   public Map<String, int[]> getItemTags() {
      return this.itemTags;
   }

   public Map<String, int[]> getFluidTags() {
      return this.fluidTags;
   }

   public BiMap<Short, String> getOldEnchantmentsIds() {
      return this.oldEnchantmentsIds;
   }

   public Map<String, String> getTranslateMapping() {
      return this.translateMapping;
   }

   public Map<String, String> getMojangTranslation() {
      return this.mojangTranslation;
   }

   public BiMap<String, String> getChannelMappings() {
      return this.channelMappings;
   }
}
