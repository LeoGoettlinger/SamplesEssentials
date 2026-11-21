package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.power.ElitePowerActionPosition;
import com.magmaguy.elitemobs.api.power.ElitePowerActionRequest;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptInstance;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptQueryResult;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptableEntity;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Shape;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class ScriptableBoss extends ScriptableEntity {
   public static final ScriptHook ON_DAMAGED = new ScriptHook("on_boss_damaged");
   public static final ScriptHook ON_DAMAGED_BY_PLAYER = new ScriptHook("on_boss_damaged_by_player");
   public static final ScriptHook ON_REINFORCEMENT_DAMAGED_BY_PLAYER = new ScriptHook("on_reinforcement_damaged_by_player");
   public static final ScriptHook ON_DAMAGED_BY_ELITE = new ScriptHook("on_boss_damaged_by_elite");
   public static final ScriptHook ON_PLAYER_DAMAGED = new ScriptHook("on_player_damaged_by_boss");
   public static final ScriptHook ON_ENTER_COMBAT = new ScriptHook("on_enter_combat");
   public static final ScriptHook ON_EXIT_COMBAT = new ScriptHook("on_exit_combat");
   public static final ScriptHook ON_HEAL = new ScriptHook("on_heal");
   public static final ScriptHook ON_TARGET = new ScriptHook("on_boss_target_changed");
   public static final ScriptHook ON_DEATH = new ScriptHook("on_death");
   public static final ScriptHook ON_PHASE_SWITCH = new ScriptHook("on_phase_switch");
   public static final ScriptHook ON_MIND_ACTION = new ScriptHook("on_mind_action");
   private static final Set<ScriptHook> SUPPORTED_HOOKS = Set.of(
      ScriptHook.ON_SPAWN,
      ScriptHook.ON_TICK,
      ScriptHook.ON_ZONE_ENTER,
      ScriptHook.ON_ZONE_LEAVE,
      ON_DAMAGED,
      ON_DAMAGED_BY_PLAYER,
      ON_REINFORCEMENT_DAMAGED_BY_PLAYER,
      ON_DAMAGED_BY_ELITE,
      ON_PLAYER_DAMAGED,
      ON_ENTER_COMBAT,
      ON_EXIT_COMBAT,
      ON_HEAL,
      ON_TARGET,
      ON_DEATH,
      ON_PHASE_SWITCH,
      ON_MIND_ACTION
   );
   private final EliteEntity eliteEntity;
   private boolean helpersReady = false;
   private String fileName;
   private LuaPowerSupport support;
   private LuaPowerEntityTables entityTables;
   private LuaPowerContextTables contextTables;
   private LuaPowerScriptApi scriptApi;
   private ElitePowerActionRequest currentMindAction;

   public ScriptableBoss(EliteEntity eliteEntity) {
      this.eliteEntity = eliteEntity;
   }

   public EliteEntity getEliteEntity() {
      return this.eliteEntity;
   }

   ScriptQueryResult handleMindAction(ScriptInstance instance, ElitePowerActionRequest request) {
      if (request.actor() != this.eliteEntity) {
         throw new IllegalArgumentException("Mind action actor does not own this Lua power");
      } else {
         ElitePowerActionRequest previous = this.currentMindAction;
         this.currentMindAction = request;

         ScriptQueryResult var4;
         try {
            var4 = instance.handleQuery(ON_MIND_ACTION, null, null, null);
         } finally {
            this.currentMindAction = previous;
         }

         return var4;
      }
   }

   private void ensureHelpers(final ScriptInstance instance) {
      if (!this.helpersReady) {
         this.fileName = instance.getDefinition().getFileName();
         this.support = new LuaPowerSupport(instance.getDefinition(), this.eliteEntity);
         LuaPowerScriptApi.OwnedTaskController taskController = new LuaPowerScriptApi.OwnedTaskController() {
            @Override
            public int runLater(int ticks, Runnable runnable) {
               return instance.ownLater(ticks, runnable);
            }

            @Override
            public int runRepeating(int initialDelayTicks, int intervalTicks, Runnable runnable) {
               return instance.ownRepeating(initialDelayTicks, intervalTicks, runnable);
            }

            @Override
            public void cancel(int taskId) {
               instance.cancelOwned(taskId);
            }

            @Override
            public void ownCleanup(Runnable cleanup) {
               instance.ownCleanup(cleanup);
            }
         };
         LuaPowerScriptApi.CallbackInvoker callbackInvoker = instance::invokeOwnedCallback;
         this.entityTables = new LuaPowerEntityTables(instance.getDefinition(), this.eliteEntity, this.support, taskController, callbackInvoker);
         this.contextTables = new LuaPowerContextTables(
            instance.getDefinition(), this.eliteEntity, this.support, this.entityTables, taskController, callbackInvoker
         );
         this.scriptApi = new LuaPowerScriptApi(instance.getDefinition(), this.eliteEntity, this.support, this.entityTables, taskController, callbackInvoker);
         this.helpersReady = true;
      }
   }

   @Override
   public LuaTable buildContextTable(ScriptInstance instance) {
      this.ensureHelpers(instance);
      return this.entityTables.createBossTable();
   }

   @Override
   public String getContextKey() {
      return "boss";
   }

   @Override
   public Set<ScriptHook> getSupportedHooks() {
      return SUPPORTED_HOOKS;
   }

   @Override
   public Entity getBukkitEntity() {
      return this.eliteEntity.getLivingEntity();
   }

   @Override
   public Location getLocation() {
      return this.eliteEntity.getLocation();
   }

   @Override
   public LuaValue resolveExtraContext(String key, ScriptInstance instance) {
      this.ensureHelpers(instance);
      LivingEntity directTarget = instance.getCurrentDirectTarget();
      LivingEntity eventActor = instance.getCurrentEventActor();
      Event event = instance.getCurrentEvent();
      Player contextPlayer = this.resolveContextPlayer(directTarget, eventActor);

      return (LuaValue)(switch (key) {
         case "log" -> this.createLogTable();
         case "cooldowns" -> this.createCooldownTable();
         case "scheduler" -> this.createSchedulerTable(instance);
         case "players" -> this.contextTables.createPlayersTable(eventActor);
         case "entities" -> this.contextTables.createEntitiesTable(directTarget);
         case "vectors" -> this.contextTables.createVectorsTable();
         case "world" -> this.contextTables.createWorldTable();
         case "settings" -> this.contextTables.createSettingsTable();
         case "zones" -> this.createZonesTable(instance);
         case "player" -> contextPlayer == null ? LuaValue.NIL : this.entityTables.createPlayerTable(contextPlayer);
         case "event" -> event == null ? LuaValue.NIL : this.entityTables.createEventTable(event);
         case "script" -> this.scriptApi.createTable(event, directTarget);
         case "mind_action" -> this.createMindActionTable();
         default -> LuaValue.NIL;
      });
   }

   private LuaValue createMindActionTable() {
      ElitePowerActionRequest request = this.currentMindAction;
      if (request == null) {
         return LuaValue.NIL;
      } else {
         LuaTable action = new LuaTable();
         action.set("key", LuaValue.valueOf(request.actionKey().toString()));
         action.set("game_tick", LuaValue.valueOf((double)request.gameTick()));
         action.set("generation", LuaValue.valueOf((double)request.generation()));
         LuaTable payload = new LuaTable();

         for (Entry<String, Object> entry : request.payload().entrySet()) {
            payload.set(entry.getKey(), mindActionValue(entry.getValue()));
         }

         action.set("payload", payload);
         return action;
      }
   }

   private static LuaValue mindActionValue(Object value) {
      if (value instanceof String string) {
         return LuaValue.valueOf(string);
      } else if (value instanceof Boolean flag) {
         return LuaValue.valueOf(flag);
      } else if (value instanceof Long number) {
         return LuaValue.valueOf(number.doubleValue());
      } else if (value instanceof Double number) {
         return LuaValue.valueOf(number);
      } else if (value instanceof UUID uuid) {
         return LuaValue.valueOf(uuid.toString());
      } else if (value instanceof ElitePowerActionPosition position) {
         LuaTable table = new LuaTable();
         table.set("world", LuaValue.valueOf(position.world()));
         table.set("x", LuaValue.valueOf(position.x()));
         table.set("y", LuaValue.valueOf(position.y()));
         table.set("z", LuaValue.valueOf(position.z()));
         return table;
      } else {
         throw new IllegalArgumentException("Unsupported Mind action payload type " + value.getClass().getName());
      }
   }

   private Player resolveContextPlayer(LivingEntity directTarget, LivingEntity eventActor) {
      if (directTarget instanceof Player player) {
         return player;
      } else {
         return eventActor instanceof Player player ? player : null;
      }
   }

   private LuaTable createLogTable() {
      LuaTable log = new LuaTable();
      log.set("info", this.logFunction(log, message -> Logger.info("[LuaPower] " + message)));
      log.set("warn", this.logFunction(log, Logger::warn));
      log.set("debug", this.logFunction(log, message -> Logger.info("[LuaPowerDebug] " + message)));
      return log;
   }

   private LuaTable createCooldownTable() {
      LuaTable cooldowns = new LuaTable();
      cooldowns.set("local_ready", this.method(cooldowns, args -> LuaValue.valueOf(this.eliteEntity.isSharedCooldownReady(this.resolveCooldownKey(args, 1)))));
      cooldowns.set(
         "local_remaining",
         this.method(cooldowns, args -> LuaValue.valueOf((double)this.eliteEntity.getSharedCooldownRemainingTicks(this.resolveCooldownKey(args, 1))))
      );
      cooldowns.set("check_local", this.method(cooldowns, args -> {
         String key = this.resolveCooldownKey(args, 1);
         int duration = args.checkint(2);
         if (!this.eliteEntity.isSharedCooldownReady(key)) {
            return LuaValue.FALSE;
         } else {
            this.eliteEntity.setSharedCooldown(key, duration);
            return LuaValue.TRUE;
         }
      }));
      cooldowns.set("set_local", this.method(cooldowns, args -> {
         this.eliteEntity.setSharedCooldown(this.resolveCooldownKey(args, 2), args.checklong(1));
         return LuaValue.NIL;
      }));
      cooldowns.set("global_ready", this.method(cooldowns, args -> LuaValue.valueOf(!this.eliteEntity.isInCooldown())));
      cooldowns.set("set_global", this.method(cooldowns, args -> {
         this.eliteEntity.doGlobalPowerCooldown(args.checkint(1));
         return LuaValue.NIL;
      }));
      return cooldowns;
   }

   private LuaTable createSchedulerTable(ScriptInstance instance) {
      LuaTable scheduler = new LuaTable();
      scheduler.set("run_after", this.method(scheduler, args -> {
         int ticks = args.checkint(1);
         LuaFunction callback = args.checkfunction(2);
         return LuaValue.valueOf(instance.ownLuaLater(ticks, callback));
      }));
      scheduler.set("run_every", this.method(scheduler, args -> {
         int ticks = args.checkint(1);
         LuaFunction callback = args.checkfunction(2);
         return LuaValue.valueOf(instance.ownLuaRepeating(0, ticks, callback));
      }));
      scheduler.set("cancel_task", this.method(scheduler, args -> {
         instance.cancelOwned(args.checkint(1));
         return LuaValue.NIL;
      }));
      scheduler.set("run_later", this.method(scheduler, args -> {
         int ticks = args.checkint(1);
         LuaFunction callback = args.checkfunction(2);
         return LuaValue.valueOf(instance.ownLuaLater(ticks, callback));
      }));
      scheduler.set("run_repeating", this.method(scheduler, args -> {
         int delay = args.checkint(1);
         int interval = args.checkint(2);
         LuaFunction callback = args.checkfunction(3);
         return LuaValue.valueOf(instance.ownLuaRepeating(delay, interval, callback));
      }));
      scheduler.set("cancel", this.method(scheduler, args -> {
         instance.cancelOwned(args.checkint(1));
         return LuaValue.NIL;
      }));
      return scheduler;
   }

   private LuaTable createZonesTable(ScriptInstance instance) {
      LuaTable zones = new LuaTable();
      zones.set("get_entities_in_zone", this.method(zones, args -> this.contextTables.queryZoneEntities(args.arg1(), args.arg(2))));
      zones.set("get_locations_in_zone", this.method(zones, args -> this.contextTables.queryZoneLocations(args.arg1(), args.arg(2))));
      zones.set("zone_contains", this.method(zones, args -> {
         Shape shape = this.support.createShape(args.arg1());
         Location location = this.support.toLocation(args.arg(2));
         boolean border = args.narg() >= 3 && "border".equalsIgnoreCase(args.arg(3).optjstring("full"));
         return shape != null && location != null ? LuaValue.valueOf(border ? shape.borderContains(location) : shape.contains(location)) : LuaValue.FALSE;
      }));
      zones.set(
         "watch_zone",
         this.method(
            zones,
            args -> {
               Shape shape = this.support.createShape(args.arg1());
               LuaTable callbacks = args.checktable(2);
               if (shape == null) {
                  return LuaValue.NIL;
               } else {
                  LuaFunction onEnter = callbacks.get("on_enter").isfunction() ? callbacks.get("on_enter").checkfunction() : null;
                  LuaFunction onLeave = callbacks.get("on_leave").isfunction() ? callbacks.get("on_leave").checkfunction() : null;
                  ScriptableBoss.ZoneWatch watch = new ScriptableBoss.ZoneWatch(
                     instance, shape, onEnter, onLeave, this.support.resolveZoneFilter(args.arg(3)), this.support.resolveZoneMode(args.arg(3))
                  );
                  int[] taskId = new int[1];
                  taskId[0] = instance.ownRepeating(1, 1, () -> {
                     if (this.eliteEntity.exists() && this.eliteEntity.getLocation() != null && this.eliteEntity.getLocation().getWorld() != null) {
                        watch.tick();
                     } else {
                        instance.cancelOwned(taskId[0]);
                     }
                  });
                  return LuaValue.NIL;
               }
            }
         )
      );
      return zones;
   }

   private String resolveCooldownKey(Varargs args, int index) {
      return args.narg() >= index && !args.arg(index).isnil() ? args.checkjstring(index) : "__lua:" + this.fileName;
   }

   private VarArgFunction logFunction(final LuaTable owner, final Consumer<String> consumer) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            Varargs strippedArgs = ScriptableBoss.this.stripMethodSelf(args, owner);
            if (strippedArgs.narg() > 0 && strippedArgs.arg1().isstring()) {
               consumer.accept(strippedArgs.arg1().tojstring());
            }

            return LuaValue.NIL;
         }
      };
   }

   private VarArgFunction method(final LuaTable owner, final ScriptableBoss.LuaCallback callback) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            return callback.invoke(ScriptableBoss.this.stripMethodSelf(args, owner));
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

   private final class ZoneWatch {
      private final ScriptInstance instance;
      private final Shape shape;
      private final LuaFunction onEnter;
      private final LuaFunction onLeave;
      private final String filter;
      private final boolean borderMode;
      private final Map<UUID, LivingEntity> currentInside = new HashMap<>();

      private ZoneWatch(ScriptInstance instance, Shape shape, LuaFunction onEnter, LuaFunction onLeave, String filter, boolean borderMode) {
         this.instance = instance;
         this.shape = shape;
         this.onEnter = onEnter;
         this.onLeave = onLeave;
         this.filter = filter;
         this.borderMode = borderMode;
      }

      private void tick() {
         Set<UUID> seen = new HashSet<>();

         for (LivingEntity livingEntity : ScriptableBoss.this.support.filterEntities(ScriptableBoss.this.eliteEntity.getLocation().getWorld(), this.filter)) {
            if (!livingEntity.equals(ScriptableBoss.this.eliteEntity.getLivingEntity())) {
               boolean contains = this.borderMode ? this.shape.borderContains(livingEntity) : this.shape.contains(livingEntity);
               if (contains) {
                  seen.add(livingEntity.getUniqueId());
                  if (!this.currentInside.containsKey(livingEntity.getUniqueId())) {
                     this.currentInside.put(livingEntity.getUniqueId(), livingEntity);
                     if (this.onEnter != null) {
                        this.instance
                           .invokeOwnedCallback("a zone enter callback", this.onEnter, ScriptableBoss.this.entityTables.createEntityTable(livingEntity));
                     }
                  }
               }
            }
         }

         Iterator<Entry<UUID, LivingEntity>> iterator = this.currentInside.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<UUID, LivingEntity> entry = iterator.next();
            if (!seen.contains(entry.getKey())) {
               LivingEntity leavingEntity = entry.getValue();
               iterator.remove();
               if (this.onLeave != null && leavingEntity != null) {
                  this.instance.invokeOwnedCallback("a zone leave callback", this.onLeave, ScriptableBoss.this.entityTables.createEntityTable(leavingEntity));
               }
            }
         }
      }
   }
}
