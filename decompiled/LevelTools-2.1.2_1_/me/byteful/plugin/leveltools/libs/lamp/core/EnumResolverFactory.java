package me.byteful.plugin.leveltools.libs.lamp.core;

import java.util.HashMap;
import java.util.Map;
import me.byteful.plugin.leveltools.libs.lamp.annotation.CaseSensitive;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandParameter;
import me.byteful.plugin.leveltools.libs.lamp.exception.EnumNotFoundException;
import me.byteful.plugin.leveltools.libs.lamp.process.ValueResolver;
import me.byteful.plugin.leveltools.libs.lamp.process.ValueResolverFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum EnumResolverFactory implements ValueResolverFactory {
   INSTANCE;

   @Nullable
   @Override
   public ValueResolver<?> create(@NotNull CommandParameter parameter) {
      Class<?> type = parameter.getType();
      if (!type.isEnum()) {
         return null;
      } else {
         Class<? extends Enum> enumType = type.asSubclass(Enum.class);
         Map<String, Enum<?>> values = new HashMap<>();
         boolean caseSensitive = parameter.hasAnnotation(CaseSensitive.class);

         for (Enum<?> enumConstant : enumType.getEnumConstants()) {
            if (caseSensitive) {
               values.put(enumConstant.name(), enumConstant);
            } else {
               values.put(enumConstant.name().toLowerCase(), enumConstant);
            }
         }

         return context -> {
            String value = context.pop();
            Enum<?> v = values.get(caseSensitive ? value : value.toLowerCase());
            if (v == null) {
               throw new EnumNotFoundException(parameter, value);
            } else {
               return v;
            }
         };
      }
   }
}
