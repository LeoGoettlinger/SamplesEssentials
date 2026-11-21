package eu.kennytv.maintenance.core.hook;

import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.plugin.ServerListPlusPlugin;

public final class ServerListPlusHook {
   private final ServerListPlusCore serverListPlusCore;

   public ServerListPlusHook(Object serverListPlus) {
      if (serverListPlus instanceof ServerListPlusPlugin serverListPlusPlugin) {
         this.serverListPlusCore = serverListPlusPlugin.getCore();
      } else {
         throw new IllegalArgumentException("Couldn't parse ServerListPlus instance!");
      }
   }

   public void setEnabled(boolean enable) {
      this.serverListPlusCore.getProfiles().setEnabled(enable);
   }
}
