package com.viaversion.viabackwards.protocol.v1_18to1_17_1.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectIterator;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap.Entry;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.Protocol1_17_1To1_18;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.data.BlockEntities1_18;

public final class BackwardsMappingData1_18 extends BackwardsMappingData {
   private final Int2ObjectMap<String> blockEntities = new Int2ObjectOpenHashMap();

   public BackwardsMappingData1_18() {
      super("1.18", "1.17", Protocol1_17_1To1_18.class);
   }

   @Override
   protected void loadExtras(CompoundTag data) {
      super.loadExtras(data);
      ObjectIterator var2 = BlockEntities1_18.blockEntityIds().object2IntEntrySet().iterator();

      while (var2.hasNext()) {
         Entry<String> entry = (Entry<String>)var2.next();
         this.blockEntities.put(entry.getIntValue(), (String)entry.getKey());
      }
   }

   public Int2ObjectMap<String> blockEntities() {
      return this.blockEntities;
   }
}
