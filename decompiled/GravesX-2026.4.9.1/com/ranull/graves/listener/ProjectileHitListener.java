package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.compatibility.CompatibilitySoundEnum;
import dev.cwhead.GravesX.event.GraveProjectileHitEvent;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ProjectileHitListener implements Listener {
   private final Graves plugin;

   public ProjectileHitListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onProjectileHitGrave(ProjectileHitEvent event) {
      Block block = event.getHitBlock();
      if (block != null) {
         Grave grave = this.plugin.getCacheManager().getGrave(block);
         if (grave != null) {
            if (this.plugin.getConfigManager().getConfigSection("drop.projectile.enabled", grave).getBoolean("drop.projectile.enabled")) {
               Location location = block.getLocation();
               ProjectileSource shooter = event.getEntity().getShooter();
               ProjectileHitListener.ShooterKind kind = this.classifyShooter(shooter);
               switch (kind) {
                  case PLAYER:
                     if (!this.plugin.getConfigManager().getConfigSection("drop.projectile.player", grave).getBoolean("drop.projectile.player")) {
                        return;
                     }

                     this.handleProjectileHitFromPlayer((Player)shooter, event, block, grave, location);
                     break;
                  case LIVING:
                     if (!this.plugin.getConfigManager().getConfigSection("drop.projectile.living-entity", grave).getBoolean("drop.projectile.living-entity")) {
                        return;
                     }

                     this.handleProjectileHitFromLiving((LivingEntity)shooter, event, block, grave, location);
                     break;
                  case OTHER:
                     if (!this.plugin.getConfigManager().getConfigSection("drop.projectile.other", grave).getBoolean("drop.projectile.other")) {
                        return;
                     }

                     this.handleProjectileHitFromOther(event, block, grave, location);
               }
            }
         }
      }
   }

   private ProjectileHitListener.ShooterKind classifyShooter(ProjectileSource shooter) {
      if (shooter instanceof Player) {
         return ProjectileHitListener.ShooterKind.PLAYER;
      } else {
         return shooter instanceof LivingEntity ? ProjectileHitListener.ShooterKind.LIVING : ProjectileHitListener.ShooterKind.OTHER;
      }
   }

   private void handleProjectileHitFromPlayer(Player player, ProjectileHitEvent event, Block block, Grave grave, Location location) {
      GraveProjectileHitEvent modern = new GraveProjectileHitEvent(location, player, grave, event.getEntity(), block);
      this.plugin.getServer().getPluginManager().callEvent(modern);
      com.ranull.graves.event.GraveProjectileHitEvent legacy = new com.ranull.graves.event.GraveProjectileHitEvent(
         location, player, grave, event.getEntity(), block
      );
      this.plugin.getServer().getPluginManager().callEvent(legacy);
      if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
         Location effectiveLoc = modern.hasLocation() ? modern.getLocation() : (legacy.hasLocation() ? legacy.getLocation() : location);
         this.playLootedExplosionEffectIfEnabled(grave);
         this.breakCloseAndEffect(effectiveLoc, grave);
         if (this.plugin.getIntegrationManager().hasNoteBlockAPI()) {
            if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(player)) {
               this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(player);
            }

            if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
               this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
            }
         }

         String playerName = player.getDisplayName();
         this.plugin.debugMessage("Grave destroyed by projectile " + event.getEntity().getName().toLowerCase() + " caused by player " + playerName + ".", 1);
      }
   }

   private void handleProjectileHitFromLiving(LivingEntity livingShooter, ProjectileHitEvent event, Block block, Grave grave, Location location) {
      GraveProjectileHitEvent modern = new GraveProjectileHitEvent(location, livingShooter, grave, event.getEntity(), block);
      this.plugin.getServer().getPluginManager().callEvent(modern);
      com.ranull.graves.event.GraveProjectileHitEvent legacy = new com.ranull.graves.event.GraveProjectileHitEvent(
         location, livingShooter, grave, event.getEntity(), block
      );
      this.plugin.getServer().getPluginManager().callEvent(legacy);
      if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
         Location effectiveLoc = modern.hasLocation() ? modern.getLocation() : (legacy.hasLocation() ? legacy.getLocation() : location);
         this.breakCloseAndEffect(effectiveLoc, grave);
         if (this.plugin.getIntegrationManager().hasNoteBlockAPI()) {
            if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(grave.getOwnerUUID())) {
               this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(grave.getOwnerUUID());
            }

            if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
               this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
            }
         }

         this.plugin
            .debugMessage(
               "Grave destroyed by projectile "
                  + event.getEntity().getName().toLowerCase()
                  + "  caused by living entity "
                  + livingShooter.getType().name().toLowerCase()
                  + ".",
               1
            );
      }
   }

   private void handleProjectileHitFromOther(ProjectileHitEvent event, Block block, Grave grave, Location location) {
      GraveProjectileHitEvent modern = new GraveProjectileHitEvent(location, grave, event.getEntity(), block);
      this.plugin.getServer().getPluginManager().callEvent(modern);
      com.ranull.graves.event.GraveProjectileHitEvent legacy = new com.ranull.graves.event.GraveProjectileHitEvent(location, grave, event.getEntity(), block);
      this.plugin.getServer().getPluginManager().callEvent(legacy);
      if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
         Location effectiveLoc = modern.hasLocation() ? modern.getLocation() : (legacy.hasLocation() ? legacy.getLocation() : location);
         this.breakCloseAndEffect(effectiveLoc, grave);
         if (this.plugin.getIntegrationManager().hasNoteBlockAPI()) {
            if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForPlayer(grave.getOwnerUUID())) {
               this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForPlayer(grave.getOwnerUUID());
            }

            if (this.plugin.getIntegrationManager().getNoteBlockAPI().isSongPlayingForAllPlayers()) {
               this.plugin.getIntegrationManager().getNoteBlockAPI().stopSongForAllPlayers();
            }
         }

         this.plugin
            .debugMessage("Grave destroyed by projectile " + event.getEntity().getName().toLowerCase() + "  caused by unknown player or living entity.", 1);
      }
   }

   private void playLootedExplosionEffectIfEnabled(Grave grave) {
      if (this.plugin.getConfigManager().getConfigSection("drop.looted-explosion-effect", grave).getBoolean("drop.looted-explosion-effect", false)) {
         try {
            Location loc = grave.getLocationDeath();
            Objects.requireNonNull(loc.getWorld()).spawnParticle(this.plugin.getVersionManager().getParticleForVersion("EXPLOSION"), loc, 1);

            try {
               loc.getWorld().playSound(loc, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("ENTITY_GENERIC_EXPLODE")), 1.0F, 1.0F);
            } catch (Exception var4) {
               loc.getWorld().playSound(loc, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("EXPLODE")), 1.0F, 1.0F);
            }
         } catch (Exception var5) {
         }
      }
   }

   private void breakCloseAndEffect(Location loc, Grave grave) {
      this.plugin.getGraveManager().breakGrave(loc, grave);
      this.plugin.getGraveManager().closeGrave(grave);
      this.plugin.getGraveManager().playEffect("effect.loot", loc, grave);
   }

   private static enum ShooterKind {
      PLAYER,
      LIVING,
      OTHER;
   }
}
