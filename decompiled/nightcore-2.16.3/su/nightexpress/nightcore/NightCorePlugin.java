package su.nightexpress.nightcore;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.chat.UniversalChatEventHandler;
import su.nightexpress.nightcore.bridge.permission.PermissionNamespace;
import su.nightexpress.nightcore.bridge.scheduler.AdaptedScheduler;
import su.nightexpress.nightcore.command.CommandManager;
import su.nightexpress.nightcore.command.api.NightPluginCommand;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.nightcore.language.LangManager;
import su.nightexpress.nightcore.locale.LangContainer;
import su.nightexpress.nightcore.ui.dialog.wrap.DialogKey;
import su.nightexpress.nightcore.ui.dialog.wrap.DialogRegistry;
import su.nightexpress.nightcore.ui.inventory.MenuRegistry;
import su.nightexpress.nightcore.util.wrapper.UniTask;

public interface NightCorePlugin extends Plugin {
   void enable();

   void disable();

   void reload();

   @Deprecated
   NightPluginCommand getBaseCommand();

   @NonNull
   FileConfig getConfig();

   @Deprecated
   @NonNull
   FileConfig getLang();

   @NonNull
   PluginDetails getDetails();

   Path dataPath();

   void registerPermissions(PermissionNamespace var1);

   void extractResources(@NonNull String var1);

   void extractResources(@NonNull String var1, @NonNull String var2);

   void injectLang(@NonNull Class<? extends LangContainer> var1);

   void injectLang(@NonNull LangContainer var1);

   @NonNull
   default String getNameLocalized() {
      return this.getDetails().getName();
   }

   @NonNull
   default String getPrefix() {
      return this.getDetails().getPrefix();
   }

   default String[] getCommandAliases() {
      return this.getDetails().getCommandAliases();
   }

   @Deprecated
   @NonNull
   default String getLanguage() {
      return this.getDetails().getLanguage();
   }

   default void info(@NonNull String msg) {
      this.getLogger().info(msg);
   }

   default void warn(@NonNull String msg) {
      this.getLogger().warning(msg);
   }

   default void error(@NonNull String msg) {
      this.getLogger().severe(msg);
   }

   default void warn(@NonNull String msg, Throwable t) {
      this.getLogger().log(Level.WARNING, msg, t);
   }

   default void error(@NonNull String msg, Throwable t) {
      this.getLogger().log(Level.SEVERE, msg, t);
   }

   default void debug(@NonNull String msg) {
      this.info("[DEBUG] " + msg);
   }

   void registerListener(@NonNull Listener var1);

   void addChatHandler(@NonNull EventPriority var1, @NonNull UniversalChatEventHandler var2);

   void removeChatHandler(@NonNull UniversalChatEventHandler var1);

   @Deprecated
   @NonNull
   LangManager getLangManager();

   @Deprecated
   @NonNull
   CommandManager getCommandManager();

   @NonNull
   MenuRegistry getMenuRegistry();

   @Deprecated
   @NonNull
   default BukkitScheduler getScheduler() {
      return this.getServer().getScheduler();
   }

   @NonNull
   AdaptedScheduler scheduler();

   @NonNull
   DialogRegistry dialogRegistry();

   @NonNull
   PluginManager getPluginManager();

   <T> void showDialog(@NonNull Player var1, @NonNull DialogKey<T> var2, @NonNull T var3, @Nullable Runnable var4);

   void runTask(@NonNull Runnable var1);

   void runTask(Entity var1, @NonNull Runnable var2);

   void runTask(Location var1, @NonNull Runnable var2);

   void runTask(Chunk var1, @NonNull Runnable var2);

   void runTaskAsync(@NonNull Runnable var1);

   void runTaskLater(@NonNull Runnable var1, long var2);

   void runTaskLaterAsync(@NonNull Runnable var1, long var2);

   void runTaskTimer(@NonNull Runnable var1, long var2, long var4);

   void runTaskTimerAsync(@NonNull Runnable var1, long var2, long var4);

   @Deprecated
   default void runTask(@NonNull Consumer<BukkitTask> consumer) {
      this.getScheduler().runTask(this, consumer);
   }

   @Deprecated
   default void runTaskAsync(@NonNull Consumer<BukkitTask> consumer) {
      this.getScheduler().runTaskAsynchronously(this, consumer);
   }

   @Deprecated
   default void runTaskLater(@NonNull Consumer<BukkitTask> consumer, long delay) {
      this.getScheduler().runTaskLater(this, consumer, delay);
   }

   @Deprecated
   default void runTaskLaterAsync(@NonNull Consumer<BukkitTask> consumer, long delay) {
      this.getScheduler().runTaskLaterAsynchronously(this, consumer, delay);
   }

   @Deprecated
   default void runTaskTimer(@NonNull Consumer<BukkitTask> consumer, long delay, long interval) {
      this.getScheduler().runTaskTimer(this, consumer, delay, interval);
   }

   @Deprecated
   default void runTaskTimerAsync(@NonNull Consumer<BukkitTask> consumer, long delay, long interval) {
      this.getScheduler().runTaskTimerAsynchronously(this, consumer, delay, interval);
   }

   @Deprecated
   @NonNull
   default UniTask createTask(@NonNull Runnable runnable) {
      return new UniTask(this, runnable);
   }

   @Deprecated
   @NonNull
   default UniTask createAsyncTask(@NonNull Runnable runnable) {
      return this.createTask(runnable).setAsync();
   }
}
