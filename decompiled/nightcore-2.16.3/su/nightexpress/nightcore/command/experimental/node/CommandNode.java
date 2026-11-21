package su.nightexpress.nightcore.command.experimental.node;

import java.util.List;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCorePlugin;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.TabContext;

@Deprecated
public abstract class CommandNode implements NodeExecutor {
   protected final NightCorePlugin plugin;
   protected final String name;
   protected final String[] aliases;
   protected final String description;
   protected final String permission;
   protected final boolean playerOnly;
   protected CommandNode parent;

   public CommandNode(
      @NonNull NightCorePlugin plugin, @NonNull String name, String[] aliases, @NonNull String description, @Nullable String permission, boolean playerOnly
   ) {
      this.plugin = plugin;
      this.name = name.toLowerCase();
      this.aliases = Stream.of(aliases).map(String::toLowerCase).toArray(String[]::new);
      this.description = description;
      this.permission = permission;
      this.playerOnly = playerOnly;
   }

   @NonNull
   public abstract List<String> getTab(@NonNull TabContext var1);

   @Override
   public boolean run(@NonNull CommandContext context) {
      if (this.isPlayerOnly() && !(context.getSender() instanceof Player)) {
         context.errorPlayerOnly();
         return false;
      } else if (!this.hasPermission(context.getSender())) {
         context.errorPermission();
         return false;
      } else {
         return this.onRun(context);
      }
   }

   protected abstract boolean onRun(@NonNull CommandContext var1);

   public boolean hasPermission(@NonNull CommandSender sender) {
      return this.permission == null || sender.hasPermission(this.permission);
   }

   @NonNull
   public String getNameWithParents() {
      StringBuilder builder = new StringBuilder();

      for (CommandNode parent = this.getParent(); parent != null; parent = parent.getParent()) {
         if (!builder.isEmpty()) {
            builder.insert(0, " ");
         }

         builder.insert(0, parent.getName());
      }

      builder.append(" ").append(this.getName());
      return builder.toString().strip();
   }

   @NonNull
   public String getName() {
      return this.name;
   }

   public String[] getAliases() {
      return this.aliases;
   }

   @NonNull
   public String getUsage() {
      return "";
   }

   @NonNull
   public String getDescription() {
      return this.description;
   }

   @Nullable
   public CommandNode getParent() {
      return this.parent;
   }

   protected void setParent(@Nullable CommandNode parent) {
      this.parent = parent;
   }

   @Nullable
   public String getPermission() {
      return this.permission;
   }

   public boolean isPlayerOnly() {
      return this.playerOnly;
   }
}
