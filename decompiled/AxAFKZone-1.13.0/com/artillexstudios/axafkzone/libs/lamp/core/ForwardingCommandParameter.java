package com.artillexstudios.axafkzone.libs.lamp.core;

import com.artillexstudios.axafkzone.libs.lamp.CommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.autocomplete.SuggestionProvider;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandActor;
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

abstract class ForwardingCommandParameter implements CommandParameter {
   protected ForwardingCommandParameter() {
   }

   @NotNull
   public abstract CommandParameter delegate();

   @NotNull
   @Override
   public String getName() {
      return this.delegate().getName();
   }

   @Nullable
   @Override
   public String getDescription() {
      return this.delegate().getDescription();
   }

   @Override
   public int getMethodIndex() {
      return this.delegate().getMethodIndex();
   }

   @Override
   public int getCommandIndex() {
      return this.delegate().getCommandIndex();
   }

   @NotNull
   @Override
   public Class<?> getType() {
      return this.delegate().getType();
   }

   @NotNull
   @Override
   public Type getFullType() {
      return this.delegate().getFullType();
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<String> getDefaultValue() {
      return this.delegate().getDefaultValue();
   }

   @Override
   public boolean consumesAllString() {
      return this.delegate().consumesAllString();
   }

   @Override
   public Parameter getJavaParameter() {
      return this.delegate().getJavaParameter();
   }

   @NotNull
   @Override
   public SuggestionProvider getSuggestionProvider() {
      return this.delegate().getSuggestionProvider();
   }

   @NotNull
   @Unmodifiable
   @Override
   public List<ParameterValidator<Object>> getValidators() {
      return this.delegate().getValidators();
   }

   @Override
   public boolean isOptional() {
      return this.delegate().isOptional();
   }

   @Override
   public boolean isLastInMethod() {
      return this.delegate().isLastInMethod();
   }

   @Override
   public boolean isSwitch() {
      return this.delegate().isSwitch();
   }

   @NotNull
   @Override
   public String getSwitchName() {
      return this.delegate().getSwitchName();
   }

   @Override
   public boolean isFlag() {
      return this.delegate().isFlag();
   }

   @NotNull
   @Override
   public String getFlagName() {
      return this.delegate().getFlagName();
   }

   @Override
   public boolean getDefaultSwitch() {
      return this.delegate().getDefaultSwitch();
   }

   @NotNull
   @Override
   public <T> ParameterResolver<T> getResolver() {
      return this.delegate().getResolver();
   }

   @NotNull
   @Override
   public CommandHandler getCommandHandler() {
      return this.delegate().getCommandHandler();
   }

   @NotNull
   @Override
   public ExecutableCommand getDeclaringCommand() {
      return this.delegate().getDeclaringCommand();
   }

   @NotNull
   @Override
   public CommandPermission getPermission() {
      return this.delegate().getPermission();
   }

   public int compareTo(@NotNull CommandParameter o) {
      return this.delegate().compareTo(o);
   }

   @Override
   public <A extends Annotation> A getAnnotation(@NotNull Class<A> annotation) {
      return this.delegate().getAnnotation(annotation);
   }

   @Override
   public boolean hasAnnotation(@NotNull Class<? extends Annotation> annotation) {
      return this.delegate().hasAnnotation(annotation);
   }

   @Override
   public boolean hasPermission(@NotNull CommandActor actor) {
      return this.delegate().hasPermission(actor);
   }

   @Override
   public void checkPermission(@NotNull CommandActor actor) {
      this.delegate().checkPermission(actor);
   }
}
