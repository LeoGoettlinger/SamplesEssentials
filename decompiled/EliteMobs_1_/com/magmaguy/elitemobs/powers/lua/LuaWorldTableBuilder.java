package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaWorldTable;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powers.ProjectileDamage;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.scripts.ScriptListener;
import com.magmaguy.elitemobs.powers.specialpowers.EnderDragonEmpoweredLightningSupport;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

final class LuaWorldTableBuilder {
   private final ScriptDefinition definition;
   private final EliteEntity eliteEntity;
   private final LuaPowerSupport support;
   private final LuaPowerEntityTables entityTables;
   private final LuaPowerScriptApi.OwnedTaskController taskController;
   private final LuaPowerScriptApi.CallbackInvoker callbackInvoker;

   LuaWorldTableBuilder(
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
      taskController.ownCleanup(() -> ScriptListener.removeLuaFallingBlocks(this));
   }

   LuaTable build() {
      World contextWorld = this.eliteEntity.getLocation() != null ? this.eliteEntity.getLocation().getWorld() : null;
      LuaTable world = LuaWorldTable.build(contextWorld);
      world.set(
         "play_sound_at_location",
         this.method(
            world,
            args -> {
               this.support
                  .playSound(
                     this.support.toLocation(args.arg1()), args.checkjstring(2), this.support.getFloat(args, 3, 1.0F), this.support.getFloat(args, 4, 1.0F)
                  );
               return LuaValue.NIL;
            }
         )
      );
      world.set("spawn_particle_at_location", this.method(world, args -> {
         this.support.spawnParticle(this.support.toLocation(args.arg1()), args.arg(2), args.optint(3, 1));
         return LuaValue.NIL;
      }));
      world.set("set_block_at_location", this.method(world, args -> {
         this.support.placeTemporaryBlock(this.support.toLocation(args.arg1()), args.checkjstring(2), 0, args.optboolean(3, false));
         return LuaValue.NIL;
      }));
      world.set("place_temporary_block_at_location", this.method(world, args -> {
         this.support.placeTemporaryBlock(this.support.toLocation(args.arg1()), args.checkjstring(2), args.optint(3, 0), args.optboolean(4, false));
         return LuaValue.NIL;
      }));
      world.set("get_block_type_at_location", this.method(world, args -> {
         Location location = this.support.toLocation(args.arg1());
         return (Varargs)(location == null ? LuaValue.NIL : LuaValue.valueOf(location.getBlock().getType().name()));
      }));
      world.set(
         "get_highest_block_y_at_location",
         this.method(
            world,
            args -> {
               Location location = this.support.toLocation(args.arg1());
               return (Varargs)(location != null && location.getWorld() != null
                  ? LuaValue.valueOf(location.getWorld().getHighestBlockAt(location).getY())
                  : LuaValue.NIL);
            }
         )
      );
      world.set(
         "get_blast_resistance_at_location",
         this.method(world, args -> LuaValue.valueOf((double)this.support.getBlastResistance(this.support.toLocation(args.arg1()))))
      );
      world.set("is_air_at_location", this.method(world, args -> {
         Location location = this.support.toLocation(args.arg1());
         return LuaValue.valueOf(location != null && location.getBlock().getType().isAir());
      }));
      world.set("is_passable_at_location", this.method(world, args -> {
         Location location = this.support.toLocation(args.arg1());
         return LuaValue.valueOf(location != null && location.getBlock().isPassable());
      }));
      world.set("is_passthrough_at_location", this.method(world, args -> {
         Location location = this.support.toLocation(args.arg1());
         return LuaValue.valueOf(location != null && !location.getBlock().getType().isSolid());
      }));
      world.set(
         "is_on_floor_at_location",
         this.method(
            world,
            args -> {
               Location location = this.support.toLocation(args.arg1());
               return location == null
                  ? LuaValue.FALSE
                  : LuaValue.valueOf(!location.getBlock().getType().isSolid() && location.clone().subtract(0.0, 1.0, 0.0).getBlock().getType().isSolid());
            }
         )
      );
      world.set(
         "is_standing_on_material",
         this.method(
            world,
            args -> {
               Location location = this.support.toLocation(args.arg1());
               Material material = this.support.parseMaterial(args.checkjstring(2));
               return location != null && material != null
                  ? LuaValue.valueOf(location.clone().subtract(0.0, 1.0, 0.0).getBlock().getType() == material)
                  : LuaValue.FALSE;
            }
         )
      );
      world.set("strike_lightning_at_location", this.method(world, args -> {
         Location location = this.support.toLocation(args.arg1());
         if (location != null && location.getWorld() != null) {
            LightningSpawnBypass.bypass();
            location.getWorld().strikeLightning(location);
         }

         return LuaValue.NIL;
      }));
      world.set("run_empowered_lightning_task_at_location", this.method(world, args -> {
         EnderDragonEmpoweredLightningSupport.lightningTask(this.support.toLocation(args.arg1()));
         return LuaValue.NIL;
      }));
      world.set("set_world_time", this.method(world, args -> {
         Location location = args.narg() >= 2 ? this.support.toLocation(args.arg1()) : this.eliteEntity.getLocation();
         long time = args.narg() >= 2 ? args.checklong(2) : args.checklong(1);
         if (location != null && location.getWorld() != null) {
            location.getWorld().setTime(time);
         }

         return LuaValue.NIL;
      }));
      world.set("set_world_weather", this.method(world, args -> {
         Location location = this.eliteEntity.getLocation();
         int weatherIndex = 1;
         if (args.narg() >= 2 && args.arg1().istable()) {
            location = this.support.toLocation(args.arg1());
            weatherIndex = 2;
         }

         if (location != null && location.getWorld() != null) {
            String weather = args.checkjstring(weatherIndex).toUpperCase(Locale.ROOT);
            int duration = args.narg() > weatherIndex ? args.optint(weatherIndex + 1, 6000) : 6000;
            World worldRef = location.getWorld();
            switch (weather) {
               case "CLEAR":
                  worldRef.setStorm(false);
                  worldRef.setThundering(false);
                  worldRef.setWeatherDuration(duration);
                  break;
               case "PRECIPITATION":
               case "RAIN":
                  worldRef.setStorm(true);
                  worldRef.setThundering(false);
                  worldRef.setWeatherDuration(duration);
                  break;
               case "THUNDER":
                  worldRef.setStorm(true);
                  worldRef.setThundering(true);
                  worldRef.setThunderDuration(duration);
                  break;
               default:
                  Logger.warn("Unknown weather type " + weather + " in Lua power " + this.definition.getFileName() + ".");
            }

            return LuaValue.NIL;
         } else {
            return LuaValue.NIL;
         }
      }));
      world.set("run_console_command", this.method(world, args -> {
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args.checkjstring(1));
         return LuaValue.NIL;
      }));
      world.set("spawn_boss_at_location", this.method(world, args -> {
         CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(args.checkjstring(1));
         if (customBossEntity == null) {
            return LuaValue.NIL;
         } else {
            Location location = args.narg() >= 2 ? this.support.toLocation(args.arg(2)) : this.eliteEntity.getLocation();
            int level = args.narg() >= 3 ? args.checkint(3) : this.eliteEntity.getLevel();
            if (location != null) {
               customBossEntity.spawn(location, level, false);
               if (customBossEntity.getLivingEntity() != null) {
                  return this.entityTables.createEntityTable(customBossEntity.getLivingEntity());
               }
            }

            return LuaValue.NIL;
         }
      }));
      world.set("spawn_custom_boss_at_location", this.method(world, args -> {
         CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(args.checkjstring(1));
         if (customBossEntity == null) {
            return LuaValue.NIL;
         } else {
            Location location = this.support.toLocation(args.arg(2));
            if (location == null) {
               return LuaValue.NIL;
            } else {
               LuaTable options = args.narg() >= 3 && args.arg(3).istable() ? args.arg(3).checktable() : new LuaTable();
               boolean silent = options.get("silent").optboolean(false);
               if (options.get("level").isnumber()) {
                  customBossEntity.spawn(location, options.get("level").toint(), silent);
               } else {
                  customBossEntity.spawn(location, silent);
               }

               if (customBossEntity.getLivingEntity() == null) {
                  return LuaValue.NIL;
               } else {
                  if (options.get("add_as_reinforcement").optboolean(false)) {
                     this.eliteEntity.addReinforcement(customBossEntity);
                  }

                  Vector velocity = this.support.toVector(options.get("velocity"));
                  if (velocity != null) {
                     customBossEntity.getLivingEntity().setVelocity(velocity);
                  }

                  return this.entityTables.createEntityTable(customBossEntity.getLivingEntity());
               }
            }
         }
      }));
      world.set("spawn_entity_at_location", this.method(world, args -> {
         Location location = this.support.toLocation(args.arg(2));
         if (location != null && location.getWorld() != null) {
            EntityType entityType;
            try {
               entityType = EntityType.valueOf(args.checkjstring(1).toUpperCase(Locale.ROOT));
            } catch (Exception var11) {
               Logger.warn("Unknown entity type " + args.arg(1) + " in Lua power " + this.definition.getFileName() + ".");
               return LuaValue.NIL;
            }

            LuaTable options = args.narg() >= 3 && args.arg(3).istable() ? args.arg(3).checktable() : new LuaTable();
            Entity entity = location.getWorld().spawnEntity(location, entityType);
            Vector velocity = this.support.toVector(options.get("velocity"));
            if (velocity != null) {
               entity.setVelocity(velocity);
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
               } catch (IllegalArgumentException var10) {
               }
            }

            if (options.get("on_land").isfunction()) {
               this.monitorEntityLanding(entity, options.get("on_land").checkfunction(), options.get("max_ticks").optint(6000));
            }

            return this.entityTables.createEntityReferenceTable(entity);
         } else {
            return LuaValue.NIL;
         }
      }));
      world.set(
         "spawn_falling_block_at_location",
         this.method(
            world,
            args -> {
               Location location = this.support.toLocation(args.arg1());
               Material material = this.support.parseMaterial(args.checkjstring(2));
               if (location != null && location.getWorld() != null && material != null) {
                  LuaTable options = args.narg() >= 3 && args.arg(3).istable() ? args.arg(3).checktable() : new LuaTable();
                  FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location, material.createBlockData());
                  fallingBlock.setDropItem(options.get("drop_item").optboolean(false));
                  fallingBlock.setHurtEntities(options.get("hurt_entities").optboolean(false));
                  Vector velocity = this.support.toVector(options.get("velocity"));
                  if (velocity != null) {
                     fallingBlock.setVelocity(velocity);
                  }

                  if (options.get("on_land").isfunction()) {
                     LuaFunction onLand = options.get("on_land").checkfunction();
                     ScriptListener.registerLuaFallingBlock(
                        this,
                        fallingBlock,
                        (entity, landingLocation) -> this.callbackInvoker
                           .invoke(
                              "a falling block landing callback",
                              onLand,
                              this.support.toLocationTable(landingLocation),
                              this.entityTables.createEntityReferenceTable(entity)
                           )
                     );
                  }

                  return this.entityTables.createEntityReferenceTable(fallingBlock);
               } else {
                  return LuaValue.NIL;
               }
            }
         )
      );
      world.set("spawn_reinforcement_at_location", this.method(world, args -> {
         Location location = this.support.toLocation(args.arg(2));
         if (location == null) {
            return LuaValue.NIL;
         } else {
            CustomBossEntity reinforcement = CustomSummonPower.summonReinforcement(this.eliteEntity, location, args.checkjstring(1), args.optint(3, 0));
            if (reinforcement != null && reinforcement.getLivingEntity() != null) {
               if (args.narg() >= 4) {
                  Vector velocity = this.support.toVector(args.arg(4));
                  if (velocity != null) {
                     reinforcement.getLivingEntity().setVelocity(velocity);
                  }
               }

               return this.entityTables.createEntityTable(reinforcement.getLivingEntity());
            } else {
               return LuaValue.NIL;
            }
         }
      }));
      world.set("spawn_fireworks_at_location", this.method(world, args -> {
         Entity firework = this.support.spawnFirework(this.support.toLocation(args.arg1()), args.arg(2).checktable());
         return firework == null ? LuaValue.NIL : this.entityTables.createEntityReferenceTable(firework);
      }));
      world.set("spawn_splash_potion_at_location", this.method(world, args -> {
         Location location = this.support.toLocation(args.arg1());
         if (location != null && args.arg(2).istable()) {
            Entity thrownPotion = this.support.spawnSplashPotion(location, args.arg(2).checktable());
            return thrownPotion == null ? LuaValue.NIL : this.entityTables.createEntityReferenceTable(thrownPotion);
         } else {
            return LuaValue.NIL;
         }
      }));
      world.set("generate_fake_explosion", this.method(world, args -> {
         List<Location> blockLocations = new ArrayList<>();
         if (args.arg1().istable()) {
            LuaTable locations = args.arg1().checktable();
            LuaValue key = LuaValue.NIL;

            while (true) {
               Varargs next = locations.next(key);
               key = next.arg1();
               if (key.isnil()) {
                  break;
               }

               Location blockLocation = this.support.toLocation(next.arg(2));
               if (blockLocation != null) {
                  blockLocations.add(blockLocation);
               }
            }
         }

         this.support.generateFakeExplosion(blockLocations, args.narg() >= 2 ? this.support.toLocation(args.arg(2)) : null);
         return LuaValue.NIL;
      }));
      ProjectileDamage.GoldNuggetBurst goldBurst = new ProjectileDamage.GoldNuggetBurst();
      world.set("spawn_fake_gold_nugget_at_location", this.method(world, args -> {
         ProjectileDamage.FakeProjectile projectile = goldBurst.create(this.support.toLocation(args.arg1()), this.support.toVector(args.arg(2)));
         if (projectile == null) {
            return LuaValue.NIL;
         } else {
            if (args.narg() >= 3 && args.arg(3).optboolean(false)) {
               projectile.setGravity(true);
            }

            return this.createFakeProjectileTable(projectile);
         }
      }));
      world.set("run_fake_gold_nugget_damage", this.method(world, args -> {
         if (!args.arg1().istable()) {
            return LuaValue.NIL;
         } else {
            List<ProjectileDamage.FakeProjectile> projectiles = new ArrayList<>();
            LuaTable table = args.arg1().checktable();
            LuaValue key = LuaValue.NIL;

            while (true) {
               Varargs next = table.next(key);
               key = next.arg1();
               if (key.isnil()) {
                  this.support.runGoldNuggetDamage(projectiles);
                  return LuaValue.NIL;
               }

               ProjectileDamage.FakeProjectile projectile = this.extractFakeProjectile(next.arg(2));
               if (projectile != null) {
                  projectiles.add(projectile);
               }
            }
         }
      }));
      world.set("generate_player_loot", this.method(world, args -> {
         this.support.generatePlayerLoot(args.optint(1, 1));
         return LuaValue.NIL;
      }));
      world.set("drop_bonus_coins", this.method(world, args -> {
         this.support.dropBonusCoins(args.optdouble(1, 2.0));
         return LuaValue.NIL;
      }));
      return world;
   }

   LuaTable buildSettingsTable() {
      LuaTable settings = new LuaTable();
      settings.set("warning_visual_effects_enabled", this.method(settings, args -> LuaValue.valueOf(MobCombatSettingsConfig.isEnableWarningVisualEffects())));
      return settings;
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
                     LuaWorldTableBuilder.this.taskController.cancel(taskId[0]);
                     LuaWorldTableBuilder.this.callbackInvoker
                        .invoke(
                           "an entity landing callback",
                           callback,
                           LuaWorldTableBuilder.this.support.toLocationTable(entity.getLocation()),
                           LuaWorldTableBuilder.this.entityTables.createEntityReferenceTable(entity)
                        );
                  }
               }
            }
         );
   }

   private LuaTable createFakeProjectileTable(ProjectileDamage.FakeProjectile projectile) {
      LuaTable table = new LuaTable();
      table.set("__fake_projectile", LuaValue.userdataOf(projectile));
      table.set("get_location", this.method(table, args -> this.support.toLocationTable(projectile.getLocation())));
      table.set("set_gravity", this.method(table, args -> {
         projectile.setGravity(args.checkboolean(1));
         return LuaValue.NIL;
      }));
      table.set("remove", this.method(table, args -> {
         projectile.remove();
         return LuaValue.NIL;
      }));
      table.set("is_removed", this.method(table, args -> LuaValue.valueOf(projectile.isRemoved())));
      return table;
   }

   private ProjectileDamage.FakeProjectile extractFakeProjectile(LuaValue value) {
      if (!value.istable()) {
         return null;
      } else {
         LuaValue userdata = value.checktable().get("__fake_projectile");
         return userdata.isuserdata(ProjectileDamage.FakeProjectile.class)
            ? (ProjectileDamage.FakeProjectile)userdata.checkuserdata(ProjectileDamage.FakeProjectile.class)
            : null;
      }
   }

   private VarArgFunction method(final LuaTable owner, final LuaWorldTableBuilder.LuaCallback callback) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            return callback.invoke(LuaWorldTableBuilder.this.stripMethodSelf(args, owner));
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
