package dev.demora.demoralegendaryweapons.weapons.elementalstaff;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class ElementalStaffListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final ElementalStaff elementalStaff;
   private final Map<UUID, ItemDisplay[]> orbs = new HashMap<>();
   private final Map<UUID, long[]> cooldownEnds = new HashMap<>();
   private final Map<UUID, Long> switchCooldownEnds = new HashMap<>();
   private final List<ElementalStaffListener.Projectile> projectiles = new ArrayList<>();
   private long ticks = 0L;
   private static final Color[] TRAIL_COLORS = new Color[]{
      Color.fromRGB(40, 120, 255), Color.fromRGB(60, 200, 60), Color.fromRGB(255, 30, 30), Color.fromRGB(255, 190, 40), Color.fromRGB(120, 20, 170)
   };

   public ElementalStaffListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.elementalStaff = new ElementalStaff(plugin);
      this.startOrbitTask();
      this.startProjectileTask();
   }

   public ElementalStaff getElementalStaff() {
      return this.elementalStaff;
   }

   @EventHandler
   public void onChat(AsyncChatEvent event) {
      String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
      if (message.equalsIgnoreCase("admr")) {
         event.setCancelled(true);
         Player player = event.getPlayer();
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (player.isOp()) {
               player.getInventory().addItem(new ItemStack[]{this.elementalStaff.createElementalStaff()});
            }
         });
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         Player player = event.getPlayer();
         Action action = event.getAction();
         boolean holding = this.elementalStaff.isElementalStaff(player.getInventory().getItemInMainHand());
         if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && holding) {
               event.setCancelled(true);
               int index = this.elementalStaff.getElementIndex(player.getInventory().getItemInMainHand());
               this.throwElement(player, index);
            }
         } else if (holding) {
            event.setCancelled(true);
            int index = this.elementalStaff.cycleElement(player.getInventory().getItemInMainHand());
            player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(this.elementalStaff.getElementName(index)));
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.4F);
         } else {
            this.tryDeflect(player);
         }
      }
   }

   private void throwElement(Player player, int index) {
      if (!this.elementalStaff.isImplemented(index)) {
         player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize("§7Эта стихия ещё не реализована"));
      } else if (this.isOnCooldown(player, index)) {
         long remaining = this.getRemainingCooldown(player, index);
         player.sendActionBar(
            LegacyComponentSerializer.legacySection().deserialize(this.elementalStaff.getElementName(index) + " §7— откат " + remaining + "с")
         );
         player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 0.6F);
      } else if (this.isOnSwitchCooldown(player)) {
         long remaining = this.getRemainingSwitchCooldown(player);
         player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize("§7Смена стихии — откат " + remaining + "с"));
         player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 0.6F);
      } else {
         this.setCooldown(player, index);
         this.setSwitchCooldown(player);
         World world = player.getWorld();
         Location eye = player.getEyeLocation();
         Vector dir = eye.getDirection().normalize();
         double speed = this.elementalStaff.getProjectileSpeed();
         Location spawnLoc = eye.clone().add(dir.clone().multiply(1.0));
         ItemDisplay display = this.spawnDisplay(world, spawnLoc, index, 0.9F);
         ElementalStaffListener.Projectile projectile = new ElementalStaffListener.Projectile();
         projectile.display = display;
         projectile.velocity = dir.clone().multiply(speed);
         projectile.element = index;
         projectile.owner = player.getUniqueId();
         projectile.speed = speed;
         projectile.life = 0;
         this.projectiles.add(projectile);
         world.playSound(eye, Sound.ENTITY_ENDER_DRAGON_SHOOT, 0.8F, 1.4F);
         world.playSound(eye, Sound.ENTITY_GENERIC_SPLASH, 0.6F, 1.2F);
      }
   }

   private void tryDeflect(Player player) {
      Location eye = player.getEyeLocation();
      Vector look = eye.getDirection().normalize();
      double reach = 4.5;
      ElementalStaffListener.Projectile best = null;
      double bestDot = 0.65;

      for (ElementalStaffListener.Projectile projectile : this.projectiles) {
         if (projectile.display != null && projectile.display.isValid()) {
            Vector to = projectile.display.getLocation().toVector().subtract(eye.toVector());
            double dist = to.length();
            if (!(dist > reach) && !(dist < 0.1)) {
               double dot = to.normalize().dot(look);
               if (dot > bestDot) {
                  bestDot = dot;
                  best = projectile;
               }
            }
         }
      }

      if (best != null) {
         best.velocity = look.clone().multiply(best.speed);
         best.owner = player.getUniqueId();
         World world = player.getWorld();
         Location loc = best.display.getLocation();
         world.playSound(loc, Sound.ITEM_SHIELD_BLOCK, 1.2F, 1.2F);
         world.spawnParticle(Particle.CRIT, loc, 12, 0.2, 0.2, 0.2, 0.2);
         player.swingMainHand();
      }
   }

   private void startProjectileTask() {
      (new BukkitRunnable() {
         public void run() {
            Iterator<ElementalStaffListener.Projectile> iterator = ElementalStaffListener.this.projectiles.iterator();

            while (iterator.hasNext()) {
               ElementalStaffListener.Projectile projectile = iterator.next();
               ItemDisplay display = projectile.display;
               if (display != null && display.isValid()) {
                  projectile.life++;
                  if (projectile.life > 120) {
                     display.remove();
                     iterator.remove();
                  } else {
                     World world = display.getWorld();
                     Location current = display.getLocation();
                     Location next = current.clone().add(projectile.velocity);
                     if (!next.getBlock().isPassable()) {
                        ElementalStaffListener.this.detonate(projectile, current);
                        iterator.remove();
                     } else {
                        LivingEntity hit = null;
                        Iterator var8 = world.getNearbyEntities(next, 0.6, 0.6, 0.6).iterator();

                        while (true) {
                           if (var8.hasNext()) {
                              Entity entity = (Entity)var8.next();
                              if (!(entity instanceof LivingEntity living) || entity.getUniqueId().equals(projectile.owner)) {
                                 continue;
                              }

                              hit = living;
                           }

                           if (hit != null) {
                              ElementalStaffListener.this.detonate(projectile, hit.getLocation().add(0.0, hit.getHeight() * 0.5, 0.0));
                              iterator.remove();
                           } else {
                              next.setYaw(projectile.life * 8 % 360);
                              display.teleport(next);
                              ElementalStaffListener.this.spawnTrail(projectile.element, next);
                           }
                           break;
                        }
                     }
                  }
               } else {
                  iterator.remove();
               }
            }
         }
      }).runTaskTimer(this.plugin, 1L, 1L);
   }

   private void spawnTrail(int element, Location loc) {
      World world = loc.getWorld();
      world.spawnParticle(Particle.DUST, loc, 6, 0.12, 0.12, 0.12, 0.0, new DustOptions(TRAIL_COLORS[element], 1.1F));
      if (element == 0) {
         world.spawnParticle(Particle.BUBBLE, loc, 4, 0.12, 0.12, 0.12, 0.02);
      } else if (element == 1) {
         world.spawnParticle(Particle.ITEM_SLIME, loc, 2, 0.1, 0.1, 0.1, 0.01);
      } else if (element == 3) {
         world.spawnParticle(Particle.FLAME, loc, 4, 0.1, 0.1, 0.1, 0.01);
         world.spawnParticle(Particle.SMALL_FLAME, loc, 3, 0.12, 0.12, 0.12, 0.01);
      } else if (element == 4) {
         world.spawnParticle(Particle.SQUID_INK, loc, 3, 0.1, 0.1, 0.1, 0.01);
      }
   }

   private void detonate(ElementalStaffListener.Projectile projectile, Location hit) {
      Player shooter = Bukkit.getPlayer(projectile.owner);
      if (projectile.element == 4) {
         this.applyDarkness(hit, shooter);
      } else if (projectile.element == 0) {
         this.applyWater(hit, shooter);
      } else if (projectile.element == 1) {
         this.applyPoison(hit, shooter);
      } else if (projectile.element == 2) {
         this.applyChaos(hit, shooter);
      } else if (projectile.element == 3) {
         this.applyFire(hit, shooter);
      }

      if (projectile.display.isValid()) {
         projectile.display.remove();
      }
   }

   private void applyDarkness(Location center, Player shooter) {
      World world = center.getWorld();
      world.spawnParticle(Particle.SQUID_INK, center, 70, 1.3, 1.3, 1.3, 0.05);
      world.spawnParticle(Particle.LARGE_SMOKE, center, 50, 1.2, 1.2, 1.2, 0.02);
      world.spawnParticle(Particle.SCULK_SOUL, center, 40, 1.0, 1.0, 1.0, 0.05);
      world.spawnParticle(Particle.SMOKE, center, 60, 1.4, 1.4, 1.4, 0.03);
      world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.9F, 0.6F);
      world.playSound(center, Sound.PARTICLE_SOUL_ESCAPE, 1.2F, 0.7F);
      double radius = this.elementalStaff.getDarknessRadius();
      int blindness = this.elementalStaff.getDarknessBlindnessSeconds() * 20;
      int wither = this.elementalStaff.getDarknessWitherSeconds() * 20;
      double damage = this.elementalStaff.getDarknessDamage();

      for (LivingEntity target : world.getNearbyLivingEntities(center, radius)) {
         target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindness, 0));
         target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, wither, 0));
         target.setNoDamageTicks(0);
         if (shooter != null) {
            target.damage(damage, shooter);
         } else {
            target.damage(damage);
         }
      }
   }

   private void applyChaos(Location center, Player shooter) {
      World world = center.getWorld();
      world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 3, 0.6, 0.6, 0.6, 0.0);
      world.spawnParticle(Particle.EXPLOSION, center, 12, 1.4, 1.4, 1.4, 0.0);
      world.spawnParticle(Particle.DUST, center, 160, 2.2, 2.2, 2.2, 0.0, new DustOptions(Color.fromRGB(255, 25, 25), 2.2F));
      world.spawnParticle(Particle.DUST, center, 90, 1.8, 1.8, 1.8, 0.0, new DustOptions(Color.fromRGB(130, 0, 0), 3.0F));
      world.spawnParticle(Particle.CRIT, center, 40, 1.2, 1.2, 1.2, 0.4);
      world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.6F, 0.8F);
      world.playSound(center, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.3F, 0.9F);
      double radius = this.elementalStaff.getChaosRadius();
      double damage = this.elementalStaff.getChaosDamage();

      for (LivingEntity target : world.getNearbyLivingEntities(center, radius)) {
         target.setNoDamageTicks(0);
         if (shooter != null) {
            target.damage(damage, shooter);
         } else {
            target.damage(damage);
         }

         Vector knock = target.getLocation().toVector().subtract(center.toVector());
         if (knock.lengthSquared() > 0.001) {
            knock.normalize().multiply(1.2).setY(0.6);
            target.setVelocity(target.getVelocity().add(knock));
         }
      }
   }

   private void applyPoison(Location hit, Player shooter) {
      final World world = hit.getWorld();
      final Location center = this.settleToGround(hit, 4).add(0.0, 0.2, 0.0);
      final double radius = this.elementalStaff.getPoisonRadius();
      final int cloudTicks = this.elementalStaff.getPoisonCloudSeconds() * 20;
      final int poisonTicks = this.elementalStaff.getPoisonEffectSeconds() * 20;
      final int amplifier = this.elementalStaff.getPoisonAmplifier();
      double impact = this.elementalStaff.getPoisonImpactDamage();
      world.spawnParticle(Particle.ITEM_SLIME, center, 60, radius * 0.6, 0.4, radius * 0.6, 0.1);
      world.spawnParticle(Particle.DUST, center, 80, radius * 0.6, 0.4, radius * 0.6, 0.0, new DustOptions(Color.fromRGB(60, 200, 60), 2.0F));
      world.playSound(center, Sound.ENTITY_SPLASH_POTION_BREAK, 1.2F, 1.1F);
      world.playSound(center, Sound.BLOCK_BREWING_STAND_BREW, 1.2F, 0.7F);

      for (LivingEntity target : world.getNearbyLivingEntities(center, radius)) {
         target.setNoDamageTicks(0);
         if (shooter != null) {
            target.damage(impact, shooter);
         } else {
            target.damage(impact);
         }
      }

      (new BukkitRunnable() {
         private int t = 0;

         public void run() {
            if (this.t >= cloudTicks) {
               world.playSound(center, Sound.BLOCK_FIRE_EXTINGUISH, 0.7F, 1.6F);
               this.cancel();
            } else {
               for (int i = 0; i < 10; i++) {
                  double angle = Math.random() * Math.PI * 2.0;
                  double r = radius * Math.sqrt(Math.random());
                  Location p = center.clone().add(Math.cos(angle) * r, Math.random() * 1.3, Math.sin(angle) * r);
                  world.spawnParticle(Particle.DUST, p, 1, 0.0, 0.0, 0.0, 0.0, new DustOptions(Color.fromRGB(70, 190, 60), 1.6F));
               }

               if (this.t % 4 == 0) {
                  double angle = Math.random() * Math.PI * 2.0;
                  double r = radius * Math.sqrt(Math.random());
                  world.spawnParticle(Particle.ITEM_SLIME, center.clone().add(Math.cos(angle) * r, 0.3, Math.sin(angle) * r), 1, 0.05, 0.05, 0.05, 0.0);
               }

               if (this.t % 10 == 0) {
                  world.playSound(center, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 0.4F, 0.6F);

                  for (LivingEntity targetx : world.getNearbyLivingEntities(center, radius)) {
                     targetx.addPotionEffect(new PotionEffect(PotionEffectType.POISON, poisonTicks, amplifier, false, true));
                  }
               }

               this.t++;
            }
         }
      }).runTaskTimer(this.plugin, 1L, 1L);
   }

   private void applyFire(Location hit, Player shooter) {
      final World world = hit.getWorld();
      final double radius = this.elementalStaff.getFireRadius();
      int burnTicks = this.elementalStaff.getFireBurnSeconds() * 20;
      double damage = this.elementalStaff.getFireDamage();
      world.spawnParticle(Particle.FLAME, hit, 140, radius * 0.5, 0.5, radius * 0.5, 0.14);
      world.spawnParticle(Particle.LAVA, hit, 25, radius * 0.4, 0.3, radius * 0.4, 0.0);
      world.spawnParticle(Particle.EXPLOSION, hit, 5, 0.7, 0.7, 0.7, 0.0);
      world.spawnParticle(Particle.LARGE_SMOKE, hit, 40, radius * 0.4, 0.5, radius * 0.4, 0.03);
      world.spawnParticle(Particle.DUST, hit, 90, radius * 0.5, 0.5, radius * 0.5, 0.0, new DustOptions(Color.fromRGB(255, 150, 30), 2.2F));
      world.playSound(hit, Sound.ITEM_FIRECHARGE_USE, 1.5F, 0.8F);
      world.playSound(hit, Sound.ENTITY_BLAZE_SHOOT, 1.3F, 0.7F);
      world.playSound(hit, Sound.ENTITY_GENERIC_EXPLODE, 0.9F, 1.4F);

      for (LivingEntity target : world.getNearbyLivingEntities(hit, radius)) {
         target.setNoDamageTicks(0);
         if (shooter != null) {
            target.damage(damage, shooter);
         } else {
            target.damage(damage);
         }

         target.setFireTicks(Math.max(target.getFireTicks(), burnTicks));
      }

      (new BukkitRunnable() {
         private int step = 0;

         public void run() {
            if (this.step > 8) {
               this.cancel();
            } else {
               double r = radius * (this.step / 8.0);

               for (int i = 0; i < 26; i++) {
                  double angle = (Math.PI / 13) * i;
                  Location p = hit.clone().add(Math.cos(angle) * r, 0.25, Math.sin(angle) * r);
                  world.spawnParticle(Particle.FLAME, p, 2, 0.05, 0.05, 0.05, 0.02);
                  world.spawnParticle(Particle.SMALL_FLAME, p, 1, 0.05, 0.1, 0.05, 0.01);
               }

               this.step++;
            }
         }
      }).runTaskTimer(this.plugin, 1L, 1L);
      if (this.elementalStaff.isFireBlocksEnabled()) {
         this.placeFire(hit, radius);
      }
   }

   private void placeFire(Location center, double radius) {
      World world = center.getWorld();
      int lifetime = this.elementalStaff.getFireBlockLifetimeSeconds() * 20;
      int r = (int)Math.ceil(radius);

      for (int dx = -r; dx <= r; dx++) {
         for (int dz = -r; dz <= r; dz++) {
            if (!(dx * dx + dz * dz > radius * radius) && !(Math.random() > 0.75)) {
               for (int dy = 2; dy >= -2; dy--) {
                  Block block = world.getBlockAt(center.getBlockX() + dx, center.getBlockY() + dy, center.getBlockZ() + dz);
                  if (block.getType() == Material.AIR && block.getRelative(BlockFace.DOWN).getType().isSolid()) {
                     if (!this.plugin.isRegionProtected(block.getLocation())) {
                        block.setType(Material.FIRE, false);
                        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                           if (block.getType() == Material.FIRE) {
                              block.setType(Material.AIR, false);
                           }
                        }, lifetime);
                     }
                     break;
                  }
               }
            }
         }
      }
   }

   private Location settleToGround(Location from, int maxDrop) {
      Location loc = from.clone();

      for (int i = 0; i < maxDrop; i++) {
         Location below = loc.clone().add(0.0, -1.0, 0.0);
         if (!below.getBlock().isPassable()) {
            return loc;
         }

         loc = below;
      }

      return loc;
   }

   private void applyWater(Location hit, Player shooter) {
      final World world = hit.getWorld();
      final int height = this.elementalStaff.getWaterColumnHeight();
      int width = Math.max(1, this.elementalStaff.getWaterColumnWidth());
      final int half = width / 2;
      Location base = hit.clone();

      for (int scan = 0; scan < 12 && base.getBlock().isPassable(); scan++) {
         base.add(0.0, -1.0, 0.0);
      }

      base.add(0.0, 1.0, 0.0);
      final Location columnBase = base.clone();
      world.playSound(columnBase, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.2F, 0.8F);
      world.playSound(columnBase, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1.4F, 1.0F);
      (new BukkitRunnable() {
         private int step = 0;

         public void run() {
            if (this.step > height * 3) {
               this.cancel();
            } else {
               double y = this.step * 0.4;
               if (y <= height) {
                  for (int dx = -half; dx <= half; dx++) {
                     for (int dz = -half; dz <= half; dz++) {
                        Location p = new Location(world, columnBase.getX() + dx + 0.5, columnBase.getY() + y, columnBase.getZ() + dz + 0.5);
                        world.spawnParticle(Particle.BUBBLE_COLUMN_UP, p, 6, 0.25, 0.25, 0.25, 0.12);
                        world.spawnParticle(Particle.BUBBLE, p, 3, 0.25, 0.35, 0.25, 0.05);
                        world.spawnParticle(Particle.SPLASH, p, 2, 0.2, 0.2, 0.2, 0.0);
                     }
                  }
               }

               this.step++;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
      Location boxCenter = columnBase.clone().add(0.0, height / 2.0, 0.0);
      double launch = this.elementalStaff.getWaterLaunchPower();
      int drownTicks = this.elementalStaff.getWaterDrowningSeconds() * 20;
      double drownDamage = this.elementalStaff.getWaterDrowningDamage();

      for (Entity entity : world.getNearbyEntities(boxCenter, half + 0.5, height / 2.0, half + 0.5)) {
         if (entity instanceof LivingEntity living) {
            living.setVelocity(new Vector(0.0, launch, 0.0));
            this.applyDrowning(living, drownTicks, drownDamage);
         }
      }
   }

   private void applyDrowning(LivingEntity target, int durationTicks, double damagePerSecond) {
      (new BukkitRunnable() {
         private int t = 0;

         public void run() {
            if (this.t < durationTicks && !target.isDead() && target.isValid()) {
               target.setRemainingAir(Math.max(target.getRemainingAir() - 60, -20));
               target.getWorld().spawnParticle(Particle.BUBBLE, target.getEyeLocation(), 6, 0.25, 0.25, 0.25, 0.02);
               if (this.t > 0 && this.t % 20 == 0) {
                  target.setNoDamageTicks(0);
                  target.damage(damagePerSecond);
               }

               this.t++;
            } else {
               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 1L, 1L);
   }

   private void startOrbitTask() {
      (new BukkitRunnable() {
            public void run() {
               ElementalStaffListener.this.ticks++;

               for (Player player : Bukkit.getOnlinePlayers()) {
                  UUID id = player.getUniqueId();
                  boolean holding = ElementalStaffListener.this.elementalStaff.isElementalStaff(player.getInventory().getItemInMainHand());
                  ItemDisplay[] set = ElementalStaffListener.this.orbs.get(id);
                  if (!holding) {
                     if (set != null) {
                        ElementalStaffListener.this.removeOrbs(set);
                        ElementalStaffListener.this.orbs.remove(id);
                     }
                  } else {
                     if (set == null) {
                        set = new ItemDisplay[ElementalStaffListener.this.elementalStaff.getElementCount()];
                        ElementalStaffListener.this.orbs.put(id, set);
                     }

                     ElementalStaffListener.this.updateOrbs(player, set);
                     if (ElementalStaffListener.this.ticks % 20L == 0L) {
                        int index = ElementalStaffListener.this.elementalStaff.getElementIndex(player.getInventory().getItemInMainHand());
                        player.sendActionBar(
                           LegacyComponentSerializer.legacySection().deserialize(ElementalStaffListener.this.elementalStaff.getElementName(index))
                        );
                     }
                  }
               }
            }
         })
         .runTaskTimer(this.plugin, 1L, 1L);
   }

   private void updateOrbs(Player player, ItemDisplay[] set) {
      World world = player.getWorld();
      Location center = player.getLocation();
      double radius = 1.6;
      double baseY = 1.1;
      double orbitAngle = this.ticks * 0.06;
      int count = set.length;

      for (int i = 0; i < count; i++) {
         boolean ready = !this.isOnCooldown(player, i);
         if (ready && set[i] == null) {
            set[i] = this.spawnDisplay(world, center, i, 0.9F);
         } else if (!ready && set[i] != null) {
            if (set[i].isValid()) {
               set[i].remove();
            }

            set[i] = null;
         }

         ItemDisplay display = set[i];
         if (display != null && display.isValid()) {
            double angle = orbitAngle + (Math.PI * 2) / count * i;
            double bob = Math.sin(this.ticks * 0.15 + i * 1.2) * 0.25;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            double y = center.getY() + baseY + bob;
            Location loc = new Location(world, x, y, z);
            loc.setYaw((float)((this.ticks * 4L + i * 72) % 360L));
            display.teleport(loc);
         }
      }
   }

   private ItemStack sphereItem(int index) {
      ItemStack sphere = new ItemStack(Material.SLIME_BALL);
      ItemMeta meta = sphere.getItemMeta();
      if (meta != null) {
         meta.setCustomModelData(this.elementalStaff.getSphereModelData()[index]);
         sphere.setItemMeta(meta);
      }

      return sphere;
   }

   private ItemDisplay spawnDisplay(World world, Location loc, int index, float scale) {
      return (ItemDisplay)world.spawn(
         loc,
         ItemDisplay.class,
         display -> {
            display.setItemStack(this.sphereItem(index));
            display.setItemDisplayTransform(ItemDisplayTransform.FIXED);
            display.setBillboard(Billboard.FIXED);
            display.setBrightness(new Brightness(15, 15));
            display.setPersistent(false);
            display.setTransformation(
               new Transformation(
                  new Vector3f(0.0F, 0.0F, 0.0F),
                  new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F),
                  new Vector3f(scale, scale, scale),
                  new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F)
               )
            );

            try {
               display.setTeleportDuration(2);
            } catch (NoSuchMethodError var5) {
            }
         }
      );
   }

   private void removeOrbs(ItemDisplay[] set) {
      for (ItemDisplay display : set) {
         if (display != null && display.isValid()) {
            display.remove();
         }
      }
   }

   private boolean isOnCooldown(Player player, int index) {
      long[] ends = this.cooldownEnds.get(player.getUniqueId());
      return ends != null && System.currentTimeMillis() < ends[index];
   }

   private long getRemainingCooldown(Player player, int index) {
      long[] ends = this.cooldownEnds.get(player.getUniqueId());
      return ends == null ? 0L : Math.max(0L, (ends[index] - System.currentTimeMillis() + 999L) / 1000L);
   }

   private void setCooldown(Player player, int index) {
      long[] ends = this.cooldownEnds.computeIfAbsent(player.getUniqueId(), k -> new long[this.elementalStaff.getElementCount()]);
      ends[index] = System.currentTimeMillis() + this.elementalStaff.getThrowCooldownSeconds() * 1000L;
   }

   private boolean isOnSwitchCooldown(Player player) {
      Long end = this.switchCooldownEnds.get(player.getUniqueId());
      return end != null && System.currentTimeMillis() < end;
   }

   private long getRemainingSwitchCooldown(Player player) {
      Long end = this.switchCooldownEnds.get(player.getUniqueId());
      return end == null ? 0L : Math.max(0L, (end - System.currentTimeMillis() + 999L) / 1000L);
   }

   private void setSwitchCooldown(Player player) {
      this.switchCooldownEnds.put(player.getUniqueId(), System.currentTimeMillis() + this.elementalStaff.getSwitchCooldownSeconds() * 1000L);
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      UUID id = event.getPlayer().getUniqueId();
      ItemDisplay[] set = this.orbs.remove(id);
      if (set != null) {
         this.removeOrbs(set);
      }

      this.cooldownEnds.remove(id);
      this.switchCooldownEnds.remove(id);
   }

   private static class Projectile {
      private ItemDisplay display;
      private Vector velocity;
      private int element;
      private UUID owner;
      private double speed;
      private int life;
   }
}
