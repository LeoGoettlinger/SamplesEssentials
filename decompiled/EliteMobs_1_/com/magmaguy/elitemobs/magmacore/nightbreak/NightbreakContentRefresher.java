package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NightbreakContentRefresher {
   private static final ConcurrentMap<NightbreakContentRefresher.RefreshKey, Long> LAST_REFRESH_NANOS = new ConcurrentHashMap<>();

   private NightbreakContentRefresher() {
   }

   public static <T extends NightbreakManagedContent> boolean refreshAsyncIfDue(
      JavaPlugin plugin,
      String catalogKey,
      Duration cooldown,
      Supplier<? extends Collection<? extends T>> packageSupplier,
      Predicate<T> shouldCheckVersion,
      Consumer<List<T>> onComplete
   ) {
      Objects.requireNonNull(plugin, "plugin");
      Objects.requireNonNull(catalogKey, "catalogKey");
      Objects.requireNonNull(cooldown, "cooldown");
      Objects.requireNonNull(packageSupplier, "packageSupplier");
      if (cooldown.isNegative()) {
         throw new IllegalArgumentException("cooldown must not be negative");
      } else {
         NightbreakContentRefresher.RefreshKey key = refreshKey(plugin, catalogKey);
         long now = System.nanoTime();
         long cooldownNanos = cooldown.toNanos();
         AtomicBoolean accepted = new AtomicBoolean(false);
         LAST_REFRESH_NANOS.compute(key, (ignored, previous) -> {
            if (previous != null && now - previous < cooldownNanos) {
               return (Long)previous;
            } else {
               accepted.set(true);
               return now;
            }
         });
         if (!accepted.get()) {
            return false;
         } else {
            Collection<? extends T> suppliedPackages = Objects.requireNonNull((Collection<? extends T>)packageSupplier.get(), "packageSupplier result");
            refreshAsync(plugin, new ArrayList<>(suppliedPackages), shouldCheckVersion, onComplete);
            return true;
         }
      }
   }

   public static void resetRefreshCooldown(JavaPlugin plugin, String catalogKey) {
      LAST_REFRESH_NANOS.remove(refreshKey(Objects.requireNonNull(plugin, "plugin"), Objects.requireNonNull(catalogKey, "catalogKey")));
   }

   public static void shutdown(JavaPlugin plugin) {
      String pluginName = Objects.requireNonNull(plugin, "plugin").getName().toLowerCase(Locale.ROOT);
      LAST_REFRESH_NANOS.keySet().removeIf(key -> key.pluginName().equals(pluginName));
   }

   private static NightbreakContentRefresher.RefreshKey refreshKey(JavaPlugin plugin, String catalogKey) {
      return new NightbreakContentRefresher.RefreshKey(plugin.getName().toLowerCase(Locale.ROOT), catalogKey.toLowerCase(Locale.ROOT));
   }

   public static <T extends NightbreakManagedContent> void refreshAsync(
      JavaPlugin plugin, Collection<T> packages, Predicate<T> shouldCheckVersion, Consumer<List<T>> onComplete
   ) {
      NightbreakContentManager.OperationGate lifecycleGate = NightbreakBulkDownloader.captureOperationGate(plugin);
      refreshAsync(plugin, packages, shouldCheckVersion, lifecycleGate::isCurrent, onComplete, () -> {});
   }

   public static <T extends NightbreakManagedContent> void refreshAsync(
      JavaPlugin plugin,
      Collection<T> packages,
      Predicate<T> shouldCheckVersion,
      BooleanSupplier operationCurrent,
      Consumer<List<T>> onComplete,
      Runnable onCancelled
   ) {
      AtomicBoolean cancellationNotified = new AtomicBoolean(false);
      Runnable cancelOnce = () -> {
         if (cancellationNotified.compareAndSet(false, true)) {
            onCancelled.run();
         }
      };
      Bukkit.getScheduler()
         .runTaskAsynchronously(
            plugin,
            () -> {
               if (!operationCurrent.getAsBoolean()) {
                  cancelOnce.run();
               } else {
                  Map<String, NightbreakAccount.VersionInfo> versionInfoBySlug = new LinkedHashMap<>();
                  NightbreakAccount account = NightbreakAccount.getInstance();
                  if (account != null) {
                     versionInfoBySlug.putAll(account.getAllVersions());
                  }

                  if (!operationCurrent.getAsBoolean()) {
                     cancelOnce.run();
                  } else {
                     if (!versionInfoBySlug.isEmpty()) {
                        NightbreakContentManager.getVersionCache().clear();
                        NightbreakContentManager.getVersionCache().putAll(versionInfoBySlug);
                     }

                     Map<String, NightbreakAccount.AccessInfo> accessInfoBySlug = new LinkedHashMap<>();
                     List<T> newlyOutdated = new ArrayList<>();

                     for (T contentPackage : packages) {
                        if (!operationCurrent.getAsBoolean()) {
                           cancelOnce.run();
                           return;
                        }

                        String slug = contentPackage.getNightbreakSlug();
                        if (slug != null && !slug.isEmpty()) {
                           NightbreakAccount iterationAccount = NightbreakAccount.getInstance();
                           if (iterationAccount != null) {
                              if (NightbreakAccount.hasAuthFailure()) {
                                 contentPackage.setCachedAccessInfo(null);
                                 break;
                              }

                              NightbreakAccount.AccessInfo accessInfo = accessInfoBySlug.computeIfAbsent(slug, key -> {
                                 if (!operationCurrent.getAsBoolean()) {
                                    return null;
                                 } else {
                                    NightbreakAccount.AccessInfo fetched = iterationAccount.checkAccess(key);
                                    if (fetched != null && operationCurrent.getAsBoolean()) {
                                       NightbreakContentManager.cacheAccess(key, fetched, iterationAccount);
                                    }

                                    return fetched;
                                 }
                              });
                              if (!operationCurrent.getAsBoolean()) {
                                 cancelOnce.run();
                                 return;
                              }

                              contentPackage.setCachedAccessInfo(accessInfo);
                              if (NightbreakAccount.hasAuthFailure()) {
                                 break;
                              }
                           } else {
                              contentPackage.setCachedAccessInfo(null);
                           }

                           if (shouldCheckVersion.test(contentPackage) && contentPackage.isInstalled()) {
                              NightbreakAccount.VersionInfo versionInfo = versionInfoBySlug.get(slug);
                              if (versionInfo != null) {
                                 boolean outOfDate = versionInfo.versionInt > contentPackage.getLocalVersion();
                                 if (!operationCurrent.getAsBoolean()) {
                                    cancelOnce.run();
                                    return;
                                 }

                                 if (outOfDate && !contentPackage.isOutOfDate()) {
                                    newlyOutdated.add(contentPackage);
                                    Logger.warn(
                                       "Content "
                                          + contentPackage.getDisplayName()
                                          + " is outdated! Your version: "
                                          + contentPackage.getLocalVersion()
                                          + " / remote version: "
                                          + versionInfo.versionInt
                                          + " / link: "
                                          + contentPackage.getDownloadLink()
                                    );
                                 }

                                 contentPackage.setOutOfDate(outOfDate);
                              }
                           }
                        }
                     }

                     if (!operationCurrent.getAsBoolean()) {
                        cancelOnce.run();
                     } else {
                        try {
                           Bukkit.getScheduler().runTask(plugin, () -> {
                              if (operationCurrent.getAsBoolean()) {
                                 onComplete.accept(newlyOutdated);
                              } else {
                                 cancelOnce.run();
                              }
                           });
                        } catch (RuntimeException var16) {
                           cancelOnce.run();
                        }
                     }
                  }
               }
            }
         );
   }

   private record RefreshKey(String pluginName, String catalogKey) {
   }
}
