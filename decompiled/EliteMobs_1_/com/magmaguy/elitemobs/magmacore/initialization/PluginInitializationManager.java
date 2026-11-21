package com.magmaguy.elitemobs.magmacore.initialization;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PluginInitializationManager {
   private static final String STATE_PROPERTY_PREFIX = "magmacore.init.";
   private static final String OWNER_PROPERTY_SUFFIX = ".owner";
   private static final long SHUTDOWN_QUIESCENCE_TIMEOUT_SECONDS = 30L;
   private static final Map<String, PluginInitializationState> pluginStates = new ConcurrentHashMap<>();
   private static final Map<String, PluginInitializationLifecycle> lifecycles = new ConcurrentHashMap<>();
   private static final ThreadLocal<PluginInitializationManager.AttemptScope> activeAttempt = new ThreadLocal<>();

   private PluginInitializationManager() {
   }

   public static void run(
      JavaPlugin plugin,
      PluginInitializationConfig config,
      Consumer<PluginInitializationContext> asyncInitialization,
      Consumer<PluginInitializationContext> syncInitialization,
      Runnable onSuccess,
      Consumer<Throwable> onFailure
   ) {
      if (plugin.isEnabled()) {
         PluginInitializationLifecycle lifecycle = lifecycles.computeIfAbsent(plugin.getName(), ignored -> new PluginInitializationLifecycle());
         PluginInitializationLifecycle.AttemptStart attemptStart;
         synchronized (System.getProperties()) {
            attemptStart = lifecycle.prepareAttempt();
            if (attemptStart == null) {
               return;
            }

            claimAttemptState(plugin, attemptStart.attempt(), PluginInitializationState.INITIALIZING);
         }

         PluginInitializationLifecycle.Attempt attempt = attemptStart.attempt();
         attemptStart.cancelPrevious();

         try {
            if (!isAttemptActive(plugin, lifecycle, attempt)) {
               clearAttemptState(plugin, attempt);
               attempt.completeTerminal();
               return;
            }

            PluginInitializationProgressBar.start(
               plugin, attempt.ownerToken(), attempt.generation(), config.displayName(), config.adminPermission(), config.totalSteps()
            );
            if (!isAttemptActive(plugin, lifecycle, attempt)) {
               PluginInitializationProgressBar.complete(plugin, attempt.ownerToken());
               clearAttemptState(plugin, attempt);
               attempt.completeTerminal();
               return;
            }

            PluginInitializationContext context = new PluginInitializationContext(
               plugin, config, attempt.ownerToken(), () -> !isAttemptActive(plugin, lifecycle, attempt)
            );
            List<String> presentDependencies = config.resolveDependencies(plugin).stream().filter(PluginInitializationManager::isDependencyPresent).toList();
            if (presentDependencies.isEmpty()) {
               runAsyncPhase(plugin, lifecycle, attempt, context, asyncInitialization, syncInitialization, onSuccess, onFailure);
               return;
            }

            context.status("Waiting for " + String.join(", ", presentDependencies));
            waitForDependencies(
               plugin,
               lifecycle,
               attempt,
               context,
               presentDependencies,
               () -> runAsyncPhase(plugin, lifecycle, attempt, context, asyncInitialization, syncInitialization, onSuccess, onFailure),
               onFailure
            );
         } catch (Throwable var11) {
            dispatchFailure(plugin, lifecycle, attempt, var11, onFailure);
         }
      }
   }

   public static PluginInitializationState getState(String pluginName) {
      synchronized (System.getProperties()) {
         String sharedState = System.getProperty(stateProperty(pluginName));
         if (sharedState != null) {
            PluginInitializationState var10000;
            try {
               var10000 = PluginInitializationState.valueOf(sharedState);
            } catch (IllegalArgumentException var5) {
               return pluginStates.getOrDefault(pluginName, PluginInitializationState.UNINITIALIZED);
            }

            return var10000;
         } else {
            return pluginStates.getOrDefault(pluginName, PluginInitializationState.UNINITIALIZED);
         }
      }
   }

   public static boolean isPluginReady(String pluginName) {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
      return plugin != null && plugin.isEnabled() ? getState(pluginName) != PluginInitializationState.INITIALIZING : true;
   }

   public static void onEnable(JavaPlugin plugin) {
      PluginInitializationLifecycle lifecycle = lifecycles.get(plugin.getName());
      if (lifecycle != null) {
         lifecycle.resume();
      }
   }

   public static boolean isShutdownRequested(JavaPlugin plugin) {
      PluginInitializationManager.AttemptScope scope = activeAttempt.get();
      if (scope != null && scope.pluginName().equals(plugin.getName())) {
         return !isAttemptActive(plugin, scope.lifecycle(), scope.attempt());
      } else {
         PluginInitializationLifecycle lifecycle = lifecycles.get(plugin.getName());
         return lifecycle != null && lifecycle.isShutdownRequested();
      }
   }

   public static void requestShutdown(JavaPlugin plugin) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Initialization shutdown must be requested on the server thread.");
      } else {
         PluginInitializationLifecycle lifecycle = lifecycles.get(plugin.getName());
         if (lifecycle != null) {
            PluginInitializationLifecycle.Attempt attempt = lifecycle.requestShutdown();
            if (attempt != null) {
               PluginInitializationProgressBar.complete(plugin, attempt.ownerToken());
               clearAttemptState(plugin, attempt);
            }
         }
      }
   }

   public static void shutdown(JavaPlugin plugin) {
      PluginInitializationLifecycle lifecycle = lifecycles.get(plugin.getName());
      if (lifecycle == null) {
         PluginInitializationProgressBar.complete(plugin);
         synchronized (System.getProperties()) {
            System.clearProperty(stateProperty(plugin.getName()));
            System.clearProperty(ownerProperty(plugin.getName()));
            pluginStates.put(plugin.getName(), PluginInitializationState.UNINITIALIZED);
         }
      } else {
         PluginInitializationLifecycle.Attempt attempt = lifecycle.shutdownCurrent();
         if (attempt == null) {
            PluginInitializationProgressBar.complete(plugin);
            clearPluginState(plugin.getName());
         } else {
            PluginInitializationProgressBar.complete(plugin, attempt.ownerToken());
            clearAttemptState(plugin, attempt);
            awaitShutdownQuiescence(plugin, lifecycle, attempt);
         }
      }
   }

   public static void setState(JavaPlugin plugin, PluginInitializationState state) {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Initialization state overrides must run on the server thread.");
      } else {
         PluginInitializationLifecycle lifecycle = lifecycles.get(plugin.getName());
         PluginInitializationLifecycle.Attempt attempt = null;
         if (lifecycle != null) {
            attempt = lifecycle.cancelCurrent();
         }

         synchronized (System.getProperties()) {
            System.clearProperty(ownerProperty(plugin.getName()));
            System.setProperty(stateProperty(plugin.getName()), state.name());
            pluginStates.put(plugin.getName(), state);
         }

         if (attempt != null) {
            PluginInitializationProgressBar.complete(plugin, attempt.ownerToken());
         }
      }
   }

   private static boolean isDependencyPresent(String dependencyName) {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(dependencyName);
      return plugin != null && plugin.isEnabled();
   }

   private static void waitForDependencies(
      final JavaPlugin plugin,
      final PluginInitializationLifecycle lifecycle,
      final PluginInitializationLifecycle.Attempt attempt,
      final PluginInitializationContext context,
      final List<String> dependencies,
      final Runnable onReady,
      final Consumer<Throwable> onFailure
   ) {
      (new BukkitRunnable() {
            private String lastDescription = "";

            public void run() {
               try {
                  if (!PluginInitializationManager.isAttemptActive(plugin, lifecycle, attempt) || !plugin.isEnabled()) {
                     this.cancel();
                     return;
                  }

                  List<String> pendingDependencies = dependencies.stream()
                     .filter(dependency -> !PluginInitializationManager.isPluginReady(dependency))
                     .collect(Collectors.toList());
                  if (pendingDependencies.isEmpty()) {
                     this.cancel();
                     onReady.run();
                     return;
                  }

                  String description = "Waiting for " + String.join(", ", pendingDependencies);
                  if (!description.equals(this.lastDescription)) {
                     this.lastDescription = description;
                     context.status(description);
                  }
               } catch (Throwable var4) {
                  try {
                     this.cancel();
                  } catch (Throwable var3) {
                     if (var3 != var4) {
                        var4.addSuppressed(var3);
                     }
                  }

                  PluginInitializationManager.dispatchFailure(plugin, lifecycle, attempt, var4, onFailure);
               }
            }
         })
         .runTaskTimer(plugin, 0L, 10L);
   }

   private static void runAsyncPhase(
      JavaPlugin plugin,
      PluginInitializationLifecycle lifecycle,
      PluginInitializationLifecycle.Attempt attempt,
      PluginInitializationContext context,
      Consumer<PluginInitializationContext> asyncInitialization,
      Consumer<PluginInitializationContext> syncInitialization,
      Runnable onSuccess,
      Consumer<Throwable> onFailure
   ) {
      if (attempt.claimAsyncDispatch()) {
         attempt.predecessor()
            .whenComplete(
               (ignored, predecessorFailure) -> {
                  if (!isAttemptActive(plugin, lifecycle, attempt)) {
                     attempt.cancel();
                  } else {
                     try {
                        Bukkit.getScheduler()
                           .runTaskAsynchronously(
                              plugin,
                              () -> {
                                 if (attempt.startAsync()) {
                                    try {
                                       if (!isAttemptActive(plugin, lifecycle, attempt)) {
                                          attempt.completeAsyncWithoutSync();
                                          return;
                                       }

                                       runInAttemptScope(plugin, lifecycle, attempt, () -> asyncInitialization.accept(context));
                                       if (!isAttemptActive(plugin, lifecycle, attempt)) {
                                          attempt.completeAsyncWithoutSync();
                                          return;
                                       }

                                       attempt.scheduleSync(
                                          () -> Bukkit.getScheduler()
                                             .runTask(plugin, () -> runSyncPhase(plugin, lifecycle, attempt, context, syncInitialization, onSuccess, onFailure))
                                       );
                                    } catch (Throwable var9x) {
                                       if (!isAttemptActive(plugin, lifecycle, attempt)) {
                                          attempt.completeAsyncWithoutSync();
                                          return;
                                       }

                                       dispatchFailure(plugin, lifecycle, attempt, var9x, onFailure);
                                    }
                                 }
                              }
                           );
                     } catch (Throwable var11) {
                        dispatchFailure(plugin, lifecycle, attempt, var11, onFailure);
                     }
                  }
               }
            );
      }
   }

   private static void runSyncPhase(
      JavaPlugin plugin,
      PluginInitializationLifecycle lifecycle,
      PluginInitializationLifecycle.Attempt attempt,
      PluginInitializationContext context,
      Consumer<PluginInitializationContext> syncInitialization,
      Runnable onSuccess,
      Consumer<Throwable> onFailure
   ) {
      if (attempt.startSync()) {
         try {
            if (!isAttemptActive(plugin, lifecycle, attempt)) {
               return;
            }

            runInAttemptScope(plugin, lifecycle, attempt, () -> syncInitialization.accept(context));
            if (!isAttemptActive(plugin, lifecycle, attempt)) {
               return;
            }

            try {
               context.reportTimings();
            } catch (Throwable var12) {
            }

            runInAttemptScope(plugin, lifecycle, attempt, onSuccess);
            if (isAttemptActive(plugin, lifecycle, attempt)) {
               PluginInitializationProgressBar.complete(plugin, attempt.ownerToken());
               publishAttemptState(plugin, attempt, PluginInitializationState.INITIALIZED);
               return;
            }
         } catch (Throwable var13) {
            publishFailureOnMainThread(plugin, lifecycle, attempt, var13, onFailure);
            return;
         } finally {
            attempt.completeSync();
         }
      }
   }

   private static void runFailurePhase(
      JavaPlugin plugin,
      PluginInitializationLifecycle lifecycle,
      PluginInitializationLifecycle.Attempt attempt,
      Throwable throwable,
      Consumer<Throwable> onFailure
   ) {
      if (attempt.startSync()) {
         try {
            publishFailureOnMainThread(plugin, lifecycle, attempt, throwable, onFailure);
         } finally {
            attempt.completeSync();
         }
      }
   }

   private static void runInAttemptScope(
      JavaPlugin plugin, PluginInitializationLifecycle lifecycle, PluginInitializationLifecycle.Attempt attempt, Runnable action
   ) {
      PluginInitializationManager.AttemptScope previous = activeAttempt.get();
      activeAttempt.set(new PluginInitializationManager.AttemptScope(plugin.getName(), lifecycle, attempt));

      try {
         action.run();
      } finally {
         if (previous == null) {
            activeAttempt.remove();
         } else {
            activeAttempt.set(previous);
         }
      }
   }

   private static boolean isAttemptActive(JavaPlugin plugin, PluginInitializationLifecycle lifecycle, PluginInitializationLifecycle.Attempt attempt) {
      return lifecycle.isCurrent(attempt) && ownsAttemptState(plugin, attempt);
   }

   private static void claimAttemptState(JavaPlugin plugin, PluginInitializationLifecycle.Attempt attempt, PluginInitializationState state) {
      String pluginName = plugin.getName();
      synchronized (System.getProperties()) {
         System.setProperty(stateProperty(pluginName), state.name());
         System.setProperty(ownerProperty(pluginName), attempt.ownerToken());
         pluginStates.put(pluginName, state);
      }
   }

   private static boolean publishAttemptState(JavaPlugin plugin, PluginInitializationLifecycle.Attempt attempt, PluginInitializationState state) {
      String pluginName = plugin.getName();
      synchronized (System.getProperties()) {
         if (!attempt.ownerToken().equals(System.getProperty(ownerProperty(pluginName)))) {
            return false;
         } else {
            System.setProperty(stateProperty(pluginName), state.name());
            pluginStates.put(pluginName, state);
            return true;
         }
      }
   }

   private static void clearAttemptState(JavaPlugin plugin, PluginInitializationLifecycle.Attempt attempt) {
      String pluginName = plugin.getName();
      synchronized (System.getProperties()) {
         if (attempt.ownerToken().equals(System.getProperty(ownerProperty(pluginName)))) {
            System.clearProperty(stateProperty(pluginName));
            System.clearProperty(ownerProperty(pluginName));
            pluginStates.put(pluginName, PluginInitializationState.UNINITIALIZED);
         }
      }
   }

   private static void clearPluginState(String pluginName) {
      synchronized (System.getProperties()) {
         System.clearProperty(stateProperty(pluginName));
         System.clearProperty(ownerProperty(pluginName));
         pluginStates.put(pluginName, PluginInitializationState.UNINITIALIZED);
      }
   }

   private static void awaitShutdownQuiescence(JavaPlugin plugin, PluginInitializationLifecycle lifecycle, PluginInitializationLifecycle.Attempt attempt) {
      PluginInitializationManager.AttemptScope scope = activeAttempt.get();
      if (scope != null && scope.lifecycle() == lifecycle && scope.attempt() == attempt) {
         throw new IllegalStateException(plugin.getName() + " cannot reload or shut down reentrantly from its initialization callback.");
      } else {
         try {
            attempt.serializedCompletion().get(30L, TimeUnit.SECONDS);
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for " + plugin.getName() + " initialization to stop.", var5);
         } catch (ExecutionException var6) {
            throw new IllegalStateException(plugin.getName() + " initialization termination completed exceptionally.", var6.getCause());
         } catch (TimeoutException var7) {
            throw new IllegalStateException(plugin.getName() + " initialization did not stop within 30 seconds; refusing to overlap a replacement.", var7);
         }
      }
   }

   private static boolean ownsAttemptState(JavaPlugin plugin, PluginInitializationLifecycle.Attempt attempt) {
      synchronized (System.getProperties()) {
         return attempt.ownerToken().equals(System.getProperty(ownerProperty(plugin.getName())));
      }
   }

   private static String stateProperty(String pluginName) {
      return "magmacore.init." + pluginName;
   }

   private static String ownerProperty(String pluginName) {
      return stateProperty(pluginName) + ".owner";
   }

   private static void dispatchFailure(
      JavaPlugin plugin,
      PluginInitializationLifecycle lifecycle,
      PluginInitializationLifecycle.Attempt attempt,
      Throwable throwable,
      Consumer<Throwable> onFailure
   ) {
      if (!attempt.predecessor().isDone()) {
         if (attempt.claimAsyncDispatch()) {
            attempt.predecessor()
               .whenComplete((ignored, predecessorFailure) -> dispatchFailureAfterPredecessor(plugin, lifecycle, attempt, throwable, onFailure));
         }
      } else {
         dispatchFailureAfterPredecessor(plugin, lifecycle, attempt, throwable, onFailure);
      }
   }

   private static void dispatchFailureAfterPredecessor(
      JavaPlugin plugin,
      PluginInitializationLifecycle lifecycle,
      PluginInitializationLifecycle.Attempt attempt,
      Throwable throwable,
      Consumer<Throwable> onFailure
   ) {
      if (!isAttemptActive(plugin, lifecycle, attempt)) {
         attempt.completeTerminal();
      } else if (Bukkit.isPrimaryThread()) {
         terminateWithFailureOnMainThread(plugin, lifecycle, attempt, throwable, onFailure);
      } else {
         try {
            boolean scheduled = attempt.scheduleFailureSync(
               () -> Bukkit.getScheduler().runTask(plugin, () -> runFailurePhase(plugin, lifecycle, attempt, throwable, onFailure))
            );
            if (!scheduled) {
               attempt.completeTerminal();
            }
         } catch (Throwable var6) {
            if (var6 != throwable) {
               throwable.addSuppressed(var6);
            }

            publishFailureWithoutMainThread(plugin, lifecycle, attempt, throwable);
            attempt.completeTerminal();
         }
      }
   }

   private static void terminateWithFailureOnMainThread(
      JavaPlugin plugin,
      PluginInitializationLifecycle lifecycle,
      PluginInitializationLifecycle.Attempt attempt,
      Throwable throwable,
      Consumer<Throwable> onFailure
   ) {
      try {
         publishFailureOnMainThread(plugin, lifecycle, attempt, throwable, onFailure);
      } finally {
         attempt.completeTerminal();
      }
   }

   private static void publishFailureOnMainThread(
      JavaPlugin plugin,
      PluginInitializationLifecycle lifecycle,
      PluginInitializationLifecycle.Attempt attempt,
      Throwable throwable,
      Consumer<Throwable> onFailure
   ) {
      if (isAttemptActive(plugin, lifecycle, attempt)) {
         try {
            PluginInitializationProgressBar.complete(plugin, attempt.ownerToken());
            Logger.warn(plugin.getName() + " initialization failed!");
            runInAttemptScope(plugin, lifecycle, attempt, () -> onFailure.accept(throwable));
         } finally {
            if (isAttemptActive(plugin, lifecycle, attempt)) {
               publishAttemptState(plugin, attempt, PluginInitializationState.FAILED);
            }
         }
      }
   }

   private static void publishFailureWithoutMainThread(
      JavaPlugin plugin, PluginInitializationLifecycle lifecycle, PluginInitializationLifecycle.Attempt attempt, Throwable throwable
   ) {
      if (isAttemptActive(plugin, lifecycle, attempt) && publishAttemptState(plugin, attempt, PluginInitializationState.FAILED)) {
         plugin.getLogger()
            .log(Level.SEVERE, plugin.getName() + " initialization failed and its main-thread failure callback could not be scheduled.", throwable);
      }
   }

   private record AttemptScope(String pluginName, PluginInitializationLifecycle lifecycle, PluginInitializationLifecycle.Attempt attempt) {
   }
}
