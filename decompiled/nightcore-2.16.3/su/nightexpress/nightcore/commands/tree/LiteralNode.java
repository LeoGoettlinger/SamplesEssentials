package su.nightexpress.nightcore.commands.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.commands.CommandRequirement;
import su.nightexpress.nightcore.commands.NodeExecutor;
import su.nightexpress.nightcore.commands.NodeUtils;
import su.nightexpress.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.nightcore.commands.context.Suggestions;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;

public class LiteralNode extends ExecutableNode {
   private final Map<String, FlagNode> flags = new LinkedHashMap<>();

   public LiteralNode(
      @NonNull String name,
      @NonNull String description,
      @Nullable String permission,
      @NonNull List<CommandRequirement> requirements,
      @Nullable NodeExecutor executor
   ) {
      super(name, description, permission, requirements, executor);
   }

   @NonNull
   @Override
   public Collection<? extends CommandNode> getRelevantNodes(@NonNull ArgumentReader reader) {
      return this.getChildren();
   }

   public void setArguments(CommandNode... arguments) {
      this.setArguments(Arrays.asList(arguments));
   }

   public void setArguments(@NonNull List<? extends CommandNode> arguments) {
      CommandNode parent = this;

      while (!arguments.isEmpty()) {
         CommandNode first = arguments.removeFirst();
         parent.getChildren().clear();
         parent.addChildren(first);
         parent = first;
         if (first instanceof FlagNode flagNode) {
            this.flags.put(first.getName(), flagNode);
         }
      }
   }

   @Override
   public boolean run(@NonNull CommandContext context) throws CommandSyntaxException {
      if (this.executor != null) {
         this.executor.run(context, context.getArguments());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void parse(@NonNull ArgumentReader reader, @NonNull CommandContextBuilder contextBuilder) throws CommandSyntaxException {
      contextBuilder.withNode(this, reader.getCursor());
      contextBuilder.withExecutor(this);
   }

   @Override
   protected void provideSuggestions(@NonNull ArgumentReader reader, @NonNull CommandContext context, @NonNull Suggestions suggestions) {
      suggestions.setSuggestions(List.of(this.name));
   }

   @NonNull
   public List<CommandNode> getArguments() {
      return NodeUtils.getArguments(this);
   }

   @NonNull
   public List<FlagNode> getFlags() {
      return new ArrayList<>(this.flags.values());
   }

   @Nullable
   public FlagNode getFlag(@NonNull String name) {
      return this.flags.get(name);
   }

   @Override
   public boolean hasRequiredArguments() {
      return this.getArguments().stream().anyMatch(CommandNode::isRequired);
   }

   @NonNull
   @Override
   public String getUsage() {
      StringBuilder builder = new StringBuilder().append(this.name);
      this.getArguments().forEach(node -> {
         builder.append(" ");
         builder.append(node.getUsage());
      });
      return builder.toString();
   }
}
