package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.TemporaryWorldManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;

public final class TemporaryWorlds implements MatchSpace {
   private final List<World> worlds = new ArrayList<>();
   private final Consumer<World> deleter;

   TemporaryWorlds(List<World> initial) {
      this(initial, TemporaryWorldManager::permanentlyDeleteWorld);
   }

   TemporaryWorlds(List<World> initial, Consumer<World> deleter) {
      this.deleter = Objects.requireNonNull(deleter, "deleter");
      initial.forEach(this::addWorld);
   }

   public void addWorld(World world) {
      Objects.requireNonNull(world, "world");
      if (!this.worlds.contains(world)) {
         this.worlds.add(world);
         LeftoverWorlds.markLoaded(world);
      }
   }

   @Override
   public boolean contains(Location location) {
      return location != null && location.getWorld() != null && this.worlds.contains(location.getWorld());
   }

   @Override
   public Collection<World> worlds() {
      return List.copyOf(this.worlds);
   }

   @Override
   public boolean guardsEntryDuring(MatchPhase phase) {
      return phase != MatchPhase.DESTROYED;
   }

   @Override
   public void teardown() {
      for (World world : List.copyOf(this.worlds)) {
         this.deleter.accept(world);
      }

      this.worlds.clear();
   }
}
