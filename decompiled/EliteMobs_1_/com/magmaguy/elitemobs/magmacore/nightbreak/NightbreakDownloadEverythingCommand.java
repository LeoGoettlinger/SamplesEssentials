package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class NightbreakDownloadEverythingCommand<T extends NightbreakManagedContent> extends AdvancedCommand {
   private final JavaPlugin plugin;
   private final NightbreakPluginSpec pluginSpec;
   private final Supplier<List<T>> packagesSupplier;
   private final AtomicBoolean guard;
   private final Consumer<CommandSender> reloadAction;

   public NightbreakDownloadEverythingCommand(
      JavaPlugin plugin, NightbreakPluginSpec pluginSpec, Supplier<List<T>> packagesSupplier, Consumer<CommandSender> reloadAction
   ) {
      this(plugin, pluginSpec, packagesSupplier, NightbreakPluginStateRegistry.getBulkOperationGuard(plugin), reloadAction);
   }

   public NightbreakDownloadEverythingCommand(
      JavaPlugin plugin, NightbreakPluginSpec pluginSpec, Supplier<List<T>> packagesSupplier, AtomicBoolean guard, Consumer<CommandSender> reloadAction
   ) {
      super(List.of("downloadall"));
      this.plugin = plugin;
      this.pluginSpec = pluginSpec;
      this.packagesSupplier = packagesSupplier;
      this.guard = guard;
      this.reloadAction = reloadAction;
      this.setPermission(pluginSpec.adminPermission());
      this.setSenderType(SenderType.ANY);
      this.setDescription("Downloads the " + pluginSpec.displayName() + " plugin update and all available content.");
      this.setUsage("/" + pluginSpec.rootCommand() + " downloadall");
      NightbreakPluginUpdater.autoDownloadContentUpdatesIfEnabled(plugin, pluginSpec, packagesSupplier, guard, reloadAction);
   }

   @Override
   public void execute(CommandData commandData) {
      CommandSender sender = commandData.getCommandSender();
      NightbreakPluginUpdater.downloadPluginUpdateAsync(
         this.plugin,
         this.pluginSpec,
         sender,
         result -> {
            if (this.pluginSpec.hasContentPackages()) {
               if (result.status() != NightbreakPluginUpdater.DownloadStatus.NO_TOKEN && result.status() != NightbreakPluginUpdater.DownloadStatus.AUTH_FAILURE
                  )
                {
                  NightbreakBulkDownloader.execute(
                     this.plugin, this.pluginSpec.displayName(), sender, this.packagesSupplier.get(), false, this.guard, this.reloadAction
                  );
               }
            }
         }
      );
   }
}
