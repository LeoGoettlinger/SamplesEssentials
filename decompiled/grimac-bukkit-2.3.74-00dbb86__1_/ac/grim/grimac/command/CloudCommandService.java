package ac.grim.grimac.command;

import ac.grim.grimac.command.commands.GrimAlerts;
import ac.grim.grimac.command.commands.GrimBrands;
import ac.grim.grimac.command.commands.GrimDebug;
import ac.grim.grimac.command.commands.GrimDump;
import ac.grim.grimac.command.commands.GrimHelp;
import ac.grim.grimac.command.commands.GrimHistory;
import ac.grim.grimac.command.commands.GrimHistoryCopy;
import ac.grim.grimac.command.commands.GrimHistoryMigrate;
import ac.grim.grimac.command.commands.GrimList;
import ac.grim.grimac.command.commands.GrimLog;
import ac.grim.grimac.command.commands.GrimPerf;
import ac.grim.grimac.command.commands.GrimProfile;
import ac.grim.grimac.command.commands.GrimReload;
import ac.grim.grimac.command.commands.GrimSendAlert;
import ac.grim.grimac.command.commands.GrimSpectate;
import ac.grim.grimac.command.commands.GrimStopSpectating;
import ac.grim.grimac.command.commands.GrimTestWebhook;
import ac.grim.grimac.command.commands.GrimVerbose;
import ac.grim.grimac.command.commands.GrimVersion;
import ac.grim.grimac.command.handler.GrimCommandFailureHandler;
import ac.grim.grimac.platform.api.command.CommandService;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.shaded.geantyref.TypeToken;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.exception.InvalidSyntaxException;
import ac.grim.grimac.shaded.incendo.cloud.key.CloudKey;
import ac.grim.grimac.shaded.incendo.cloud.processors.requirements.RequirementApplicable;
import ac.grim.grimac.shaded.incendo.cloud.processors.requirements.RequirementPostprocessor;
import ac.grim.grimac.shaded.incendo.cloud.processors.requirements.Requirements;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.ComponentLike;
import ac.grim.grimac.shaded.kyori.adventure.text.format.NamedTextColor;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

public class CloudCommandService implements CommandService {
   public static final CloudKey<Requirements<Sender, SenderRequirement>> REQUIREMENT_KEY = CloudKey.of(
      "requirements", new TypeToken<Requirements<Sender, SenderRequirement>>() {}
   );
   public static final RequirementApplicable.RequirementApplicableFactory<Sender, SenderRequirement> REQUIREMENT_FACTORY = RequirementApplicable.factory(
      REQUIREMENT_KEY
   );
   private boolean commandsRegistered = false;
   private final Supplier<CommandManager<Sender>> commandManagerSupplier;
   private final CloudPlatformCommandArguments commandArguments;

   public CloudCommandService(Supplier<CommandManager<Sender>> commandManagerSupplier, CloudPlatformCommandArguments commandArguments) {
      this.commandManagerSupplier = commandManagerSupplier;
      this.commandArguments = commandArguments;
   }

   @Override
   public void registerCommands() {
      if (!this.commandsRegistered) {
         CommandManager<Sender> commandManager = this.commandManagerSupplier.get();
         new GrimPerf().register(commandManager, this.commandArguments);
         new GrimDebug().register(commandManager, this.commandArguments);
         new GrimAlerts().register(commandManager, this.commandArguments);
         new GrimProfile().register(commandManager, this.commandArguments);
         new GrimSendAlert().register(commandManager, this.commandArguments);
         new GrimHelp().register(commandManager, this.commandArguments);
         new GrimHistory().register(commandManager, this.commandArguments);
         new GrimHistoryMigrate().register(commandManager, this.commandArguments);
         new GrimHistoryCopy().register(commandManager, this.commandArguments);
         new GrimReload().register(commandManager, this.commandArguments);
         new GrimSpectate().register(commandManager, this.commandArguments);
         new GrimStopSpectating().register(commandManager, this.commandArguments);
         new GrimLog().register(commandManager, this.commandArguments);
         new GrimVerbose().register(commandManager, this.commandArguments);
         new GrimVersion().register(commandManager, this.commandArguments);
         new GrimDump().register(commandManager, this.commandArguments);
         new GrimBrands().register(commandManager, this.commandArguments);
         new GrimList().register(commandManager, this.commandArguments);
         new GrimTestWebhook().register(commandManager, this.commandArguments);
         RequirementPostprocessor<Sender, SenderRequirement> senderRequirementPostprocessor = RequirementPostprocessor.of(
            REQUIREMENT_KEY, new GrimCommandFailureHandler()
         );
         commandManager.registerCommandPostProcessor(senderRequirementPostprocessor);
         this.registerInvalidSyntaxHandler(commandManager);
         this.commandsRegistered = true;
      }
   }

   private void registerInvalidSyntaxHandler(CommandManager<Sender> commandManager) {
      commandManager.exceptionController().registerHandler(InvalidSyntaxException.class, context -> {
         Sender sender = context.context().sender();
         if (isHistoryInput(context.context().rawInput().input())) {
            sender.sendMessage(Component.text("Invalid history syntax.", NamedTextColor.RED));
            sender.sendMessage(Component.text("Use: /grim history <player> [page <N>]", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Use: /grim history <player> session <N|latest> [page <N>] [-d] [-v]", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Tip: /grim history <player> session shows filter and detail options.", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Use /grim history player <player> ... for names that collide with history subcommands.", NamedTextColor.GRAY));
         } else {
            sender.sendMessage(Component.text(context.exception().correctSyntax(), NamedTextColor.RED));
         }
      });
   }

   private static boolean isHistoryInput(String rawInput) {
      String input = rawInput.strip();
      if (input.startsWith("/")) {
         input = input.substring(1).strip();
      }

      String[] tokens = input.toLowerCase(Locale.ROOT).split("\\s+");
      return tokens.length >= 2 && (tokens[0].equals("grim") || tokens[0].equals("grimac")) && (tokens[1].equals("history") || tokens[1].equals("hist"));
   }

   protected <E extends Exception> void registerExceptionHandler(CommandManager<Sender> commandManager, Class<E> ex, Function<E, ComponentLike> toComponent) {
      commandManager.exceptionController()
         .registerHandler(ex, c -> c.context().sender().sendMessage(toComponent.apply((E)c.exception()).asComponent().colorIfAbsent(NamedTextColor.RED)));
   }
}
