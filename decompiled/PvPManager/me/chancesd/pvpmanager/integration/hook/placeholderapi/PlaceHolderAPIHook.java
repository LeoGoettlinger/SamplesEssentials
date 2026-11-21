package me.chancesd.pvpmanager.integration.hook.placeholderapi;

import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.DependencyException;
import me.chancesd.pvpmanager.integration.Hook;

public class PlaceHolderAPIHook extends BaseDependency {
   private final PlaceHolderAPI placeHolderAPI = new PlaceHolderAPI(PvPManager.getInstance());

   public PlaceHolderAPIHook(Hook hook) {
      super(hook);
      if (!this.placeHolderAPI.register() && !this.placeHolderAPI.isRegistered()) {
         throw new DependencyException("Error registering placeholder expansion, placeholders disabled!", hook);
      }
   }

   public PlaceHolderAPI getPlaceHolder() {
      return this.placeHolderAPI;
   }
}
