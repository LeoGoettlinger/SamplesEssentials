package com.magmaguy.elitemobs.easyminecraftgoals.v26;

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
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketSizeEstimator;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketTextEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.entitydata.BodyRotation;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.flee.TransientFleeGoal;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.hitbox.Hitbox;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.massblockedit.MassEditBlocks;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.mind.NativeMindHost;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.move.Move;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.packets.FakeItemImpl;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.packets.FakeTextImpl;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.packets.NmsPacketSizeFunction;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.packets.PacketArmorStandEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.packets.PacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.packets.PacketDisplayEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.packets.PacketGenericEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.packets.PacketInteractionListener;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.passenger.PassengerOffset;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.pathfinding.NativePathfindingGoal;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.wanderbacktopoint.WanderBackToPointBehavior;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.wanderbacktopoint.WanderBackToPointGoal;
import com.magmaguy.elitemobs.magmacore.ai.MindActionSink;
import com.magmaguy.elitemobs.magmacore.ai.MindFailureListener;
import com.magmaguy.elitemobs.magmacore.ai.MindHost;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.component.Consumable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;

public class NMSAdapter extends com.magmaguy.elitemobs.easyminecraftgoals.NMSAdapter {
   private PacketInteractionListener packetInteractionListener;
   private NativeMindHost mindHost;

   @Override
   public RangedAmmunition grantOrdinaryAmmunition(Plugin plugin, Predicate<Player> eligible) {
      return new NativeRangedAmmunition(plugin, eligible).start();
   }

   @Override
   public float getConsumptionSeconds(ItemStack item) {
      net.minecraft.world.item.ItemStack nativeItem = CraftBukkitBridge.asNMSCopy(item);
      Consumable consumable = (Consumable)nativeItem.get(DataComponents.CONSUMABLE);
      return consumable == null ? -1.0F : consumable.consumeSeconds();
   }

   @Override
   public ItemStack withConsumptionSeconds(ItemStack item, float seconds) {
      if (Float.isFinite(seconds) && !(seconds < 0.0F)) {
         net.minecraft.world.item.ItemStack nativeItem = CraftBukkitBridge.asNMSCopy(item);
         Consumable original = (Consumable)nativeItem.get(DataComponents.CONSUMABLE);
         if (original == null) {
            throw new IllegalArgumentException("Item is not consumable");
         } else {
            nativeItem.set(
               DataComponents.CONSUMABLE,
               new Consumable(seconds, original.animation(), original.sound(), original.hasConsumeParticles(), original.onConsumeEffects())
            );
            return CraftBukkitBridge.asBukkitCopy(nativeItem);
         }
      } else {
         throw new IllegalArgumentException("Consumption duration must be finite and non-negative");
      }
   }

   @Override
   public void damageWithoutCooldown(LivingEntity target, double amount, Entity source) {
      if (Double.isFinite(amount) && !(amount < 0.0) && !(amount > Float.MAX_VALUE)) {
         net.minecraft.world.entity.LivingEntity victim = CraftBukkitBridge.getNMSLivingEntity(target);
         if (victim.generation) {
            throw new IllegalStateException("Cannot damage an entity during world generation");
         } else {
            net.minecraft.world.entity.Entity attacker = CraftBukkitBridge.getNMSEntity(source);
            DamageSources sources = victim.damageSources();
            DamageSource original;
            if (attacker instanceof AbstractArrow arrow) {
               original = sources.arrow(arrow, arrow.getOwner());
            } else if (attacker instanceof net.minecraft.world.entity.player.Player player) {
               original = sources.playerAttack(player);
            } else {
               if (!(attacker instanceof net.minecraft.world.entity.LivingEntity living)) {
                  throw new IllegalArgumentException("Damage source must be a living attacker or arrow");
               }

               original = sources.mobAttack(living);
            }

            var damage = new DamageSource(original.typeHolder(), original.getDirectEntity(), original.getEntity(), original.sourcePositionRaw()) {
               public boolean is(TagKey<DamageType> tag) {
                  return tag.equals(DamageTypeTags.BYPASSES_COOLDOWN) || super.is(tag);
               }
            };
            victim.hurtServer((ServerLevel)victim.level(), damage, (float)amount);
         }
      } else {
         throw new IllegalArgumentException("Damage must be finite and fit a native float");
      }
   }

   @Override
   public int getDamageCooldownTicks(LivingEntity entity) {
      DamageCooldownAccess access = DamageCooldownAccess.current();
      return access == null ? super.getDamageCooldownTicks(entity) : access.get(CraftBukkitBridge.getNMSLivingEntity(entity));
   }

   @Override
   public void setDamageCooldownTicks(LivingEntity entity, int ticks) {
      DamageCooldownAccess access = DamageCooldownAccess.current();
      if (access == null) {
         super.setDamageCooldownTicks(entity, ticks);
      } else {
         access.set(CraftBukkitBridge.getNMSLivingEntity(entity), ticks);
      }
   }

   public NMSAdapter() {
      PacketSizeEstimator.setImpl(new NmsPacketSizeFunction());
   }

   @Override
   public MindHost createMindHost(NamespacedKey hostIdentity, MindFailureListener failureListener, MindActionSink actionSink) {
      if (this.mindHost != null) {
         throw new IllegalStateException("This adapter already has a native mind host");
      } else {
         this.mindHost = new NativeMindHost(hostIdentity, failureListener, actionSink);
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

   private PathfinderMob getPathfinderMob(Entity entity) {
      return CraftBukkitBridge.getNMSEntity(entity) instanceof PathfinderMob pathfinderMob ? pathfinderMob : null;
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
      PathfinderMob pathfinderMob = this.getPathfinderMob(livingEntity);
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
      net.minecraft.world.entity.LivingEntity nmsLivingEntity = CraftBukkitBridge.getNMSLivingEntity(livingEntity);
      PathfinderMob pathfinderMob;
      if (nmsLivingEntity instanceof PathfinderMob pm) {
         pathfinderMob = pm;
      } else {
         pathfinderMob = null;
      }

      if (!(nmsLivingEntity instanceof Mob)) {
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
   protected Optional<PathfindingHandle> createPathfindingHandle(LivingEntity livingEntity, int priority) {
      if (this.isMindBody(livingEntity)) {
         return this.mindHost == null ? Optional.empty() : this.mindHost.openPathfinding(livingEntity);
      } else {
         PathfinderMob pathfinderMob = this.getPathfinderMob(livingEntity);
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
      PathfinderMob pathfinderMob = this.getPathfinderMob(livingEntity);
      if (pathfinderMob == null) {
         return false;
      } else {
         pathfinderMob.removeFreeWill();
         return true;
      }
   }

   @Override
   public boolean isPositionEntityTicking(Location location) {
      return location.getWorld() == null
         ? false
         : CraftBukkitBridge.getServerLevel(location).isPositionEntityTicking(BlockPos.containing(location.getX(), location.getY(), location.getZ()));
   }

   @Override
   public Optional<TransientMovementOverride> beginFlee(LivingEntity livingEntity, Location threatLocation, double speedModifier) {
      Objects.requireNonNull(livingEntity, "livingEntity");
      Objects.requireNonNull(threatLocation, "threatLocation");
      if (Double.isFinite(speedModifier) && !(speedModifier <= 0.0)) {
         if (threatLocation.getWorld() == null || threatLocation.getWorld() != livingEntity.getWorld()) {
            return Optional.empty();
         } else if (NativeMindHost.isMindCarrier(livingEntity)) {
            return this.mindHost == null ? Optional.empty() : this.mindHost.beginFlee(livingEntity, threatLocation, speedModifier);
         } else {
            PathfinderMob pathfinderMob = this.getPathfinderMob(livingEntity);
            if (pathfinderMob != null && !pathfinderMob.isNoAi()) {
               TransientFleeGoal goal = new TransientFleeGoal(pathfinderMob, livingEntity.getWorld(), threatLocation, speedModifier);
               if (!goal.canUse()) {
                  goal.close();
                  return Optional.empty();
               } else {
                  goal.register();
                  return Optional.of(goal);
               }
            } else {
               return Optional.empty();
            }
         }
      } else {
         return Optional.empty();
      }
   }

   @Override
   public void doNotMove(LivingEntity livingEntity) {
      PathfinderMob pathfinderMob = this.getPathfinderMob(livingEntity);
      if (pathfinderMob != null) {
         Move.doNotMove(pathfinderMob);
      }
   }

   @Override
   public boolean forcedMove(LivingEntity livingEntity, double speedModifier, Location location) {
      if (CraftBukkitBridge.getNMSLivingEntity(livingEntity) instanceof Mob mob) {
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
      if (CraftBukkitBridge.getNMSLivingEntity(livingEntity) instanceof Mob mob) {
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
      net.minecraft.world.entity.LivingEntity nmsLivingEntity = CraftBukkitBridge.getNMSLivingEntity(livingEntity);
      PathfinderMob pathfinderMob;
      if (nmsLivingEntity instanceof PathfinderMob pm) {
         pathfinderMob = pm;
      } else {
         pathfinderMob = null;
      }

      if (nmsLivingEntity instanceof Mob mob) {
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
      if (objective != null && objective.getScoreboard() instanceof CraftScoreboard craftScoreboard) {
         net.minecraft.world.scores.Objective nmsObjective = craftScoreboard.getHandle().getObjective(objective.getName());
         if (nmsObjective == null) {
            return false;
         } else {
            nmsObjective.setNumberFormat(BlankFormat.INSTANCE);
            return true;
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
      return new com.magmaguy.elitemobs.easyminecraftgoals.v26.packets.PacketInteractionEntity(location, width, height);
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
