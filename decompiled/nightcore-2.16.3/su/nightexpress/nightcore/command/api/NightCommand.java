package su.nightexpress.nightcore.command.api;

import java.util.Collection;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.command.CommandFlag;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.util.placeholder.Placeholder;

@Deprecated
public interface NightCommand extends Placeholder {
   @NonNull
   List<String> getTab(@NonNull Player var1, int var2, String[] var3);

   void execute(@NonNull CommandSender var1, @NonNull String var2, String[] var3);

   void addChildren(@NonNull NightCommand var1);

   void removeChildren(@NonNull String var1);

   default boolean hasPermission(@NonNull CommandSender sender) {
      return this.getPermission() == null || sender.hasPermission(this.getPermission());
   }

   @NonNull
   default String getLabelWithParents() {
      StringBuilder builder = new StringBuilder();

      for (NightCommand parent = this.getParent(); parent != null; parent = parent.getParent()) {
         builder.insert(0, parent.getAliases()[0] + " ");
      }

      builder.append(this.getAliases()[0]);
      return builder.toString();
   }

   @Nullable
   NightCommand getParent();

   void setParent(@Nullable NightCommand var1);

   @Nullable
   NightCommand getChildren(@NonNull String var1);

   @NonNull
   Collection<NightCommand> getChildrens();

   String[] getAliases();

   @Nullable
   String getPermission();

   default void setPermission(@Nullable Permission permission) {
      this.setPermission(permission == null ? null : permission.getName());
   }

   void setPermission(@Nullable String var1);

   @Nullable
   CommandFlag<?> getFlag(@NonNull String var1);

   @NonNull
   Collection<CommandFlag<?>> getFlags();

   default void addFlag(CommandFlag<?>... flags) {
      for (CommandFlag<?> flag : flags) {
         this.addFlag(flag);
      }
   }

   void addFlag(@NonNull CommandFlag<?> var1);

   @NonNull
   String getUsage();

   default void setUsage(@NonNull LangString string) {
      this.setUsage(string.getString());
   }

   void setUsage(@NonNull String var1);

   @NonNull
   String getDescription();

   default void setDescription(@NonNull LangString string) {
      this.setDescription(string.getString());
   }

   void setDescription(@NonNull String var1);

   boolean isPlayerOnly();

   void setPlayerOnly(boolean var1);
}
