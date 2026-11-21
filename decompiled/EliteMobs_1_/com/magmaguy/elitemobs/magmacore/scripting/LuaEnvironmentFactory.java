package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.magmacore.location.LocationQueryRegistry;
import com.magmaguy.elitemobs.magmacore.location.LuaLocations;
import com.magmaguy.elitemobs.magmacore.location.api.LocationOwnership;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Globals;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.jse.JsePlatform;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Location;

public final class LuaEnvironmentFactory {
   private LuaEnvironmentFactory() {
   }

   public static Globals createGlobals() {
      Globals globals = JsePlatform.standardGlobals();
      globals.load(LuaExecutionBudget.debugLibrary());
      globals.set("debug", LuaValue.NIL);
      globals.set("dofile", LuaValue.NIL);
      globals.set("io", LuaValue.NIL);
      globals.set("load", LuaValue.NIL);
      globals.set("loadfile", LuaValue.NIL);
      globals.set("luajava", LuaValue.NIL);
      globals.set("module", LuaValue.NIL);
      globals.set("os", LuaValue.NIL);
      globals.set("package", LuaValue.NIL);
      globals.set("require", LuaValue.NIL);
      LuaTable em = new LuaTable();
      em.set("create_location", new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            LuaTable table = LuaEnvironmentFactory.createLocationTable(args.checkdouble(1), args.checkdouble(2), args.checkdouble(3));
            if (args.narg() >= 4 && args.arg(4).isstring()) {
               table.set("world", args.arg(4));
            }

            if (args.narg() >= 5 && args.arg(5).isnumber()) {
               table.set("yaw", args.arg(5));
            }

            if (args.narg() >= 6 && args.arg(6).isnumber()) {
               table.set("pitch", args.arg(6));
            }

            return table;
         }
      });
      em.set("create_vector", new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            LuaTable table = new LuaTable();
            table.set("x", args.checkdouble(1));
            table.set("y", args.checkdouble(2));
            table.set("z", args.checkdouble(3));
            return table;
         }
      });
      LuaTable zone = new LuaTable();
      zone.set("create_sphere_zone", zoneBuilder("sphere", "set_center", null, "radius"));
      zone.set("create_dome_zone", zoneBuilder("dome", "set_center", null, "radius"));
      zone.set("create_cylinder_zone", zoneBuilder("cylinder", "set_center", null, "radius", "height"));
      zone.set("create_cuboid_zone", zoneBuilder("cuboid", "set_center", null, "x", "y", "z"));
      zone.set("create_cone_zone", zoneBuilder("cone", "set_origin", "set_destination", "length", "radius"));
      zone.set("create_static_ray_zone", zoneBuilder("static_ray", "set_origin", "set_destination", "length", "thickness"));
      zone.set("create_rotating_ray_zone", zoneBuilder("rotating_ray", "set_origin", "set_destination", "length", "point_radius", "animation_duration"));
      zone.set("create_translating_ray_zone", zoneBuilder("translating_ray", "set_origin", "set_destination", "length", "point_radius", "animation_duration"));
      em.set("zone", zone);
      LuaTable location = new LuaTable();
      location.set("is_in_dungeon", locationPredicate(LocationQueryRegistry::isInAnyDungeon));
      location.set("is_protected", locationPredicate(LocationQueryRegistry::isInAnyProtectedRegion));
      location.set("owned_by", new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            Location loc = LuaLocations.tableToLocation(args.arg1());
            String namespace = args.arg(2).optjstring(null);
            return loc != null && namespace != null ? LuaValue.valueOf(LocationOwnership.ownedBy(loc, namespace)) : LuaValue.FALSE;
         }
      });
      location.set("owners", new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            Location loc = LuaLocations.tableToLocation(args.arg1());
            return loc == null ? new LuaTable() : LuaEnvironmentFactory.toLuaArray(LocationOwnership.ownersAt(loc));
         }
      });
      location.set("kinds_at", new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            Location loc = LuaLocations.tableToLocation(args.arg1());
            return loc == null ? new LuaTable() : LuaEnvironmentFactory.toLuaArray(LocationOwnership.kindsAt(loc));
         }
      });
      location.set("has_kind", new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            Location loc = LuaLocations.tableToLocation(args.arg1());
            String kind = args.arg(2).optjstring(null);
            return loc != null && kind != null ? LuaValue.valueOf(LocationOwnership.hasKind(loc, kind)) : LuaValue.FALSE;
         }
      });
      em.set("location", location);
      globals.set("em", em);
      LuaMindLibrary.install(globals);
      return globals;
   }

   private static LuaTable toLuaArray(Set<String> values) {
      LuaTable table = new LuaTable();
      int index = 1;

      for (String value : values) {
         table.set(index++, LuaValue.valueOf(value));
      }

      return table;
   }

   private static VarArgFunction locationPredicate(final Predicate<Location> predicate) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            Location loc = LuaLocations.tableToLocation(args.arg1());
            return loc == null ? LuaValue.FALSE : LuaValue.valueOf(predicate.test(loc));
         }
      };
   }

   private static LuaValue zoneBuilder(final String kind, final String originMethod, final String destinationMethod, final String... numericFields) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            LuaTable table = new LuaTable();
            table.set("kind", kind);

            for (int index = 0; index < numericFields.length; index++) {
               table.set(numericFields[index], args.checkdouble(index + 1));
            }

            table.set(originMethod, LuaEnvironmentFactory.tableMethod(table, methodArgs -> {
               table.set("origin", methodArgs.arg1());
               return table;
            }));
            if (destinationMethod != null) {
               table.set(destinationMethod, LuaEnvironmentFactory.tableMethod(table, methodArgs -> {
                  table.set("destination", methodArgs.arg1());
                  return table;
               }));
            }

            return table;
         }
      };
   }

   private static LuaTable createLocationTable(double x, double y, double z) {
      LuaTable table = new LuaTable();
      table.set("x", x);
      table.set("y", y);
      table.set("z", z);
      table.set("add", tableMethod(table, args -> {
         table.set("x", LuaValue.valueOf(table.get("x").optdouble(0.0) + args.optdouble(1, 0.0)));
         table.set("y", LuaValue.valueOf(table.get("y").optdouble(0.0) + args.optdouble(2, 0.0)));
         table.set("z", LuaValue.valueOf(table.get("z").optdouble(0.0) + args.optdouble(3, 0.0)));
         return table;
      }));
      return table;
   }

   private static LuaValue tableMethod(final LuaTable owner, final LuaEnvironmentFactory.LuaCallback callback) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            return callback.invoke(LuaEnvironmentFactory.stripMethodSelf(args, owner));
         }
      };
   }

   private static Varargs stripMethodSelf(Varargs args, LuaTable owner) {
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
