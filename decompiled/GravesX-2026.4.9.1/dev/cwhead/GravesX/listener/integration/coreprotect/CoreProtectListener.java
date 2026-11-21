package dev.cwhead.GravesX.listener.integration.coreprotect;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.event.GraveAutoLootEvent;
import dev.cwhead.GravesX.event.GraveBlockPlaceEvent;
import dev.cwhead.GravesX.event.GraveBreakEvent;
import dev.cwhead.GravesX.event.GraveCloseEvent;
import dev.cwhead.GravesX.event.GraveExplodeEvent;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@Deprecated
public class CoreProtectListener implements Listener {
   private final Graves plugin;

   @Deprecated
   public CoreProtectListener(Graves plugin) {
      this.plugin = plugin;
   }

   @Deprecated
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onGraveBreak(GraveBreakEvent e) {
      if (!e.isCancelled()) {
         if (this.plugin.getIntegrationManager().hasCoreProtect()) {
            CoreProtectAPI coreProtect = this.plugin.getIntegrationManager().getCoreProtect().getCoreProtectAPI();

            try {
               e.getBlock();
               coreProtect.logRemoval(e.getPlayer().getName(), e.getGrave().getLocationDeath(), e.getBlock().getType(), e.getBlock().getBlockData());
            } catch (NullPointerException var4) {
               coreProtect.logContainerTransaction(e.getPlayer().getName(), e.getGrave().getLocationDeath());
            }

            this.plugin.debugMessage("Logged CoreProtect removal data for " + e.getGrave().getLocationDeath() + ".", 5);
         }
      }
   }

   @Deprecated
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onGraveAutoLoot(GraveAutoLootEvent e) {
      if (!e.isCancelled()) {
         if (this.plugin.getIntegrationManager().hasCoreProtect()) {
            CoreProtectAPI coreProtect = this.plugin.getIntegrationManager().getCoreProtect().getCoreProtectAPI();
            coreProtect.logContainerTransaction(e.getPlayer() != null ? e.getPlayer().getName() : null, e.getGrave().getLocationDeath());

            try {
               e.getBlock();
               coreProtect.logRemoval(
                  e.getPlayer() != null ? e.getPlayer().getName() : null, e.getGrave().getLocationDeath(), e.getBlock().getType(), e.getBlock().getBlockData()
               );
            } catch (NullPointerException var4) {
               coreProtect.logContainerTransaction(e.getPlayer() != null ? e.getPlayer().getName() : null, e.getGrave().getLocationDeath());
            }

            this.plugin.debugMessage("Logged CoreProtect interaction and removal data for " + e.getGrave().getLocationDeath() + ".", 5);
         }
      }
   }

   @Deprecated
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onGraveBlockPlace(GraveBlockPlaceEvent e) {
      if (!e.isCancelled()) {
         if (this.plugin.getIntegrationManager().hasCoreProtect()) {
            CoreProtectAPI coreProtect = this.plugin.getIntegrationManager().getCoreProtect().getCoreProtectAPI();

            try {
               e.getBlock();
               coreProtect.logPlacement(
                  e.getPlayer() != null ? e.getPlayer().getName() : null, e.getGrave().getLocationDeath(), e.getBlock().getType(), e.getBlock().getBlockData()
               );
            } catch (NullPointerException var4) {
               coreProtect.logContainerTransaction(e.getPlayer() != null ? e.getPlayer().getName() : null, e.getGrave().getLocationDeath());
            }

            this.plugin.debugMessage("Logged CoreProtect placement data for " + e.getGrave().getLocationDeath() + ".", 5);
         }
      }
   }

   @Deprecated
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onGraveClose(GraveCloseEvent e) {
      if (!e.isCancelled()) {
         if (this.plugin.getIntegrationManager().hasCoreProtect()) {
            CoreProtectAPI coreProtect = this.plugin.getIntegrationManager().getCoreProtect().getCoreProtectAPI();
            coreProtect.logContainerTransaction(e.getPlayer().getName(), e.getGrave().getLocationDeath());
            this.plugin.debugMessage("Logged CoreProtect interaction data for " + e.getGrave().getLocationDeath() + ".", 5);
         }
      }
   }

   @Deprecated
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onGraveOpen(GraveCloseEvent e) {
      if (!e.isCancelled()) {
         if (this.plugin.getIntegrationManager().hasCoreProtect()) {
            CoreProtectAPI coreProtect = this.plugin.getIntegrationManager().getCoreProtect().getCoreProtectAPI();
            coreProtect.logContainerTransaction(e.getPlayer().getName(), e.getGrave().getLocationDeath());
            this.plugin.debugMessage("Logged CoreProtect interaction data for " + e.getGrave().getLocationDeath() + ".", 5);
         }
      }
   }

   @Deprecated
   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onGraveExplode(GraveExplodeEvent e) {
      if (!e.isCancelled()) {
         if (this.plugin.getIntegrationManager().hasCoreProtect()) {
            CoreProtectAPI coreProtect = this.plugin.getIntegrationManager().getCoreProtect().getCoreProtectAPI();

            try {
               e.getBlock();
               e.getPlayer();
               coreProtect.logRemoval(e.getPlayer().getName(), e.getGrave().getLocationDeath(), e.getBlock().getType(), e.getBlock().getBlockData());
            } catch (NullPointerException var4) {
               e.getPlayer();
               coreProtect.logContainerTransaction(e.getPlayer().getName(), e.getGrave().getLocationDeath());
            }

            this.plugin.debugMessage("Logged CoreProtect removal data for " + e.getGrave().getLocationDeath() + ".", 5);
         }
      }
   }
}
