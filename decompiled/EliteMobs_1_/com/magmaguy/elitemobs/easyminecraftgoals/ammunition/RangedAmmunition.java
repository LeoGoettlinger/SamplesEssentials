package com.magmaguy.elitemobs.easyminecraftgoals.ammunition;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class RangedAmmunition implements Listener, AutoCloseable {
   private static final NamespacedKey FREE_AMMUNITION = new NamespacedKey("magmacore", "granted_arrow");
   private final Plugin plugin;
   private final Predicate<Player> eligible;
   private final String handlerName;
   private final Map<UUID, RangedAmmunition.Session> sessions = new ConcurrentHashMap<>();
   private final Map<UUID, Channel> channels = new HashMap<>();
   private BukkitTask ticker;
   private volatile boolean closed;

   protected RangedAmmunition(Plugin plugin, Predicate<Player> eligible) {
      this.plugin = plugin;
      this.eligible = eligible;
      this.handlerName = "magmacore_ammunition_" + plugin.getName();
   }

   public final RangedAmmunition start() {
      if (this.ticker == null && !this.closed) {
         try {
            for (Player player : Bukkit.getOnlinePlayers()) {
               this.inject(player);
            }

            Bukkit.getPluginManager().registerEvents(this, this.plugin);
            this.ticker = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 1L, 1L);
            return this;
         } catch (RuntimeException var3) {
            this.close();
            throw var3;
         }
      } else {
         throw new IllegalStateException("Ammunition service already started or closed");
      }
   }

   protected abstract Channel channel(Player var1);

   protected abstract boolean isReleasePacket(Object var1);

   protected abstract Object orderedRelease(Player var1, Object var2, BooleanSupplier var3);

   protected abstract RangedAmmunition.Use begin(Player var1, EquipmentSlot var2);

   private boolean eligible(Player player) {
      return player.isOnline()
         && !player.isDead()
         && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
         && this.eligible.test(player);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public final void onUse(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Player player = event.getPlayer();
         if (!this.closed
            && this.channels.containsKey(player.getUniqueId())
            && this.eligible(player)
            && event.useItemInHand() != Result.DENY
            && event.getHand() != null) {
            if (player.getOpenInventory().getType() == InventoryType.CRAFTING) {
               ItemStack item = event.getItem();
               if (item != null && (item.getType() == Material.BOW || item.getType() == Material.CROSSBOW)) {
                  if (event.getClickedBlock() == null
                     || !event.getClickedBlock().getType().isInteractable()
                     || player.isSneaking()
                     || event.useInteractedBlock() == Result.DENY) {
                     if (!this.sessions.containsKey(player.getUniqueId())) {
                        RangedAmmunition.Use use = this.begin(player, event.getHand());
                        if (use != null) {
                           this.sessions.put(player.getUniqueId(), new RangedAmmunition.Session(player, player.getWorld().getUID(), use));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void tick() {
      for (RangedAmmunition.Session session : this.sessions.values()) {
         if (this.eligible(session.player()) && session.world().equals(session.player().getWorld().getUID()) && session.use().valid()) {
            try {
               session.use().tick();
            } catch (RuntimeException var4) {
               this.cancel(session.player());
               this.plugin.getLogger().severe("Native ammunition update failed: " + var4);
            }
         } else {
            this.cancel(session.player());
         }
      }
   }

   private void release(RangedAmmunition.Session expected) {
      if (this.sessions.remove(expected.player().getUniqueId(), expected)) {
         try {
            if (this.eligible(expected.player()) && expected.world().equals(expected.player().getWorld().getUID()) && expected.use().valid()) {
               expected.use().release();
            }
         } finally {
            expected.use().cancel();
         }
      }
   }

   private void cancel(Player player) {
      RangedAmmunition.Session session = this.sessions.remove(player.getUniqueId());
      if (session != null) {
         session.use().cancel();
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public final void validateGrantedShot(EntityShootBowEvent event) {
      if (isGranted(event.getConsumable()) && !(event.getEntity() instanceof Player player && this.eligible(player))) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public final void preventPickup(EntityShootBowEvent event) {
      if (isGranted(event.getConsumable()) && event.getProjectile() instanceof AbstractArrow arrow) {
         arrow.setPickupStatus(PickupStatus.DISALLOWED);
      }
   }

   protected static ItemStack grantedArrow() {
      ItemStack arrow = new ItemStack(Material.ARROW, 64);
      ItemMeta meta = arrow.getItemMeta();
      meta.getPersistentDataContainer().set(FREE_AMMUNITION, PersistentDataType.BYTE, (byte)1);
      arrow.setItemMeta(meta);
      return arrow;
   }

   protected static boolean ordinary(ItemStack arrow) {
      return arrow.getType() == Material.ARROW && !arrow.hasItemMeta();
   }

   private static boolean isGranted(ItemStack item) {
      return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(FREE_AMMUNITION, PersistentDataType.BYTE);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public final void onJoin(PlayerJoinEvent event) {
      try {
         this.inject(event.getPlayer());
      } catch (RuntimeException var3) {
         this.plugin.getLogger().severe("Cannot enable native ammunition for " + event.getPlayer().getUniqueId() + ": " + var3);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public final void onQuit(PlayerQuitEvent event) {
      this.cancel(event.getPlayer());
      this.uninject(event.getPlayer().getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public final void onWorld(PlayerChangedWorldEvent event) {
      this.cancel(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public final void onDeath(PlayerDeathEvent event) {
      this.cancel(event.getEntity());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public final void onSlot(PlayerItemHeldEvent event) {
      this.cancel(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public final void onSwap(PlayerSwapHandItemsEvent event) {
      this.cancel(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public final void onDrop(PlayerDropItemEvent event) {
      this.cancel(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public final void onTeleport(PlayerTeleportEvent event) {
      this.cancel(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public final void onInventory(InventoryOpenEvent event) {
      if (event.getPlayer() instanceof Player player) {
         this.cancel(player);
      }
   }

   private void inject(final Player player) {
      Channel channel = this.channel(player);
      if (channel == null) {
         throw new IllegalStateException("No item-use channel for " + player.getUniqueId());
      } else if (channel.pipeline().get(this.handlerName) != null) {
         throw new IllegalStateException("Duplicate ammunition owner: " + this.handlerName);
      } else {
         final UUID playerId = player.getUniqueId();
         channel.pipeline().addBefore("packet_handler", this.handlerName, new ChannelDuplexHandler() {
            public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
               if (!RangedAmmunition.this.closed && RangedAmmunition.this.isReleasePacket(packet)) {
                  packet = RangedAmmunition.this.orderedRelease(player, packet, () -> {
                     RangedAmmunition.Session session = RangedAmmunition.this.sessions.get(playerId);
                     if (session == null) {
                        return false;
                     } else {
                        RangedAmmunition.this.release(session);
                        return true;
                     }
                  });
               }

               super.channelRead(context, packet);
            }
         });
         this.channels.put(playerId, channel);
      }
   }

   private void uninject(UUID player) {
      Channel channel = this.channels.remove(player);
      if (channel != null && channel.pipeline().get(this.handlerName) != null) {
         channel.pipeline().remove(this.handlerName);
      }
   }

   @Override
   public final void close() {
      this.closed = true;
      if (this.ticker != null) {
         this.ticker.cancel();
         this.ticker = null;
      }

      HandlerList.unregisterAll(this);

      for (RangedAmmunition.Session session : this.sessions.values()) {
         session.use().cancel();
      }

      this.sessions.clear();

      for (UUID player : this.channels.keySet().toArray(UUID[]::new)) {
         this.uninject(player);
      }
   }

   protected static MethodHandle nativeMethod(Class<?> owner, Class<?> result, boolean isStatic, Class<?>... arguments) {
      MethodHandle method = findNativeMethod(owner, result, isStatic, arguments);
      if (method == null) {
         throw new IllegalStateException("Missing native ammunition method on " + owner + " with parameters " + Arrays.toString((Object[])arguments));
      } else {
         return method;
      }
   }

   protected static MethodHandle nativeMethod(Class<?> owner, Class<?> result, Class<?>... arguments) {
      MethodHandle method = findNativeMethod(owner, result, true, arguments);
      return method != null ? method : nativeMethod(owner, result, false, arguments);
   }

   protected static MethodHandle nativeShootMethod(Class<?> owner, Class<?>... arguments) {
      Class<?>[] withForce = Arrays.copyOf(arguments, arguments.length + 1);
      withForce[arguments.length] = float.class;
      MethodHandle method = findNativeMethod(owner, void.class, false, withForce);
      if (method != null) {
         return method;
      } else {
         method = nativeMethod(owner, void.class, false, arguments);
         return MethodHandles.dropArguments(method, method.type().parameterCount(), float.class);
      }
   }

   private static MethodHandle findNativeMethod(Class<?> owner, Class<?> result, boolean isStatic, Class<?>... arguments) {
      Method found = null;

      for (Method method : owner.getDeclaredMethods()) {
         if (method.getReturnType() == result
            && Modifier.isStatic(method.getModifiers()) == isStatic
            && Arrays.equals((Object[])method.getParameterTypes(), (Object[])arguments)) {
            if (found != null) {
               throw new IllegalStateException("Ambiguous native ammunition method on " + owner);
            }

            found = method;
         }
      }

      if (found == null) {
         return null;
      } else {
         try {
            found.setAccessible(true);
            return MethodHandles.lookup().unreflect(found);
         } catch (IllegalAccessException var9) {
            throw new IllegalStateException(var9);
         }
      }
   }

   protected static Field nativeField(Class<?> owner, Class<?> type) {
      Field found = null;

      for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
         for (Field field : current.getDeclaredFields()) {
            if (field.getType() == type && !Modifier.isStatic(field.getModifiers())) {
               if (found != null) {
                  throw new IllegalStateException("Ambiguous native connection field on " + owner);
               }

               found = field;
            }
         }
      }

      if (found == null) {
         throw new IllegalStateException("Missing native connection field on " + owner);
      } else {
         found.setAccessible(true);
         return found;
      }
   }

   private record Session(Player player, UUID world, RangedAmmunition.Use use) {
   }

   protected interface Use {
      boolean valid();

      void tick();

      void release();

      void cancel();
   }
}
