package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.entity.EntityType;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinNumberTag;

public class Pre13HangingCompatibilityHandler implements EntityNBTCompatibilityHandler {
   @Override
   public LinCompoundTag updateNbt(EntityType type, LinCompoundTag tag) {
      if (!type.id().startsWith("minecraft:")) {
         return tag;
      } else {
         Direction newDirection;
         if (tag.value().get("Dir") instanceof LinNumberTag<?> legacyDir) {
            newDirection = MCDirections.fromPre13Hanging(MCDirections.fromLegacyHanging(legacyDir.value().byteValue()));
         } else if (tag.value().get("Direction") instanceof LinNumberTag<?> legacyDirection) {
            newDirection = MCDirections.fromPre13Hanging(legacyDirection.value().intValue());
         } else {
            if (!(tag.value().get("Facing") instanceof LinNumberTag<?> legacyFacing)) {
               return tag;
            }

            newDirection = MCDirections.fromPre13Hanging(legacyFacing.value().intValue());
         }

         byte hangingByte = (byte)MCDirections.toHanging(newDirection);
         LinCompoundTag.Builder builder = tag.toBuilder();
         builder.putByte("Facing", hangingByte);
         return builder.build();
      }
   }
}
