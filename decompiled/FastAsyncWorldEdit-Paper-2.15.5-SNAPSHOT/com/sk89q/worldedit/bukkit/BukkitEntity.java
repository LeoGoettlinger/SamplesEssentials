package com.sk89q.worldedit.bukkit;

import com.fastasyncworldedit.core.util.TaskManager;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;
import java.lang.ref.WeakReference;
import javax.annotation.Nullable;
import org.bukkit.entity.EntityType;

public class BukkitEntity implements Entity {
   private final WeakReference<org.bukkit.entity.Entity> entityRef;
   private final EntityType type;

   public BukkitEntity(org.bukkit.entity.Entity entity) {
      Preconditions.checkNotNull(entity);
      this.type = entity.getType();
      this.entityRef = new WeakReference<>(entity);
   }

   @Override
   public Extent getExtent() {
      org.bukkit.entity.Entity entity = this.entityRef.get();
      return (Extent)(entity != null ? BukkitAdapter.adapt(entity.getWorld()) : NullWorld.getInstance());
   }

   @Override
   public Location getLocation() {
      org.bukkit.entity.Entity entity = this.entityRef.get();
      return entity != null ? BukkitAdapter.adapt(entity.getLocation()) : new Location(NullWorld.getInstance());
   }

   @Override
   public boolean setLocation(Location location) {
      org.bukkit.entity.Entity entity = this.entityRef.get();
      return entity != null ? entity.teleport(BukkitAdapter.adapt(location)) : false;
   }

   @Override
   public BaseEntity getState() {
      org.bukkit.entity.Entity entity = this.entityRef.get();
      if (entity != null) {
         if (entity instanceof Player) {
            return null;
         } else {
            BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
            return adapter != null ? adapter.getEntity(entity) : null;
         }
      } else {
         return null;
      }
   }

   @Override
   public boolean remove() {
      return TaskManager.taskManager().sync(() -> {
         org.bukkit.entity.Entity entity = this.entityRef.get();
         if (entity != null) {
            try {
               entity.remove();
            } catch (UnsupportedOperationException var3) {
               return false;
            }

            return entity.isDead();
         } else {
            return true;
         }
      });
   }

   @Nullable
   @Override
   public <T> T getFacet(Class<? extends T> cls) {
      org.bukkit.entity.Entity entity = this.entityRef.get();
      return (T)(entity != null && EntityProperties.class.isAssignableFrom(cls) ? new BukkitEntityProperties(entity) : null);
   }
}
