package dev.demora.demoralegendaryweapons.weapons.poseidontrident;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

public class PoseidonTridentListener implements Listener {
   private static final float RIPTIDE_STRENGTH = 8.0F;
   private static final int RIPTIDE_SPIN_TICKS = 20;
   private final DemoraLegendaryWeapons plugin;
   private final PoseidonTrident poseidonTrident;
   private final Map<UUID, ItemDisplay> thrownTridents;
   private Method getHandleMethod;
   private Method asNmsCopyMethod;
   private Method startAutoSpinAttackMethod;
   private boolean nmsUnavailableWarned = false;

   public PoseidonTridentListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.poseidonTrident = new PoseidonTrident(plugin);
      this.thrownTridents = new HashMap<>();
      this.startDolphinGraceTask();
   }

   public PoseidonTrident getPoseidonTrident() {
      return this.poseidonTrident;
   }

   @EventHandler
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         Player attacker = (Player)event.getDamager();
         ItemStack weapon = attacker.getInventory().getItemInMainHand();
         if (this.poseidonTrident.isPoseidonTrident(weapon)) {
            if (event.getEntity() instanceof LivingEntity) {
               event.setDamage(this.poseidonTrident.getMeleeDamage());
            }
         }
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         Player player = event.getPlayer();
         ItemStack item = event.getItem();
         if (this.poseidonTrident.isPoseidonTrident(item)) {
            EquipmentSlot hand = event.getHand();
            if (hand != null) {
               player.startUsingItem(hand);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEntityShootBow(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player) {
         if (this.poseidonTrident.isPoseidonTrident(event.getBow())) {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onPlayerStopUsingItem(PlayerStopUsingItemEvent event) {
      Player player = event.getPlayer();
      ItemStack item = event.getItem();
      if (this.poseidonTrident.isPoseidonTrident(item)) {
         player.clearActiveItem();
         if (!player.isSneaking()) {
            if (this.poseidonTrident.isOnThrowCooldown(player.getUniqueId())) {
               long remaining = this.poseidonTrident.getRemainingThrowCooldown(player.getUniqueId());
               player.sendMessage(
                  this.plugin.getMessageWithPrefix("weapon-messages.poseidon-trident-throw-cooldown").replace("{time}", String.valueOf(remaining))
               );
            } else {
               this.throwTrident(player);
            }
         } else if (this.poseidonTrident.isOnRiptideCooldown(player.getUniqueId())) {
            long remaining = this.poseidonTrident.getRemainingRiptideCooldown(player.getUniqueId());
            player.sendMessage(
               this.plugin.getMessageWithPrefix("weapon-messages.poseidon-trident-riptide-cooldown").replace("{time}", String.valueOf(remaining))
            );
         } else {
            this.poseidonTrident.setRiptideCooldown(player.getUniqueId());
            this.simulateRiptide(player);
         }
      }
   }

   private void throwTrident(Player player) {
      this.poseidonTrident.setThrowCooldown(player.getUniqueId());
      final Location eyeLoc = player.getEyeLocation();
      final Vector direction = eyeLoc.getDirection().normalize();
      final ItemDisplay tridentDisplay = (ItemDisplay)player.getWorld().spawnEntity(eyeLoc, EntityType.ITEM_DISPLAY);
      ItemStack displayItem = new ItemStack(Material.BOW);
      ItemMeta displayMeta = displayItem.getItemMeta();
      if (displayMeta != null) {
         displayMeta.setCustomModelData(8);
         displayItem.setItemMeta(displayMeta);
      }

      tridentDisplay.setItemStack(displayItem);
      this.thrownTridents.put(player.getUniqueId(), tridentDisplay);
      player.getWorld().playSound(eyeLoc, Sound.ITEM_TRIDENT_THROW, 1.0F, 1.0F);
      final float rotationX = 90.0F;
      final float rotationY = 0.0F;
      final float rotationZ = 0.0F;
      this.setStableRotation(tridentDisplay, rotationX, rotationY, rotationZ);
      (new BukkitRunnable() {
         private Location currentLoc = eyeLoc.clone();
         private int ticks = 0;
         private final int maxTicks = 100;
         private final double speed = 1.8;
         private boolean returning = false;

         public void run() {
            if (this.ticks < 100 && !tridentDisplay.isDead() && tridentDisplay.isValid()) {
               if (!this.returning) {
                  this.currentLoc.add(direction.clone().multiply(1.8));
                  tridentDisplay.teleport(this.currentLoc);
                  PoseidonTridentListener.this.setStableRotation(tridentDisplay, rotationX, rotationY, rotationZ);
                  this.currentLoc.getWorld().spawnParticle(Particle.BUBBLE, this.currentLoc, 3, 0.1, 0.1, 0.1, 0.01);

                  for (Entity entity : this.currentLoc.getWorld().getNearbyEntities(this.currentLoc, 1.0, 1.0, 1.0)) {
                     if (entity instanceof LivingEntity && entity != player && entity != tridentDisplay) {
                        LivingEntity target = (LivingEntity)entity;
                        double damage = PoseidonTridentListener.this.poseidonTrident.getThrowDamage();
                        target.setNoDamageTicks(0);
                        target.damage(damage, player);
                        entity.getWorld().strikeLightning(entity.getLocation());
                        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
                        this.returning = true;
                        break;
                     }
                  }

                  if (!this.currentLoc.getBlock().getType().isAir() && this.currentLoc.getBlock().getType().isSolid()) {
                     this.currentLoc.getWorld().strikeLightning(this.currentLoc);
                     this.currentLoc.getWorld().playSound(this.currentLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
                     this.returning = true;
                  }
               } else {
                  Vector returnDirection = player.getLocation().add(0.0, 1.0, 0.0).subtract(this.currentLoc).toVector().normalize();
                  this.currentLoc.add(returnDirection.multiply(1.6));
                  tridentDisplay.teleport(this.currentLoc);
                  PoseidonTridentListener.this.setStableRotation(tridentDisplay, rotationX, rotationY, rotationZ);
                  if (this.currentLoc.distance(player.getLocation()) < 2.0) {
                     player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.0F, 1.0F);
                     this.cleanup();
                     return;
                  }
               }

               this.ticks++;
            } else {
               this.cleanup();
            }
         }

         private void cleanup() {
            if (!tridentDisplay.isDead()) {
               tridentDisplay.remove();
            }

            PoseidonTridentListener.this.thrownTridents.remove(player.getUniqueId());
            this.cancel();
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   private void simulateRiptide(Player player) {
      float yaw = player.getLocation().getYaw();
      float pitch = player.getLocation().getPitch();
      double yawRad = Math.toRadians(yaw);
      double pitchRad = Math.toRadians(pitch);
      double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
      double dy = -Math.sin(pitchRad);
      double dz = Math.cos(yawRad) * Math.cos(pitchRad);
      double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
      dx *= 8.0 / len;
      dy *= 8.0 / len;
      dz *= 8.0 / len;
      player.setVelocity(player.getVelocity().add(new Vector(dx, dy, dz)));
      this.triggerAutoSpinAttack(player);
      player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, SoundCategory.PLAYERS, 1.0F, 1.0F);
   }

   private void triggerAutoSpinAttack(Player player) {
      try {
         if (this.startAutoSpinAttackMethod == null) {
            this.getHandleMethod = player.getClass().getMethod("getHandle");
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            Class<?> nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
            this.asNmsCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            Object handle = this.getHandleMethod.invoke(player);
            this.startAutoSpinAttackMethod = handle.getClass().getMethod("startAutoSpinAttack", int.class, float.class, nmsItemStackClass);
         }

         Object handle = this.getHandleMethod.invoke(player);
         Object nmsStack = this.asNmsCopyMethod.invoke(null, player.getInventory().getItemInMainHand());
         this.startAutoSpinAttackMethod.invoke(handle, 20, 8.0F, nmsStack);
      } catch (ReflectiveOperationException var5) {
         if (!this.nmsUnavailableWarned) {
            this.nmsUnavailableWarned = true;
            this.plugin
               .getLogger()
               .warning("[PoseidonTrident] Не удалось вызвать ванильный startAutoSpinAttack, рывок будет работать без анимации кручения: " + var5);
         }
      }
   }

   private void setStableRotation(ItemDisplay display, float x, float y, float z) {
      Transformation transformation = display.getTransformation();
      Quaternionf rotation = new Quaternionf();
      rotation.rotateX((float)Math.toRadians(x));
      rotation.rotateY((float)Math.toRadians(y));
      rotation.rotateZ((float)Math.toRadians(z));
      transformation.getLeftRotation().set(rotation);
      display.setTransformation(transformation);
   }

   private void startDolphinGraceTask() {
      (new BukkitRunnable() {
            public void run() {
               for (Player player : Bukkit.getOnlinePlayers()) {
                  ItemStack mainHand = player.getInventory().getItemInMainHand();
                  ItemStack offHand = player.getInventory().getItemInOffHand();
                  boolean hasTrident = PoseidonTridentListener.this.poseidonTrident.isPoseidonTrident(mainHand)
                     || PoseidonTridentListener.this.poseidonTrident.isPoseidonTrident(offHand);
                  if (hasTrident) {
                     player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 2, true, false, false));
                  }
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 20L);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();
      ItemDisplay thrownTrident = this.thrownTridents.remove(playerId);
      if (thrownTrident != null && !thrownTrident.isDead()) {
         thrownTrident.remove();
      }
   }
}
