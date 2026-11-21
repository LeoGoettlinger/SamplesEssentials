package dev.cwhead.GravesX.compatibility;

import dev.cwhead.GravesX.exception.GravesXIllegalArgumentException;
import java.lang.reflect.Method;
import java.util.Locale;
import org.bukkit.Particle;

public final class CompatibilityParticleEnum {
   public static Particle valueOf(String particleName) {
      if (particleName != null && !particleName.isBlank()) {
         String normalized = particleName.toUpperCase(Locale.ROOT);

         try {
            Method method = Particle.class.getMethod("valueOf", String.class);
            return (Particle)method.invoke(null, normalized);
         } catch (NoSuchMethodException | IllegalArgumentException var3) {
            throw new GravesXIllegalArgumentException("Particle " + normalized + " does not exist on this server version.");
         } catch (Exception var4) {
            throw new GravesXIllegalArgumentException("An issue occurred while retrieving particle enum " + normalized);
         }
      } else {
         return null;
      }
   }
}
