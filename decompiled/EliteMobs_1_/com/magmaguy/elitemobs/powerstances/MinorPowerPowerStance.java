package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class MinorPowerPowerStance implements Listener, AutoCloseable {
   public static int trackAmount = 2;
   public static int individualEffectsPerTrack = 2;
   private EliteEntity eliteEntity;
   private final List<Item> spawnedItems = new ArrayList<>();
   private VisualItemProcessor visualItemProcessor;

   public MinorPowerPowerStance(EliteEntity eliteEntity) {
      this(eliteEntity, ignored -> {});
   }

   public MinorPowerPowerStance(EliteEntity eliteEntity, Consumer<Runnable> cleanupRegistrar) {
      if (MobCombatSettingsConfig.isEnableVisualEffectsForNaturalMobs()) {
         if (!MobCombatSettingsConfig.isDisableVisualEffectsForSpawnerMobs() || eliteEntity.isNaturalEntity()) {
            if (eliteEntity.getLivingEntity() != null) {
               this.eliteEntity = eliteEntity;
               Objects.requireNonNull(cleanupRegistrar, "cleanupRegistrar").accept(this::close);
               if (!eliteEntity.isMinorVisualEffect()) {
                  eliteEntity.setMinorVisualEffect(true);
                  if (eliteEntity.getMinorPowerCount() >= 1) {
                     if (MobCombatSettingsConfig.isObfuscateMobPowers() && eliteEntity.isVisualEffectObfuscated()) {
                        Object[][] multiDimensionalTrailTracker = new Object[trackAmount][individualEffectsPerTrack];

                        for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
                           ArrayList<Object> localObjects = new ArrayList<>();

                           for (int a = 0; a < multiDimensionalTrailTracker.length; a++) {
                              localObjects.addAll(this.addObfuscatedEffects());
                           }

                           for (int j = 0; j < multiDimensionalTrailTracker[0].length; j++) {
                              if (j < localObjects.size() && localObjects.get(j) != null) {
                                 multiDimensionalTrailTracker[i][j] = localObjects.get(j);
                              }
                           }
                        }

                        this.visualItemProcessor = new VisualItemProcessor(
                           multiDimensionalTrailTracker, MinorPowerStanceMath.cachedVectors, eliteEntity.isMinorVisualEffect(), 30, eliteEntity
                        );
                     } else {
                        Object[][] multiDimensionalTrailTracker = new Object[trackAmount][eliteEntity.getMinorPowerCount() * individualEffectsPerTrack];

                        for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
                           ArrayList<Object> localObjects = new ArrayList<>();

                           for (int a = 0; a < multiDimensionalTrailTracker.length; a++) {
                              localObjects.addAll(this.addAllEffects());
                           }

                           for (int jx = 0; jx < multiDimensionalTrailTracker[0].length; jx++) {
                              if (jx < localObjects.size() && localObjects.get(jx) != null) {
                                 multiDimensionalTrailTracker[i][jx] = localObjects.get(jx);
                              }
                           }
                        }

                        this.visualItemProcessor = new VisualItemProcessor(
                           multiDimensionalTrailTracker, MinorPowerStanceMath.cachedVectors, eliteEntity.isMinorVisualEffect(), 30, eliteEntity
                        );
                     }
                  }
               }
            }
         }
      }
   }

   private ArrayList<Object> addObfuscatedEffects() {
      return new ArrayList<>(List.of(Particle.END_ROD));
   }

   private ArrayList<Object> addAllEffects() {
      ArrayList<Object> effects = new ArrayList<>();

      for (String trail : PowerStanceEffectSelector.selectTrails(this.eliteEntity, false)) {
         effects.add(this.effectParser(trail));
      }

      return effects;
   }

   private Object effectParser(String powerString) {
      Material material = null;

      try {
         material = Material.valueOf(powerString);
      } catch (IllegalArgumentException var4) {
      }

      return material != null ? this.addEffect(material) : this.addEffect(Particle.valueOf(powerString));
   }

   private Object addEffect(Material material) {
      Item item = this.eliteEntity.getLivingEntity().getWorld().dropItem(this.eliteEntity.getLivingEntity().getLocation(), new ItemStack(material));
      this.spawnedItems.add(item);
      item.setPickupDelay(Integer.MAX_VALUE);
      item.setGravity(false);
      item.setInvulnerable(true);
      EntityTracker.registerVisualEffects(item);
      return item;
   }

   private Object addEffect(Particle particle) {
      return particle;
   }

   @Override
   public void close() {
      if (this.visualItemProcessor != null) {
         this.visualItemProcessor.close();
      }

      for (Item item : this.spawnedItems) {
         item.remove();
         EntityTracker.unregister(item, RemovalReason.EFFECT_TIMEOUT);
      }

      this.spawnedItems.clear();
   }
}
