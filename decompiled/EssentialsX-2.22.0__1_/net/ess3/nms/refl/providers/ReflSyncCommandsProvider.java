package net.ess3.nms.refl.providers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.ess3.nms.refl.ReflUtil;
import net.ess3.provider.SyncCommandsProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.Bukkit;
import org.bukkit.Server;

@ProviderData(
   description = "Reflection Sync Commands Provider"
)
public class ReflSyncCommandsProvider implements SyncCommandsProvider {
   private final MethodHandle nmsSyncCommands;

   public ReflSyncCommandsProvider() {
      MethodHandle syncCommands = null;
      Class<?> nmsClass = ReflUtil.getOBCClass("CraftServer");

      try {
         syncCommands = MethodHandles.lookup().findVirtual(nmsClass, "syncCommands", MethodType.methodType(void.class));
      } catch (Exception var4) {
      }

      this.nmsSyncCommands = syncCommands;
   }

   @Override
   public void syncCommands() {
      if (this.nmsSyncCommands != null) {
         try {
            this.nmsSyncCommands.invoke((Server)Bukkit.getServer());
         } catch (Throwable var2) {
            var2.printStackTrace();
         }
      }
   }
}
