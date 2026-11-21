package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.PlayerSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ScanManager {
   private final Main plugin;
   private final CollectionManager collectionManager;
   private final ProximityManager proximityManager;
   private final ParticleManager particleManager;
   private final PlaceModeManager placeModeManager;
   private final AtomicBoolean processing = new AtomicBoolean(false);
   private BukkitTask task;

   public ScanManager(
      Main plugin, CollectionManager collectionManager, ProximityManager proximityManager, ParticleManager particleManager, PlaceModeManager placeModeManager
   ) {
      this.plugin = plugin;
      this.collectionManager = collectionManager;
      this.proximityManager = proximityManager;
      this.particleManager = particleManager;
      this.placeModeManager = placeModeManager;
   }

   public void start() {
      this.stop();
      this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 20L, 20L);
   }

   public void stop() {
      if (this.task != null) {
         this.task.cancel();
         this.task = null;
      }
   }

   private void tick() {
      if (!this.processing.get()) {
         this.processing.set(true);
         Set<UUID> availableCollections = new HashSet<>();
         Set<UUID> singlePlayerFindCollections = new HashSet<>();

         for (Collection c : this.collectionManager.getAllCollections()) {
            if (this.collectionManager.isCollectionAvailable(c)) {
               availableCollections.add(c.getId());
               if (c.isSinglePlayerFind()) {
                  singlePlayerFindCollections.add(c.getId());
               }
            }
         }

         List<PlayerSnapshot> snapshots = new ArrayList<>();

         for (Player player : Bukkit.getOnlinePlayers()) {
            snapshots.add(new PlayerSnapshot(player, player.getLocation()));
         }

         Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
               this.proximityManager.processTick(snapshots, availableCollections);
               this.placeModeManager.processTick(snapshots);
               this.particleManager.processTick(snapshots, availableCollections, singlePlayerFindCollections);
            } finally {
               this.processing.set(false);
            }
         });
      }
   }
}
