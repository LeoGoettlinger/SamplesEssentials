package me.chancesd.pvpmanager.integration;

import me.chancesd.pvpmanager.integration.type.Dependency;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class BaseDependency implements Dependency {
   private final JavaPlugin plugin;
   private final Hook hook;

   public BaseDependency(Hook hook) {
      this.hook = hook;
      this.plugin = (JavaPlugin)hook.getPlugin();
      Log.infoColor(ChatColor.DARK_PURPLE + this.onEnableMessage());
   }

   @Override
   public String getName() {
      return this.getPlugin().getName();
   }

   @Override
   public Hook getHook() {
      return this.hook;
   }

   @Override
   public JavaPlugin getPlugin() {
      return this.plugin;
   }
}
