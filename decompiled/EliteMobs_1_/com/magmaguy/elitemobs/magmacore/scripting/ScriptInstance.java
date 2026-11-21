package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaWorldTable;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Cuboid;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Cylinder;
import com.magmaguy.elitemobs.magmacore.scripting.zones.ScriptZone;
import com.magmaguy.elitemobs.magmacore.scripting.zones.Sphere;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class ScriptInstance {
   private final ScriptDefinition definition;
   private final ScriptableEntity entity;
   private final LuaTable stateTable = new LuaTable();
   private final Map<Integer, ScriptInstance.OwnedTask> ownedTasks = new LinkedHashMap<>();
   private final Deque<Runnable> ownedCleanup = new ArrayDeque<>();
   private final Map<Integer, ScriptZone> zoneWatches = new LinkedHashMap<>();
   private int nextZoneHandle = 1;
   private LuaTable scriptTable;
   private Integer tickTaskId = null;
   private boolean closed = false;
   private boolean paused = false;
   private Event currentEvent = null;
   private LivingEntity currentEventActor = null;
   private LivingEntity currentDirectTarget = null;

   public ScriptInstance(ScriptDefinition definition, ScriptableEntity entity) {
      this.definition = definition;
      this.entity = entity;
   }

   public boolean isClosed() {
      return this.closed;
   }

   public boolean hasOwnedWorkExcept(int hostGuardTask) {
      return this.tickTaskId != null
         || !this.zoneWatches.isEmpty()
         || !this.ownedCleanup.isEmpty()
         || this.ownedTasks.keySet().stream().anyMatch(id -> id != hostGuardTask);
   }

   public void ownCleanup(Runnable cleanup) {
      Objects.requireNonNull(cleanup, "cleanup");
      if (this.closed) {
         cleanup.run();
      } else {
         this.ownedCleanup.addFirst(cleanup);
      }
   }

   public void setPaused(boolean paused) {
      if (!this.closed) {
         this.paused = paused;
      }
   }

   public boolean isPaused() {
      return this.paused;
   }

   public void start() {
      if (!this.closed) {
         this.ensureScriptTable();
      }
   }

   public LivingEntity getCurrentEventActor() {
      return this.currentEventActor;
   }

   public Event getCurrentEvent() {
      return this.currentEvent;
   }

   public LivingEntity getCurrentDirectTarget() {
      return this.currentDirectTarget;
   }

   public int ownLater(int ticks, Runnable runnable) {
      this.requireOpen();
      JavaPlugin plugin = MagmaCore.getInstance().getRequestingPlugin();
      int[] holder = new int[1];
      BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
         if (!this.paused) {
            Bukkit.getScheduler().cancelTask(holder[0]);
            this.ownedTasks.remove(holder[0]);
            runnable.run();
         }
      }, ticks, 1L);
      holder[0] = task.getTaskId();
      this.ownedTasks.put(holder[0], () -> Bukkit.getScheduler().cancelTask(holder[0]));
      return holder[0];
   }

   public int ownRepeating(int delayTicks, int intervalTicks, Runnable runnable) {
      this.requireOpen();
      JavaPlugin plugin = MagmaCore.getInstance().getRequestingPlugin();
      BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
         if (!this.paused) {
            runnable.run();
         }
      }, delayTicks, intervalTicks);
      int id = task.getTaskId();
      this.ownedTasks.put(id, () -> Bukkit.getScheduler().cancelTask(id));
      return id;
   }

   public int ownLuaLater(int ticks, LuaFunction callback) {
      return this.ownLaterTask(ticks, callback);
   }

   public int ownLuaRepeating(int delayTicks, int intervalTicks, LuaFunction callback) {
      return this.ownRepeatingTask(delayTicks, intervalTicks, callback);
   }

   public void cancelOwned(int taskId) {
      this.cancelOwnedTask(taskId);
   }

   public void invokeOwnedCallback(String failureContext, LuaFunction callback, LuaValue... args) {
      if (!this.closed && !this.paused) {
         try {
            LuaExecutionBudget.run(() -> callback.invoke(LuaValue.varargsOf(args)));
         } catch (Exception var5) {
            this.logLuaError(failureContext, var5);
            this.shutdown();
         }
      }
   }

   public void handleEvent(ScriptHook hook, Event event, LivingEntity directTarget, LivingEntity eventActor) {
      this.invokeHook(hook, event, directTarget, eventActor);
   }

   public ScriptQueryResult handleQuery(ScriptHook hook, Event event, LivingEntity directTarget, LivingEntity eventActor) {
      ScriptInstance.HookInvocation invocation = this.invokeHook(hook, event, directTarget, eventActor);
      if (invocation.kind() == ScriptInstance.HookInvocationKind.UNHANDLED) {
         return ScriptQueryResult.unhandled();
      } else if (invocation.kind() == ScriptInstance.HookInvocationKind.FAILED) {
         return ScriptQueryResult.failed();
      } else {
         LuaValue value = invocation.value();

         try {
            if (value.isnil()) {
               return ScriptQueryResult.nil();
            } else if (value.type() == 4) {
               return ScriptQueryResult.string(value.checkjstring());
            } else if (value.isboolean()) {
               return ScriptQueryResult.bool(value.checkboolean());
            } else if (value.isnumber()) {
               return ScriptQueryResult.number(value.checkdouble());
            } else {
               throw new IllegalArgumentException("Lua query hooks may return only nil, string, number, or boolean");
            }
         } catch (Exception var8) {
            this.logLuaError(hook.getKey() + " result", var8);
            this.shutdown();
            return ScriptQueryResult.failed();
         }
      }
   }

   private ScriptInstance.HookInvocation invokeHook(ScriptHook hook, Event event, LivingEntity directTarget, LivingEntity eventActor) {
      if (!this.closed && !this.paused && hook != null && this.definition.supportsHook(hook)) {
         Event previousEvent = this.currentEvent;
         LivingEntity previousEventActor = this.currentEventActor;
         LivingEntity previousDirectTarget = this.currentDirectTarget;

         ScriptInstance.HookInvocation var16;
         try {
            this.ensureScriptTable();
            LuaValue function = this.scriptTable.get(hook.getKey());
            if (function.isfunction()) {
               this.currentEvent = event;
               this.currentEventActor = eventActor;
               this.currentDirectTarget = directTarget;
               LuaValue result = LuaExecutionBudget.run(() -> function.checkfunction().call(this.buildContext(event, directTarget, eventActor)));
               return ScriptInstance.HookInvocation.handled(result);
            }

            var16 = ScriptInstance.HookInvocation.unhandled();
         } catch (Exception var14) {
            this.logLuaError(hook.getKey(), var14);
            this.shutdown();
            return ScriptInstance.HookInvocation.failed();
         } finally {
            this.currentEvent = previousEvent;
            this.currentEventActor = previousEventActor;
            this.currentDirectTarget = previousDirectTarget;
         }

         return var16;
      } else {
         return ScriptInstance.HookInvocation.unhandled();
      }
   }

   public void onTick() {
      if (!this.closed && !this.paused) {
         if (!this.entity.isScriptOwnerActive()) {
            this.shutdown();
         } else {
            this.tickZoneWatches();
            if (this.definition.supportsHook(ScriptHook.ON_TICK)) {
               this.handleEvent(ScriptHook.ON_TICK, null, null, null);
            }
         }
      }
   }

   public void shutdown() {
      if (!this.closed) {
         this.closed = true;
         if (this.tickTaskId != null) {
            Bukkit.getScheduler().cancelTask(this.tickTaskId);
            this.tickTaskId = null;
         }

         for (ScriptInstance.OwnedTask task : new ArrayList<>(this.ownedTasks.values())) {
            task.cancel();
         }

         this.ownedTasks.clear();

         for (ScriptZone zone : this.zoneWatches.values()) {
            zone.shutdown();
         }

         this.zoneWatches.clear();

         while (!this.ownedCleanup.isEmpty()) {
            try {
               this.ownedCleanup.removeFirst().run();
            } catch (RuntimeException var3) {
               this.logLuaError("owned cleanup", var3);
            }
         }

         this.entity.onShutdown();
      }
   }

   private void ensureScriptTable() {
      if (this.scriptTable == null) {
         this.scriptTable = this.definition.instantiate();
         this.updateTickRegistration();
      }
   }

   private LuaValue buildContext(final Event event, final LivingEntity directTarget, final LivingEntity eventActor) {
      LuaTable context = new LuaTable();
      context.set("state", this.stateTable);
      LuaTable metatable = new LuaTable();
      metatable.set("__index", new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            LuaTable owner = args.arg1().checktable();
            LuaValue keyValue = args.arg(2);
            if (!keyValue.isstring()) {
               return LuaValue.NIL;
            } else {
               String key = keyValue.tojstring();
               LuaValue resolved = ScriptInstance.this.resolveContextValue(key, event, directTarget, eventActor);
               if (!resolved.isnil()) {
                  owner.rawset(key, resolved);
               }

               return resolved;
            }
         }
      });
      context.setmetatable(metatable);
      return context;
   }

   private LuaValue resolveContextValue(String key, Event event, LivingEntity directTarget, LivingEntity eventActor) {
      if (key.equals(this.entity.getContextKey())) {
         return this.entity.buildContextTable(this);
      } else {
         LuaValue custom = this.entity.resolveExtraContext(key, this);
         if (custom != null && !custom.isnil()) {
            return custom;
         } else {
            return (LuaValue)(switch (key) {
               case "log" -> this.createLogTable();
               case "cooldowns" -> this.createCooldownTable();
               case "scheduler" -> this.createSchedulerTable();
               case "world" -> {
                  Location loc = this.entity.getLocation();
                  yield loc != null && loc.getWorld() != null ? LuaWorldTable.build(loc.getWorld()) : LuaValue.NIL;
               }
               case "zones" -> this.createZonesTable();
               case "event" -> this.createEventTable();
               case "player" -> {
                  LivingEntity player = this.resolveContextPlayer(directTarget, eventActor);
                  yield player == null ? LuaValue.NIL : LuaLivingEntityTable.build(player);
               }
               default -> LuaValue.NIL;
            });
         }
      }
   }

   private LivingEntity resolveContextPlayer(LivingEntity directTarget, LivingEntity eventActor) {
      if (eventActor instanceof Player) {
         return eventActor;
      } else {
         return directTarget instanceof Player ? directTarget : null;
      }
   }

   private LuaTable createLogTable() {
      LuaTable log = new LuaTable();
      log.set("info", this.logFunction(log, message -> Logger.info("[Lua] " + message)));
      log.set("warn", this.logFunction(log, Logger::warn));
      log.set("error", this.logFunction(log, Logger::warn));
      return log;
   }

   private VarArgFunction logFunction(final LuaTable owner, final Consumer<String> consumer) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            Varargs strippedArgs = LuaTableSupport.stripMethodSelf(args, owner);
            if (strippedArgs.narg() > 0 && strippedArgs.arg1().isstring()) {
               consumer.accept(strippedArgs.arg1().tojstring());
            }

            return LuaValue.NIL;
         }
      };
   }

   private LuaTable createSchedulerTable() {
      LuaTable scheduler = new LuaTable();
      scheduler.set("run_later", this.method(scheduler, args -> {
         int ticks = args.checkint(1);
         LuaFunction callback = args.checkfunction(2);
         return LuaValue.valueOf(this.ownLaterTask(ticks, callback));
      }));
      scheduler.set("run_repeating", this.method(scheduler, args -> {
         int delay = args.checkint(1);
         int interval = args.checkint(2);
         LuaFunction callback = args.checkfunction(3);
         return LuaValue.valueOf(this.ownRepeatingTask(delay, interval, callback));
      }));
      scheduler.set("cancel", this.method(scheduler, args -> {
         this.cancelOwnedTask(args.checkint(1));
         return LuaValue.NIL;
      }));
      scheduler.set("run_after", this.method(scheduler, args -> {
         int ticks = args.checkint(1);
         LuaFunction callback = args.checkfunction(2);
         return LuaValue.valueOf(this.ownLaterTask(ticks, callback));
      }));
      scheduler.set("run_every", this.method(scheduler, args -> {
         int interval = args.checkint(1);
         LuaFunction callback = args.checkfunction(2);
         return LuaValue.valueOf(this.ownRepeatingTask(0, interval, callback));
      }));
      scheduler.set("cancel_task", this.method(scheduler, args -> {
         this.cancelOwnedTask(args.checkint(1));
         return LuaValue.NIL;
      }));
      return scheduler;
   }

   public boolean isCooldownReady(String key) {
      Map<String, Long> cooldowns = this.entity.getLocalCooldownStore(this.definition);
      Long expiresAt = cooldowns.get(key);
      if (expiresAt == null) {
         return true;
      } else if (expiresAt <= System.nanoTime()) {
         cooldowns.remove(key);
         return true;
      } else {
         return false;
      }
   }

   public long getCooldownRemainingTicks(String key) {
      Map<String, Long> cooldowns = this.entity.getLocalCooldownStore(this.definition);
      Long expiresAt = cooldowns.get(key);
      if (expiresAt == null) {
         return 0L;
      } else {
         long remainingNanos = expiresAt - System.nanoTime();
         if (remainingNanos <= 0L) {
            cooldowns.remove(key);
            return 0L;
         } else {
            return Math.max(1L, remainingNanos / 50000000L);
         }
      }
   }

   public void setCooldown(String key, long ticks) {
      Map<String, Long> cooldowns = this.entity.getLocalCooldownStore(this.definition);
      if (ticks <= 0L) {
         cooldowns.remove(key);
      } else {
         cooldowns.put(key, System.nanoTime() + ticks * 50000000L);
      }
   }

   private String resolveCooldownKey(Varargs args, int index) {
      return args.narg() >= index && !args.arg(index).isnil() ? args.checkjstring(index) : "__lua:" + this.definition.getFileName();
   }

   private LuaTable createCooldownTable() {
      LuaTable cd = new LuaTable();
      cd.set("local_ready", this.method(cd, args -> LuaValue.valueOf(this.isCooldownReady(this.resolveCooldownKey(args, 1)))));
      cd.set("local_remaining", this.method(cd, args -> LuaValue.valueOf((double)this.getCooldownRemainingTicks(this.resolveCooldownKey(args, 1)))));
      cd.set("check_local", this.method(cd, args -> {
         String key = this.resolveCooldownKey(args, 1);
         int duration = args.checkint(2);
         if (!this.isCooldownReady(key)) {
            return LuaValue.FALSE;
         } else {
            this.setCooldown(key, duration);
            return LuaValue.TRUE;
         }
      }));
      cd.set("set_local", this.method(cd, args -> {
         this.setCooldown(this.resolveCooldownKey(args, 2), args.checklong(1));
         return LuaValue.NIL;
      }));
      Map<String, Long> globalStore = this.entity.getGlobalCooldownStore();
      cd.set("global_ready", this.method(cd, args -> LuaValue.valueOf(isGlobalCooldownReady(globalStore))));
      cd.set("set_global", this.method(cd, args -> {
         setGlobalCooldown(globalStore, args.checklong(1));
         return LuaValue.NIL;
      }));
      return cd;
   }

   private static boolean isGlobalCooldownReady(Map<String, Long> store) {
      Long expiresAt = store.get("__global");
      if (expiresAt == null) {
         return true;
      } else if (expiresAt <= System.nanoTime()) {
         store.remove("__global");
         return true;
      } else {
         return false;
      }
   }

   private static void setGlobalCooldown(Map<String, Long> store, long ticks) {
      if (ticks <= 0L) {
         store.remove("__global");
      } else {
         store.put("__global", System.nanoTime() + ticks * 50000000L);
      }
   }

   private void updateTickRegistration() {
      boolean shouldTick = !this.closed
         && this.entity.isScriptOwnerActive()
         && (this.definition.supportsHook(ScriptHook.ON_TICK) || !this.zoneWatches.isEmpty());
      if (shouldTick && this.tickTaskId == null) {
         JavaPlugin plugin = MagmaCore.getInstance().getRequestingPlugin();
         BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, this::onTick, 1L, 1L);
         this.tickTaskId = task.getTaskId();
      } else if (!shouldTick && this.tickTaskId != null) {
         Bukkit.getScheduler().cancelTask(this.tickTaskId);
         this.tickTaskId = null;
      }
   }

   private int ownLaterTask(int ticks, LuaFunction callback) {
      this.requireOpen();
      JavaPlugin plugin = MagmaCore.getInstance().getRequestingPlugin();
      int[] taskIdHolder = new int[1];
      BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
         if (!this.paused) {
            Bukkit.getScheduler().cancelTask(taskIdHolder[0]);
            this.ownedTasks.remove(taskIdHolder[0]);
            this.runCallback(callback);
         }
      }, ticks, 1L);
      taskIdHolder[0] = task.getTaskId();
      this.ownedTasks.put(taskIdHolder[0], () -> Bukkit.getScheduler().cancelTask(taskIdHolder[0]));
      return taskIdHolder[0];
   }

   private int ownRepeatingTask(int delay, int interval, LuaFunction callback) {
      this.requireOpen();
      JavaPlugin plugin = MagmaCore.getInstance().getRequestingPlugin();
      BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
         if (!this.paused) {
            this.runCallback(callback);
         }
      }, delay, interval);
      int taskId = task.getTaskId();
      this.ownedTasks.put(taskId, () -> Bukkit.getScheduler().cancelTask(taskId));
      return taskId;
   }

   private void requireOpen() {
      if (this.closed) {
         throw new IllegalStateException("Script instance is closed");
      }
   }

   private void cancelOwnedTask(int taskId) {
      ScriptInstance.OwnedTask task = this.ownedTasks.remove(taskId);
      if (task != null) {
         task.cancel();
      }
   }

   private void runCallback(LuaFunction callback) {
      if (!this.closed && !this.paused) {
         try {
            LuaExecutionBudget.run(() -> callback.call(this.buildContext(null, null, null)));
         } catch (Exception var3) {
            this.logLuaError("scheduled callback", var3);
            this.shutdown();
         }
      }
   }

   private LuaTable createZonesTable() {
      LuaTable zones = new LuaTable();
      zones.set("create_sphere", this.method(zones, args -> {
         double x = args.checkdouble(1);
         double y = args.checkdouble(2);
         double z = args.checkdouble(3);
         double radius = args.checkdouble(4);
         Location loc = this.entity.getLocation();
         World world = loc != null ? loc.getWorld() : null;
         if (world == null) {
            return LuaValue.NIL;
         } else {
            Sphere shape = new Sphere(radius, new Location(world, x, y, z), 1.0);
            return LuaValue.valueOf(this.registerZone(new ScriptZone(shape)));
         }
      }));
      zones.set("create_cylinder", this.method(zones, args -> {
         double x = args.checkdouble(1);
         double y = args.checkdouble(2);
         double z = args.checkdouble(3);
         double radius = args.checkdouble(4);
         double height = args.checkdouble(5);
         Location loc = this.entity.getLocation();
         World world = loc != null ? loc.getWorld() : null;
         if (world == null) {
            return LuaValue.NIL;
         } else {
            Cylinder shape = new Cylinder(new Location(world, x, y, z), radius, height, 1.0);
            return LuaValue.valueOf(this.registerZone(new ScriptZone(shape)));
         }
      }));
      zones.set("create_cuboid", this.method(zones, args -> {
         double x = args.checkdouble(1);
         double y = args.checkdouble(2);
         double z = args.checkdouble(3);
         float xSize = (float)args.checkdouble(4);
         float ySize = (float)args.checkdouble(5);
         float zSize = (float)args.checkdouble(6);
         Location loc = this.entity.getLocation();
         World world = loc != null ? loc.getWorld() : null;
         if (world == null) {
            return LuaValue.NIL;
         } else {
            Cuboid shape = new Cuboid(xSize, ySize, zSize, 0.0F, 0.0F, 0.0F, new Location(world, x, y, z));
            return LuaValue.valueOf(this.registerZone(new ScriptZone(shape)));
         }
      }));
      zones.set("watch", this.method(zones, args -> {
         int handle = args.checkint(1);
         LuaFunction onEnter = args.isfunction(2) ? args.checkfunction(2) : null;
         LuaFunction onLeave = args.isfunction(3) ? args.checkfunction(3) : null;
         ScriptZone zone = this.zoneWatches.get(handle);
         if (zone == null) {
            return LuaValue.NIL;
         } else {
            if (onEnter != null) {
               zone.setOnEnter((player, z) -> this.handleEvent(ScriptHook.ON_ZONE_ENTER, null, player, player));
            }

            if (onLeave != null) {
               zone.setOnLeave((player, z) -> this.handleEvent(ScriptHook.ON_ZONE_LEAVE, null, player, player));
            }

            return LuaValue.TRUE;
         }
      }));
      zones.set("unwatch", this.method(zones, args -> {
         int handle = args.checkint(1);
         ScriptZone zone = this.zoneWatches.remove(handle);
         if (zone != null) {
            zone.shutdown();
         }

         this.updateTickRegistration();
         return LuaValue.NIL;
      }));
      return zones;
   }

   private LuaValue createEventTable() {
      if (this.currentEvent == null && this.currentEventActor == null) {
         return LuaValue.NIL;
      } else {
         LuaTable eventTable = new LuaTable();
         if (this.currentEvent instanceof Cancellable cancellable) {
            eventTable.set("is_cancelled", LuaValue.valueOf(cancellable.isCancelled()));
            eventTable.set("cancel", new VarArgFunction() {
               @Override
               public Varargs invoke(Varargs args) {
                  cancellable.setCancelled(true);
                  return LuaValue.NIL;
               }
            });
            eventTable.set("uncancel", new VarArgFunction() {
               @Override
               public Varargs invoke(Varargs args) {
                  cancellable.setCancelled(false);
                  return LuaValue.NIL;
               }
            });
         } else {
            eventTable.set("is_cancelled", LuaValue.FALSE);
         }

         if (this.currentEventActor != null) {
            eventTable.set("player", LuaLivingEntityTable.build(this.currentEventActor));
         }

         return eventTable;
      }
   }

   private int registerZone(ScriptZone zone) {
      int handle = this.nextZoneHandle++;
      this.zoneWatches.put(handle, zone);
      this.updateTickRegistration();
      return handle;
   }

   private void tickZoneWatches() {
      if (!this.zoneWatches.isEmpty()) {
         Location loc = this.entity.getLocation();
         if (loc != null && loc.getWorld() != null) {
            Collection<Player> nearby = loc.getWorld().getPlayers();

            for (ScriptZone zone : this.zoneWatches.values()) {
               zone.tick(nearby);
            }
         }
      }
   }

   private VarArgFunction method(final LuaTable owner, final LuaTableSupport.LuaCallback callback) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            return callback.invoke(LuaTableSupport.stripMethodSelf(args, owner));
         }
      };
   }

   private void logLuaError(String context, Exception exception) {
      if (!this.entity.handleScriptError(context, exception)) {
         String fileName = this.definition.getFileName();
         String rawMessage = exception.getMessage() != null ? exception.getMessage() : exception.toString();
         String lineInfo = "";
         String errorDetail = rawMessage;
         if (rawMessage.contains(fileName)) {
            int fileStart = rawMessage.indexOf(fileName);
            String afterFile = rawMessage.substring(fileStart + fileName.length());
            if (afterFile.startsWith(":")) {
               afterFile = afterFile.substring(1);
               int spaceIndex = afterFile.indexOf(32);
               if (spaceIndex > 0) {
                  lineInfo = afterFile.substring(0, spaceIndex);
                  errorDetail = afterFile.substring(spaceIndex + 1).trim();
               }
            }
         }

         StringBuilder message = new StringBuilder();
         message.append("[Lua] Error in '").append(fileName).append("'");
         if (!lineInfo.isEmpty()) {
            message.append(" at line ").append(lineInfo);
         }

         message.append(" during '").append(context).append("':");
         Logger.warn(message.toString());
         if (errorDetail.contains("attempt to call nil")) {
            Logger.warn("[Lua]   -> You tried to call a method or function that doesn't exist.");
            Logger.warn("[Lua]   -> Check the method name for typos, or make sure you're using ':' (colon) for method calls, not '.' (dot).");
         } else if (errorDetail.contains("index expected, got nil")) {
            Logger.warn("[Lua]   -> You tried to access a field on something that is nil (doesn't exist).");
            Logger.warn("[Lua]   -> A variable or table field you're reading hasn't been set yet. Check that earlier code initialized it.");
         } else if (errorDetail.contains("attempt to index")) {
            Logger.warn("[Lua]   -> You tried to access a property on a nil or invalid value.");
            Logger.warn("[Lua]   -> Check that the object exists before accessing its fields.");
         } else if (errorDetail.contains("bad argument")) {
            Logger.warn("[Lua]   -> A function received the wrong type of argument (e.g. string instead of number).");
            Logger.warn("[Lua]   -> Detail: " + errorDetail);
         } else {
            Logger.warn("[Lua]   -> " + errorDetail);
         }

         Logger.warn("[Lua]   -> Script has been disabled for this entity to prevent further errors.");
      }
   }

   @Generated
   public ScriptDefinition getDefinition() {
      return this.definition;
   }

   @Generated
   public ScriptableEntity getEntity() {
      return this.entity;
   }

   private record HookInvocation(ScriptInstance.HookInvocationKind kind, LuaValue value) {
      private HookInvocation(ScriptInstance.HookInvocationKind kind, LuaValue value) {
         Objects.requireNonNull(kind, "kind");
         if (kind == ScriptInstance.HookInvocationKind.HANDLED != (value != null)) {
            throw new IllegalArgumentException("Hook invocation result does not match " + kind);
         } else {
            this.kind = kind;
            this.value = value;
         }
      }

      private static ScriptInstance.HookInvocation unhandled() {
         return new ScriptInstance.HookInvocation(ScriptInstance.HookInvocationKind.UNHANDLED, null);
      }

      private static ScriptInstance.HookInvocation handled(LuaValue value) {
         return new ScriptInstance.HookInvocation(ScriptInstance.HookInvocationKind.HANDLED, Objects.requireNonNull(value, "value"));
      }

      private static ScriptInstance.HookInvocation failed() {
         return new ScriptInstance.HookInvocation(ScriptInstance.HookInvocationKind.FAILED, null);
      }
   }

   private static enum HookInvocationKind {
      UNHANDLED,
      HANDLED,
      FAILED;
   }

   @FunctionalInterface
   private interface OwnedTask {
      void cancel();
   }
}
