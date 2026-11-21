package net.coreprotect.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.coreprotect.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class TabHandler implements TabCompleter {
   private static final String[] HELP = new String[]{
      "inspect", "rollback", "restore", "lookup", "purge", "teleport", "status", "params", "users", "time", "radius", "action", "include", "exclude"
   };
   private static final String[] PARAMS = new String[]{"user:", "time:", "radius:", "action:", "include:", "exclude:", "#container"};
   private static final String[] ACTIONS = new String[]{
      "block",
      "+block",
      "-block",
      "click",
      "kill",
      "+container",
      "-container",
      "container",
      "chat",
      "command",
      "+inventory",
      "-inventory",
      "inventory",
      "item",
      "+item",
      "-item",
      "sign",
      "session",
      "+session",
      "-session",
      "username"
   };
   private static final String[] NUMBERS = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
   private static final String[] TIMES = new String[]{"w", "d", "h", "m", "s"};
   private static ArrayList<String> materials = null;

   public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
      if (sender instanceof Player && args.length != 0) {
         if (args.length == 1) {
            return this.getFirstLevelCompletions(sender, args[0]);
         } else {
            String argument0 = args[0].toLowerCase(Locale.ROOT);
            String currentArg = args[args.length - 1].toLowerCase(Locale.ROOT).trim();
            String lastArg = args.length > 1 ? args[args.length - 2].toLowerCase(Locale.ROOT).trim() : "";
            TabHandler.ParamState paramState = this.getParamState(args);
            if (this.isActionParam(lastArg, currentArg) && this.hasLookupPermission(sender)) {
               return this.handleActionParamCompletions(currentArg, lastArg);
            } else if (this.isUserParam(lastArg, currentArg) && this.hasLookupPermission(sender)) {
               return this.handleUserParamCompletions(currentArg, lastArg);
            } else if (this.isTimeParam(lastArg, currentArg) && this.hasTimePermission(sender)) {
               return this.handleTimeParamCompletions(currentArg, lastArg);
            } else if (this.isPageParam(lastArg, currentArg) && this.hasPagePermission(sender)) {
               return this.handlePageParamCompletions(currentArg, lastArg);
            } else if (this.isRadiusParam(lastArg, currentArg) && this.hasRadiusPermission(sender)) {
               return this.handleRadiusParamCompletions(currentArg, lastArg, argument0);
            } else if (this.isMaterialParam(lastArg, currentArg) && this.hasLookupPermission(sender)) {
               return this.handleMaterialParamCompletions(currentArg, lastArg);
            } else if (args.length == 2) {
               return this.handleSecondArgCompletions(sender, argument0, args[1], paramState);
            } else if (args.length == 3 && argument0.equals("purge") && sender.hasPermission("coreprotect.purge")) {
               return this.handlePurgeThirdArgCompletions(args[1], args[2]);
            } else {
               return !this.hasLookupCommand(argument0, sender) || (argument0.equals("l") || argument0.equals("lookup")) && paramState.hasPage
                  ? Arrays.asList("")
                  : this.handleGenericLookupCompletions(argument0, currentArg, paramState);
            }
         }
      } else {
         return null;
      }
   }

   private List<String> getFirstLevelCompletions(CommandSender sender, String argument) {
      String arg = argument.toLowerCase(Locale.ROOT);
      List<String> completions = new ArrayList<>();
      this.addCompletionIfPermitted(sender, "coreprotect.help", "help", completions);
      this.addCompletionIfPermitted(sender, "coreprotect.inspect", "inspect", completions);
      this.addCompletionIfPermitted(sender, "coreprotect.rollback", "rollback", completions);
      this.addCompletionIfPermitted(sender, "coreprotect.restore", "restore", completions);
      this.addCompletionIfPermitted(sender, "coreprotect.lookup", "lookup", completions);
      this.addCompletionIfPermitted(sender, "coreprotect.purge", "purge", completions);
      this.addCompletionIfPermitted(sender, "coreprotect.reload", "reload", completions);
      this.addCompletionIfPermitted(sender, "coreprotect.status", "status", completions);
      this.addCompletionIfPermitted(sender, "coreprotect.lookup.near", "near", completions);
      this.addCompletionIfPermitted(sender, "coreprotect.restore", "undo", completions);
      return (List<String>)StringUtil.copyPartialMatches(arg, completions, new ArrayList(completions.size()));
   }

   private void addCompletionIfPermitted(CommandSender sender, String permission, String completion, List<String> completions) {
      if (sender.hasPermission(permission)) {
         completions.add(completion);
      }
   }

   private boolean hasLookupPermission(CommandSender sender) {
      return sender.hasPermission("coreprotect.lookup") || sender.hasPermission("coreprotect.rollback") || sender.hasPermission("coreprotect.restore");
   }

   private boolean hasTimePermission(CommandSender sender) {
      return this.hasLookupPermission(sender) || sender.hasPermission("coreprotect.purge");
   }

   private boolean hasRadiusPermission(CommandSender sender) {
      return this.hasTimePermission(sender);
   }

   private boolean hasPagePermission(CommandSender sender) {
      return sender.hasPermission("coreprotect.lookup") || sender.hasPermission("coreprotect.lookup.near") || sender.hasPermission("coreprotect.inspect");
   }

   private boolean hasLookupCommand(String cmd, CommandSender sender) {
      return sender.hasPermission("coreprotect.lookup") && (cmd.equals("l") || cmd.equals("lookup"))
         || sender.hasPermission("coreprotect.rollback") && (cmd.equals("rollback") || cmd.equals("rb") || cmd.equals("ro"))
         || sender.hasPermission("coreprotect.restore") && (cmd.equals("restore") || cmd.equals("rs") || cmd.equals("re"));
   }

   private boolean isActionParam(String lastArg, String currentArg) {
      return lastArg.equals("a:") || lastArg.equals("action:") || currentArg.startsWith("a:") || currentArg.startsWith("action:");
   }

   private boolean isUserParam(String lastArg, String currentArg) {
      return lastArg.equals("u:")
         || lastArg.equals("user:")
         || lastArg.equals("users:")
         || lastArg.equals("p:")
         || currentArg.startsWith("u:")
         || currentArg.startsWith("user:")
         || currentArg.startsWith("users:")
         || currentArg.startsWith("p:");
   }

   private boolean isTimeParam(String lastArg, String currentArg) {
      return lastArg.equals("t:") || lastArg.equals("time:") || currentArg.startsWith("t:") || currentArg.startsWith("time:");
   }

   private boolean isPageParam(String lastArg, String currentArg) {
      return lastArg.equals("page:") || currentArg.startsWith("page:");
   }

   private boolean isRadiusParam(String lastArg, String currentArg) {
      return lastArg.equals("r:") || lastArg.equals("radius:") || currentArg.startsWith("r:") || currentArg.startsWith("radius:");
   }

   private boolean isMaterialParam(String lastArg, String currentArg) {
      return lastArg.equals("i:")
         || lastArg.equals("include:")
         || lastArg.equals("item:")
         || lastArg.equals("items:")
         || lastArg.equals("b:")
         || lastArg.equals("block:")
         || lastArg.equals("blocks:")
         || currentArg.startsWith("i:")
         || currentArg.startsWith("include:")
         || currentArg.startsWith("item:")
         || currentArg.startsWith("items:")
         || currentArg.startsWith("b:")
         || currentArg.startsWith("block:")
         || currentArg.startsWith("blocks:")
         || lastArg.equals("e:")
         || lastArg.equals("exclude:")
         || currentArg.startsWith("e:")
         || currentArg.startsWith("exclude:");
   }

   private TabHandler.ParamState getParamState(String[] args) {
      TabHandler.ParamState state = new TabHandler.ParamState();
      if (ConfigHandler.lookupType.get(args[0]) != null && ConfigHandler.lookupPage.get(args[0]) != null) {
         state.pageLookup = true;
      }

      for (int i = 1; i < args.length; i++) {
         String arg = args[i].toLowerCase(Locale.ROOT);
         if (arg.equals("#container")) {
            state.hasContainer = true;
         } else if (arg.equals("#count") || arg.equals("#sum")) {
            state.hasCount = true;
         } else if (arg.equals("#preview")) {
            state.hasPreview = true;
         } else if ((arg.contains(":") || args[i - 1].contains(":") || args.length <= i + 1)
            && !arg.contains("u:")
            && !arg.contains("user:")
            && !arg.contains("users:")
            && !arg.contains("p:")) {
            if (arg.contains("page:")) {
               state.hasPage = true;
            } else if (arg.contains("a:") || arg.contains("action:")) {
               state.hasAction = true;
            } else if (arg.contains("i:")
               || arg.contains("include:")
               || arg.contains("item:")
               || arg.contains("items:")
               || arg.contains("b:")
               || arg.contains("block:")
               || arg.contains("blocks:")) {
               state.hasInclude = true;
            } else if (arg.contains("t:") || arg.contains("time:")) {
               state.hasTime = true;
            } else if (arg.contains("e:") || arg.contains("exclude:")) {
               state.hasExclude = true;
            } else if (arg.contains("r:") || arg.contains("radius:")) {
               state.hasRadius = true;
            }
         } else {
            state.hasUser = true;
         }
      }

      if (!state.hasContainer && ConfigHandler.lookupType.get(args[0]) != null) {
         int lookupType = ConfigHandler.lookupType.get(args[0]);
         if (lookupType == 1) {
            state.validContainer = true;
         } else if (lookupType == 5 && ConfigHandler.lookupUlist.get(args[0]).contains("#container")) {
            state.validContainer = true;
         }
      }

      return state;
   }

   private List<String> handleActionParamCompletions(String currentArg, String lastArg) {
      String arg = "";
      String filter = lastArg;
      if (currentArg.contains(":")) {
         String[] split = currentArg.split(":", 2);
         filter = split[0] + ":";
         if (split.length > 1) {
            arg = split[1];
         }
      }

      List<String> completions = new ArrayList<>(Arrays.asList(ACTIONS));

      for (int index = 0; index < completions.size(); index++) {
         completions.set(index, filter + completions.get(index));
      }

      return (List<String>)StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList(completions.size()));
   }

   private List<String> handleUserParamCompletions(String currentArg, String lastArg) {
      if (!lastArg.equals("u:") && !lastArg.equals("user:") && !lastArg.equals("users:") && !lastArg.equals("p:")) {
         String arg = "";
         String[] split = currentArg.split(":", 2);
         String filter = split[0] + ":";
         if (split.length > 1) {
            arg = split[1];
         }

         List<String> completions = Bukkit.getOnlinePlayers().stream().<String>map(Player::getName).collect(Collectors.toList());

         for (int index = 0; index < completions.size(); index++) {
            completions.set(index, filter + completions.get(index));
         }

         return (List<String>)StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList(completions.size()));
      } else {
         return null;
      }
   }

   private List<String> handleTimeParamCompletions(String currentArg, String lastArg) {
      String arg = "";
      String filter;
      if (currentArg.contains(":")) {
         String[] split = currentArg.split(":", 2);
         filter = split[0] + ":";
         if (split.length > 1) {
            arg = split[1];
         }
      } else {
         filter = "";
         arg = currentArg;
      }

      List<String> completions = new ArrayList<>();
      if (arg.chars().allMatch(Character::isDigit)) {
         boolean addNumbers = true;
         if (currentArg.length() > 0) {
            char lastChar = currentArg.charAt(currentArg.length() - 1);
            if (Character.isDigit(lastChar)) {
               completions.addAll(Arrays.asList(TIMES));
               addNumbers = false;
            }
         }

         if (addNumbers) {
            completions.addAll(Arrays.asList(NUMBERS));
         }
      }

      completions = new ArrayList<>(completions);

      for (int index = 0; index < completions.size(); index++) {
         completions.set(index, filter + arg + completions.get(index));
      }

      return (List<String>)StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList(completions.size()));
   }

   private List<String> handlePageParamCompletions(String currentArg, String lastArg) {
      String arg = "";
      String filter;
      if (currentArg.contains(":")) {
         String[] split = currentArg.split(":", 2);
         filter = split[0] + ":";
         if (split.length > 1) {
            arg = split[1];
         }
      } else {
         filter = "";
         arg = currentArg;
      }

      if (!arg.chars().allMatch(Character::isDigit)) {
         return null;
      } else {
         List<String> completions = new ArrayList<>(Arrays.asList(NUMBERS));
         if (arg.length() < 1) {
            for (int index = 0; index < completions.size(); index++) {
               completions.set(index, filter + arg + completions.get(index));
            }

            if (arg.length() == 0) {
               completions.remove(0);
            }
         }

         return (List<String>)StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList(completions.size()));
      }
   }

   private List<String> handleRadiusParamCompletions(String currentArg, String lastArg, String baseCommand) {
      String arg = "";
      String filter;
      if (currentArg.contains(":")) {
         String[] split = currentArg.split(":", 2);
         filter = split[0] + ":";
         if (split.length > 1) {
            arg = split[1];
         }
      } else {
         filter = "";
         arg = currentArg;
      }

      if (!baseCommand.equals("purge") && arg.chars().allMatch(Character::isDigit)) {
         List<String> completions = new ArrayList<>(Arrays.asList(NUMBERS));
         if (arg.length() < 2) {
            for (int index = 0; index < completions.size(); index++) {
               completions.set(index, filter + arg + completions.get(index));
            }
         }

         return (List<String>)StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList(completions.size()));
      } else if (!baseCommand.equals("purge") && !arg.startsWith("#")) {
         return null;
      } else {
         ArrayList<String> params = new ArrayList<>();
         params.add("#global");
         if (!baseCommand.equals("purge") && Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            params.add("#worldedit");
         }

         for (World world : Bukkit.getServer().getWorlds()) {
            params.add("#" + world.getName());
         }

         List<String> completions = new ArrayList<>(params);

         for (int index = 0; index < completions.size(); index++) {
            completions.set(index, filter + completions.get(index));
         }

         return (List<String>)StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList(completions.size()));
      }
   }

   private List<String> handleMaterialParamCompletions(String currentArg, String lastArg) {
      String arg = "";
      String filter;
      if (currentArg.contains(":")) {
         String[] split = currentArg.split(":", 2);
         filter = split[0] + ":";
         if (split.length > 1) {
            arg = split[1];
         }
      } else {
         filter = "";
         arg = currentArg;
      }

      this.initializeMaterialsIfNeeded();
      List<String> completions = new ArrayList<>(materials);

      for (int index = 0; index < completions.size(); index++) {
         completions.set(index, filter + completions.get(index));
      }

      return (List<String>)StringUtil.copyPartialMatches(filter + arg, completions, new ArrayList(completions.size()));
   }

   private void initializeMaterialsIfNeeded() {
      if (materials == null) {
         List<Material> addList = Arrays.asList(Material.ARMOR_STAND);
         List<Material> excludeList = Arrays.asList();
         Set<String> materialList = new HashSet<>();
         Material[] materialValues = Material.values();

         for (Material material : materialValues) {
            if (material.isBlock() || material.isItem()) {
               materialList.add(material.name().toLowerCase(Locale.ROOT));
            }
         }

         for (Material exclude : excludeList) {
            materialList.remove(exclude.name().toLowerCase(Locale.ROOT));
         }

         for (Material add : addList) {
            materialList.add(add.name().toLowerCase(Locale.ROOT));
         }

         for (String tag : CommandParser.getTags().keySet()) {
            materialList.add(tag);
         }

         materials = new ArrayList<>(materialList);
      }
   }

   private List<String> handleSecondArgCompletions(CommandSender sender, String cmd0, String cmd1, TabHandler.ParamState paramState) {
      String argument0 = cmd0.toLowerCase(Locale.ROOT);
      String argument1 = cmd1.toLowerCase(Locale.ROOT);
      if (argument0.equals("help") && sender.hasPermission("coreprotect.help")) {
         List<String> completions = new ArrayList<>(Arrays.asList(HELP));
         return (List<String>)StringUtil.copyPartialMatches(argument1, completions, new ArrayList(completions.size()));
      } else if (argument0.equals("purge") && sender.hasPermission("coreprotect.purge")) {
         List<String> completions = new ArrayList<>(Arrays.asList("t:", "r:", "i:"));
         return (List<String>)StringUtil.copyPartialMatches(argument1, completions, new ArrayList(completions.size()));
      } else if (this.hasLookupCommand(argument0, sender)) {
         List<String> completions = new ArrayList<>(this.filterParams(true, argument0, argument1, paramState));
         completions.addAll(
            Bukkit.getOnlinePlayers()
               .stream()
               .filter(player -> player.getName().toLowerCase(Locale.ROOT).startsWith(argument1))
               .map(Player::getName)
               .collect(Collectors.toList())
         );
         return (List<String>)StringUtil.copyPartialMatches(argument1, completions, new ArrayList(completions.size()));
      } else {
         return null;
      }
   }

   private List<String> handlePurgeThirdArgCompletions(String arg1, String arg2) {
      String argument1 = arg1.toLowerCase(Locale.ROOT);
      String argument2 = arg2.toLowerCase(Locale.ROOT);
      if (argument1.startsWith("t:")) {
         List<String> completions = new ArrayList<>(Arrays.asList("r:", "i:"));
         return (List<String>)StringUtil.copyPartialMatches(argument2, completions, new ArrayList(completions.size()));
      } else if (!argument1.startsWith("r:") && !argument1.startsWith("i:")) {
         return Arrays.asList("");
      } else {
         List<String> completions = new ArrayList<>(Arrays.asList("t:"));
         return (List<String>)StringUtil.copyPartialMatches(argument2, completions, new ArrayList(completions.size()));
      }
   }

   private List<String> handleGenericLookupCompletions(String cmd, String currentArg, TabHandler.ParamState paramState) {
      ArrayList<String> params = this.filterParams(false, cmd, currentArg, paramState);
      List<String> completions = new ArrayList<>(params);
      return (List<String>)StringUtil.copyPartialMatches(currentArg, completions, new ArrayList(completions.size()));
   }

   private ArrayList<String> filterParams(boolean firstParam, String lastArgument, String argument, TabHandler.ParamState state) {
      ArrayList<String> params = new ArrayList<>();

      for (String param : PARAMS) {
         if (param.equals("user:") && !state.hasUser) {
            params.add(param);
         } else if (param.equals("action:") && !state.hasAction) {
            params.add(param);
         } else if (param.equals("include:") && !state.hasInclude) {
            params.add(param);
         } else if (param.equals("exclude:") && !state.hasExclude) {
            params.add(param);
         } else if (param.equals("radius:") && !state.hasRadius) {
            params.add(param);
         } else if (param.equals("time:") && !state.hasTime) {
            params.add(param);
         } else if (param.equals("#container") && !state.hasContainer && !state.hasRadius && state.validContainer) {
            params.add(param);
         }
      }

      if (!firstParam || !state.pageLookup || !lastArgument.equals("l") && !lastArgument.equals("lookup")) {
         if (!firstParam && argument.startsWith("#")) {
            if (!state.hasCount) {
               params.add("#count");
            }

            if (!state.hasPreview) {
               params.add("#preview");
            }
         }
      } else {
         params.add("page:");
      }

      return params;
   }

   private static class ParamState {
      boolean hasUser;
      boolean hasAction;
      boolean hasInclude;
      boolean hasExclude;
      boolean hasRadius;
      boolean hasTime;
      boolean hasContainer;
      boolean hasCount;
      boolean hasPreview;
      boolean hasPage;
      boolean validContainer;
      boolean pageLookup;
   }
}
