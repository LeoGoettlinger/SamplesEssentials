package com.artillexstudios.axtrade.libs.axapi.nms;

import com.artillexstudios.axtrade.libs.axapi.AxPlugin;
import com.artillexstudios.axtrade.libs.axapi.utils.Version;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;

public class NMSHandlers {
   private static NMSHandler nmsHandler;

   private static boolean init(AxPlugin plugin) {
      Version version = Version.getServerVersion();
      if (version == Version.UNKNOWN) {
         LogUtils.warn(
            "Could not load plugin {} due to version mismatch! Found protocol version {} which is unsupported!", plugin.getName(), Version.getProtocolVersion()
         );
         return false;
      } else if (nmsHandler != null) {
         LogUtils.warn("NMS support has already been enabled!");
         return false;
      } else {
         try {
            nmsHandler = (NMSHandler)Class.forName("com.artillexstudios.axtrade.libs.axapi.nms.%s.NMSHandler".formatted(version.getNMSVersion()))
               .getConstructor()
               .newInstance();
            return true;
         } catch (Exception var3) {
            LogUtils.warn("Could not enable NMSHandler due to an internal exception while loading NMSHandler!", var3);
            return false;
         }
      }
   }

   public static NMSHandler getNmsHandler() {
      return nmsHandler;
   }

   public static class American {
      public static boolean initialize(AxPlugin plugin) {
         return NMSHandlers.init(plugin);
      }
   }

   public static class British {
      public static boolean initialise(AxPlugin plugin) {
         return NMSHandlers.init(plugin);
      }
   }
}
