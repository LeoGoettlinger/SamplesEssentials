package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.util.Key;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TagRewriter<C extends ClientboundPacketType> implements com.viaversion.viaversion.api.rewriter.TagRewriter {
   private static final int[] EMPTY_ARRAY = new int[0];
   private final Map<RegistryType, List<TagData>> toAdd = new EnumMap<>(RegistryType.class);
   private final Map<RegistryType, Map<String, String>> toRename = new EnumMap<>(RegistryType.class);
   private final Map<RegistryType, Set<String>> toRemove = new EnumMap<>(RegistryType.class);
   private final Set<String> toRemoveRegistries = new HashSet<>();
   private final Protocol<C, ?, ?, ?> protocol;

   public TagRewriter(Protocol<C, ?, ?, ?> protocol) {
      this.protocol = protocol;
   }

   @Override
   public void onMappingDataLoaded() {
      if (this.protocol.getMappingData() != null) {
         for (RegistryType type : RegistryType.getValues()) {
            List<TagData> tags = this.protocol.getMappingData().getTags(type);
            if (tags != null) {
               this.getOrComputeNewTags(type).addAll(tags);
            }
         }
      }
   }

   @Override
   public void removeTags(String registryKey) {
      this.toRemoveRegistries.add(Key.stripMinecraftNamespace(registryKey));
   }

   @Override
   public void removeTag(RegistryType type, String tagId) {
      this.toRemove.computeIfAbsent(type, t -> new HashSet<>()).add(Key.stripMinecraftNamespace(tagId));
   }

   @Override
   public void renameTag(RegistryType type, String tagId, String renameTo) {
      this.toRename.computeIfAbsent(type, t -> new HashMap<>()).put(Key.stripMinecraftNamespace(tagId), renameTo);
   }

   @Override
   public void addEmptyTag(RegistryType tagType, String tagId) {
      this.getOrComputeNewTags(tagType).add(new TagData(tagId, EMPTY_ARRAY));
   }

   @Override
   public void addEmptyTags(RegistryType tagType, String... tagIds) {
      List<TagData> tagList = this.getOrComputeNewTags(tagType);

      for (String id : tagIds) {
         tagList.add(new TagData(id, EMPTY_ARRAY));
      }
   }

   @Override
   public void addEntityTag(String tagId, EntityType... entities) {
      int[] ids = new int[entities.length];

      for (int i = 0; i < entities.length; i++) {
         ids[i] = entities[i].getId();
      }

      this.addTagRaw(RegistryType.ENTITY, tagId, ids);
   }

   @Override
   public void addTag(RegistryType tagType, String tagId, int... unmappedIds) {
      List<TagData> newTags = this.getOrComputeNewTags(tagType);
      IdRewriteFunction rewriteFunction = this.getRewriter(tagType);
      if (rewriteFunction != null) {
         for (int i = 0; i < unmappedIds.length; i++) {
            int unmappedId = unmappedIds[i];
            unmappedIds[i] = rewriteFunction.rewrite(unmappedId);
         }
      }

      newTags.add(new TagData(tagId, unmappedIds));
   }

   @Override
   public void addTagRaw(RegistryType tagType, String tagId, int... ids) {
      this.getOrComputeNewTags(tagType).add(new TagData(tagId, ids));
   }

   public void register(C packetType, @Nullable RegistryType readUntilType) {
      this.protocol.registerClientbound(packetType, this.getHandler(readUntilType));
   }

   public void registerGeneric(C packetType) {
      this.protocol.registerClientbound(packetType, this::handleGeneric);
   }

   public PacketHandler getHandler(@Nullable RegistryType readUntilType) {
      return wrapper -> {
         for (RegistryType type : RegistryType.getValues()) {
            this.handle(wrapper, type);
            if (type == readUntilType) {
               break;
            }
         }
      };
   }

   public void handleGeneric(PacketWrapper wrapper) {
      int length = wrapper.passthrough(Types.VAR_INT);
      int finalLength = length;
      Set<RegistryType> readTypes = EnumSet.noneOf(RegistryType.class);

      for (int i = 0; i < length; i++) {
         String registryKey = wrapper.read(Types.STRING);
         String strippedKey = Key.stripMinecraftNamespace(registryKey);
         if (this.shouldRemoveRegistry(strippedKey)) {
            finalLength--;
            int tagsSize = wrapper.read(Types.VAR_INT);

            for (int j = 0; j < tagsSize; j++) {
               wrapper.read(Types.STRING);
               wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
            }
         } else {
            wrapper.write(Types.STRING, registryKey);
            RegistryType type = RegistryType.getByKey(strippedKey);
            if (type != null) {
               this.handle(wrapper, type);
               readTypes.add(type);
            } else {
               this.handle(wrapper, null, null, null, null);
            }
         }
      }

      for (Entry<RegistryType, List<TagData>> entry : this.toAdd.entrySet()) {
         if (!readTypes.contains(entry.getKey())) {
            wrapper.write(Types.STRING, entry.getKey().identifier());
            this.appendNewTags(wrapper, entry.getKey());
            finalLength++;
         }
      }

      if (length != finalLength) {
         wrapper.set(Types.VAR_INT, 0, finalLength);
      }
   }

   public void handle(PacketWrapper wrapper, String registryKey) {
      RegistryType type = RegistryType.getByKey(registryKey);
      if (type != null) {
         this.handle(wrapper, type);
      } else {
         this.handle(wrapper, null, null, null, null);
      }
   }

   public void handle(PacketWrapper wrapper, RegistryType registryType) {
      this.handle(wrapper, this.getRewriter(registryType), this.getNewTags(registryType), this.toRename.get(registryType), this.toRemove.get(registryType));
   }

   protected void handle(
      PacketWrapper wrapper,
      @Nullable IdRewriteFunction rewriteFunction,
      @Nullable List<TagData> newTags,
      @Nullable Map<String, String> tagsToRename,
      @Nullable Set<String> tagsToRemove
   ) {
      int tagsSize = wrapper.read(Types.VAR_INT);
      List<TagData> tags = new ArrayList<>(newTags != null ? tagsSize + newTags.size() : tagsSize);
      Set<String> currentTags = new HashSet<>(tagsSize);

      for (int i = 0; i < tagsSize; i++) {
         String key = wrapper.read(Types.STRING);
         if (tagsToRename != null) {
            String renamedKey = tagsToRename.get(Key.stripMinecraftNamespace(key));
            if (renamedKey != null) {
               key = renamedKey;
            }
         }

         int[] ids = wrapper.read(Types.VAR_INT_ARRAY_PRIMITIVE);
         if (rewriteFunction != null) {
            IntList idList = new IntArrayList(ids.length);

            for (int id : ids) {
               int mappedId = rewriteFunction.rewrite(id);
               if (mappedId != -1) {
                  idList.add(mappedId);
               }
            }

            ids = idList.toArray(EMPTY_ARRAY);
         }

         tags.add(new TagData(key, ids));
         currentTags.add(Key.stripMinecraftNamespace(key));
      }

      if (tagsToRemove != null) {
         tags.removeIf(tagx -> tagsToRemove.contains(Key.stripMinecraftNamespace(tagx.identifier())));
      }

      if (newTags != null) {
         for (TagData tag : newTags) {
            if (!currentTags.contains(Key.stripMinecraftNamespace(tag.identifier()))) {
               tags.add(tag);
            }
         }
      }

      wrapper.write(Types.VAR_INT, tags.size());

      for (TagData tagx : tags) {
         wrapper.write(Types.STRING, tagx.identifier());
         wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, tagx.entries());
      }
   }

   public void appendNewTags(PacketWrapper wrapper, RegistryType type) {
      List<TagData> newTags = this.getNewTags(type);
      if (newTags != null) {
         wrapper.write(Types.VAR_INT, newTags.size());

         for (TagData tag : newTags) {
            wrapper.write(Types.STRING, tag.identifier());
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, (int[])tag.entries().clone());
         }
      } else {
         wrapper.write(Types.VAR_INT, 0);
      }
   }

   @Nullable
   @Override
   public List<TagData> getNewTags(RegistryType tagType) {
      return this.toAdd.get(tagType);
   }

   @Override
   public List<TagData> getOrComputeNewTags(RegistryType tagType) {
      return this.toAdd.computeIfAbsent(tagType, type -> new ArrayList<>());
   }

   public boolean shouldRemoveRegistry(String registryKey) {
      return this.protocol.getRegistryDataRewriter() != null && this.protocol.getRegistryDataRewriter().shouldRemoveRegistry(registryKey)
         ? true
         : this.toRemoveRegistries.contains(registryKey);
   }

   @Nullable
   public IdRewriteFunction getRewriter(RegistryType tagType) {
      MappingData mappingData = this.protocol.getMappingData();

      return switch (tagType) {
         case BLOCK -> mappingData != null && !Mappings.isIntIdIdentity(mappingData.getBlockMappings()) ? mappingData::getNewBlockId : null;
         case ITEM -> mappingData != null && !Mappings.isIntIdIdentity(mappingData.getItemMappings()) ? mappingData::getNewItemId : null;
         case ENCHANTMENT -> mappingData != null && !Mappings.isIntIdIdentity(mappingData.getEnchantmentMappings())
            ? mappingData.getEnchantmentMappings()::getNewId
            : null;
         case ENTITY -> this.protocol.getEntityRewriter() != null ? this.protocol.getEntityRewriter()::newEntityId : null;
         case FLUID, GAME_EVENT, DAMAGE_TYPE, BANNER_PATTERN -> null;
      };
   }
}
