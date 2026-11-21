package de.oliver.fancynpcs.loaders;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class FancyNpcsBootstrapper implements PluginBootstrap {
   public void bootstrap(@NotNull BootstrapContext bootstrapContext) {
   }

   @NotNull
   public JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
      return super.createPlugin(context);
   }
}
