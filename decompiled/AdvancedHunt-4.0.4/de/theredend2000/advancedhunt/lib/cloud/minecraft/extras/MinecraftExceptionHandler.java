package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras;

import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.cloud.caption.StandardCaptionKeys;
import de.theredend2000.advancedhunt.lib.cloud.exception.ArgumentParseException;
import de.theredend2000.advancedhunt.lib.cloud.exception.CommandExecutionException;
import de.theredend2000.advancedhunt.lib.cloud.exception.InvalidCommandSenderException;
import de.theredend2000.advancedhunt.lib.cloud.exception.InvalidSyntaxException;
import de.theredend2000.advancedhunt.lib.cloud.exception.NoPermissionException;
import de.theredend2000.advancedhunt.lib.cloud.exception.handling.ExceptionContext;
import de.theredend2000.advancedhunt.lib.cloud.exception.parsing.ParserException;
import de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.caption.ComponentCaptionFormatter;
import de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.caption.RichVariable;
import de.theredend2000.advancedhunt.lib.cloud.util.TypeUtils;
import de.theredend2000.advancedhunt.lib.kyori.adventure.audience.Audience;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.JoinConfiguration;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.ClickEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.HoverEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.NamedTextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextDecoration;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.ComponentMessageThrowable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.returnsreceiver.qual.This;

public final class MinecraftExceptionHandler<C> {
   private static final Component NULL = Component.text("null");
   private final Map<Class<? extends Throwable>, MinecraftExceptionHandler.MessageFactory<C, ?>> componentBuilders = new HashMap<>();
   private final AudienceProvider<C> audienceProvider;
   private MinecraftExceptionHandler.Decorator<C> decorator = (formatter, ctx, msg) -> msg;
   private ComponentCaptionFormatter<C> captionFormatter = ComponentCaptionFormatter.placeholderReplacing();

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static <C> MinecraftExceptionHandler.MessageFactory<C, InvalidSyntaxException> createDefaultInvalidSyntaxHandler() {
      return (formatter, ctx) -> Component.text()
         .color(NamedTextColor.RED)
         .append(
            ctx.context()
               .formatCaption(
                  formatter,
                  StandardCaptionKeys.EXCEPTION_INVALID_SYNTAX,
                  RichVariable.of(
                     "syntax",
                     ComponentHelper.highlight(Component.text(String.format("/%s", ctx.exception().correctSyntax()), NamedTextColor.GRAY), NamedTextColor.WHITE)
                  )
               )
         );
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static <C> MinecraftExceptionHandler.MessageFactory<C, InvalidCommandSenderException> createDefaultInvalidSenderHandler() {
      return (formatter, ctx) -> {
         boolean multiple = ctx.exception().requiredSenderTypes().size() > 1;
         Component expected = (Component)(multiple
            ? Component.join(
               JoinConfiguration.commas(true),
               ctx.exception()
                  .requiredSenderTypes()
                  .stream()
                  .map(TypeUtils::simpleName)
                  .map(name -> Component.text(name, NamedTextColor.GRAY))
                  .collect(Collectors.toList())
            )
            : Component.text(TypeUtils.simpleName(ctx.exception().requiredSenderTypes().iterator().next()), NamedTextColor.GRAY));
         return Component.text()
            .color(NamedTextColor.RED)
            .append(
               ctx.context()
                  .formatCaption(
                     formatter,
                     multiple ? StandardCaptionKeys.EXCEPTION_INVALID_SENDER_LIST : StandardCaptionKeys.EXCEPTION_INVALID_SENDER,
                     RichVariable.of("actual", Component.text(TypeUtils.simpleName(ctx.context().sender().getClass()), NamedTextColor.GRAY)),
                     RichVariable.of("expected", expected)
                  )
            );
      };
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static <C> MinecraftExceptionHandler.MessageFactory<C, NoPermissionException> createDefaultNoPermissionHandler() {
      return (formatter, ctx) -> Component.text()
         .color(NamedTextColor.RED)
         .append(
            ctx.context()
               .formatCaption(
                  formatter,
                  StandardCaptionKeys.EXCEPTION_NO_PERMISSION,
                  CaptionVariable.of("permission", ctx.exception().permissionResult().permission().permissionString())
               )
         );
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static <C> MinecraftExceptionHandler.MessageFactory<C, ArgumentParseException> createDefaultArgumentParsingHandler() {
      return (formatter, ctx) -> Component.text()
         .color(NamedTextColor.RED)
         .append(
            ctx.context()
               .formatCaption(
                  formatter,
                  StandardCaptionKeys.EXCEPTION_INVALID_ARGUMENT,
                  RichVariable.of("cause", getMessage(formatter, ctx.exception().getCause()).colorIfAbsent(NamedTextColor.GRAY))
               )
         );
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static <C> Consumer<ExceptionContext<C, CommandExecutionException>> createDefaultCommandExecutionLogger() {
      return ctx -> ctx.exception().getCause().printStackTrace();
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static <C> MinecraftExceptionHandler.MessageFactory<C, CommandExecutionException> createDefaultCommandExecutionHandler() {
      return createDefaultCommandExecutionHandler(createDefaultCommandExecutionLogger());
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static <C> MinecraftExceptionHandler.MessageFactory<C, CommandExecutionException> createDefaultCommandExecutionHandler(
      final Consumer<ExceptionContext<C, CommandExecutionException>> logger
   ) {
      return (formatter, ctx) -> {
         logger.accept(ctx);
         Throwable cause = ctx.exception().getCause();
         StringWriter writer = new StringWriter();
         cause.printStackTrace(new PrintWriter(writer));
         String stackTrace = writer.toString().replaceAll("\t", "    ");
         HoverEvent<Component> hover = HoverEvent.showText(
            Component.text()
               .append(getMessage(formatter, cause))
               .append(Component.newline())
               .append(Component.text(stackTrace))
               .append(Component.newline())
               .append(Component.text("    Click to copy", NamedTextColor.GRAY, TextDecoration.ITALIC))
         );
         ClickEvent click = ClickEvent.copyToClipboard(stackTrace);
         return Component.text()
            .append(ctx.context().formatCaption(formatter, StandardCaptionKeys.EXCEPTION_UNEXPECTED))
            .color(NamedTextColor.RED)
            .hoverEvent(hover)
            .clickEvent(click);
      };
   }

   private MinecraftExceptionHandler(final AudienceProvider<C> audienceProvider) {
      this.audienceProvider = audienceProvider;
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static <C> MinecraftExceptionHandler<C> create(final AudienceProvider<C> audienceProvider) {
      return new MinecraftExceptionHandler<>(audienceProvider);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public static <C extends Audience> MinecraftExceptionHandler<C> createNative() {
      return create(AudienceProvider.nativeAudience());
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @This
   @NonNull
   public MinecraftExceptionHandler<C> defaultInvalidSyntaxHandler() {
      return this.handler(InvalidSyntaxException.class, createDefaultInvalidSyntaxHandler());
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @This
   @NonNull
   public MinecraftExceptionHandler<C> defaultInvalidSenderHandler() {
      return this.handler(InvalidCommandSenderException.class, createDefaultInvalidSenderHandler());
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @This
   @NonNull
   public MinecraftExceptionHandler<C> defaultNoPermissionHandler() {
      return this.handler(NoPermissionException.class, createDefaultNoPermissionHandler());
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @This
   @NonNull
   public MinecraftExceptionHandler<C> defaultArgumentParsingHandler() {
      return this.handler(ArgumentParseException.class, createDefaultArgumentParsingHandler());
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @This
   @NonNull
   public MinecraftExceptionHandler<C> defaultCommandExecutionHandler() {
      return this.defaultCommandExecutionHandler(createDefaultCommandExecutionLogger());
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @This
   @NonNull
   public MinecraftExceptionHandler<C> defaultCommandExecutionHandler(@NonNull final Consumer<ExceptionContext<C, CommandExecutionException>> logger) {
      return this.handler(CommandExecutionException.class, createDefaultCommandExecutionHandler(logger));
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @This
   @NonNull
   public MinecraftExceptionHandler<C> defaultHandlers() {
      return this.defaultArgumentParsingHandler()
         .defaultInvalidSenderHandler()
         .defaultInvalidSyntaxHandler()
         .defaultNoPermissionHandler()
         .defaultCommandExecutionHandler();
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @This
   @NonNull
   public <T extends Throwable> MinecraftExceptionHandler<C> handler(
      @NonNull final Class<T> type, @NonNull final MinecraftExceptionHandler.MessageFactory<C, T> componentFactory
   ) {
      this.componentBuilders.put(type, componentFactory);
      return this;
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @This
   @NonNull
   public MinecraftExceptionHandler<C> decorator(@NonNull final MinecraftExceptionHandler.Decorator<C> decorator) {
      this.decorator = decorator;
      return this;
   }

   @This
   @NonNull
   public MinecraftExceptionHandler<C> captionFormatter(@NonNull final ComponentCaptionFormatter<C> captionFormatter) {
      this.captionFormatter = Objects.requireNonNull(captionFormatter, "captionFormatter");
      return this;
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @This
   @NonNull
   public MinecraftExceptionHandler<C> decorator(@NonNull final Function<Component, ComponentLike> decorator) {
      return this.decorator((formatter, ctx, message) -> decorator.apply(message));
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public void registerTo(@NonNull final CommandManager<C> manager) {
      this.componentBuilders.forEach((type, formatter) -> manager.exceptionController().registerHandler((Class<? extends Throwable>)type, ctx -> {
         ComponentLike message = formatter.message(this.captionFormatter, ctx);
         if (message != null) {
            this.audienceProvider.apply(ctx.context().sender()).sendMessage(this.decorator.decorate(this.captionFormatter, ctx, message.asComponent()));
         }
      }));
   }

   private static <C> Component getMessage(final ComponentCaptionFormatter<C> formatter, final Throwable throwable) {
      if (throwable instanceof ParserException) {
         return ((ParserException)throwable).formatCaption(formatter);
      } else {
         Component msg = ComponentMessageThrowable.getOrConvertMessage(
            MinecraftExceptionHandler.ComponentMessageThrowableConverterHolder.instance.maybeConvert(throwable)
         );
         return msg == null ? NULL : msg;
      }
   }

   @API(
      status = API.Status.INTERNAL
   )
   @FunctionalInterface
   public interface ComponentMessageThrowableConverter {
      Throwable maybeConvert(Throwable thr);
   }

   @API(
      status = API.Status.INTERNAL
   )
   public static final class ComponentMessageThrowableConverterHolder implements MinecraftExceptionHandler.ComponentMessageThrowableConverter {
      private static MinecraftExceptionHandler.ComponentMessageThrowableConverter instance = new MinecraftExceptionHandler.ComponentMessageThrowableConverterHolder();

      public static void converter(final MinecraftExceptionHandler.ComponentMessageThrowableConverter converter) {
         instance = Objects.requireNonNull(converter, "converter");
      }

      private ComponentMessageThrowableConverterHolder() {
      }

      @Override
      public Throwable maybeConvert(final Throwable thr) {
         return thr;
      }
   }

   @FunctionalInterface
   public interface Decorator<C> {
      @NonNull
      ComponentLike decorate(@NonNull ComponentCaptionFormatter<C> formatter, @NonNull ExceptionContext<C, ?> exceptionContext, @NonNull Component message);
   }

   @FunctionalInterface
   public interface MessageFactory<C, T extends Throwable> {
      @Nullable
      ComponentLike message(@NonNull ComponentCaptionFormatter<C> formatter, @NonNull ExceptionContext<C, T> exceptionContext);
   }
}
