package com.sk89q.worldedit.world.entity;

import com.fastasyncworldedit.core.registry.RegistryItem;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;

public class EntityType implements RegistryItem, Keyed {
   public static final NamespacedRegistry<EntityType> REGISTRY = new NamespacedRegistry<>("entity type", "entity_type", "minecraft", true);
   private final String id;
   private int internalId;

   public EntityType(String id) {
      if (!id.contains(":")) {
         id = "minecraft:" + id;
      }

      this.id = id;
   }

   @Override
   public String id() {
      return this.id;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.0"
   )
   @Override
   public String getId() {
      return this.id;
   }

   @Override
   public void setInternalId(int internalId) {
      this.internalId = internalId;
   }

   @Override
   public int getInternalId() {
      return this.internalId;
   }

   public String getName() {
      return this.id();
   }

   @Override
   public String toString() {
      return this.id();
   }

   @Override
   public int hashCode() {
      return this.id.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof EntityType && this.id.equals(((EntityType)obj).id);
   }
}
