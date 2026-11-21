package me.byteful.plugin.leveltools.libs.lamp.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.core.reflect.MethodCaller;
import me.byteful.plugin.leveltools.libs.lamp.core.reflect.MethodCallerFactory;
import me.byteful.plugin.leveltools.libs.lamp.util.ClassMap;
import org.jetbrains.annotations.NotNull;

public abstract class CommandExceptionAdapter implements CommandExceptionHandler {
   private static final List<Method> IGNORED_METHODS = new ArrayList<>();
   private final ClassMap<CommandExceptionAdapter.MethodExceptionHandler> handlers = new ClassMap<>();
   private final CommandExceptionAdapter.MethodExceptionHandler unknownHandler = this::onUnhandledException;

   @CommandExceptionAdapter.Ignore
   public void onUnhandledException(@NotNull CommandActor actor, @NotNull Throwable throwable) {
   }

   public void missingArgument(@NotNull CommandActor actor, @NotNull MissingArgumentException exception) {
   }

   public void invalidEnumValue(@NotNull CommandActor actor, @NotNull EnumNotFoundException exception) {
   }

   public void invalidUUID(@NotNull CommandActor actor, @NotNull InvalidUUIDException exception) {
   }

   public void invalidNumber(@NotNull CommandActor actor, @NotNull InvalidNumberException exception) {
   }

   public void invalidURL(@NotNull CommandActor actor, @NotNull InvalidURLException exception) {
   }

   public void invalidBoolean(@NotNull CommandActor actor, @NotNull InvalidBooleanException exception) {
   }

   public void numberNotInRange(@NotNull CommandActor actor, @NotNull NumberNotInRangeException exception) {
   }

   public void noPermission(@NotNull CommandActor actor, @NotNull NoPermissionException exception) {
   }

   public void argumentParse(@NotNull CommandActor actor, @NotNull ArgumentParseException exception) {
   }

   public void commandInvocation(@NotNull CommandActor actor, @NotNull CommandInvocationException exception) {
   }

   public void tooManyArguments(@NotNull CommandActor actor, @NotNull TooManyArgumentsException exception) {
   }

   public void invalidCommand(@NotNull CommandActor actor, @NotNull InvalidCommandException exception) {
   }

   public void invalidSubcommand(@NotNull CommandActor actor, @NotNull InvalidSubcommandException exception) {
   }

   public void noSubcommandSpecified(@NotNull CommandActor actor, @NotNull NoSubcommandSpecifiedException exception) {
   }

   public void cooldown(@NotNull CommandActor actor, @NotNull CooldownException exception) {
   }

   public void invalidHelpPage(@NotNull CommandActor actor, @NotNull InvalidHelpPageException exception) {
   }

   public void sendableException(@NotNull CommandActor actor, @NotNull SendableException exception) {
   }

   @CommandExceptionAdapter.Ignore
   @Override
   public void handleException(@NotNull Throwable throwable, @NotNull CommandActor actor) {
      CommandExceptionAdapter.MethodExceptionHandler handler = this.handlers.getFlexibleOrDefault(throwable.getClass(), this.unknownHandler);
      if (handler == this.unknownHandler && throwable instanceof SelfHandledException) {
         ((SelfHandledException)throwable).handle(actor);
      } else {
         handler.handle(actor, throwable);
      }
   }

   public CommandExceptionAdapter() {
      for (Method m : this.getClass().getMethods()) {
         this.register(m);
      }
   }

   private void register(@NotNull Method method) {
      try {
         if (CommandExceptionAdapter.class.isAssignableFrom(method.getDeclaringClass())) {
            if (method.getParameterCount() == 2) {
               if (!method.isAnnotationPresent(CommandExceptionAdapter.Ignore.class)) {
                  for (Method ignoredMethod : IGNORED_METHODS) {
                     if (method.getName().equals(ignoredMethod.getName())
                        && Arrays.equals((Object[])method.getParameterTypes(), (Object[])ignoredMethod.getParameterTypes())) {
                        return;
                     }
                  }

                  Parameter[] parameters = method.getParameters();
                  Class<?> firstType = parameters[0].getType();
                  Class<?> secondType = parameters[1].getType();
                  Class<?> exceptionType;
                  CommandExceptionAdapter.MethodExceptionHandler handler;
                  if (CommandActor.class.isAssignableFrom(firstType) && Throwable.class.isAssignableFrom(secondType)) {
                     exceptionType = secondType;
                     MethodCaller.BoundMethodCaller caller = MethodCallerFactory.defaultFactory().createFor(method).bindTo(this);
                     handler = (xva$0, xva$1) -> caller.call(xva$0, xva$1);
                  } else {
                     if (!Throwable.class.isAssignableFrom(firstType) || !CommandActor.class.isAssignableFrom(secondType)) {
                        return;
                     }

                     exceptionType = firstType;
                     MethodCaller.BoundMethodCaller caller = MethodCallerFactory.defaultFactory().createFor(method).bindTo(this);
                     handler = (actor, throwable) -> caller.call(throwable, actor);
                  }

                  this.handlers.add(exceptionType, handler);
               }
            }
         }
      } catch (Throwable var8) {
         throw var8;
      }
   }

   static {
      for (Method method : CommandExceptionAdapter.class.getDeclaredMethods()) {
         if (method.getParameterCount() == 2 && method.isAnnotationPresent(CommandExceptionAdapter.Ignore.class)) {
            IGNORED_METHODS.add(method);
         }
      }
   }

   @Target({ElementType.METHOD})
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Ignore {
   }

   private interface MethodExceptionHandler {
      void handle(@NotNull CommandActor var1, @NotNull Throwable var2);
   }
}
