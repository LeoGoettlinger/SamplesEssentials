package com.viaversion.viabackwards.protocol.v26_1to1_21_11.rewriter;

import com.viaversion.viabackwards.api.rewriters.EntityRewriter;
import com.viaversion.viabackwards.protocol.v26_1to1_21_11.Protocol26_1To1_21_11;
import com.viaversion.viabackwards.protocol.v26_1to1_21_11.storage.ProtocolStorables26_1;
import com.viaversion.viaversion.api.minecraft.GameMode;
import com.viaversion.viaversion.api.minecraft.Vector3d;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_11;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes26_1;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;

public final class EntityPacketRewriter26_1 extends EntityRewriter<ClientboundPacket26_1, Protocol26_1To1_21_11> {
   private static final int INTERACT_ACTION = 0;
   private static final int ATTACK_ACTION = 1;
   private static final int INTERACT_AT_ACTION = 2;

   public EntityPacketRewriter26_1(Protocol26_1To1_21_11 protocol) {
      super(
         protocol,
         ((EntityDataTypes1_21_11)VersionedTypes.V1_21_11.entityDataTypes).optionalComponentType,
         ((EntityDataTypes1_21_11)VersionedTypes.V1_21_11.entityDataTypes).booleanType
      );
   }

   public void registerPackets() {
      ((Protocol26_1To1_21_11)this.protocol).appendClientbound(ClientboundPackets26_1.RESPAWN, wrapper -> {
         byte gamemode = (Byte)wrapper.get(Types.BYTE, 0);
         ProtocolStorables26_1 storables = (ProtocolStorables26_1)wrapper.user().storables(this.protocol);
         storables.setGameMode(gamemode);
      });
      ((Protocol26_1To1_21_11)this.protocol).appendClientbound(ClientboundPackets26_1.LOGIN, wrapper -> {
         byte gamemode = (Byte)wrapper.get(Types.BYTE, 0);
         ProtocolStorables26_1 storables = (ProtocolStorables26_1)wrapper.user().storables(this.protocol);
         storables.setGameMode(gamemode);
      });
      ((Protocol26_1To1_21_11)this.protocol).appendClientbound(ClientboundPackets26_1.GAME_EVENT, wrapper -> {
         wrapper.resetReader();
         short event = (Short)wrapper.passthrough(Types.UNSIGNED_BYTE);
         if (event == 3) {
            int value = (int)Math.floor((Float)wrapper.passthrough(Types.FLOAT) + 0.5F);
            ProtocolStorables26_1 storables = (ProtocolStorables26_1)wrapper.user().storables(this.protocol);
            storables.setGameMode(value);
         }
      });
      ((Protocol26_1To1_21_11)this.protocol).registerServerbound(ServerboundPackets1_21_6.INTERACT, wrapper -> {
         wrapper.passthrough(Types.VAR_INT);
         int action = (Integer)wrapper.read(Types.VAR_INT);
         switch (action) {
            case 0:
               wrapper.cancel();
               break;
            case 1:
               ProtocolStorables26_1 storables = (ProtocolStorables26_1)wrapper.user().storables(this.protocol);
               boolean spectator = storables.gameMode() == GameMode.SPECTATOR.id();
               wrapper.setPacketType(spectator ? ServerboundPackets26_1.SPECTATE_ENTITY : ServerboundPackets26_1.ATTACK);
               wrapper.read(Types.BOOLEAN);
               break;
            case 2:
               float x = (Float)wrapper.read(Types.FLOAT);
               float y = (Float)wrapper.read(Types.FLOAT);
               float z = (Float)wrapper.read(Types.FLOAT);
               wrapper.passthrough(Types.VAR_INT);
               wrapper.write(Types.LOW_PRECISION_VECTOR, new Vector3d(x, y, z));
               break;
            default:
               throw new IllegalArgumentException("Invalid interact action");
         }
      });
   }

   protected void registerRewrites() {
      EntityDataTypes26_1 entityDataTypes = (EntityDataTypes26_1)VersionedTypes.V26_1.entityDataTypes;
      EntityDataTypes1_21_11 mappedEntityDataTypes = (EntityDataTypes1_21_11)VersionedTypes.V1_21_11.entityDataTypes;
      this.dataTypeMapper()
         .removed(entityDataTypes.catSoundVariant)
         .removed(entityDataTypes.cowSoundVariant)
         .removed(entityDataTypes.pigSoundVariant)
         .removed(entityDataTypes.chickenSoundVariant)
         .register();
      this.registerEntityDataTypeHandler1_20_3(
         mappedEntityDataTypes.itemType,
         mappedEntityDataTypes.blockStateType,
         mappedEntityDataTypes.optionalBlockStateType,
         mappedEntityDataTypes.particleType,
         mappedEntityDataTypes.particlesType,
         mappedEntityDataTypes.componentType,
         mappedEntityDataTypes.optionalComponentType
      );
      this.filter().type(EntityTypes1_21_11.ZOMBIE_VILLAGER).removeIndex(21);
      this.filter().type(EntityTypes1_21_11.VILLAGER).removeIndex(20);
      this.filter().type(EntityTypes1_21_11.CAT).removeIndex(24);
      this.filter().type(EntityTypes1_21_11.CHICKEN).removeIndex(19);
      this.filter().type(EntityTypes1_21_11.PIG).removeIndex(20);
      this.filter().type(EntityTypes1_21_11.COW).removeIndex(19);
      this.filter().type(EntityTypes1_21_11.TADPOLE).removeIndex(17);
      this.filter().type(EntityTypes1_21_11.ABSTRACT_AGEABLE).removeIndex(17);
   }

   public EntityType typeFromId(int type) {
      return EntityTypes1_21_11.getTypeFromId(type);
   }
}
