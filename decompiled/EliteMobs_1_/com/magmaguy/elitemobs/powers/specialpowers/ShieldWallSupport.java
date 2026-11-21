package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class ShieldWallSupport {
   private static final Map<UUID, ShieldWallSupport.ShieldWallState> states = new ConcurrentHashMap<>();

   private ShieldWallSupport() {
   }

   public static boolean isActive(EliteEntity eliteEntity) {
      ShieldWallSupport.ShieldWallState state = states.get(eliteEntity.getEliteUUID());
      return state != null && state.isActive;
   }

   public static boolean initialize(EliteEntity eliteEntity, int openingDirection) {
      ShieldWallSupport.ShieldWallState existing = states.get(eliteEntity.getEliteUUID());
      if (existing != null && existing.isActive) {
         return false;
      } else {
         ShieldWallSupport.ShieldWallState state = new ShieldWallSupport.ShieldWallState(eliteEntity);
         state.initialize(openingDirection);
         states.put(eliteEntity.getEliteUUID(), state);
         return true;
      }
   }

   public static boolean preventDamage(Player player, EliteEntity eliteEntity, double damage) {
      ShieldWallSupport.ShieldWallState state = states.get(eliteEntity.getEliteUUID());
      return state != null && state.preventDamage(player, eliteEntity, damage);
   }

   public static void deactivate(EliteEntity eliteEntity) {
      ShieldWallSupport.ShieldWallState state = states.remove(eliteEntity.getEliteUUID());
      if (state != null) {
         state.deactivate();
      }
   }

   private static enum Direction {
      NORTH,
      SOUTH,
      EAST,
      WEST;
   }

   private static final class ShieldWallState {
      private final EliteEntity eliteEntity;
      private final Map<ShieldWallSupport.Direction, List<ArmorStand>> armorStands = new EnumMap<>(ShieldWallSupport.Direction.class);
      private final Map<ShieldWallSupport.Direction, Vector> armorStandOffBaseOffsets = new EnumMap<>(ShieldWallSupport.Direction.class);
      private double northHealthPool;
      private double southHealthPool;
      private double eastHealthPool;
      private double westHealthPool;
      private boolean isActive = false;
      private BukkitTask task = null;

      private ShieldWallState(EliteEntity eliteEntity) {
         this.eliteEntity = eliteEntity;
         this.offsetInitializer();
      }

      private boolean preventDamage(Player player, EliteEntity eliteEntity, double damage) {
         if (!Objects.equals(player.getLocation().getWorld(), eliteEntity.getLocation().getWorld())) {
            return false;
         } else {
            Vector direction = player.getLocation().subtract(eliteEntity.getLocation()).toVector().normalize();
            ShieldWallSupport.Direction damageDirection;
            if (Math.abs(direction.getX()) > Math.abs(direction.getZ())) {
               damageDirection = direction.getX() > 0.0 ? ShieldWallSupport.Direction.EAST : ShieldWallSupport.Direction.WEST;
            } else if (direction.getZ() > 0.0) {
               damageDirection = ShieldWallSupport.Direction.SOUTH;
            } else {
               damageDirection = ShieldWallSupport.Direction.NORTH;
            }

            switch (damageDirection) {
               case NORTH:
                  if (this.northHealthPool > 0.0) {
                     this.northHealthPool -= damage;
                     return true;
                  }
                  break;
               case SOUTH:
                  if (this.southHealthPool > 0.0) {
                     this.southHealthPool -= damage;
                     return true;
                  }
                  break;
               case EAST:
                  if (this.eastHealthPool > 0.0) {
                     this.eastHealthPool -= damage;
                     return true;
                  }
                  break;
               case WEST:
                  if (this.westHealthPool > 0.0) {
                     this.westHealthPool -= damage;
                     return true;
                  }
            }

            return false;
         }
      }

      private void initialize(int direction) {
         this.northHealthPool = this.eliteEntity.getLevel();
         this.southHealthPool = this.eliteEntity.getLevel();
         this.eastHealthPool = this.eliteEntity.getLevel();
         this.westHealthPool = this.eliteEntity.getLevel();
         switch (direction) {
            case 1:
               this.northHealthPool = 0.0;
               break;
            case 2:
               this.eastHealthPool = 0.0;
               break;
            case 3:
               this.southHealthPool = 0.0;
               break;
            case 4:
               this.westHealthPool = 0.0;
         }

         for (int i = 1; i < 5; i++) {
            if (direction != i) {
               this.armorStands.put(this.directionConverter(i), this.armorStandCreator(this.directionConverter(i), this.eliteEntity));
            }
         }

         this.armorStandTracker(this.eliteEntity);
         this.isActive = true;
      }

      private void deactivate() {
         this.isActive = false;
         if (this.task != null) {
            this.task.cancel();
            this.task = null;
         }

         for (List<ArmorStand> stands : this.armorStands.values()) {
            if (stands != null) {
               for (ArmorStand armorStand : stands) {
                  if (armorStand != null && armorStand.isValid()) {
                     EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                  }
               }
            }
         }

         this.armorStands.clear();
      }

      private ShieldWallSupport.Direction directionConverter(int direction) {
         return switch (direction) {
            case 1 -> ShieldWallSupport.Direction.NORTH;
            case 2 -> ShieldWallSupport.Direction.EAST;
            case 3 -> ShieldWallSupport.Direction.SOUTH;
            default -> ShieldWallSupport.Direction.WEST;
         };
      }

      private void offsetInitializer() {
         this.armorStandOffBaseOffsets.put(ShieldWallSupport.Direction.NORTH, new Vector(0, 0, -1));
         this.armorStandOffBaseOffsets.put(ShieldWallSupport.Direction.EAST, new Vector(1, 0, 0));
         this.armorStandOffBaseOffsets.put(ShieldWallSupport.Direction.SOUTH, new Vector(0, 0, 1));
         this.armorStandOffBaseOffsets.put(ShieldWallSupport.Direction.WEST, new Vector(-1, 0, 0));
      }

      private Location getRealLocation(ShieldWallSupport.Direction direction, Location livingEntityLocation, int iteration) {
         Vector finalOffsetVector;
         if (direction != ShieldWallSupport.Direction.NORTH && direction != ShieldWallSupport.Direction.SOUTH) {
            finalOffsetVector = this.armorStandOffBaseOffsets.get(direction).clone().add(new Vector(0, 0, iteration));
         } else {
            finalOffsetVector = this.armorStandOffBaseOffsets.get(direction).clone().add(new Vector(iteration, 0, 0));
         }

         Location realLocation = livingEntityLocation.clone().add(finalOffsetVector);
         switch (direction) {
            case NORTH:
               realLocation.setYaw(180.0F);
               break;
            case SOUTH:
               realLocation.setYaw(0.0F);
               break;
            case EAST:
               realLocation.setYaw(-90.0F);
               break;
            case WEST:
               realLocation.setYaw(90.0F);
         }

         realLocation.setPitch(0.0F);
         return realLocation;
      }

      private List<ArmorStand> armorStandCreator(ShieldWallSupport.Direction direction, EliteEntity eliteEntity) {
         List<ArmorStand> armorStands = new ArrayList<>();

         for (int i = -1; i < 2; i++) {
            ArmorStand armorStand = VisualDisplay.generateTemporaryArmorStand(
               this.getRealLocation(direction, eliteEntity.getLivingEntity().getLocation(), i), "Barrier"
            );
            armorStands.add(armorStand);
            armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.SHIELD));
            armorStand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
            armorStand.setRightArmPose(new EulerAngle(Math.PI / 2, Math.PI * 3.0 / 2.0, Math.PI));
         }

         return armorStands;
      }

      private void armorStandTracker(EliteEntity eliteEntity) {
         this.task = Bukkit.getScheduler()
            .runTaskTimer(
               MetadataHandler.PLUGIN,
               () -> {
                  if (eliteEntity.isValid()
                     && (this.northHealthPool != 0.0 || this.southHealthPool != 0.0 || this.eastHealthPool != 0.0 || this.westHealthPool != 0.0)
                     && this.isActive) {
                     int visualShieldsLeft = 0;

                     for (ShieldWallSupport.Direction direction : this.armorStands.keySet()) {
                        for (int i = -1; i < 2; i++) {
                           ArmorStand armorStand = this.armorStands.get(direction).get(i + 1);
                           if (armorStand != null && armorStand.isValid()) {
                              switch (direction) {
                                 case NORTH:
                                    if (0.33 * (i + 1) >= this.northHealthPool / eliteEntity.getLevel()) {
                                       EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                                    }
                                    break;
                                 case SOUTH:
                                    if (0.33 * (i + 1) >= this.southHealthPool / eliteEntity.getLevel()) {
                                       EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                                    }
                                    break;
                                 case EAST:
                                    if (0.33 * (i + 1) >= this.eastHealthPool / eliteEntity.getLevel()) {
                                       EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                                    }
                                    break;
                                 case WEST:
                                    if (0.33 * (i + 1) >= this.westHealthPool / eliteEntity.getLevel()) {
                                       EntityTracker.unregister(armorStand, RemovalReason.EFFECT_TIMEOUT);
                                    }
                              }

                              visualShieldsLeft++;
                              armorStand.teleport(this.getRealLocation(direction, eliteEntity.getLivingEntity().getLocation(), i));
                           }
                        }
                     }

                     if (visualShieldsLeft == 0) {
                        this.deactivate();
                        ShieldWallSupport.states.remove(eliteEntity.getEliteUUID());
                     }
                  } else {
                     this.deactivate();
                     ShieldWallSupport.states.remove(eliteEntity.getEliteUUID());
                  }
               },
               1L,
               1L
            );
      }
   }
}
