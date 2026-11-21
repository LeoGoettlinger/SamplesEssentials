package dev.demora.demoralegendaryweapons.weapons.sculkweaverslantern;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

public class SculkweaversLanternListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final SculkweaversLantern lantern;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private final Map<UUID, SculkweaversLanternListener.SculkSphereData> activeSpheres = new HashMap<>();
   private final Map<UUID, UUID> sphereMobOwners = new HashMap<>();

   public SculkweaversLanternListener(DemoraLegendaryWeapons plugin, SculkweaversLantern lantern) {
      this.plugin = plugin;
      this.lantern = lantern;
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onSphereMobTarget(EntityTargetLivingEntityEvent event) {
      if (event.getEntity() instanceof Mob mob) {
         if (event.getTarget() instanceof Player target) {
            UUID ownerId = this.sphereMobOwners.get(mob.getUniqueId());
            if (ownerId != null) {
               if (ownerId.equals(target.getUniqueId())) {
                  event.setCancelled(true);
                  mob.setTarget(null);
               }
            }
         }
      }
   }

   @EventHandler
   public void onFish(PlayerFishEvent event) {
      Player player = event.getPlayer();
      ItemStack rod = player.getInventory().getItemInMainHand();
      if (this.lantern.isSculkweaversLantern(rod)) {
         if (event.getState() == State.FISHING) {
            event.setCancelled(true);
            if (this.isOnCooldown(player)) {
               long remaining = this.getRemainingCooldown(player);
               player.sendMessage(
                  this.plugin.getMessageWithPrefix("weapon-messages.sculkweavers-lantern-cooldown").replace("{time}", String.valueOf(remaining))
               );
            } else if (this.activeSpheres.containsKey(player.getUniqueId())) {
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.sculkweavers-lantern-active"));
            } else {
               this.setCooldown(player);
               this.launchLantern(player);
            }
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();
      this.cooldowns.remove(playerId);
      SculkweaversLanternListener.SculkSphereData sphereData = this.activeSpheres.remove(playerId);
      if (sphereData != null) {
         this.cleanupSphereAssociations(sphereData);
         sphereData.cleanup();
      }
   }

   private void cleanupSphereAssociations(SculkweaversLanternListener.SculkSphereData sphereData) {
      if (sphereData.retargetTask != null) {
         sphereData.retargetTask.cancel();
         sphereData.retargetTask = null;
      }

      for (Warden w : sphereData.wardens) {
         if (w != null) {
            this.sphereMobOwners.remove(w.getUniqueId());
            this.removeFromOwnerTeam(sphereData, w);
         }
      }

      for (Enderman e : sphereData.endermen) {
         if (e != null) {
            this.sphereMobOwners.remove(e.getUniqueId());
            this.removeFromOwnerTeam(sphereData, e);
         }
      }

      this.cleanupOwnerTeam(sphereData);
   }

   private void launchLantern(Player player) {
      final Location eyeLoc = player.getEyeLocation();
      final Vector direction = eyeLoc.getDirection().normalize();
      final ItemDisplay lanternDisplay = (ItemDisplay)player.getWorld().spawnEntity(eyeLoc, EntityType.ITEM_DISPLAY);
      ItemStack displayItem = new ItemStack(Material.RECOVERY_COMPASS);
      ItemMeta displayMeta = displayItem.getItemMeta();
      if (displayMeta != null) {
         displayMeta.setCustomModelData(2);
         displayItem.setItemMeta(displayMeta);
      }

      lanternDisplay.setItemStack(displayItem);

      try {
         lanternDisplay.setInterpolationDelay(0);
         lanternDisplay.setInterpolationDuration(2);
         lanternDisplay.setTeleportDuration(2);
      } catch (NoSuchMethodError var10) {
      }

      player.getWorld().playSound(eyeLoc, Sound.ENTITY_FISHING_BOBBER_THROW, 1.0F, 0.8F);
      final double flightSpeed = this.plugin.getConfig().getDouble("weapons.sculkweavers_lantern.flight_speed", 1.2);
      final int maxFlightTicks = this.plugin.getConfig().getInt("weapons.sculkweavers_lantern.max_flight_ticks", 60);
      (new BukkitRunnable() {
         private Location currentLoc = eyeLoc.clone();
         private int ticks = 0;
         private Vector velocity = direction.clone().multiply(flightSpeed);

         public void run() {
            if (!lanternDisplay.isDead() && lanternDisplay.isValid() && this.ticks < maxFlightTicks) {
               this.velocity.setY(this.velocity.getY() - 0.03);
               this.currentLoc.add(this.velocity);
               lanternDisplay.teleport(this.currentLoc);
               this.currentLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, this.currentLoc, 2, 0.1, 0.1, 0.1, 0.01);
               if (!this.currentLoc.getBlock().isPassable()) {
                  SculkweaversLanternListener.this.landLantern(this.currentLoc, player, lanternDisplay);
                  this.cancel();
               } else {
                  this.ticks++;
               }
            } else {
               SculkweaversLanternListener.this.landLantern(this.currentLoc, player, lanternDisplay);
               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   private void landLantern(Location landLoc, Player player, ItemDisplay lanternDisplay) {
      lanternDisplay.remove();
      Location groundLoc = landLoc.clone();

      while (groundLoc.getY() > landLoc.getWorld().getMinHeight() && !groundLoc.getBlock().getType().isSolid()) {
         groundLoc.subtract(0.0, 1.0, 0.0);
      }

      groundLoc.add(0.0, 1.0, 0.0);
      groundLoc.getWorld().playSound(groundLoc, Sound.BLOCK_SCULK_CATALYST_BLOOM, 2.0F, 0.5F);
      groundLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, groundLoc, 50, 2.0, 2.0, 2.0, 0.1);
      this.createSculkSphere(groundLoc, player);
   }

   private static int clampInt(int v, int min, int max) {
      return Math.max(min, Math.min(max, v));
   }

   private int computeBuildDurationTicks(int totalBlocks) {
      int speed = clampInt(this.plugin.getConfig().getInt("weapons.sculkweavers_lantern.build_speed_level", 50), 1, 100);
      int minTicks = 5;
      int maxTicks = 200;
      int duration = (int)Math.round(maxTicks - (speed - 1) * (maxTicks - minTicks) / 99.0);
      duration = clampInt(duration, minTicks, maxTicks);
      return Math.min(duration, Math.max(1, totalBlocks));
   }

   private void createSculkSphere(Location center, Player owner) {
      int radius = this.plugin.getConfig().getInt("weapons.sculkweavers_lantern.sphere_radius", 12);
      final int sphereLifetimeSeconds = this.plugin.getConfig().getInt("weapons.sculkweavers_lantern.sphere_lifetime_seconds", 10);
      final SculkweaversLanternListener.SculkSphereData sphereData = new SculkweaversLanternListener.SculkSphereData(center, radius, owner);
      this.activeSpheres.put(owner.getUniqueId(), sphereData);
      this.ensureOwnerTeam(sphereData);
      final List<Location> appearOrder = this.buildHollowSphereCurvedOrder(center, radius);
      if (appearOrder.isEmpty()) {
         this.spawnWardens(sphereData);
         this.spawnEndermen(sphereData);
         this.startMobRetargetTask(sphereData);
         this.startSphereLifetime(sphereData, sphereLifetimeSeconds);
      } else {
         final int totalBlocks = appearOrder.size();
         int buildDurationTicks = this.computeBuildDurationTicks(totalBlocks);
         final int blocksPerTick = Math.max(1, (totalBlocks + buildDurationTicks - 1) / buildDurationTicks);
         (new BukkitRunnable() {
            private int index = 0;

            public void run() {
               World world = sphereData.center.getWorld();

               for (int stepped = 0; stepped < blocksPerTick && this.index < totalBlocks; stepped++) {
                  SculkweaversLanternListener.this.tryPlaceSculkBlock(sphereData, world, appearOrder.get(this.index++));
               }

               if (this.index >= totalBlocks) {
                  SculkweaversLanternListener.this.spawnWardens(sphereData);
                  SculkweaversLanternListener.this.spawnEndermen(sphereData);
                  SculkweaversLanternListener.this.startMobRetargetTask(sphereData);
                  SculkweaversLanternListener.this.startSphereLifetime(sphereData, sphereLifetimeSeconds);
                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 5L, 1L);
      }
   }

   private List<Location> buildHollowSphereCurvedOrder(Location center, int radius) {
      World world = center.getWorld();
      List<Location> shell = new ArrayList<>();

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               double distance = Math.sqrt(x * x + y * y + z * z);
               if (distance >= radius - 0.5 && distance <= radius + 0.5) {
                  Location blockLoc = center.clone().add(x, y, z);
                  if (blockLoc.getY() >= world.getMinHeight() && blockLoc.getY() <= world.getMaxHeight()) {
                     shell.add(blockLoc);
                  }
               }
            }
         }
      }

      shell.sort(Comparator.comparingDouble(loc -> curvedAppearKey(center, loc)));
      return shell;
   }

   private static double curvedAppearKey(Location center, Location blockLoc) {
      int dx = blockLoc.getBlockX() - center.getBlockX();
      int dy = blockLoc.getBlockY() - center.getBlockY();
      int dz = blockLoc.getBlockZ() - center.getBlockZ();
      double angle = Math.atan2(dz, dx);
      double wave1 = 1.4 * Math.sin(angle * 2.6 + dy * 0.42);
      double wave2 = 0.65 * Math.cos(angle * 1.45 - dy * 0.52 + (dx + dz) * 0.14);
      double wave3 = 0.4 * Math.sin(angle * 4.1 + (dx - dz) * 0.11);
      return dy + wave1 + wave2 + wave3;
   }

   private void tryPlaceSculkBlock(SculkweaversLanternListener.SculkSphereData sphereData, World world, Location blockLoc) {
      Block block = blockLoc.getBlock();
      Material currentType = block.getType();
      if (this.canReplaceForSphere(currentType)) {
         Location key = new Location(world, block.getX(), block.getY(), block.getZ());
         if (!sphereData.originalBlocks.containsKey(key)) {
            if (!this.plugin.isRegionProtected(key)) {
               sphereData.addBlock(key, block.getBlockData().clone());
               block.setType(Material.SCULK, false);
               Location particleLoc = blockLoc.clone().add(0.5, 0.5, 0.5);
               world.spawnParticle(Particle.SCULK_SOUL, particleLoc, 3, 0.2, 0.2, 0.2, 0.05);
               world.playSound(blockLoc, Sound.BLOCK_SCULK_PLACE, 0.3F, 1.2F);
            }
         }
      }
   }

   private boolean canReplaceForSphere(Material type) {
      if (type == Material.SCULK) {
         return false;
      } else if (type == Material.BEDROCK) {
         return false;
      } else if (type == Material.BARRIER) {
         return false;
      } else if (type == Material.END_PORTAL_FRAME) {
         return false;
      } else if (type == Material.END_PORTAL) {
         return false;
      } else if (type == Material.NETHER_PORTAL) {
         return false;
      } else if (type == Material.STRUCTURE_BLOCK) {
         return false;
      } else if (type == Material.COMMAND_BLOCK) {
         return false;
      } else {
         return type == Material.CHAIN_COMMAND_BLOCK ? false : type != Material.REPEATING_COMMAND_BLOCK;
      }
   }

   private void spawnWardens(SculkweaversLanternListener.SculkSphereData sphereData) {
      Location center = sphereData.center;
      World world = center.getWorld();
      int wardenCount = this.plugin.getConfig().getInt("weapons.sculkweavers_lantern.warden_count", 5);
      double damageMultiplier = this.plugin.getConfig().getDouble("weapons.sculkweavers_lantern.warden_damage_multiplier", 0.5);

      for (int i = 0; i < wardenCount; i++) {
         Location spawnLoc = this.findSafeSpawnLocation(center, sphereData.radius);
         if (spawnLoc != null) {
            Warden warden = (Warden)world.spawnEntity(spawnLoc, EntityType.WARDEN);
            if (AttributeCompat.getScale() != null && warden.getAttribute(AttributeCompat.getScale()) != null) {
               warden.getAttribute(AttributeCompat.getScale()).setBaseValue(0.5);
            }

            if (AttributeCompat.getMovementSpeed() != null && warden.getAttribute(AttributeCompat.getMovementSpeed()) != null) {
               double currentSpeed = warden.getAttribute(AttributeCompat.getMovementSpeed()).getBaseValue();
               warden.getAttribute(AttributeCompat.getMovementSpeed()).setBaseValue(currentSpeed * 2.0);
            }

            if (AttributeCompat.getAttackDamage() != null && warden.getAttribute(AttributeCompat.getAttackDamage()) != null) {
               double currentDamage = warden.getAttribute(AttributeCompat.getAttackDamage()).getBaseValue();
               warden.getAttribute(AttributeCompat.getAttackDamage()).setBaseValue(currentDamage * damageMultiplier);
            }

            warden.setCustomName("§8§lSculk Guardian");
            warden.setCustomNameVisible(true);
            warden.setPersistent(false);
            this.sphereMobOwners.put(warden.getUniqueId(), sphereData.owner.getUniqueId());
            this.addToOwnerTeam(sphereData, warden);
            sphereData.addWarden(warden);
            world.playSound(spawnLoc, Sound.ENTITY_WARDEN_EMERGE, 1.0F, 1.2F);
            world.spawnParticle(Particle.SCULK_SOUL, spawnLoc.add(0.0, 1.0, 0.0), 20, 0.5, 1.0, 0.5, 0.1);
         }
      }
   }

   private void spawnEndermen(SculkweaversLanternListener.SculkSphereData sphereData) {
      Location center = sphereData.center;
      World world = center.getWorld();
      int endermanCount = this.plugin.getConfig().getInt("weapons.sculkweavers_lantern.enderman_count", 3);
      double damageMultiplier = this.plugin.getConfig().getDouble("weapons.sculkweavers_lantern.enderman_damage_multiplier", 1.0);

      for (int i = 0; i < endermanCount; i++) {
         Location spawnLoc = this.findSafeSpawnLocation(center, sphereData.radius);
         if (spawnLoc != null) {
            Enderman enderman = (Enderman)world.spawnEntity(spawnLoc, EntityType.ENDERMAN);
            if (AttributeCompat.getAttackDamage() != null && enderman.getAttribute(AttributeCompat.getAttackDamage()) != null) {
               double currentDamage = enderman.getAttribute(AttributeCompat.getAttackDamage()).getBaseValue();
               enderman.getAttribute(AttributeCompat.getAttackDamage()).setBaseValue(currentDamage * damageMultiplier);
            }

            enderman.setPersistent(false);
            this.sphereMobOwners.put(enderman.getUniqueId(), sphereData.owner.getUniqueId());
            this.addToOwnerTeam(sphereData, enderman);
            Player target = this.findNearestPlayerTarget(center, sphereData.radius, sphereData.owner.getUniqueId());
            if (target != null) {
               enderman.setTarget(target);
            }

            sphereData.addEnderman(enderman);
            world.playSound(spawnLoc, Sound.ENTITY_ENDERMAN_SCREAM, 0.6F, 1.2F);
            world.spawnParticle(Particle.PORTAL, spawnLoc.clone().add(0.0, 1.0, 0.0), 18, 0.4, 0.8, 0.4, 0.02);
         }
      }
   }

   private void startMobRetargetTask(SculkweaversLanternListener.SculkSphereData sphereData) {
      if (sphereData.retargetTask != null) {
         sphereData.retargetTask.cancel();
         sphereData.retargetTask = null;
      }

      sphereData.retargetTask = new BukkitRunnable() {
         public void run() {
            if (sphereData.owner.isOnline() && SculkweaversLanternListener.this.activeSpheres.containsKey(sphereData.owner.getUniqueId())) {
               Player target = SculkweaversLanternListener.this.findNearestPlayerTarget(sphereData.center, sphereData.radius, sphereData.owner.getUniqueId());
               if (target != null) {
                  for (Warden w : sphereData.wardens) {
                     if (w.isValid() && !w.isDead()) {
                        w.setTarget(target);
                     }
                  }

                  for (Enderman e : sphereData.endermen) {
                     if (e.isValid() && !e.isDead()) {
                        e.setTarget(target);
                     }
                  }
               }
            } else {
               this.cancel();
            }
         }
      };
      sphereData.retargetTask.runTaskTimer(this.plugin, 10L, 10L);
   }

   private Player findNearestPlayerTarget(Location center, int radius, UUID excludedPlayerId) {
      World world = center.getWorld();
      if (world == null) {
         return null;
      } else {
         double maxDistSq = (radius + 1.5) * (radius + 1.5);
         Player best = null;
         double bestSq = Double.MAX_VALUE;

         for (Player p : world.getPlayers()) {
            if (p.isOnline() && !p.isDead() && (excludedPlayerId == null || !excludedPlayerId.equals(p.getUniqueId()))) {
               double d = p.getLocation().distanceSquared(center);
               if (d <= maxDistSq && d < bestSq) {
                  bestSq = d;
                  best = p;
               }
            }
         }

         return best;
      }
   }

   private void ensureOwnerTeam(SculkweaversLanternListener.SculkSphereData sphereData) {
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
      String teamName = "sculk_sphere_" + sphereData.owner.getUniqueId().toString().substring(0, 8);
      Team team = scoreboard.getTeam(teamName);
      if (team == null) {
         team = scoreboard.registerNewTeam(teamName);
         team.setAllowFriendlyFire(false);
      }

      team.addEntry(sphereData.owner.getName());
      sphereData.teamName = teamName;
   }

   private void addToOwnerTeam(SculkweaversLanternListener.SculkSphereData sphereData, Entity entity) {
      if (sphereData.teamName != null) {
         Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
         Team team = scoreboard.getTeam(sphereData.teamName);
         if (team != null) {
            team.addEntry(entity.getUniqueId().toString());
         }
      }
   }

   private void removeFromOwnerTeam(SculkweaversLanternListener.SculkSphereData sphereData, Entity entity) {
      if (sphereData.teamName != null) {
         Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
         Team team = scoreboard.getTeam(sphereData.teamName);
         if (team != null) {
            team.removeEntry(entity.getUniqueId().toString());
         }
      }
   }

   private void cleanupOwnerTeam(SculkweaversLanternListener.SculkSphereData sphereData) {
      if (sphereData.teamName != null) {
         Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
         Team team = scoreboard.getTeam(sphereData.teamName);
         if (team == null) {
            sphereData.teamName = null;
         } else {
            team.removeEntry(sphereData.owner.getName());
            if (team.getEntries().isEmpty()) {
               team.unregister();
            }

            sphereData.teamName = null;
         }
      }
   }

   private Location findSafeSpawnLocation(Location center, int radius) {
      Random random = new Random();
      int inner = Math.max(1, radius - 3);

      for (int attempts = 0; attempts < 40; attempts++) {
         int x = random.nextInt(2 * inner + 1) - inner;
         int y = random.nextInt(2 * inner + 1) - inner;
         int z = random.nextInt(2 * inner + 1) - inner;
         double dist = Math.sqrt(x * x + y * y + z * z);
         if (!(dist >= radius - 1.5)) {
            Location feet = center.clone().add(x + 0.5, y, z + 0.5);
            if (feet.getBlock().getType().isAir() && feet.clone().add(0.0, 1.0, 0.0).getBlock().getType().isAir()) {
               return feet;
            }
         }
      }

      return center.clone().add(0.0, 1.0, 0.0);
   }

   private void startSphereLifetime(SculkweaversLanternListener.SculkSphereData sphereData, int lifetimeSeconds) {
      (new BukkitRunnable() {
         public void run() {
            SculkweaversLanternListener.this.dissolveSphere(sphereData);
         }
      }).runTaskLater(this.plugin, lifetimeSeconds * 20L);
   }

   private void dissolveSphere(SculkweaversLanternListener.SculkSphereData sphereData) {
      final World world = sphereData.center.getWorld();
      this.cleanupSphereAssociations(sphereData);

      for (Warden warden : sphereData.wardens) {
         if (warden.isValid() && !warden.isDead()) {
            Location loc = warden.getLocation();
            world.spawnParticle(Particle.SCULK_SOUL, loc.add(0.0, 1.0, 0.0), 30, 0.5, 1.0, 0.5, 0.2);
            world.playSound(loc, Sound.ENTITY_WARDEN_DEATH, 0.5F, 1.5F);
            warden.remove();
         }
      }

      for (Enderman enderman : sphereData.endermen) {
         if (enderman.isValid() && !enderman.isDead()) {
            Location loc = enderman.getLocation();
            world.spawnParticle(Particle.PORTAL, loc.add(0.0, 1.0, 0.0), 25, 0.5, 1.0, 0.5, 0.05);
            world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5F, 1.4F);
            enderman.remove();
         }
      }

      final List<Location> vanishOrder = new ArrayList<>(sphereData.placementOrder);
      Collections.reverse(vanishOrder);
      final int n = vanishOrder.size();
      if (n == 0) {
         this.activeSpheres.remove(sphereData.owner.getUniqueId());
      } else {
         int buildDurationTicks = this.computeBuildDurationTicks(n);
         final int blocksPerTick = Math.max(1, (n + buildDurationTicks - 1) / buildDurationTicks);
         (new BukkitRunnable() {
            private int index = 0;

            public void run() {
               int stepped = 0;

               while (stepped < blocksPerTick && this.index < n) {
                  Location loc = vanishOrder.get(this.index++);
                  stepped++;
                  BlockData original = sphereData.originalBlocks.get(loc);
                  if (original != null) {
                     Block b = loc.getBlock();
                     if (b.getType() == Material.SCULK) {
                        b.setBlockData(original, false);
                        Location particleLoc = loc.clone().add(0.5, 0.5, 0.5);
                        world.spawnParticle(Particle.SCULK_SOUL, particleLoc, 2, 0.1, 0.1, 0.1, 0.05);
                        world.playSound(loc, Sound.BLOCK_SCULK_BREAK, 0.2F, 1.5F);
                     }
                  }
               }

               if (this.index >= n) {
                  SculkweaversLanternListener.this.activeSpheres.remove(sphereData.owner.getUniqueId());
                  this.cancel();
               }
            }
         }).runTaskTimer(this.plugin, 0L, 1L);
      }
   }

   private boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownTime = this.plugin.getConfig().getLong("weapons.sculkweavers_lantern.cooldown_seconds", 60L) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         return System.currentTimeMillis() - lastUse < cooldownTime;
      }
   }

   private long getRemainingCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownTime = this.plugin.getConfig().getLong("weapons.sculkweavers_lantern.cooldown_seconds", 60L) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }

   private void setCooldown(Player player) {
      this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
   }

   public long getRemainingCooldownSeconds(UUID playerId) {
      if (!this.cooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownTime = this.plugin.getConfig().getLong("weapons.sculkweavers_lantern.cooldown_seconds", 60L) * 1000L;
         long lastUse = this.cooldowns.get(playerId);
         long remaining = cooldownTime - (System.currentTimeMillis() - lastUse);
         return Math.max(0L, remaining / 1000L);
      }
   }

   private static class SculkSphereData {
      final Location center;
      final int radius;
      final Player owner;
      final Map<Location, BlockData> originalBlocks = new HashMap<>();
      final List<Location> placementOrder = new ArrayList<>();
      final List<Warden> wardens = new ArrayList<>();
      final List<Enderman> endermen = new ArrayList<>();
      BukkitRunnable retargetTask;
      String teamName;

      SculkSphereData(Location center, int radius, Player owner) {
         this.center = center;
         this.radius = radius;
         this.owner = owner;
      }

      void addBlock(Location key, BlockData original) {
         this.originalBlocks.put(key, original);
         this.placementOrder.add(key);
      }

      void addWarden(Warden warden) {
         this.wardens.add(warden);
      }

      void addEnderman(Enderman enderman) {
         this.endermen.add(enderman);
      }

      void cleanup() {
         if (this.retargetTask != null) {
            this.retargetTask.cancel();
            this.retargetTask = null;
         }

         for (Warden warden : this.wardens) {
            if (warden.isValid() && !warden.isDead()) {
               warden.remove();
            }
         }

         for (Enderman enderman : this.endermen) {
            if (enderman.isValid() && !enderman.isDead()) {
               enderman.remove();
            }
         }

         for (Entry<Location, BlockData> entry : this.originalBlocks.entrySet()) {
            Block b = entry.getKey().getBlock();
            if (b.getType() == Material.SCULK) {
               b.setBlockData(entry.getValue(), false);
            }
         }
      }
   }
}
