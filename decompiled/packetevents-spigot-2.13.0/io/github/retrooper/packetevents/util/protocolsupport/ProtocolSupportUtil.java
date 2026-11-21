package io.github.retrooper.packetevents.util.protocolsupport;

import com.github.retrooper.packetevents.PacketEvents;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Deprecated
public final class ProtocolSupportUtil {
   private static ProtocolSupportState available = ProtocolSupportState.UNKNOWN;
   @Nullable
   private static Method getProtocolVersionByAddress;
   @Nullable
   private static Method getProtocolVersionByPlayer;
   @Nullable
   private static Method getId;

   private ProtocolSupportUtil() {
   }

   public static boolean isAvailable() {
      if (available != ProtocolSupportState.UNKNOWN) {
         return available == ProtocolSupportState.ENABLED;
      } else {
         try {
            ClassLoader classLoader = PacketEvents.getAPI().getPlugin().getClass().getClassLoader();
            Class<?> apiClass = classLoader.loadClass("protocolsupport.api.ProtocolSupportAPI");
            getProtocolVersionByAddress = apiClass.getMethod("getProtocolVersion", SocketAddress.class);
            getProtocolVersionByPlayer = apiClass.getMethod("getProtocolVersion", Player.class);
            Class<?> versionClass = classLoader.loadClass("protocolsupport.api.ProtocolVersion");
            getId = versionClass.getMethod("getId");
            available = ProtocolSupportState.ENABLED;
            return true;
         } catch (Exception var3) {
            available = ProtocolSupportState.DISABLED;
            return false;
         }
      }
   }

   @Deprecated
   @Internal
   public static void checkIfProtocolSupportIsPresent() {
      isAvailable();
   }

   public static int getProtocolVersion(SocketAddress address) {
      if (getId != null && getProtocolVersionByAddress != null) {
         try {
            return (Integer)getId.invoke(getProtocolVersionByAddress.invoke(null, address));
         } catch (ReflectiveOperationException var2) {
            throw new IllegalStateException("Error while trying to access ProtocolSupport", var2);
         }
      } else {
         throw new IllegalStateException("ProtocolSupport is not loaded");
      }
   }

   public static int getProtocolVersion(Player player) {
      if (getId != null && getProtocolVersionByPlayer != null) {
         try {
            return (Integer)getId.invoke(getProtocolVersionByPlayer.invoke(null, player));
         } catch (ReflectiveOperationException var2) {
            throw new IllegalStateException("Error while trying to access ProtocolSupport", var2);
         }
      } else {
         throw new IllegalStateException("ProtocolSupport is not loaded");
      }
   }
}
