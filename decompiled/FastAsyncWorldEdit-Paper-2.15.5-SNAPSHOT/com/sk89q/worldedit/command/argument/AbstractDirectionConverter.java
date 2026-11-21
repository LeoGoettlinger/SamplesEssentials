package com.sk89q.worldedit.command.argument;

import com.google.auto.value.AutoAnnotation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.UnknownDirectionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.MultiDirection;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.List;
import javax.annotation.Nullable;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.converter.SuggestionHelper;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

public abstract class AbstractDirectionConverter<D> implements ArgumentConverter<D> {
   private static final ImmutableSet<String> ORTHOGONAL = ImmutableSet.of("north", "south", "east", "west", "up", "down", new String[0]);
   private static final ImmutableSet<String> RELATIVE = ImmutableSet.of("me", "forward", "back", "left", "right");
   private static final ImmutableSet<String> DIAGONAL = ImmutableSet.of("northeast", "northwest", "southeast", "southwest");
   private final WorldEdit worldEdit;
   private final boolean includeDiagonals;
   private final ImmutableList<String> suggestions;

   @AutoAnnotation
   private static Direction direction(boolean includeDiagonals) {
      return new AutoAnnotation_AbstractDirectionConverter_direction(includeDiagonals);
   }

   @AutoAnnotation
   private static MultiDirection multiDirection(boolean includeDiagonals) {
      return new AutoAnnotation_AbstractDirectionConverter_multiDirection(includeDiagonals);
   }

   protected static <D> void register(CommandManager commandManager, AbstractDirectionConverter<D> converter, Class<D> keyClass, boolean includeDiagonals) {
      commandManager.registerConverter(Key.of(keyClass, direction(includeDiagonals)), converter);
      commandManager.registerConverter(Key.of(keyClass, multiDirection(includeDiagonals)), CommaSeparatedValuesConverter.wrap(converter));
   }

   protected AbstractDirectionConverter(WorldEdit worldEdit, boolean includeDiagonals) {
      this.worldEdit = worldEdit;
      this.includeDiagonals = includeDiagonals;
      this.suggestions = ImmutableList.builder()
         .addAll(ORTHOGONAL)
         .addAll(RELATIVE)
         .addAll((Iterable)(includeDiagonals ? DIAGONAL : ImmutableList.of()))
         .build();
   }

   @Override
   public ConversionResult<D> convert(String argument, InjectedValueAccess context) {
      Player player = context.injectedValue(Key.of(Actor.class)).filter(Player.class::isInstance).map(Player.class::cast).orElse(null);

      try {
         return SuccessfulConversion.fromSingle(this.convertDirection(argument, player, this.includeDiagonals));
      } catch (Exception var5) {
         return FailedConversion.from(var5);
      }
   }

   protected abstract D convertDirection(String argument, @Nullable Player player, boolean includeDiagonals) throws UnknownDirectionException;

   @Override
   public Component describeAcceptableArguments() {
      return TextComponent.of("`me` to use facing direction, or any " + (this.includeDiagonals ? "direction" : "non-diagonal direction"));
   }

   @Override
   public List<String> getSuggestions(String input, InjectedValueAccess context) {
      return SuggestionHelper.limitByPrefix(this.suggestions.stream(), input);
   }

   protected WorldEdit getWorldEdit() {
      return this.worldEdit;
   }
}
