package net.ess3.provider.providers;

import net.ess3.provider.SyncCommandsProvider;
import net.essentialsx.providers.ProviderData;
import net.essentialsx.providers.ProviderTest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@ProviderData(
   description = "1.21.4+ Sync Commands Provider",
   weight = 1
)
public class ModernSyncCommandsProvider implements SyncCommandsProvider {
   @Override
   public void syncCommands() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         player.updateCommands();
      }
   }

   @ProviderTest
   public static boolean test() {
      try {
         Class.forName("org.bukkit.entity.Creaking");
         return true;
      } catch (Throwable var1) {
         return false;
      }
   }
}
