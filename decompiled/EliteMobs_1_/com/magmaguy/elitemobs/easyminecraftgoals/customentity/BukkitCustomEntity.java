package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

import java.util.Collection;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public interface BukkitCustomEntity extends CustomEntityHandle {
   Entity entity();

   void prepareSpawnFor(Player var1);

   void prepareSpawnFor(Collection<? extends Player> var1);

   void syncTo(Player var1);

   void syncToViewers();

   void forgetViewer(Player var1);

   void forgetViewer(UUID var1);

   void remove();

   boolean isValid();

   public interface Builder {
      BukkitCustomEntity.Builder identifier(String var1);

      BukkitCustomEntity.Builder carrierEntityType(EntityType var1);

      BukkitCustomEntity.Builder dimensions(float var1, float var2);

      BukkitCustomEntity.Builder scale(float var1);

      BukkitCustomEntity.Builder color(Integer var1);

      BukkitCustomEntity.Builder variant(Integer var1);

      BukkitCustomEntity.Builder tracked(boolean var1);

      BukkitCustomEntity.Builder propertySchema(CustomEntityPropertySchema var1);

      BukkitCustomEntity.Builder onShow(CustomEntityViewerHook var1);

      BukkitCustomEntity.Builder onHide(CustomEntityViewerHook var1);

      BukkitCustomEntity.Builder onRemove(CustomEntityLifecycleHook var1);

      BukkitCustomEntity build(Entity var1);
   }
}
