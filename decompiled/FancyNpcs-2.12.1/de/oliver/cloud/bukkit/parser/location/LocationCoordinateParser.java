package de.oliver.cloud.bukkit.parser.location;

import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.cloud.parser.ArgumentParseResult;
import de.oliver.cloud.parser.ArgumentParser;
import de.oliver.cloud.parser.standard.DoubleParser;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class LocationCoordinateParser<C> implements ArgumentParser<C, LocationCoordinate> {
   @NonNull
   @Override
   public ArgumentParseResult<LocationCoordinate> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.skipWhitespace().peekString();
      LocationCoordinateType locationCoordinateType;
      if (commandInput.peek() == '^') {
         locationCoordinateType = LocationCoordinateType.LOCAL;
         commandInput.moveCursor(1);
      } else if (commandInput.peek() == '~') {
         locationCoordinateType = LocationCoordinateType.RELATIVE;
         commandInput.moveCursor(1);
      } else {
         locationCoordinateType = LocationCoordinateType.ABSOLUTE;
      }

      double coordinate;
      try {
         boolean empty = commandInput.peekString().isEmpty() || commandInput.peek() == ' ';
         coordinate = empty ? 0.0 : commandInput.readDouble();
         if (commandInput.hasRemainingInput()) {
            commandInput.skipWhitespace();
         }
      } catch (Exception var8) {
         return ArgumentParseResult.failure(
            new DoubleParser.DoubleParseException(input, new DoubleParser(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), commandContext)
         );
      }

      return ArgumentParseResult.success(LocationCoordinate.of(locationCoordinateType, coordinate));
   }
}
