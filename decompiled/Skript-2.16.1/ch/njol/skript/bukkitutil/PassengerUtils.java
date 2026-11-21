package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import java.lang.reflect.Method;
import org.bukkit.entity.Entity;

public abstract class PassengerUtils {
   private static Method getPassenger = null;
   private static Method setPassenger = null;

   private PassengerUtils() {
   }

   public static Entity[] getPassenger(Entity e) {
      if (hasMultiplePassenger()) {
         return e.getPassengers().toArray(new Entity[0]);
      } else {
         try {
            return new Entity[]{(Entity)getPassenger.invoke(e)};
         } catch (Exception var2) {
            Skript.exception(var2, "A error occured while trying to get a passenger in version lower than 1.11.2.");
            return null;
         }
      }
   }

   public static void addPassenger(Entity vehicle, Entity passenger) {
      if (vehicle != null && passenger != null) {
         if (hasMultiplePassenger()) {
            vehicle.addPassenger(passenger);
         } else {
            try {
               vehicle.eject();
               setPassenger.invoke(vehicle, passenger);
            } catch (Exception var3) {
               Skript.exception(var3, "A error occured while trying to set a passenger in version lower than 1.11.2.");
            }
         }
      }
   }

   public static void removePassenger(Entity vehicle, Entity passenger) {
      if (vehicle != null && passenger != null) {
         if (hasMultiplePassenger()) {
            vehicle.removePassenger(passenger);
         } else {
            vehicle.eject();
         }
      }
   }

   public static boolean hasMultiplePassenger() {
      return setPassenger == null;
   }

   static {
      if (!Skript.methodExists(Entity.class, "getPassengers")) {
         try {
            getPassenger = Entity.class.getDeclaredMethod("getPassenger");
            setPassenger = Entity.class.getDeclaredMethod("setPassenger", Entity.class);
         } catch (NoSuchMethodException var1) {
            Skript.outdatedError(var1);
         } catch (Exception var2) {
            Skript.exception(var2);
         }
      }
   }
}
