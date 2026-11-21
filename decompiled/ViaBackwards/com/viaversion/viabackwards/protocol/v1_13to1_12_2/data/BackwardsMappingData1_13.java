package com.viaversion.viabackwards.protocol.v1_13to1_12_2.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viabackwards.api.data.MappedItem;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectIterator;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.ConnectionData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.BlockStates1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.StatisticMappings1_13;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BackwardsMappingData1_13 extends BackwardsMappingData {
   private final Int2ObjectMap<String> statisticMappings = new Int2ObjectOpenHashMap();
   private final Map<String, String> translateMappings = new HashMap<>();
   private final Object2IntMap<String> pistonIds = new Object2IntOpenHashMap();

   public BackwardsMappingData1_13() {
      super("1.13", "1.12", Protocol1_12_2To1_13.class);
   }

   @Override
   public void loadExtras(CompoundTag data) {
      super.loadExtras(data);

      for (Entry<String, Integer> entry : StatisticMappings1_13.CUSTOM_STATS.entrySet()) {
         this.statisticMappings.put(entry.getValue(), entry.getKey());
      }

      for (Entry<String, String> entry : Protocol1_12_2To1_13.MAPPINGS.getTranslateMapping().entrySet()) {
         this.translateMappings.put(entry.getValue(), entry.getKey());
      }

      this.pistonIds.defaultReturnValue(-1);
      Object2IntMap<String> keyToId = ConnectionData.getKeyToId();
      if (!keyToId.isEmpty()) {
         ObjectIterator var8 = keyToId.object2IntEntrySet().iterator();

         while (var8.hasNext()) {
            com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap.Entry<String> entry = (com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap.Entry<String>)var8.next();
            if (((String)entry.getKey()).contains("piston")) {
               this.addPistonEntries((String)entry.getKey(), entry.getIntValue());
            }
         }
      } else {
         CompoundTag blockStatesData = MappingDataLoader.INSTANCE.loadNBT("blockstates-1.13.nbt");
         BlockStates1_13.forEach(blockStatesData, (key, id) -> {
            if (key.contains("piston")) {
               this.addPistonEntries(key, id);
            }
         });
      }
   }

   @Override
   protected void loadBackwardsItemMappings(CompoundTag itemCustomModelDatas) {
      for (Entry<String, Tag> entry : itemCustomModelDatas.entrySet()) {
         int id = Integer.parseInt(entry.getKey());
         int customModelData = ((NumberTag)entry.getValue()).asInt();
         String identifier = BackwardsItemMappings.identifier(id);
         String fallbackName = this.unmappedVersion + " " + nameFromIdentifier(identifier);
         this.backwardsItemMappings.put(id, new MappedItem(this.getNewItemId(id), identifier, fallbackName, customModelData));
      }
   }

   private void addPistonEntries(String data, int id) {
      id = this.getNewBlockStateId(id);
      this.pistonIds.put(data, id);
      String substring = data.substring(10);
      if (substring.startsWith("piston") || substring.startsWith("sticky_piston")) {
         String[] split = data.substring(0, data.length() - 1).split("\\[");
         String[] properties = split[1].split(",");
         data = split[0] + "[" + properties[1] + "," + properties[0] + "]";
         this.pistonIds.put(data, id);
      }
   }

   public int getNewBlockStateId(int id) {
      if (id >= 5635 && id <= 5650) {
         int mappedId = super.getNewBlockStateId(id);
         if (mappedId >= 2400) {
            mappedId -= 16;
         }

         return mappedId;
      } else {
         int mappedId = super.getNewBlockStateId(id);

         return switch (mappedId) {
            case 1595, 1596, 1597 -> 1584;
            default -> mappedId;
            case 1611, 1612, 1613 -> 1600;
         };
      }
   }

   protected int checkValidity(int id, int mappedId, String type) {
      return mappedId;
   }

   public Int2ObjectMap<String> getStatisticMappings() {
      return this.statisticMappings;
   }

   public Map<String, String> getTranslateMappings() {
      return this.translateMappings;
   }

   public Object2IntMap<String> getPistonIds() {
      return this.pistonIds;
   }
}
