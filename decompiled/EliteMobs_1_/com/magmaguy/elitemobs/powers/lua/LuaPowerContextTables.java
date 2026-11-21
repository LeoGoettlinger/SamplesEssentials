package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Shape;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

final class LuaPowerContextTables {
   private final ScriptDefinition definition;
   private final EliteEntity eliteEntity;
   private final LuaPowerSupport support;
   private final LuaPowerEntityTables entityTables;
   private final LuaPowerScriptApi.OwnedTaskController taskController;
   private final LuaPowerScriptApi.CallbackInvoker callbackInvoker;
   private final LuaWorldTableBuilder worldTableBuilder;

   LuaPowerContextTables(
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
      this.worldTableBuilder = new LuaWorldTableBuilder(definition, eliteEntity, support, entityTables, taskController, callbackInvoker);
   }

   LuaTable createPlayersTable(LivingEntity eventActor) {
      LuaTable players = new LuaTable();
      players.set(
         "current_target",
         this.method(
            players,
            args -> {
               if (eventActor instanceof Player player) {
                  return this.entityTables.createPlayerTable(player);
               } else {
                  return (Varargs)(this.eliteEntity.getLivingEntity() instanceof Mob mob && mob.getTarget() instanceof Player player
                     ? this.entityTables.createPlayerTable(player)
                     : LuaValue.NIL);
               }
            }
         )
      );
      players.set("nearby_players", this.method(players, args -> {
         LuaTable results = new LuaTable();
         if (this.eliteEntity.getLocation() != null && this.eliteEntity.getLocation().getWorld() != null) {
            double radius = args.checkdouble(1);
            int index = 1;

            for (Player player : this.eliteEntity.getLocation().getWorld().getPlayers()) {
               if (player.getLocation().distanceSquared(this.eliteEntity.getLocation()) <= radius * radius) {
                  results.set(index++, this.entityTables.createPlayerTable(player));
               }
            }

            return results;
         } else {
            return results;
         }
      }));
      players.set("all_players_in_world", this.method(players, args -> {
         LuaTable results = new LuaTable();
         if (this.eliteEntity.getLocation() != null && this.eliteEntity.getLocation().getWorld() != null) {
            int index = 1;

            for (Player player : this.eliteEntity.getLocation().getWorld().getPlayers()) {
               results.set(index++, this.entityTables.createPlayerTable(player));
            }

            return results;
         } else {
            return results;
         }
      }));
      return players;
   }

   LuaTable createEntitiesTable(LivingEntity directTarget) {
      LuaTable entities = new LuaTable();
      entities.set(
         "get_nearby_entities",
         this.method(
            entities,
            args -> {
               double radius = args.checkdouble(1);
               String filter = args.narg() >= 2 && args.arg(2).isstring() ? args.arg(2).tojstring() : "living";
               LuaTable results = new LuaTable();
               if (this.eliteEntity.getLocation() != null && this.eliteEntity.getLocation().getWorld() != null) {
                  int index = 1;
                  if (!"all".equalsIgnoreCase(filter) && !"entities".equalsIgnoreCase(filter)) {
                     for (LivingEntity livingEntity : this.support
                        .filterEntities(this.eliteEntity.getLocation(), Math.abs(radius), Math.abs(radius), Math.abs(radius), filter)) {
                        if ((this.eliteEntity.getLivingEntity() == null || !livingEntity.getUniqueId().equals(this.eliteEntity.getLivingEntity().getUniqueId()))
                           && livingEntity.getLocation().distanceSquared(this.eliteEntity.getLocation()) <= radius * radius) {
                           results.set(index++, this.entityTables.createEntityTable(livingEntity));
                        }
                     }
                  } else {
                     for (Entity entity : this.eliteEntity.getLocation().getWorld().getNearbyEntities(this.eliteEntity.getLocation(), radius, radius, radius)) {
                        if (this.eliteEntity.getLivingEntity() == null || !entity.getUniqueId().equals(this.eliteEntity.getLivingEntity().getUniqueId())) {
                           results.set(index++, this.entityTables.createEntityReferenceTable(entity));
                        }
                     }
                  }

                  return results;
               } else {
                  return results;
               }
            }
         )
      );
      entities.set(
         "get_entities_in_box",
         this.method(
            entities,
            args -> {
               Location center = this.support.toLocation(args.arg1());
               double halfX = args.checkdouble(2);
               double halfY = args.checkdouble(3);
               double halfZ = args.checkdouble(4);
               String filter = args.narg() >= 5 && args.arg(5).isstring() ? args.arg(5).tojstring() : "living";
               LuaTable results = new LuaTable();
               if (center != null && center.getWorld() != null) {
                  int index = 1;
                  double minX = center.getX() - halfX;
                  double maxX = center.getX() + halfX;
                  double minY = center.getY() - halfY;
                  double maxY = center.getY() + halfY;
                  double minZ = center.getZ() - halfZ;
                  double maxZ = center.getZ() + halfZ;

                  for (LivingEntity livingEntity : this.support.filterEntities(center, halfX, halfY, halfZ, filter)) {
                     if (this.eliteEntity.getLivingEntity() == null || !livingEntity.getUniqueId().equals(this.eliteEntity.getLivingEntity().getUniqueId())) {
                        Location entityLocation = livingEntity.getLocation();
                        if (entityLocation.getX() >= minX
                           && entityLocation.getX() <= maxX
                           && entityLocation.getY() >= minY
                           && entityLocation.getY() <= maxY
                           && entityLocation.getZ() >= minZ
                           && entityLocation.getZ() <= maxZ) {
                           results.set(index++, this.entityTables.createEntityTable(livingEntity));
                        }
                     }
                  }

                  return results;
               } else {
                  return results;
               }
            }
         )
      );
      entities.set("get_all_entities", this.method(entities, args -> {
         String filter = args.narg() >= 1 && args.arg(1).isstring() ? args.arg(1).tojstring() : "living";
         LuaTable results = new LuaTable();
         if (this.eliteEntity.getLocation() != null && this.eliteEntity.getLocation().getWorld() != null) {
            int index = 1;

            for (LivingEntity livingEntity : this.support.filterEntities(this.eliteEntity.getLocation().getWorld(), filter)) {
               if (this.eliteEntity.getLivingEntity() == null || !livingEntity.getUniqueId().equals(this.eliteEntity.getLivingEntity().getUniqueId())) {
                  results.set(index++, this.entityTables.createEntityTable(livingEntity));
               }
            }

            return results;
         } else {
            return results;
         }
      }));
      entities.set(
         "get_direct_target_entity",
         this.method(entities, args -> (Varargs)(directTarget == null ? LuaValue.NIL : this.entityTables.createEntityTable(directTarget)))
      );
      entities.set("get_boss_spawn_location", this.method(entities, args -> this.support.toLocationTable(this.eliteEntity.getSpawnLocation())));
      return entities;
   }

   LuaTable createVectorsTable() {
      LuaTable vectors = new LuaTable();
      vectors.set(
         "get_vector_between_locations",
         this.method(
            vectors,
            args -> {
               Location source = this.support.toLocation(args.arg1());
               Location destination = this.support.toLocation(args.arg(2));
               if (source != null
                  && destination != null
                  && source.getWorld() != null
                  && destination.getWorld() != null
                  && source.getWorld().getUID().equals(destination.getWorld().getUID())) {
                  Vector vector = destination.toVector().subtract(source.toVector());
                  if (args.narg() >= 3 && args.arg(3).istable()) {
                     this.support.applyVectorOptions(vector, args.arg(3).checktable());
                  }

                  return this.support.toVectorTable(vector);
               } else {
                  return this.support.toVectorTable(new Vector(0, 0, 0));
               }
            }
         )
      );
      vectors.set("rotate_vector", this.method(vectors, args -> {
         Vector vector = this.support.toVector(args.arg1());
         if (vector == null) {
            return this.support.toVectorTable(new Vector(0, 0, 0));
         } else {
            double pitch = args.narg() >= 2 ? args.arg(2).optdouble(0.0) : 0.0;
            double yaw = args.narg() >= 3 ? args.arg(3).optdouble(0.0) : 0.0;
            return this.support.toVectorTable(this.support.rotateVector(vector, pitch, yaw));
         }
      }));
      vectors.set("normalize_vector", this.method(vectors, args -> {
         Vector vector = this.support.toVector(args.arg1());
         if (vector == null) {
            return this.support.toVectorTable(new Vector(0, 0, 0));
         } else {
            if (vector.lengthSquared() > 0.0) {
               vector.normalize();
            }

            return this.support.toVectorTable(vector);
         }
      }));
      return vectors;
   }

   LuaTable createWorldTable() {
      return this.worldTableBuilder.build();
   }

   LuaTable createSettingsTable() {
      return this.worldTableBuilder.buildSettingsTable();
   }

   LuaTable queryZoneEntities(LuaValue zoneDefinition, LuaValue optionsValue) {
      LuaTable results = new LuaTable();
      Shape shape = this.support.createShape(zoneDefinition);
      if (shape != null && this.eliteEntity.getLocation() != null && this.eliteEntity.getLocation().getWorld() != null) {
         String filter = this.support.resolveZoneFilter(optionsValue);
         boolean borderMode = this.support.resolveZoneMode(optionsValue);
         int index = 1;

         for (LivingEntity livingEntity : this.support.filterEntities(this.eliteEntity.getLocation().getWorld(), shape, filter)) {
            if (this.eliteEntity.getLivingEntity() == null || !livingEntity.getUniqueId().equals(this.eliteEntity.getLivingEntity().getUniqueId())) {
               boolean contains = borderMode ? shape.borderContains(livingEntity) : shape.contains(livingEntity);
               if (contains) {
                  results.set(index++, this.entityTables.createEntityTable(livingEntity));
               }
            }
         }

         return results;
      } else {
         return results;
      }
   }

   LuaTable queryZoneLocations(LuaValue zoneDefinition, LuaValue optionsValue) {
      LuaTable results = new LuaTable();
      Shape shape = this.support.createShape(zoneDefinition);
      if (shape == null) {
         return results;
      } else {
         boolean borderMode = this.support.resolveZoneMode(optionsValue);
         double coverage = 1.0;
         if (optionsValue != null && optionsValue.istable()) {
            coverage = optionsValue.checktable().get("coverage").optdouble(1.0);
         }

         Collection<Location> locations = borderMode ? shape.getEdgeLocations() : shape.getLocations();
         int index = 1;

         for (Location location : locations) {
            if (!(coverage < 1.0) || !(Math.random() > coverage)) {
               results.set(index++, this.support.toLocationTable(location));
            }
         }

         return results;
      }
   }

   private VarArgFunction method(final LuaTable owner, final LuaPowerContextTables.LuaCallback callback) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            return callback.invoke(LuaPowerContextTables.this.stripMethodSelf(args, owner));
         }
      };
   }

   private Varargs stripMethodSelf(Varargs args, LuaTable owner) {
      if (args.narg() != 0 && args.arg1().raweq(owner)) {
         LuaValue[] strippedArgs = new LuaValue[Math.max(0, args.narg() - 1)];

         for (int index = 2; index <= args.narg(); index++) {
            strippedArgs[index - 2] = args.arg(index);
         }

         return LuaValue.varargsOf(strippedArgs);
      } else {
         return args;
      }
   }

   @FunctionalInterface
   private interface LuaCallback {
      Varargs invoke(Varargs var1);
   }
}
