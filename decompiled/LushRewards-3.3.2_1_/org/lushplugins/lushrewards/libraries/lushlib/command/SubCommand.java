package org.lushplugins.lushrewards.libraries.lushlib.command;

import com.google.common.collect.HashMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SubCommand {
   private final String name;
   private final HashMap<String, SubCommand> subCommands = new HashMap<>();
   private final HashMultimap<Integer, Callable<List<String>>> requiredArgs = HashMultimap.create();
   private final List<String> requiredPermissions = new ArrayList<>();
   private boolean isChild = false;

   public SubCommand(String name) {
      this.name = name;
   }

   public abstract boolean execute(
      @NotNull CommandSender var1, @NotNull org.bukkit.command.Command var2, @NotNull String var3, @NotNull String[] var4, @NotNull String[] var5
   );

   @Nullable
   public List<String> tabComplete(
      @NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs
   ) {
      return null;
   }

   public String getName() {
      return this.name;
   }

   public SubCommand getSubCommand(String name) {
      return this.subCommands.get(name);
   }

   public Collection<SubCommand> getSubCommands() {
      return this.subCommands.values();
   }

   public SubCommand addSubCommand(SubCommand subCommand) {
      if (!subCommand.isChild() && !this.subCommands.containsKey(subCommand.getName())) {
         this.subCommands.put(subCommand.getName(), subCommand);
         subCommand.setIsChild(true);
         return this;
      } else {
         throw new IllegalStateException("This sub-command already has a parent");
      }
   }

   public void removeSubCommand(String subCommand) {
      this.subCommands.remove(subCommand);
   }

   @Nullable
   public String getRequiredPermission() {
      return !this.requiredPermissions.isEmpty() ? this.requiredPermissions.get(0) : null;
   }

   @NotNull
   public List<String> getRequiredPermissions() {
      return this.requiredPermissions;
   }

   public boolean hasRequiredArgs() {
      return this.requiredArgs.size() > 0;
   }

   public List<String> getRequiredArgs(int index) {
      List<String> args = new ArrayList<>();

      for (Callable<List<String>> callable : this.requiredArgs.get(index)) {
         try {
            args.addAll(callable.call());
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }

      return args;
   }

   public int getRequiredArgCount() {
      return this.requiredArgs.size();
   }

   public void addRequiredArgs(int index, Callable<List<String>> argsCallable) {
      this.requiredArgs.put(index, argsCallable);
   }

   public void removeRequiredArgs(int index) {
      this.requiredArgs.removeAll(index);
   }

   public boolean hasRequiredPermissions(CommandSender sender) {
      for (String permission : this.requiredPermissions) {
         if (!sender.hasPermission(permission)) {
            return false;
         }
      }

      return true;
   }

   public SubCommand addRequiredPermission(String permission) {
      this.requiredPermissions.add(permission);
      return this;
   }

   public void removeRequiredPermission(String permission) {
      this.requiredPermissions.remove(permission);
   }

   public boolean isChild() {
      return this.isChild;
   }

   public void setIsChild(boolean isChild) {
      this.isChild = isChild;
   }
}
