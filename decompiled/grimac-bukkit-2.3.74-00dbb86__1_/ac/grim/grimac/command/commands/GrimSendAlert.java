package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.incendo.cloud.parser.standard.StringParser;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.utils.anticheat.MessageUtil;

public class GrimSendAlert implements BuildableCommand {
   @Override
   public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
      commandManager.command(
         commandManager.commandBuilder("grim", new String[]{"grimac"})
            .literal("sendalert")
            .permission("grim.sendalert")
            .required("message", StringParser.greedyStringParser())
            .handler(this::handleSendAlert)
      );
   }

   private void handleSendAlert(@NotNull CommandContext<Sender> context) {
      String string = context.get("message");
      string = MessageUtil.replacePlaceholders((Sender)null, string);
      Component message = MessageUtil.miniMessage(string);
      GrimAPI.INSTANCE.getAlertManager().sendAlert(message, null);
   }
}
