package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.magmacore.ai.MindActionRequest;
import com.magmaguy.elitemobs.magmacore.ai.MindContext;
import com.magmaguy.elitemobs.magmacore.ai.MindMemoryView;
import com.magmaguy.elitemobs.magmacore.ai.MindNavigationProgress;
import com.magmaguy.elitemobs.magmacore.ai.MindNavigationStatus;
import com.magmaguy.elitemobs.magmacore.ai.MindPosition;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

final class LuaMindContextTable {
   private LuaMindContextTable() {
   }

   static LuaTable build(MindContext context, Map<String, LuaMindProgramCompiler.MemoryBinding<?>> memories) {
      LuaTable table = new LuaTable();
      table.set("tick", LuaValue.valueOf((double)context.gameTick()));
      table.set("generation", LuaValue.valueOf((double)context.generation()));
      snapshotField(table, "entity", () -> LuaLivingEntityTable.build(context.entity()));
      snapshotField(table, "memory", () -> memoryTable(context.memory(), memories));
      snapshotField(table, "perception", () -> perceptionTable(context));
      snapshotField(table, "actuator", () -> actuatorTable(context));
      snapshotField(table, "actions", () -> actionsTable(context));
      return table;
   }

   private static void snapshotField(LuaTable table, String key, Supplier<? extends LuaValue> factory) {
      LuaValue[] snapshot = new LuaValue[1];
      LuaTableSupport.lazyField(table, key, () -> {
         LuaValue value = snapshot[0];
         if (value == null) {
            value = factory.get();
            snapshot[0] = value == null ? LuaValue.NIL : value;
         }

         return snapshot[0];
      });
   }

   private static LuaTable perceptionTable(MindContext context) {
      LuaTable table = new LuaTable();
      table.set(
         "current_target",
         LuaTableSupport.tableMethod(table, args -> context.perception().currentTarget().map(LuaLivingEntityTable::build).orElse(LuaValue.NIL))
      );
      table.set(
         "nearest_player",
         LuaTableSupport.tableMethod(
            table, args -> context.perception().nearestPlayer(args.checkdouble(1)).map(LuaLivingEntityTable::build).orElse(LuaValue.NIL)
         )
      );
      table.set("can_see", LuaTableSupport.tableMethod(table, args -> {
         LivingEntity target = resolveLivingEntity(args.arg1());
         return LuaValue.valueOf(target != null && context.perception().lineOfSight(target));
      }));
      table.set("navigation", LuaTableSupport.tableMethod(table, args -> navigationTable(context.perception().navigation())));
      return table;
   }

   private static LuaTable navigationTable(MindNavigationStatus status) {
      LuaTable table = new LuaTable();
      table.set("destination_active", LuaValue.valueOf(status.destinationActive()));
      table.set("navigation_in_progress", LuaValue.valueOf(status.navigationInProgress()));
      table.set("consecutive_stuck_ticks", status.consecutiveStuckTicks());
      table.set("destination", status.destination().map(LuaMindContextTable::positionTable).orElse(LuaValue.NIL));
      table.set("last_progress", status.lastProgress().map(LuaMindContextTable::progressTable).orElse(LuaValue.NIL));
      return table;
   }

   private static LuaTable progressTable(MindNavigationProgress progress) {
      LuaTable table = new LuaTable();
      table.set("tick", LuaValue.valueOf((double)progress.gameTick()));
      table.set("position", positionTable(progress.position()));
      return table;
   }

   private static LuaTable positionTable(MindPosition position) {
      LuaTable table = new LuaTable();
      table.set("world", position.world());
      table.set("x", position.x());
      table.set("y", position.y());
      table.set("z", position.z());
      return table;
   }

   private static LuaTable actionsTable(MindContext context) {
      LuaTable table = new LuaTable();
      table.set("request", LuaTableSupport.tableMethod(table, args -> {
         String identifier = args.checkjstring(1);
         Map<String, Object> payload = args.narg() >= 2 && !args.arg(2).isnil() ? actionPayload(args.checktable(2)) : Map.of();
         return LuaValue.valueOf(context.actions().request(new MindActionRequest(identifier, payload)).name().toLowerCase(Locale.ROOT));
      }));
      return table;
   }

   private static Map<String, Object> actionPayload(LuaTable table) {
      Map<String, Object> payload = new LinkedHashMap<>();
      LuaValue key = LuaValue.NIL;

      while (true) {
         Varargs next = table.next(key);
         key = next.arg1();
         if (key.isnil()) {
            return payload;
         }

         payload.put(key.checkjstring(), actionValue(next.arg(2)));
      }
   }

   private static Object actionValue(LuaValue value) {
      if (value.isboolean()) {
         return value.checkboolean();
      } else if (value.type() == 4) {
         return value.checkjstring();
      } else if (value.type() == 3) {
         double number = value.checkdouble();
         if (!Double.isFinite(number)) {
            throw new IllegalArgumentException("Mind action numbers must be finite");
         } else {
            long integral = (long)number;
            return number >= -9.223372E18F && number < 9.223372E18F && number == integral ? integral : number;
         }
      } else if (!value.istable()) {
         throw new IllegalArgumentException("Unsupported Lua Mind action payload value");
      } else {
         LuaTable table = value.checktable();
         LuaValue uuid = table.get("uuid");
         if (uuid.isstring()) {
            return UUID.fromString(uuid.checkjstring());
         } else {
            LuaValue world = table.get("world");
            LuaValue x = table.get("x");
            LuaValue y = table.get("y");
            LuaValue z = table.get("z");
            if (world.isstring() && x.isnumber() && y.isnumber() && z.isnumber()) {
               return new MindPosition(world.checkjstring(), x.checkdouble(), y.checkdouble(), z.checkdouble());
            } else {
               throw new IllegalArgumentException("Mind action tables must contain either uuid or world/x/y/z fields");
            }
         }
      }
   }

   private static LuaTable memoryTable(MindMemoryView memory, Map<String, LuaMindProgramCompiler.MemoryBinding<?>> memories) {
      LuaTable table = new LuaTable();
      table.set("get", LuaTableSupport.tableMethod(table, args -> {
         LuaMindProgramCompiler.MemoryBinding<?> binding = requireBinding(memories, args.checkjstring(1));
         return encode(memory, binding);
      }));
      table.set("set", LuaTableSupport.tableMethod(table, args -> {
         LuaMindProgramCompiler.MemoryBinding<?> binding = requireBinding(memories, args.checkjstring(1));
         set(memory, binding, args.arg(2), args.narg() >= 3 ? args.checklong(3) : null);
         return LuaValue.NIL;
      }));
      table.set("forget", LuaTableSupport.tableMethod(table, args -> {
         memory.forget(requireBinding(memories, args.checkjstring(1)).key);
         return LuaValue.NIL;
      }));
      table.set("contains", LuaTableSupport.tableMethod(table, args -> LuaValue.valueOf(memory.contains(requireBinding(memories, args.checkjstring(1)).key))));
      return table;
   }

   private static LuaTable actuatorTable(MindContext context) {
      LuaTable table = new LuaTable();
      table.set("move_to", LuaTableSupport.tableMethod(table, args -> {
         Location location = LuaTableSupport.tableToLocation(args.checktable(1), context.entity().getWorld());
         return LuaValue.valueOf(context.actuator().moveTo(location, args.optdouble(2, 1.0)));
      }));
      table.set("stop_moving", LuaTableSupport.tableMethod(table, args -> {
         context.actuator().stopMoving();
         return LuaValue.NIL;
      }));
      table.set("look_at", LuaTableSupport.tableMethod(table, args -> {
         context.actuator().lookAt(LuaTableSupport.tableToLocation(args.checktable(1), context.entity().getWorld()));
         return LuaValue.NIL;
      }));
      table.set("jump", LuaTableSupport.tableMethod(table, args -> {
         context.actuator().jump();
         return LuaValue.NIL;
      }));
      table.set("set_target", LuaTableSupport.tableMethod(table, args -> {
         LivingEntity target = resolveLivingEntity(args.arg1());
         if (target == null) {
            return LuaValue.FALSE;
         } else {
            context.actuator().setTarget(target);
            return LuaValue.TRUE;
         }
      }));
      table.set("clear_target", LuaTableSupport.tableMethod(table, args -> {
         context.actuator().clearTarget();
         return LuaValue.NIL;
      }));
      table.set("attack", LuaTableSupport.tableMethod(table, args -> {
         LivingEntity target = resolveLivingEntity(args.arg1());
         if (target == null) {
            return LuaValue.FALSE;
         } else {
            context.actuator().attack(target);
            return LuaValue.TRUE;
         }
      }));
      table.set("stop_all", LuaTableSupport.tableMethod(table, args -> {
         context.actuator().stopAll();
         return LuaValue.NIL;
      }));
      return table;
   }

   private static <T> LuaValue encode(MindMemoryView memory, LuaMindProgramCompiler.MemoryBinding<T> binding) {
      Optional<T> value = memory.get(binding.key);
      return value.map(binding.encoder::encode).orElse(LuaValue.NIL);
   }

   private static <T> void set(MindMemoryView memory, LuaMindProgramCompiler.MemoryBinding<T> binding, LuaValue value, Long ttlTicks) {
      T decoded = binding.decoder.decode(value);
      if (ttlTicks == null) {
         memory.set(binding.key, decoded);
      } else {
         memory.set(binding.key, decoded, ttlTicks);
      }
   }

   private static LuaMindProgramCompiler.MemoryBinding<?> requireBinding(Map<String, LuaMindProgramCompiler.MemoryBinding<?>> memories, String identifier) {
      LuaMindProgramCompiler.MemoryBinding<?> binding = memories.get(identifier);
      if (binding == null) {
         throw new IllegalArgumentException("Undeclared mind memory '" + identifier + "'");
      } else {
         return binding;
      }
   }

   private static LivingEntity resolveLivingEntity(LuaValue value) {
      String uuidString;
      if (value.isstring()) {
         uuidString = value.checkjstring();
      } else {
         if (!value.istable()) {
            return null;
         }

         uuidString = value.checktable().get("uuid").checkjstring();
      }

      Entity entity;
      try {
         entity = Bukkit.getEntity(UUID.fromString(uuidString));
      } catch (IllegalArgumentException var4) {
         return null;
      }

      return entity instanceof LivingEntity living ? living : null;
   }
}
