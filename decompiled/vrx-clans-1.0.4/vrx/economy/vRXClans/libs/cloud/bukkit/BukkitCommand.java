package vrx.economy.vRXClans.libs.cloud.bukkit;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import vrx.economy.vRXClans.libs.cloud.Command;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.BukkitHelper;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.internal.CommandNode;
import vrx.economy.vRXClans.libs.cloud.permission.Permission;
import vrx.economy.vRXClans.libs.cloud.suggestion.Suggestion;
import vrx.economy.vRXClans.libs.cloud.suggestion.Suggestions;
import vrx.economy.vRXClans.libs.cloud.util.StringUtils;

final class BukkitCommand<C> extends org.bukkit.command.Command implements PluginIdentifiableCommand {
   private final CommandComponent<C> command;
   private final BukkitCommandManager<C> manager;
   private final Command<C> cloudCommand;
   private boolean disabled;

   BukkitCommand(
      @NonNull final String label,
      @NonNull final List<String> aliases,
      @NonNull final Command<C> cloudCommand,
      @NonNull final CommandComponent<C> command,
      @NonNull final BukkitCommandManager<C> manager
   ) {
      super(label, BukkitHelper.description(cloudCommand), "", aliases);
      this.command = command;
      this.manager = manager;
      this.cloudCommand = cloudCommand;
      this.disabled = false;
   }

   @NonNull
   public List<String> tabComplete(@NonNull final CommandSender sender, @NonNull final String alias, @NonNull final String[] args) throws IllegalArgumentException {
      StringBuilder builder = new StringBuilder(this.command.name());

      for (String string : args) {
         builder.append(" ").append(string);
      }

      Suggestions<C, ?> result = this.manager.suggestionFactory().suggestImmediately(this.manager.senderMapper().map(sender), builder.toString());
      return result.list()
         .stream()
         .map(Suggestion::suggestion)
         .map(suggestion -> StringUtils.trimBeforeLastSpace(suggestion, result.commandInput()))
         .filter(Objects::nonNull)
         .collect(Collectors.toList());
   }

   public boolean execute(@NonNull final CommandSender commandSender, @NonNull final String commandLabel, @NonNull final String[] strings) {
      StringBuilder builder = new StringBuilder(this.command.name());

      for (String string : strings) {
         builder.append(" ").append(string);
      }

      C sender = this.manager.senderMapper().map(commandSender);
      this.manager.commandExecutor().executeCommand(sender, builder.toString());
      return true;
   }

   @NonNull
   public String getDescription() {
      return BukkitHelper.description(this.cloudCommand);
   }

   @NonNull
   public Plugin getPlugin() {
      return this.manager.owningPlugin();
   }

   @NonNull
   public String getUsage() {
      CommandNode<C> node = this.namedNode();
      if (node == null) {
         this.getPlugin().getLogger().log(Level.WARNING, "Node does not exist in tree for command " + this.getLabel() + ".");
         return "";
      } else {
         return this.manager.commandSyntaxFormatter().apply(null, Collections.singletonList(Objects.requireNonNull(node.component())), node);
      }
   }

   public boolean testPermissionSilent(@NonNull final CommandSender target) {
      CommandNode<C> node = this.namedNode();
      if (!this.disabled && node != null) {
         Map<Type, Permission> accessMap = node.nodeMeta().getOrDefault(CommandNode.META_KEY_ACCESS, Collections.emptyMap());
         C cloudSender = this.manager.senderMapper().map(target);

         for (Entry<Type, Permission> entry : accessMap.entrySet()) {
            if (GenericTypeReflector.isSuperType(entry.getKey(), cloudSender.getClass())
               && this.manager.testPermission(cloudSender, entry.getValue()).allowed()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @API(
      status = API.Status.INTERNAL,
      since = "1.7.0"
   )
   void disable() {
      this.disabled = true;
   }

   public boolean isRegistered() {
      return !this.disabled;
   }

   @Nullable
   private CommandNode<C> namedNode() {
      return this.manager.commandTree().getNamedNode(this.command.name());
   }
}
