package su.nightexpress.nightcore;

import java.util.Set;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.integration.permission.PermissionBridge;
import su.nightexpress.nightcore.integration.permission.PermissionProvider;
import su.nightexpress.nightcore.util.bridge.Software;

@Deprecated
public class Engine {
   @Deprecated
   @NonNull
   public static Set<NightPlugin> getChildrens() {
      return NightCore.CHILDRENS;
   }

   @Deprecated
   @NonNull
   public static NightCore core() {
      return NightCore.get();
   }

   @Deprecated
   @NonNull
   public static Software software() {
      return Software.instance();
   }

   @Deprecated
   @NonNull
   public static PermissionProvider getPermissions() {
      return PermissionBridge.getProvider();
   }

   @Deprecated
   public static boolean hasPermissions() {
      return PermissionBridge.hasProvider();
   }
}
