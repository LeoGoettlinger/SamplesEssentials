package su.nightexpress.nightcore.commands.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.commands.CommandRequirement;
import su.nightexpress.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.nightcore.commands.context.Suggestions;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;
import su.nightexpress.nightcore.util.LowerCase;

public abstract class CommandNode {
   protected final String name;
   protected final String permission;
   protected final List<CommandRequirement> requirements;
   protected final Map<String, CommandNode> children;

   public CommandNode(@NonNull String name, @Nullable String permission, @NonNull List<CommandRequirement> requirements) {
      this.name = LowerCase.USER_LOCALE.apply(name);
      this.permission = permission;
      this.children = new LinkedHashMap<>();
      this.requirements = requirements;
   }

   public boolean canUse(@NonNull CommandSender sender) {
      return this.requirements.stream().allMatch(requirement -> requirement.test(sender));
   }

   public void suggests(@NonNull ArgumentReader reader, @NonNull CommandContext context, @NonNull Suggestions suggestions) {
      List<CommandNode> children = new ArrayList<>(this.getChildren());
      if (children.isEmpty()) {
         suggestions.setSuggestions(Collections.emptyList());
      } else {
         CommandNode nextNode = children.getFirst();
         nextNode.provideSuggestions(reader, context, suggestions);
      }
   }

   public abstract void parse(@NonNull ArgumentReader var1, @NonNull CommandContextBuilder var2) throws CommandSyntaxException;

   protected abstract void provideSuggestions(@NonNull ArgumentReader var1, @NonNull CommandContext var2, @NonNull Suggestions var3);

   public boolean hasPermission(@NonNull CommandSender sender) {
      return this.permission == null || sender.hasPermission(this.permission);
   }

   @NonNull
   public Collection<? extends CommandNode> getChildren() {
      return this.children.values();
   }

   public boolean hasChildren() {
      return !this.children.isEmpty();
   }

   @NonNull
   public abstract Collection<? extends CommandNode> getRelevantNodes(@NonNull ArgumentReader var1);

   @Nullable
   public CommandNode getChild(@NonNull String name) {
      return this.children.get(name);
   }

   protected void addChildren(@NonNull CommandNode node) {
      this.children.put(node.getName(), node);
   }

   public boolean isRequired() {
      return false;
   }

   public boolean hasRequiredArguments() {
      return false;
   }

   @NonNull
   public String getName() {
      return this.name;
   }

   @NonNull
   public String getLocalizedName() {
      return this.name;
   }

   @NonNull
   public String getUsage() {
      return "";
   }

   @Nullable
   public String getPermission() {
      return this.permission;
   }

   @NonNull
   public List<CommandRequirement> getRequirements() {
      return this.requirements;
   }
}
