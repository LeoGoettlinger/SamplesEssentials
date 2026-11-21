package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.CommandHandler;
import com.artillexstudios.axtrade.libs.lamp.command.ArgumentStack;
import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axtrade.libs.lamp.exception.CommandInvocationException;
import com.artillexstudios.axtrade.libs.lamp.exception.InvalidCommandException;
import com.artillexstudios.axtrade.libs.lamp.exception.InvalidNumberException;
import com.artillexstudios.axtrade.libs.lamp.exception.MissingArgumentException;
import com.artillexstudios.axtrade.libs.lamp.exception.NoSubcommandSpecifiedException;
import com.artillexstudios.axtrade.libs.lamp.exception.TooManyArgumentsException;
import com.artillexstudios.axtrade.libs.lamp.ktx.call.KotlinConstants;
import com.artillexstudios.axtrade.libs.lamp.process.ContextResolver;
import com.artillexstudios.axtrade.libs.lamp.process.ParameterResolver;
import com.artillexstudios.axtrade.libs.lamp.process.ParameterValidator;
import com.artillexstudios.axtrade.libs.lamp.process.ValueResolver;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public final class BaseCommandDispatcher {
   private final BaseCommandHandler handler;

   public BaseCommandDispatcher(BaseCommandHandler handler) {
      this.handler = handler;
   }

   public Object eval(@NotNull CommandActor actor, @NotNull ArgumentStack arguments) {
      try {
         MutableCommandPath path = MutableCommandPath.empty();
         String argument = arguments.getFirst();
         path.add(argument);
         CommandExecutable executable = this.handler.executables.get(path);
         if (executable != null) {
            arguments.removeFirst();
            return this.execute(executable, actor, arguments);
         } else {
            BaseCommandCategory category = this.handler.categories.get(path);
            if (category != null) {
               arguments.removeFirst();
               return this.searchCategory(actor, category, path, arguments);
            } else {
               throw new InvalidCommandException(path, path.getFirst());
            }
         }
      } catch (Throwable var7) {
         this.handler.getExceptionHandler().handleException(var7, actor);
         return null;
      }
   }

   private Object searchCategory(CommandActor actor, BaseCommandCategory category, MutableCommandPath path, ArgumentStack arguments) {
      if (!arguments.isEmpty()) {
         path.add(arguments.getFirst());
      }

      CommandExecutable executable = (CommandExecutable)category.commands.get(path);
      if (executable != null) {
         arguments.removeFirst();
         return this.execute(executable, actor, arguments);
      } else {
         category.checkPermission(actor);
         BaseCommandCategory found = (BaseCommandCategory)category.getCategories().get(path);
         if (found == null) {
            if (category.defaultAction == null) {
               throw new NoSubcommandSpecifiedException(category);
            } else {
               return this.execute(category.defaultAction, actor, arguments);
            }
         } else {
            arguments.removeFirst();
            return this.searchCategory(actor, found, path, arguments);
         }
      }
   }

   private Object execute(@NotNull CommandExecutable executable, @NotNull CommandActor actor, @NotNull ArgumentStack args) {
      List<String> input = args.asImmutableCopy();
      this.handler.conditions.forEach(condition -> condition.test(actor, executable, args.asImmutableView()));
      Object[] methodArguments = this.getMethodArguments(executable, actor, args, input);
      if (!args.isEmpty() && this.handler.failOnExtra) {
         throw new TooManyArgumentsException(executable, args);
      } else {
         Object result;
         try {
            result = executable.methodCaller.call(methodArguments);
         } catch (Throwable var8) {
            throw new CommandInvocationException(executable, var8);
         }

         executable.responseHandler.handleResponse(result, actor, executable);
         return result;
      }
   }

   private Object[] getMethodArguments(CommandExecutable executable, CommandActor actor, ArgumentStack args, List<String> input) {
      try {
         Object[] values = new Object[executable.parameters.size()];

         for (CommandParameter parameter : executable.parameters) {
            if (ArgumentStack.class.isAssignableFrom(parameter.getType())) {
               values[parameter.getMethodIndex()] = args;
            } else if (parameter.isSwitch()) {
               this.handleSwitch(args, values, parameter);
            } else if (parameter.isFlag()) {
               this.handleFlag(input, actor, args, values, parameter);
            }
         }

         for (CommandParameter parameterx : executable.parameters) {
            if (ArgumentStack.class.isAssignableFrom(parameterx.getType())) {
               values[parameterx.getMethodIndex()] = args;
            } else if (!parameterx.isSwitch() && !parameterx.isFlag()) {
               ParameterResolver<?> resolver = parameterx.getResolver();
               if (!resolver.mutatesArguments()) {
                  parameterx.checkPermission(actor);
                  BaseCommandDispatcher.ContextResolverContext cxt = new BaseCommandDispatcher.ContextResolverContext(input, actor, parameterx, values);
                  Object value = resolver.resolve(cxt);

                  for (ParameterValidator<Object> v : parameterx.getValidators()) {
                     v.validate(value, parameterx, actor);
                  }

                  values[parameterx.getMethodIndex()] = value;
               } else {
                  boolean added = this.addDefaultValues(args, parameterx, values);
                  if (added) {
                     parameterx.checkPermission(actor);
                     BaseCommandDispatcher.ValueContextR cxt = new BaseCommandDispatcher.ValueContextR(input, actor, parameterx, values, args);
                     Object value = resolver.resolve(cxt);

                     for (ParameterValidator<Object> v : parameterx.getValidators()) {
                        v.validate(value, parameterx, actor);
                     }

                     values[parameterx.getMethodIndex()] = value;
                  }
               }
            }
         }

         return values;
      } catch (Throwable var14) {
         throw var14;
      }
   }

   private boolean addDefaultValues(ArgumentStack args, CommandParameter parameter, Object[] values) {
      if (args.isEmpty()) {
         if (parameter.isOptional() && parameter.getDefaultValue().isEmpty()) {
            if (KotlinConstants.isKotlinClass(parameter.getJavaParameter().getDeclaringExecutable().getDeclaringClass())) {
               values[parameter.getMethodIndex()] = KotlinConstants.ABSENT_VALUE;
            } else {
               values[parameter.getMethodIndex()] = KotlinConstants.defaultPrimitiveValue(parameter.getType());
            }

            return false;
         } else if (!parameter.getDefaultValue().isEmpty()) {
            args.addAll(parameter.getDefaultValue());
            return true;
         } else {
            throw new MissingArgumentException(parameter);
         }
      } else {
         return true;
      }
   }

   private void handleSwitch(ArgumentStack args, Object[] values, CommandParameter parameter) {
      boolean provided = args.remove(this.handler.switchPrefix + parameter.getSwitchName());
      if (!provided) {
         values[parameter.getMethodIndex()] = parameter.getDefaultSwitch();
      } else {
         values[parameter.getMethodIndex()] = true;
      }
   }

   private void handleFlag(List<String> input, CommandActor actor, ArgumentStack args, Object[] values, CommandParameter parameter) {
      String lookup = this.handler.getFlagPrefix() + parameter.getFlagName();
      int index = args.indexOf(lookup);
      ArgumentStack flagArguments;
      if (index == -1) {
         if (!parameter.isOptional()) {
            throw new MissingArgumentException(parameter);
         }

         if (parameter.getDefaultValue().isEmpty()) {
            for (ParameterValidator<Object> v : parameter.getValidators()) {
               v.validate(null, parameter, actor);
            }

            if (KotlinConstants.isKotlinClass(parameter.getJavaParameter().getDeclaringExecutable().getDeclaringClass())) {
               values[parameter.getMethodIndex()] = KotlinConstants.ABSENT_VALUE;
            } else {
               values[parameter.getMethodIndex()] = KotlinConstants.defaultPrimitiveValue(parameter.getType());
            }

            return;
         }

         args.add(lookup);
         args.addAll(parameter.getDefaultValue());
         index = args.indexOf(lookup);
         args.remove(index);
         flagArguments = ArgumentStack.parse(args.remove(index));
      } else {
         args.remove(index);
         if (index >= args.size()) {
            throw new MissingArgumentException(parameter);
         }

         flagArguments = ArgumentStack.copyExact(args.remove(index));
      }

      BaseCommandDispatcher.ValueContextR contextR = new BaseCommandDispatcher.ValueContextR(input, actor, parameter, values, flagArguments);
      Object value = parameter.getResolver().resolve(contextR);

      for (ParameterValidator<Object> v : parameter.getValidators()) {
         v.validate(value, parameter, actor);
      }

      values[parameter.getMethodIndex()] = value;
   }

   private static final class ContextResolverContext extends BaseCommandDispatcher.ParamResolverContext implements ContextResolver.ContextResolverContext {
      public ContextResolverContext(List<String> input, CommandActor actor, CommandParameter parameter, Object[] resolved) {
         super(input, actor, parameter, resolved);
      }
   }

   private abstract static class ParamResolverContext implements ParameterResolver.ParameterResolverContext {
      private final List<String> input;
      private final CommandActor actor;
      private final CommandParameter parameter;
      private final Object[] resolved;

      @NotNull
      @Unmodifiable
      @Override
      public List<String> input() {
         return this.input;
      }

      @NotNull
      @Override
      public <A extends CommandActor> A actor() {
         return (A)this.actor;
      }

      @NotNull
      @Override
      public CommandParameter parameter() {
         return this.parameter;
      }

      @NotNull
      @Override
      public ExecutableCommand command() {
         return this.parameter.getDeclaringCommand();
      }

      @NotNull
      @Override
      public CommandHandler commandHandler() {
         return this.parameter.getCommandHandler();
      }

      @NotNull
      @Override
      public <T> T getResolvedParameter(@NotNull CommandParameter parameter) {
         try {
            return (T)this.resolved[parameter.getMethodIndex()];
         } catch (Throwable var3) {
            throw new IllegalArgumentException("This parameter has not been resolved yet!");
         }
      }

      @NotNull
      @Override
      public <T> T getResolvedArgument(@NotNull Class<T> type) {
         for (Object o : this.resolved) {
            if (type.isInstance(o)) {
               return (T)o;
            }
         }

         throw new IllegalArgumentException("This parameter has not been resolved yet!");
      }

      public ParamResolverContext(List<String> input, CommandActor actor, CommandParameter parameter, Object[] resolved) {
         this.input = input;
         this.actor = actor;
         this.parameter = parameter;
         this.resolved = resolved;
      }
   }

   static final class ValueContextR extends BaseCommandDispatcher.ParamResolverContext implements ValueResolver.ValueResolverContext {
      ArgumentStack argumentStack;

      public ValueContextR(List<String> input, CommandActor actor, CommandParameter parameter, Object[] resolved, ArgumentStack argumentStack) {
         super(input, actor, parameter, resolved);
         this.argumentStack = argumentStack;
      }

      @Override
      public ArgumentStack arguments() {
         return this.argumentStack;
      }

      @Override
      public String popForParameter() {
         return this.arguments().popForParameter(this.parameter());
      }

      @Override
      public String pop() {
         return this.arguments().pop();
      }

      private <T> T num(Function<String, T> f) {
         String input = this.pop();

         try {
            return (T)(input.startsWith("0x") ? Integer.valueOf(input.substring(2), 16) : f.apply(input));
         } catch (NumberFormatException var4) {
            throw new InvalidNumberException(this.parameter(), input);
         }
      }

      @Override
      public int popInt() {
         return this.num(Integer::parseInt);
      }

      @Override
      public double popDouble() {
         return this.num(Double::parseDouble);
      }

      @Override
      public byte popByte() {
         return this.num(Byte::parseByte);
      }

      @Override
      public short popShort() {
         return this.num(Short::parseShort);
      }

      @Override
      public float popFloat() {
         return this.num(Float::parseFloat);
      }

      @Override
      public long popLong() {
         return this.num(Long::parseLong);
      }
   }
}
