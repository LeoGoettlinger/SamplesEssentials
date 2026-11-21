package su.nightexpress.nightcore.commands.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.commands.tree.ArgumentNode;
import su.nightexpress.nightcore.commands.tree.CommandNode;
import su.nightexpress.nightcore.commands.tree.FlagNode;
import su.nightexpress.nightcore.commands.tree.LiteralNode;

public class LiteralNodeBuilder extends ExecutableNodeBuilder<LiteralNode, LiteralNodeBuilder> {
   private final List<ArgumentNode<?>> arguments = new ArrayList<>();
   private final List<FlagNode> flags = new ArrayList<>();

   public LiteralNodeBuilder(@NonNull String literal) {
      super(literal);
   }

   @NonNull
   protected LiteralNodeBuilder getThis() {
      return this;
   }

   @NonNull
   public LiteralNodeBuilder withArguments(ArgumentNodeBuilder<?>... argument) {
      for (ArgumentNodeBuilder<?> builder : argument) {
         this.arguments.add(builder.build());
      }

      return this.getThis();
   }

   @NonNull
   public LiteralNodeBuilder withArguments(ArgumentNode<?>... argument) {
      this.arguments.addAll(Arrays.asList(argument));
      return this.getThis();
   }

   @NonNull
   public LiteralNodeBuilder withFlags(String... flags) {
      for (String name : flags) {
         this.withFlags(new FlagNode(name));
      }

      return this.getThis();
   }

   @NonNull
   public LiteralNodeBuilder withFlags(FlagNode... flags) {
      this.flags.addAll(Arrays.asList(flags));
      return this.getThis();
   }

   @NonNull
   public LiteralNode build() {
      LiteralNode result = new LiteralNode(this.name, this.description, this.permission, this.requirements, this.executor);
      List<CommandNode> args = new ArrayList<>();
      args.addAll(this.arguments);
      args.addAll(this.flags);
      result.setArguments(args);
      return result;
   }
}
