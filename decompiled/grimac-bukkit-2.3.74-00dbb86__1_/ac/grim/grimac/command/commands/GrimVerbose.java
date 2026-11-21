package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.manager.AlertManagerImpl;
import ac.grim.grimac.manager.datastore.PlayerToggleStore;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Objects;

public class GrimVerbose implements BuildableCommand {
   @Override
   public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
      commandManager.command(
         commandManager.commandBuilder("grim", new String[]{"grimac"}).literal("verbose").permission("grim.verbose").handler(this::handleVerbose)
      );
   }

   private void handleVerbose(@NotNull CommandContext<Sender> context) {
      Sender sender = context.sender();
      if (sender.isPlayer()) {
         PlatformPlayer p = Objects.requireNonNull(context.sender().getPlatformPlayer());
         AlertManagerImpl am = GrimAPI.INSTANCE.getAlertManager();
         boolean newState = !am.hasVerboseEnabled(p);
         am.setVerboseEnabled(p, newState, false);
         PlayerToggleStore toggles = GrimAPI.INSTANCE.getDataStoreLifecycle().playerToggleStore();
         toggles.applyUserToggle(p.getUniqueId(), "verbose", newState);
         if (newState) {
            toggles.applyUserToggle(p.getUniqueId(), "alerts", true);
         }
      } else if (sender.isConsole()) {
         GrimAPI.INSTANCE.getAlertManager().toggleConsoleVerbose();
      }
   }
}
