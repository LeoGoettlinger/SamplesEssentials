package su.nightexpress.nightcore.command.experimental.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.experimental.node.CommandNode;
import su.nightexpress.nightcore.command.experimental.node.DirectNode;
import su.nightexpress.nightcore.command.experimental.node.NodeExecutor;
import su.nightexpress.nightcore.language.entry.LangString;

@Deprecated
public class ChainedNodeBuilder extends NodeBuilder<ChainedNode, ChainedNodeBuilder> {
   private final Map<String, CommandNode> childrenMap = new HashMap<>();
   private String localized;
   private NodeExecutor executor;

   public ChainedNodeBuilder(@NonNull NightCorePlugin plugin, String... aliases) {
      super(plugin, aliases);
   }

   @NonNull
   protected ChainedNodeBuilder getThis() {
      return this;
   }

   @NonNull
   public ChainedNodeBuilder localized(@NonNull LangString localized) {
      return this.localized(localized.getString());
   }

   @NonNull
   public ChainedNodeBuilder localized(@Nullable String localized) {
      this.localized = localized;
      return this;
   }

   @NonNull
   public ChainedNodeBuilder addChained(@NonNull String name, @NonNull Consumer<ChainedNodeBuilder> consumer) {
      ChainedNodeBuilder builder = ChainedNode.builder(this.plugin, name);
      consumer.accept(builder);
      return this.child(builder);
   }

   @NonNull
   public ChainedNodeBuilder addDirect(@NonNull String name, @NonNull Consumer<DirectNodeBuilder> consumer) {
      DirectNodeBuilder builder = DirectNode.builder(this.plugin, name);
      consumer.accept(builder);
      return this.child(builder);
   }

   @NonNull
   public <S extends CommandNode, B extends NodeBuilder<S, B>> ChainedNodeBuilder child(@NonNull B builder) {
      return this.child(builder.build());
   }

   @NonNull
   private ChainedNodeBuilder child(@NonNull CommandNode name) {
      this.childrenMap.put(name.getName(), name);
      return this;
   }

   @NonNull
   public ChainedNodeBuilder fallback(@NonNull NodeExecutor executor) {
      this.executor = executor;
      return this;
   }

   @NonNull
   public ChainedNode build() {
      return new ChainedNode(
         this.plugin, this.name, this.aliases, this.description, this.localized, this.permission, this.playerOnly, this.executor, this.childrenMap
      );
   }
}
