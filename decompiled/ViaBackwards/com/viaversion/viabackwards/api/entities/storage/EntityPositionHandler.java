package com.viaversion.viabackwards.api.entities.storage;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.EntityRewriterBase;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.TrackedEntity;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import java.util.function.Supplier;

public class EntityPositionHandler {
   public static final double RELATIVE_MOVE_FACTOR = 4096.0;
   private final EntityRewriterBase<?, ?> entityRewriter;
   private final Class<? extends EntityPositionStorage> storageClass;
   private final Supplier<? extends EntityPositionStorage> storageSupplier;

   public EntityPositionHandler(
      EntityRewriterBase<?, ?> entityRewriter, Class<? extends EntityPositionStorage> storageClass, Supplier<? extends EntityPositionStorage> storageSupplier
   ) {
      this.entityRewriter = entityRewriter;
      this.storageClass = storageClass;
      this.storageSupplier = storageSupplier;
   }

   public void cacheEntityPosition(PacketWrapper wrapper, boolean create, boolean relative) {
      this.cacheEntityPosition(
         wrapper, (Double)wrapper.get(Types.DOUBLE, 0), (Double)wrapper.get(Types.DOUBLE, 1), (Double)wrapper.get(Types.DOUBLE, 2), create, relative
      );
   }

   public void cacheEntityPosition(PacketWrapper wrapper, double x, double y, double z, boolean create, boolean relative) {
      this.cacheEntityPosition(wrapper, (Integer)wrapper.get(Types.VAR_INT, 0), x, y, z, create, relative);
   }

   public void cacheEntityPosition(PacketWrapper wrapper, int entityId, double x, double y, double z, boolean create, boolean relative) {
      TrackedEntity entity = this.entityRewriter.tracker(wrapper.user()).entity(entityId);
      if (entity != null) {
         EntityPositionStorage positionStorage;
         if (create) {
            positionStorage = this.storageSupplier.get();
            entity.put(positionStorage);
         } else {
            positionStorage = (EntityPositionStorage)entity.get(this.storageClass);
            if (positionStorage == null) {
               ((BackwardsProtocol)this.entityRewriter.protocol())
                  .getLogger()
                  .warning("Stored entity with id " + entityId + " missing " + this.storageClass.getSimpleName());
               return;
            }
         }

         if (relative) {
            positionStorage.addRelativePosition(x, y, z);
         } else {
            positionStorage.setPosition(x, y, z);
         }
      }
   }

   public EntityPositionStorage getStorage(UserConnection user, int entityId) {
      TrackedEntity entity = this.entityRewriter.tracker(user).entity(entityId);
      EntityPositionStorage entityStorage;
      if (entity != null && (entityStorage = (EntityPositionStorage)entity.get(EntityPositionStorage.class)) != null) {
         return entityStorage;
      } else {
         ((BackwardsProtocol)this.entityRewriter.protocol())
            .getLogger()
            .warning("Untracked entity with id " + entityId + " in " + this.storageClass.getSimpleName());
         return null;
      }
   }

   public static void writeFacingAngles(PacketWrapper wrapper, double x, double y, double z, double targetX, double targetY, double targetZ) {
      double dX = targetX - x;
      double dY = targetY - y;
      double dZ = targetZ - z;
      double r = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
      double yaw = -Math.atan2(dX, dZ) / Math.PI * 180.0;
      if (yaw < 0.0) {
         yaw += 360.0;
      }

      double pitch = -Math.asin(dY / r) / Math.PI * 180.0;
      wrapper.write(Types.BYTE, (byte)(yaw * 256.0 / 360.0));
      wrapper.write(Types.BYTE, (byte)(pitch * 256.0 / 360.0));
   }

   public static void writeFacingDegrees(PacketWrapper wrapper, double x, double y, double z, double targetX, double targetY, double targetZ) {
      double dX = targetX - x;
      double dY = targetY - y;
      double dZ = targetZ - z;
      double r = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
      double yaw = -Math.atan2(dX, dZ) / Math.PI * 180.0;
      if (yaw < 0.0) {
         yaw += 360.0;
      }

      double pitch = -Math.asin(dY / r) / Math.PI * 180.0;
      wrapper.write(Types.FLOAT, (float)yaw);
      wrapper.write(Types.FLOAT, (float)pitch);
   }
}
