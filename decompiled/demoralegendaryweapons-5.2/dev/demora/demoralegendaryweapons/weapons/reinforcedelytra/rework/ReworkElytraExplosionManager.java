package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ReworkElytraExplosionManager {
   private static final Set<Material> NON_REPLACEABLE = Set.of(
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
   );
   private final DemoraLegendaryWeapons plugin;
   private final ReworkElytra reworkElytra;
   private final Set<UUID> immuneToLandingExplosion = new HashSet<>();
   private final Set<UUID> bleedingEntities = new HashSet<>();
   private final Random random = new Random();

   public ReworkElytraExplosionManager(DemoraLegendaryWeapons plugin, ReworkElytra reworkElytra) {
      this.plugin = plugin;
      this.reworkElytra = reworkElytra;
   }

   public void triggerExplosiveLanding(Player player, double maxSpeed) {
      if (!this.reworkElytra.isOnExplosionLandingCooldown(player.getUniqueId())) {
         double minSpeed = this.reworkElytra.getMinTriggerSpeed();
         if (!(maxSpeed < minSpeed)) {
            double maxSpeedConfig = this.reworkElytra.getMaxSpeedForExplosion();
            float minExplosion = this.reworkElytra.getMinExplosionPower();
            float maxExplosion = this.reworkElytra.getMaxExplosionPower();
            double t = Math.min(1.0, (maxSpeed - minSpeed) / (maxSpeedConfig - minSpeed));
            float explosionPower = (float)(minExplosion + t * (maxExplosion - minExplosion));
            boolean breakBlocks = this.reworkElytra.isExplosionBreakBlocks();
            boolean setFire = this.reworkElytra.isExplosionSetFire();
            boolean damagePlayer = this.reworkElytra.isDamagePlayer();
            Location loc = player.getLocation();
            if (!damagePlayer) {
               this.immuneToLandingExplosion.add(player.getUniqueId());
            }

            this.reworkElytra.setExplosionLandingCooldown(player.getUniqueId());
            loc.getWorld().createExplosion(loc, explosionPower, setFire, breakBlocks && !this.plugin.isRegionProtected(loc), player);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.immuneToLandingExplosion.remove(player.getUniqueId()), 3L);
            double radius = explosionPower * 2.5;
            List<LivingEntity> damaged = new ArrayList<>();

            for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
               if (entity instanceof LivingEntity && entity != player) {
                  LivingEntity target = (LivingEntity)entity;
                  double dist = entity.getLocation().distance(loc);
                  double damage = explosionPower * 2.0 * Math.max(0.0, 1.0 - dist / radius);
                  if (damage > 0.5) {
                     target.setNoDamageTicks(0);
                     target.damage(damage, player);
                     damaged.add(target);
                  }
               }
            }

            for (LivingEntity victim : damaged) {
               this.applyBleeding(victim, player);
            }

            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5F, 0.7F);
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.2F);
            this.spawnBloodyExplosionParticles(loc, explosionPower);
            this.startNetherWartSpread(loc);
         }
      }
   }

   private void applyBleeding(LivingEntity target, Player source) {
      final UUID id = target.getUniqueId();
      if (!this.bleedingEntities.contains(id)) {
         this.bleedingEntities.add(id);
         final int duration = this.reworkElytra.getBleedDurationTicks();
         final double damage = this.reworkElytra.getBleedDamage();
         if (duration <= 0) {
            this.bleedingEntities.remove(id);
         } else {
            (new BukkitRunnable() {
               private int ticks = 0;

               public void run() {
                  if (target.isValid() && !target.isDead()) {
                     Location l = target.getLocation().add(0.0, 1.0, 0.0);
                     target.getWorld().spawnParticle(Particle.DUST, l, 20, 0.4, 0.6, 0.4, 0.0, new DustOptions(Color.fromRGB(180, 0, 0), 1.2F));
                     target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, l, 5, 0.3, 0.5, 0.3, 0.0);
                     target.damage(damage, source);
                     this.ticks += 20;
                     if (this.ticks >= duration) {
                        ReworkElytraExplosionManager.this.bleedingEntities.remove(id);
                        this.cancel();
                     }
                  } else {
                     ReworkElytraExplosionManager.this.bleedingEntities.remove(id);
                     this.cancel();
                  }
               }
            }).runTaskTimer(this.plugin, 0L, 20L);
         }
      }
   }

   private void spawnBloodyExplosionParticles(Location loc, float explosionPower) {
      DustOptions redDust = new DustOptions(Color.fromRGB(180, 0, 0), 1.5F);
      DustOptions darkRedDust = new DustOptions(Color.fromRGB(100, 0, 0), 1.4F);
      DustOptions crimsonDust = new DustOptions(Color.fromRGB(220, 20, 60), 1.2F);
      double particleRadius = explosionPower * 4.0;
      int particleCount = (int)(explosionPower * 120.0F);
      World world = loc.getWorld();
      if (world != null) {
         for (int i = 0; i < particleCount; i++) {
            double angle = Math.random() * Math.PI * 2.0;
            double verticalAngle = Math.random() * Math.PI;
            double distance = Math.random() * particleRadius;
            double x = Math.sin(verticalAngle) * Math.cos(angle) * distance;
            double y = Math.cos(verticalAngle) * distance;
            double z = Math.sin(verticalAngle) * Math.sin(angle) * distance;
            Location particleLoc = loc.clone().add(x, y, z);
            double rand = Math.random();
            if (rand < 0.35) {
               world.spawnParticle(Particle.DUST, particleLoc, 3, 0.15, 0.15, 0.15, 0.0, redDust);
            } else if (rand < 0.65) {
               world.spawnParticle(Particle.DUST, particleLoc, 3, 0.15, 0.15, 0.15, 0.0, darkRedDust);
            } else {
               world.spawnParticle(Particle.DUST, particleLoc, 2, 0.12, 0.12, 0.12, 0.0, crimsonDust);
            }
         }

         world.spawnParticle(Particle.EXPLOSION, loc, 10, 1.0, 1.0, 1.0, 0.1);
         world.spawnParticle(Particle.DAMAGE_INDICATOR, loc, 30, 1.5, 1.5, 1.5, 0.1);
         world.spawnParticle(Particle.DUST, loc, 50, 1.2, 1.2, 1.2, 0.0, redDust);
         world.spawnParticle(Particle.DUST, loc, 40, 1.0, 1.0, 1.0, 0.0, darkRedDust);
      }
   }

   private void startNetherWartSpread(Location center) {
      int radius = this.reworkElytra.getNetherWartSpreadRadius();
      final int blocksPerTick = this.reworkElytra.getNetherWartBlocksPerTick();
      int tickDelay = this.reworkElytra.getNetherWartTickDelay();
      final double replaceChance = this.reworkElytra.getNetherWartReplaceChance();
      final List<Block> blocksToProcess = this.getBlocksInRadius(center, radius);
      Collections.shuffle(blocksToProcess);
      (new BukkitRunnable() {
            private int processedBlocks = 0;

            public void run() {
               int processed = 0;

               while (processed < blocksPerTick && this.processedBlocks < blocksToProcess.size()) {
                  Block block = blocksToProcess.get(this.processedBlocks);
                  this.processedBlocks++;
                  if (ReworkElytraExplosionManager.this.shouldReplace(block, replaceChance)
                     && !ReworkElytraExplosionManager.this.plugin.isRegionProtected(block.getLocation())) {
                     block.setType(Material.NETHER_WART_BLOCK);
                     Location blockLoc = block.getLocation().add(0.5, 0.5, 0.5);
                     World w = blockLoc.getWorld();
                     if (w != null) {
                        w.spawnParticle(Particle.BLOCK, blockLoc, 4, 0.2, 0.2, 0.2, 0.0, Material.NETHER_WART_BLOCK.createBlockData());
                        w.playSound(blockLoc, Sound.BLOCK_SLIME_BLOCK_PLACE, 0.5F, 1.0F);
                     }

                     processed++;
                  }
               }

               if (this.processedBlocks >= blocksToProcess.size()) {
                  this.cancel();
               }
            }
         })
         .runTaskTimer(this.plugin, 20L, tickDelay);
   }

   private List<Block> getBlocksInRadius(Location center, int radius) {
      World world = center.getWorld();
      if (world == null) {
         return Collections.emptyList();
      } else {
         List<Block> blocks = new ArrayList<>();
         int cx = center.getBlockX();
         int cy = center.getBlockY();
         int cz = center.getBlockZ();

         for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = Math.max(world.getMinHeight(), cy - radius); y <= Math.min(world.getMaxHeight() - 1, cy + radius); y++) {
               for (int z = cz - radius; z <= cz + radius; z++) {
                  double dist = Math.sqrt(Math.pow(x - cx, 2.0) + Math.pow(y - cy, 2.0) + Math.pow(z - cz, 2.0));
                  if (dist <= radius) {
                     blocks.add(world.getBlockAt(x, y, z));
                  }
               }
            }
         }

         return blocks;
      }
   }

   private boolean shouldReplace(Block block, double chance) {
      Material type = block.getType();
      return !type.isAir() && !NON_REPLACEABLE.contains(type) ? this.random.nextDouble() < chance : false;
   }

   public boolean isImmuneToLandingExplosion(UUID playerId) {
      return this.immuneToLandingExplosion.contains(playerId);
   }

   public void cleanupPlayer(UUID playerId) {
      this.immuneToLandingExplosion.remove(playerId);
   }
}
