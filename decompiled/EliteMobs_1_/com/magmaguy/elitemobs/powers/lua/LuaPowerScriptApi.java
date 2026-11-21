package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaTableConverter;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Shape;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.scripts.ScriptActionData;
import com.magmaguy.elitemobs.powers.scripts.ScriptParticles;
import com.magmaguy.elitemobs.powers.scripts.ScriptRelativeVector;
import com.magmaguy.elitemobs.powers.scripts.ScriptTargets;
import com.magmaguy.elitemobs.powers.scripts.ScriptZone;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptParticlesBlueprint;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptRelativeVectorBlueprint;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptTargetsBlueprint;
import com.magmaguy.elitemobs.powers.scripts.caching.ScriptZoneBlueprint;
import com.magmaguy.elitemobs.powers.scripts.enums.TargetType;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

final class LuaPowerScriptApi {
   private static final String HANDLE_KEY = "__lua_script_handle";
   private final ScriptDefinition definition;
   private final EliteEntity eliteEntity;
   private final LuaPowerSupport support;
   private final LuaPowerEntityTables entityTables;
   private final LuaPowerScriptApi.OwnedTaskController taskController;
   private final LuaPowerScriptApi.CallbackInvoker callbackInvoker;

   LuaPowerScriptApi(
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
   }

   LuaTable createTable(Event event, LivingEntity directTarget) {
      LuaTable scripts = new LuaTable();
      scripts.set("target", this.method(scripts, args -> this.createTargetHandle(args.checktable(1), event, directTarget, null)));
      scripts.set("zone", this.method(scripts, args -> this.createZoneHandle(args.checktable(1), event, directTarget)));
      scripts.set("relative_vector", this.method(scripts, args -> {
         LuaPowerScriptApi.LuaZoneHandle zoneHandle = args.narg() >= 3 ? this.extractHandle(args.arg(3), LuaPowerScriptApi.LuaZoneHandle.class) : null;
         LuaValue actionLocation = args.narg() >= 2 ? args.arg(2) : LuaValue.NIL;
         return this.createRelativeVectorHandle(args.checktable(1), actionLocation, zoneHandle, event, directTarget);
      }));
      scripts.set("damage", this.method(scripts, args -> {
         double amount = args.narg() >= 2 ? args.arg(2).optdouble(0.0) : 0.0;
         double multiplier = args.narg() >= 3 ? args.arg(3).optdouble(1.0) : 1.0;
         this.damageTargets(this.resolveTargets(args.arg1()), amount, multiplier);
         return LuaValue.NIL;
      }));
      scripts.set("push", this.method(scripts, args -> {
         boolean additive = args.narg() >= 3 && args.arg(3).optboolean(false);
         this.pushTargets(this.resolveTargets(args.arg1()), this.resolveVector(args.arg(2)), additive);
         return LuaValue.NIL;
      }));
      scripts.set("set_facing", this.method(scripts, args -> {
         this.setFacing(this.resolveTargets(args.arg1()), this.resolveVector(args.arg(2)));
         return LuaValue.NIL;
      }));
      scripts.set("spawn_particles", this.method(scripts, args -> {
         this.spawnParticles(args.arg1(), args.arg(2));
         return LuaValue.NIL;
      }));
      return scripts;
   }

   private <T> T extractHandle(LuaValue value, Class<T> type) {
      if (value != null && !value.isnil() && value.istable()) {
         LuaValue userdata = value.checktable().rawget("__lua_script_handle");
         if (userdata.isnil()) {
            return null;
         } else {
            try {
               return type.cast(userdata.checkuserdata(type));
            } catch (Exception var5) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private LuaTable createTargetHandle(LuaTable specTable, Event event, LivingEntity directTarget, LuaPowerScriptApi.LuaZoneHandle zoneHandle) {
      LuaPowerScriptApi.LuaTargetHandle handle = new LuaPowerScriptApi.LuaTargetHandle(this.buildTargetResolver(specTable, event, directTarget, zoneHandle));
      LuaTable table = new LuaTable();
      table.rawset("__lua_script_handle", LuaValue.userdataOf(handle));
      table.set("entities", this.method(table, args -> this.toEntityArray(handle.resolver.resolveEntities())));
      table.set("locations", this.method(table, args -> this.toLocationArray(handle.resolver.resolveLocations())));
      table.set("first_entity", this.method(table, args -> {
         Collection<LivingEntity> entities = handle.resolver.resolveEntities();
         return (Varargs)(entities.isEmpty() ? LuaValue.NIL : this.entityTables.createEntityTable(entities.iterator().next()));
      }));
      table.set("first_location", this.method(table, args -> {
         Collection<Location> locations = handle.resolver.resolveLocations();
         return (Varargs)(locations.isEmpty() ? LuaValue.NIL : this.support.toLocationTable(locations.iterator().next()));
      }));
      return table;
   }

   private LuaTable createZoneHandle(LuaTable specTable, Event event, LivingEntity directTarget) {
      LuaPowerScriptApi.LuaZoneHandle handle = this.buildZoneHandle(specTable, event, directTarget);
      LuaTable table = new LuaTable();
      table.rawset("__lua_script_handle", LuaValue.userdataOf(handle));
      table.set("full_target", this.method(table, args -> {
         LuaValue coverage = args.narg() >= 1 ? args.arg1() : LuaValue.NIL;
         return this.createTargetHandle(handle.targetSpec("ZONE_FULL", coverage), event, directTarget, handle);
      }));
      table.set("border_target", this.method(table, args -> {
         LuaValue coverage = args.narg() >= 1 ? args.arg1() : LuaValue.NIL;
         return this.createTargetHandle(handle.targetSpec("ZONE_BORDER", coverage), event, directTarget, handle);
      }));
      table.set("full_locations", this.method(table, args -> {
         LuaValue coverage = args.narg() >= 1 ? args.arg1() : LuaValue.NIL;
         return this.toLocationArray(handle.resolveLocations("ZONE_FULL", coverage));
      }));
      table.set("border_locations", this.method(table, args -> {
         LuaValue coverage = args.narg() >= 1 ? args.arg1() : LuaValue.NIL;
         return this.toLocationArray(handle.resolveLocations("ZONE_BORDER", coverage));
      }));
      table.set("full_entities", this.method(table, args -> this.toEntityArray(handle.resolveEntities("ZONE_FULL"))));
      table.set("border_entities", this.method(table, args -> this.toEntityArray(handle.resolveEntities("ZONE_BORDER"))));
      table.set("contains", this.method(table, args -> {
         String mode = args.narg() >= 2 ? args.arg(2).optjstring("full") : "full";
         return LuaValue.valueOf(handle.contains(args.arg1(), mode));
      }));
      table.set("watch", this.method(table, args -> {
         LuaTable callbacks = args.checktable(1);
         String mode = args.narg() >= 2 ? args.arg(2).optjstring("full") : "full";
         return LuaValue.valueOf(this.watchZone(handle, callbacks, mode));
      }));
      return table;
   }

   private LuaTable createRelativeVectorHandle(
      LuaTable specTable, LuaValue actionLocationValue, LuaPowerScriptApi.LuaZoneHandle zoneHandle, Event event, LivingEntity directTarget
   ) {
      LuaPowerScriptApi.LuaRelativeVectorHandle handle = this.buildRelativeVectorHandle(specTable, actionLocationValue, zoneHandle, event, directTarget);
      LuaTable table = new LuaTable();
      table.rawset("__lua_script_handle", LuaValue.userdataOf(handle));
      table.set("resolve", this.method(table, args -> this.support.toVectorTable(handle.resolve())));
      return table;
   }

   private int watchZone(LuaPowerScriptApi.LuaZoneHandle handle, LuaTable callbacks, String mode) {
      LuaValue onEnter = callbacks.get("on_enter");
      LuaValue onLeave = callbacks.get("on_leave");
      Map<UUID, LivingEntity> inside = new HashMap<>();
      int[] taskId = new int[1];
      taskId[0] = this.taskController.runRepeating(1, 1, () -> {
         if (this.eliteEntity.exists() && this.eliteEntity.getLivingEntity() != null && this.eliteEntity.getLivingEntity().isValid()) {
            Map<UUID, LivingEntity> current = new LinkedHashMap<>();

            for (LivingEntity livingEntity : "border".equalsIgnoreCase(mode) ? handle.resolveEntities("ZONE_BORDER") : handle.resolveEntities("ZONE_FULL")) {
               if (livingEntity != null && livingEntity.isValid()) {
                  current.put(livingEntity.getUniqueId(), livingEntity);
                  if (!inside.containsKey(livingEntity.getUniqueId()) && onEnter.isfunction()) {
                     this.callbackInvoker.invoke("a zone enter callback", onEnter.checkfunction(), this.entityTables.createEntityTable(livingEntity));
                  }
               }
            }

            for (Entry<UUID, LivingEntity> entry : inside.entrySet()) {
               if (!current.containsKey(entry.getKey()) && entry.getValue() != null && onLeave.isfunction()) {
                  this.callbackInvoker.invoke("a zone leave callback", onLeave.checkfunction(), this.entityTables.createEntityTable(entry.getValue()));
               }
            }

            inside.clear();
            inside.putAll(current);
         } else {
            this.taskController.cancel(taskId[0]);
         }
      });
      return taskId[0];
   }

   private LuaPowerScriptApi.LuaTargetResolver buildTargetResolver(
      LuaTable specTable, Event event, LivingEntity directTarget, LuaPowerScriptApi.LuaZoneHandle zoneHandle
   ) {
      Map<String, Object> spec = (Map<String, Object>)LuaTableConverter.toJavaObject(specTable);
      LuaScriptRuntimeOwner runtimeOwner = zoneHandle == null ? new LuaScriptRuntimeOwner(this.definition.getFileName()) : zoneHandle.runtimeOwner;
      ScriptTargets targets = new ScriptTargets(new ScriptTargetsBlueprint(spec, "__lua_target__", this.definition.getFileName()), runtimeOwner);
      return new LuaPowerScriptApi.LuaTargetResolver(
         targets, () -> zoneHandle == null ? new ScriptActionData(this.eliteEntity, directTarget, targets, event) : zoneHandle.actionData
      );
   }

   private LuaPowerScriptApi.LuaZoneHandle buildZoneHandle(LuaTable specTable, Event event, LivingEntity directTarget) {
      Map<String, Object> zoneSpec = (Map<String, Object>)LuaTableConverter.toJavaObject(specTable);
      LuaScriptRuntimeOwner runtimeOwner = new LuaScriptRuntimeOwner(this.definition.getFileName());
      ScriptZone scriptZone = new ScriptZone(new ScriptZoneBlueprint(Map.of("Zone", zoneSpec), "__lua_zone__", this.definition.getFileName()), runtimeOwner);
      runtimeOwner.setScriptZone(scriptZone);
      ScriptTargets selfTarget = new ScriptTargets(
         new ScriptTargetsBlueprint(Collections.emptyMap(), "__lua_zone__", this.definition.getFileName()), runtimeOwner
      );
      ScriptActionData actionData = new ScriptActionData(this.eliteEntity, directTarget, selfTarget, scriptZone, event);
      if (scriptZone.getZoneBlueprint().getAnimationDuration().getValue() > 0) {
         actionData.setShapesCachedByTarget(scriptZone.generateShapes(actionData, true));
      }

      return new LuaPowerScriptApi.LuaZoneHandle(runtimeOwner, scriptZone, actionData);
   }

   private LuaPowerScriptApi.LuaRelativeVectorHandle buildRelativeVectorHandle(
      LuaTable specTable, LuaValue actionLocationValue, LuaPowerScriptApi.LuaZoneHandle zoneHandle, Event event, LivingEntity directTarget
   ) {
      Map<String, Object> spec = (Map<String, Object>)LuaTableConverter.toJavaObject(specTable);
      LuaScriptRuntimeOwner runtimeOwner = zoneHandle == null ? new LuaScriptRuntimeOwner(this.definition.getFileName()) : zoneHandle.runtimeOwner;
      Location actionLocation = actionLocationValue != null && !actionLocationValue.isnil()
         ? this.support.toLocation(actionLocationValue)
         : this.eliteEntity.getLocation();
      ScriptRelativeVector vector = new ScriptRelativeVector(
         new ScriptRelativeVectorBlueprint("__lua_vector__", this.definition.getFileName(), spec), runtimeOwner, actionLocation
      );
      ScriptTargets selfTarget = new ScriptTargets(
         new ScriptTargetsBlueprint(Collections.emptyMap(), "__lua_vector__", this.definition.getFileName()), runtimeOwner
      );
      return new LuaPowerScriptApi.LuaRelativeVectorHandle(
         vector, () -> zoneHandle == null ? new ScriptActionData(this.eliteEntity, directTarget, selfTarget, event) : zoneHandle.actionData
      );
   }

   private Collection<LivingEntity> resolveTargets(LuaValue value) {
      LuaPowerScriptApi.LuaTargetHandle handle = this.extractHandle(value, LuaPowerScriptApi.LuaTargetHandle.class);
      return (Collection<LivingEntity>)(handle == null ? Collections.emptyList() : handle.resolver.resolveEntities());
   }

   private Vector resolveVector(LuaValue value) {
      LuaPowerScriptApi.LuaRelativeVectorHandle handle = this.extractHandle(value, LuaPowerScriptApi.LuaRelativeVectorHandle.class);
      return handle != null ? handle.resolve() : this.support.toVector(value);
   }

   private void damageTargets(Collection<LivingEntity> targets, double amount, double multiplier) {
      for (LivingEntity target : targets) {
         if (target instanceof Player) {
            CombatDamageContext.runEliteToPlayerMultiplier(multiplier, () -> this.damageTarget(target, amount));
         } else {
            this.damageTarget(target, amount);
         }
      }
   }

   private void damageTarget(LivingEntity target, double amount) {
      if (this.eliteEntity.getLivingEntity() != null) {
         target.damage(amount, this.eliteEntity.getLivingEntity());
      } else {
         target.damage(amount);
      }
   }

   private void pushTargets(Collection<LivingEntity> targets, Vector velocity, boolean additive) {
      Vector finalVelocity = velocity != null && this.isFiniteVector(velocity) ? velocity : new Vector(0, 0, 0);
      this.taskController.runLater(1, () -> {
         for (LivingEntity target : targets) {
            if (target.isValid()) {
               target.setVelocity(additive ? target.getVelocity().add(finalVelocity) : finalVelocity);
            }
         }
      });
   }

   private void setFacing(Collection<LivingEntity> targets, Vector direction) {
      if (direction != null) {
         for (LivingEntity target : targets) {
            Location location = target.getLocation();
            location.setDirection(direction);
            target.teleport(location);
         }
      }
   }

   private boolean isFiniteVector(Vector vector) {
      return Double.isFinite(vector.getX()) && Double.isFinite(vector.getY()) && Double.isFinite(vector.getZ());
   }

   private void spawnParticles(LuaValue targetValue, LuaValue particlesValue) {
      LuaPowerScriptApi.LuaTargetHandle handle = this.extractHandle(targetValue, LuaPowerScriptApi.LuaTargetHandle.class);
      if (handle != null && particlesValue != null && !particlesValue.isnil()) {
         ScriptParticles scriptParticles = this.buildScriptParticles(particlesValue);
         if (scriptParticles != null) {
            ScriptActionData actionData = handle.resolver.createActionData();

            boolean needsCentering = switch (handle.resolver.getTargetType()) {
               case ZONE_FULL, ZONE_BORDER, INHERIT_SCRIPT_ZONE_FULL, INHERIT_SCRIPT_ZONE_BORDER, LOCATION, LOCATIONS, LANDING_LOCATION -> true;
               default -> false;
            };

            for (Location location : handle.resolver.resolveLocations(actionData)) {
               Location targetLocation = needsCentering ? location.clone().add(0.5, 0.0, 0.5) : location;
               scriptParticles.visualize(actionData, targetLocation, handle.resolver.getRuntimeOwner());
            }
         }
      }
   }

   private ScriptParticles buildScriptParticles(LuaValue particlesValue) {
      Object rawParticles = LuaTableConverter.toJavaObject(particlesValue);
      List<Map<?, ?>> particleEntries = new ArrayList<>();
      if (rawParticles instanceof List) {
         for (Object entry : (List)rawParticles) {
            if (entry instanceof Map<?, ?> map) {
               particleEntries.add(map);
            }
         }
      } else if (rawParticles instanceof Map<?, ?> map) {
         particleEntries.add(map);
      } else if (particlesValue.isstring()) {
         particleEntries.add(Map.of("particle", particlesValue.tojstring()));
      }

      return particleEntries.isEmpty()
         ? null
         : new ScriptParticles(new ScriptParticlesBlueprint(particleEntries, "__lua_particles__", this.definition.getFileName()));
   }

   private LuaTable toEntityArray(Collection<LivingEntity> entities) {
      LuaTable results = new LuaTable();
      int index = 1;

      for (LivingEntity livingEntity : entities) {
         results.set(index++, this.entityTables.createEntityTable(livingEntity));
      }

      return results;
   }

   private LuaTable toLocationArray(Collection<Location> locations) {
      LuaTable results = new LuaTable();
      int index = 1;

      for (Location location : locations) {
         results.set(index++, this.support.toLocationTable(location));
      }

      return results;
   }

   private VarArgFunction method(final LuaTable owner, final LuaPowerScriptApi.LuaCallback callback) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            return callback.invoke(LuaPowerScriptApi.this.stripMethodSelf(args, owner));
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
   private interface ActionDataFactory {
      ScriptActionData create();
   }

   interface CallbackInvoker {
      void invoke(String var1, LuaFunction var2, LuaValue... var3);
   }

   @FunctionalInterface
   private interface LuaCallback {
      Varargs invoke(Varargs var1);
   }

   private static final class LuaRelativeVectorHandle {
      private final ScriptRelativeVector vector;
      private final LuaPowerScriptApi.ActionDataFactory actionDataFactory;

      private Vector resolve() {
         return this.vector.getVector(this.actionDataFactory.create());
      }

      @Generated
      public LuaRelativeVectorHandle(ScriptRelativeVector vector, LuaPowerScriptApi.ActionDataFactory actionDataFactory) {
         this.vector = vector;
         this.actionDataFactory = actionDataFactory;
      }
   }

   private static final class LuaTargetHandle {
      private final LuaPowerScriptApi.LuaTargetResolver resolver;

      @Generated
      public LuaTargetHandle(LuaPowerScriptApi.LuaTargetResolver resolver) {
         this.resolver = resolver;
      }
   }

   private static final class LuaTargetResolver {
      private final ScriptTargets scriptTargets;
      private final LuaPowerScriptApi.ActionDataFactory actionDataFactory;

      private Collection<LivingEntity> resolveEntities() {
         return this.resolveEntities(this.createActionData());
      }

      private Collection<LivingEntity> resolveEntities(ScriptActionData actionData) {
         return this.scriptTargets.getTargetEntities(actionData);
      }

      private Collection<Location> resolveLocations() {
         return this.resolveLocations(this.createActionData());
      }

      private Collection<Location> resolveLocations(ScriptActionData actionData) {
         return this.scriptTargets.getTargetLocations(actionData);
      }

      private ScriptActionData createActionData() {
         return this.actionDataFactory.create();
      }

      private TargetType getTargetType() {
         return this.scriptTargets.getTargetBlueprint().getTargetType();
      }

      private LuaScriptRuntimeOwner getRuntimeOwner() {
         return (LuaScriptRuntimeOwner)this.scriptTargets.getRuntimeOwner();
      }

      @Generated
      public LuaTargetResolver(ScriptTargets scriptTargets, LuaPowerScriptApi.ActionDataFactory actionDataFactory) {
         this.scriptTargets = scriptTargets;
         this.actionDataFactory = actionDataFactory;
      }
   }

   private final class LuaZoneHandle {
      private final LuaScriptRuntimeOwner runtimeOwner;
      private final ScriptZone scriptZone;
      private final ScriptActionData actionData;

      private LuaZoneHandle(LuaScriptRuntimeOwner runtimeOwner, ScriptZone scriptZone, ScriptActionData actionData) {
         this.runtimeOwner = runtimeOwner;
         this.scriptZone = scriptZone;
         this.actionData = actionData;
      }

      private LuaTable targetSpec(String targetType, LuaValue coverageValue) {
         LuaTable spec = new LuaTable();
         spec.set("targetType", targetType);
         if (coverageValue != null && coverageValue.isnumber()) {
            spec.set("coverage", coverageValue);
         }

         return spec;
      }

      private Collection<Location> resolveLocations(String targetType, LuaValue coverageValue) {
         Map<String, Object> targetSpec = new LinkedHashMap<>();
         targetSpec.put("targetType", targetType);
         if (coverageValue != null && coverageValue.isnumber()) {
            targetSpec.put("coverage", coverageValue.todouble());
         }

         ScriptTargets targets = new ScriptTargets(
            new ScriptTargetsBlueprint(targetSpec, "__lua_zone_target__", this.runtimeOwner.getFileName()), this.runtimeOwner
         );
         return targets.getTargetLocations(this.actionData);
      }

      private Collection<LivingEntity> resolveEntities(String targetType) {
         Map<String, Object> targetSpec = new LinkedHashMap<>();
         targetSpec.put("targetType", targetType);
         ScriptTargets targets = new ScriptTargets(
            new ScriptTargetsBlueprint(targetSpec, "__lua_zone_target__", this.runtimeOwner.getFileName()), this.runtimeOwner
         );
         return targets.getTargetEntities(this.actionData);
      }

      private boolean contains(LuaValue locationValue, String mode) {
         Location location = LuaPowerScriptApi.this.support.toLocation(locationValue);
         if (location == null) {
            return false;
         } else {
            boolean border = "border".equalsIgnoreCase(mode);
            boolean staticZone = this.scriptZone.getZoneBlueprint().getAnimationDuration().getValue() == 0;

            for (Shape shape : this.scriptZone.generateShapes(this.actionData, staticZone)) {
               if (border ? shape.borderContains(location) : shape.contains(location)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   interface OwnedTaskController {
      int runLater(int var1, Runnable var2);

      int runRepeating(int var1, int var2, Runnable var3);

      void cancel(int var1);

      void ownCleanup(Runnable var1);
   }
}
