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

public class NightbreakDownloadContentCommand<T extends NightbreakManagedContent> extends AdvancedCommand {
   private final JavaPlugin plugin;
   private final NightbreakPluginSpec pluginSpec;
   private final Supplier<List<T>> packagesSupplier;
   private final AtomicBoolean guard;
   private final Consumer<CommandSender> reloadAction;
   private final boolean updatesOnly;

   public NightbreakDownloadContentCommand(
      JavaPlugin plugin, NightbreakPluginSpec pluginSpec, Supplier<List<T>> packagesSupplier, Consumer<CommandSender> reloadAction, boolean updatesOnly
   ) {
      this(plugin, pluginSpec, packagesSupplier, reloadAction, updatesOnly, pluginSpec.setupPermission());
   }

   public NightbreakDownloadContentCommand(
      JavaPlugin plugin,
      NightbreakPluginSpec pluginSpec,
      Supplier<List<T>> packagesSupplier,
      Consumer<CommandSender> reloadAction,
      boolean updatesOnly,
      String permission
   ) {
      this(plugin, pluginSpec, packagesSupplier, NightbreakPluginStateRegistry.getBulkOperationGuard(plugin), reloadAction, updatesOnly, permission);
   }

   public NightbreakDownloadContentCommand(
      JavaPlugin plugin,
      NightbreakPluginSpec pluginSpec,
      Supplier<List<T>> packagesSupplier,
      AtomicBoolean guard,
      Consumer<CommandSender> reloadAction,
      boolean updatesOnly
   ) {
      this(plugin, pluginSpec, packagesSupplier, guard, reloadAction, updatesOnly, pluginSpec.setupPermission());
   }

   public NightbreakDownloadContentCommand(
      JavaPlugin plugin,
      NightbreakPluginSpec pluginSpec,
      Supplier<List<T>> packagesSupplier,
      AtomicBoolean guard,
      Consumer<CommandSender> reloadAction,
      boolean updatesOnly,
      String permission
   ) {
      super(updatesOnly ? List.of("updatecontent", "updateallcontent") : List.of("downloadallcontent"));
      this.plugin = plugin;
      this.pluginSpec = pluginSpec;
      this.packagesSupplier = packagesSupplier;
      this.guard = guard;
      this.reloadAction = reloadAction;
      this.updatesOnly = updatesOnly;
      this.setPermission(permission);
      this.setSenderType(SenderType.ANY);
      this.setDescription(
         updatesOnly
            ? "Downloads updates for outdated " + pluginSpec.displayName() + " content."
            : "Downloads all available " + pluginSpec.displayName() + " content."
      );
      this.setUsage("/" + pluginSpec.rootCommand() + " " + (updatesOnly ? "updatecontent" : "downloadallcontent"));
   }

   @Override
   public void execute(CommandData commandData) {
      NightbreakBulkDownloader.execute(
         this.plugin,
         this.pluginSpec.displayName(),
         commandData.getCommandSender(),
         this.packagesSupplier.get(),
         this.updatesOnly,
         this.guard,
         this.reloadAction
      );
   }
}
