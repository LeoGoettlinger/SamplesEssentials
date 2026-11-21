package com.artillexstudios.axafkzone.libs.lamp.core;

import com.artillexstudios.axafkzone.libs.lamp.CommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.annotation.Flag;
import com.artillexstudios.axafkzone.libs.lamp.annotation.Switch;
import com.artillexstudios.axafkzone.libs.lamp.autocomplete.SuggestionProvider;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandPermission;
import com.artillexstudios.axafkzone.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axafkzone.libs.lamp.process.ParameterResolver;
import com.artillexstudios.axafkzone.libs.lamp.process.ParameterValidator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

final class BaseCommandParameter implements CommandParameter {
   String name;
   @Nullable
   private final String description;
   private final int index;
   @Unmodifiable
   private final List<String> def;
   private final boolean consumeString;
   private final boolean optional;
   private final ExecutableCommand parent;
   SuggestionProvider suggestionProvider;
   private final Parameter parameter;
   @Nullable
   CommandPermission permission;
   ParameterResolver resolver;
   int cindex = -1;
   @Nullable
   private final Switch switchAnn;
   @Nullable
   private final Flag flag;
   private final List<ParameterValidator<Object>> validators;

   @NotNull
   @Override
   public String getName() {
      return this.name;
   }

   @Nullable
   @Override
   public String getDescription() {
      return this.description;
   }

   @Override
   public int getMethodIndex() {
      return this.index;
   }

   @Override
   public int getCommandIndex() {
      return this.cindex;
   }

   @NotNull
   @Override
   public Class<?> getType() {
      return this.parameter.getType();
   }

   @NotNull
   @Override
   public Type getFullType() {
      return this.parameter.getParameterizedType();
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> getDefaultValue() {
      return this.def;
   }

   @Override
   public boolean consumesAllString() {
      return this.consumeString;
   }

   @Override
   public Parameter getJavaParameter() {
      return this.parameter;
   }

   @NotNull
   @Override
   public SuggestionProvider getSuggestionProvider() {
      return this.suggestionProvider;
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<ParameterValidator<Object>> getValidators() {
      return this.validators;
   }

   @Override
   public boolean isOptional() {
      return this.optional || this.isSwitch();
   }

   @Override
   public boolean isLastInMethod() {
      return this.getMethodIndex() + 1 == this.parameter.getDeclaringExecutable().getParameterCount();
   }

   @Override
   public <A extends Annotation> A getAnnotation(@NotNull Class<A> annotation) {
      return this.parameter.getAnnotation(annotation);
   }

   @Override
   public boolean isSwitch() {
      return this.switchAnn != null;
   }

   @NotNull
   @Override
   public String getSwitchName() {
      if (!this.isSwitch()) {
         throw new IllegalStateException("Not a switch.");
      } else {
         return this.switchAnn.value().isEmpty() ? this.getName() : this.switchAnn.value();
      }
   }

   @Override
   public boolean isFlag() {
      return this.flag != null;
   }

   @NotNull
   @Override
   public String getFlagName() {
      if (!this.isFlag()) {
         throw new IllegalStateException("Not a flag.");
      } else {
         return this.flag.value().isEmpty() ? this.getName() : this.flag.value();
      }
   }

   @Override
   public boolean getDefaultSwitch() {
      if (!this.isSwitch()) {
         throw new IllegalStateException("Not a switch.");
      } else {
         return this.switchAnn.defaultValue();
      }
   }

   @Override
   public boolean hasAnnotation(@NotNull Class<? extends Annotation> annotation) {
      return this.parameter.isAnnotationPresent(annotation);
   }

   @NotNull
   @Override
   public <T> ParameterResolver<T> getResolver() {
      return this.resolver;
   }

   @NotNull
   @Override
   public CommandHandler getCommandHandler() {
      return this.parent.getCommandHandler();
   }

   @NotNull
   @Override
   public ExecutableCommand getDeclaringCommand() {
      return this.parent;
   }

   @NotNull
   @Override
   public CommandPermission getPermission() {
      return this.permission == null ? this.getDeclaringCommand().getPermission() : this.permission;
   }

   public int compareTo(@NotNull CommandParameter o) {
      if (this.isFlag() && o.isSwitch()) {
         return 1;
      } else {
         return this.isSwitch() && o.isFlag() ? -1 : Integer.compare(this.getCommandIndex(), o.getCommandIndex());
      }
   }

   public BaseCommandParameter(
      @Nullable String description,
      int index,
      List<String> def,
      boolean consumeString,
      boolean optional,
      ExecutableCommand parent,
      Parameter parameter,
      @Nullable Switch switchAnn,
      @Nullable Flag flag,
      List<ParameterValidator<Object>> validators
   ) {
      this.description = description;
      this.index = index;
      this.def = def;
      this.consumeString = consumeString;
      this.optional = optional;
      this.parent = parent;
      this.parameter = parameter;
      this.switchAnn = switchAnn;
      this.flag = flag;
      this.validators = validators;
   }
}
