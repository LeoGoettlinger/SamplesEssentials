package com.magmaguy.elitemobs.magmacore.location;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class LuaLocations {
   private LuaLocations() {
   }

   public static Location tableToLocation(LuaValue value) {
      if (value != null && value.istable()) {
         LuaTable table = value.checktable();
         LuaValue worldValue = table.get("world");
         World world = resolveWorld(worldValue);
         if (world == null) {
            return null;
         } else {
            double x = table.get("x").optdouble(0.0);
            double y = table.get("y").optdouble(0.0);
            double z = table.get("z").optdouble(0.0);
            float yaw = (float)table.get("yaw").optdouble(0.0);
            float pitch = (float)table.get("pitch").optdouble(0.0);
            return new Location(world, x, y, z, yaw, pitch);
         }
      } else {
         return null;
      }
   }

   private static World resolveWorld(LuaValue worldValue) {
      if (worldValue == null || worldValue.isnil()) {
         return null;
      } else if (worldValue.isstring()) {
         return Bukkit.getWorld(worldValue.tojstring());
      } else {
         if (worldValue.istable()) {
            LuaValue nameValue = worldValue.get("name");
            if (nameValue.isstring()) {
               return Bukkit.getWorld(nameValue.tojstring());
            }
         }

         return null;
      }
   }
}
