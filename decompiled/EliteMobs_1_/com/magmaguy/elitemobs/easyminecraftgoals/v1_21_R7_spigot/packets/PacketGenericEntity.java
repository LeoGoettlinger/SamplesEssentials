package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.CraftBukkitBridge;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class PacketGenericEntity extends AbstractPacketEntity<Entity> {
   private final EntityType bukkitType;

   public PacketGenericEntity(EntityType entityType, Location location) {
      super(createGenericEntity(entityType, location));
      this.bukkitType = entityType;
   }

   @Override
   protected Entity createEntity(Location location) {
      return createGenericEntity(this.bukkitType, location);
   }

   private static Entity createGenericEntity(EntityType bukkitType, Location location) {
      if (bukkitType == null) {
         throw new IllegalArgumentException("entityType cannot be null");
      } else {
         return CraftBukkitBridge.createNMSEntity(bukkitType, CraftBukkitBridge.getServerLevel(location), location);
      }
   }

   public EntityType getEntityType() {
      return this.bukkitType;
   }
}
