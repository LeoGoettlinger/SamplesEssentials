package ac.grim.grimac.utils.data.packetentity;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import lombok.Generated;

public abstract class TypedPacketEntity {
   private final EntityType type;
   public final boolean isLivingEntity;
   public final boolean isMinecart;
   public final boolean isHorse;
   public final boolean isAgeable;
   public final boolean isAnimal;
   public final boolean isBoat;
   public final boolean isHappyGhast;

   public TypedPacketEntity(EntityType type) {
      this.type = type;
      this.isLivingEntity = EntityTypes.isTypeInstanceOf(type, EntityTypes.LIVINGENTITY);
      this.isMinecart = EntityTypes.isTypeInstanceOf(type, EntityTypes.MINECART_ABSTRACT);
      this.isHorse = EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_HORSE);
      this.isAgeable = EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_AGEABLE)
            && !EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_PARROT)
            && type != EntityTypes.FROG
         || EntityTypes.isTypeInstanceOf(type, EntityTypes.ZOMBIE)
         || EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_PIGLIN)
         || type == EntityTypes.ZOGLIN
         || type == EntityTypes.SQUID
         || type == EntityTypes.GLOW_SQUID;
      this.isAnimal = EntityTypes.isTypeInstanceOf(type, EntityTypes.ABSTRACT_ANIMAL);
      this.isBoat = EntityTypes.isTypeInstanceOf(type, EntityTypes.BOAT);
      this.isHappyGhast = EntityTypes.HAPPY_GHAST.equals(type);
   }

   public boolean isPushable() {
      return this.getType() != EntityTypes.ARMOR_STAND && this.getType() != EntityTypes.BAT && this.getType() != EntityTypes.PARROT
         ? this.isLivingEntity || this.isBoat || this.isMinecart
         : false;
   }

   @Generated
   public EntityType getType() {
      return this.type;
   }
}
