package su.nightexpress.nightcore.command.experimental.node;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.TabContext;
import su.nightexpress.nightcore.command.experimental.builder.ChainedNodeBuilder;
import su.nightexpress.nightcore.command.experimental.builder.NodeBuilder;
import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.util.StringUtil;

@Deprecated
public class ChainedNode extends CommandNode {
   private final String localized;
   private final Map<String, CommandNode> commandMap;
   private NodeExecutor fallback;

   public ChainedNode(
      @NonNull NightCorePlugin plugin,
      @NonNull String name,
      String[] aliases,
      @NonNull String description,
      @Nullable String localized,
      @Nullable String permission,
      boolean playerOnly,
      @Nullable NodeExecutor fallback,
      @NonNull Map<String, CommandNode> commandMap
   ) {
      super(plugin, name, aliases, description, permission, playerOnly);
      this.localized = localized == null ? StringUtil.capitalizeUnderscored(name) : localized;
      this.commandMap = new HashMap<>();
      this.setFallback(fallback);
      this.addChildren(
         DirectNode.builder(plugin, "help")
            .description(CoreLang.COMMAND_HELP_DESC)
            .permission(permission)
            .executes((context, arguments) -> this.sendCommandList(context))
      );
      commandMap.values().forEach(this::addChildren);
   }

   @NonNull
   public static ChainedNodeBuilder builder(@NonNull NightCorePlugin plugin, String... aliases) {
      return new ChainedNodeBuilder(plugin, aliases);
   }

   @Override
   protected boolean onRun(@NonNull CommandContext context) {
      if (context.length() != 0 && context.getArgumentIndex() < context.length()) {
         String node = context.getArgs()[context.getArgumentIndex()];
         CommandNode children = this.commandMap.get(node);
         if (children == null) {
            return this.onFallback(context);
         } else {
            context.setArgumentIndex(context.getArgumentIndex() + 1);
            return children.run(context);
         }
      } else {
         return this.onFallback(context);
      }
   }

   public void setFallback(@Nullable NodeExecutor fallback) {
      this.fallback = fallback;
   }

   private boolean onFallback(@NonNull CommandContext context) {
      return this.fallback != null ? this.fallback.run(context) : this.sendCommandList(context);
   }

   private boolean sendCommandList(@NonNull CommandContext context) {
      CommandSender sender = context.getSender();
      context.send(
         CoreLang.COMMAND_HELP_LIST.getMessage(),
         replacer -> replacer.replace("%name%", this.localized)
            .replace(
               "%entry%",
               list -> this.getChildrens()
                  .stream()
                  .sorted(Comparator.comparing(CommandNode::getName))
                  .forEach(
                     children -> {
                        if (children.hasPermission(sender)) {
                           list.add(
                              CoreLang.COMMAND_HELP_ENTRY
                                 .getString()
                                 .replace("%command_label%", children.getNameWithParents())
                                 .replace("%command_usage%", children.getUsage())
                                 .replace("%command_description%", children.getDescription())
                           );
                        }
                     }
                  )
            )
      );
      return true;
   }

   @NonNull
   @Override
   public List<String> getTab(@NonNull TabContext context) {
      int index = context.getLastCommandIndex();
      if (index == context.length() - 1) {
         return this.getChildrens().stream().filter(nodex -> nodex.hasPermission(context.getSender())).map(CommandNode::getName).toList();
      } else if (index >= context.length()) {
         return Collections.emptyList();
      } else {
         String node = context.getArg(index);
         CommandNode children = this.commandMap.get(node);
         if (children == null) {
            return Collections.emptyList();
         } else {
            context.setLastCommandIndex(index + 1);
            return children.getTab(context);
         }
      }
   }

   public void addChildren(@NonNull NodeBuilder<?, ?> builder) {
      this.addChildren(builder.build());
   }

   public void addChildren(@NonNull CommandNode children) {
      if (children.getParent() == null) {
         this.commandMap.put(children.getName(), children);

         for (String alias : children.getAliases()) {
            this.commandMap.put(alias, children);
         }

         children.setParent(this);
      }
   }

   public void removeChildren(@NonNull String alias) {
      this.commandMap.keySet().removeIf(key -> key.equalsIgnoreCase(alias));
   }

   @Nullable
   public CommandNode getChildren(@NonNull String alias) {
      return this.commandMap.get(alias);
   }

   @NonNull
   public Set<CommandNode> getChildrens() {
      return new HashSet<>(this.commandMap.values());
   }

   @NonNull
   public String getLocalized() {
      return this.localized;
   }

   @NonNull
   public Map<String, CommandNode> getCommandMap() {
      return this.commandMap;
   }
}
