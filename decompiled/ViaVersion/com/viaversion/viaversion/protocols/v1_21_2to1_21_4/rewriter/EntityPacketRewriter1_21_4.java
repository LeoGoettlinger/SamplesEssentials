package com.viaversion.viaversion.protocols.v1_21_2to1_21_4.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_4;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.packet.ServerboundPackets1_21_4;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public final class EntityPacketRewriter1_21_4 extends EntityRewriter<ClientboundPacket1_21_2, Protocol1_21_2To1_21_4> {
   public EntityPacketRewriter1_21_4(Protocol1_21_2To1_21_4 protocol) {
      super(protocol);
   }

   @Override
   public void registerPackets() {
      this.protocol.getRegistryDataRewriter().addHandler("worldgen/biome", (key, biome) -> {
         CompoundTag effectsTag = biome.getCompoundTag("effects");
         CompoundTag musicTag = effectsTag.getCompoundTag("music");
         if (musicTag != null) {
            ListTag<CompoundTag> weightedMusicTags = new ListTag<>(CompoundTag.class);
            CompoundTag weightedMusicTag = new CompoundTag();
            weightedMusicTag.put("data", musicTag);
            weightedMusicTag.putInt("weight", 1);
            weightedMusicTags.add(weightedMusicTag);
            effectsTag.put("music", weightedMusicTags);
         }
      });
      this.protocol.registerServerbound(ServerboundPackets1_21_4.MOVE_VEHICLE, wrapper -> {
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.DOUBLE);
         wrapper.passthrough(Types.FLOAT);
         wrapper.passthrough(Types.FLOAT);
         wrapper.read(Types.BOOLEAN);
      });
      this.protocol.cancelServerbound(ServerboundPackets1_21_4.PLAYER_LOADED);
   }

   @Override
   protected void registerRewrites() {
      this.dataTypeMapper().register();
      this.registerEntityDataTypeHandler(
         VersionedTypes.V1_21_4.entityDataTypes.itemType,
         VersionedTypes.V1_21_4.entityDataTypes.blockStateType,
         VersionedTypes.V1_21_4.entityDataTypes.optionalBlockStateType,
         VersionedTypes.V1_21_4.entityDataTypes.particleType,
         VersionedTypes.V1_21_4.entityDataTypes.particlesType,
         VersionedTypes.V1_21_4.entityDataTypes.componentType,
         VersionedTypes.V1_21_4.entityDataTypes.optionalComponentType
      );
      this.registerBlockStateHandler(EntityTypes1_21_4.ABSTRACT_MINECART, 11);
      this.filter().type(EntityTypes1_21_4.CREAKING).addIndex(18);
      this.filter().type(EntityTypes1_21_4.SALMON).index(17).handler((event, data) -> {
         String type = data.value();

         int typeId = switch (type) {
            case "small" -> 0;
            case "large" -> 2;
            default -> 1;
         };
         data.setTypeAndValue(VersionedTypes.V1_21_4.entityDataTypes.varIntType, typeId);
      });
   }

   @Override
   public EntityType typeFromId(int type) {
      return EntityTypes1_21_4.getTypeFromId(type);
   }
}
