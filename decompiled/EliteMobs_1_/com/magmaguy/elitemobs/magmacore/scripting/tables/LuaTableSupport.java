package com.magmaguy.elitemobs.magmacore.scripting.tables;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class LuaTableSupport {
   private LuaTableSupport() {
   }

   public static void lazyField(LuaTable table, String key, Supplier<LuaValue> supplier) {
      LuaTableSupport.LazyIndex index = LuaTableSupport.LazyIndex.attach(table);
      index.put(key, supplier);
   }

   public static LuaTable locationToTable(Location location) {
      LuaTable table = new LuaTable();
      table.set("x", location.getX());
      table.set("y", location.getY());
      table.set("z", location.getZ());
      table.set("yaw", location.getYaw());
      table.set("pitch", location.getPitch());
      if (location.getWorld() != null) {
         table.set("world", location.getWorld().getName());
      }

      return table;
   }

   public static LuaTable vectorToTable(Vector vector) {
      LuaTable table = new LuaTable();
      table.set("x", vector.getX());
      table.set("y", vector.getY());
      table.set("z", vector.getZ());
      return table;
   }

   public static Location tableToLocation(LuaTable table, World defaultWorld) {
      double x = table.get("x").checkdouble();
      double y = table.get("y").checkdouble();
      double z = table.get("z").checkdouble();
      float yaw = (float)table.get("yaw").optdouble(0.0);
      float pitch = (float)table.get("pitch").optdouble(0.0);
      World world = defaultWorld;
      LuaValue worldValue = table.get("world");
      if (worldValue.isstring()) {
         World namedWorld = Bukkit.getWorld(worldValue.tojstring());
         if (namedWorld != null) {
            world = namedWorld;
         }
      }

      return new Location(world, x, y, z, yaw, pitch);
   }

   public static Vector tableToVector(LuaTable table) {
      return new Vector(table.get("x").checkdouble(), table.get("y").checkdouble(), table.get("z").checkdouble());
   }

   public static LuaValue tableMethod(final LuaTable owner, final LuaTableSupport.LuaCallback callback) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            return callback.invoke(LuaTableSupport.stripMethodSelf(args, owner));
         }
      };
   }

   public static Varargs stripMethodSelf(Varargs args, LuaTable owner) {
      if (args.narg() != 0 && args.arg1().raweq(owner)) {
         LuaValue[] stripped = new LuaValue[Math.max(0, args.narg() - 1)];

         for (int i = 2; i <= args.narg(); i++) {
            stripped[i - 2] = args.arg(i);
         }

         return LuaValue.varargsOf(stripped);
      } else {
         return args;
      }
   }

   private static final class LazyIndex extends VarArgFunction {
      private final Map<String, Supplier<LuaValue>> suppliers = new HashMap<>();

      static LuaTableSupport.LazyIndex attach(LuaTable table) {
         if (table.getmetatable() instanceof LuaTable mt) {
            if (mt.rawget("__index") instanceof LuaTableSupport.LazyIndex li) {
               return li;
            } else {
               LuaTableSupport.LazyIndex li = new LuaTableSupport.LazyIndex();
               mt.set("__index", li);
               return li;
            }
         } else {
            LuaTable mtx = new LuaTable();
            LuaTableSupport.LazyIndex li = new LuaTableSupport.LazyIndex();
            mtx.set("__index", li);
            table.setmetatable(mtx);
            return li;
         }
      }

      void put(String key, Supplier<LuaValue> supplier) {
         this.suppliers.put(key, supplier);
      }

      @Override
      public Varargs invoke(Varargs args) {
         LuaValue keyValue = args.arg(2);
         if (!keyValue.isstring()) {
            return LuaValue.NIL;
         } else {
            Supplier<LuaValue> supplier = this.suppliers.get(keyValue.tojstring());
            if (supplier == null) {
               return LuaValue.NIL;
            } else {
               try {
                  LuaValue v = supplier.get();
                  return v != null ? v : LuaValue.NIL;
               } catch (Throwable var5) {
                  return LuaValue.NIL;
               }
            }
         }
      }
   }

   @FunctionalInterface
   public interface LuaCallback {
      Varargs invoke(Varargs var1);
   }
}
