package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class FollowingText implements TrackedPacketEntity, AutoCloseable {
   private final UUID id = UUID.randomUUID();
   private final FakeText text;
   private final Entity anchor;
   private final Supplier<Location> position;
   private final Predicate<Player> viewerFilter;
   private final Set<UUID> viewers = new HashSet<>();
   private Location location;
   private boolean closed;

   public FollowingText(FakeText text, Entity anchor, Supplier<Location> position, Predicate<Player> viewerFilter) {
      this.text = text;
      this.anchor = anchor;
      this.position = position;
      this.viewerFilter = viewerFilter;
      this.location = position.get().clone();
      text.teleport(this.location);
      PacketEntityTracker.getInstance().register(this);
   }

   @Override
   public void updateTracking() {
      if (!this.isValid()) {
         this.close();
      } else {
         Location next = this.position.get();
         if (!next.getWorld().equals(this.location.getWorld())) {
            for (UUID viewer : Set.copyOf(this.viewers)) {
               this.text.hideFrom(viewer);
            }

            this.viewers.clear();
         }

         if (!next.equals(this.location)) {
            this.location = next.clone();
            this.text.teleport(this.location);
         }
      }
   }

   @Override
   public Location getTrackingLocation() {
      return this.location.clone();
   }

   @Override
   public World getWorld() {
      return this.location.getWorld();
   }

   @Override
   public void showToPlayer(Player player) {
      this.text.displayTo(player);
      this.viewers.add(player.getUniqueId());
   }

   @Override
   public void hideFromPlayer(Player player) {
      this.text.hideFrom(player);
      this.viewers.remove(player.getUniqueId());
   }

   @Override
   public boolean isVisibleTo(Player player) {
      return this.viewers.contains(player.getUniqueId());
   }

   @Override
   public boolean canBeSeenBy(Player player) {
      return this.viewerFilter.test(player);
   }

   @Override
   public Set<UUID> getCurrentViewers() {
      return Set.copyOf(this.viewers);
   }

   @Override
   public boolean isValid() {
      return !this.closed && this.anchor.isValid();
   }

   @Override
   public UUID getUniqueId() {
      return this.id;
   }

   @Override
   public Entity getVehicle() {
      return null;
   }

   @Override
   public void remount() {
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         PacketEntityTracker.getInstance().unregister(this);
         this.text.remove();
         this.viewers.clear();
      }
   }
}
