package ac.grim.grimac.utils.nmsutil;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import lombok.Generated;

public final class EntityTypeTags {
   public static final EntityTypeTags.EntityTag CAN_FLOAT_WHILE_RIDDEN = new EntityTypeTags.EntityTag(
      EntityTypes.HORSE, EntityTypes.ZOMBIE_HORSE, EntityTypes.MULE, EntityTypes.DONKEY, EntityTypes.CAMEL, EntityTypes.CAMEL_HUSK
   );
   public static final EntityTypeTags.EntityTag NOT_AFFECTED_BY_GEYSERS = new EntityTypeTags.EntityTag(EntityTypes.ENDER_DRAGON);

   @Generated
   private EntityTypeTags() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public record EntityTag(EntityType... tags) {
      public boolean anyOf(EntityType tested) {
         for (EntityType type : this.tags) {
            if (tested.isInstanceOf(type)) {
               return true;
            }
         }

         return false;
      }
   }
}
