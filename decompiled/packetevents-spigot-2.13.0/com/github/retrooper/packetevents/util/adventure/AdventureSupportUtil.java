package com.github.retrooper.packetevents.util.adventure;

import com.github.retrooper.packetevents.util.reflection.Reflection;
import io.github.retrooper.packetevents.adventure.serializer.gson.BackwardCompatUtil;
import java.lang.reflect.Method;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Internal
public class AdventureSupportUtil {
   private static final Method CLICK_EVENT_VALUE = Reflection.getMethod(ClickEvent.class, "value", 0);

   private AdventureSupportUtil() {
   }

   public static String getStringValue(ClickEvent event) {
      if (BackwardCompatUtil.IS_4_22_0_OR_NEWER) {
         return ((ClickEvent.Payload.Text)event.payload()).value();
      } else {
         try {
            return (String)CLICK_EVENT_VALUE.invoke(event);
         } catch (ReflectiveOperationException var2) {
            throw new RuntimeException(var2);
         }
      }
   }

   public static int getIntValue(ClickEvent event) {
      if (BackwardCompatUtil.IS_4_22_0_OR_NEWER) {
         return ((ClickEvent.Payload.Int)event.payload()).integer();
      } else {
         try {
            return Integer.parseInt((String)CLICK_EVENT_VALUE.invoke(event));
         } catch (ReflectiveOperationException var2) {
            throw new RuntimeException(var2);
         }
      }
   }
}
