package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.magmacore.location.LocationQueryRegistry;
import com.magmaguy.elitemobs.magmacore.projectiles.MagicProjectileEngine;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.util.function.BooleanSupplier;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public final class ScriptProjectiles implements AutoCloseable {
   private final MagicProjectileEngine<ScriptProjectiles.Flight> engine;

   public ScriptProjectiles(Plugin plugin) {
      this.engine = new MagicProjectileEngine<ScriptProjectiles.Flight>(
         plugin,
         (flight, target, at, incoming) -> {
            if (flight.active()) {
               flight.instance
                  .invokeOwnedCallback(
                     "projectile impact",
                     flight.impact,
                     (LuaValue)(target == null ? LuaValue.NIL : LuaLivingEntityTable.build(target)),
                     LuaTableSupport.locationToTable(at)
                  );
            }
         }
      ) {
         protected void renderStraightFlight(ScriptProjectiles.Flight flight, Location location) {
            if (flight.visual != null) {
               flight.instance.invokeOwnedCallback("projectile visual", flight.visual, LuaTableSupport.locationToTable(location));
            }
         }

         protected boolean isAbsorbed(ScriptProjectiles.Flight flight, Location from, Location to) {
            if (!flight.protectedPath) {
               return false;
            } else {
               double distance = from.distance(to);
               int steps = Math.max(1, (int)Math.ceil(distance * 4.0));
               Vector step = to.toVector().subtract(from.toVector()).multiply(1.0 / steps);

               for (int i = 0; i <= steps; i++) {
                  Location at = from.clone().add(step.clone().multiply(i));
                  if (!at.getWorld().isChunkLoaded(at.getBlockX() >> 4, at.getBlockZ() >> 4) || LocationQueryRegistry.isInAnyProtectedRegion(at)) {
                     return true;
                  }
               }

               return false;
            }
         }
      };
      this.engine.start();
   }

   public boolean launch(
      ScriptInstance instance,
      Player owner,
      BooleanSupplier active,
      Location at,
      Vector direction,
      double speed,
      double range,
      int ticks,
      LuaFunction impact,
      LuaFunction visual,
      boolean protectedPath
   ) {
      if (Double.isFinite(speed) && !(speed <= 0.0) && Double.isFinite(range) && !(range <= 0.0) && ticks >= 1) {
         ScriptProjectiles.Flight flight = new ScriptProjectiles.Flight(
            owner, new ScriptProjectiles.Traits(speed, range, ticks), instance, active, impact, visual, protectedPath
         );
         if (!this.engine.launchStraight(flight, at, direction)) {
            return false;
         } else {
            instance.ownCleanup(() -> this.engine.cancel(flight));
            return true;
         }
      } else {
         throw new IllegalArgumentException("Invalid projectile flight parameters");
      }
   }

   @Override
   public void close() {
      this.engine.close();
   }

   private record Flight(
      Player owner,
      MagicProjectileEngine.Traits traits,
      ScriptInstance instance,
      BooleanSupplier alive,
      LuaFunction impact,
      LuaFunction visual,
      boolean protectedPath
   ) implements MagicProjectileEngine.Source {
      @Override
      public boolean active() {
         return this.alive.getAsBoolean();
      }
   }

   private record Traits(double projectileSpeed, double range, int travelTicks) implements MagicProjectileEngine.Traits {
      @Override
      public int missileCount() {
         return 1;
      }

      @Override
      public double spreadDegrees() {
         return 0.0;
      }
   }
}
