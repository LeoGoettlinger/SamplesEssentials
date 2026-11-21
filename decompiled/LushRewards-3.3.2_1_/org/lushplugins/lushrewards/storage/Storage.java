package org.lushplugins.lushrewards.storage;

import com.google.gson.JsonObject;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.lushrewards.module.UserDataModule;

public abstract class Storage {
   public void enable(ConfigurationSection config) {
   }

   public void disable() {
   }

   public abstract JsonObject loadModuleUserDataJson(UUID var1, String var2);

   public abstract void saveModuleUserData(UserDataModule.UserData var1);
}
