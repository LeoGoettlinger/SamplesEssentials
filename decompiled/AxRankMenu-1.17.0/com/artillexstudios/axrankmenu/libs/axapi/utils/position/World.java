package com.artillexstudios.axrankmenu.libs.axapi.utils.position;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;

public final class World {
   private final String name;

   public static World create(org.bukkit.World world) {
      return create(world.getName());
   }

   public static World create(String name) {
      return new World(name);
   }

   public World(String name) {
      this.name = (String)Preconditions.checkNotNull(name, "Can't create World with null name!");
   }

   public World(org.bukkit.World world) {
      Preconditions.checkNotNull(world, "Can't create World with null name!");
      this.name = world.getName();
   }

   public String getName() {
      return this.name;
   }

   public org.bukkit.World toBukkit() {
      return Bukkit.getWorld(this.name);
   }

   @Override
   public String toString() {
      return "World{name='" + this.name + "'}";
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof World world ? this.name.equals(world.name) : false;
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }
}
