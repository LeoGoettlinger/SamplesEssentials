package dev.demora.demoralegendaryweapons.weapons.beehiveblaster;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Bee;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class BeehiveBlasterListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final BeehiveBlaster beehiveBlaster;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private final Map<UUID, UUID> beeOwners = new HashMap<>();
   private final Map<UUID, Set<UUID>> ownerBees = new HashMap<>();
   private final Map<UUID, UUID> ownerTargets = new HashMap<>();
   private final Map<UUID, String> ownerTeams = new HashMap<>();
   private static final int BEE_COUNT = 3;

   public BeehiveBlasterListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.beehiveBlaster = new BeehiveBlaster(plugin);
      this.startPursuitTask();
   }

   private void startPursuitTask() {
      (new BukkitRunnable() {
         public void run() {
            if (!BeehiveBlasterListener.this.ownerBees.isEmpty()) {
               int angerTicks = BeehiveBlasterListener.this.beehiveBlaster.getBeeDespawnSeconds() * 20;

               for (Entry<UUID, Set<UUID>> entry : BeehiveBlasterListener.this.ownerBees.entrySet()) {
                  UUID targetId = BeehiveBlasterListener.this.ownerTargets.get(entry.getKey());
                  Player target = targetId != null ? Bukkit.getPlayer(targetId) : null;
                  boolean validTarget = target != null && target.isOnline() && !target.isDead();
                  if (validTarget) {
                     for (UUID beeId : entry.getValue()) {
                        if (Bukkit.getEntity(beeId) instanceof Bee bee && bee.isValid() && !bee.isDead()) {
                           bee.setHasStung(false);
                           if (bee.getTarget() == null || !target.getUniqueId().equals(bee.getTarget().getUniqueId())) {
                              bee.setTarget(target);
                           }

                           bee.setAnger(angerTicks);
                        }
                     }
                  }
               }
            }
         }
      }).runTaskTimer(this.plugin, 20L, 10L);
   }

   public BeehiveBlaster getBeehiveBlaster() {
      return this.beehiveBlaster;
   }

   @EventHandler
   public void onEntityShootBow(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player player) {
         ItemStack var6 = event.getBow();
         if (this.beehiveBlaster.isBeehiveBlaster(var6)) {
            if (this.isOnCooldown(player)) {
               event.setCancelled(true);
               long remaining = this.getRemainingCooldownSeconds(player.getUniqueId());
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.beehive-blaster-cooldown").replace("{time}", String.valueOf(remaining)));
            } else {
               Entity var5 = event.getProjectile();
               if (var5 instanceof Entity) {
                  var5.remove();
               }

               this.setCooldown(player);
               this.launchBeehive(player);
            }
         }
      }
   }

   private void launchBeehive(Player player) {
      final Location eyeLoc = player.getEyeLocation();
      final Vector direction = eyeLoc.getDirection().normalize();
      final World world = player.getWorld();
      final BlockDisplay display = (BlockDisplay)world.spawnEntity(eyeLoc, EntityType.BLOCK_DISPLAY);
      display.setBlock(Material.BEE_NEST.createBlockData());
      Transformation transformation = new Transformation(
         new Vector3f(-0.35F, -0.35F, -0.35F), new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F), new Vector3f(0.7F, 0.7F, 0.7F), new AxisAngle4f(0.0F, 0.0F, 0.0F, 1.0F)
      );
      display.setTransformation(transformation);

      try {
         display.setInterpolationDelay(0);
         display.setInterpolationDuration(2);
         display.setTeleportDuration(2);
      } catch (NoSuchMethodError var8) {
      }

      world.playSound(eyeLoc, Sound.BLOCK_BEEHIVE_WORK, 1.0F, 0.8F);
      world.playSound(eyeLoc, Sound.ENTITY_BEE_LOOP_AGGRESSIVE, 1.0F, 1.0F);
      (new BukkitRunnable() {
         private Location currentLoc = eyeLoc.clone();
         private final Vector velocity = direction.clone().multiply(1.4);
         private int ticks = 0;

         public void run() {
            if (!display.isDead() && display.isValid() && this.ticks < 100) {
               this.velocity.setY(this.velocity.getY() - 0.03);
               this.currentLoc.add(this.velocity);
               display.teleport(this.currentLoc);
               world.spawnParticle(Particle.FALLING_HONEY, this.currentLoc, 3, 0.1, 0.1, 0.1, 0.01);
               LivingEntity hitEntity = BeehiveBlasterListener.this.findHitEntity(this.currentLoc, player);
               if (hitEntity != null) {
                  BeehiveBlasterListener.this.breakBeehive(hitEntity.getLocation(), player, display);
                  this.cancel();
               } else if (!this.currentLoc.getBlock().isPassable()) {
                  BeehiveBlasterListener.this.breakBeehive(this.currentLoc, player, display);
                  this.cancel();
               } else {
                  this.ticks++;
               }
            } else {
               BeehiveBlasterListener.this.breakBeehive(this.currentLoc, player, display);
               this.cancel();
            }
         }
      }).runTaskTimer(this.plugin, 0L, 1L);
   }

   private LivingEntity findHitEntity(Location loc, Player owner) {
      LivingEntity nearest = null;
      double nearestSq = Double.MAX_VALUE;

      for (Entity entity : loc.getWorld().getNearbyEntities(loc, 1.2, 1.2, 1.2)) {
         if (entity instanceof LivingEntity living && !entity.getUniqueId().equals(owner.getUniqueId()) && !this.beeOwners.containsKey(entity.getUniqueId())) {
            double distSq = entity.getLocation().distanceSquared(loc);
            if (distSq < nearestSq) {
               nearestSq = distSq;
               nearest = living;
            }
         }
      }

      return nearest;
   }

   private void breakBeehive(Location location, Player owner, BlockDisplay display) {
      if (display.isValid()) {
         display.remove();
      }

      World world = location.getWorld();
      if (world != null) {
         world.playSound(location, Sound.BLOCK_BEEHIVE_SHEAR, 1.2F, 0.9F);
         world.playSound(location, Sound.ENTITY_BEE_LOOP_AGGRESSIVE, 1.4F, 1.0F);
         world.spawnParticle(Particle.FALLING_HONEY, location, 40, 0.5, 0.5, 0.5, 0.05);
         world.spawnParticle(Particle.BLOCK, location, 30, 0.5, 0.5, 0.5, 0.1, Material.BEE_NEST.createBlockData());
         this.spawnBees(location, owner);
      }
   }

   private void spawnBees(Location location, Player owner) {
      World world = location.getWorld();
      if (world != null) {
         this.ensureOwnerTeam(owner);
         double health = this.beehiveBlaster.getBeeHealth();
         double damage = this.beehiveBlaster.getBeeDamage();
         int despawnSeconds = this.beehiveBlaster.getBeeDespawnSeconds();
         UUID targetId = this.ownerTargets.get(owner.getUniqueId());
         Player target = targetId != null ? Bukkit.getPlayer(targetId) : null;

         for (int i = 0; i < 3; i++) {
            Location spawnLoc = location.clone().add((Math.random() - 0.5) * 1.5, 0.5 + Math.random() * 0.5, (Math.random() - 0.5) * 1.5);
            Bee bee = (Bee)world.spawnEntity(spawnLoc, EntityType.BEE);
            AttributeInstance maxHealth = AttributeCompat.getMaxHealth() != null ? bee.getAttribute(AttributeCompat.getMaxHealth()) : null;
            if (maxHealth != null) {
               maxHealth.setBaseValue(health);
            }

            bee.setHealth(Math.min(health, bee.getMaxHealth()));
            AttributeInstance attackDamage = AttributeCompat.getAttackDamage() != null ? bee.getAttribute(AttributeCompat.getAttackDamage()) : null;
            if (attackDamage != null) {
               attackDamage.setBaseValue(damage);
            }

            bee.setAnger(despawnSeconds * 20);
            bee.setPersistent(false);
            bee.setCannotEnterHiveTicks(despawnSeconds * 20 + 200);
            this.beeOwners.put(bee.getUniqueId(), owner.getUniqueId());
            this.ownerBees.computeIfAbsent(owner.getUniqueId(), k -> new HashSet<>()).add(bee.getUniqueId());
            this.addToOwnerTeam(owner, bee);
            if (target != null && target.isOnline() && !target.isDead()) {
               bee.setTarget(target);
            }

            world.spawnParticle(Particle.FALLING_HONEY, spawnLoc, 10, 0.2, 0.2, 0.2, 0.02);
            this.scheduleBeeDespawn(bee, owner.getUniqueId(), despawnSeconds);
         }
      }
   }

   private void scheduleBeeDespawn(Bee bee, UUID ownerId, int despawnSeconds) {
      (new BukkitRunnable() {
         public void run() {
            BeehiveBlasterListener.this.removeBee(bee, ownerId);
         }
      }).runTaskLater(this.plugin, despawnSeconds * 20L);
   }

   private void removeBee(Bee bee, UUID ownerId) {
      UUID beeId = bee.getUniqueId();
      this.beeOwners.remove(beeId);
      Set<UUID> bees = this.ownerBees.get(ownerId);
      if (bees != null) {
         bees.remove(beeId);
         if (bees.isEmpty()) {
            this.ownerBees.remove(ownerId);
         }
      }

      this.removeFromOwnerTeam(ownerId, bee);
      if (bee.isValid() && !bee.isDead()) {
         Location loc = bee.getLocation();
         bee.getWorld().spawnParticle(Particle.FALLING_HONEY, loc, 12, 0.3, 0.3, 0.3, 0.02);
         bee.getWorld().playSound(loc, Sound.ENTITY_BEE_DEATH, 0.7F, 1.0F);
         bee.remove();
      }

      this.cleanupOwnerTeamIfEmpty(ownerId);
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onOwnerAttack(EntityDamageByEntityEvent event) {
      if (event.getEntity() instanceof Player victim) {
         Player attacker = this.resolveAttacker(event.getDamager());
         if (attacker != null) {
            if (this.ownerBees.containsKey(attacker.getUniqueId())) {
               if (!attacker.getUniqueId().equals(victim.getUniqueId())) {
                  this.ownerTargets.put(attacker.getUniqueId(), victim.getUniqueId());
                  this.retargetBees(attacker.getUniqueId(), victim);
               }
            }
         }
      }
   }

   private Player resolveAttacker(Entity damager) {
      if (damager instanceof Player player) {
         return player;
      } else {
         return damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter ? shooter : null;
      }
   }

   private void retargetBees(UUID ownerId, Player target) {
      Set<UUID> bees = this.ownerBees.get(ownerId);
      if (bees != null) {
         for (UUID beeId : bees) {
            if (Bukkit.getEntity(beeId) instanceof Bee bee && bee.isValid() && !bee.isDead()) {
               bee.setTarget(target);
               bee.setAnger(this.beehiveBlaster.getBeeDespawnSeconds() * 20);
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onBeeTarget(EntityTargetLivingEntityEvent event) {
      if (event.getEntity() instanceof Bee bee) {
         UUID ownerId = this.beeOwners.get(bee.getUniqueId());
         if (ownerId != null) {
            UUID allowedTargetId = this.ownerTargets.get(ownerId);
            if (allowedTargetId == null) {
               event.setCancelled(true);
               event.setTarget(null);
            } else {
               if (event.getTarget() == null || !event.getTarget().getUniqueId().equals(allowedTargetId)) {
                  event.setCancelled(true);
                  if (Bukkit.getEntity(allowedTargetId) instanceof Player allowedPlayer && allowedPlayer.isOnline() && !allowedPlayer.isDead()) {
                     bee.setTarget(allowedPlayer);
                  } else {
                     event.setTarget(null);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      this.cooldowns.remove(playerId);
      this.ownerTargets.remove(playerId);
      Set<UUID> bees = this.ownerBees.remove(playerId);
      if (bees != null) {
         for (UUID beeId : bees) {
            this.beeOwners.remove(beeId);
            if (Bukkit.getEntity(beeId) instanceof Bee bee) {
               this.removeFromOwnerTeam(playerId, bee);
               if (bee.isValid() && !bee.isDead()) {
                  bee.remove();
               }
            }
         }
      }

      Iterator<Entry<UUID, UUID>> iterator = this.ownerTargets.entrySet().iterator();

      while (iterator.hasNext()) {
         if (iterator.next().getValue().equals(playerId)) {
            iterator.remove();
         }
      }

      this.cleanupOwnerTeamIfEmpty(playerId);
   }

   private void ensureOwnerTeam(Player owner) {
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
      String teamName = "beehive_" + owner.getUniqueId().toString().substring(0, 8);
      Team team = scoreboard.getTeam(teamName);
      if (team == null) {
         team = scoreboard.registerNewTeam(teamName);
         team.setAllowFriendlyFire(false);
      }

      team.addEntry(owner.getName());
      this.ownerTeams.put(owner.getUniqueId(), teamName);
   }

   private void addToOwnerTeam(Player owner, Entity entity) {
      String teamName = this.ownerTeams.get(owner.getUniqueId());
      if (teamName != null) {
         Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
         Team team = scoreboard.getTeam(teamName);
         if (team != null) {
            team.addEntry(entity.getUniqueId().toString());
         }
      }
   }

   private void removeFromOwnerTeam(UUID ownerId, Entity entity) {
      String teamName = this.ownerTeams.get(ownerId);
      if (teamName != null) {
         Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
         Team team = scoreboard.getTeam(teamName);
         if (team != null) {
            team.removeEntry(entity.getUniqueId().toString());
         }
      }
   }

   private void cleanupOwnerTeamIfEmpty(UUID ownerId) {
      if (!this.ownerBees.containsKey(ownerId)) {
         String teamName = this.ownerTeams.remove(ownerId);
         if (teamName != null) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
               Player owner = Bukkit.getPlayer(ownerId);
               if (owner != null) {
                  team.removeEntry(owner.getName());
               }

               if (team.getEntries().isEmpty()) {
                  team.unregister();
               }
            }
         }
      }
   }

   private boolean isOnCooldown(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.cooldowns.containsKey(playerId)) {
         return false;
      } else {
         long cooldownTime = this.beehiveBlaster.getCooldownSeconds() * 1000L;
         return System.currentTimeMillis() - this.cooldowns.get(playerId) < cooldownTime;
      }
   }

   private void setCooldown(Player player) {
      this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
   }

   public long getRemainingCooldownSeconds(UUID playerId) {
      if (!this.cooldowns.containsKey(playerId)) {
         return 0L;
      } else {
         long cooldownTime = this.beehiveBlaster.getCooldownSeconds() * 1000L;
         long remaining = cooldownTime - (System.currentTimeMillis() - this.cooldowns.get(playerId));
         return Math.max(0L, remaining / 1000L);
      }
   }
}
