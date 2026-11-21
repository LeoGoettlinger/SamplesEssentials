package com.viaversion.viaversion.api.data;

import com.viaversion.viaversion.api.minecraft.RegistryKey;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import com.viaversion.viaversion.util.Key;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface MappingData {
   void load();

   int getNewBlockStateId(int var1);

   int getNewBlockId(int var1);

   int getOldBlockId(int var1);

   int getNewItemId(int var1);

   int getOldItemId(int var1);

   int getNewParticleId(int var1);

   int getNewAttributeId(int var1);

   int getNewSoundId(int var1);

   int getOldSoundId(int var1);

   @Nullable
   List<TagData> getTags(RegistryType var1);

   @Nullable
   BiMappings getItemMappings();

   @Nullable
   FullMappings getFullItemMappings();

   @Nullable
   ParticleMappings getParticleMappings();

   @Nullable
   Mappings getBlockMappings();

   @Nullable
   FullMappings getFullBlockMappings();

   @Nullable
   Mappings getBlockStateMappings();

   @Nullable
   Mappings getSoundMappings();

   @Nullable
   FullMappings getFullSoundMappings();

   @Nullable
   Mappings getStatisticsMappings();

   @Nullable
   Mappings getMenuMappings();

   @Nullable
   Mappings getEnchantmentMappings();

   @Nullable
   Mappings getPaintingMappings();

   @Nullable
   FullMappings getBlockEntityMappings();

   @Nullable
   FullMappings getAttributeMappings();

   @Nullable
   FullMappings getEntityMappings();

   @Nullable
   FullMappings getArgumentTypeMappings();

   @Nullable
   FullMappings getRecipeSerializerMappings();

   @Nullable
   FullMappings getSlotDisplayMappings();

   @Nullable
   FullMappings getDataComponentSerializerMappings();

   @Nullable
   default FullMappings getFullMappings(MappingData.MappingType mappingType) {
      return switch (mappingType) {
         case ITEM -> this.getFullItemMappings();
         case BLOCK -> this.getFullBlockMappings();
         case SOUND -> this.getFullSoundMappings();
         case ENTITY_TYPE -> this.getEntityMappings();
      };
   }

   @Nullable
   IntSet changedBlocks();

   default boolean hasBlockChanged(int blockId) {
      return this.changedBlocks() != null && this.changedBlocks().contains(blockId);
   }

   @Nullable
   Set<String> changedEnvironmentAttributes();

   public static enum MappingType implements RegistryKey {
      ITEM,
      BLOCK,
      SOUND,
      ENTITY_TYPE;

      private final Key key = Key.of(this.name().toLowerCase(Locale.ROOT));

      @Override
      public Key registry() {
         return this.key;
      }
   }
}
