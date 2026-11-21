package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.incendo.cloud.description.Description;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.utils.anticheat.MessageUtil;

public class GrimHelp implements BuildableCommand {
   @Override
   public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
      commandManager.command(
         commandManager.commandBuilder("grim", new String[]{"grimac"})
            .literal("help", Description.of("Display help information"))
            .permission("grim.help")
            .handler(this::handleHelp)
      );
   }

   private void handleHelp(@NotNull CommandContext<Sender> context) {
      Sender sender = context.sender();

      for (String string : GrimAPI.INSTANCE.getConfigManager().getConfig().getStringList("help")) {
         if (string != null) {
            string = MessageUtil.replacePlaceholders(sender, string);
            sender.sendMessage(MessageUtil.miniMessage(string));
         }
      }
   }
}
