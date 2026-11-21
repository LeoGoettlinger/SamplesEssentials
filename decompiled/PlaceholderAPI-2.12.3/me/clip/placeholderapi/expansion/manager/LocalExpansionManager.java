package me.clip.placeholderapi.expansion.manager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.events.ExpansionRegisterEvent;
import me.clip.placeholderapi.events.ExpansionUnregisterEvent;
import me.clip.placeholderapi.events.ExpansionsLoadedEvent;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Cleanable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Taskable;
import me.clip.placeholderapi.expansion.VersionSpecific;
import me.clip.placeholderapi.expansion.cloud.CloudExpansion;
import me.clip.placeholderapi.util.FileUtil;
import me.clip.placeholderapi.util.Futures;
import me.clip.placeholderapi.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class LocalExpansionManager implements Listener {
   @NotNull
   private static final String EXPANSIONS_FOLDER_NAME = "expansions";
   @NotNull
   private static final Set<MethodSignature> ABSTRACT_EXPANSION_METHODS = Arrays.stream(PlaceholderExpansion.class.getDeclaredMethods())
      .filter(method -> Modifier.isAbstract(method.getModifiers()))
      .map(method -> new MethodSignature(method.getName(), method.getParameterTypes()))
      .collect(Collectors.toSet());
   @NotNull
   private final File folder;
   @NotNull
   private final PlaceholderAPIPlugin plugin;
   @NotNull
   private final Map<String, PlaceholderExpansion> expansions = new ConcurrentHashMap<>();
   private final ReentrantLock expansionsLock = new ReentrantLock();

   public LocalExpansionManager(@NotNull PlaceholderAPIPlugin plugin) {
      this.plugin = plugin;
      this.folder = new File(plugin.getDataFolder(), "expansions");
      if (!this.folder.exists() && !this.folder.mkdirs()) {
         plugin.getLogger().warning("Failed to create expansions folder!");
      }
   }

   public void load(@NotNull CommandSender sender) {
      this.registerAll(sender);
   }

   public void kill() {
      this.unregisterAll();
   }

   @NotNull
   public File getExpansionsFolder() {
      return this.folder;
   }

   @NotNull
   @Unmodifiable
   public Set<String> getIdentifiers() {
      this.expansionsLock.lock();

      ImmutableSet var1;
      try {
         var1 = ImmutableSet.copyOf(this.expansions.keySet());
      } finally {
         this.expansionsLock.unlock();
      }

      return var1;
   }

   @NotNull
   @Unmodifiable
   public Collection<PlaceholderExpansion> getExpansions() {
      this.expansionsLock.lock();

      ImmutableSet var1;
      try {
         var1 = ImmutableSet.copyOf(this.expansions.values());
      } finally {
         this.expansionsLock.unlock();
      }

      return var1;
   }

   @Nullable
   public PlaceholderExpansion getExpansion(@NotNull String identifier) {
      return this.expansions.get(identifier.toLowerCase(Locale.ROOT));
   }

   @NotNull
   public Optional<PlaceholderExpansion> findExpansionByName(@NotNull String name) {
      this.expansionsLock.lock();

      Optional var9;
      try {
         PlaceholderExpansion bestMatch = null;

         for (Entry<String, PlaceholderExpansion> entry : this.expansions.entrySet()) {
            PlaceholderExpansion expansion = entry.getValue();
            if (expansion.getName().equalsIgnoreCase(name)) {
               bestMatch = expansion;
               break;
            }
         }

         var9 = Optional.ofNullable(bestMatch);
      } finally {
         this.expansionsLock.unlock();
      }

      return var9;
   }

   @NotNull
   public Optional<PlaceholderExpansion> findExpansionByIdentifier(@NotNull String identifier) {
      return Optional.ofNullable(this.getExpansion(identifier));
   }

   public Optional<PlaceholderExpansion> register(@NotNull Class<? extends PlaceholderExpansion> clazz) {
      try {
         PlaceholderExpansion expansion = this.createExpansionInstance(clazz);
         if (expansion == null) {
            return Optional.empty();
         } else {
            Objects.requireNonNull(expansion.getAuthor(), "The expansion author is null!");
            Objects.requireNonNull(expansion.getIdentifier(), "The expansion identifier is null!");
            Objects.requireNonNull(expansion.getVersion(), "The expansion version is null!");
            if (expansion.getRequiredPlugin() != null
               && !expansion.getRequiredPlugin().isEmpty()
               && !Bukkit.getPluginManager().isPluginEnabled(expansion.getRequiredPlugin())) {
               Msg.warn("Cannot load expansion %s due to a missing plugin: %s", expansion.getIdentifier(), expansion.getRequiredPlugin());
               return Optional.empty();
            } else {
               expansion.setExpansionType(PlaceholderExpansion.Type.EXTERNAL);
               if (!expansion.register()) {
                  Msg.warn("Cannot load expansion %s due to an unknown issue.", expansion.getIdentifier());
                  return Optional.empty();
               } else {
                  return Optional.of(expansion);
               }
            }
         }
      } catch (NullPointerException | LinkageError var4) {
         String reason;
         if (var4 instanceof LinkageError) {
            reason = " (Is a dependency missing?)";
         } else {
            reason = " - One of its properties is null which is not allowed!";
         }

         Msg.severe("Failed to load expansion class %s%s", var4, clazz.getSimpleName(), reason);
         return Optional.empty();
      }
   }

   @Internal
   public boolean register(@NotNull PlaceholderExpansion expansion) {
      String identifier = expansion.getIdentifier().toLowerCase(Locale.ROOT);
      if (!expansion.canRegister()) {
         return false;
      } else if (expansion.getExpansionType() == PlaceholderExpansion.Type.EXTERNAL && this.expansions.containsKey(identifier)) {
         Msg.warn("Failed to load external expansion %s. Identifier is already in use.", expansion.getIdentifier());
         return false;
      } else {
         if (expansion instanceof Configurable) {
            Map<String, Object> defaults = ((Configurable)expansion).getDefaults();
            String pre = "expansions." + identifier + ".";
            FileConfiguration cfg = this.plugin.getConfig();
            boolean save = false;
            if (defaults != null) {
               for (Entry<String, Object> entries : defaults.entrySet()) {
                  if (entries.getKey() != null && !entries.getKey().isEmpty()) {
                     if (entries.getValue() == null) {
                        if (cfg.contains(pre + entries.getKey())) {
                           save = true;
                           cfg.set(pre + entries.getKey(), null);
                        }
                     } else if (!cfg.contains(pre + entries.getKey())) {
                        save = true;
                        cfg.set(pre + entries.getKey(), entries.getValue());
                     }
                  }
               }
            }

            if (save) {
               this.plugin.saveConfig();
               this.plugin.reloadConfig();
            }
         }

         if (expansion instanceof VersionSpecific) {
            VersionSpecific nms = (VersionSpecific)expansion;
            if (!nms.isCompatibleWith(PlaceholderAPIPlugin.getServerVersion())) {
               Msg.warn("Your server version is incompatible with expansion %s %s", expansion.getIdentifier(), expansion.getVersion());
               return false;
            }
         }

         PlaceholderExpansion removed = this.getExpansion(identifier);
         if (removed != null && !removed.unregister()) {
            return false;
         } else {
            ExpansionRegisterEvent event = new ExpansionRegisterEvent(expansion);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return false;
            } else {
               this.expansionsLock.lock();

               try {
                  this.expansions.put(identifier, expansion);
               } finally {
                  this.expansionsLock.unlock();
               }

               if (expansion instanceof Listener) {
                  Bukkit.getPluginManager().registerEvents((Listener)expansion, this.plugin);
               }

               Msg.info(
                  "Successfully registered %s expansion: %s [%s]",
                  expansion.getExpansionType().name().toLowerCase(),
                  expansion.getIdentifier(),
                  expansion.getVersion()
               );
               if (expansion instanceof Taskable) {
                  ((Taskable)expansion).start();
               }

               if (this.plugin.getPlaceholderAPIConfig().isCloudEnabled() && expansion.getExpansionType() == PlaceholderExpansion.Type.EXTERNAL) {
                  Optional<CloudExpansion> cloudExpansionOptional = this.plugin.getCloudExpansionManager().findCloudExpansionByName(identifier);
                  if (cloudExpansionOptional.isPresent()) {
                     CloudExpansion cloudExpansion = cloudExpansionOptional.get();
                     cloudExpansion.setHasExpansion(true);
                     cloudExpansion.setShouldUpdate(!cloudExpansion.getLatestVersion().equals(expansion.getVersion()));
                  }
               }

               return true;
            }
         }
      }
   }

   @Internal
   public boolean unregister(@NotNull PlaceholderExpansion expansion) {
      if (this.expansions.remove(expansion.getIdentifier().toLowerCase(Locale.ROOT)) == null) {
         return false;
      } else {
         Bukkit.getPluginManager().callEvent(new ExpansionUnregisterEvent(expansion));
         if (expansion instanceof Listener) {
            HandlerList.unregisterAll((Listener)expansion);
         }

         if (expansion instanceof Taskable) {
            ((Taskable)expansion).stop();
         }

         if (expansion instanceof Cacheable) {
            ((Cacheable)expansion).clear();
         }

         if (this.plugin.getPlaceholderAPIConfig().isCloudEnabled()) {
            this.plugin.getCloudExpansionManager().findCloudExpansionByName(expansion.getName()).ifPresent(cloud -> {
               cloud.setHasExpansion(false);
               cloud.setShouldUpdate(false);
            });
         }

         return true;
      }
   }

   private void registerAll(@NotNull CommandSender sender) {
      Msg.info("Placeholder expansion registration initializing...");
      Futures.onMainThread(
         this.plugin,
         this.findExpansionsOnDisk(),
         (classes, exception) -> {
            if (exception != null) {
               Msg.severe("Failed to load class files of expansion.", exception);
            } else {
               List<PlaceholderExpansion> registered = classes.stream()
                  .filter(Objects::nonNull)
                  .map(this::register)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .collect(Collectors.toList());
               long needsUpdate = registered.stream()
                  .map(expansion -> this.plugin.getCloudExpansionManager().findCloudExpansionByName(expansion.getName()).orElse(null))
                  .filter(Objects::nonNull)
                  .filter(CloudExpansion::shouldUpdate)
                  .count();
               StringBuilder message = new StringBuilder(registered.size() == 0 ? "&6" : "&a")
                  .append(registered.size())
                  .append(' ')
                  .append("placeholder hook(s) registered!");
               if (needsUpdate > 0L) {
                  message.append(' ').append("&6").append(needsUpdate).append(' ').append("placeholder hook(s) have an update available.");
               }

               Msg.msg(sender, message.toString());
               Bukkit.getPluginManager().callEvent(new ExpansionsLoadedEvent(registered));
            }
         }
      );
   }

   private void unregisterAll() {
      for (PlaceholderExpansion expansion : Sets.newHashSet(this.expansions.values())) {
         if (!expansion.persist()) {
            expansion.unregister();
         }
      }
   }

   @NotNull
   public CompletableFuture<List<Class<? extends PlaceholderExpansion>>> findExpansionsOnDisk() {
      File[] files = this.folder.listFiles((dir, name) -> name.endsWith(".jar"));
      return files == null
         ? CompletableFuture.completedFuture(Collections.emptyList())
         : Arrays.stream(files).map(this::findExpansionInFile).collect(Futures.collector());
   }

   @NotNull
   public CompletableFuture<Class<? extends PlaceholderExpansion>> findExpansionInFile(@NotNull File file) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               Class<? extends PlaceholderExpansion> expansionClass = FileUtil.findClass(file, PlaceholderExpansion.class);
               if (expansionClass == null) {
                  Msg.severe("Failed to load expansion %s, as it does not have a class which extends PlaceholderExpansion", file.getName());
                  return null;
               } else {
                  Set<MethodSignature> expansionMethods = Arrays.stream(expansionClass.getDeclaredMethods())
                     .map(method -> new MethodSignature(method.getName(), method.getParameterTypes()))
                     .collect(Collectors.toSet());
                  if (!expansionMethods.containsAll(ABSTRACT_EXPANSION_METHODS)) {
                     Msg.severe("Failed to load expansion %s, as it does not have the required methods declared for a PlaceholderExpansion.", file.getName());
                     return null;
                  } else {
                     return expansionClass;
                  }
               }
            } catch (NoClassDefFoundError | VerifyError var4) {
               Msg.severe("Failed to load expansion %s (is a dependency missing?)", var4, file.getName());
               return null;
            } catch (Exception var5) {
               this.plugin.getLogger().log(Level.SEVERE, "Failed to load expansion file: " + file.getAbsolutePath(), (Throwable)var5);
               return null;
            }
         }
      );
   }

   @Nullable
   public PlaceholderExpansion createExpansionInstance(@NotNull Class<? extends PlaceholderExpansion> clazz) throws LinkageError {
      try {
         return clazz.getDeclaredConstructor().newInstance();
      } catch (Exception var3) {
         if (var3.getCause() instanceof LinkageError) {
            throw (LinkageError)var3.getCause();
         } else {
            Msg.severe("Failed to create expansion instance for class %s.", var3, clazz.getName());
            return null;
         }
      }
   }

   @EventHandler
   public void onQuit(@NotNull PlayerQuitEvent event) {
      for (PlaceholderExpansion expansion : this.getExpansions()) {
         if (expansion instanceof Cleanable) {
            ((Cleanable)expansion).cleanup(event.getPlayer());
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onPluginDisable(@NotNull PluginDisableEvent event) {
      String name = event.getPlugin().getName();
      if (!name.equals(this.plugin.getName())) {
         for (PlaceholderExpansion expansion : this.getExpansions()) {
            if (name.equalsIgnoreCase(expansion.getRequiredPlugin())) {
               expansion.unregister();
               Msg.info("Unregistered placeholder expansion %s", expansion.getIdentifier());
               Msg.info("Reason: required plugin %s was disabled.", name);
            }
         }
      }
   }
}
