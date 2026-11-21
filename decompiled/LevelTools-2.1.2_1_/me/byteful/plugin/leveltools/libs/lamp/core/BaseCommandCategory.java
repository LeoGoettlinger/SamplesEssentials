package me.byteful.plugin.leveltools.libs.lamp.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import me.byteful.plugin.leveltools.libs.lamp.CommandHandler;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandCategory;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandPermission;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

final class BaseCommandCategory implements CommandCategory {
   CommandPath path;
   String name;
   @Nullable
   BaseCommandCategory parent;
   @Nullable
   CommandExecutable defaultAction;
   CommandHandler handler;
   final Map<CommandPath, ExecutableCommand> commands = new HashMap<>();
   final Map<CommandPath, BaseCommandCategory> categories = new HashMap<>();
   final CommandPermission permission = new BaseCommandCategory.CategoryPermission();
   private final Map<CommandPath, CommandCategory> unmodifiableCategories = Collections.unmodifiableMap(this.categories);
   private final Map<CommandPath, ExecutableCommand> unmodifiableCommands = Collections.unmodifiableMap(this.commands);

   @NotNull
   @Override
   public String getName() {
      return this.name;
   }

   @NotNull
   @Override
   public CommandPath getPath() {
      return this.path;
   }

   @NotNull
   @Override
   public CommandHandler getCommandHandler() {
      return this.handler;
   }

   @Nullable
   @Override
   public CommandCategory getParent() {
      return this.parent;
   }

   @Nullable
   @Override
   public ExecutableCommand getDefaultAction() {
      return this.defaultAction;
   }

   @NotNull
   @Override
   public CommandPermission getPermission() {
      return this.permission;
   }

   @Override
   public boolean isSecret() {
      for (ExecutableCommand command : this.commands.values()) {
         if (!command.isSecret()) {
            return false;
         }
      }

      for (CommandCategory category : this.categories.values()) {
         if (!category.isSecret()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean isEmpty() {
      return this.defaultAction == null && this.commands.isEmpty() && this.categories.isEmpty();
   }

   @NotNull
   @UnmodifiableView
   @Override
   public Map<CommandPath, CommandCategory> getCategories() {
      return this.unmodifiableCategories;
   }

   @NotNull
   @UnmodifiableView
   @Override
   public Map<CommandPath, ExecutableCommand> getCommands() {
      return this.unmodifiableCommands;
   }

   @Override
   public String toString() {
      return "CommandCategory{path=" + this.path + ", name='" + this.name + "'}";
   }

   public void parent(BaseCommandCategory cat) {
      this.parent = cat;
      if (cat != null) {
         cat.categories.put(this.path, this);
      }
   }

   public int compareTo(@NotNull CommandCategory o) {
      return this.path.compareTo(o.getPath());
   }

   private class CategoryPermission implements CommandPermission {
      private CategoryPermission() {
      }

      @Override
      public boolean canExecute(@NotNull CommandActor actor) {
         for (ExecutableCommand command : BaseCommandCategory.this.commands.values()) {
            if (command.getPermission().canExecute(actor)) {
               return true;
            }
         }

         for (CommandCategory category : BaseCommandCategory.this.categories.values()) {
            if (category.getPermission().canExecute(actor)) {
               return true;
            }
         }

         return BaseCommandCategory.this.defaultAction == null ? false : BaseCommandCategory.this.defaultAction.hasPermission(actor);
      }
   }
}
