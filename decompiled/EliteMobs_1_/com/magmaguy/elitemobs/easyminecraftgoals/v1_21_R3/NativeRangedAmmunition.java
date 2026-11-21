package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R3;

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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.PlayerConnectionUtils;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig.EnumPlayerDigType;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcher.c;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.Unit;
import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBow;
import net.minecraft.world.item.ItemCrossbow;
import net.minecraft.world.item.ItemProjectileWeapon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemCrossbow.b;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;

public final class NativeRangedAmmunition extends RangedAmmunition {
   private static final DataWatcherObject<Byte> USE_FLAGS = useFlags();
   private static final Field CONNECTION = nativeField(ServerCommonPacketListenerImpl.class, NetworkManager.class);
   private static final Field CHANNEL = nativeField(NetworkManager.class, Channel.class);
   private static final MethodHandle DRAW = nativeMethod(
      ItemProjectileWeapon.class, List.class, true, new Class[]{ItemStack.class, ItemStack.class, EntityLiving.class}
   );
   private static final MethodHandle SHOOT = nativeShootMethod(
      ItemProjectileWeapon.class,
      new Class[]{
         WorldServer.class, EntityLiving.class, EnumHand.class, ItemStack.class, List.class, float.class, float.class, boolean.class, EntityLiving.class
      }
   );
   private static final MethodHandle CHARGING_SOUNDS = nativeMethod(ItemCrossbow.class, b.class, false, new Class[]{ItemStack.class});

   public NativeRangedAmmunition(Plugin plugin, Predicate<Player> eligible) {
      super(plugin, eligible);
   }

   private static EntityPlayer handle(Player player) {
      return ((CraftPlayer)player).getHandle();
   }

   private static ItemStack nativeCopy(org.bukkit.inventory.ItemStack item) {
      return CraftItemStack.asNMSCopy(item);
   }

   private static org.bukkit.inventory.ItemStack bukkitCopy(ItemStack item) {
      return CraftItemStack.asBukkitCopy(item);
   }

   @Override
   protected Channel channel(Player player) {
      try {
         return (Channel)CHANNEL.get(CONNECTION.get(handle(player).f));
      } catch (IllegalAccessException var3) {
         throw new IllegalStateException("Cannot access item-use channel", var3);
      }
   }

   @Override
   protected boolean isReleasePacket(Object packet) {
      return packet instanceof PacketPlayInBlockDig action && action.f() == EnumPlayerDigType.f;
   }

   @Override
   protected Object orderedRelease(final Player player, Object packet, final BooleanSupplier release) {
      final PacketPlayInBlockDig original = (PacketPlayInBlockDig)packet;
      return new Packet<PacketListenerPlayIn>() {
         public PacketType<? extends Packet<PacketListenerPlayIn>> a() {
            return original.a();
         }

         public void handle(PacketListenerPlayIn listener) {
            PlayerConnectionUtils.a(this, listener, (WorldServer)NativeRangedAmmunition.handle(player).dV());
            if (!release.getAsBoolean()) {
               original.a(listener);
            }
         }
      };
   }

   @Override
   protected RangedAmmunition.Use begin(Player player, EquipmentSlot slot) {
      EntityPlayer entity = handle(player);
      if (entity.fz()) {
         return null;
      } else {
         EnumHand hand = slot == EquipmentSlot.OFF_HAND ? EnumHand.b : EnumHand.a;
         ItemStack weapon = entity.b(hand);
         if (entity.gE().a(weapon)) {
            return null;
         } else if (!(weapon.h() instanceof ItemBow) && !(weapon.h() instanceof ItemCrossbow)) {
            return null;
         } else if (weapon.h() instanceof ItemCrossbow && ItemCrossbow.g(weapon)) {
            return null;
         } else {
            ItemStack selected = entity.d(weapon);
            if (!selected.f() && !ordinary(bukkitCopy(selected))) {
               return null;
            } else {
               NativeRangedAmmunition.NativeUse use = new NativeRangedAmmunition.NativeUse(entity, hand, weapon);

               try {
                  entity.c(hand);
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

   private static DataWatcherObject<Byte> useFlags() {
      Field found = null;

      for (Field field : EntityLiving.class.getDeclaredFields()) {
         if (Modifier.isStatic(field.getModifiers())
            && field.getType() == DataWatcherObject.class
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
            return (DataWatcherObject<Byte>)found.get(null);
         } catch (IllegalAccessException var7) {
            throw new IllegalStateException(var7);
         }
      }
   }

   private static void syncUse(EntityPlayer player) {
      player.f.b(new PacketPlayOutEntityMetadata(player.ar(), List.of(c.a(USE_FLAGS, (Byte)player.au().a(USE_FLAGS)))));
   }

   private static final class NativeUse implements RangedAmmunition.Use {
      private final EntityPlayer player;
      private final EnumHand hand;
      private final ItemStack weapon;

      private NativeUse(EntityPlayer player, EnumHand hand, ItemStack weapon) {
         this.player = player;
         this.hand = hand;
         this.weapon = weapon;
      }

      @Override
      public boolean valid() {
         return !this.player.fi() && this.player.fz() && this.player.fB() == this.weapon && this.player.b(this.hand) == this.weapon;
      }

      @Override
      public void tick() {
      }

      private void loadCrossbow() {
         if (this.weapon.h() instanceof ItemCrossbow && !ItemCrossbow.g(this.weapon) && this.player.fD() >= ItemCrossbow.b(this.weapon, this.player)) {
            List<ItemStack> arrows = this.draw();
            if (!arrows.isEmpty()) {
               this.weapon.b(DataComponents.O, ChargedProjectiles.a(arrows));

               try {
                  b sounds = (b)NativeRangedAmmunition.CHARGING_SOUNDS.invoke((Item)this.weapon.h(), (ItemStack)this.weapon);
                  sounds.c()
                     .ifPresent(
                        sound -> this.player
                           .dV()
                           .a(null, this.player.dA(), this.player.dC(), this.player.dG(), (SoundEffect)sound.a(), SoundCategory.h, 1.0F, 1.0F)
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
               .invoke((ItemStack)this.weapon, (ItemStack)NativeRangedAmmunition.nativeCopy(NativeRangedAmmunition.grantedArrow()), (EntityPlayer)this.player);

            for (ItemStack arrow : arrows) {
               arrow.b(DataComponents.v, Unit.a);
            }

            return arrows;
         } catch (Throwable var4) {
            throw new IllegalStateException("Native ammunition draw failed", var4);
         }
      }

      @Override
      public void release() {
         if (this.weapon.h() instanceof ItemCrossbow) {
            this.loadCrossbow();
         } else {
            float power = ItemBow.a(this.player.fD());
            if (!(power < 0.1F)) {
               List<ItemStack> arrows = this.draw();
               if (!arrows.isEmpty()) {
                  try {
                     ItemProjectileWeapon item = (ItemProjectileWeapon)this.weapon.h();
                     NativeRangedAmmunition.SHOOT
                        .invoke(
                           (ItemProjectileWeapon)item,
                           (WorldServer)((WorldServer)this.player.dV()),
                           (EntityPlayer)this.player,
                           (EnumHand)this.hand,
                           (ItemStack)this.weapon,
                           (List)arrows,
                           (float)(power * 3.0F),
                           (float)1.0F,
                           (boolean)(power == 1.0F),
                           (Void)null,
                           (float)power
                        );
                     this.player
                        .dV()
                        .a(
                           null,
                           this.player.dA(),
                           this.player.dC(),
                           this.player.dG(),
                           SoundEffects.aG,
                           SoundCategory.h,
                           1.0F,
                           1.0F / (this.player.dY().i() * 0.4F + 1.2F) + power * 0.5F
                        );
                     this.player.b(StatisticList.c.b(item));
                  } catch (Throwable var4) {
                     throw new IllegalStateException("Native bow release failed", var4);
                  }
               }
            }
         }
      }

      @Override
      public void cancel() {
         if (this.player.fB() == this.weapon) {
            this.player.fF();
            NativeRangedAmmunition.syncUse(this.player);
         }
      }
   }
}
