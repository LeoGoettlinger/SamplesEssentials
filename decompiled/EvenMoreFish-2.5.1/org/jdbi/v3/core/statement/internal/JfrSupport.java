package org.jdbi.v3.core.statement.internal;

import java.lang.reflect.Method;
import org.jdbi.v3.core.statement.JdbiStatementEvent;

public final class JfrSupport {
   private static final boolean JFR_AVAILABLE = ModuleLayer.boot().findModule("jdk.jfr").isPresent();
   private static final boolean FLIGHT_RECORDER_AVAILABLE;

   private JfrSupport() {
      throw new AssertionError("JfrSupport can not be instantiated");
   }

   public static boolean isJfrAvailable() {
      return JFR_AVAILABLE;
   }

   public static boolean isFlightRecorderAvailable() {
      return FLIGHT_RECORDER_AVAILABLE;
   }

   public static OptionalEvent newStatementEvent() {
      return (OptionalEvent)(isFlightRecorderAvailable() ? JfrSupport.Holder.newEvent() : new NoStatementEvent());
   }

   static {
      boolean flightRecorderAvailable = false;
      if (isJfrAvailable()) {
         try {
            Class<?> flightRecorder = Class.forName("jdk.jfr.FlightRecorder");
            Method isAvailable = flightRecorder.getMethod("isAvailable");
            flightRecorderAvailable = (Boolean)isAvailable.invoke(null);
            if (flightRecorderAvailable) {
               Method register = flightRecorder.getMethod("register", Class.class);
               register.invoke(null, JdbiStatementEvent.class);
            }
         } catch (ReflectiveOperationException var4) {
         }
      }

      FLIGHT_RECORDER_AVAILABLE = flightRecorderAvailable;
   }

   private static final class Holder {
      public static OptionalEvent newEvent() {
         return new JdbiStatementEvent();
      }
   }
}
