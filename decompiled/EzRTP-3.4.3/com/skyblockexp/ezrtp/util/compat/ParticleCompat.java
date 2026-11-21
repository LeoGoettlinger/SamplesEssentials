package com.skyblockexp.ezrtp.util.compat;

import java.lang.reflect.Method;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public final class ParticleCompat {
   private ParticleCompat() {
   }

   public static void spawnParticle(
      World world, Particle particle, Location loc, int count, double offsetX, double offsetY, double offsetZ, double extra, Object data, boolean force
   ) {
      if (world != null && particle != null && loc != null) {
         try {
            Method m = world.getClass()
               .getMethod(
                  "spawnParticle",
                  Particle.class,
                  Location.class,
                  int.class,
                  double.class,
                  double.class,
                  double.class,
                  double.class,
                  Object.class,
                  boolean.class
               );
            m.invoke(world, particle, loc, count, offsetX, offsetY, offsetZ, extra, data, force);
         } catch (LinkageError | RuntimeException | ReflectiveOperationException var17) {
            try {
               Method m2 = world.getClass()
                  .getMethod("spawnParticle", Particle.class, Location.class, int.class, double.class, double.class, double.class, double.class);
               m2.invoke(world, particle, loc, count, offsetX, offsetY, offsetZ, extra);
            } catch (LinkageError | RuntimeException | ReflectiveOperationException var16) {
               try {
                  Method m3 = world.getClass().getMethod("spawnParticle", Location.class, Particle.class, int.class, double.class, double.class, double.class);
                  m3.invoke(world, loc, particle, count, offsetX, offsetY, offsetZ);
               } catch (LinkageError | RuntimeException | ReflectiveOperationException var15) {
               }
            }
         }
      }
   }
}
