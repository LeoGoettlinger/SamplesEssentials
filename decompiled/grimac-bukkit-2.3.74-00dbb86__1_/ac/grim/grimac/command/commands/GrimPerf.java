package ac.grim.grimac.command.commands;

import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.predictionengine.MovementCheckRunner;
import ac.grim.grimac.shaded.incendo.cloud.Command;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.context.CommandContext;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.format.NamedTextColor;

public class GrimPerf {
   public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
      Command.Builder<Sender> grimCommand = commandManager.commandBuilder("grim", new String[]{"grimac"});
      Command.Builder<Sender> configuredBuilder = grimCommand.literal("perf", "performance").permission("grim.performance").handler(this::handlePerformance);
      commandManager.command(configuredBuilder);
   }

   private void handlePerformance(@NotNull CommandContext<Sender> context) {
      Sender sender = context.sender();
      double millis = MovementCheckRunner.predictionNanos / 1000000.0;
      double longMillis = MovementCheckRunner.longPredictionNanos / 1000000.0;
      Component message1 = Component.text()
         .append(Component.text("Milliseconds per prediction (avg. 500): ", NamedTextColor.GRAY))
         .append(Component.text(millis, NamedTextColor.WHITE))
         .build();
      Component message2 = Component.text()
         .append(Component.text("Milliseconds per prediction (avg. 20k): ", NamedTextColor.GRAY))
         .append(Component.text(longMillis, NamedTextColor.WHITE))
         .build();
      sender.sendMessage(message1);
      sender.sendMessage(message2);
   }
}
