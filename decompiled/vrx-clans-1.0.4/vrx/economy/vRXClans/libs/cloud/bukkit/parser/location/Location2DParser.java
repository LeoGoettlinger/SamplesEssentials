package vrx.economy.vRXClans.libs.cloud.bukkit.parser.location;

import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitCommandContextKeys;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;
import vrx.economy.vRXClans.libs.cloud.suggestion.BlockingSuggestionProvider;

public final class Location2DParser<C> implements ArgumentParser<C, Location2D>, BlockingSuggestionProvider.Strings<C> {
   private final LocationCoordinateParser<C> locationCoordinateParser = new LocationCoordinateParser<>();

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, Location2D> location2DParser() {
      return ParserDescriptor.of(new Location2DParser<>(), Location2D.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Location2D> location2DComponent() {
      return CommandComponent.<C, Location2D>builder().parser(location2DParser());
   }

   @Override
   public ArgumentParseResult<Location2D> parse(final CommandContext<C> commandContext, final CommandInput commandInput) {
      if (commandInput.remainingTokens() < 2) {
         return ArgumentParseResult.failure(
            new LocationParser.LocationParseException(
               commandContext, LocationParser.LocationParseException.FailureReason.WRONG_FORMAT, commandInput.remainingInput()
            )
         );
      } else {
         LocationCoordinate[] coordinates = new LocationCoordinate[2];

         for (int i = 0; i < 2; i++) {
            if (commandInput.peekString().isEmpty()) {
               return ArgumentParseResult.failure(
                  new LocationParser.LocationParseException(
                     commandContext, LocationParser.LocationParseException.FailureReason.WRONG_FORMAT, commandInput.remainingInput()
                  )
               );
            }

            ArgumentParseResult<LocationCoordinate> coordinate = this.locationCoordinateParser.parse(commandContext, commandInput);
            if (coordinate.failure().isPresent()) {
               return ArgumentParseResult.failure(coordinate.failure().get());
            }

            coordinates[i] = coordinate.parsedValue().orElseThrow(NullPointerException::new);
         }

         CommandSender bukkitSender = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
         Location originalLocation;
         if (bukkitSender instanceof BlockCommandSender) {
            originalLocation = ((BlockCommandSender)bukkitSender).getBlock().getLocation();
         } else if (bukkitSender instanceof Entity) {
            originalLocation = ((Entity)bukkitSender).getLocation();
         } else {
            originalLocation = new Location((World)Bukkit.getWorlds().get(0), 0.0, 0.0, 0.0);
         }

         if (coordinates[0].type() == LocationCoordinateType.LOCAL && coordinates[1].type() != LocationCoordinateType.LOCAL) {
            return ArgumentParseResult.failure(
               new LocationParser.LocationParseException(commandContext, LocationParser.LocationParseException.FailureReason.MIXED_LOCAL_ABSOLUTE, "")
            );
         } else {
            if (coordinates[0].type() == LocationCoordinateType.ABSOLUTE) {
               originalLocation.setX(coordinates[0].coordinate());
            } else if (coordinates[0].type() == LocationCoordinateType.RELATIVE) {
               originalLocation.add(coordinates[0].coordinate(), 0.0, 0.0);
            }

            if (coordinates[1].type() == LocationCoordinateType.ABSOLUTE) {
               originalLocation.setZ(coordinates[1].coordinate());
            } else {
               if (coordinates[1].type() != LocationCoordinateType.RELATIVE) {
                  Vector declaredPos = new Vector(coordinates[0].coordinate(), 0.0, coordinates[1].coordinate());
                  Location local = LocationParser.toLocalSpace(originalLocation, declaredPos);
                  return ArgumentParseResult.success(Location2D.from(originalLocation.getWorld(), local.getX(), local.getZ()));
               }

               originalLocation.add(0.0, 0.0, coordinates[1].coordinate());
            }

            return ArgumentParseResult.success(Location2D.from(originalLocation.getWorld(), originalLocation.getX(), originalLocation.getZ()));
         }
      }
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return LocationParser.getSuggestions(2, commandContext, input);
   }
}
