package dev.demora.demoralegendaryweapons.weapons.sculkcrossbow;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SculkCrossbow implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final NamespacedKey sculkCrossbowKey;
   private final Map<UUID, Long> cooldowns;
   private final Set<Material> nonReplaceableBlocks;
   private final List<Material> sculkBlocks;
   private final Random random;

   public SculkCrossbow(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.sculkCrossbowKey = new NamespacedKey(plugin, "sculk_crossbow");
      this.cooldowns = new HashMap<>();
      this.nonReplaceableBlocks = new HashSet<>();
      this.sculkBlocks = new ArrayList<>();
      this.random = new Random();
      this.initNonReplaceableBlocks();
      this.initSculkBlocks();
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

   private void initSculkBlocks() {
      this.sculkBlocks.add(Material.SCULK);
      this.sculkBlocks.add(Material.SCULK_VEIN);
   }

   public ItemStack createSculkCrossbow() {
      ItemStack crossbow = new ItemStack(Material.CROSSBOW);
      ItemMeta meta = crossbow.getItemMeta();
      if (meta != null) {
         String weaponName = this.plugin.getLanguageManager().getWeaponName("sculk_crossbow");
         if (weaponName.isEmpty()) {
            weaponName = "§3§lСкалковый Арбалет";
         }

         meta.displayName(LegacyComponentSerializer.legacySection().deserialize(weaponName));
         meta.setCustomModelData(1);
         meta.addEnchant(Enchantment.QUICK_CHARGE, 3, true);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.setUnbreakable(true);
         meta.getPersistentDataContainer().set(this.sculkCrossbowKey, PersistentDataType.BYTE, (byte)1);
         this.plugin.applyWeaponLore(meta, "sculk_crossbow");
         crossbow.setItemMeta(meta);
      }

      return crossbow;
   }

   public boolean isSculkCrossbow(ItemStack item) {
      if (item != null && item.getType() == Material.CROSSBOW) {
         ItemMeta meta = item.getItemMeta();
         return meta == null ? false : meta.getPersistentDataContainer().has(this.sculkCrossbowKey, PersistentDataType.BYTE);
      } else {
         return false;
      }
   }

   public boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.sculk_crossbow.cooldown") * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         return System.currentTimeMillis() - lastUse < cooldownTime;
      }
   }

   public int getRemainingCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return 0;
      } else {
         long cooldownTime = this.plugin.getConfig().getInt("weapons.sculk_crossbow.cooldown") * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
         return remaining > 0L ? (int)(remaining / 1000L) + 1 : 0;
      }
   }

   public void setCooldown(Player player) {
      this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
   }

   private void launchSculkProjectile(Player shooter) {
      final Location eyeLoc = shooter.getEyeLocation();
      final Vector direction = eyeLoc.getDirection().normalize();
      (new BukkitRunnable() {
         private Location currentLoc = eyeLoc.clone();
         private int ticks = 0;
         private final int maxTicks = 100;
         private final double speed = 1.5;

         public void run() {
            if (this.ticks >= 100) {
               this.cancel();
            } else {
               this.currentLoc.add(direction.clone().multiply(1.5));
               World world = this.currentLoc.getWorld();
               if (world == null) {
                  this.cancel();
               } else {
                  if (SculkCrossbow.this.plugin.getConfig().getBoolean("weapons.sculk_crossbow.particles.warden-particles")) {
                     int particlesPerTick = SculkCrossbow.this.plugin.getConfig().getInt("weapons.sculk_crossbow.particles.particles-per-tick");
                     world.spawnParticle(Particle.SONIC_BOOM, this.currentLoc, 1, 0.0, 0.0, 0.0, 0.0);
                     world.spawnParticle(Particle.SCULK_SOUL, this.currentLoc, particlesPerTick, 0.2, 0.2, 0.2, 0.02);
                     world.spawnParticle(Particle.SCULK_CHARGE_POP, this.currentLoc, particlesPerTick / 2, 0.1, 0.1, 0.1, 0.0);
                  }

                  Iterator var4 = world.getNearbyLivingEntities(this.currentLoc, 0.5, entityx -> !entityx.equals(shooter)).iterator();
                  if (var4.hasNext()) {
                     LivingEntity entity = (LivingEntity)var4.next();
                     SculkCrossbow.this.handleExplosionAndSpread(this.currentLoc, shooter);
                     this.cancel();
                  } else {
                     Block block = this.currentLoc.getBlock();
                     if (block.getType() != Material.AIR && block.getType().isSolid()) {
                        SculkCrossbow.this.handleExplosionAndSpread(this.currentLoc, shooter);
                        this.cancel();
                     } else {
                        this.ticks++;
                     }
                  }
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   public void handleExplosionAndSpread(Location location, Player shooter) {
      this.createExplosion(location);
      (new BukkitRunnable() {
         public void run() {
            SculkCrossbow.this.startSculkSpread(location);
         }
      }).runTaskLater(this.plugin, 20L);
   }

   private void createExplosion(Location location) {
      World world = location.getWorld();
      if (world != null) {
         float power = (float)this.plugin.getConfig().getDouble("weapons.sculk_crossbow.explosion.power");
         boolean breakBlocks = this.plugin.getConfig().getBoolean("weapons.sculk_crossbow.explosion.break-blocks");
         boolean setFire = this.plugin.getConfig().getBoolean("weapons.sculk_crossbow.explosion.set-fire");
         world.createExplosion(location, power, setFire, breakBlocks && !this.plugin.isRegionProtected(location));
         world.spawnParticle(Particle.SONIC_BOOM, location, 3, 1.0, 1.0, 1.0, 0.0);
         world.spawnParticle(Particle.SCULK_SOUL, location, 20, 2.0, 2.0, 2.0, 0.1);
         world.playSound(location, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0F, 0.8F);
         world.playSound(location, Sound.BLOCK_SCULK_CATALYST_BLOOM, 1.0F, 1.0F);
      }
   }

   private void startSculkSpread(Location centerLocation) {
      int radius = this.plugin.getConfig().getInt("weapons.sculk_crossbow.sculk-spread.radius");
      final int blocksPerTick = this.plugin.getConfig().getInt("weapons.sculk_crossbow.sculk-spread.blocks-per-tick");
      int tickDelay = this.plugin.getConfig().getInt("weapons.sculk_crossbow.sculk-spread.tick-delay");
      final double replaceChance = this.plugin.getConfig().getDouble("weapons.sculk_crossbow.sculk-spread.replace-chance");
      final List<Block> blocksToProcess = this.getBlocksInRadius(centerLocation, radius);
      Collections.shuffle(blocksToProcess);
      (new BukkitRunnable() {
         private int processedBlocks = 0;

         public void run() {
            int processed = 0;

            while (processed < blocksPerTick && this.processedBlocks < blocksToProcess.size()) {
               Block block = blocksToProcess.get(this.processedBlocks);
               this.processedBlocks++;
               if (SculkCrossbow.this.shouldReplaceBlock(block, replaceChance)) {
                  SculkCrossbow.this.replaceBlockWithSculk(block);
                  processed++;
               }
            }

            if (this.processedBlocks >= blocksToProcess.size()) {
               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 0L, tickDelay);
   }

   private List<Block> getBlocksInRadius(Location center, int radius) {
      List<Block> blocks = new ArrayList<>();
      World world = center.getWorld();
      if (world == null) {
         return blocks;
      } else {
         int centerX = center.getBlockX();
         int centerY = center.getBlockY();
         int centerZ = center.getBlockZ();

         for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = Math.max(world.getMinHeight(), centerY - radius); y <= Math.min(world.getMaxHeight() - 1, centerY + radius); y++) {
               for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                  double distance = Math.sqrt(Math.pow(x - centerX, 2.0) + Math.pow(y - centerY, 2.0) + Math.pow(z - centerZ, 2.0));
                  if (distance <= radius) {
                     blocks.add(world.getBlockAt(x, y, z));
                  }
               }
            }
         }

         return blocks;
      }
   }

   private boolean shouldReplaceBlock(Block block, double chance) {
      Material type = block.getType();
      return !type.isAir() && !this.nonReplaceableBlocks.contains(type) ? this.random.nextDouble() < chance : false;
   }

   private void replaceBlockWithSculk(Block block) {
      if (!this.plugin.isRegionProtected(block.getLocation())) {
         Material sculkType = this.getRandomSculkBlock(block);
         block.setType(sculkType);
         Location loc = block.getLocation().add(0.5, 0.5, 0.5);
         World world = loc.getWorld();
         if (world != null) {
            world.spawnParticle(Particle.SCULK_CHARGE_POP, loc, 5, 0.3, 0.3, 0.3, 0.0);
            world.playSound(loc, Sound.BLOCK_SCULK_PLACE, 0.5F, 1.0F + (this.random.nextFloat() * 0.4F - 0.2F));
         }
      }
   }

   private Material getRandomSculkBlock(Block block) {
      boolean isSurface = block.getRelative(0, 1, 0).getType() == Material.AIR;
      int rand = this.random.nextInt(100);
      if (isSurface) {
         return rand < 70 ? Material.SCULK : Material.SCULK_VEIN;
      } else {
         return Material.SCULK;
      }
   }

   @EventHandler
   public void onEntityShootBow(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player) {
         Player player = (Player)event.getEntity();
         ItemStack bow = event.getBow();
         if (this.isSculkCrossbow(bow)) {
            if (this.isOnCooldown(player)) {
               int remaining = this.getRemainingCooldown(player);
               player.sendMessage(
                  DemoraLegendaryWeapons.getInstance()
                     .getMessageWithPrefix("weapon-messages.sculk-crossbow-cooldown-chat")
                     .replace("{time}", String.valueOf(remaining))
               );
               player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 0.8F);
               event.setCancelled(true);
            } else {
               event.setCancelled(true);
               this.setCooldown(player);
               Location shooterLoc = player.getLocation();
               if (shooterLoc.getWorld() != null) {
                  shooterLoc.getWorld().playSound(shooterLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0F, 0.8F);
               }

               this.launchSculkProjectile(player);
            }
         }
      }
   }
}
