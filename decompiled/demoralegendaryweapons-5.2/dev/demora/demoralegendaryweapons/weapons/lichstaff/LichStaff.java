package dev.demora.demoralegendaryweapons.weapons.lichstaff;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class LichStaff {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey lichStaffKey;
   private final Map<UUID, Integer> ammoCount;
   private final Map<UUID, Long> cooldowns;
   private final Set<Material> nonReplaceableBlocks;

   public LichStaff(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.lichStaffKey = new NamespacedKey(plugin, "lich_staff");
      this.ammoCount = new HashMap<>();
      this.cooldowns = new HashMap<>();
      this.nonReplaceableBlocks = new HashSet<>();
      this.initNonReplaceableBlocks();
   }

   private void initNonReplaceableBlocks() {
      this.nonReplaceableBlocks
         .addAll(
            Arrays.asList(
               Material.BEDROCK,
               Material.BARRIER,
               Material.COMMAND_BLOCK,
               Material.CHAIN_COMMAND_BLOCK,
               Material.REPEATING_COMMAND_BLOCK,
               Material.STRUCTURE_BLOCK,
               Material.JIGSAW,
               Material.END_PORTAL,
               Material.END_PORTAL_FRAME,
               Material.END_GATEWAY,
               Material.NETHER_PORTAL,
               Material.RESPAWN_ANCHOR,
               Material.LIGHT,
               Material.STRUCTURE_VOID,
               Material.OBSIDIAN,
               Material.CRYING_OBSIDIAN
            )
         );
   }

   public ItemStack createLichStaff() {
      ItemStack item = new ItemStack(Material.WARPED_FUNGUS_ON_A_STICK);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("lich_staff");
         if (weaponName.isEmpty()) {
            weaponName = "§9§lПосох Лича";
         }

         meta.setDisplayName(weaponName);
         meta.setCustomModelData(2);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.lichStaffKey, PersistentDataType.STRING, "lich_staff");
         this.plugin.applyWeaponLore(meta, "lich_staff");
         item.setItemMeta(meta);
      }

      return item;
   }

   public boolean isLichStaff(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.lichStaffKey, PersistentDataType.STRING);
      } else {
         return false;
      }
   }

   public int getAmmo(Player player) {
      int defaultAmmo = this.plugin.getConfig().getInt("weapons.lich_staff.ammo_count", 4);
      return this.ammoCount.getOrDefault(player.getUniqueId(), defaultAmmo);
   }

   public int getMaxAmmo() {
      return this.plugin.getConfig().getInt("weapons.lich_staff.ammo_count", 4);
   }

   public void setAmmo(Player player, int ammo) {
      this.ammoCount.put(player.getUniqueId(), ammo);
   }

   public boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return false;
      } else {
         long lastUse = this.cooldowns.get(playerId);
         long currentTime = System.currentTimeMillis();
         long cooldownDuration = this.plugin.getConfig().getInt("weapons.lich_staff.cooldown_seconds", 30) * 1000L;
         return currentTime - lastUse < cooldownDuration;
      }
   }

   public void setCooldown(Player player) {
      this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
   }

   public long getRemainingCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long lastUse = this.cooldowns.get(playerId);
         long currentTime = System.currentTimeMillis();
         long cooldownDuration = this.plugin.getConfig().getInt("weapons.lich_staff.cooldown_seconds", 30) * 1000L;
         long remaining = cooldownDuration - (currentTime - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }

   public void updateActionBar(Player player) {
      int ammo = this.getAmmo(player);
      int maxAmmo = this.getMaxAmmo();
      StringBuilder actionBar = new StringBuilder();

      for (int i = 0; i < maxAmmo; i++) {
         if (i < ammo) {
            actionBar.append("\ue033");
         } else {
            actionBar.append("\ue034");
         }
      }

      if (this.isOnCooldown(player)) {
         long remaining = this.getRemainingCooldown(player);
         actionBar.append(" §c").append(remaining).append("s");
      }

      player.sendActionBar(actionBar.toString());
   }

   public boolean canShoot(Player player) {
      return this.getAmmo(player) > 0 && !this.isOnCooldown(player);
   }

   public void shoot(Player player) {
      if (this.canShoot(player)) {
         int currentAmmo = this.getAmmo(player);
         this.setAmmo(player, currentAmmo - 1);
         final Location eyeLoc = player.getEyeLocation();
         final Vector direction = eyeLoc.getDirection().normalize();
         final ItemDisplay projectile = (ItemDisplay)player.getWorld().spawnEntity(eyeLoc, EntityType.ITEM_DISPLAY);
         ItemStack displayItem = new ItemStack(Material.SNOWBALL);
         ItemMeta displayMeta = displayItem.getItemMeta();
         displayMeta.setCustomModelData(6);
         displayItem.setItemMeta(displayMeta);
         projectile.setItemStack(displayItem);
         player.getWorld().playSound(eyeLoc, Sound.ENTITY_SNOWBALL_THROW, 1.0F, 0.8F);
         (new BukkitRunnable() {
            private Location currentLoc = eyeLoc.clone();
            private int ticks = 0;
            private final int maxTicks = 200;
            private final double speed = 2.4;

            public void run() {
               if (this.ticks < 200 && !projectile.isDead() && projectile.isValid()) {
                  this.currentLoc.add(direction.clone().multiply(2.4));
                  projectile.teleport(this.currentLoc);
                  this.currentLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, this.currentLoc, 3, 0.1, 0.1, 0.1, 0.01);

                  for (Entity entity : this.currentLoc.getWorld().getNearbyEntities(this.currentLoc, 0.5, 0.5, 0.5)) {
                     if (entity instanceof LivingEntity && entity != player && entity != projectile) {
                        LichStaff.this.handleImpact(this.currentLoc, player);
                        if (!projectile.isDead()) {
                           projectile.remove();
                        }

                        this.cancel();
                        return;
                     }
                  }

                  Block block = this.currentLoc.getBlock();
                  if (!block.getType().isAir() && block.getType().isSolid()) {
                     LichStaff.this.handleImpact(this.currentLoc, player);
                     if (!projectile.isDead()) {
                        projectile.remove();
                     }

                     this.cancel();
                  } else {
                     this.ticks++;
                  }
               } else {
                  if (!projectile.isDead()) {
                     projectile.remove();
                  }

                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 0L, 1L);
         if (this.getAmmo(player) == 0) {
            this.setCooldown(player);
         }
      }
   }

   private void handleImpact(Location location, Player shooter) {
      World world = location.getWorld();
      if (world != null) {
         world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.5F);
         world.playSound(location, Sound.BLOCK_GLASS_BREAK, 1.0F, 0.9F);
         world.spawnParticle(Particle.EXPLOSION, location, 1);
         world.spawnParticle(Particle.SNOWFLAKE, location, 50, 2.5, 2.5, 2.5, 0.1);
         this.freezeBlocks(location);
         this.knockbackEntities(location, shooter);
      }
   }

   private void freezeBlocks(Location center) {
      World world = center.getWorld();
      if (world != null) {
         int radius = 5;
         int centerX = center.getBlockX();
         int centerY = center.getBlockY();
         int centerZ = center.getBlockZ();

         for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = Math.max(world.getMinHeight(), centerY - radius); y <= Math.min(world.getMaxHeight() - 1, centerY + radius); y++) {
               for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                  double distance = Math.sqrt(Math.pow(x - centerX, 2.0) + Math.pow(y - centerY, 2.0) + Math.pow(z - centerZ, 2.0));
                  if (distance <= radius) {
                     Block block = world.getBlockAt(x, y, z);
                     if (this.shouldFreezeBlock(block) && !this.plugin.isRegionProtected(block.getLocation())) {
                        block.setType(Material.ICE);
                        world.spawnParticle(Particle.SNOWFLAKE, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0.0);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean shouldFreezeBlock(Block block) {
      Material type = block.getType();
      return !type.isAir() && !this.nonReplaceableBlocks.contains(type) && type != Material.ICE && type != Material.PACKED_ICE && type != Material.BLUE_ICE;
   }

   private void knockbackEntities(Location center, Player shooter) {
      for (Entity entity : center.getWorld().getNearbyEntities(center, 5.0, 5.0, 5.0)) {
         if (entity instanceof LivingEntity) {
            Vector knockback = entity.getLocation().toVector().subtract(center.toVector()).normalize();
            knockback.setY(Math.max(0.3, knockback.getY()));
            knockback.multiply(1.5);
            entity.setVelocity(knockback);
            if (entity != shooter && entity instanceof LivingEntity) {
               double explosionDamage = this.plugin.getConfig().getDouble("weapons.lich_staff.explosion_damage", 6.0);
               ((LivingEntity)entity).damage(explosionDamage, shooter);
            }
         }
      }
   }

   public void reloadAmmo(Player player) {
      int maxAmmo = this.getMaxAmmo();
      this.setAmmo(player, maxAmmo);
      this.cooldowns.remove(player.getUniqueId());
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, 1.0F, 1.2F);
   }
}
