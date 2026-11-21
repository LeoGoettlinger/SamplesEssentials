package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.manager.AlertManagerImpl;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.incendo.cloud.description.Description;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Objects;

public class GrimAlerts implements BuildableCommand {
   @Override
   public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
      commandManager.command(
         commandManager.commandBuilder("grim", new String[]{"grimac"})
            .literal("alerts", Description.of("Toggle alerts for the sender"))
            .permission("grim.alerts")
            .handler(this::handleAlerts)
      );
   }

   private void handleAlerts(@NotNull CommandContext<Sender> context) {
      Sender sender = context.sender();
      if (sender.isPlayer()) {
         PlatformPlayer p = Objects.requireNonNull(context.sender().getPlatformPlayer());
         AlertManagerImpl am = GrimAPI.INSTANCE.getAlertManager();
         boolean newState = !am.hasAlertsEnabled(p);
         am.setAlertsEnabled(p, newState, false);
         GrimAPI.INSTANCE.getDataStoreLifecycle().playerToggleStore().applyUserToggle(p.getUniqueId(), "alerts", newState);
      } else if (sender.isConsole()) {
         GrimAPI.INSTANCE.getAlertManager().toggleConsoleAlerts();
      }
   }
}
