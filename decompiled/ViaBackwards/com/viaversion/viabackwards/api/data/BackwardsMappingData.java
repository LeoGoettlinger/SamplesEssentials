package com.viaversion.viabackwards.api.data;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.BiMappings;
import com.viaversion.viaversion.api.data.IdentityMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectArrayMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.util.Key;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BackwardsMappingData extends MappingDataBase {
   private final Class<? extends Protocol<?, ?, ?, ?>> vvProtocolClass;
   protected Int2ObjectMap<MappedItem> backwardsItemMappings;
   private Int2ObjectMap<String> enchantmentNames;

   public BackwardsMappingData(String unmappedVersion, String mappedVersion) {
      this(unmappedVersion, mappedVersion, null);
   }

   public BackwardsMappingData(String unmappedVersion, String mappedVersion, @Nullable Class<? extends Protocol<?, ?, ?, ?>> vvProtocolClass) {
      super(unmappedVersion, mappedVersion);
      Preconditions.checkArgument(vvProtocolClass == null || !BackwardsProtocol.class.isAssignableFrom(vvProtocolClass));
      this.vvProtocolClass = vvProtocolClass;
   }

   protected void loadExtras(CompoundTag data) {
      CompoundTag itemCustomModelDatas = data.getCompoundTag("custom_model_data");
      if (itemCustomModelDatas != null) {
         Preconditions.checkNotNull(this.itemMappings);
         this.backwardsItemMappings = new Int2ObjectOpenHashMap(itemCustomModelDatas.size());
         this.loadBackwardsItemMappings(itemCustomModelDatas);
      }

      this.enchantmentNames = this.loadNameByIdMappings(data, "enchantmentnames");
   }

   protected void loadBackwardsItemMappings(CompoundTag itemCustomModelDatas) {
      for (Entry<String, Tag> entry : itemCustomModelDatas.entrySet()) {
         int id = Integer.parseInt(entry.getKey());
         int customModelData = ((NumberTag)entry.getValue()).asInt();
         String identifier = this.getFullItemMappings().identifier(id);
         String fallbackName = this.unmappedVersion + " " + nameFromIdentifier(identifier);
         this.backwardsItemMappings.put(id, new MappedItem(this.getNewItemId(id), identifier, fallbackName, customModelData));
      }
   }

   @Nullable
   private Int2ObjectMap<String> loadNameByIdMappings(CompoundTag data, String key) {
      CompoundTag nameMappings = data.getCompoundTag(key);
      if (nameMappings == null) {
         return null;
      } else {
         Int2ObjectMap<String> map = new Int2ObjectArrayMap(nameMappings.size());

         for (Entry<String, Tag> entry : nameMappings.entrySet()) {
            StringTag mappedTag = (StringTag)entry.getValue();
            map.put(Integer.parseInt(entry.getKey()), mappedTag.getValue());
         }

         return map;
      }
   }

   @Nullable
   protected BiMappings loadBiMappings(CompoundTag data, String key) {
      if (key.equals("items") && this.vvProtocolClass != null) {
         Mappings mappings = super.loadMappings(data, key);
         MappingData mappingData = Via.getManager().getProtocolManager().getProtocol(this.vvProtocolClass).getMappingData();
         if (mappingData != null && mappingData.getItemMappings() != null) {
            BiMappings vvItemMappings = mappingData.getItemMappings();
            if (mappings == null) {
               mappings = new IdentityMappings(vvItemMappings.mappedSize(), vvItemMappings.size());
            }

            return ItemMappings.of(mappings, vvItemMappings);
         }
      }

      return super.loadBiMappings(data, key);
   }

   public int getNewItemId(int id) {
      return this.itemMappings.getNewId(id);
   }

   public int getNewBlockId(int id) {
      return this.blockMappings.getNewId(id);
   }

   public int getOldItemId(int id) {
      return this.checkValidity(id, this.itemMappings.inverse().getNewId(id), "item");
   }

   public int getNewAttributeId(int id) {
      return this.attributeMappings.getNewId(id);
   }

   @Nullable
   public MappedItem getMappedItem(int id) {
      return this.backwardsItemMappings != null ? (MappedItem)this.backwardsItemMappings.get(id) : null;
   }

   @Nullable
   public String getMappedNamedSound(String id) {
      return this.getFullSoundMappings().mappedIdentifier(id);
   }

   @Nullable
   public String mappedEnchantmentName(int enchantmentId) {
      if (this.enchantmentNames == null) {
         ViaBackwards.getPlatform()
            .getLogger()
            .log(Level.SEVERE, "No enchantment name mappings found when requesting " + enchantmentId, (Throwable)(new RuntimeException()));
         return null;
      } else {
         return (String)this.enchantmentNames.get(enchantmentId);
      }
   }

   @Nullable
   public Int2ObjectMap<MappedItem> getBackwardsItemMappings() {
      return this.backwardsItemMappings;
   }

   @Nullable
   public Class<? extends Protocol<?, ?, ?, ?>> getViaVersionProtocolClass() {
      return this.vvProtocolClass;
   }

   protected Logger getLogger() {
      return ViaBackwards.getPlatform().getLogger();
   }

   @Nullable
   protected CompoundTag readMappingsFile(String name) {
      return BackwardsMappingDataLoader.INSTANCE.loadNBTFromDir(name);
   }

   public static String nameFromIdentifier(String identifier) {
      StringBuilder result = new StringBuilder();

      for (String split : Key.stripMinecraftNamespace(identifier).split("_")) {
         result.append(' ').append(Character.toUpperCase(split.charAt(0))).append(split.substring(1));
      }

      return result.substring(1);
   }
}
