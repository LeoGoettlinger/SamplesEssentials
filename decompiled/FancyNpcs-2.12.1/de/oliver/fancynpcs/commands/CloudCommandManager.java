package de.oliver.fancynpcs.commands;

import de.oliver.cloud.annotations.AnnotationParser;
import de.oliver.cloud.bukkit.CloudBukkitCapabilities;
import de.oliver.cloud.bukkit.parser.WorldParser;
import de.oliver.cloud.bukkit.parser.location.LocationParser;
import de.oliver.cloud.component.CommandComponent;
import de.oliver.cloud.exception.ArgumentParseException;
import de.oliver.cloud.exception.CommandExecutionException;
import de.oliver.cloud.exception.InvalidCommandSenderException;
import de.oliver.cloud.exception.InvalidSyntaxException;
import de.oliver.cloud.exception.NoPermissionException;
import de.oliver.cloud.exception.handling.ExceptionHandler;
import de.oliver.cloud.exception.handling.ExceptionHandlerRegistration;
import de.oliver.cloud.exception.parsing.NumberParseException;
import de.oliver.cloud.exception.parsing.ParserException;
import de.oliver.cloud.execution.ExecutionCoordinator;
import de.oliver.cloud.paper.LegacyPaperCommandManager;
import de.oliver.cloud.parser.standard.BooleanParser;
import de.oliver.cloud.parser.standard.EnumParser;
import de.oliver.fancyanalytics.logger.properties.StringProperty;
import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancylib.translations.message.Message;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.data.property.NpcVisibility;
import de.oliver.fancynpcs.commands.arguments.ActionTriggerArgument;
import de.oliver.fancynpcs.commands.arguments.ActionTypeArgument;
import de.oliver.fancynpcs.commands.arguments.LocationArgument;
import de.oliver.fancynpcs.commands.arguments.NpcArgument;
import de.oliver.fancynpcs.commands.exceptions.ReplyingParseException;
import de.oliver.fancynpcs.commands.npc.ActionCMD;
import de.oliver.fancynpcs.commands.npc.AttributeCMD;
import de.oliver.fancynpcs.commands.npc.CenterCMD;
import de.oliver.fancynpcs.commands.npc.CollidableCMD;
import de.oliver.fancynpcs.commands.npc.CopyCMD;
import de.oliver.fancynpcs.commands.npc.CreateCMD;
import de.oliver.fancynpcs.commands.npc.DisplayNameCMD;
import de.oliver.fancynpcs.commands.npc.EquipmentCMD;
import de.oliver.fancynpcs.commands.npc.FixCMD;
import de.oliver.fancynpcs.commands.npc.GlowingCMD;
import de.oliver.fancynpcs.commands.npc.HelpCMD;
import de.oliver.fancynpcs.commands.npc.InfoCMD;
import de.oliver.fancynpcs.commands.npc.InteractionCooldownCMD;
import de.oliver.fancynpcs.commands.npc.ListCMD;
import de.oliver.fancynpcs.commands.npc.MoveHereCMD;
import de.oliver.fancynpcs.commands.npc.MoveToCMD;
import de.oliver.fancynpcs.commands.npc.NearbyCMD;
import de.oliver.fancynpcs.commands.npc.RemoveCMD;
import de.oliver.fancynpcs.commands.npc.RotateCMD;
import de.oliver.fancynpcs.commands.npc.ScaleCMD;
import de.oliver.fancynpcs.commands.npc.ShowInTabCMD;
import de.oliver.fancynpcs.commands.npc.SkinCMD;
import de.oliver.fancynpcs.commands.npc.TeleportCMD;
import de.oliver.fancynpcs.commands.npc.TurnToPlayerCMD;
import de.oliver.fancynpcs.commands.npc.TurnToPlayerDistanceCMD;
import de.oliver.fancynpcs.commands.npc.TypeCMD;
import de.oliver.fancynpcs.commands.npc.VisibilityCMD;
import de.oliver.fancynpcs.commands.npc.VisibilityDistanceCMD;
import de.oliver.fancynpcs.utils.GlowingColor;
import io.leangen.geantyref.TypeToken;
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public final class CloudCommandManager {
   @NotNull
   private final FancyNpcs plugin;
   @NotNull
   private final LegacyPaperCommandManager<CommandSender> commandManager;
   @NotNull
   private final AnnotationParser<CommandSender> annotationParser;

   public CloudCommandManager(@NotNull final FancyNpcs plugin, final boolean isBrigadier) {
      this.plugin = plugin;
      this.commandManager = LegacyPaperCommandManager.createNative(plugin, ExecutionCoordinator.simpleCoordinator());
      if (isBrigadier && this.commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
         this.commandManager.registerBrigadier();
      }

      this.annotationParser = new AnnotationParser<>(this.commandManager, CommandSender.class);
   }

   @NotNull
   public CloudCommandManager registerArguments() {
      this.annotationParser.parse(NpcArgument.INSTANCE);
      this.annotationParser.parse(LocationArgument.INSTANCE);
      this.annotationParser.parse(ActionTriggerArgument.INSTANCE);
      this.annotationParser.parse(ActionTypeArgument.INSTANCE);
      return this;
   }

   @NotNull
   public CloudCommandManager registerExceptionHandlers() {
      Translator translator = this.plugin.getTranslator();
      this.commandManager.exceptionController().registerHandler(ArgumentParseException.class, ExceptionHandler.unwrappingHandler(NumberParseException.class));
      this.commandManager
         .exceptionController()
         .registerHandler(ArgumentParseException.class, ExceptionHandler.unwrappingHandler(BooleanParser.BooleanParseException.class));
      this.commandManager
         .exceptionController()
         .registerHandler(ArgumentParseException.class, ExceptionHandler.unwrappingHandler(EnumParser.EnumParseException.class));
      this.commandManager
         .exceptionController()
         .registerHandler(ArgumentParseException.class, ExceptionHandler.unwrappingHandler(WorldParser.WorldParseException.class));
      this.commandManager.exceptionController().registerHandler(ArgumentParseException.class, ExceptionHandler.unwrappingHandler(ReplyingParseException.class));
      this.commandManager
         .exceptionController()
         .registerHandler(
            NoPermissionException.class,
            exceptionContext -> translator.translate("command_missing_permissions").withPrefix().send(exceptionContext.context().sender())
         );
      this.commandManager
         .exceptionController()
         .registerHandler(
            InvalidCommandSenderException.class,
            exceptionContext -> translator.translate("command_player_only").withPrefix().send(exceptionContext.context().sender())
         );
      this.commandManager
         .exceptionController()
         .registerHandler(
            NumberParseException.class,
            exceptionContext -> translator.translate("command_invalid_number")
               .withPrefix()
               .replaceStripped("input", exceptionContext.exception().input())
               .replace("min", exceptionContext.exception().range().min().toString())
               .replace("max", exceptionContext.exception().range().max().toString())
               .send(exceptionContext.context().sender())
         );
      this.commandManager
         .exceptionController()
         .registerHandler(
            BooleanParser.BooleanParseException.class,
            exceptionContext -> translator.translate("command_invalid_boolean")
               .withPrefix()
               .replaceStripped("input", exceptionContext.exception().input())
               .send(exceptionContext.context().sender())
         );
      this.commandManager
         .exceptionController()
         .registerHandler(
            WorldParser.WorldParseException.class,
            exceptionContext -> translator.translate("command_invalid_world")
               .withPrefix()
               .replaceStripped("input", exceptionContext.exception().input())
               .send(exceptionContext.context().sender())
         );
      this.commandManager
         .exceptionController()
         .register(
            ExceptionHandlerRegistration.<CommandSender, ArgumentParseException>builder(TypeToken.get(ArgumentParseException.class))
               .exceptionFilter(
                  exception -> exception.getCause() instanceof ParserException parserException && parserException.argumentParserClass() == LocationParser.class
               )
               .exceptionHandler(
                  exceptionContext -> {
                     ParserException exception = (ParserException)exceptionContext.exception().getCause();
                     String input = exception.captionVariables()[0].value();
                     translator.translate("command_invalid_location")
                        .withPrefix()
                        .replaceStripped("input", !input.isBlank() ? input : "N/A")
                        .send(exceptionContext.context().sender());
                  }
               )
               .build()
         );
      this.commandManager
         .exceptionController()
         .registerHandler(
            EnumParser.EnumParseException.class,
            exceptionContext -> {
               String translationKey = "command_invalid_enum_generic";
               if (exceptionContext.exception().enumClass() == ListCMD.SortType.class) {
                  translationKey = "command_invalid_list_sort_type";
               } else if (exceptionContext.exception().enumClass() == NearbyCMD.SortType.class) {
                  translationKey = "command_invalid_nearby_sort_type";
               } else if (exceptionContext.exception().enumClass() == EntityType.class) {
                  translationKey = "command_invalid_entity_type";
               } else if (exceptionContext.exception().enumClass() == GlowingColor.class) {
                  translationKey = "command_invalid_glowing_color";
               } else if (exceptionContext.exception().enumClass() == NpcVisibility.class) {
                  translationKey = "command_invalid_npc_visibility";
               }

               translator.translate(translationKey)
                  .withPrefix()
                  .replaceStripped("input", exceptionContext.exception().input())
                  .replace("enum", exceptionContext.exception().enumClass().getSimpleName().toLowerCase())
                  .send(exceptionContext.context().sender());
            }
         );
      this.commandManager.exceptionController().registerHandler(ReplyingParseException.class, context -> context.exception().runnable().run());
      this.commandManager
         .exceptionController()
         .registerHandler(
            InvalidSyntaxException.class,
            exceptionContext -> {
               StringBuilder translationKeyBuilder = new StringBuilder("command_syntax.");
               exceptionContext.exception()
                  .currentChain()
                  .stream()
                  .filter(c -> c.type() == CommandComponent.ComponentType.LITERAL)
                  .forEach(literal -> translationKeyBuilder.append(literal.name()).append(' '));
               String translationKey = translationKeyBuilder.toString().trim().replace(' ', '_');
               Message message = Optional.ofNullable(this.plugin.getTranslator().getSelectedLanguage().getMessage(translationKey))
                  .orElse(this.plugin.getTranslator().getFallbackLanguage().getMessage(translationKey));
               if (message == null) {
                  this.plugin
                     .getTranslator()
                     .translate("command_invalid_syntax_generic")
                     .withPrefix()
                     .replace("syntax", exceptionContext.exception().correctSyntax())
                     .send(exceptionContext.context().sender());
               } else {
                  message.send(exceptionContext.context().sender());
               }
            }
         );
      this.commandManager
         .exceptionController()
         .registerHandler(
            CommandExecutionException.class,
            context -> {
               this.plugin
                  .getFancyLogger()
                  .error(
                     "An unexpected error occurred while executing a command!",
                     ThrowableProperty.of(context.exception()),
                     StringProperty.of("rawInput", context.context().rawInput().input())
                  );
               this.plugin.getTranslator().translate("command_unexpected_exception").withPrefix().send(context.context().sender());
            }
         );
      return this;
   }

   @NotNull
   public CloudCommandManager registerCommands() {
      this.annotationParser.parse(FancyNpcsCMD.INSTANCE);
      this.annotationParser.parse(NpcConvertCMD.INSTANCE);
      this.annotationParser.parse(AttributeCMD.INSTANCE);
      this.annotationParser.parse(CenterCMD.INSTANCE);
      this.annotationParser.parse(CollidableCMD.INSTANCE);
      this.annotationParser.parse(CopyCMD.INSTANCE);
      this.annotationParser.parse(CreateCMD.INSTANCE);
      this.annotationParser.parse(DisplayNameCMD.INSTANCE);
      this.annotationParser.parse(EquipmentCMD.INSTANCE);
      this.annotationParser.parse(FixCMD.INSTANCE);
      this.annotationParser.parse(GlowingCMD.INSTANCE);
      this.annotationParser.parse(InfoCMD.INSTANCE);
      this.annotationParser.parse(InteractionCooldownCMD.INSTANCE);
      this.annotationParser.parse(ListCMD.INSTANCE);
      this.annotationParser.parse(MoveHereCMD.INSTANCE);
      this.annotationParser.parse(MoveToCMD.INSTANCE);
      this.annotationParser.parse(NearbyCMD.INSTANCE);
      this.annotationParser.parse(HelpCMD.INSTANCE);
      this.annotationParser.parse(RemoveCMD.INSTANCE);
      this.annotationParser.parse(RotateCMD.INSTANCE);
      this.annotationParser.parse(ShowInTabCMD.INSTANCE);
      this.annotationParser.parse(SkinCMD.INSTANCE);
      this.annotationParser.parse(TeleportCMD.INSTANCE);
      this.annotationParser.parse(TurnToPlayerCMD.INSTANCE);
      this.annotationParser.parse(TurnToPlayerDistanceCMD.INSTANCE);
      this.annotationParser.parse(TypeCMD.INSTANCE);
      this.annotationParser.parse(ActionCMD.INSTANCE);
      this.annotationParser.parse(VisibilityDistanceCMD.INSTANCE);
      this.annotationParser.parse(VisibilityCMD.INSTANCE);
      this.annotationParser.parse(ScaleCMD.INSTANCE);
      if (FancyNpcs.ENABLE_DEBUG_MODE_FEATURE_FLAG.isEnabled()) {
         this.annotationParser.parse(FancyNpcsDebugCMD.INSTANCE);
      }

      return this;
   }

   @NotNull
   public LegacyPaperCommandManager<CommandSender> getCommandManager() {
      return this.commandManager;
   }

   @NotNull
   public AnnotationParser<CommandSender> getAnnotationParser() {
      return this.annotationParser;
   }
}
