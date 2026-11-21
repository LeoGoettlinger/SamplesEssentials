package com.sk89q.worldedit.command.argument;

import com.google.common.reflect.TypeToken;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.internal.annotation.VertHeight;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverters;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

public class HeightConverter implements ArgumentConverter<Integer> {
   public static final String DEFAULT_VALUE = "default-vertical-height";
   private static final ArgumentConverter<Integer> INT_CONVERTER = ArgumentConverters.get(TypeToken.of(int.class));

   public static void register(CommandManager commandManager) {
      commandManager.registerConverter(Key.of(int.class, VertHeight.class), new HeightConverter());
   }

   private HeightConverter() {
   }

   @Override
   public Component describeAcceptableArguments() {
      return TextComponent.of("Any integer");
   }

   @Override
   public ConversionResult<Integer> convert(String argument, InjectedValueAccess context) {
      return (ConversionResult<Integer>)("default-vertical-height".equals(argument)
         ? SuccessfulConversion.fromSingle(WorldEdit.getInstance().getConfiguration().defaultVerticalHeight)
         : INT_CONVERTER.convert(argument, context));
   }
}
