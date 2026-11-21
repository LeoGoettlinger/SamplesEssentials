package dev.cwhead.GravesX.api.provider;

import com.ranull.graves.data.EntityData;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;

public interface GraveProvider {
   String id();

   default int order() {
      return 0;
   }

   void place(Location var1, Grave var2) throws Exception;

   void remove(Grave var1) throws Exception;

   boolean isPlaced(Grave var1);

   default boolean shouldUseGraveHead() {
      return false;
   }

   default boolean shouldBlockBeReplaceable() {
      return true;
   }

   boolean supports(EntityData var1);

   boolean removeEntityData(EntityData var1) throws Exception;
}
