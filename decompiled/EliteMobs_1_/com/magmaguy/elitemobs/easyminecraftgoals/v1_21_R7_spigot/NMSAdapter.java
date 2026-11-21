package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot;

import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.TransientMovementOverride;
import com.magmaguy.elitemobs.easyminecraftgoals.ammunition.RangedAmmunition;
import com.magmaguy.elitemobs.easyminecraftgoals.constants.OverridableWanderPriority;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeItem;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeItemSettings;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeTextSettings;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketInteractionEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketModelEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketTextEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.entitydata.BodyRotation;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.hitbox.Hitbox;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.massblockedit.MassEditBlocks;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.mind.NativeMindHost;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.move.Move;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.packets.FakeItemImpl;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.packets.FakeTextImpl;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.packets.PacketArmorStandEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.packets.PacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.packets.PacketDisplayEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.packets.PacketGenericEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.packets.PacketInteractionListener;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.passenger.PassengerOffset;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.pathfinding.NativePathfindingGoal;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.wanderbacktopoint.WanderBackToPointBehavior;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.wanderbacktopoint.WanderBackToPointGoal;
import com.magmaguy.elitemobs.magmacore.ai.MindActionSink;
import com.magmaguy.elitemobs.magmacore.ai.MindFailureListener;
import com.magmaguy.elitemobs.magmacore.ai.MindHost;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.arrow.EntityArrow;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardObjective;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;

public class NMSAdapter extends com.magmaguy.elitemobs.easyminecraftgoals.NMSAdapter {
   private NativeMindHost mindHost;
   private PacketInteractionListener packetInteractionListener;

   @Override
   public MindHost createMindHost(NamespacedKey identity, MindFailureListener failures, MindActionSink actions) {
      if (this.mindHost != null) {
         throw new IllegalStateException("This adapter already has a native mind host");
      } else {
         this.mindHost = new NativeMindHost(identity, failures, actions);
         return this.mindHost;
      }
   }

   @Override
   public boolean isMindBody(Entity entity) {
      return NativeMindHost.isMindCarrier(entity);
   }

   @Override
   public void shutdownMindHost() {
      if (this.mindHost != null) {
         this.mindHost.shutdown();
         this.mindHost = null;
      }
   }

   @Override
   public Optional<TransientMovementOverride> beginFlee(LivingEntity entity, Location threat, double speed) {
      return this.mindHost == null ? Optional.empty() : this.mindHost.beginFlee(entity, threat, speed);
   }

   @Override
   public RangedAmmunition grantOrdinaryAmmunition(Plugin plugin, Predicate<Player> eligible) {
      return new NativeRangedAmmunition(plugin, eligible).start();
   }

   @Override
   public float getConsumptionSeconds(ItemStack item) {
      net.minecraft.world.item.ItemStack nativeItem = CraftBukkitBridge.asNMSCopy(item);
      Consumable consumable = (Consumable)nativeItem.a(DataComponents.z);
      return consumable == null ? -1.0F : consumable.c();
   }

   @Override
   public ItemStack withConsumptionSeconds(ItemStack item, float seconds) {
      if (Float.isFinite(seconds) && !(seconds < 0.0F)) {
         net.minecraft.world.item.ItemStack nativeItem = CraftBukkitBridge.asNMSCopy(item);
         Consumable original = (Consumable)nativeItem.a(DataComponents.z);
         if (original == null) {
            throw new IllegalArgumentException("Item is not consumable");
         } else {
            nativeItem.b(DataComponents.z, new Consumable(seconds, original.d(), original.e(), original.f(), original.g()));
            return CraftBukkitBridge.asBukkitCopy(nativeItem);
         }
      } else {
         throw new IllegalArgumentException("Consumption duration must be finite and non-negative");
      }
   }

   @Override
   public void damageWithoutCooldown(LivingEntity target, double amount, Entity source) {
      if (Double.isFinite(amount) && !(amount < 0.0) && !(amount > Float.MAX_VALUE)) {
         EntityLiving victim = CraftBukkitBridge.getNMSLivingEntity(target);
         if (victim.generation) {
            throw new IllegalStateException("Cannot damage an entity during world generation");
         } else {
            net.minecraft.world.entity.Entity attacker = CraftBukkitBridge.getNMSEntity(source);
            DamageSources sources = victim.en();
            DamageSource original;
            if (attacker instanceof EntityArrow arrow) {
               original = sources.a(arrow, arrow.p());
            } else if (attacker instanceof EntityHuman player) {
               original = sources.a(player);
            } else {
               if (!(attacker instanceof EntityLiving living)) {
                  throw new IllegalArgumentException("Damage source must be a living attacker or arrow");
               }

               original = sources.b(living);
            }

            var damage = new DamageSource(original.l(), original.c(), original.d(), original.j()) {
               public boolean a(TagKey<DamageType> tag) {
                  return tag.equals(DamageTypeTags.e) || super.a(tag);
               }
            };
            victim.a((WorldServer)victim.ao(), damage, (float)amount);
         }
      } else {
         throw new IllegalArgumentException("Damage must be finite and fit a native float");
      }
   }

   private EntityCreature getPathfinderMob(Entity entity) {
      return CraftBukkitBridge.getNMSEntity(entity) instanceof EntityCreature pathfinderMob ? pathfinderMob : null;
   }

   @Override
   protected Optional<PathfindingHandle> createPathfindingHandle(LivingEntity livingEntity, int priority) {
      if (this.isMindBody(livingEntity)) {
         return this.mindHost == null ? Optional.empty() : this.mindHost.openPathfinding(livingEntity);
      } else {
         EntityCreature pathfinderMob = this.getPathfinderMob(livingEntity);
         if (pathfinderMob == null) {
            return Optional.empty();
         } else {
            NativePathfindingGoal goal = new NativePathfindingGoal(pathfinderMob, livingEntity, priority);
            goal.register();
            return Optional.of(goal);
         }
      }
   }

   @Override
   public boolean removeFreeWill(LivingEntity livingEntity) {
      EntityCreature pathfinderMob = this.getPathfinderMob(livingEntity);
      if (pathfinderMob == null) {
         return false;
      } else {
         pathfinderMob.gJ();
         return true;
      }
   }

   @Override
   public boolean isPositionEntityTicking(Location location) {
      return location.getWorld() == null
         ? false
         : CraftBukkitBridge.getServerLevel(location).h(BlockPosition.a(location.getX(), location.getY(), location.getZ()));
   }

   @Override
   public PacketModelEntity createPacketArmorStandEntity(Location location) {
      return new PacketArmorStandEntity(location);
   }

   @Override
   public PacketModelEntity createPacketDisplayEntity(Location location) {
      return new PacketDisplayEntity(location);
   }

   @Override
   public PacketTextEntity createPacketTextArmorStandEntity(Location location) {
      return new PacketArmorStandEntity(location);
   }

   @Override
   public boolean canReach(LivingEntity livingEntity, Location destination) {
      EntityCreature pathfinderMob = this.getPathfinderMob(livingEntity);
      return pathfinderMob == null ? false : Move.canReach(pathfinderMob, destination);
   }

   @Override
   public boolean setCustomHitbox(Entity entity, float width, float height, boolean fixed) {
      if (entity == null) {
         return false;
      } else {
         net.minecraft.world.entity.Entity nmsEntity = CraftBukkitBridge.getNMSEntity(entity);
         return Hitbox.setCustomHitbox(nmsEntity, width, height, fixed);
      }
   }

   @Override
   public boolean setPassengerOffset(Entity entity, double offsetX, double offsetY, double offsetZ) {
      return entity == null ? false : PassengerOffset.setPassengerOffset(CraftBukkitBridge.getNMSEntity(entity), offsetX, offsetY, offsetZ);
   }

   @Override
   public float getBodyRotation(Entity entity) {
      net.minecraft.world.entity.Entity nmsEntity = CraftBukkitBridge.getNMSEntity(entity);
      return BodyRotation.getBodyRotation(nmsEntity);
   }

   @Override
   public boolean move(LivingEntity livingEntity, double speedModifier, Location location) {
      EntityLiving nmsLivingEntity = CraftBukkitBridge.getNMSLivingEntity(livingEntity);
      EntityCreature pathfinderMob;
      if (nmsLivingEntity instanceof EntityCreature pm) {
         pathfinderMob = pm;
      } else {
         pathfinderMob = null;
      }

      if (!(nmsLivingEntity instanceof EntityInsentient)) {
         Bukkit.getLogger()
            .info(
               "[EasyMinecraftPathfinding] Entity type "
                  + livingEntity.getType()
                  + " does not extend Mob and is therefore unable to have goals! It will not be able to pathfind."
            );
         return false;
      } else {
         return Move.simpleMove(pathfinderMob, speedModifier, location);
      }
   }

   @Override
   public void doNotMove(LivingEntity livingEntity) {
      EntityCreature pathfinderMob = this.getPathfinderMob(livingEntity);
      if (pathfinderMob != null) {
         Move.doNotMove(pathfinderMob);
      }
   }

   @Override
   public boolean forcedMove(LivingEntity livingEntity, double speedModifier, Location location) {
      if (CraftBukkitBridge.getNMSLivingEntity(livingEntity) instanceof EntityInsentient mob) {
         return Move.forcedMove(mob, speedModifier, location);
      } else {
         Bukkit.getLogger()
            .info(
               "[EasyMinecraftPathfinding] Entity type "
                  + livingEntity.getType()
                  + " does not extend Mob and is therefore unable to have goals! It will not be able to pathfind."
            );
         return false;
      }
   }

   @Override
   public void universalMove(LivingEntity livingEntity, double speedModifier, Location location) {
      if (CraftBukkitBridge.getNMSLivingEntity(livingEntity) instanceof EntityInsentient mob) {
         Move.universalMove(mob, speedModifier, location);
      } else {
         Bukkit.getLogger()
            .info(
               "[EasyMinecraftPathfinding] Entity type "
                  + livingEntity.getType()
                  + " does not extend Mob and is therefore unable to have goals! It will not be able to pathfind."
            );
      }
   }

   @Override
   public AbstractWanderBackToPoint wanderBackToPoint(
      LivingEntity livingEntity,
      Location blockLocation,
      double maximumDistanceFromPoint,
      int maxDurationTicks,
      OverridableWanderPriority overridableWanderPriority
   ) {
      EntityLiving nmsLivingEntity = CraftBukkitBridge.getNMSLivingEntity(livingEntity);
      EntityCreature pathfinderMob;
      if (nmsLivingEntity instanceof EntityCreature pm) {
         pathfinderMob = pm;
      } else {
         pathfinderMob = null;
      }

      if (nmsLivingEntity instanceof EntityInsentient mob) {
         return (AbstractWanderBackToPoint)(overridableWanderPriority.brain
            ? new WanderBackToPointBehavior(livingEntity, mob, blockLocation, maximumDistanceFromPoint, overridableWanderPriority.priority, maxDurationTicks)
            : new WanderBackToPointGoal(
               mob, livingEntity, pathfinderMob, blockLocation, maximumDistanceFromPoint, overridableWanderPriority.priority, maxDurationTicks
            ));
      } else {
         Bukkit.getLogger()
            .info(
               "[EasyMinecraftPathfinding] Entity type "
                  + livingEntity.getType()
                  + " does not extend Mob and is therefore unable to have goals! It will not be able to pathfind."
            );
         return null;
      }
   }

   @Override
   public void setBlockInNativeDataPalette(World world, int x, int y, int z, BlockData blockData, boolean applyPhysics) {
      MassEditBlocks.setBlockInNativeDataPalette(world, x, y, z, blockData, applyPhysics);
   }

   @Override
   public AbstractPacketBundle createPacketBundle() {
      return new PacketBundle();
   }

   @Override
   public boolean hideScoreboardNumbers(Objective objective) {
      if (objective != null && objective.getScoreboard() != null) {
         try {
            Method getHandle = objective.getScoreboard().getClass().getMethod("getHandle");
            Scoreboard nmsScoreboard = (Scoreboard)getHandle.invoke(objective.getScoreboard());
            ScoreboardObjective nmsObjective = nmsScoreboard.a(objective.getName());
            if (nmsObjective == null) {
               return false;
            } else {
               nmsObjective.b(BlankFormat.a);
               return true;
            }
         } catch (RuntimeException | ReflectiveOperationException var5) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public PacketEntityInterface createPacketEntity(EntityType entityType, Location location) {
      return new PacketGenericEntity(entityType, location);
   }

   @Override
   public FakeText createFakeText(Location location, FakeTextSettings settings) {
      return new FakeTextImpl(location, settings);
   }

   @Override
   public boolean supportsFakeItems() {
      return true;
   }

   @Override
   public FakeItem createFakeItem(Location location, FakeItemSettings settings) {
      return new FakeItemImpl(location, settings);
   }

   @Override
   public PacketInteractionEntity createPacketInteractionEntity(Location location, float width, float height) {
      return new com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.packets.PacketInteractionEntity(location, width, height);
   }

   @Override
   public void initializePacketInteractionListener(Plugin plugin) {
      this.packetInteractionListener = new PacketInteractionListener(plugin);
      this.packetInteractionListener.initialize();
   }

   @Override
   public void shutdownPacketInteractionListener() {
      if (this.packetInteractionListener != null) {
         this.packetInteractionListener.shutdown();
         this.packetInteractionListener = null;
      }

      super.shutdownPacketInteractionListener();
   }
}
