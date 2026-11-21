package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

final class HitEffectManager {
   private static final List<HitEffectManager.HitEffect> activeEffects = new ArrayList<>();

   private HitEffectManager() {
   }

   static void createResistEffect(EliteEntity eliteEntity, Player player) {
      if (sameValidWorld(eliteEntity, player)) {
         try {
            activeEffects.add(new HitEffectManager.ResistEffect(eliteEntity, player));
         } catch (RuntimeException var3) {
         }
      }
   }

   static void createWeakEffect(EliteEntity eliteEntity, Player player) {
      if (sameValidWorld(eliteEntity, player)) {
         HitEffectManager.WeakEffect effect = HitEffectManager.WeakEffect.create(eliteEntity, player);
         if (effect != null) {
            activeEffects.add(effect);
         }
      }
   }

   static void update() {
      Iterator<HitEffectManager.HitEffect> iterator = activeEffects.iterator();

      while (iterator.hasNext()) {
         HitEffectManager.HitEffect effect = iterator.next();
         boolean keep = false;

         try {
            keep = effect.update();
         } catch (RuntimeException var10) {
            MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Discarding a failed combat hit effect", (Throwable)var10);
         }

         if (!keep) {
            try {
               effect.cleanup();
            } catch (RuntimeException var8) {
               MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Failed to clean a combat hit effect", (Throwable)var8);
            } finally {
               iterator.remove();
            }
         }
      }
   }

   static void shutdown() {
      activeEffects.forEach(effect -> {
         try {
            effect.cleanup();
         } catch (RuntimeException var2) {
            MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Failed to clean a combat hit effect during shutdown", (Throwable)var2);
         }
      });
      activeEffects.clear();
   }

   private static boolean sameValidWorld(EliteEntity eliteEntity, Player player) {
      if (eliteEntity != null && player != null && eliteEntity.isValid() && player.isValid()) {
         Location location = eliteEntity.getLocation();
         return location != null && location.getWorld() != null && location.getWorld().equals(player.getWorld());
      } else {
         return false;
      }
   }

   private static Location resistLocation(Player player, EliteEntity eliteEntity) {
      Vector direction = player.getLocation().subtract(eliteEntity.getLocation()).toVector().normalize().multiply(1.5);
      Location location = eliteEntity.getLocation().add(direction);
      location.setDirection(direction);
      return location;
   }

   private interface HitEffect {
      boolean update();

      void cleanup();
   }

   private static final class ResistEffect implements HitEffectManager.HitEffect {
      private final EliteEntity eliteEntity;
      private final Player player;
      private final ArmorStand armorStand;
      private int ticks;
      private boolean cleaned;

      private ResistEffect(EliteEntity eliteEntity, Player player) {
         this.eliteEntity = eliteEntity;
         this.player = player;
         Location location = HitEffectManager.resistLocation(player, eliteEntity);
         if (location.getWorld() == null) {
            throw new IllegalStateException("Resist effect has no world");
         } else {
            ArmorStand spawnedArmorStand = (ArmorStand)location.getWorld().spawn(location, ArmorStand.class, stand -> {
               stand.setVisible(false);
               stand.setGravity(false);
               stand.setMarker(true);
               stand.setPersistent(false);
               stand.getEquipment().setItemInMainHand(new ItemStack(Material.SHIELD));
               stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
               stand.setRightArmPose(new EulerAngle(Math.PI / 2, Math.PI * 3.0 / 2.0, Math.PI));
            });

            try {
               EntityTracker.registerVisualEffects(spawnedArmorStand);
            } catch (RuntimeException var6) {
               spawnedArmorStand.remove();
               throw var6;
            }

            this.armorStand = spawnedArmorStand;
         }
      }

      @Override
      public boolean update() {
         if (this.ticks <= 20 && HitEffectManager.sameValidWorld(this.eliteEntity, this.player)) {
            try {
               this.armorStand.teleport(HitEffectManager.resistLocation(this.player, this.eliteEntity));
            } catch (IllegalArgumentException var2) {
            }

            this.ticks++;
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void cleanup() {
         if (!this.cleaned) {
            EntityTracker.unregister(this.armorStand, RemovalReason.EFFECT_TIMEOUT);
            this.cleaned = true;
         }
      }
   }

   private static final class WeakEffect implements HitEffectManager.HitEffect {
      private final EliteEntity eliteEntity;
      private final Player player;
      private final FakeText[] displays;
      private final Location[] locations;
      private int ticks;

      private WeakEffect(EliteEntity eliteEntity, Player player, FakeText[] displays, Location[] locations) {
         this.eliteEntity = eliteEntity;
         this.player = player;
         this.displays = displays;
         this.locations = locations;
      }

      private static HitEffectManager.WeakEffect create(EliteEntity eliteEntity, Player player) {
         FakeText[] displays = new FakeText[2];
         Location[] locations = new Location[2];
         displays[0] = createDisplay(player, eliteEntity, -1, locations, 0);
         displays[1] = createDisplay(player, eliteEntity, 1, locations, 1);
         if (displays[0] != null && displays[1] != null) {
            return new HitEffectManager.WeakEffect(eliteEntity, player, displays, locations);
         } else {
            for (FakeText display : displays) {
               if (display != null) {
                  display.remove();
               }
            }

            return null;
         }
      }

      @Override
      public boolean update() {
         if (this.ticks <= 10 && HitEffectManager.sameValidWorld(this.eliteEntity, this.player)) {
            for (int index = 0; index < this.displays.length; index++) {
               Location newLocation = this.locations[index]
                  .add(this.eliteEntity.getLocation().subtract(this.locations[index]).toVector().normalize().multiply(0.4));
               this.displays[index].teleport(newLocation);
            }

            this.ticks++;
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void cleanup() {
         for (FakeText display : this.displays) {
            if (display != null) {
               display.remove();
            }
         }
      }

      private static FakeText createDisplay(Player player, EliteEntity eliteEntity, int offset, Location[] locations, int index) {
         Vector direction = player.getLocation()
            .clone()
            .add(new Vector(0, 2, 0))
            .subtract(eliteEntity.getLocation())
            .toVector()
            .normalize()
            .multiply(3.0)
            .rotateAroundY((Math.PI / 8) * offset);
         Location location = eliteEntity.getLocation().add(direction);
         location.setDirection(direction.clone().multiply(-1));
         if (location.getWorld() == null) {
            return null;
         } else {
            locations[index] = location.clone();
            FakeText display = VisualDisplay.createStyledFakeText(location, "&9&l✦", Color.fromARGB(0, 0, 0, 0), true, 1.0F);
            if (display != null) {
               display.displayTo(player);
            }

            return display;
         }
      }
   }
}
