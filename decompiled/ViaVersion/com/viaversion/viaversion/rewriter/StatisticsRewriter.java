package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Types;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StatisticsRewriter<C extends ClientboundPacketType> {
   private static final int CUSTOM_STATS_CATEGORY = 8;
   private final Protocol<C, ?, ?, ?> protocol;

   public StatisticsRewriter(Protocol<C, ?, ?, ?> protocol) {
      this.protocol = protocol;
   }

   public void register(C packetType) {
      MappingData mappingData = this.protocol.getMappingData();
      if (mappingData != null
         && (
            !Mappings.isIntIdIdentity(mappingData.getBlockMappings())
               || !Mappings.isIntIdIdentity(mappingData.getItemMappings())
               || !Mappings.isIntIdIdentity(mappingData.getEntityMappings())
               || !Mappings.isIntIdIdentity(mappingData.getStatisticsMappings())
         )) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            int size = wrapper.passthrough(Types.VAR_INT);
            int newSize = size;

            for (int i = 0; i < size; i++) {
               int categoryId = wrapper.read(Types.VAR_INT);
               int statisticId = wrapper.read(Types.VAR_INT);
               int value = wrapper.read(Types.VAR_INT);
               if (categoryId == 8 && mappingData.getStatisticsMappings() != null) {
                  statisticId = mappingData.getStatisticsMappings().getNewId(statisticId);
                  if (statisticId == -1) {
                     newSize--;
                     continue;
                  }
               } else {
                  RegistryType type = this.getRegistryTypeForStatistic(categoryId);
                  IdRewriteFunction statisticsRewriter;
                  if (type != null && (statisticsRewriter = this.getRewriter(type)) != null) {
                     statisticId = statisticsRewriter.rewrite(statisticId);
                  }
               }

               wrapper.write(Types.VAR_INT, categoryId);
               wrapper.write(Types.VAR_INT, statisticId);
               wrapper.write(Types.VAR_INT, value);
            }

            if (newSize != size) {
               wrapper.set(Types.VAR_INT, 0, newSize);
            }
         });
      }
   }

   @Nullable
   protected IdRewriteFunction getRewriter(RegistryType type) {
      return switch (type) {
         case BLOCK -> this.protocol.getMappingData().getBlockMappings() != null ? id -> this.protocol.getMappingData().getNewBlockId(id) : null;
         case ITEM -> this.protocol.getMappingData().getItemMappings() != null ? id -> this.protocol.getMappingData().getNewItemId(id) : null;
         case ENTITY -> this.protocol.getEntityRewriter() != null ? id -> this.protocol.getEntityRewriter().newEntityId(id) : null;
         default -> throw new IllegalArgumentException("Unknown registry type in statistics packet: " + type);
      };
   }

   @Nullable
   public RegistryType getRegistryTypeForStatistic(int statisticsId) {
      return switch (statisticsId) {
         case 0 -> RegistryType.BLOCK;
         case 1, 2, 3, 4, 5 -> RegistryType.ITEM;
         case 6, 7 -> RegistryType.ENTITY;
         default -> null;
      };
   }
}
