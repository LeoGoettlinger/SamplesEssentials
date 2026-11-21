package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.CommandManager;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class NightbreakPluginBootstrap {
   private NightbreakPluginBootstrap() {
   }

   public static void startInitialization(
      JavaPlugin plugin, PluginInitializationConfig initializationConfig, NightbreakPluginSpec pluginSpec, NightbreakPluginHooks hooks
   ) {
      MagmaCore.onEnable(plugin);
      MagmaCore.startInitialization(
         plugin, initializationConfig, context -> hooks.asyncInitialization(context), context -> hooks.syncInitialization(context), () -> {
            hooks.onInitializationSuccess();
            NightbreakPluginUpdater.autoDownloadPluginUpdateIfEnabled(plugin, pluginSpec);
            CommandSender pendingReloadSender = NightbreakPluginStateRegistry.consumePendingReloadSender(plugin);
            if (pendingReloadSender != null) {
               Logger.sendMessage(pendingReloadSender, pluginSpec.reloadSuccessMessage());
            }
         }, throwable -> {
            NightbreakPluginStateRegistry.clearPendingReloadSender(plugin);
            hooks.onInitializationFailure(throwable);
         }
      );
   }

   public static void setPendingReloadSender(JavaPlugin plugin, CommandSender sender) {
      NightbreakPluginStateRegistry.setPendingReloadSender(plugin, sender);
   }

   public static void reloadPlugin(JavaPlugin plugin, CommandSender sender) {
      if (plugin != null) {
         plugin.onDisable();
         setPendingReloadSender(plugin, sender);
         plugin.onLoad();
         plugin.onEnable();
      }
   }

   public static <T extends NightbreakManagedContent> void registerStandardCommands(
      JavaPlugin plugin,
      CommandManager commandManager,
      NightbreakPluginSpec pluginSpec,
      Consumer<Player> setupAction,
      Consumer<Player> initializeAction,
      Supplier<List<T>> packagesSupplier,
      Consumer<CommandSender> reloadAction
   ) {
      Objects.requireNonNull(initializeAction, "initializeAction");
      commandManager.registerCommand(new NightbreakPluginBootstrap.SetupCommand(pluginSpec, setupAction));
      commandManager.registerCommand(new NightbreakPluginBootstrap.InitializeCommand(pluginSpec, initializeAction));
      registerContentCommands(plugin, commandManager, pluginSpec, packagesSupplier, reloadAction);
   }

   private static <T extends NightbreakManagedContent> void registerContentCommands(
      JavaPlugin plugin,
      CommandManager commandManager,
      NightbreakPluginSpec pluginSpec,
      Supplier<List<T>> packagesSupplier,
      Consumer<CommandSender> reloadAction
   ) {
      AtomicBoolean guard = NightbreakPluginStateRegistry.getBulkOperationGuard(plugin);
      commandManager.registerCommand(new NightbreakRecommendedPluginsCommand(plugin, pluginSpec));
      commandManager.registerCommand(new NightbreakDownloadPluginUpdateCommand(plugin, pluginSpec));
      commandManager.registerCommand(new NightbreakApplyPluginUpdateCommand(plugin, pluginSpec));
      commandManager.registerCommand(new NightbreakDownloadEverythingCommand<>(plugin, pluginSpec, packagesSupplier, guard, reloadAction));
      if (pluginSpec.hasContentPackages()) {
         commandManager.registerCommand(new NightbreakDownloadContentCommand<>(plugin, pluginSpec, packagesSupplier, guard, reloadAction, false));
         commandManager.registerCommand(new NightbreakDownloadContentCommand<>(plugin, pluginSpec, packagesSupplier, guard, reloadAction, true));
         commandManager.registerCommand(new NightbreakForceReinstallContentCommand<>(plugin, pluginSpec, packagesSupplier, guard, reloadAction));
      }
   }

   public static void registerStandardCommands(
      JavaPlugin plugin, CommandManager commandManager, NightbreakPluginSpec pluginSpec, Consumer<Player> setupAction, Consumer<CommandSender> reloadAction
   ) {
      commandManager.registerCommand(new NightbreakPluginBootstrap.SetupCommand(pluginSpec, setupAction));
      registerContentCommands(plugin, commandManager, pluginSpec, List::of, reloadAction);
   }

   private static final class InitializeCommand extends AdvancedCommand {
      private final Consumer<Player> initializeAction;

      private InitializeCommand(NightbreakPluginSpec pluginSpec, Consumer<Player> initializeAction) {
         super(List.of("initialize"));
         this.initializeAction = initializeAction;
         this.setPermission(pluginSpec.initializePermission());
         this.setSenderType(SenderType.PLAYER);
         this.setDescription("Opens the first-time setup menu for " + pluginSpec.displayName() + ".");
         this.setUsage("/" + pluginSpec.rootCommand() + " initialize");
      }

      @Override
      public void execute(CommandData commandData) {
         this.initializeAction.accept(commandData.getPlayerSender());
      }
   }

   private static final class SetupCommand extends AdvancedCommand {
      private final Consumer<Player> setupAction;

      private SetupCommand(NightbreakPluginSpec pluginSpec, Consumer<Player> setupAction) {
         super(List.of("setup"));
         this.setupAction = setupAction;
         this.setPermission(pluginSpec.setupPermission());
         this.setSenderType(SenderType.PLAYER);
         this.setDescription("Opens the setup menu for " + pluginSpec.displayName() + ".");
         this.setUsage("/" + pluginSpec.rootCommand() + " setup");
      }

      @Override
      public void execute(CommandData commandData) {
         this.setupAction.accept(commandData.getPlayerSender());
      }
   }
}
