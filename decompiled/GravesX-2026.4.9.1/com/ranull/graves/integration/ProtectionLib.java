package com.ranull.graves.integration;

import com.ranull.graves.Graves;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ProtectionLib {
   private final Graves plugin;
   private final Plugin protectionLibPlugin;

   public ProtectionLib(Graves plugin, Plugin protectionLibPlugin) {
      this.plugin = plugin;
      this.protectionLibPlugin = protectionLibPlugin;
   }

   public boolean canBuild(Location location, Player player) {
      if (this.plugin.getIntegrationManager().hasFurnitureLib()) {
         return this.plugin.getIntegrationManager().getFurnitureLib().canBuild(location, player);
      } else {
         try {
            Class<?> apiClass = Class.forName("de.Ste3et_C0st.ProtectionLib.main.ProtectionLib");
            Object protectionLib = apiClass.cast(this.protectionLibPlugin);
            Method canBuild = protectionLib.getClass().getMethod("canBuild", Location.class, Player.class);
            Object result = canBuild.invoke(protectionLib, location, player);
            return result instanceof Boolean && (Boolean)result;
         } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException var7) {
            return true;
         }
      }
   }
}
