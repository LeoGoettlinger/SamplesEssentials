package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.entities.storage.EntityObjectData;
import com.viaversion.viabackwards.api.entities.storage.EntityReplacement;
import com.viaversion.viabackwards.api.entities.storage.WrappedEntityData;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.ObjectType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_9;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class LegacyEntityRewriter<C extends ClientboundPacketType, T extends BackwardsProtocol<C, ?, ?, ?>> extends EntityRewriterBase<C, T> {
   private final Map<ObjectType, EntityReplacement> objectTypes = new HashMap<>();

   protected LegacyEntityRewriter(T protocol) {
      this(protocol, EntityDataTypes1_9.STRING, EntityDataTypes1_9.BOOLEAN);
   }

   protected LegacyEntityRewriter(T protocol, EntityDataType displayType, EntityDataType displayVisibilityType) {
      super(protocol, displayType, 2, displayVisibilityType, 3);
   }

   protected EntityObjectData mapObjectType(ObjectType oldObjectType, ObjectType replacement, int data) {
      EntityObjectData entData = new EntityObjectData(
         (BackwardsProtocol<?, ?, ?, ?>)this.protocol,
         oldObjectType.getType().name().toLowerCase(Locale.ROOT),
         oldObjectType.getId(),
         replacement.getId(),
         data
      );
      this.objectTypes.put(oldObjectType, entData);
      return entData;
   }

   @Nullable
   protected EntityReplacement getObjectData(ObjectType type) {
      return this.objectTypes.get(type);
   }

   protected void registerRespawn(C packetType) {
      ((BackwardsProtocol)this.protocol).registerClientbound(packetType, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.handler(wrapper -> {
               ClientWorld clientWorld = wrapper.user().storables(LegacyEntityRewriter.this.protocol).clientWorld();
               if (clientWorld.setEnvironment((Integer)wrapper.get(Types.INT, 0))) {
                  LegacyEntityRewriter.this.tracker(wrapper.user()).clearEntities();
               }
            });
         }
      });
   }

   protected void registerJoinGame(C packetType, final EntityType playerType) {
      ((BackwardsProtocol)this.protocol).registerClientbound(packetType, new PacketHandlers() {
         public void register() {
            this.map(Types.INT);
            this.map(Types.UNSIGNED_BYTE);
            this.map(Types.INT);
            this.handler(wrapper -> {
               ClientWorld clientWorld = wrapper.user().storables(LegacyEntityRewriter.this.protocol).clientWorld();
               clientWorld.setEnvironment((Integer)wrapper.get(Types.INT, 1));
               int entityId = (Integer)wrapper.get(Types.INT, 0);
               EntityTracker tracker = LegacyEntityRewriter.this.tracker(wrapper.user());
               tracker.addEntity(entityId, playerType);
               tracker.setClientEntityId(entityId);
            });
         }
      });
   }

   protected PacketHandler getMobSpawnRewriter(Type<List<EntityData>> dataType, LegacyEntityRewriter.IdSetter idSetter) {
      return wrapper -> {
         int entityId = (Integer)wrapper.get(Types.VAR_INT, 0);
         EntityType type = this.tracker(wrapper.user()).entityType(entityId);
         if (type != null) {
            List<EntityData> entityDataList = (List<EntityData>)wrapper.get(dataType, 0);
            this.handleEntityData(entityId, entityDataList, wrapper.user());
            EntityReplacement entityReplacement = this.entityDataForType(type);
            if (entityReplacement != null) {
               idSetter.setId(wrapper, entityReplacement.replacementId());
               if (entityReplacement.hasBaseData()) {
                  entityReplacement.defaultData().createData(new WrappedEntityData(entityDataList));
               }
            }
         }
      };
   }

   public PacketHandler getMobSpawnRewriter(Type<List<EntityData>> dataType) {
      return this.getMobSpawnRewriter(dataType, (wrapper, id) -> wrapper.set(Types.UNSIGNED_BYTE, 0, (short)id));
   }

   public PacketHandler getMobSpawnRewriter1_11(Type<List<EntityData>> dataType) {
      return this.getMobSpawnRewriter(dataType, (wrapper, id) -> wrapper.set(Types.VAR_INT, 1, id));
   }

   protected PacketHandler getObjectTrackerHandler() {
      return wrapper -> {
         int id = (Byte)wrapper.get(Types.BYTE, 0);
         int data = (Integer)wrapper.get(Types.INT, 0);
         EntityType type = this.objectTypeFromId(id, data);
         if (type != null) {
            this.tracker(wrapper.user()).addEntity((Integer)wrapper.get(Types.VAR_INT, 0), type);
         }
      };
   }

   protected PacketHandler getTrackerAndDataHandler(Type<List<EntityData>> dataType, EntityType entityType) {
      return wrapper -> {
         this.tracker(wrapper.user()).addEntity((Integer)wrapper.get(Types.VAR_INT, 0), entityType);
         List<EntityData> entityDataList = (List<EntityData>)wrapper.get(dataType, 0);
         this.handleEntityData((Integer)wrapper.get(Types.VAR_INT, 0), entityDataList, wrapper.user());
      };
   }

   protected PacketHandler getObjectRewriter(LegacyEntityRewriter.ObjectTypeGetter objectGetter) {
      return wrapper -> {
         int id = (Byte)wrapper.get(Types.BYTE, 0);
         int data = (Integer)wrapper.get(Types.INT, 0);
         ObjectType type = objectGetter.get(id, data);
         if (type != null) {
            EntityReplacement replacement = this.getObjectData(type);
            if (replacement != null) {
               wrapper.set(Types.BYTE, 0, (byte)replacement.replacementId());
               if (replacement.objectData() != -1) {
                  wrapper.set(Types.INT, 0, replacement.objectData());
               }
            }
         }
      };
   }

   @FunctionalInterface
   protected interface IdSetter {
      void setId(PacketWrapper var1, int var2);
   }

   @FunctionalInterface
   protected interface ObjectTypeGetter {
      ObjectType get(int var1, int var2);
   }
}
