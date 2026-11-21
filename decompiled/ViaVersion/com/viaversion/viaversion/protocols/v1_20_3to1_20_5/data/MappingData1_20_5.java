package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectSet;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappingData1_20_5 extends MappingDataBase {
   private final Object2ObjectMap<String, CompoundTag> damageTypes = new Object2ObjectOpenHashMap<>();

   public MappingData1_20_5() {
      super("1.20.3", "1.20.5");
   }

   @Override
   protected void loadExtras(CompoundTag data) {
      super.loadExtras(data);
      CompoundTag damageTypes = MappingDataLoader.INSTANCE.loadNBT("damage-types-1.20.3.nbt");

      for (String key : damageTypes.keySet()) {
         this.damageTypes.put(key, damageTypes.getCompoundTag(key));
      }
   }

   public int blockId(String name) {
      return Protocol1_20_3To1_20_5.MAPPINGS.getFullBlockMappings().id(name);
   }

   @Nullable
   public String blockName(int id) {
      return Protocol1_20_3To1_20_5.MAPPINGS.getFullBlockMappings().identifier(id);
   }

   public int soundId(String name) {
      return Protocol1_20_3To1_20_5.MAPPINGS.getFullSoundMappings().id(name);
   }

   @Nullable
   public String soundName(int id) {
      return Protocol1_20_3To1_20_5.MAPPINGS.getFullSoundMappings().identifier(id);
   }

   public CompoundTag damageType(String key) {
      return this.damageTypes.get(key).copy();
   }

   public ObjectSet<String> damageKeys() {
      return this.damageTypes.keySet();
   }
}
