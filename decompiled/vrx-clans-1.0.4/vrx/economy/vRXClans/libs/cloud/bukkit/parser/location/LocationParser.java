package vrx.economy.vRXClans.libs.cloud.bukkit.parser.location;

import java.util.List;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitCaptionKeys;
import vrx.economy.vRXClans.libs.cloud.bukkit.BukkitCommandContextKeys;
import vrx.economy.vRXClans.libs.cloud.caption.Caption;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionVariable;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.exception.parsing.ParserException;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;
import vrx.economy.vRXClans.libs.cloud.parser.standard.IntegerParser;
import vrx.economy.vRXClans.libs.cloud.suggestion.BlockingSuggestionProvider;
import vrx.economy.vRXClans.libs.cloud.type.range.Range;

public final class LocationParser<C> implements ArgumentParser<C, Location>, BlockingSuggestionProvider.Strings<C> {
   private static final Range<Integer> SUGGESTION_RANGE = Range.intRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
   private final LocationCoordinateParser<C> locationCoordinateParser = new LocationCoordinateParser<>();

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ParserDescriptor<C, Location> locationParser() {
      return ParserDescriptor.of(new LocationParser<>(), Location.class);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> CommandComponent.Builder<C, Location> locationComponent() {
      return CommandComponent.<C, Location>builder().parser(locationParser());
   }

   @Override
   public ArgumentParseResult<Location> parse(final CommandContext<C> commandContext, final CommandInput commandInput) {
      if (commandInput.remainingTokens() < 3) {
         return ArgumentParseResult.failure(
            new LocationParser.LocationParseException(
               commandContext, LocationParser.LocationParseException.FailureReason.WRONG_FORMAT, commandInput.remainingInput()
            )
         );
      } else {
         LocationCoordinate[] coordinates = new LocationCoordinate[3];

         for (int i = 0; i < 3; i++) {
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
         } else if (Bukkit.getWorlds().isEmpty()) {
            originalLocation = new Location(null, 0.0, 0.0, 0.0);
         } else {
            originalLocation = new Location((World)Bukkit.getWorlds().get(0), 0.0, 0.0, 0.0);
         }

         if (coordinates[0].type() == LocationCoordinateType.LOCAL == (coordinates[1].type() == LocationCoordinateType.LOCAL)
            && coordinates[0].type() == LocationCoordinateType.LOCAL == (coordinates[2].type() == LocationCoordinateType.LOCAL)) {
            if (coordinates[0].type() == LocationCoordinateType.ABSOLUTE) {
               originalLocation.setX(coordinates[0].coordinate());
            } else if (coordinates[0].type() == LocationCoordinateType.RELATIVE) {
               originalLocation.add(coordinates[0].coordinate(), 0.0, 0.0);
            }

            if (coordinates[1].type() == LocationCoordinateType.ABSOLUTE) {
               originalLocation.setY(coordinates[1].coordinate());
            } else if (coordinates[1].type() == LocationCoordinateType.RELATIVE) {
               originalLocation.add(0.0, coordinates[1].coordinate(), 0.0);
            }

            if (coordinates[2].type() == LocationCoordinateType.ABSOLUTE) {
               originalLocation.setZ(coordinates[2].coordinate());
            } else {
               if (coordinates[2].type() != LocationCoordinateType.RELATIVE) {
                  Vector declaredPos = new Vector(coordinates[0].coordinate(), coordinates[1].coordinate(), coordinates[2].coordinate());
                  return ArgumentParseResult.success(toLocalSpace(originalLocation, declaredPos));
               }

               originalLocation.add(0.0, 0.0, coordinates[2].coordinate());
            }

            return ArgumentParseResult.success(originalLocation);
         } else {
            return ArgumentParseResult.failure(
               new LocationParser.LocationParseException(commandContext, LocationParser.LocationParseException.FailureReason.MIXED_LOCAL_ABSOLUTE, "")
            );
         }
      }
   }

   @NonNull
   static Location toLocalSpace(@NonNull final Location originalLocation, @NonNull final Vector declaredPos) {
      double cosYaw = Math.cos(toRadians(originalLocation.getYaw() + 90.0F));
      double sinYaw = Math.sin(toRadians(originalLocation.getYaw() + 90.0F));
      double cosPitch = Math.cos(toRadians(-originalLocation.getPitch()));
      double sinPitch = Math.sin(toRadians(-originalLocation.getPitch()));
      double cosNegYaw = Math.cos(toRadians(-originalLocation.getPitch() + 90.0F));
      double sinNegYaw = Math.sin(toRadians(-originalLocation.getPitch() + 90.0F));
      Vector zModifier = new Vector(cosYaw * cosPitch, sinPitch, sinYaw * cosPitch);
      Vector yModifier = new Vector(cosYaw * cosNegYaw, sinNegYaw, sinYaw * cosNegYaw);
      Vector xModifier = zModifier.crossProduct(yModifier).multiply(-1);
      double xOffset = dotProduct(declaredPos, xModifier.getX(), yModifier.getX(), zModifier.getX());
      double yOffset = dotProduct(declaredPos, xModifier.getY(), yModifier.getY(), zModifier.getY());
      double zOffset = dotProduct(declaredPos, xModifier.getZ(), yModifier.getZ(), zModifier.getZ());
      return originalLocation.add(xOffset, yOffset, zOffset);
   }

   private static double dotProduct(final Vector location, final double x, final double y, final double z) {
      return location.getX() * x + location.getY() * y + location.getZ() * z;
   }

   private static float toRadians(final float degrees) {
      return degrees * (float) Math.PI / 180.0F;
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return getSuggestions(3, commandContext, input);
   }

   @NonNull
   static <C> List<String> getSuggestions(final int components, @NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      CommandInput inputCopy = input.copy();
      int idx = input.cursor();

      for (int i = 0; i < components; i++) {
         idx = input.cursor();
         if (!input.hasRemainingInput(true)) {
            break;
         }

         ArgumentParseResult<LocationCoordinate> coordinateResult = new LocationCoordinateParser<C>().parse(commandContext, input);
         if (coordinateResult.failure().isPresent()) {
            break;
         }
      }

      input.cursor(idx);
      if (input.hasRemainingInput() && (input.peek() == '~' || input.peek() == '^')) {
         input.read();
      }

      String prefix = inputCopy.difference(input, true);
      return IntegerParser.getSuggestions(SUGGESTION_RANGE, input).stream().map(string -> prefix + string).collect(Collectors.toList());
   }

   static class LocationParseException extends ParserException {
      protected LocationParseException(
         @NonNull final CommandContext<?> context, @NonNull final LocationParser.LocationParseException.FailureReason reason, @NonNull final String input
      ) {
         super(LocationParser.class, context, reason.caption(), CaptionVariable.of("input", input));
      }

      public static enum FailureReason {
         WRONG_FORMAT(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_LOCATION_INVALID_FORMAT),
         MIXED_LOCAL_ABSOLUTE(BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_LOCATION_MIXED_LOCAL_ABSOLUTE);

         private final Caption caption;

         private FailureReason(@NonNull final Caption caption) {
            this.caption = caption;
         }

         @NonNull
         public Caption caption() {
            return this.caption;
         }
      }
   }
}
