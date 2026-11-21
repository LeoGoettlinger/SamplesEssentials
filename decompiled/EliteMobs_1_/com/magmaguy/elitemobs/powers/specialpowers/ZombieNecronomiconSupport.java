package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import com.magmaguy.elitemobs.powerstances.VisualItemInitializer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class ZombieNecronomiconSupport implements Listener {
   private static final Map<UUID, ZombieNecronomiconSupport.State> states = new ConcurrentHashMap<>();

   public static void handleBossDamagedByPlayer(EliteEntity eliteEntity, LivingEntity targetted, String summoningChant) {
      if (eliteEntity != null && targetted != null) {
         ZombieNecronomiconSupport.State state = states.computeIfAbsent(eliteEntity.getEliteUUID(), ignored -> new ZombieNecronomiconSupport.State(eliteEntity));
         state.pruneReinforcements();
         if (state.reinforcements.size() <= 9 && !state.hasReinforcementController()) {
            state.start(targetted, summoningChant);
         }
      }
   }

   @EventHandler
   public void onEliteMobRemove(EliteMobRemoveEvent event) {
      ZombieNecronomiconSupport.State state = states.remove(event.getEliteMobEntity().getEliteUUID());
      if (state != null) {
         state.cleanup(true, false);
      }
   }

   public static void shutdown() {
      for (ZombieNecronomiconSupport.State state : new ArrayList<>(states.values())) {
         state.cleanup(true, false);
      }

      states.clear();
   }

   private static void itemMover(Map<Integer, List<Item>> xTrack, Entity entity, int counter) {
      double a = 0.0;
      double b = 1.0;
      double c = 0.0;
      double numberOfPointsPerFullRotation = 64.0;
      double x = 0.0;
      double y = 0.0;

      for (int trackNumber : xTrack.keySet()) {
         List<Item> itemList = xTrack.get(trackNumber);

         for (Item item : itemList) {
            double z = itemList.indexOf(item) + 1;
            int newCounter = (int)(counter + trackNumber * (numberOfPointsPerFullRotation / 8.0));
            Location currentLocation = item.getLocation();
            Location centerLocationFixed = entity.getLocation().add(0.0, 3.0, 0.0);
            Vector vector = GenericRotationMatrixMath.applyRotation(a, b, c, numberOfPointsPerFullRotation, x, y, z, newCounter);
            Location newLocation = new Location(entity.getWorld(), vector.getX(), vector.getY(), vector.getZ()).add(centerLocationFixed);
            Vector velocity = newLocation.subtract(currentLocation).toVector().multiply(0.3);
            item.setVelocity(velocity);
         }
      }
   }

   private static final class State {
      private final UUID eliteUuid;
      private final EliteEntity eliteEntity;
      private final List<CustomBossEntity> reinforcements = new ArrayList<>();
      private final Map<Integer, List<Item>> visualItems = new HashMap<>();
      private LivingEntity target;
      private String summoningChant;
      private int chantIndex;
      private BukkitTask reinforcementController;
      private BukkitTask nameController;
      private BukkitTask visualController;

      private State(EliteEntity eliteEntity) {
         this.eliteUuid = eliteEntity.getEliteUUID();
         this.eliteEntity = eliteEntity;
      }

      private void start(LivingEntity target, String summoningChant) {
         this.target = target;
         this.summoningChant = summoningChant;
         LivingEntity owner = this.eliteEntity.getLivingEntity();
         if (owner != null && owner.isValid()) {
            this.eliteEntity.setAIEnabled(false);
            this.ensurePresentation();
            this.reinforcementController = (new BukkitRunnable() {
               public void run() {
                  State.this.runReinforcementTick();
               }
            }).runTaskTimer(MetadataHandler.PLUGIN, 60L, 60L);
         } else {
            this.cleanup(true, true);
         }
      }

      private void runReinforcementTick() {
         if (!this.hasValidContext()) {
            this.cleanup(true, true);
         } else {
            this.pruneReinforcements();
            LivingEntity owner = this.eliteEntity.getLivingEntity();
            if (this.reinforcements.size() >= 11) {
               this.eliteEntity.setAIEnabled(true);
               this.stopPresentation();
            } else {
               this.eliteEntity.setAIEnabled(false);
               this.ensurePresentation();
               this.spawnOneReinforcement(owner);
            }
         }
      }

      private boolean hasValidContext() {
         if (this.eliteEntity.isValid() && this.target != null && this.target.isValid()) {
            LivingEntity owner = this.eliteEntity.getLivingEntity();
            if (owner != null && owner.isValid()) {
               return this.target.getWorld() != owner.getWorld() ? false : this.target.getLocation().distanceSquared(owner.getLocation()) <= 900.0;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private void spawnOneReinforcement(LivingEntity owner) {
         int randomizedNumber = ThreadLocalRandom.current().nextInt(5) + 1;
         boolean spawnZombie = randomizedNumber < 5;
         String filename = spawnZombie ? "necronomicon_zombie.yml" : "necronomicon_skeleton.yml";
         CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(filename);
         if (customBossEntity == null) {
            Logger.warn(filename + " is not valid!");
            if (!spawnZombie) {
               this.stopAfterSpawnFailure();
            }
         } else {
            customBossEntity.spawn(owner.getLocation(), this.eliteEntity.getLevel(), false);
            if (customBossEntity.exists() && customBossEntity.getLivingEntity() != null && customBossEntity.getLivingEntity().isValid()) {
               customBossEntity.getLivingEntity()
                  .setVelocity(
                     new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) / 30.0, 0.5, (ThreadLocalRandom.current().nextDouble() - 0.5) / 30.0)
                  );
               this.eliteEntity.addReinforcement(customBossEntity);
               this.reinforcements.add(customBossEntity);
            } else {
               this.stopAfterSpawnFailure();
            }
         }
      }

      private void ensurePresentation() {
         LivingEntity owner = this.eliteEntity.getLivingEntity();
         if (owner != null && owner.isValid() && !this.eliteEntity.isAIEnabled()) {
            this.startNameController(owner);
            if (MobCombatSettingsConfig.isEnableWarningVisualEffects()) {
               this.startVisualController(owner);
            }
         }
      }

      private void startNameController(final LivingEntity owner) {
         if (!isActive(this.nameController)) {
            String convertedChant = ChatColorConverter.convert(this.summoningChant == null ? "" : this.summoningChant);
            if (convertedChant == null || convertedChant.isBlank()) {
               convertedChant = this.eliteEntity.getName();
            }

            if (convertedChant == null) {
               convertedChant = "";
            }

            final String fullChant = convertedChant.length() < 31 ? (convertedChant + " ".repeat(31)).substring(0, 31) : convertedChant;
            this.nameController = (new BukkitRunnable() {
               public void run() {
                  if (owner.isValid() && !State.this.eliteEntity.isAIEnabled()) {
                     if (State.this.chantIndex + 31 > fullChant.length()) {
                        State.this.chantIndex = 0;
                     }

                     owner.setCustomName(fullChant.substring(State.this.chantIndex, State.this.chantIndex + 31));
                     State.this.chantIndex++;
                  } else {
                     State.this.stopPresentation();
                  }
               }
            }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
         }
      }

      private void startVisualController(final LivingEntity owner) {
         if (!isActive(this.visualController)) {
            this.visualController = (new BukkitRunnable() {
               private int counter;

               public void run() {
                  if (owner.isValid() && !State.this.eliteEntity.isAIEnabled()) {
                     if (this.counter == 0) {
                        for (int i = 0; i < 8; i++) {
                           List<Item> itemList = new ArrayList<>();

                           for (int j = 0; j < 4; j++) {
                              ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);
                              itemList.add(VisualItemInitializer.initializeItem(itemStack, owner.getLocation()));
                           }

                           State.this.visualItems.put(i, itemList);
                        }
                     } else {
                        ZombieNecronomiconSupport.itemMover(State.this.visualItems, owner, this.counter);
                     }

                     this.counter++;
                  } else {
                     State.this.stopPresentation();
                  }
               }
            }).runTaskTimer(MetadataHandler.PLUGIN, 5L, 5L);
         }
      }

      private boolean hasReinforcementController() {
         return isActive(this.reinforcementController);
      }

      private static boolean isActive(BukkitTask task) {
         return task != null && !task.isCancelled();
      }

      private void stopAfterSpawnFailure() {
         cancelTask(this.reinforcementController);
         this.reinforcementController = null;
         LivingEntity owner = this.eliteEntity.getLivingEntity();
         if (owner != null && owner.isValid()) {
            this.eliteEntity.setAIEnabled(true);
         }

         this.stopPresentation();
      }

      private void stopPresentation() {
         cancelTask(this.nameController);
         cancelTask(this.visualController);
         this.nameController = null;
         this.visualController = null;
         this.removeVisualItems();
         LivingEntity owner = this.eliteEntity.getLivingEntity();
         if (owner != null && owner.isValid()) {
            owner.setCustomName(this.eliteEntity.getName());
         }
      }

      private void cleanup(boolean cullReinforcements, boolean removeState) {
         cancelTask(this.reinforcementController);
         this.reinforcementController = null;
         this.stopPresentation();
         LivingEntity owner = this.eliteEntity.getLivingEntity();
         if (owner != null && owner.isValid()) {
            this.eliteEntity.setAIEnabled(true);
         }

         if (cullReinforcements) {
            for (CustomBossEntity reinforcement : new ArrayList<>(this.reinforcements)) {
               if (reinforcement != null && reinforcement.isValid()) {
                  reinforcement.remove(RemovalReason.REINFORCEMENT_CULL);
               }
            }

            this.reinforcements.clear();
         }

         if (removeState) {
            ZombieNecronomiconSupport.states.remove(this.eliteUuid, this);
         }
      }

      private void pruneReinforcements() {
         this.reinforcements.removeIf(entity -> entity == null || !entity.exists());
      }

      private void removeVisualItems() {
         for (List<Item> itemList : this.visualItems.values()) {
            for (Item item : itemList) {
               if (item != null && item.isValid()) {
                  item.remove();
               }
            }
         }

         this.visualItems.clear();
      }

      private static void cancelTask(BukkitTask task) {
         if (task != null && !task.isCancelled()) {
            task.cancel();
         }
      }
   }
}
