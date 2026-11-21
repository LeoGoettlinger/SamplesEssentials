package eu.kennytv.maintenance.core.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.event.ClickEvent.Payload;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ComponentUtil {
   private static final MethodHandle LEGACY_CLICK_EVENT = legacyClickEvent();

   public static ClickEvent clickEvent(Action action, String value) {
      if (LEGACY_CLICK_EVENT != null) {
         try {
            return (ClickEvent)LEGACY_CLICK_EVENT.invoke((Action)action, (String)value);
         } catch (Throwable var3) {
            throw new RuntimeException(var3);
         }
      } else {
         return ClickEvent.clickEvent(action, Payload.string(value));
      }
   }

   @Nullable
   private static MethodHandle legacyClickEvent() {
      try {
         return MethodHandles.publicLookup().unreflect(ClickEvent.class.getDeclaredMethod("clickEvent", Action.class, String.class));
      } catch (IllegalAccessException | NoSuchMethodException var1) {
         return null;
      }
   }
}
