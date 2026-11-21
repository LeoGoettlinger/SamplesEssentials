package com.lishid.openinv.util.config;

import com.lishid.openinv.util.AccessEqualMode;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Config {
   @NotNull
   private Configuration root = new MemoryConfiguration();
   @Nullable
   private AccessEqualMode accessEqualMode;

   public void reload(@NotNull Configuration configuration) {
      this.root = configuration;
      this.accessEqualMode = null;
   }

   public boolean isSaveDisabled() {
      return this.root.getBoolean("settings.disable-saving", false);
   }

   public boolean isOfflineDisabled() {
      return this.root.getBoolean("settings.disable-offline-access", false);
   }

   public boolean doesNoArgsOpenSelf() {
      return this.root.getBoolean("settings.command.open.no-args-opens-self", false);
   }

   @NotNull
   public AccessEqualMode getAccessEqualMode() {
      if (this.accessEqualMode == null) {
         this.accessEqualMode = AccessEqualMode.of(this.root.getString("settings.equal-access"));
      }

      return this.accessEqualMode;
   }
}
