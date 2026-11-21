package com.artillexstudios.axtrade.libs.lamp.core;

import com.artillexstudios.axtrade.libs.lamp.annotation.WithNames;
import com.artillexstudios.axtrade.libs.lamp.autocomplete.SuggestionProvider;
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.process.ParameterResolver;
import com.artillexstudios.axtrade.libs.lamp.process.ParameterValidator;
import com.artillexstudios.axtrade.libs.lamp.util.Primitives;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

@ApiStatus.Internal
public final class EitherParameter extends ForwardingCommandParameter {
   private final CommandParameter delegate;
   private final Type type;
   private final Class<?> rawType;
   private final List<ParameterValidator<Object>> validators;
   private String name;
   private ParameterResolver<Object> resolver;
   private SuggestionProvider suggestionProvider;

   public EitherParameter(CommandParameter delegate, Type type) {
      this.delegate = delegate;
      this.type = type;
      this.name = delegate.getName();
      this.rawType = Primitives.getRawType(type);
      this.suggestionProvider = ((BaseAutoCompleter)delegate.getCommandHandler().getAutoCompleter()).getProvider(this);
      this.validators = new ArrayList<>(
         ((BaseCommandHandler)delegate.getCommandHandler()).validators.getFlexibleOrDefault(this.rawType, Collections.emptyList())
      );
   }

   @NotNull
   @Override
   public CommandParameter delegate() {
      return this.delegate;
   }

   @NotNull
   @Override
   public String getName() {
      return this.name;
   }

   @NotNull
   @Override
   public Class<?> getType() {
      return this.rawType;
   }

   @NotNull
   @Override
   public Type getFullType() {
      return this.type;
   }

   @NotNull
   @Override
   public <T> ParameterResolver<T> getResolver() {
      return (ParameterResolver<T>)this.resolver;
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

   public static Type[] getTypes(CommandParameter parameter) {
      Type type = parameter.getFullType();
      if (!(type instanceof ParameterizedType)) {
         throw new IllegalArgumentException("'Either' parameter does not specify types!");
      } else {
         return ((ParameterizedType)type).getActualTypeArguments();
      }
   }

   public static EitherParameter[] create(CommandParameter parameter) {
      Type[] types = getTypes(parameter);
      EitherParameter[] parameters = new EitherParameter[]{new EitherParameter(parameter, types[0]), new EitherParameter(parameter, types[1])};
      if (parameter.hasAnnotation(WithNames.class)) {
         String[] values = parameter.getAnnotation(WithNames.class).value();
         if (values.length != 2) {
            throw new IllegalArgumentException("@WithNames() must have exactly two values when used with Either!");
         }

         parameters[0].name = values[0];
         parameters[1].name = values[1];
      } else {
         parameters[0].name = parameter.getName() + " as " + parameters[0].getType().getSimpleName().toLowerCase();
         parameters[1].name = parameter.getName() + " as " + parameters[1].getType().getSimpleName().toLowerCase();
      }

      return parameters;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setResolver(ParameterResolver<Object> resolver) {
      this.resolver = resolver;
   }

   public void setSuggestionProvider(SuggestionProvider suggestionProvider) {
      this.suggestionProvider = suggestionProvider;
   }
}
