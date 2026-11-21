package revxrsal.commands.bukkit.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitBrigadier;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.util.Preconditions;
import revxrsal.commands.util.Strings;

final class Node {
   private final CommandNode<?> node;

   private Node(CommandNode node) {
      this.node = node;
   }

   public Node addChild(@NotNull Node node) {
      Preconditions.notNull(node, "node");
      this.node.addChild(node.node);
      return this;
   }

   public Node addChildren(@NotNull List<Node> nodes) {
      Preconditions.notNull(nodes, "nodes");

      for (Node node : nodes) {
         this.node.addChild(node.node);
      }

      return this;
   }

   public void action(Command<?> command) {
      NodeReflection.setCommand(this.node, command);
   }

   public Node canBeExecuted(BukkitBrigadier brigadier) {
      this.action(a -> {
         String input = a.getInput();
         ArgumentStack args = ArgumentStack.parse(input.indexOf(47) == 0 ? input.substring(1) : input);
         args.set(0, Strings.stripNamespace(args.getFirst()));
         CommandActor actor = brigadier.wrapSource(a.getSource());

         try {
            brigadier.getCommandHandler().dispatch(actor, args);
         } catch (Throwable var6) {
            brigadier.getCommandHandler().getExceptionHandler().handleException(var6, actor);
         }

         return 1;
      });
      return this;
   }

   public Node require(Predicate<Object> requirement) {
      NodeReflection.setRequirement((CommandNode<Object>)this.node, requirement);
      return this;
   }

   public Node suggest(SuggestionProvider provider) {
      if (!(this.node instanceof ArgumentCommandNode)) {
         throw new IllegalArgumentException("Not an argument node.");
      } else {
         NodeReflection.setSuggestionProvider((ArgumentCommandNode)this.node, provider);
         return this;
      }
   }

   public <T extends CommandNode<?>> T getNode() {
      return (T)this.node;
   }

   @NotNull
   public static Node from(@NotNull ArgumentBuilder<?, ?> builder) {
      Preconditions.notNull(builder, "builder");
      return new Node(builder.build());
   }

   @NotNull
   public static Node from(@NotNull CommandNode<?> node) {
      Preconditions.notNull(node, "node");
      return new Node(node);
   }
}
