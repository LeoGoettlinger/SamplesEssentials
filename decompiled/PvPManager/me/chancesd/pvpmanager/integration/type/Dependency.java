package me.chancesd.pvpmanager.integration.type;

import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.setting.Conf;
import org.bukkit.plugin.java.JavaPlugin;

public interface Dependency {
   String getName();

   Hook getHook();

   JavaPlugin getPlugin();

   default String onEnableMessage() {
      return this.getName() + " Found! Hooked successfully";
   }

   default boolean getConfigBoolean(String key, boolean def) {
      return Conf.getPluginHooks().getBoolean(this.getName() + "." + key, def);
   }
}
