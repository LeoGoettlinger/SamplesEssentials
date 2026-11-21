package com.magmaguy.elitemobs.easyminecraftgoals.v26;

import com.magmaguy.elitemobs.easyminecraftgoals.ammunition.RangedAmmunition;
import io.netty.channel.Channel;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.CrossbowItem.ChargingSounds;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

public final class NativeRangedAmmunition extends RangedAmmunition {
   private static final EntityDataAccessor<Byte> USE_FLAGS = useFlags();
   private static final Field CONNECTION = nativeField(ServerCommonPacketListenerImpl.class, Connection.class);
   private static final Field CHANNEL = nativeField(Connection.class, Channel.class);
   private static final MethodHandle DRAW = nativeMethod(
      ProjectileWeaponItem.class, List.class, true, new Class[]{ItemStack.class, ItemStack.class, LivingEntity.class}
   );
   private static final MethodHandle SHOOT = nativeShootMethod(
      ProjectileWeaponItem.class,
      new Class[]{
         ServerLevel.class, LivingEntity.class, InteractionHand.class, ItemStack.class, List.class, float.class, float.class, boolean.class, LivingEntity.class
      }
   );
   private static final MethodHandle CHARGING_SOUNDS = nativeMethod(CrossbowItem.class, ChargingSounds.class, new Class[]{ItemStack.class});

   public NativeRangedAmmunition(Plugin plugin, Predicate<Player> eligible) {
      super(plugin, eligible);
   }

   private static ServerPlayer handle(Player player) {
      return CraftBukkitBridge.getServerPlayer(player);
   }

   private static ItemStack nativeCopy(org.bukkit.inventory.ItemStack item) {
      return CraftBukkitBridge.asNMSCopy(item);
   }

   private static org.bukkit.inventory.ItemStack bukkitCopy(ItemStack item) {
      return CraftBukkitBridge.asBukkitCopy(item);
   }

   @Override
   protected Channel channel(Player player) {
      try {
         return (Channel)CHANNEL.get(CONNECTION.get(handle(player).connection));
      } catch (IllegalAccessException var3) {
         throw new IllegalStateException("Cannot access item-use channel", var3);
      }
   }

   @Override
   protected boolean isReleasePacket(Object packet) {
      return packet instanceof ServerboundPlayerActionPacket action && action.getAction() == Action.RELEASE_USE_ITEM;
   }

   @Override
   protected Object orderedRelease(final Player player, Object packet, final BooleanSupplier release) {
      final ServerboundPlayerActionPacket original = (ServerboundPlayerActionPacket)packet;
      return new Packet<ServerGamePacketListener>() {
         public PacketType<? extends Packet<ServerGamePacketListener>> type() {
            return original.type();
         }

         public void handle(ServerGamePacketListener listener) {
            PacketUtils.ensureRunningOnSameThread(this, listener, NativeRangedAmmunition.handle(player).level());
            if (!release.getAsBoolean()) {
               original.handle(listener);
            }
         }
      };
   }

   @Override
   protected RangedAmmunition.Use begin(Player player, EquipmentSlot slot) {
      ServerPlayer entity = handle(player);
      if (entity.isUsingItem()) {
         return null;
      } else {
         InteractionHand hand = slot == EquipmentSlot.OFF_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
         ItemStack weapon = entity.getItemInHand(hand);
         if (entity.getCooldowns().isOnCooldown(weapon)) {
            return null;
         } else if (!(weapon.getItem() instanceof BowItem) && !(weapon.getItem() instanceof CrossbowItem)) {
            return null;
         } else if (weapon.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(weapon)) {
            return null;
         } else {
            ItemStack selected = entity.getProjectile(weapon);
            if (!selected.isEmpty() && !ordinary(bukkitCopy(selected))) {
               return null;
            } else {
               NativeRangedAmmunition.NativeUse use = new NativeRangedAmmunition.NativeUse(entity, hand, weapon);

               try {
                  entity.startUsingItem(hand);
                  syncUse(entity);
                  return use;
               } catch (RuntimeException var9) {
                  use.cancel();
                  throw var9;
               }
            }
         }
      }
   }

   private static EntityDataAccessor<Byte> useFlags() {
      Field found = null;

      for (Field field : LivingEntity.class.getDeclaredFields()) {
         if (Modifier.isStatic(field.getModifiers())
            && field.getType() == EntityDataAccessor.class
            && field.getGenericType() instanceof ParameterizedType type
            && type.getActualTypeArguments()[0] == Byte.class) {
            if (found != null) {
               throw new IllegalStateException("Ambiguous living item-use flags");
            }

            found = field;
         }
      }

      if (found == null) {
         throw new IllegalStateException("Missing living item-use flags");
      } else {
         try {
            found.setAccessible(true);
            return (EntityDataAccessor<Byte>)found.get(null);
         } catch (IllegalAccessException var7) {
            throw new IllegalStateException(var7);
         }
      }
   }

   private static void syncUse(ServerPlayer player) {
      player.connection
         .send(new ClientboundSetEntityDataPacket(player.getId(), List.of(DataValue.create(USE_FLAGS, (Byte)player.getEntityData().get(USE_FLAGS)))));
   }

   private static final class NativeUse implements RangedAmmunition.Use {
      private final ServerPlayer player;
      private final InteractionHand hand;
      private final ItemStack weapon;

      private NativeUse(ServerPlayer player, InteractionHand hand, ItemStack weapon) {
         this.player = player;
         this.hand = hand;
         this.weapon = weapon;
      }

      @Override
      public boolean valid() {
         return !this.player.isImmobile()
            && this.player.isUsingItem()
            && this.player.getUseItem() == this.weapon
            && this.player.getItemInHand(this.hand) == this.weapon;
      }

      @Override
      public void tick() {
         if (this.weapon.getItem() instanceof CrossbowItem
            && !CrossbowItem.isCharged(this.weapon)
            && this.player.getTicksUsingItem() >= CrossbowItem.getChargeDuration(this.weapon, this.player)) {
            List<ItemStack> arrows = this.draw();
            if (!arrows.isEmpty()) {
               this.weapon.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(arrows));

               try {
                  ChargingSounds sounds = (ChargingSounds)(
                     NativeRangedAmmunition.CHARGING_SOUNDS.type().parameterCount() == 1
                        ? (Object)NativeRangedAmmunition.CHARGING_SOUNDS.invoke((ItemStack)this.weapon)
                        : (Object)NativeRangedAmmunition.CHARGING_SOUNDS.invoke((Item)this.weapon.getItem(), (ItemStack)this.weapon)
                  );
                  sounds.end()
                     .ifPresent(
                        sound -> this.player
                           .level()
                           .playSound(
                              null, this.player.getX(), this.player.getY(), this.player.getZ(), (SoundEvent)sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F
                           )
                     );
               } catch (Throwable var3) {
                  throw new IllegalStateException("Native crossbow loading failed", var3);
               }

               this.player.getBukkitEntity().updateInventory();
            }
         }
      }

      private List<ItemStack> draw() {
         try {
            List<ItemStack> arrows = (List)NativeRangedAmmunition.DRAW
               .invoke((ItemStack)this.weapon, (ItemStack)NativeRangedAmmunition.nativeCopy(NativeRangedAmmunition.grantedArrow()), (ServerPlayer)this.player);

            for (ItemStack arrow : arrows) {
               arrow.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
            }

            return arrows;
         } catch (Throwable var4) {
            throw new IllegalStateException("Native ammunition draw failed", var4);
         }
      }

      @Override
      public void release() {
         if (this.weapon.getItem() instanceof CrossbowItem) {
            this.tick();
         } else {
            float power = BowItem.getPowerForTime(this.player.getTicksUsingItem());
            if (!(power < 0.1F)) {
               List<ItemStack> arrows = this.draw();
               if (!arrows.isEmpty()) {
                  try {
                     ProjectileWeaponItem item = (ProjectileWeaponItem)this.weapon.getItem();
                     NativeRangedAmmunition.SHOOT
                        .invoke(
                           (ProjectileWeaponItem)item,
                           (ServerLevel)this.player.level(),
                           (ServerPlayer)this.player,
                           (InteractionHand)this.hand,
                           (ItemStack)this.weapon,
                           (List)arrows,
                           (float)(power * 3.0F),
                           (float)1.0F,
                           (boolean)(power == 1.0F),
                           (Void)null,
                           (float)power
                        );
                     this.player
                        .level()
                        .playSound(
                           null,
                           this.player.getX(),
                           this.player.getY(),
                           this.player.getZ(),
                           SoundEvents.ARROW_SHOOT,
                           SoundSource.PLAYERS,
                           1.0F,
                           1.0F / (this.player.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F
                        );
                     this.player.awardStat(Stats.ITEM_USED.get(item));
                  } catch (Throwable var4) {
                     throw new IllegalStateException("Native bow release failed", var4);
                  }
               }
            }
         }
      }

      @Override
      public void cancel() {
         if (this.player.getUseItem() == this.weapon) {
            this.player.stopUsingItem();
            NativeRangedAmmunition.syncUse(this.player);
         }
      }
   }
}
