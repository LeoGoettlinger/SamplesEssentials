package com.magmaguy.elitemobs.magmacore.projectiles;

import java.util.function.Consumer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class MagicProjectileMarker {
   private static final String IMPACT_OWNER = "magmacore_magic_impact_owner";

   private MagicProjectileMarker() {
   }

   public static boolean isMarked(Projectile projectile) {
      return projectile != null && projectile.getPersistentDataContainer().has(key(), PersistentDataType.BYTE);
   }

   public static boolean resolve(Projectile projectile, LivingEntity target) {
      for (MetadataValue value : projectile.getMetadata("magmacore_magic_impact_owner")) {
         if (value.getOwningPlugin() != null && value.getOwningPlugin().isEnabled() && value.value() instanceof Consumer<?> handler) {
            ((Consumer<LivingEntity>)handler).accept(target);
            return true;
         }
      }

      return false;
   }

   static void mark(Projectile projectile, Plugin owner, Consumer<LivingEntity> impacts) {
      projectile.getPersistentDataContainer().set(key(), PersistentDataType.BYTE, (byte)1);
      projectile.setMetadata("magmacore_magic_impact_owner", new FixedMetadataValue(owner, impacts));
   }

   private static NamespacedKey key() {
      return new NamespacedKey("magmacore", "magic_projectile");
   }
}
