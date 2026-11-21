package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R6.packets;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R6.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class PacketGenericEntity extends AbstractPacketEntity<net.minecraft.world.entity.Entity> {
   private final EntityType bukkitType;

   public PacketGenericEntity(EntityType entityType, Location location) {
      super(createGenericEntity(entityType, location));
      this.bukkitType = entityType;
   }

   @Override
   protected net.minecraft.world.entity.Entity createEntity(Location location) {
      return createGenericEntity(this.bukkitType, location);
   }

   private static net.minecraft.world.entity.Entity createGenericEntity(EntityType bukkitType, Location location) {
      if (bukkitType == null) {
         throw new IllegalArgumentException("entityType cannot be null");
      } else {
         CraftWorld craftWorld = (CraftWorld)location.getWorld();
         Class<? extends Entity> entityClass = bukkitType.getEntityClass();
         if (entityClass == null) {
            throw new RuntimeException("No entity class for type: " + bukkitType);
         } else {
            Entity bukkitEntity = craftWorld.createEntity(location, entityClass);
            net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity)bukkitEntity).getHandle();
            nmsEntity.a_(location.getX(), location.getY(), location.getZ());
            if (location.getYaw() != 0.0F) {
               nmsEntity.v(location.getYaw());
            }

            if (location.getPitch() != 0.0F) {
               nmsEntity.w(location.getPitch());
            }

            return nmsEntity;
         }
      }
   }

   public EntityType getEntityType() {
      return this.bukkitType;
   }
}
