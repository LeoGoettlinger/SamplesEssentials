package dev.demora.demoralegendaryweapons.weapons.dragonkatana.rework;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class AwakenedDragonKatana {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey awakenedKatanaKey;
   private final Map<UUID, Long> cooldowns;
   private final Map<UUID, Long> teleportCooldowns;
   private final Map<UUID, Location> lastTeleportLocations;
   private FileConfiguration reworkConfig;

   public AwakenedDragonKatana(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.awakenedKatanaKey = new NamespacedKey(plugin, "awakened_dragon_katana");
      this.cooldowns = new HashMap<>();
      this.teleportCooldowns = new HashMap<>();
      this.lastTeleportLocations = new HashMap<>();
      this.loadReworkConfig();
   }

   private void loadReworkConfig() {
      File reworkFile = new File(this.plugin.getDataFolder(), "rework.yml");
      if (!reworkFile.exists()) {
         try {
            this.plugin.getDataFolder().mkdirs();
            InputStream defaultConfig = this.plugin.getResource("rework.yml");
            if (defaultConfig != null) {
               Files.copy(defaultConfig, reworkFile.toPath());
               defaultConfig.close();
            }
         } catch (IOException var3) {
            this.plugin.getLogger().severe("Не удалось создать rework.yml: " + var3.getMessage());
         }
      }

      this.reworkConfig = YamlConfiguration.loadConfiguration(reworkFile);
   }

   public void reloadConfig() {
      File reworkFile = new File(this.plugin.getDataFolder(), "rework.yml");
      this.reworkConfig = YamlConfiguration.loadConfiguration(reworkFile);
   }

   public ItemStack createAwakenedDragonKatana() {
      ItemStack katana = new ItemStack(Material.NETHERITE_SWORD);
      ItemMeta meta = katana.getItemMeta();
      String weaponName = this.plugin.getLanguageManager().getWeaponName("awakened_dragon_katana");
      if (weaponName.isEmpty()) {
         weaponName = "§5§lПробуждённая Драконья катана";
      }

      meta.setDisplayName(weaponName);
      meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
      meta.setUnbreakable(true);
      double damage = this.plugin
         .getConfig()
         .getDouble("weapons.awakened_dragon_katana.attribute_damage", this.reworkConfig.getDouble("awakened_dragon_katana.damage", 11.0));
      if (AttributeCompat.getAttackDamage() != null) {
         meta.addAttributeModifier(
            AttributeCompat.getAttackDamage(),
            new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", damage, Operation.ADD_NUMBER, EquipmentSlot.HAND)
         );
      }

      double attackSpeed = this.plugin.getConfig().getDouble("weapons.awakened_dragon_katana.attack_speed", -2.4);
      if (AttributeCompat.getAttackSpeed() != null) {
         meta.addAttributeModifier(
            AttributeCompat.getAttackSpeed(),
            new AttributeModifier(UUID.randomUUID(), "generic.attack_speed", attackSpeed, Operation.ADD_NUMBER, EquipmentSlot.HAND)
         );
      }

      meta.setCustomModelData(4);
      PersistentDataContainer container = meta.getPersistentDataContainer();
      container.set(this.awakenedKatanaKey, PersistentDataType.BYTE, (byte)1);
      this.plugin.applyWeaponLore(meta, "awakened_dragon_katana");
      katana.setItemMeta(meta);
      return katana;
   }

   public boolean isAwakenedDragonKatana(ItemStack item) {
      if (item != null && item.getType() == Material.NETHERITE_SWORD) {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return false;
         } else {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            return container.has(this.awakenedKatanaKey, PersistentDataType.BYTE);
         }
      } else {
         return false;
      }
   }

   public void handleTeleport(Player player, Location from, Location to) {
      this.lastTeleportLocations.put(player.getUniqueId(), from);
      this.createParticleSphere(from);
      this.createExplosionPath(from, to, player);
      int cooldownTime = this.reworkConfig.getInt("awakened_dragon_katana.teleport_cooldown", 5);
      this.teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime * 1000L);
   }

   public boolean isOnTeleportCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.teleportCooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownEnd = this.teleportCooldowns.get(playerId);
         long currentTime = System.currentTimeMillis();
         if (currentTime >= cooldownEnd) {
            this.teleportCooldowns.remove(playerId);
            return false;
         } else {
            return true;
         }
      }
   }

   public long getTeleportRemainingCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.teleportCooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownEnd = this.teleportCooldowns.get(playerId);
         long currentTime = System.currentTimeMillis();
         long remainingMs = cooldownEnd - currentTime;
         return Math.max(0L, remainingMs / 1000L);
      }
   }

   private void createParticleSphere(Location location) {
      (new BukkitRunnable() {
         private int ticks = 0;

         public void run() {
            int particleDuration = AwakenedDragonKatana.this.reworkConfig.getInt("awakened_dragon_katana.particle_sphere_duration", 2) * 20;
            if (this.ticks >= particleDuration) {
               AwakenedDragonKatana.this.explodeParticleSphere(location);
               this.cancel();
            } else {
               double radius = 1.5;

               for (int i = 0; i < 30; i++) {
                  double angle = Math.random() * Math.PI * 2.0;
                  double yAngle = Math.random() * Math.PI;
                  double x = radius * Math.sin(yAngle) * Math.cos(angle);
                  double y = radius * Math.cos(yAngle);
                  double z = radius * Math.sin(yAngle) * Math.sin(angle);
                  Location particleLoc = location.clone().add(x, y + 1.0, z);

                  try {
                     location.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 2, 0.1, 0.1, 0.1, 0.01, 0.5F);
                  } catch (IllegalArgumentException var17) {
                     location.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 2, 0.1, 0.1, 0.1, 0.01);
                  }

                  location.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 5, 0.2, 0.2, 0.2, 0.5);
                  if (i % 3 == 0) {
                     location.getWorld().spawnParticle(Particle.WITCH, particleLoc, 1, 0.0, 0.0, 0.0, 0.0);
                  }
               }

               this.ticks++;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   private void explodeParticleSphere(Location location) {
      Location centerLoc = location.clone().add(0.0, 1.0, 0.0);
      location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, centerLoc, 3, 0.5, 0.5, 0.5, 0.0);

      try {
         location.getWorld().spawnParticle(Particle.DRAGON_BREATH, centerLoc, 100, 2.0, 2.0, 2.0, 0.3, 1.0F);
      } catch (IllegalArgumentException var11) {
         location.getWorld().spawnParticle(Particle.DRAGON_BREATH, centerLoc, 100, 2.0, 2.0, 2.0, 0.3);
      }

      location.getWorld().spawnParticle(Particle.PORTAL, centerLoc, 150, 3.0, 3.0, 3.0, 1.5);
      location.getWorld().spawnParticle(Particle.WITCH, centerLoc, 50, 2.5, 2.5, 2.5, 0.2);
      location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, centerLoc, 80, 2.0, 2.0, 2.0, 0.5);
      location.getWorld().spawnParticle(Particle.END_ROD, centerLoc, 40, 2.0, 2.0, 2.0, 0.15);

      for (int i = 0; i < 20; i++) {
         double angle = (Math.PI * 2) * i / 20.0;
         double x = Math.cos(angle) * 3.0;
         double z = Math.sin(angle) * 3.0;
         Location ringLoc = centerLoc.clone().add(x, 0.0, z);
         location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, ringLoc, 3, 0.1, 0.5, 0.1, 0.02);
      }

      location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.5F, 0.8F);
      location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 1.5F);
      double explosionRadius = this.reworkConfig.getDouble("awakened_dragon_katana.teleport_explosion_radius", 3.0);
      double damage = this.reworkConfig.getDouble("awakened_dragon_katana.teleport_explosion_damage", 8.0);
      location.getWorld().getNearbyEntities(location, explosionRadius, explosionRadius, explosionRadius).forEach(entity -> {
         if (entity instanceof Player target) {
            target.damage(damage);
         }
      });
   }

   private void createExplosionPath(Location from, Location to, Player owner) {
      Vector direction = to.toVector().subtract(from.toVector()).normalize();
      double distance = from.distance(to);
      int explosions = (int)(distance / this.reworkConfig.getDouble("awakened_dragon_katana.path_explosion_interval", 5.0));

      for (int i = 1; i <= explosions; i++) {
         final Location explosionLoc = from.clone().add(direction.clone().multiply(i * 5));
         (new BukkitRunnable() {
            public void run() {
               AwakenedDragonKatana.this.createPathExplosion(explosionLoc, owner);
            }
         }).runTaskLater(this.plugin, i * 5L);
      }
   }

   private void createPathExplosion(Location location, Player owner) {
      location.getWorld().spawnParticle(Particle.EXPLOSION, location, 5, 0.5, 0.5, 0.5, 0.0);

      try {
         location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 50, 1.5, 1.5, 1.5, 0.15, 0.8F);
      } catch (IllegalArgumentException var11) {
         location.getWorld().spawnParticle(Particle.DRAGON_BREATH, location, 50, 1.5, 1.5, 1.5, 0.15);
      }

      location.getWorld().spawnParticle(Particle.PORTAL, location, 60, 1.5, 1.5, 1.5, 1.0);
      location.getWorld().spawnParticle(Particle.WITCH, location, 25, 1.0, 1.0, 1.0, 0.1);
      location.getWorld().spawnParticle(Particle.REVERSE_PORTAL, location, 40, 1.5, 1.5, 1.5, 0.3);
      location.getWorld().spawnParticle(Particle.END_ROD, location, 20, 1.0, 1.0, 1.0, 0.1);
      location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, location, 15, 1.0, 1.0, 1.0, 0.05);

      for (int i = 0; i < 8; i++) {
         double angle = (Math.PI * 2) * i / 8.0;
         double x = Math.cos(angle) * 2.0;
         double z = Math.sin(angle) * 2.0;
         Location ringLoc = location.clone().add(x, 0.0, z);
         location.getWorld().spawnParticle(Particle.FLAME, ringLoc, 2, 0.1, 0.3, 0.1, 0.02);
      }

      location.getWorld().playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0F, 1.2F);
      location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.6F, 1.5F);
      double explosionRadius = this.reworkConfig.getDouble("awakened_dragon_katana.path_explosion_radius", 2.0);
      double damage = this.reworkConfig.getDouble("awakened_dragon_katana.path_explosion_damage", 6.0);
      location.getWorld().getNearbyEntities(location, explosionRadius, explosionRadius, explosionRadius).forEach(entity -> {
         if (entity instanceof Player && !entity.equals(owner)) {
            Player target = (Player)entity;
            target.damage(damage);
         }
      });
   }

   public void swapAllPlayers(Player owner) {
      final List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
      if (onlinePlayers.size() >= 2) {
         for (Player player : onlinePlayers) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 255, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 200, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, false, false, false));
            player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.0F, 0.5F);
            Location playerLoc = player.getLocation();
            playerLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, playerLoc.add(0.0, 1.0, 0.0), 50, 0.5, 1.0, 0.5, 0.1);
         }

         owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5F, 0.5F);
         (new BukkitRunnable() {
            private int countdown = 5;

            public void run() {
               if (this.countdown > 0) {
                  for (Player player : onlinePlayers) {
                     if (player.isOnline()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F + 0.2F * (5 - this.countdown));
                        Location loc = player.getLocation();
                        loc.getWorld().spawnParticle(Particle.PORTAL, loc.add(0.0, 1.0, 0.0), 30, 0.5, 1.0, 0.5, 0.5);
                     }
                  }

                  this.countdown--;
               } else {
                  owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0F, 0.5F);
                  Map<Player, Location> originalLocations = new HashMap<>();
                  List<Player> validPlayers = new ArrayList<>();

                  for (Player playerx : onlinePlayers) {
                     if (playerx.isOnline()) {
                        originalLocations.put(playerx, playerx.getLocation().clone());
                        validPlayers.add(playerx);
                     }
                  }

                  Collections.shuffle(validPlayers);
                  List<Location> locations = new ArrayList<>(originalLocations.values());

                  for (int i = 0; i < validPlayers.size(); i++) {
                     Player playerxx = validPlayers.get(i);
                     Location newLocation = locations.get((i + 1) % locations.size());
                     playerxx.teleport(newLocation);
                     playerxx.playSound(playerxx.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 0.5F);
                     Location particleLoc = playerxx.getLocation().clone();
                     playerxx.getWorld().spawnParticle(Particle.PORTAL, particleLoc.add(0.0, 1.0, 0.0), 100, 1.0, 1.0, 1.0, 1.0);

                     try {
                        playerxx.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 50, 1.0, 1.0, 1.0, 0.1, 0.8F);
                     } catch (IllegalArgumentException var9) {
                        playerxx.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 50, 1.0, 1.0, 1.0, 0.1);
                     }

                     playerxx.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 60, 1.0, 1.0, 1.0, 0.5);
                     if (!playerxx.equals(owner)) {
                        playerxx.damage(15.0);
                     }
                  }

                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 0L, 20L);
      }
   }

   public boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownEnd = this.cooldowns.get(playerId);
         long currentTime = System.currentTimeMillis();
         if (currentTime >= cooldownEnd) {
            this.cooldowns.remove(playerId);
            return false;
         } else {
            return true;
         }
      }
   }

   public long getRemainingCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownEnd = this.cooldowns.get(playerId);
         long currentTime = System.currentTimeMillis();
         long remainingMs = cooldownEnd - currentTime;
         return Math.max(0L, remainingMs / 1000L);
      }
   }

   public void setCooldown(Player player) {
      int cooldownTime = this.reworkConfig.getInt("awakened_dragon_katana.swap_cooldown", 60);
      this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime * 1000L);
   }

   public void removePlayerData(UUID playerId) {
      this.cooldowns.remove(playerId);
      this.teleportCooldowns.remove(playerId);
      this.lastTeleportLocations.remove(playerId);
   }
}
