package dev.demora.demoralegendaryweapons.weapons.hypnosisstaff;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class HypnosisStaffListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final HypnosisStaff hypnosisStaff;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private final Map<UUID, Set<UUID>> controlledMobs = new HashMap<>();
   private final Map<UUID, UUID> mobOwners = new HashMap<>();
   private final Map<UUID, UUID> ownerTargets = new HashMap<>();

   public HypnosisStaffListener(DemoraLegendaryWeapons plugin, HypnosisStaff hypnosisStaff) {
      this.plugin = plugin;
      this.hypnosisStaff = hypnosisStaff;
      this.startTeleportTask();
   }

   public boolean isOnCooldown(Player player) {
      Long cooldownEnd = this.cooldowns.get(player.getUniqueId());
      return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
   }

   public long getRemainingCooldown(Player player) {
      Long cooldownEnd = this.cooldowns.get(player.getUniqueId());
      return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd ? (cooldownEnd - System.currentTimeMillis()) / 1000L : 0L;
   }

   @EventHandler
   public void onOwnerAttack(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         if (event.getEntity() instanceof Player) {
            Player owner = (Player)event.getDamager();
            Set<UUID> mobIds = this.controlledMobs.get(owner.getUniqueId());
            if (mobIds != null && !mobIds.isEmpty()) {
               Player target = (Player)event.getEntity();
               if (!target.getUniqueId().equals(owner.getUniqueId())) {
                  this.ownerTargets.put(owner.getUniqueId(), target.getUniqueId());

                  for (UUID mobId : new HashSet<>(mobIds)) {
                     if (Bukkit.getEntity(mobId) instanceof Mob mob && !mob.isDead() && mob.isValid()) {
                        mob.setTarget(target);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onControlledMobTarget(EntityTargetLivingEntityEvent event) {
      if (event.getEntity() instanceof Mob mob) {
         UUID ownerId = this.mobOwners.get(mob.getUniqueId());
         if (ownerId != null) {
            if (event.getTarget() instanceof Player targetPlayer) {
               if (targetPlayer.getUniqueId().equals(ownerId)) {
                  event.setCancelled(true);
                  event.setTarget(null);
               } else {
                  UUID designatedTargetId = this.ownerTargets.get(ownerId);
                  if (designatedTargetId == null || !targetPlayer.getUniqueId().equals(designatedTargetId)) {
                     event.setCancelled(true);
                     event.setTarget(null);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction().isRightClick()) {
         Player player = event.getPlayer();
         ItemStack item = player.getInventory().getItemInMainHand();
         if (this.hypnosisStaff.isHypnosisStaff(item)) {
            event.setCancelled(true);
            Long cooldownEnd = this.cooldowns.get(player.getUniqueId());
            if (cooldownEnd != null && System.currentTimeMillis() < cooldownEnd) {
               long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000L;
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.hypnosis-cooldown").replace("{time}", String.valueOf(remaining)));
            } else {
               this.fireHypnosisRay(player);
            }
         }
      }
   }

   private void fireHypnosisRay(Player player) {
      int cooldownSeconds = this.plugin.getConfig().getInt("weapons.hypnosis_staff.cooldown", 5);
      double beamSpeed = this.plugin.getConfig().getDouble("weapons.hypnosis_staff.beam_speed", 0.5);
      final double beamStep = Math.max(0.1, beamSpeed);
      this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldownSeconds * 1000L);
      final Location start = player.getEyeLocation();
      final Vector direction = start.getDirection();
      player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.0F, 1.5F);
      (new BukkitRunnable() {
            private double distance = 0.0;
            private final double maxDistance = 50.0;
            private final double step = beamStep;

            public void run() {
               if (this.distance >= 50.0) {
                  this.cancel();
               } else {
                  Location current = start.clone().add(direction.clone().multiply(this.distance));
                  current.getWorld().spawnParticle(Particle.DUST, current, 3, 0.1, 0.1, 0.1, 0.0, new DustOptions(Color.YELLOW, 1.0F));
                  RayTraceResult result = current.getWorld()
                     .rayTraceEntities(current, direction, this.step, entity -> entity instanceof LivingEntity && entity != player);
                  if (result != null && result.getHitEntity() instanceof LivingEntity) {
                     LivingEntity target = (LivingEntity)result.getHitEntity();
                     if (!(target instanceof Player)) {
                        HypnosisStaffListener.this.hypnotizeMob(player, target);
                     }

                     this.cancel();
                  } else if (current.getBlock().getType().isSolid()) {
                     this.cancel();
                  } else {
                     this.distance = this.distance + this.step;
                  }
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 1L);
   }

   private void hypnotizeMob(Player player, LivingEntity mob) {
      Set<UUID> playerMobs = this.controlledMobs.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
      int maxMobs = this.plugin.getConfig().getInt("weapons.hypnosis_staff.max_controlled_mobs", 10);
      if (playerMobs.size() >= maxMobs) {
         player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.hypnosis-max-mobs").replace("{max}", String.valueOf(maxMobs)));
      } else {
         UUID previousOwner = this.mobOwners.get(mob.getUniqueId());
         if (previousOwner != null) {
            if (previousOwner.equals(player.getUniqueId())) {
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.hypnosis-already-controlled"));
            } else {
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.hypnosis-controlled-by-other"));
            }
         } else {
            playerMobs.add(mob.getUniqueId());
            this.mobOwners.put(mob.getUniqueId(), player.getUniqueId());
            this.addToTeam(player, mob);
            this.applyOwnerName(mob, player);
            if (AttributeCompat.getMaxHealth() != null && mob.getAttribute(AttributeCompat.getMaxHealth()) != null) {
               double maxHealth = mob.getAttribute(AttributeCompat.getMaxHealth()).getValue();
               mob.setHealth(Math.min(mob.getHealth() + 40.0, maxHealth));
            }

            mob.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
            mob.getWorld().spawnParticle(Particle.HEART, mob.getLocation().add(0.0, 1.0, 0.0), 10, 0.5, 0.5, 0.5, 0.0);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.5F);
         }
      }
   }

   private void addToTeam(Player player, LivingEntity mob) {
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
      String teamName = "hypnosis_" + player.getUniqueId().toString().substring(0, 8);
      Team team = scoreboard.getTeam(teamName);
      if (team == null) {
         team = scoreboard.registerNewTeam(teamName);
         team.setAllowFriendlyFire(false);
      }

      team.addEntry(mob.getUniqueId().toString());
      team.addEntry(player.getName());
   }

   private void removeFromTeam(LivingEntity mob, UUID ownerId) {
      Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
      String teamName = "hypnosis_" + ownerId.toString().substring(0, 8);
      Team team = scoreboard.getTeam(teamName);
      if (team != null) {
         team.removeEntry(mob.getUniqueId().toString());
      }
   }

   private void removeGlowingEffect(LivingEntity mob) {
      mob.removePotionEffect(PotionEffectType.GLOWING);
   }

   private void applyOwnerName(LivingEntity mob, Player owner) {
      Component name = ((TextComponent)Component.text("Хозяин: " + owner.getName()).color(NamedTextColor.YELLOW)).decorate(TextDecoration.BOLD);
      mob.customName(name);
      mob.setCustomNameVisible(true);
   }

   private void removeOwnerName(LivingEntity mob) {
      mob.customName(null);
      mob.setCustomNameVisible(false);
   }

   private void startTeleportTask() {
      (new BukkitRunnable() {
            public void run() {
               double maxDistance = HypnosisStaffListener.this.plugin.getConfig().getDouble("weapons.hypnosis_staff.max_distance", 30.0);
               double followSpeed = HypnosisStaffListener.this.plugin.getConfig().getDouble("weapons.hypnosis_staff.follow_speed", 1.0);

               for (Entry<UUID, Set<UUID>> entry : HypnosisStaffListener.this.controlledMobs.entrySet()) {
                  UUID ownerId = entry.getKey();
                  Player owner = Bukkit.getPlayer(ownerId);
                  if (owner != null && owner.isOnline()) {
                     GameMode ownerGameMode = owner.getGameMode();
                     Set<UUID> mobIds = new HashSet<>(entry.getValue());
                     if (ownerGameMode != GameMode.CREATIVE && ownerGameMode != GameMode.SURVIVAL) {
                        for (UUID mobId : mobIds) {
                           if (Bukkit.getEntity(mobId) instanceof Mob mobEntity) {
                              mobEntity.getPathfinder().stopPathfinding();
                              mobEntity.setTarget(null);
                           }
                        }
                     } else {
                        for (UUID mobIdx : mobIds) {
                           if (!(Bukkit.getEntity(mobIdx) instanceof LivingEntity mob)) {
                              entry.getValue().remove(mobIdx);
                              HypnosisStaffListener.this.mobOwners.remove(mobIdx);
                           } else if (!mob.isDead() && mob.isValid()) {
                              double distance = mob.getLocation().distance(owner.getLocation());
                              if (distance > maxDistance) {
                                 Location teleportLoc = owner.getLocation().clone();
                                 teleportLoc.add(owner.getLocation().getDirection().multiply(-2));
                                 teleportLoc.setY(owner.getLocation().getY());
                                 if (teleportLoc.getBlock().getType().isSolid()) {
                                    teleportLoc.add(0.0, 1.0, 0.0);
                                 }

                                 mob.teleport(teleportLoc);
                                 mob.getWorld().spawnParticle(Particle.PORTAL, mob.getLocation(), 20, 0.5, 0.5, 0.5, 0.0);
                              } else {
                                 UUID designatedTargetId = HypnosisStaffListener.this.ownerTargets.get(ownerId);
                                 Player designatedTarget = designatedTargetId != null ? Bukkit.getPlayer(designatedTargetId) : null;
                                 boolean validDesignatedTarget = designatedTarget != null
                                    && designatedTarget.isOnline()
                                    && !designatedTarget.isDead()
                                    && designatedTarget.getWorld().equals(mob.getWorld());
                                 if (validDesignatedTarget && mob instanceof Mob mobEntity) {
                                    mobEntity.setTarget(designatedTarget);
                                 } else {
                                    if (mob instanceof Mob mobEntity && mobEntity.getTarget() instanceof Player) {
                                       mobEntity.setTarget(null);
                                    }

                                    if (distance > 1.5) {
                                       if (mob instanceof Mob mobEntity) {
                                          mobEntity.getPathfinder().moveTo(owner, followSpeed);
                                       }
                                    } else if (mob instanceof Mob mobEntity) {
                                       mobEntity.getPathfinder().stopPathfinding();
                                    }
                                 }
                              }
                           } else {
                              entry.getValue().remove(mobIdx);
                              HypnosisStaffListener.this.mobOwners.remove(mobIdx);
                              HypnosisStaffListener.this.removeFromTeam(mob, ownerId);
                              HypnosisStaffListener.this.removeOwnerName(mob);
                              HypnosisStaffListener.this.removeGlowingEffect(mob);
                           }
                        }
                     }
                  }
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 5L);
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      UUID playerId = event.getPlayer().getUniqueId();
      this.cooldowns.remove(playerId);
      this.ownerTargets.remove(playerId);
      this.ownerTargets.values().removeIf(targetId -> targetId.equals(playerId));
   }
}
