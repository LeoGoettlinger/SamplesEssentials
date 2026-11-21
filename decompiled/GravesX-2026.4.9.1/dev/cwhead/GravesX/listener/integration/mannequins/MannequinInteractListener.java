package dev.cwhead.GravesX.listener.integration.mannequins;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.UUIDUtil;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public final class MannequinInteractListener implements Listener {
   private static final String TAG_PREFIX = "gravesx_corpse:";
   private final Graves plugin;
   private final Map<UUID, Long> lastOpenByPlayer = new ConcurrentHashMap<>();
   private static final long OPEN_DEBOUNCE_MS = 250L;

   public MannequinInteractListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityDamage(EntityDamageEvent event) {
      Entity entity = event.getEntity();
      if (this.isCorpse(entity)) {
         event.setCancelled(true);
         this.plugin.debugMessage("[Mannequins] Blocked damage cause=" + event.getCause() + " entity=" + entity.getType() + " uuid=" + entity.getUniqueId(), 2);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      Entity victim = event.getEntity();
      if (this.isCorpse(victim)) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onTarget(EntityTargetEvent event) {
      Entity target = event.getTarget();
      if (target != null) {
         if (this.isCorpse(target)) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInteractAt(PlayerInteractAtEntityEvent event) {
      Entity clicked = event.getRightClicked();
      if (this.isCorpse(clicked)) {
         event.setCancelled(true);
         this.tryOpenOnce(event.getPlayer(), clicked, "InteractAt");
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onInteract(PlayerInteractEntityEvent event) {
      Entity clicked = event.getRightClicked();
      if (this.isCorpse(clicked)) {
         event.setCancelled(true);
         this.tryOpenOnce(event.getPlayer(), clicked, "Interact");
      }
   }

   private void tryOpenOnce(Player player, Entity clicked, String source) {
      if (player != null && clicked != null) {
         if (this.acquireDebounce(player.getUniqueId())) {
            Grave grave = this.resolveGraveFromCorpse(clicked);
            if (grave == null) {
               this.plugin
                  .debugMessage(
                     "[Mannequins] Right-click corpse but could not resolve grave uuid (source=" + source + ") entityUuid=" + clicked.getUniqueId(), 2
                  );
            } else {
               Location openAt = safeLocation(clicked);
               if (openAt == null) {
                  this.plugin.debugMessage("[Mannequins] Could not open grave (null location) (source=" + source + ") entityUuid=" + clicked.getUniqueId(), 2);
               } else {
                  this.plugin.getGraveManager().openGrave(player, openAt, grave);
                  this.plugin
                     .debugMessage(
                        "[Mannequins] Opened grave from mannequin (source=" + source + ") player=" + player.getName() + " graveUuid=" + grave.getUUID(), 2
                     );
               }
            }
         }
      }
   }

   private boolean acquireDebounce(UUID playerUuid) {
      long now = System.currentTimeMillis();
      Long last = this.lastOpenByPlayer.put(playerUuid, now);
      return last == null ? true : now - last > 250L;
   }

   private boolean isCorpse(Entity entity) {
      if (entity == null) {
         return false;
      } else {
         Set<String> tags = entity.getScoreboardTags();
         if (tags.isEmpty()) {
            return false;
         } else {
            for (String tag : tags) {
               if (tag != null && tag.startsWith("gravesx_corpse:")) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private Grave resolveGraveFromCorpse(Entity entity) {
      try {
         for (String tag : entity.getScoreboardTags()) {
            if (tag != null && tag.startsWith("gravesx_corpse:")) {
               String raw = tag.substring("gravesx_corpse:".length());
               if (raw.isEmpty()) {
                  return null;
               }

               UUID graveUuid = UUIDUtil.getUUID(raw);
               if (graveUuid == null && raw.length() == 32) {
                  graveUuid = fromUndashed(raw);
               }

               if (graveUuid == null) {
                  return null;
               }

               Grave grave = this.plugin.getCacheManager().getGraveMap().get(graveUuid);
               if (grave != null && grave.getUUID().equals(graveUuid)) {
                  return grave;
               }

               return null;
            }
         }
      } catch (Throwable var7) {
      }

      return null;
   }

   private static UUID fromUndashed(String s) {
      try {
         String dashed = s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16, 20) + "-" + s.substring(20);
         return UUID.fromString(dashed);
      } catch (Throwable var2) {
         return null;
      }
   }

   private static Location safeLocation(Entity e) {
      try {
         return e.getLocation();
      } catch (Throwable var2) {
         return null;
      }
   }
}
