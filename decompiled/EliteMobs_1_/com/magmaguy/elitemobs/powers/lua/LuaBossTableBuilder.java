package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Shape;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolService;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.meta.ProjectileTagger;
import com.magmaguy.elitemobs.powers.specialpowers.ShieldWallSupport;
import com.magmaguy.elitemobs.powers.specialpowers.SpiritWalkSupport;
import com.magmaguy.elitemobs.powers.specialpowers.TrackingFireballSupport;
import com.magmaguy.elitemobs.powers.specialpowers.ZombieNecronomiconSupport;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

final class LuaBossTableBuilder {
   private Integer trackingFireballTask;
   private final ScriptDefinition definition;
   private final EliteEntity eliteEntity;
   private final LuaPowerSupport support;
   private final LuaPowerEntityTables entityTables;
   private final LuaPowerScriptApi.OwnedTaskController taskController;
   private final LuaPowerScriptApi.CallbackInvoker callbackInvoker;

   LuaBossTableBuilder(
      ScriptDefinition definition,
      EliteEntity eliteEntity,
      LuaPowerSupport support,
      LuaPowerEntityTables entityTables,
      LuaPowerScriptApi.OwnedTaskController taskController,
      LuaPowerScriptApi.CallbackInvoker callbackInvoker
   ) {
      this.definition = definition;
      this.eliteEntity = eliteEntity;
      this.support = support;
      this.entityTables = entityTables;
      this.taskController = taskController;
      this.callbackInvoker = callbackInvoker;
      taskController.ownCleanup(this::stopTrackingFireballs);
   }

   private void startTrackingFireballs(Monster monster, double speed) {
      if (this.trackingFireballTask == null && monster.isValid()) {
         if (Double.isFinite(speed) && !(speed <= 0.0)) {
            this.trackingFireballTask = this.taskController.runRepeating(0, 160, () -> {
               try {
                  if (monster.isValid() && monster.getTarget() != null && this.eliteEntity.getUnsyncedLivingEntity() == monster) {
                     TrackingFireballSupport.fireAtNearbyPlayers(monster, speed);
                  } else {
                     this.stopTrackingFireballs();
                  }
               } catch (Error | RuntimeException var5) {
                  this.stopTrackingFireballs();
                  throw var5;
               }
            });
         } else {
            throw new IllegalArgumentException("Fireball speed must be finite and positive");
         }
      }
   }

   private void stopTrackingFireballs() {
      if (this.trackingFireballTask != null) {
         this.taskController.cancel(this.trackingFireballTask);
         this.trackingFireballTask = null;
      }
   }

   LuaTable build() {
      LuaTable boss = this.eliteEntity.getLivingEntity() != null
         ? this.entityTables.createLivingEntityTable(this.eliteEntity.getLivingEntity())
         : new LuaTable();
      boss.set("name", LuaValue.valueOf(this.eliteEntity.getName() == null ? this.definition.getFileName() : this.eliteEntity.getName()));
      boss.set("uuid", LuaValue.valueOf(this.eliteEntity.getEliteUUID().toString()));
      boss.set("health", LuaValue.valueOf(this.eliteEntity.getHealth()));
      boss.set("maximum_health", LuaValue.valueOf(this.eliteEntity.getMaxHealth()));
      boss.set("current_location", this.support.toLocationTable(this.eliteEntity.getLocation()));
      boss.set("is_alive", this.method(boss, args -> LuaValue.valueOf(this.isAlive(this.eliteEntity.getLivingEntity()) && this.eliteEntity.exists())));
      boss.set("get_health", this.method(boss, args -> LuaValue.valueOf(this.eliteEntity.getHealth())));
      boss.set("get_maximum_health", this.method(boss, args -> LuaValue.valueOf(this.eliteEntity.getMaxHealth())));
      boss.set("get_location", this.method(boss, args -> this.support.toLocationTable(this.eliteEntity.getLocation())));
      boss.set("restore_health", this.method(boss, args -> {
         this.eliteEntity.heal(args.checkdouble(1));
         return LuaValue.NIL;
      }));
      boss.set("add_tag", this.method(boss, args -> {
         this.eliteEntity.addTag(args.checkjstring(1));
         this.entityTables.scheduleTagRemoval(this.eliteEntity.getLivingEntity(), args.optint(2, 0), args.checkjstring(1));
         return LuaValue.NIL;
      }));
      boss.set("remove_tag", this.method(boss, args -> {
         this.eliteEntity.removeTag(args.checkjstring(1));
         return LuaValue.NIL;
      }));
      boss.set("has_tag", this.method(boss, args -> LuaValue.valueOf(this.eliteEntity.hasTag(args.checkjstring(1)))));
      boss.set("get_nearby_elites", this.method(boss, args -> this.nearbyEliteTables(args.checkdouble(1))));
      boss.set("reset_custom_name", this.method(boss, args -> {
         if (this.eliteEntity.getLivingEntity() != null) {
            this.eliteEntity.getLivingEntity().setCustomName(this.eliteEntity.getName());
         }

         return LuaValue.NIL;
      }));
      boss.set("play_sound_at_self", this.method(boss, args -> {
         this.support.playSound(this.eliteEntity.getLocation(), args.checkjstring(1), this.resolveVolume(args), this.resolvePitch(args));
         return LuaValue.NIL;
      }));
      boss.set("play_sound_at_entity", this.method(boss, args -> {
         this.support.playSound(this.eliteEntity.getLocation(), args.checkjstring(1), this.resolveVolume(args), this.resolvePitch(args));
         return LuaValue.NIL;
      }));
      boss.set("spawn_particle_at_self", this.method(boss, args -> {
         this.support.spawnParticle(this.eliteEntity.getLocation(), args.arg(1), args.optint(2, 1));
         return LuaValue.NIL;
      }));
      boss.set("play_model_animation", this.method(boss, args -> {
         if (this.eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomModel() != null) {
            customBossEntity.getCustomModel().playAnimationByName(args.checkjstring(1));
         }

         return LuaValue.NIL;
      }));
      boss.set("navigate_to_location", this.method(boss, args -> {
         if (this.eliteEntity instanceof CustomBossEntity customBossEntity) {
            Location destination = this.support.toLocation(args.arg1());
            if (destination != null) {
               Navigation.navigateTo(customBossEntity, args.optdouble(2, 1.0), destination, args.optboolean(3, false), args.optint(4, 0));
            }
         }

         return LuaValue.NIL;
      }));
      boss.set("level", LuaValue.valueOf(this.eliteEntity.getLevel()));
      boss.set("damager_count", LuaValue.valueOf(this.eliteEntity.getDamagers().size()));
      boss.set("is_in_combat", LuaValue.valueOf(this.eliteEntity.isInCombat()));
      boss.set("exists", LuaValue.valueOf(this.eliteEntity.exists()));
      boss.set("get_damager_count", this.method(boss, args -> LuaValue.valueOf(this.eliteEntity.getDamagers().size())));
      boss.set("despawn", this.method(boss, args -> {
         this.eliteEntity.remove(RemovalReason.OTHER);
         return LuaValue.NIL;
      }));
      boss.set("remove", this.method(boss, args -> {
         this.eliteEntity.remove(RemovalReason.OTHER);
         return LuaValue.NIL;
      }));
      boss.set("patrol_pause", this.method(boss, args -> LuaValue.valueOf(PatrolService.pause(this.eliteEntity))));
      boss.set("patrol_resume", this.method(boss, args -> LuaValue.valueOf(PatrolService.resume(this.eliteEntity))));
      boss.set("hold", this.method(boss, args -> LuaValue.valueOf(PatrolService.hold(this.eliteEntity, this.patrolOffset(args)))));
      boss.set("walk_to", this.method(boss, args -> LuaValue.valueOf(PatrolService.walkTo(this.eliteEntity, this.patrolOffset(args)))));
      boss.set("teleport", this.method(boss, args -> LuaValue.valueOf(PatrolService.teleport(this.eliteEntity, this.patrolOffset(args)))));
      boss.set(
         "get_ender_dragon_phase",
         this.method(
            boss,
            args -> (Varargs)(this.eliteEntity.getLivingEntity() instanceof EnderDragon enderDragon
               ? LuaValue.valueOf(enderDragon.getPhase().name())
               : LuaValue.NIL)
         )
      );
      boss.set("set_ender_dragon_phase", this.method(boss, args -> {
         if (this.eliteEntity.getLivingEntity() instanceof EnderDragon enderDragon) {
            Phase phase = this.support.parseEnum(args.checkjstring(1), Phase.class, null);
            if (phase != null) {
               enderDragon.setPhase(phase);
            }
         }

         return LuaValue.NIL;
      }));
      boss.set("has_mount", this.method(boss, args -> LuaValue.valueOf(this.resolveBossMountEntity() != null)));
      boss.set("get_mount", this.method(boss, args -> {
         Entity mountEntity = this.resolveBossMountEntity();
         LuaValue mountTable = mountEntity == null ? LuaValue.NIL : this.entityTables.createEntityReferenceTable(mountEntity);
         if (mountTable.istable()) {
            mountTable.checktable().set("is_mount", LuaValue.TRUE);
         }

         return mountTable;
      }));
      boss.set("set_mount", this.method(boss, args -> {
         Entity mountEntity = this.support.resolveEntityReference(args.arg1());
         return LuaValue.valueOf(this.entityTables.mountPassenger(mountEntity, this.eliteEntity.getLivingEntity()));
      }));
      VarArgFunction clearMount = this.method(boss, args -> {
         LivingEntity livingEntity = this.eliteEntity.getLivingEntity();
         return LuaValue.valueOf(livingEntity != null && livingEntity.leaveVehicle());
      });
      boss.set("clear_mount", clearMount);
      boss.set("dismount", clearMount);
      boss.set("start_tracking_fireball_system", this.method(boss, args -> {
         if (this.eliteEntity.getLivingEntity() instanceof Monster monster) {
            this.startTrackingFireballs(monster, args.optdouble(1, 0.5));
         }

         return LuaValue.NIL;
      }));
      boss.set("handle_spirit_walk_damage", this.method(boss, args -> {
         DamageCause cause = this.support.parseEnum(args.checkjstring(1), DamageCause.class, null);
         SpiritWalkSupport.handleBossDamaged(this.eliteEntity, cause);
         return LuaValue.NIL;
      }));
      boss.set("shield_wall_is_active", this.method(boss, args -> LuaValue.valueOf(ShieldWallSupport.isActive(this.eliteEntity))));
      boss.set("initialize_shield_wall", this.method(boss, args -> {
         ShieldWallSupport.initialize(this.eliteEntity, args.optint(1, 1));
         return LuaValue.NIL;
      }));
      boss.set("shield_wall_absorb_damage", this.method(boss, args -> {
         Player player = this.support.resolvePlayerReference(args.arg1());
         return player == null ? LuaValue.FALSE : LuaValue.valueOf(ShieldWallSupport.preventDamage(player, this.eliteEntity, args.checkdouble(2)));
      }));
      boss.set("deactivate_shield_wall", this.method(boss, args -> {
         ShieldWallSupport.deactivate(this.eliteEntity);
         return LuaValue.NIL;
      }));
      boss.set("start_zombie_necronomicon", this.method(boss, args -> {
         LivingEntity target = this.support.resolveLivingEntityReference(args.arg1());
         if (target != null) {
            ZombieNecronomiconSupport.handleBossDamagedByPlayer(this.eliteEntity, target, args.checkjstring(2));
         }

         return LuaValue.NIL;
      }));
      boss.set("send_message", this.method(boss, args -> {
         if (this.eliteEntity.getLocation() != null && this.eliteEntity.getLocation().getWorld() != null) {
            String message = ChatColorConverter.convert(args.checkjstring(1));
            double range = args.optdouble(2, 20.0);
            double rangeSquared = range * range;

            for (Player player : this.eliteEntity.getLocation().getWorld().getPlayers()) {
               if (player.getLocation().distanceSquared(this.eliteEntity.getLocation()) <= rangeSquared) {
                  player.sendMessage(message);
               }
            }

            return LuaValue.NIL;
         } else {
            return LuaValue.NIL;
         }
      }));
      boss.set("get_nearby_players", this.method(boss, args -> this.getNearbyPlayers(args.checkdouble(1))));
      boss.set(
         "get_target_player",
         this.method(
            boss,
            args -> (Varargs)(this.eliteEntity.getLivingEntity() instanceof Mob mob && mob.getTarget() instanceof Player player
               ? this.entityTables.createPlayerTable(player)
               : LuaValue.NIL)
         )
      );
      boss.set("get_nearby_players_in_zone", this.method(boss, args -> this.getPlayersInZone(args.arg1())));
      boss.set("spawn_particles_in_zone", this.method(boss, args -> {
         this.spawnParticlesInZone(args.arg1(), false, this.support.toParticleSpec(args), args.optdouble(8, 1.0));
         return LuaValue.NIL;
      }));
      boss.set("spawn_particles_in_zone_border", this.method(boss, args -> {
         this.spawnParticlesInZone(args.arg1(), true, this.support.toParticleSpec(args), args.optdouble(8, 1.0));
         return LuaValue.NIL;
      }));
      boss.set(
         "get_particles_from_self_toward_zone",
         this.method(
            boss, args -> this.buildDirectionalParticles(args.arg1(), args.checkjstring(2), this.eliteEntity.getLocation(), false, args.optdouble(3, 0.1))
         )
      );
      boss.set(
         "get_particles_toward_self",
         this.method(
            boss,
            args -> this.buildDirectionalParticles(args.arg1(), args.checkjstring(2), this.eliteEntity.getLocation(), true, args.optdouble(args.narg(), 0.1))
         )
      );
      boss.set("spawn_particles_with_vector", this.method(boss, args -> {
         this.spawnDirectionalParticles(args.arg1());
         return LuaValue.NIL;
      }));
      boss.set(
         "summon_reinforcement",
         this.method(
            boss,
            args -> {
               Location location = this.chooseZoneLocation(args.arg(2));
               if (location == null) {
                  location = this.eliteEntity.getLocation();
               }

               CustomBossEntity reinforcement = CustomSummonPower.summonReinforcement(this.eliteEntity, location, args.checkjstring(1), args.optint(3, 0));
               return (Varargs)(reinforcement != null && reinforcement.getLivingEntity() != null
                  ? this.entityTables.createEntityTable(reinforcement.getLivingEntity())
                  : LuaValue.NIL);
            }
         )
      );
      boss.set("summon_projectile", this.method(boss, args -> {
         Location origin = this.support.toLocation(args.arg(2));
         Location destination = this.support.toLocation(args.arg(3));
         if (origin != null && destination != null && origin.getWorld() != null) {
            EntityType entityType = this.support.parseEnum(args.checkjstring(1), EntityType.class, null);
            if (entityType == null) {
               return LuaValue.NIL;
            } else {
               LuaTable options = args.narg() >= 5 && args.arg(5).istable() ? args.arg(5).checktable() : new LuaTable();
               Vector velocity = destination.toVector().subtract(origin.toVector());
               if (velocity.lengthSquared() > 0.0) {
                  velocity.normalize().multiply(args.optdouble(4, 1.0));
               }

               boolean spawnAtOrigin = options.get("spawn_at_origin").optboolean(false);
               boolean directionOnly = options.get("direction_only").optboolean(false);
               Class<? extends Entity> entityClass = entityType.getEntityClass();
               boolean shouldTrack = options.get("track").optboolean(true);
               Entity projectile;
               if (entityClass != null && Projectile.class.isAssignableFrom(entityClass) && this.eliteEntity.getLivingEntity() != null && !spawnAtOrigin) {
                  projectile = this.eliteEntity.getLivingEntity().launchProjectile(entityClass.asSubclass(Projectile.class), velocity);
                  ((Projectile)projectile).setShooter(this.eliteEntity.getLivingEntity());
                  if (shouldTrack) {
                     EntityTracker.registerProjectileEntity((Projectile)projectile);
                  }

                  if (projectile instanceof Fireball fireball && velocity.lengthSquared() > 0.0) {
                     fireball.setDirection(velocity);
                  }
               } else {
                  projectile = origin.getWorld().spawnEntity(origin, entityType);
                  if (projectile instanceof Projectile spawnedProjectile && this.eliteEntity.getLivingEntity() != null) {
                     spawnedProjectile.setShooter(this.eliteEntity.getLivingEntity());
                  }

                  if (projectile instanceof Projectile trackedProjectile && shouldTrack) {
                     EntityTracker.registerProjectileEntity(trackedProjectile);
                  }

                  if (velocity.lengthSquared() > 0.0) {
                     if (projectile instanceof Fireball fireball) {
                        fireball.setDirection(velocity);
                        if (!directionOnly) {
                           projectile.setVelocity(velocity);
                        }
                     } else {
                        projectile.setVelocity(velocity);
                     }
                  }
               }

               if (projectile instanceof Projectile && shouldTrack) {
                  this.eliteEntity.addReinforcement(projectile);
               }

               this.applyGenericSpawnOptions(projectile, options);
               if (options.get("on_land").isfunction()) {
                  this.monitorEntityLanding(projectile, options.get("on_land").checkfunction(), options.get("max_ticks").optint(6000));
               }

               return this.entityTables.createEntityReferenceTable(projectile);
            }
         } else {
            return LuaValue.NIL;
         }
      }));
      if (this.eliteEntity.getLivingEntity() == null) {
         this.entityTables.addEntityEffectMethods(boss, null);
      }

      return boss;
   }

   private LuaValue nearbyEliteTables(double radius) {
      LivingEntity source = this.eliteEntity.getLivingEntity();
      if (this.isAlive(source) && source.getWorld() != null) {
         try {
            NearbyEliteSelectionPolicy.validateRadius(radius);
         } catch (IllegalArgumentException var22) {
            return LuaValue.argerror(1, var22.getMessage());
         }

         Map<UUID, EliteEntity> byId = new HashMap<>();
         List<NearbyEliteSelectionPolicy.Candidate> candidates = new ArrayList<>();
         Location sourceLocation = source.getLocation();
         double radiusSquared = radius * radius;

         for (LivingEntity living : this.support.filterEntities(sourceLocation, radius, radius, radius, "living")) {
            EliteEntity candidate = EntityTracker.getEliteMobEntity(living);
            if (candidate != null
               && candidate != this.eliteEntity
               && candidate.getLivingEntity() == living
               && candidate.exists()
               && candidate.isValid()
               && this.isAlive(living)) {
               Location location = living.getLocation();
               if (sourceLocation.getWorld().equals(location.getWorld())) {
                  UUID candidateId = candidate.getEliteUUID();
                  if (EntityTracker.getEliteMobEntities().get(candidateId) == candidate) {
                     double dx = location.getX() - sourceLocation.getX();
                     double dy = location.getY() - sourceLocation.getY();
                     double dz = location.getZ() - sourceLocation.getZ();
                     double distanceSquared = dx * dx + dy * dy + dz * dz;
                     if (!(distanceSquared > radiusSquared)) {
                        candidates.add(new NearbyEliteSelectionPolicy.Candidate(candidateId, location.getWorld().getUID(), true, true, distanceSquared));
                        byId.put(candidateId, candidate);
                     }
                  }
               }
            }
         }

         List<NearbyEliteSelectionPolicy.Candidate> selected = NearbyEliteSelectionPolicy.select(source.getWorld().getUID(), radius, candidates);
         LuaTable result = new LuaTable();
         int index = 1;

         for (NearbyEliteSelectionPolicy.Candidate selectedCandidate : selected) {
            EliteEntity candidate = byId.get(selectedCandidate.entityId());
            LivingEntity livingx = candidate == null ? null : candidate.getLivingEntity();
            if (candidate != null
               && EntityTracker.getEliteMobEntities().get(selectedCandidate.entityId()) == candidate
               && candidate.exists()
               && candidate.isValid()
               && this.isAlive(livingx)) {
               result.set(index++, this.entityTables.createLivingEntityTable(livingx));
            }
         }

         return result;
      } else {
         return new LuaTable();
      }
   }

   private Entity resolveBossMountEntity() {
      if (this.eliteEntity instanceof CustomBossEntity customBossEntity) {
         Entity mountEntity = customBossEntity.getMountEntity();
         if (mountEntity != null && mountEntity.isValid()) {
            return mountEntity;
         }
      }

      LivingEntity livingEntity = this.eliteEntity.getLivingEntity();
      if (livingEntity != null && livingEntity.isInsideVehicle()) {
         Entity vehicle = livingEntity.getVehicle();
         if (vehicle != null && vehicle.isValid()) {
            return vehicle;
         }
      }

      return null;
   }

   private LuaValue getNearbyPlayers(double range) {
      LuaTable results = new LuaTable();
      if (this.eliteEntity.getLocation() != null && this.eliteEntity.getLocation().getWorld() != null) {
         int index = 1;
         double rangeSquared = range * range;

         for (Player player : this.eliteEntity.getLocation().getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(this.eliteEntity.getLocation()) <= rangeSquared) {
               results.set(index++, this.entityTables.createPlayerTable(player));
            }
         }

         return results;
      } else {
         return results;
      }
   }

   private LuaValue getPlayersInZone(LuaValue zoneValue) {
      LuaTable results = new LuaTable();
      Shape shape = this.support.createShape(zoneValue);
      if (shape != null && this.eliteEntity.getLocation() != null && this.eliteEntity.getLocation().getWorld() != null) {
         int index = 1;

         for (Player player : this.eliteEntity.getLocation().getWorld().getPlayers()) {
            if (shape.contains(player)) {
               results.set(index++, this.entityTables.createPlayerTable(player));
            }
         }

         return results;
      } else {
         return results;
      }
   }

   private void spawnParticlesInZone(LuaValue zoneValue, boolean border, LuaTable particle, double coverage) {
      Shape shape = this.support.createShape(zoneValue);
      if (shape != null) {
         for (Location location : this.sampleLocations(shape, border, coverage)) {
            this.support.spawnParticle(location, particle, particle.get("amount").optint(1));
         }
      }
   }

   private LuaTable buildDirectionalParticles(LuaValue zoneValue, String particleKey, Location anchor, boolean towardAnchor, double speed) {
      LuaTable results = new LuaTable();
      Shape shape = this.support.createShape(zoneValue);
      if (shape != null && anchor != null) {
         int index = 1;

         for (Location location : this.sampleLocations(shape, false, 0.2)) {
            Vector direction = towardAnchor ? anchor.toVector().subtract(location.toVector()) : location.toVector().subtract(anchor.toVector());
            if (!(direction.lengthSquared() <= 0.0)) {
               direction.normalize();
               LuaTable particle = new LuaTable();
               particle.set("location", this.support.toLocationTable(location));
               particle.set("particle", LuaValue.valueOf(particleKey));
               particle.set("amount", LuaValue.ZERO);
               particle.set("x", LuaValue.valueOf(direction.getX()));
               particle.set("y", LuaValue.valueOf(direction.getY()));
               particle.set("z", LuaValue.valueOf(direction.getZ()));
               particle.set("speed", LuaValue.valueOf(speed));
               results.set(index++, particle);
            }
         }

         return results;
      } else {
         return results;
      }
   }

   private void spawnDirectionalParticles(LuaValue particlesValue) {
      if (particlesValue.istable()) {
         LuaTable particles = particlesValue.checktable();
         LuaValue key = LuaValue.NIL;

         while (true) {
            Varargs next = particles.next(key);
            key = next.arg1();
            if (key.isnil()) {
               return;
            }

            LuaValue particleValue = next.arg(2);
            if (particleValue.istable()) {
               LuaTable particle = particleValue.checktable();
               this.support.spawnParticle(this.support.toLocation(particle.get("location")), particle, particle.get("amount").optint(1));
            }
         }
      }
   }

   private List<Location> sampleLocations(Shape shape, boolean border, double coverage) {
      List<Location> source = border ? shape.getEdgeLocations() : shape.getLocations();
      if (source.isEmpty()) {
         return source;
      } else {
         double normalizedCoverage = Math.max(0.01, Math.min(1.0, coverage));
         List<Location> sampled = new ArrayList<>();

         for (Location location : source) {
            if (Math.random() <= normalizedCoverage) {
               sampled.add(location);
            }
         }

         if (sampled.isEmpty()) {
            sampled.add(source.get(0));
         }

         return sampled;
      }
   }

   private Location chooseZoneLocation(LuaValue zoneValue) {
      Shape shape = this.support.createShape(zoneValue);
      if (shape == null) {
         return this.support.toLocation(zoneValue);
      } else {
         List<Location> locations = shape.getLocations();
         return locations.isEmpty() ? shape.getCenter() : locations.get((int)(Math.random() * locations.size()));
      }
   }

   private void applyGenericSpawnOptions(Entity entity, LuaTable options) {
      if (entity instanceof Projectile projectile && options.get("custom_damage").isnumber()) {
         ProjectileTagger.tagProjectileWithCustomDamage(projectile, options.get("custom_damage").todouble());
      }

      if (entity instanceof Projectile projectile && options.get("detonation_power").isstring()) {
         EliteProjectile.signExplosiveWithPower(projectile, options.get("detonation_power").tojstring());
      }

      if (entity instanceof Fireball fireball) {
         if (options.get("yield").isnumber()) {
            fireball.setYield((float)options.get("yield").todouble());
         }

         if (!options.get("incendiary").isnil()) {
            fireball.setIsIncendiary(options.get("incendiary").optboolean(true));
         }
      }

      if (!options.get("gravity").isnil()) {
         entity.setGravity(options.get("gravity").optboolean(true));
      }

      if (!options.get("glowing").isnil()) {
         entity.setGlowing(options.get("glowing").optboolean(false));
      }

      if (!options.get("invulnerable").isnil()) {
         entity.setInvulnerable(options.get("invulnerable").optboolean(false));
      }

      if (!options.get("persistent").isnil()) {
         entity.setPersistent(options.get("persistent").optboolean(true));
      }

      int duration = options.get("duration").optint(0);
      if (duration > 0) {
         GameClock.scheduleLater(duration, () -> {
            if (entity.isValid()) {
               entity.remove();
            }
         });
      }

      String effectName = options.get("effect").optjstring(null);
      if (effectName != null) {
         try {
            entity.playEffect(EntityEffect.valueOf(effectName.toUpperCase(Locale.ROOT)));
         } catch (IllegalArgumentException var6) {
         }
      }
   }

   private void monitorEntityLanding(final Entity entity, final LuaFunction callback, final int maxTicks) {
      final int[] taskId = new int[1];
      taskId[0] = this.taskController
         .runRepeating(
            1,
            1,
            new Runnable() {
               private int counter = 0;

               @Override
               public void run() {
                  if (entity.isValid() && !entity.isOnGround() && this.counter <= maxTicks) {
                     this.counter++;
                  } else {
                     LuaBossTableBuilder.this.taskController.cancel(taskId[0]);
                     LuaBossTableBuilder.this.callbackInvoker
                        .invoke(
                           "a launched entity landing callback",
                           callback,
                           LuaBossTableBuilder.this.support.toLocationTable(entity.getLocation()),
                           LuaBossTableBuilder.this.entityTables.createEntityReferenceTable(entity)
                        );
                  }
               }
            }
         );
   }

   private boolean isAlive(LivingEntity livingEntity) {
      return livingEntity != null && livingEntity.isValid() && !livingEntity.isDead();
   }

   private float resolveVolume(Varargs args) {
      return args.narg() >= 2 && args.arg(2).istable() ? (float)args.arg(2).checktable().get("volume").optdouble(1.0) : this.support.getFloat(args, 2, 1.0F);
   }

   private float resolvePitch(Varargs args) {
      return args.narg() >= 2 && args.arg(2).istable() ? (float)args.arg(2).checktable().get("pitch").optdouble(1.0) : this.support.getFloat(args, 3, 1.0F);
   }

   private Vector patrolOffset(Varargs args) {
      return new Vector(args.checkdouble(1), args.checkdouble(2), args.checkdouble(3));
   }

   private VarArgFunction method(final LuaTable owner, final LuaBossTableBuilder.LuaCallback callback) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            return callback.invoke(LuaBossTableBuilder.this.stripMethodSelf(args, owner));
         }
      };
   }

   private Varargs stripMethodSelf(Varargs args, LuaTable owner) {
      if (args.narg() != 0 && args.arg1().raweq(owner)) {
         LuaValue[] stripped = new LuaValue[Math.max(0, args.narg() - 1)];

         for (int index = 2; index <= args.narg(); index++) {
            stripped[index - 2] = args.arg(index);
         }

         return LuaValue.varargsOf(stripped);
      } else {
         return args;
      }
   }

   @FunctionalInterface
   private interface LuaCallback {
      Varargs invoke(Varargs var1);
   }
}
