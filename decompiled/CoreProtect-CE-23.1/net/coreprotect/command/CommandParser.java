package net.coreprotect.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.coreprotect.command.parser.ActionParser;
import net.coreprotect.command.parser.LocationParser;
import net.coreprotect.command.parser.MaterialParser;
import net.coreprotect.command.parser.TimeParser;
import net.coreprotect.command.parser.UserParser;
import net.coreprotect.command.parser.WorldParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class CommandParser {
   protected static String[] parsePage(String[] argumentArray) {
      return ActionParser.parsePage(argumentArray);
   }

   protected static List<Integer> parseAction(String[] inputArguments) {
      return ActionParser.parseAction(inputArguments);
   }

   protected static Location parseCoordinates(Location location, String[] inputArguments, int worldId) {
      return LocationParser.parseCoordinates(location, inputArguments, worldId);
   }

   protected static boolean parseCount(String[] inputArguments) {
      return ActionParser.parseCount(inputArguments);
   }

   protected static Map<Object, Boolean> parseExcluded(CommandSender player, String[] inputArguments, List<Integer> argAction) {
      return MaterialParser.parseExcluded(player, inputArguments, argAction);
   }

   protected static List<String> parseExcludedUsers(CommandSender player, String[] inputArguments) {
      return UserParser.parseExcludedUsers(player, inputArguments);
   }

   protected static boolean parseForceGlobal(String[] inputArguments) {
      return WorldParser.parseForceGlobal(inputArguments);
   }

   protected static Location parseLocation(CommandSender user, String[] argumentArray) {
      return LocationParser.parseLocation(user, argumentArray);
   }

   protected static int parseNoisy(String[] inputArguments) {
      return ActionParser.parseNoisy(inputArguments);
   }

   protected static int parsePreview(String[] inputArguments) {
      return ActionParser.parsePreview(inputArguments);
   }

   protected static Integer[] parseRadius(String[] inputArguments, CommandSender user, Location location) {
      Integer[] result = LocationParser.parseRadius(inputArguments, user, location);
      if (result == null && WorldParser.parseWorldEdit(inputArguments) && user.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
         Integer[] worldEditResult = WorldEditHandler.runWorldEditCommand(user);
         if (worldEditResult != null) {
            result = worldEditResult;
         }
      }

      return result;
   }

   protected static List<Object> parseRestricted(CommandSender player, String[] inputArguments, List<Integer> argAction) {
      return MaterialParser.parseRestricted(player, inputArguments, argAction);
   }

   protected static long[] parseTime(String[] inputArguments) {
      return TimeParser.parseTime(inputArguments);
   }

   protected static String parseTimeString(String[] inputArguments) {
      return TimeParser.parseTimeString(inputArguments);
   }

   protected static int parseRows(String[] inputArguments) {
      return TimeParser.parseRows(inputArguments);
   }

   protected static int parseWorld(String[] inputArguments, boolean processWorldEdit, boolean requireLoaded) {
      return WorldParser.parseWorld(inputArguments, processWorldEdit, requireLoaded);
   }

   protected static boolean parseWorldEdit(String[] inputArguments) {
      return WorldParser.parseWorldEdit(inputArguments);
   }

   protected static String parseWorldName(String[] inputArguments, boolean processWorldEdit) {
      return WorldParser.parseWorldName(inputArguments, processWorldEdit);
   }

   protected static Map<String, Set<Material>> getTags() {
      return MaterialParser.getTags();
   }

   protected static boolean checkTags(String argument) {
      return MaterialParser.checkTags(argument);
   }

   protected static boolean checkTags(String argument, Map<Object, Boolean> list) {
      return MaterialParser.checkTags(argument, list);
   }

   protected static boolean checkTags(String argument, List<Object> list) {
      return MaterialParser.checkTags(argument, list);
   }

   protected static List<String> parseUsers(String[] inputArguments) {
      return UserParser.parseUsers(inputArguments);
   }

   private static String timeString(BigDecimal input) {
      return input.stripTrailingZeros().toPlainString();
   }
}
