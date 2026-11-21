package com.sk89q.worldedit.extent.clipboard.io.sponge;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTagType;

class WriterUtil {
   static LinListTag<LinCompoundTag> encodeEntities(Clipboard clipboard, boolean positionIsRelative) {
      LinListTag.Builder<LinCompoundTag> entities = LinListTag.builder(LinTagType.compoundTag());

      for (Entity entity : clipboard.getEntities()) {
         LinCompoundTag encoded = encodeEntity(clipboard, positionIsRelative, entity);
         if (encoded != null) {
            entities.add(encoded);
         }
      }

      LinListTag<LinCompoundTag> result = entities.build();
      return result.value().isEmpty() ? null : result;
   }

   private static LinCompoundTag encodeEntity(Clipboard clipboard, boolean positionIsRelative, Entity e) {
      BaseEntity state = e.getState();
      if (state == null) {
         return null;
      } else {
         LinCompoundTag.Builder fullTagBuilder = LinCompoundTag.builder();
         LinCompoundTag.Builder dataTagBuilder = LinCompoundTag.builder();
         LinCompoundTag rawData = state.getNbt();
         if (rawData != null) {
            dataTagBuilder.putAll(rawData.value());
            dataTagBuilder.remove("id");
         }

         Location location = e.getLocation();
         Vector3 pos = location.toVector();
         dataTagBuilder.put("Rotation", encodeRotation(location));
         if (positionIsRelative) {
            pos = pos.subtract(clipboard.getMinimumPoint().toVector3());
            fullTagBuilder.put("Data", dataTagBuilder.build());
         } else {
            fullTagBuilder.putAll(dataTagBuilder.build().value());
         }

         fullTagBuilder.putString("Id", state.getType().id());
         fullTagBuilder.put("Pos", encodeVector(pos));
         return fullTagBuilder.build();
      }
   }

   static LinListTag<LinDoubleTag> encodeVector(Vector3 vector) {
      return LinListTag.builder(LinTagType.doubleTag())
         .add(LinDoubleTag.of(vector.x()))
         .add(LinDoubleTag.of(vector.y()))
         .add(LinDoubleTag.of(vector.z()))
         .build();
   }

   static LinListTag<LinFloatTag> encodeRotation(Location location) {
      return LinListTag.builder(LinTagType.floatTag()).add(LinFloatTag.of(location.getYaw())).add(LinFloatTag.of(location.getPitch())).build();
   }
}
