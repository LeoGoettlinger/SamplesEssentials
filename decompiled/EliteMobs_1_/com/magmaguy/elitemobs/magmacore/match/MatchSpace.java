package com.magmaguy.elitemobs.magmacore.match;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

public interface MatchSpace {
   static MatchSpace region(World world, Predicate<Location> contains) {
      return new RegionSpace(world, contains);
   }

   static MatchSpace region(World world, BoundingBox box) {
      BoundingBox copy = box.clone();
      return new RegionSpace(world, location -> copy.contains(location.toVector()));
   }

   static TemporaryWorlds temporaryWorlds(World... worlds) {
      return new TemporaryWorlds(List.of(worlds));
   }

   boolean contains(Location var1);

   Collection<World> worlds();

   boolean guardsEntryDuring(MatchPhase var1);

   void teardown();
}
