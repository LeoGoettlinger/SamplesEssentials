package com.artillexstudios.axafkzone.libs.lamp.core;

import com.artillexstudios.axafkzone.libs.lamp.annotation.Dependency;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandParameter;
import com.artillexstudios.axafkzone.libs.lamp.process.ContextResolver;
import com.artillexstudios.axafkzone.libs.lamp.process.ContextResolverFactory;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum DependencyResolverFactory implements ContextResolverFactory {
   INSTANCE;

   @Nullable
   @Override
   public ContextResolver<?> create(@NotNull CommandParameter parameter) {
      if (!parameter.hasAnnotation(Dependency.class)) {
         return null;
      } else {
         Supplier<?> value = parameter.getCommandHandler().getDependency(parameter.getType());
         if (value == null) {
            throw new IllegalArgumentException(
               "Unable to resolve dependency for parameter " + parameter.getName() + " in " + parameter.getDeclaringCommand().getPath().toRealString()
            );
         } else {
            return context -> value.get();
         }
      }
   }
}
