package com.magmaguy.elitemobs.magmacore.command;

import com.magmaguy.elitemobs.magmacore.command.arguments.ICommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.LiteralCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AdvancedCommand {
   private final List<String> aliases;
   private final boolean enabled = true;
   private final List<ICommandArgument> argumentsList = new ArrayList<>();
   private final Map<String, Integer> argumentsMap = new HashMap<>();
   private String usage;
   private String description;
   private String permission = "";
   private SenderType senderType = SenderType.ANY;

   public AdvancedCommand(List<String> aliases) {
      this.aliases = aliases;
   }

   public static Command toBukkitCommand(JavaPlugin plugin, AdvancedCommand delegate, String name, List<String> aliases) {
      return new AdvancedCommand.AdvancedBukkitCommand(plugin, delegate, name, aliases);
   }

   public boolean aliasMatches(String potentialAlias) {
      for (String alias : this.aliases) {
         if (alias.equalsIgnoreCase(potentialAlias)) {
            return true;
         }
      }

      return false;
   }

   public boolean aliasStartMatches(String potentialAliasStart) {
      for (String alias : this.aliases) {
         if (alias.regionMatches(true, 0, potentialAliasStart, 0, potentialAliasStart.length())) {
            return true;
         }
      }

      return false;
   }

   protected void setPermission(String permission) {
      this.permission = permission;
   }

   protected void setDescription(String description) {
      this.description = description;
   }

   public String getPermissionMessage() {
      return "§cYou lack permission: " + this.permission;
   }

   protected void setSenderType(SenderType senderType) {
      this.senderType = senderType;
   }

   protected void addLiteral(String key) {
      this.addArgument(key, new LiteralCommandArgument(key));
   }

   protected void addPlayerArgument(String key) {
      this.addArgument(key, new PlayerCommandArgument());
   }

   protected void addArgument(String key, ICommandArgument arg) {
      if (!this.argumentsList.isEmpty() && this.argumentsList.getLast().isVarargs()) {
         throw new IllegalStateException("A varargs command argument must be the final argument");
      } else if (!arg.isOptional() && !this.argumentsList.isEmpty() && this.argumentsList.getLast().isOptional()) {
         throw new IllegalStateException("A required command argument cannot follow an optional argument");
      } else {
         this.argumentsMap.put(key, this.argumentsList.size());
         this.argumentsList.add(arg);
      }
   }

   protected void addOptionalArgument(String key, ICommandArgument arg) {
      this.addArgument(key, new AdvancedCommand.ArgumentMetadataDecorator(arg, true, false));
   }

   protected void addVarargsArgument(String key, ICommandArgument arg) {
      this.addArgument(key, new AdvancedCommand.ArgumentMetadataDecorator(arg, false, true));
   }

   protected void addOptionalVarargsArgument(String key, ICommandArgument arg) {
      this.addArgument(key, new AdvancedCommand.ArgumentMetadataDecorator(arg, true, true));
   }

   protected void setUsage(String usage) {
      this.usage = usage;
   }

   public String getStringArgument(String key, CommandSender commandSender, String[] args) {
      Integer pos = this.argumentsMap.get(key);
      if (pos == null) {
         Logger.sendMessage(commandSender, "Key " + key + " not found");
         return null;
      } else {
         int idx = pos + 1;
         if (idx >= args.length) {
            if (!this.argumentsList.get(pos).isOptional()) {
               Logger.sendMessage(commandSender, "Key " + key + " not found");
            }

            return null;
         } else {
            return args[idx];
         }
      }
   }

   public Integer getIntegerArgument(String key, CommandSender commandSender, String[] args) {
      String raw = this.getStringArgument(key, commandSender, args);
      if (raw == null) {
         return null;
      } else {
         try {
            return Integer.parseInt(raw);
         } catch (NumberFormatException var6) {
            Logger.sendMessage(commandSender, "Key " + key + " not a valid integer");
            return null;
         }
      }
   }

   public Double getDoubleArgument(String key, CommandSender commandSender, String[] args) {
      String raw = this.getStringArgument(key, commandSender, args);
      if (raw == null) {
         return null;
      } else {
         try {
            double value = Double.parseDouble(raw);
            if (Double.isFinite(value)) {
               return value;
            }
         } catch (NumberFormatException var7) {
         }

         Logger.sendMessage(commandSender, "Key " + key + " not a valid finite number");
         return null;
      }
   }

   public String getStringSequenceArgument(String key, CommandSender commandSender, String[] args) {
      try {
         StringBuilder output = new StringBuilder();

         for (int i = this.argumentsMap.get(key) + 1; i < args.length; i++) {
            output.append(args[i]).append(" ");
         }

         return output.toString();
      } catch (Exception var6) {
         Logger.sendMessage(commandSender, "Key " + key + " not found");
         return null;
      }
   }

   private boolean validateArgument(int index, CommandSender sender) {
      if (this.argumentsList.size() <= index) {
         Logger.sendMessage(sender, "Incorrect usage of this command!");
         Logger.sendMessage(sender, this.description);
         return false;
      } else {
         return true;
      }
   }

   public abstract void execute(CommandData var1);

   public List<String> onTabComplete(String[] args) {
      int index = args.length - 2;
      if (index >= 0 && this.argumentsList.size() > index) {
         if (this.argumentsList.get(index) instanceof List list) {
            return list;
         } else {
            return !this.argumentsList.get(index).toString().isEmpty() ? List.of(this.argumentsList.get(index).toString()) : Collections.emptyList();
         }
      } else {
         return Collections.emptyList();
      }
   }

   @Generated
   public List<String> getAliases() {
      return this.aliases;
   }

   @Generated
   public boolean isEnabled() {
      return true;
   }

   @Generated
   public List<ICommandArgument> getArgumentsList() {
      return this.argumentsList;
   }

   @Generated
   public Map<String, Integer> getArgumentsMap() {
      return this.argumentsMap;
   }

   @Generated
   public String getUsage() {
      return this.usage;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public SenderType getSenderType() {
      return this.senderType;
   }

   private static class AdvancedBukkitCommand extends Command {
      private final AdvancedCommand delegate;
      private final JavaPlugin plugin;

      AdvancedBukkitCommand(JavaPlugin plugin, AdvancedCommand delegate, String name, List<String> aliases) {
         super(name, delegate.getDescription(), delegate.getUsage(), aliases);
         this.delegate = delegate;
         this.plugin = plugin;
         if (!delegate.getPermission().isBlank()) {
            this.setPermission(delegate.getPermission());
            this.setPermissionMessage(delegate.getPermissionMessage());
         }
      }

      public boolean execute(CommandSender sender, String label, String[] args) {
         this.delegate.execute(new CommandData(sender, args, this.delegate));
         return true;
      }

      public String getDescription() {
         String description = this.delegate.getDescription();
         return description == null ? super.getDescription() : description;
      }

      public String getPermissionMessage() {
         String permissionMessage = this.delegate.getPermissionMessage();
         return permissionMessage == null ? super.getPermissionMessage() : permissionMessage;
      }

      public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
         return this.delegate.onTabComplete(args);
      }
   }

   private static final class ArgumentMetadataDecorator implements ICommandArgument {
      private final ICommandArgument delegate;
      private final boolean optional;
      private final boolean varargs;

      ArgumentMetadataDecorator(ICommandArgument delegate, boolean optional, boolean varargs) {
         this.delegate = delegate;
         this.optional = optional;
         this.varargs = varargs;
      }

      @Override
      public String hint() {
         return this.delegate.hint();
      }

      @Override
      public boolean matchesInput(String input) {
         return this.delegate.matchesInput(input);
      }

      @Override
      public List<String> literals() {
         return this.delegate.literals();
      }

      @Override
      public List<String> getSuggestions(CommandSender sender, String partialInput) {
         return this.delegate.getSuggestions(sender, partialInput);
      }

      @Override
      public boolean isLiteral() {
         return this.delegate.isLiteral();
      }

      @Override
      public boolean isOptional() {
         return this.optional || this.delegate.isOptional();
      }

      @Override
      public boolean isVarargs() {
         return this.varargs || this.delegate.isVarargs();
      }
   }
}
