package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.EliteDamageEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.api.ScriptZoneEnterEvent;
import com.magmaguy.elitemobs.api.ScriptZoneLeaveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventMountExploit;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.BossCustomAttackDamage;
import com.magmaguy.elitemobs.instanced.InstanceEffectPolicy;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.powers.scripts.ScriptListener;
import com.magmaguy.elitemobs.powers.scripts.TimedScriptStateManager;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

final class LuaPowerEntityTables {
   private final ScriptDefinition definition;
   private final EliteEntity eliteEntity;
   private final LuaPowerSupport support;
   private final LuaPowerScriptApi.OwnedTaskController taskController;
   private final LuaPowerScriptApi.CallbackInvoker callbackInvoker;
   private final LuaBossTableBuilder bossTableBuilder;
   private static final Map<EliteEntity, LuaPowerEntityTables> healingOwners = new IdentityHashMap<>();
   private final Set<EliteEntity> healingTargets = Collections.newSetFromMap(new IdentityHashMap<>());

   LuaPowerEntityTables(
      ScriptDefinition definition,
      EliteEntity eliteEntity,
      LuaPowerSupport support,
      LuaPowerScriptApi.OwnedTaskController taskController,
      LuaPowerScriptApi.CallbackInvoker callbackInvoker
   ) {
      this.definition = definition;
      this.eliteEntity = eliteEntity;
      this.support = support;
      this.taskController = taskController;
      this.callbackInvoker = callbackInvoker;
      this.bossTableBuilder = new LuaBossTableBuilder(definition, eliteEntity, support, this, taskController, callbackInvoker);
      taskController.ownCleanup(() -> {
         TimedScriptStateManager.releaseOwner(this);

         for (EliteEntity target : this.healingTargets) {
            if (healingOwners.remove(target, this)) {
               target.setHealing(false);
            }
         }

         this.healingTargets.clear();
      });
   }

   LuaTable createBossTable() {
      return this.bossTableBuilder.build();
   }

   LuaTable createEventTable(Event event) {
      LuaTable eventTable = new LuaTable();
      if (event instanceof EliteMobDamagedByPlayerEvent playerDamage) {
         eventTable.set("entity", this.createEntityTable(playerDamage.getEliteMobEntity().getLivingEntity()));
         eventTable.set("is_damage_transfer", LuaValue.valueOf(CombatDamageContext.isDamageTransferActive()));
         eventTable.set(
            "transfer_damage",
            this.method(
               eventTable,
               args -> {
                  Entity target = this.support.resolveEntityReference(args.arg1());
                  double amount = args.checkdouble(2);
                  if (!Double.isFinite(amount) || amount < 0.0) {
                     throw new IllegalArgumentException("Invalid transferred damage");
                  } else if (!CombatDamageContext.isDamageTransferActive() && !playerDamage.isCancelled() && amount != 0.0) {
                     if (target instanceof LivingEntity living
                        && this.isAlive(living)
                        && InstanceEffectPolicy.canAffect(this.eliteEntity, living)
                        && !target.equals(playerDamage.getEliteMobEntity().getLivingEntity())) {
                        EliteEntity recipient = EntityTracker.getEliteMobEntity(living);
                        if (recipient == null) {
                           throw new IllegalArgumentException("Damage transfers require an elite recipient");
                        } else {
                           int previousTicks = NMSManager.getAdapter().getDamageCooldownTicks(living);
                           double previousDamage = living.getLastDamage();

                           try {
                              NMSManager.getAdapter().setDamageCooldownTicks(living, 0);
                              CombatDamageContext.runPlayerToEliteTransfer(() -> living.damage(amount, playerDamage.getPlayer()));
                           } finally {
                              if (this.isAlive(living)) {
                                 NMSManager.getAdapter().setDamageCooldownTicks(living, previousTicks);
                                 living.setLastDamage(previousDamage);
                              }
                           }

                           return LuaValue.TRUE;
                        }
                     } else {
                        return LuaValue.FALSE;
                     }
                  } else {
                     return LuaValue.FALSE;
                  }
               }
            )
         );
      }

      if (event instanceof EliteDamageEvent eliteDamageEvent) {
         eventTable.set("damage_amount", LuaValue.valueOf(eliteDamageEvent.getDamage()));
         eventTable.set("get_damage_amount", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
               return LuaValue.valueOf(eliteDamageEvent.getDamage());
            }
         });
         if (event instanceof EliteMobDamagedEvent eliteMobDamagedEvent) {
            eventTable.set("damage_cause", LuaValue.valueOf(eliteMobDamagedEvent.getEntityDamageEvent().getCause().name()));
         } else if (event instanceof EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent) {
            eventTable.set("damage_cause", LuaValue.valueOf(eliteMobDamagedByPlayerEvent.getEntityDamageByEntityEvent().getCause().name()));
         } else if (event instanceof EliteMobDamagedByEliteMobEvent eliteMobDamagedByEliteMobEvent) {
            eventTable.set("damage_cause", LuaValue.valueOf(eliteMobDamagedByEliteMobEvent.getEntityDamageByEntityEvent().getCause().name()));
         } else if (event instanceof PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent) {
            eventTable.set("damage_cause", LuaValue.valueOf(playerDamagedByEliteMobEvent.getEntityDamageByEntityEvent().getCause().name()));
         }

         eventTable.set("cancel_event", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
               eliteDamageEvent.setCancelled(true);
               return LuaValue.NIL;
            }
         });
         eventTable.set("set_damage_amount", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
               eliteDamageEvent.setDamage(args.checkdouble(1));
               return LuaValue.NIL;
            }
         });
         eventTable.set("multiply_damage_amount", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
               eliteDamageEvent.setDamage(eliteDamageEvent.getDamage() * args.checkdouble(1));
               return LuaValue.NIL;
            }
         });
         EntityDamageByEntityEvent entityDamageByEntityEvent = null;
         if (event instanceof EliteMobDamagedEvent eliteMobDamagedEvent
            && eliteMobDamagedEvent.getEntityDamageEvent() instanceof EntityDamageByEntityEvent damageByEntityEvent) {
            entityDamageByEntityEvent = damageByEntityEvent;
         } else if (event instanceof EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent) {
            entityDamageByEntityEvent = eliteMobDamagedByPlayerEvent.getEntityDamageByEntityEvent();
         } else if (event instanceof EliteMobDamagedByEliteMobEvent eliteMobDamagedByEliteMobEvent) {
            entityDamageByEntityEvent = eliteMobDamagedByEliteMobEvent.getEntityDamageByEntityEvent();
         } else if (event instanceof PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent) {
            entityDamageByEntityEvent = playerDamagedByEliteMobEvent.getEntityDamageByEntityEvent();
         }

         if (entityDamageByEntityEvent != null) {
            Entity damager = entityDamageByEntityEvent.getDamager();
            eventTable.set("damager", damager == null ? LuaValue.NIL : this.createEntityReferenceTable(damager));
            if (damager instanceof Projectile projectile) {
               eventTable.set("projectile", this.createEntityReferenceTable(projectile));
            }
         }
      } else if (event instanceof Cancellable cancellable) {
         eventTable.set("cancel_event", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
               cancellable.setCancelled(true);
               return LuaValue.NIL;
            }
         });
      }

      if (event instanceof EliteMobSpawnEvent spawnEvent) {
         eventTable.set("spawn_reason", LuaValue.valueOf(spawnEvent.getReason().name()));
      }

      if (event instanceof EliteMobDeathEvent deathEvent) {
         eventTable.set("entity", this.createEntityReferenceTable(deathEvent.getEntity()));
      }

      if (event instanceof ScriptZoneEnterEvent zoneEnterEvent) {
         eventTable.set("entity", this.createEntityTable(zoneEnterEvent.getEntity()));
      } else if (event instanceof ScriptZoneLeaveEvent zoneLeaveEvent) {
         eventTable.set("entity", this.createEntityTable(zoneLeaveEvent.getEntity()));
      }

      return eventTable;
   }

   LuaTable createEntityTable(LivingEntity livingEntity) {
      return livingEntity instanceof Player player ? this.createPlayerTable(player) : this.createLivingEntityTable(livingEntity);
   }

   LuaValue createEntityReferenceTable(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         return this.createEntityTable(livingEntity);
      } else {
         LuaTable table = new LuaTable();
         table.set("name", LuaValue.valueOf(entity.getName()));
         table.set("uuid", LuaValue.valueOf(entity.getUniqueId().toString()));
         table.rawset("__bukkit_uuid", LuaValue.valueOf(entity.getUniqueId().toString()));
         table.set("entity_type", LuaValue.valueOf(entity.getType().name()));
         table.set("is_player", LuaValue.FALSE);
         table.set("is_elite", LuaValue.FALSE);
         table.set("is_mount", LuaValue.FALSE);
         table.set("is_valid", this.method(table, args -> LuaValue.valueOf(entity.isValid())));
         this.addVehicleMethods(table, entity);
         table.set("current_location", this.support.toLocationTable(entity.getLocation()));
         table.set("get_location", this.method(table, args -> this.support.toLocationTable(entity.getLocation())));
         table.set("get_velocity", this.method(table, args -> this.support.toVectorTable(entity.getVelocity())));
         table.set("is_on_ground", this.method(table, args -> LuaValue.valueOf(entity.isOnGround())));
         table.set("teleport_to_location", this.method(table, args -> {
            Location destination = this.support.toLocation(args.arg1());
            if (destination != null) {
               entity.teleport(destination);
            }

            return LuaValue.NIL;
         }));
         table.set("set_velocity_vector", this.method(table, args -> {
            Vector velocity = this.support.toVector(args.arg1());
            if (velocity != null) {
               entity.setVelocity(velocity);
            }

            return LuaValue.NIL;
         }));
         table.set("set_direction_vector", this.method(table, args -> {
            Vector direction = this.support.toVector(args.arg1());
            if (direction != null && entity instanceof Fireball fireball) {
               fireball.setDirection(direction);
            }

            return LuaValue.NIL;
         }));
         table.set("set_yield", this.method(table, args -> {
            if (entity instanceof Fireball fireball) {
               fireball.setYield((float)args.checkdouble(1));
            }

            return LuaValue.NIL;
         }));
         table.set("set_gravity", this.method(table, args -> {
            entity.setGravity(args.checkboolean(1));
            return LuaValue.NIL;
         }));
         table.set("detonate", this.method(table, args -> {
            if (entity instanceof Firework firework) {
               firework.detonate();
            }

            return LuaValue.NIL;
         }));
         table.set("remove", this.method(table, args -> {
            ScriptListener.forgetFallingEntity(entity);
            entity.remove();
            return LuaValue.NIL;
         }));
         table.set("unregister", this.method(table, args -> {
            EntityTracker.unregister(entity, this.support.parseEnum(args.optjstring(1, "OTHER"), RemovalReason.class, RemovalReason.OTHER));
            return LuaValue.NIL;
         }));
         return table;
      }
   }

   LuaTable createPlayerTable(Player player) {
      LuaTable table = this.createLivingEntityTable(player);
      table.set("send_message", this.method(table, args -> {
         player.sendMessage(ChatColorConverter.convert(args.checkjstring(1)));
         return LuaValue.NIL;
      }));
      table.set("show_action_bar", this.method(table, args -> {
         ActionBarCompositor.show(player, ActionBarCompositor.Source.LUA, ChatColorConverter.convert(args.checkjstring(1)));
         return LuaValue.NIL;
      }));
      table.set(
         "show_title",
         this.method(
            table,
            args -> {
               player.sendTitle(
                  ChatColorConverter.convert(args.checkjstring(1)),
                  ChatColorConverter.convert(args.optjstring(2, "")),
                  args.optint(3, 10),
                  args.optint(4, 40),
                  args.optint(5, 10)
               );
               return LuaValue.NIL;
            }
         )
      );
      VarArgFunction showBossBar = this.method(table, args -> {
         String title = ChatColorConverter.convert(args.checkjstring(1));
         BarColor color = this.support.parseEnum(args.optjstring(2, "WHITE"), BarColor.class, BarColor.WHITE);
         BarStyle style = this.support.parseEnum(args.optjstring(3, "SOLID"), BarStyle.class, BarStyle.SOLID);
         int duration = args.optint(4, 40);
         BossBar bossBar = Bukkit.createBossBar(title, color, style, new BarFlag[0]);
         BossBarOrderManager.show(player, bossBar);
         if (duration > 0) {
            GameClock.scheduleLater(duration, () -> {
               BossBarOrderManager.hide(player, bossBar);
               bossBar.removeAll();
            });
         }

         return LuaValue.NIL;
      });
      table.set("show_boss_bar", showBossBar);
      VarArgFunction runCommand = this.method(table, args -> {
         player.performCommand(args.checkjstring(1));
         return LuaValue.NIL;
      });
      table.set("run_command", runCommand);
      table.set("game_mode", LuaValue.valueOf(player.getGameMode().name()));
      return table;
   }

   LuaTable createLivingEntityTable(LivingEntity livingEntity) {
      LuaTable entity = new LuaTable();
      entity.set("name", LuaValue.valueOf(livingEntity.getName()));
      entity.set("uuid", LuaValue.valueOf(livingEntity.getUniqueId().toString()));
      entity.rawset("__bukkit_uuid", LuaValue.valueOf(livingEntity.getUniqueId().toString()));
      entity.set("entity_type", LuaValue.valueOf(livingEntity.getType().name()));
      entity.set("is_player", LuaValue.valueOf(livingEntity instanceof Player));
      entity.set("is_monster", LuaValue.valueOf(livingEntity instanceof Monster));
      EliteEntity trackedElite = EntityTracker.getEliteMobEntity(livingEntity);
      entity.set("is_elite", LuaValue.valueOf(trackedElite != null));
      entity.set("is_mount", LuaValue.valueOf(trackedElite instanceof CustomBossEntity customBossEntity && customBossEntity.isMount()));
      entity.set("is_valid", LuaValue.valueOf(livingEntity.isValid()));
      this.addVehicleMethods(entity, livingEntity);
      entity.set("is_alive", this.method(entity, args -> LuaValue.valueOf(this.isAlive(livingEntity))));
      entity.set("is_ai_enabled", this.method(entity, args -> {
         EliteEntity elite = this.resolveAiOwner(livingEntity);
         return LuaValue.valueOf(elite == null ? livingEntity.hasAI() : elite.isAIEnabled());
      }));
      entity.set(
         "is_frozen",
         this.method(
            entity,
            args -> EntityTracker.getEliteMobEntity(livingEntity) instanceof CustomBossEntity customBoss
               ? LuaValue.valueOf(customBoss.getCustomBossesConfigFields().isFrozen())
               : LuaValue.FALSE
         )
      );
      entity.set("health", LuaValue.valueOf(livingEntity.getHealth()));
      entity.set("maximum_health", LuaValue.valueOf(Objects.requireNonNull(livingEntity.getAttribute(Attribute.MAX_HEALTH)).getValue()));
      entity.set("current_location", this.support.toLocationTable(livingEntity.getLocation()));
      entity.set("get_location", this.method(entity, args -> this.support.toLocationTable(livingEntity.getLocation())));
      entity.set("get_eye_location", this.method(entity, args -> this.support.toLocationTable(livingEntity.getEyeLocation())));
      entity.set("get_height", this.method(entity, args -> LuaValue.valueOf(livingEntity.getHeight())));
      entity.set("get_health", this.method(entity, args -> LuaValue.valueOf(livingEntity.getHealth())));
      entity.set("get_maximum_health", this.method(entity, args -> {
         AttributeInstance maxHealth = livingEntity.getAttribute(Attribute.MAX_HEALTH);
         return LuaValue.valueOf(maxHealth == null ? 0.0 : maxHealth.getValue());
      }));
      entity.set("get_velocity", this.method(entity, args -> this.support.toVectorTable(livingEntity.getVelocity())));
      entity.set("deal_damage", this.method(entity, args -> {
         livingEntity.damage(args.checkdouble(1));
         return LuaValue.NIL;
      }));
      entity.set("deal_custom_damage", this.method(entity, args -> {
         if (this.eliteEntity.getLivingEntity() != null) {
            BossCustomAttackDamage.dealCustomDamage(this.eliteEntity.getLivingEntity(), livingEntity, args.checkdouble(1));
         }

         return LuaValue.NIL;
      }));
      entity.set("deal_damage_from_boss", this.method(entity, args -> {
         if (this.eliteEntity.getLivingEntity() != null) {
            livingEntity.damage(args.checkdouble(1), this.eliteEntity.getLivingEntity());
         }

         return LuaValue.NIL;
      }));
      entity.set("restore_health", this.method(entity, args -> {
         double amount = args.checkdouble(1);
         EliteEntity healTargetElite = EntityTracker.getEliteMobEntity(livingEntity);
         if (healTargetElite != null) {
            healTargetElite.heal(amount);
         } else {
            double maxHealth = AttributeManager.getAttributeBaseValue(livingEntity, "generic_max_health");
            livingEntity.setHealth(Math.min(maxHealth, livingEntity.getHealth() + amount));
         }

         return LuaValue.NIL;
      }));
      entity.set("play_sound_at_entity", this.method(entity, args -> {
         this.support.playSound(livingEntity.getLocation(), args.checkjstring(1), this.resolveVolume(args), this.resolvePitch(args));
         return LuaValue.NIL;
      }));
      entity.set("play_sound_at_self", this.method(entity, args -> {
         this.support.playSound(livingEntity.getLocation(), args.checkjstring(1), this.resolveVolume(args), this.resolvePitch(args));
         return LuaValue.NIL;
      }));
      entity.set("spawn_particle_at_self", this.method(entity, args -> {
         this.support.spawnParticle(livingEntity.getLocation(), args.arg(1), args.optint(2, 1));
         return LuaValue.NIL;
      }));
      entity.set("spawn_particles_at_location", this.method(entity, args -> {
         this.support.spawnParticle(this.support.toLocation(args.arg1()), this.support.toParticleSpec(args), args.optint(3, 1));
         return LuaValue.NIL;
      }));
      entity.set("teleport_to_location", this.method(entity, args -> {
         Location destination = this.support.toLocation(args.arg1());
         if (destination != null) {
            livingEntity.teleport(destination);
         }

         return LuaValue.NIL;
      }));
      entity.set("set_velocity_vector", this.method(entity, args -> {
         Vector velocity = this.support.toVector(args.arg1());
         if (velocity != null) {
            livingEntity.setVelocity(velocity);
         }

         return LuaValue.NIL;
      }));
      entity.set("set_gravity", this.method(entity, args -> {
         livingEntity.setGravity(args.checkboolean(1));
         return LuaValue.NIL;
      }));
      entity.set("is_on_ground", this.method(entity, args -> LuaValue.valueOf(livingEntity.isOnGround())));
      entity.set("apply_push_vector", this.method(entity, args -> {
         Vector velocity = this.support.toVector(args.arg1());
         boolean additive = args.optboolean(2, false);
         int delay = args.optint(3, 1);
         if (velocity == null) {
            return LuaValue.NIL;
         } else {
            GameClock.scheduleLater(delay, () -> {
               if (livingEntity.isValid()) {
                  livingEntity.setVelocity(additive ? livingEntity.getVelocity().add(velocity) : velocity);
               }
            });
            return LuaValue.NIL;
         }
      }));
      entity.set("set_custom_name", this.method(entity, args -> {
         livingEntity.setCustomName(ChatColorConverter.convert(args.checkjstring(1)));
         return LuaValue.NIL;
      }));
      entity.set("reset_custom_name", this.method(entity, args -> {
         EliteEntity namedElite = EntityTracker.getEliteMobEntity(livingEntity);
         livingEntity.setCustomName(namedElite != null ? namedElite.getName() : livingEntity.getName());
         return LuaValue.NIL;
      }));
      entity.set("set_custom_name_visible", this.method(entity, args -> {
         livingEntity.setCustomNameVisible(args.checkboolean(1));
         return LuaValue.NIL;
      }));
      entity.set("set_ai_enabled", this.method(entity, args -> {
         this.applyAiState(livingEntity, args.checkboolean(1), args.optint(2, 0));
         return LuaValue.NIL;
      }));
      entity.set("set_awareness_enabled", this.method(entity, args -> {
         if (livingEntity instanceof Mob mob) {
            boolean aware = args.checkboolean(1);
            int duration = args.optint(2, 0);
            if (!aware || !TimedScriptStateManager.releaseProperty(this, mob.getUniqueId(), "mob_aware")) {
               TimedScriptStateManager.applyOwned(this, mob.getUniqueId(), "mob_aware", aware, duration, () -> {
                  EliteEntity owner = this.resolveAiOwner(mob);
                  return owner == null ? mob.isAware() : owner.isAware();
               }, value -> this.setAwarenessState(mob, value));
            }
         }

         return LuaValue.NIL;
      }));
      entity.set("face_direction_or_location", this.method(entity, args -> {
         LuaValue target = args.arg1();
         Location location = livingEntity.getLocation();
         Vector direction;
         if (target.istable() && (target.get("world").isstring() || target.get("current_location").istable())) {
            Location destination = this.support.toLocation(target);
            if (destination == null || location.getWorld() == null || !location.getWorld().equals(destination.getWorld())) {
               return LuaValue.NIL;
            }

            direction = destination.toVector().subtract(location.toVector());
         } else {
            direction = this.support.toVector(target);
         }

         if (direction != null && direction.lengthSquared() > 0.0) {
            location.setDirection(direction);
            livingEntity.teleport(location);
         }

         return LuaValue.NIL;
      }));
      entity.set("play_model_animation", this.method(entity, args -> {
         if (EntityTracker.getEliteMobEntity(livingEntity) instanceof CustomBossEntity customBossEntityx && customBossEntityx.getCustomModel() != null) {
            customBossEntityx.getCustomModel().playAnimationByName(args.checkjstring(1));
         }

         return LuaValue.NIL;
      }));
      entity.set(
         "set_scale",
         this.method(
            entity,
            args -> {
               double scale = args.checkdouble(1);
               int duration = args.optint(2, 0);
               AttributeInstance attribute = AttributeManager.getAttributeInstance(livingEntity, "generic_scale");
               if (attribute != null) {
                  TimedScriptStateManager.applyOwned(
                     this, livingEntity.getUniqueId(), "generic_scale", scale, duration, attribute::getBaseValue, attribute::setBaseValue
                  );
               }

               return LuaValue.NIL;
            }
         )
      );
      entity.set("set_invulnerable", this.method(entity, args -> {
         TimedScriptStateManager.applyInvulnerability(this, livingEntity, args.checkboolean(1), args.optint(2, 0));
         return LuaValue.NIL;
      }));
      entity.set("remove_elite", this.method(entity, args -> {
         EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
         if (entityRef != null) {
            entityRef.remove(RemovalReason.OTHER);
         }

         return LuaValue.NIL;
      }));
      entity.set("is_healing", this.method(entity, args -> {
         EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
         return LuaValue.valueOf(entityRef != null && entityRef.isHealing());
      }));
      entity.set("set_healing", this.method(entity, args -> {
         EliteEntity entityRef = EntityTracker.getEliteMobEntity(livingEntity);
         if (entityRef != null) {
            boolean healing = args.checkboolean(1);
            if (healing && !entityRef.isHealing()) {
               healingOwners.put(entityRef, this);
               this.healingTargets.add(entityRef);
               entityRef.setHealing(true);
            } else if (!healing && healingOwners.remove(entityRef, this)) {
               this.healingTargets.remove(entityRef);
               entityRef.setHealing(false);
            }
         }

         return LuaValue.NIL;
      }));
      entity.set("navigate_to_location", this.method(entity, args -> {
         if (EntityTracker.getEliteMobEntity(livingEntity) instanceof CustomBossEntity customBossEntityx) {
            Location destination = this.support.toLocation(args.arg1());
            if (destination != null) {
               Navigation.navigateTo(customBossEntityx, args.optdouble(2, 1.0), destination, args.optboolean(3, false), args.optint(4, 0));
            }
         }

         return LuaValue.NIL;
      }));
      entity.set("add_tag", this.method(entity, args -> {
         this.support.applyTag(livingEntity, args.checkjstring(1), true);
         this.scheduleTagRemoval(livingEntity, args.optint(2, 0), args.checkjstring(1));
         return LuaValue.NIL;
      }));
      entity.set("remove_tag", this.method(entity, args -> {
         this.support.applyTag(livingEntity, args.checkjstring(1), false);
         return LuaValue.NIL;
      }));
      entity.set("has_tag", this.method(entity, args -> LuaValue.valueOf(this.support.hasTag(livingEntity, args.checkjstring(1)))));
      entity.set("push_relative_to", this.method(entity, args -> {
         Location source = this.support.toLocation(args.arg1());
         if (source != null && livingEntity.isValid()) {
            Vector relativePush = livingEntity.getLocation().toVector().subtract(source.toVector());
            if (relativePush.lengthSquared() > 0.0) {
               relativePush.normalize().multiply(args.optdouble(2, 1.0));
            }

            relativePush.add(new Vector(args.optdouble(3, 0.0), args.optdouble(4, 0.0), args.optdouble(5, 0.0)));
            livingEntity.setVelocity(relativePush);
            return LuaValue.NIL;
         } else {
            return LuaValue.NIL;
         }
      }));
      entity.set(
         "overlaps_box_at_location",
         this.method(
            entity,
            args -> {
               Location center = this.support.toLocation(args.arg1());
               if (center == null) {
                  return LuaValue.FALSE;
               } else {
                  double halfX = args.optdouble(2, 0.5);
                  double halfY = args.optdouble(3, halfX);
                  double halfZ = args.optdouble(4, halfX);
                  return LuaValue.valueOf(
                     livingEntity.getBoundingBox()
                        .overlaps(
                           new Vector(center.getX() - halfX, center.getY() - halfY, center.getZ() - halfZ),
                           new Vector(center.getX() + halfX, center.getY() + halfY, center.getZ() + halfZ)
                        )
                  );
               }
            }
         )
      );
      this.addEntityEffectMethods(entity, livingEntity);
      return entity;
   }

   private void addVehicleMethods(LuaTable entityTable, Entity entity) {
      entityTable.set("is_inside_vehicle", this.method(entityTable, args -> LuaValue.valueOf(entity.isInsideVehicle())));
      entityTable.set("has_vehicle", this.method(entityTable, args -> LuaValue.valueOf(entity.getVehicle() != null)));
      entityTable.set("get_vehicle", this.method(entityTable, args -> {
         Entity vehicle = entity.getVehicle();
         return vehicle == null ? LuaValue.NIL : this.createEntityReferenceTable(vehicle);
      }));
      entityTable.set("set_vehicle", this.method(entityTable, args -> {
         Entity vehicle = this.support.resolveEntityReference(args.arg1());
         return LuaValue.valueOf(this.mountPassenger(vehicle, entity));
      }));
      entityTable.set("leave_vehicle", this.method(entityTable, args -> LuaValue.valueOf(entity.leaveVehicle())));
      entityTable.set("has_passengers", this.method(entityTable, args -> LuaValue.valueOf(!entity.getPassengers().isEmpty())));
      entityTable.set("get_passenger_count", this.method(entityTable, args -> LuaValue.valueOf(entity.getPassengers().size())));
      entityTable.set("get_passengers", this.method(entityTable, args -> {
         LuaTable passengers = new LuaTable();
         int index = 1;

         for (Entity passenger : entity.getPassengers()) {
            passengers.set(index++, this.createEntityReferenceTable(passenger));
         }

         return passengers;
      }));
      entityTable.set("add_passenger", this.method(entityTable, args -> {
         Entity passenger = this.support.resolveEntityReference(args.arg1());
         return LuaValue.valueOf(this.mountPassenger(entity, passenger));
      }));
      entityTable.set("remove_passenger", this.method(entityTable, args -> {
         Entity passenger = this.support.resolveEntityReference(args.arg1());
         return LuaValue.valueOf(passenger != null && entity.removePassenger(passenger));
      }));
      entityTable.set("eject_passengers", this.method(entityTable, args -> LuaValue.valueOf(entity.eject())));
   }

   boolean mountPassenger(Entity vehicle, Entity passenger) {
      return PreventMountExploit.addPassenger(vehicle, passenger);
   }

   private boolean isAlive(LivingEntity livingEntity) {
      return livingEntity != null && livingEntity.isValid() && !livingEntity.isDead();
   }

   void applyAiState(LivingEntity livingEntity, boolean targetValue, int duration) {
      if (livingEntity != null) {
         if (!targetValue || !TimedScriptStateManager.releaseProperty(this, livingEntity.getUniqueId(), "mob_ai")) {
            TimedScriptStateManager.applyOwned(this, livingEntity.getUniqueId(), "mob_ai", targetValue, duration, () -> {
               EliteEntity owner = this.resolveAiOwner(livingEntity);
               return owner == null ? livingEntity.hasAI() : owner.isAIEnabled();
            }, value -> this.setAiState(livingEntity, value));
         }
      }
   }

   private void setAiState(LivingEntity livingEntity, boolean enabled) {
      EliteEntity elite = this.resolveAiOwner(livingEntity);
      if (elite == null) {
         livingEntity.setAI(enabled);
      } else {
         elite.setAIEnabled(enabled);
      }
   }

   private void setAwarenessState(Mob mob, boolean aware) {
      EliteEntity elite = this.resolveAiOwner(mob);
      if (elite == null) {
         mob.setAware(aware);
      } else {
         elite.setAware(aware);
      }
   }

   private EliteEntity resolveAiOwner(LivingEntity body) {
      return this.eliteEntity.getLivingEntity() != body && this.eliteEntity.getUnsyncedLivingEntity() != body
         ? EntityTracker.getEliteMobEntity(body)
         : this.eliteEntity;
   }

   private float resolveVolume(Varargs args) {
      return args.narg() >= 2 && args.arg(2).istable() ? (float)args.arg(2).checktable().get("volume").optdouble(1.0) : this.support.getFloat(args, 2, 1.0F);
   }

   private float resolvePitch(Varargs args) {
      return args.narg() >= 2 && args.arg(2).istable() ? (float)args.arg(2).checktable().get("pitch").optdouble(1.0) : this.support.getFloat(args, 3, 1.0F);
   }

   void scheduleTagRemoval(LivingEntity livingEntity, int duration, String tag) {
      if (livingEntity != null && duration > 0) {
         GameClock.scheduleLater(duration, () -> {
            if (livingEntity.isValid()) {
               this.support.applyTag(livingEntity, tag, false);
            }
         });
      }
   }

   void addEntityEffectMethods(LuaTable entityTable, LivingEntity livingEntity) {
      entityTable.set("apply_potion_effect", this.method(entityTable, args -> {
         this.support.applyPotionEffect(livingEntity, args.checkjstring(1), args.checkint(2), args.optint(3, 0));
         return LuaValue.NIL;
      }));
      entityTable.set("set_equipment", this.method(entityTable, args -> {
         if (livingEntity != null && livingEntity.getEquipment() != null) {
            EquipmentSlot slot = this.support.parseEnum(args.checkjstring(1), EquipmentSlot.class, null);
            Material material = this.support.parseMaterial(args.checkjstring(2));
            if (slot != null && material != null) {
               ItemStack itemStack = new ItemStack(material);
               LuaTable options = args.narg() >= 3 && args.arg(3).istable() ? args.arg(3).checktable() : new LuaTable();
               if (options.get("enchantments").istable()) {
                  LuaTable enchantments = options.get("enchantments").checktable();
                  LuaValue key = LuaValue.NIL;

                  while (true) {
                     Varargs next = enchantments.next(key);
                     key = next.arg1();
                     if (key.isnil()) {
                        break;
                     }

                     if (next.arg(2).istable()) {
                        LuaTable enchantmentSpec = next.arg(2).checktable();
                        Enchantment enchantment = Enchantment.getByName(enchantmentSpec.get("type").optjstring("").toUpperCase(Locale.ROOT));
                        if (enchantment != null) {
                           itemStack.addUnsafeEnchantment(enchantment, enchantmentSpec.get("level").optint(1));
                        }
                     }
                  }
               }

               ItemMeta itemMeta = itemStack.getItemMeta();
               if (itemMeta != null) {
                  itemMeta.setUnbreakable(options.get("unbreakable").optboolean(false));
                  itemStack.setItemMeta(itemMeta);
               }

               switch (slot) {
                  case HEAD:
                     livingEntity.getEquipment().setHelmet(itemStack);
                     break;
                  case CHEST:
                     livingEntity.getEquipment().setChestplate(itemStack);
                     break;
                  case LEGS:
                     livingEntity.getEquipment().setLeggings(itemStack);
                     break;
                  case FEET:
                     livingEntity.getEquipment().setBoots(itemStack);
                     break;
                  case HAND:
                     livingEntity.getEquipment().setItemInMainHand(itemStack);
                     break;
                  case OFF_HAND:
                     livingEntity.getEquipment().setItemInOffHand(itemStack);
               }

               return LuaValue.NIL;
            } else {
               return LuaValue.NIL;
            }
         } else {
            return LuaValue.NIL;
         }
      }));
      entityTable.set("damage_equipment", this.method(entityTable, args -> {
         EquipmentSlot slot;
         try {
            slot = EquipmentDamagePolicy.parseSlot(args.checkjstring(1));
         } catch (IllegalArgumentException var6) {
            return LuaValue.argerror(1, var6.getMessage());
         }

         int amount = args.checkint(2);

         try {
            EquipmentDamagePolicy.requireRequestedDamage(amount);
         } catch (IllegalArgumentException var5) {
            return LuaValue.argerror(2, var5.getMessage());
         }

         return LuaValue.valueOf(EquipmentDamageRuntime.damage(livingEntity, slot, amount));
      }));
      entityTable.set("set_fire_ticks", this.method(entityTable, args -> {
         if (InstanceEffectPolicy.canAffect(this.eliteEntity, livingEntity)) {
            livingEntity.setFireTicks(args.checkint(1));
         }

         return LuaValue.NIL;
      }));
      entityTable.set("add_visual_freeze_ticks", this.method(entityTable, args -> {
         if (InstanceEffectPolicy.canAffect(this.eliteEntity, livingEntity)) {
            livingEntity.setFreezeTicks(livingEntity.getFreezeTicks() + args.optint(1, 1));
         }

         return LuaValue.NIL;
      }));
      entityTable.set(
         "place_temporary_block",
         this.method(
            entityTable,
            args -> {
               this.support
                  .placeTemporaryBlock(
                     livingEntity == null ? null : livingEntity.getLocation(), args.checkjstring(1), args.optint(2, 0), args.optboolean(3, false)
                  );
               return LuaValue.NIL;
            }
         )
      );
   }

   private VarArgFunction method(final LuaTable owner, final LuaPowerEntityTables.LuaCallback callback) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            return callback.invoke(LuaPowerEntityTables.this.stripMethodSelf(args, owner));
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
