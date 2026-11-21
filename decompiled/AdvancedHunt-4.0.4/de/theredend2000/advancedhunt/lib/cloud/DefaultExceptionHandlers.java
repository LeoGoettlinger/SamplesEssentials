package de.theredend2000.advancedhunt.lib.cloud;

import de.theredend2000.advancedhunt.lib.cloud.caption.Caption;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.cloud.caption.StandardCaptionKeys;
import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.exception.ArgumentParseException;
import de.theredend2000.advancedhunt.lib.cloud.exception.CommandExecutionException;
import de.theredend2000.advancedhunt.lib.cloud.exception.InvalidCommandSenderException;
import de.theredend2000.advancedhunt.lib.cloud.exception.InvalidSyntaxException;
import de.theredend2000.advancedhunt.lib.cloud.exception.NoPermissionException;
import de.theredend2000.advancedhunt.lib.cloud.exception.NoSuchCommandException;
import de.theredend2000.advancedhunt.lib.cloud.exception.handling.ExceptionContext;
import de.theredend2000.advancedhunt.lib.cloud.exception.handling.ExceptionController;
import de.theredend2000.advancedhunt.lib.cloud.type.tuple.Pair;
import de.theredend2000.advancedhunt.lib.cloud.type.tuple.Triplet;
import de.theredend2000.advancedhunt.lib.cloud.util.TypeUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL
)
final class DefaultExceptionHandlers<C> {
   private final Consumer<Triplet<CommandContext<C>, Caption, List<CaptionVariable>>> messageSender;
   private final Consumer<Pair<String, Throwable>> logger;
   private final ExceptionController<C> exceptionController;

   DefaultExceptionHandlers(
      @NonNull final Consumer<Triplet<CommandContext<C>, Caption, List<CaptionVariable>>> messageSender,
      @NonNull final Consumer<Pair<String, Throwable>> logger,
      @NonNull final ExceptionController<C> exceptionController
   ) {
      this.messageSender = Objects.requireNonNull(messageSender, "messageSender");
      this.logger = Objects.requireNonNull(logger, "logger");
      this.exceptionController = Objects.requireNonNull(exceptionController, "exceptionController");
   }

   void register() {
      this.exceptionController.registerHandler(Throwable.class, context -> {
         this.sendMessage(context, StandardCaptionKeys.EXCEPTION_UNEXPECTED);
         this.log("An unhandled exception was thrown during command execution", context.exception());
      });
      this.exceptionController.registerHandler(CommandExecutionException.class, context -> {
         this.sendMessage(context, StandardCaptionKeys.EXCEPTION_UNEXPECTED);
         this.log("Exception executing command handler", context.exception().getCause());
      });
      this.exceptionController
         .registerHandler(
            ArgumentParseException.class,
            context -> this.sendMessage(
               context, StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT, CaptionVariable.of("cause", context.exception().getCause().getMessage())
            )
         );
      this.exceptionController
         .registerHandler(
            NoSuchCommandException.class,
            context -> this.sendMessage(
               context, StandardCaptionKeys.EXCEPTION_NO_SUCH_COMMAND, CaptionVariable.of("command", context.exception().suppliedCommand())
            )
         );
      this.exceptionController
         .registerHandler(
            NoPermissionException.class,
            context -> this.sendMessage(
               context,
               StandardCaptionKeys.EXCEPTION_NO_PERMISSION,
               CaptionVariable.of("permission", context.exception().permissionResult().permission().permissionString())
            )
         );
      this.exceptionController
         .registerHandler(
            InvalidCommandSenderException.class,
            context -> {
               boolean multiple = context.exception().requiredSenderTypes().size() != 1;
               String expected = multiple
                  ? context.exception().requiredSenderTypes().stream().map(TypeUtils::simpleName).collect(Collectors.joining(", "))
                  : TypeUtils.simpleName(context.exception().requiredSenderTypes().iterator().next());
               this.sendMessage(
                  context,
                  multiple ? StandardCaptionKeys.EXCEPTION_INVALID_SENDER_LIST : StandardCaptionKeys.EXCEPTION_INVALID_SENDER,
                  CaptionVariable.of("actual", context.context().sender().getClass().getSimpleName()),
                  CaptionVariable.of("expected", expected)
               );
            }
         );
      this.exceptionController
         .registerHandler(
            InvalidSyntaxException.class,
            context -> this.sendMessage(
               context, StandardCaptionKeys.EXCEPTION_INVALID_SYNTAX, CaptionVariable.of("syntax", context.exception().correctSyntax())
            )
         );
   }

   private void sendMessage(@NonNull final ExceptionContext<C, ?> context, @NonNull final Caption caption, @NonNull final CaptionVariable... variables) {
      this.sendMessage(context.context(), caption, variables);
   }

   private void sendMessage(@NonNull final CommandContext<C> context, @NonNull final Caption caption, @NonNull final CaptionVariable... variables) {
      this.messageSender.accept(Triplet.of(context, caption, Arrays.asList(variables)));
   }

   private void log(@NonNull final String message, @NonNull final Throwable exception) {
      this.logger.accept(Pair.of(message, exception));
   }
}
