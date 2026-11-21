package com.magmaguy.elitemobs.easyminecraftgoals.v26.packets;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.decoration.ArmorStand;

final class PacketEntityTypes {
   static final EntityType<ArmorStand> ARMOR_STAND = get("armor_stand");
   static final EntityType<Interaction> INTERACTION = get("interaction");
   static final EntityType<ItemDisplay> ITEM_DISPLAY = get("item_display");
   static final EntityType<TextDisplay> TEXT_DISPLAY = get("text_display");

   private PacketEntityTypes() {
   }

   private static <T extends Entity> EntityType<T> get(String key) {
      EntityType<?> entityType = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace(key));
      if (entityType == null) {
         throw new IllegalStateException("Missing built-in entity type: minecraft:" + key);
      } else {
         return (EntityType<T>)entityType;
      }
   }
}
