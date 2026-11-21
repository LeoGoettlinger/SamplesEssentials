package me.chancesd.pvpmanager.sdutils.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

public class ArgumentInfo {
   private static UnaryOperator<String> playerNotFoundMessageProvider = playerName -> "§cPlayer not found: §e" + playerName;
   private final String name;
   private final ArgumentType type;
   private final ArgumentLoader<?> loader;
   private boolean required = false;
   private String defValue = null;
   private String perm = null;
   private List<String> customTabCompletions = null;
   private String dependsOn = null;
   private final BaseCommand parent;

   public ArgumentInfo(String name, ArgumentType type, BaseCommand parent) {
      this.name = name;
      this.type = type;
      this.loader = BaseCommand.getDefaultLoader(type);
      this.parent = parent;
   }

   public ArgumentInfo(String name, ArgumentType type, ArgumentLoader<?> loader, BaseCommand parent) {
      this.name = name;
      this.type = type;
      this.loader = loader;
      this.parent = parent;
   }

   public ArgumentInfo required() {
      this.required = true;
      return this;
   }

   public ArgumentInfo defaultValue(String defaultValue) {
      this.defValue = defaultValue;
      return this;
   }

   public ArgumentInfo requirePermission(String permission) {
      this.perm = permission;
      return this;
   }

   public ArgumentInfo tabComplete(String... completions) {
      this.customTabCompletions = Arrays.asList(completions);
      return this;
   }

   public ArgumentInfo tabComplete(List<String> completions) {
      this.customTabCompletions = new ArrayList<>(completions);
      return this;
   }

   public ArgumentInfo dependsOn(String argumentName) {
      this.dependsOn = argumentName;
      return this;
   }

   public BaseCommand endArgument() {
      if (this.parent != null) {
         this.parent.addArgumentInfo(this);
      }

      return this.parent;
   }

   public static void setPlayerNotFoundMessageProvider(UnaryOperator<String> provider) {
      playerNotFoundMessageProvider = provider;
   }

   public String getName() {
      return this.name;
   }

   public ArgumentType getType() {
      return this.type;
   }

   public boolean isRequired() {
      return this.required;
   }

   public String getDefaultValue() {
      return this.defValue;
   }

   public boolean hasPermission() {
      return this.perm != null && !this.perm.isEmpty();
   }

   public String getPermission() {
      return this.perm;
   }

   public String getDependsOn() {
      return this.dependsOn;
   }

   public boolean hasDependency() {
      return this.dependsOn != null && !this.dependsOn.isEmpty();
   }

   public boolean hasLoader() {
      return this.loader != null;
   }

   public ArgumentLoader<?> getLoader() {
      return this.loader;
   }

   public boolean isValid(CommandArgument argument) {
      try {
         if (this.type == ArgumentType.PLAYER) {
            return argument.getAsPlayer() != null;
         } else if (this.type != ArgumentType.PLAYER_OR_ALL) {
            if (this.type == ArgumentType.INTEGER) {
               argument.getAsInt();
               return true;
            } else if (this.type == ArgumentType.DOUBLE) {
               argument.getAsDouble();
               return true;
            } else if (this.type == ArgumentType.BOOLEAN) {
               argument.getAsBoolean();
               return true;
            } else if (this.type == ArgumentType.WORLD) {
               argument.getAsWorld();
               return true;
            } else if (this.type == ArgumentType.MATERIAL) {
               argument.getAsMaterial();
               return true;
            } else if (this.type == ArgumentType.DURATION) {
               argument.getAsDuration();
               return true;
            } else if (this.type != ArgumentType.STRING && this.type != ArgumentType.STRING_ARRAY) {
               if (this.type.isEnumType()) {
                  String value = argument.getValue().toUpperCase();
                  Class<? extends Enum<?>> enumClass = this.type.getEnumClass();

                  for (Enum<?> constant : enumClass.getEnumConstants()) {
                     if (constant.name().equalsIgnoreCase(value)) {
                        return true;
                     }
                  }

                  return false;
               } else {
                  return true;
               }
            } else {
               return true;
            }
         } else {
            return argument.isWildcard() || argument.getAsPlayerOrWildcard() != null;
         }
      } catch (Exception var8) {
         return false;
      }
   }

   public String getValidationErrorMessage(CommandArgument argument) {
      if (this.type == ArgumentType.PLAYER) {
         return playerNotFoundMessageProvider.apply(argument.getValue());
      } else if (this.type == ArgumentType.PLAYER_OR_ALL) {
         return playerNotFoundMessageProvider.apply(argument.getValue());
      } else if (this.type == ArgumentType.INTEGER || this.type == ArgumentType.DOUBLE) {
         return "§c'§7" + argument.getValue() + "§c' is not a valid number.";
      } else if (this.type == ArgumentType.BOOLEAN) {
         return "§c'" + argument.getValue() + "' is not a valid boolean (true/false).";
      } else if (this.type == ArgumentType.WORLD) {
         return "§c'" + argument.getValue() + "' is not a valid world name.";
      } else if (this.type == ArgumentType.MATERIAL) {
         return "§c'" + argument.getValue() + "' is not a valid material name.";
      } else if (this.type == ArgumentType.DURATION) {
         return "§c'" + argument.getValue() + "' is not a valid duration. Use formats like '30s', '5m', '2h', '1d'.";
      } else if (this.type != ArgumentType.STRING && this.type != ArgumentType.STRING_ARRAY) {
         if (this.type.isEnumType()) {
            StringBuilder options = new StringBuilder();
            Enum<?>[] constants = (Enum<?>[])this.type.getEnumClass().getEnumConstants();

            for (int i = 0; i < constants.length; i++) {
               if (i > 0) {
                  options.append("&8, ");
               }

               options.append("&e").append(constants[i].name());
            }

            return "#FF5555Invalid " + this.type.getEnumClass().getSimpleName() + ". &7Available options are: &e" + options;
         } else {
            return "§c'" + argument.getValue() + "' is not valid for " + this.name + ".";
         }
      } else {
         return "§c'" + argument.getValue() + "' is not valid for " + this.name + ".";
      }
   }

   public List<String> getTabCompletions(String partial) {
      if (this.customTabCompletions != null && !this.customTabCompletions.isEmpty()) {
         return getMatchingEntries(partial, this.customTabCompletions);
      } else if (this.type.hasTabCompletions()) {
         return getMatchingEntries(partial, this.type.getTabCompletions().get());
      } else if (this.type == ArgumentType.PLAYER || this.type == ArgumentType.OFFLINE_PLAYER) {
         List<String> playerNames = Bukkit.getOnlinePlayers().stream().<String>map(Player::getName).toList();
         return getMatchingEntries(partial, playerNames);
      } else if (this.type == ArgumentType.PLAYER_OR_ALL) {
         List<String> completions = new ArrayList<>();
         completions.add("*");
         completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
         return getMatchingEntries(partial, completions);
      } else if (this.type == ArgumentType.WORLD) {
         List<String> worldNames = Bukkit.getWorlds().stream().<String>map(WorldInfo::getName).toList();
         return getMatchingEntries(partial, worldNames);
      } else if (this.type == ArgumentType.MATERIAL) {
         List<String> materialNames = Arrays.stream(Material.values()).map(Enum::name).map(String::toLowerCase).toList();
         return getMatchingEntries(partial, materialNames);
      } else if (this.type == ArgumentType.DURATION) {
         return getMatchingEntries(partial, Arrays.asList("30s", "5m", "1h", "1d", "1h30m"));
      } else if (this.type == ArgumentType.BOOLEAN) {
         return getMatchingEntries(partial, Arrays.asList("true", "false"));
      } else {
         return (List<String>)(this.type != ArgumentType.STRING && this.type != ArgumentType.STRING_ARRAY
            ? new ArrayList<>()
            : getMatchingEntries(partial, Arrays.asList(this.name)));
      }
   }

   private static List<String> getMatchingEntries(String token, List<String> toFilter) {
      return toFilter.stream().filter(s -> s.toLowerCase().contains(token.toLowerCase())).toList();
   }
}
