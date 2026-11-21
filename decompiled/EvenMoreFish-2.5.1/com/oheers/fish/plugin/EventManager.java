package com.oheers.fish.plugin;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.SkullSaver;
import com.oheers.fish.baits.BaitApplicationListener;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.events.FishInteractEvent;
import com.oheers.fish.events.JoinChecker;
import com.oheers.fish.fishing.EMFFishListener;
import com.oheers.fish.fishing.exploits.ExploitListener;
import com.oheers.fish.fishing.processors.FishingProcessor;
import com.oheers.fish.fishing.processors.HuntingProcessor;
import com.oheers.fish.recipe.RecipeListener;
import com.oheers.fish.update.UpdateNotify;
import com.oheers.fish.utils.ItemProtectionListener;
import org.bukkit.plugin.PluginManager;

public class EventManager {
   private final EvenMoreFish plugin;
   private final PluginManager pm;

   public EventManager(EvenMoreFish plugin) {
      this.plugin = plugin;
      this.pm = plugin.getServer().getPluginManager();
   }

   public void registerCoreListeners() {
      if (DatabaseUtil.isDatabaseOnline()) {
         this.pm.registerEvents(this.plugin.getPluginDataManager().getUserManager(), this.plugin);
         this.pm.registerEvents(new EMFFishListener(), this.plugin);
      }

      this.pm.registerEvents(new JoinChecker(), this.plugin);
      this.pm.registerEvents(new FishingProcessor(), this.plugin);
      this.pm.registerEvents(new HuntingProcessor(), this.plugin);
      this.pm.registerEvents(new SkullSaver(), this.plugin);
      this.pm.registerEvents(new UpdateNotify(), this.plugin);
      this.pm.registerEvents(new BaitApplicationListener(), this.plugin);
      this.pm.registerEvents(new ItemProtectionListener(), this.plugin);
      this.pm.registerEvents(new FishInteractEvent(), this.plugin);
      this.pm.registerEvents(new RecipeListener(), this.plugin);
      this.pm.registerEvents(new ExploitListener(), this.plugin);
   }

   public void registerOptionalListeners() {
      this.plugin.getDependencyManager().checkOptionalDependencies();
   }
}
