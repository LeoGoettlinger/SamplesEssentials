package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NightbreakContentManager {
   private static final Map<String, NightbreakContentManager.CachedAccess> accessCache = new ConcurrentHashMap<>();
   private static final Map<String, NightbreakAccount.VersionInfo> versionCache = new ConcurrentHashMap<>();
   private static volatile long lastCacheRefresh = 0L;
   private static final long CACHE_TTL_MS = 300000L;

   public static Map<String, NightbreakAccount.AccessInfo> getAccessCache() {
      NightbreakAccount account = NightbreakAccount.getInstance();
      long now = System.currentTimeMillis();
      Map<String, NightbreakAccount.AccessInfo> result = new HashMap<>();
      accessCache.forEach((slug, cached) -> {
         if (cached.isFresh(account, now)) {
            result.put(slug, cached.info());
         }
      });
      return Map.copyOf(result);
   }

   static void cacheAccess(String slug, NightbreakAccount.AccessInfo info, NightbreakAccount account) {
      if (info != null && account != null && account == NightbreakAccount.getInstance()) {
         accessCache.put(slug, new NightbreakContentManager.CachedAccess(account, info, System.currentTimeMillis()));
      }
   }

   public static boolean isCacheStale() {
      return System.currentTimeMillis() - lastCacheRefresh > 300000L;
   }

   public static void clearCache() {
      accessCache.clear();
      versionCache.clear();
      lastCacheRefresh = 0L;
   }

   public static void refreshVersionCache() {
      refreshVersionCache(MagmaCore.getInstance().getRequestingPlugin());
   }

   public static void refreshVersionCache(JavaPlugin ownerPlugin) {
      if (NightbreakAccount.hasToken()) {
         Map<String, NightbreakAccount.VersionInfo> versions = NightbreakAccount.getInstance().getAllVersions();
         if (!versions.isEmpty()) {
            versionCache.clear();
            versionCache.putAll(versions);
            lastCacheRefresh = System.currentTimeMillis();
            Logger.info("Refreshed Nightbreak version cache with " + versions.size() + " entries");
         }
      }
   }

   public static void checkAccessAsync(String slug, Consumer<NightbreakAccount.AccessInfo> callback) {
      checkAccessAsync(MagmaCore.getInstance().getRequestingPlugin(), slug, callback);
   }

   public static void checkAccessAsync(JavaPlugin ownerPlugin, String slug, Consumer<NightbreakAccount.AccessInfo> callback) {
      NightbreakContentManager.OperationGate lifecycleGate = NightbreakBulkDownloader.captureOperationGate(ownerPlugin);
      checkAccessAsync(ownerPlugin, slug, lifecycleGate, callback);
   }

   static void checkAccessAsync(
      JavaPlugin ownerPlugin, String slug, NightbreakContentManager.OperationGate lifecycleGate, Consumer<NightbreakAccount.AccessInfo> callback
   ) {
      checkAccessAsync(ownerPlugin, slug, lifecycleGate::isCurrent, callback);
   }

   private static void checkAccessAsync(JavaPlugin ownerPlugin, String slug, BooleanSupplier operationCurrent, Consumer<NightbreakAccount.AccessInfo> callback) {
      if (operationCurrent.getAsBoolean()) {
         if (!NightbreakAccount.hasToken()) {
            callback.accept(null);
         } else if (NightbreakAccount.hasAuthFailure()) {
            callback.accept(null);
         } else {
            NightbreakContentManager.CachedAccess cached = accessCache.get(slug);
            if (cached != null && cached.isFresh(NightbreakAccount.getInstance(), System.currentTimeMillis())) {
               if (operationCurrent.getAsBoolean()) {
                  callback.accept(cached.info());
               }
            } else {
               Bukkit.getScheduler().runTaskAsynchronously(ownerPlugin, () -> {
                  if (operationCurrent.getAsBoolean()) {
                     NightbreakAccount account = NightbreakAccount.getInstance();
                     NightbreakAccount.AccessInfo info = account == null ? null : account.checkAccess(slug);
                     if (info != null && operationCurrent.getAsBoolean()) {
                        cacheAccess(slug, info, account);
                     }

                     if (operationCurrent.getAsBoolean()) {
                        Bukkit.getScheduler().runTask(ownerPlugin, () -> {
                           if (operationCurrent.getAsBoolean()) {
                              callback.accept(info);
                           }
                        });
                     }
                  }
               });
            }
         }
      }
   }

   public static void downloadAsync(String slug, File destinationFolder, Player player, Consumer<Boolean> onComplete) {
      downloadAsync(MagmaCore.getInstance().getRequestingPlugin(), slug, destinationFolder, player, onComplete);
   }

   public static void downloadAsync(JavaPlugin ownerPlugin, String slug, File destinationFolder, Player player, Consumer<Boolean> onComplete) {
      downloadAsync(ownerPlugin, slug, destinationFolder, player, NightbreakBulkDownloader.captureOperationGate(ownerPlugin), onComplete);
   }

   static void downloadAsync(
      JavaPlugin ownerPlugin,
      String slug,
      File destinationFolder,
      Player player,
      NightbreakContentManager.OperationGate publicationGate,
      Consumer<Boolean> onComplete
   ) {
      BooleanSupplier operationCurrent = publicationGate == null ? () -> true : publicationGate::isCurrent;
      if (operationCurrent.getAsBoolean()) {
         if (!NightbreakAccount.hasToken()) {
            if (player != null && player.isOnline()) {
               player.sendMessage("§c[Nightbreak] No token registered. Use /nightbreaklogin <token> first.");
            }

            onComplete.accept(false);
         } else {
            checkAccessAsync(
               ownerPlugin,
               slug,
               operationCurrent,
               accessInfo -> {
                  if (operationCurrent.getAsBoolean()) {
                     if (accessInfo != null && accessInfo.hasAccess) {
                        NightbreakAccount.VersionInfo versionInfo = versionCache.get(slug);
                        String fileName = versionInfo != null && versionInfo.fileName != null ? versionInfo.fileName : slug + ".zip";
                        Path destinationRoot = destinationFolder.toPath().toAbsolutePath().normalize();
                        Path destinationFile = destinationRoot.resolve(fileName).normalize();
                        if (!destinationRoot.equals(destinationFile.getParent())) {
                           Logger.warn("Refusing unsafe Nightbreak download filename for '" + slug + "': " + fileName);
                           onComplete.accept(false);
                        } else {
                           if (player != null && player.isOnline()) {
                              player.sendMessage("§a[Nightbreak] Starting download of " + slug + "...");
                           }

                           Bukkit.getScheduler()
                              .runTaskAsynchronously(
                                 ownerPlugin,
                                 () -> {
                                    long[] lastUpdate = new long[]{0L};
                                    Path temporaryFile = null;
                                    boolean success = false;

                                    try {
                                       if (operationCurrent.getAsBoolean()) {
                                          Files.createDirectories(destinationRoot);
                                          temporaryFile = Files.createTempFile(destinationRoot, ".nightbreak-", ".download");
                                          NightbreakAccount account = NightbreakAccount.getInstance();
                                          boolean downloaded = account != null
                                             && account.download(
                                                slug,
                                                temporaryFile.toFile(),
                                                null,
                                                (bytesDownloaded, totalBytes) -> {
                                                   if (operationCurrent.getAsBoolean()) {
                                                      if (player != null && player.isOnline() && System.currentTimeMillis() - lastUpdate[0] > 2000L) {
                                                         lastUpdate[0] = System.currentTimeMillis();
                                                         String progress = totalBytes > 0L
                                                            ? String.format("%.1f%%", bytesDownloaded * 100.0 / totalBytes)
                                                            : formatBytes(bytesDownloaded);
                                                         Bukkit.getScheduler().runTask(ownerPlugin, () -> {
                                                            if (operationCurrent.getAsBoolean() && player.isOnline()) {
                                                               player.sendMessage("§7[Nightbreak] Downloading... " + progress);
                                                            }
                                                         });
                                                      }
                                                   }
                                                }
                                             );
                                          if (downloaded && operationCurrent.getAsBoolean()) {
                                             success = publicationGate == null
                                                ? publishDownloadedFile(temporaryFile, destinationFile)
                                                : publicationGate.publish(temporaryFile, destinationFile);
                                          }

                                          return;
                                       }
                                    } catch (RuntimeException | IOException var22) {
                                       Logger.warn("Failed to publish Nightbreak download '" + slug + "': " + var22.getMessage());
                                       return;
                                    } finally {
                                       if (temporaryFile != null) {
                                          try {
                                             Files.deleteIfExists(temporaryFile);
                                          } catch (IOException var21) {
                                             Logger.warn("Failed to remove temporary Nightbreak download " + temporaryFile + ": " + var21.getMessage());
                                          }
                                       }

                                       completeDownload(ownerPlugin, player, operationCurrent, success, onComplete);
                                    }
                                 }
                              );
                        }
                     } else {
                        if (player != null && player.isOnline()) {
                           if (accessInfo == null && NightbreakAccount.hasAuthFailure()) {
                              NightbreakSetupMenuHelper.sendTokenUpdatePrompt(player, ownerPlugin.getName());
                           } else {
                              player.sendMessage("§c[Nightbreak] You don't have access to this content.");
                           }

                           if (accessInfo != null && !accessInfo.hasAccess) {
                              showAccessLinks(player, accessInfo);
                           }
                        }

                        onComplete.accept(false);
                     }
                  }
               }
            );
         }
      }
   }

   private static boolean publishDownloadedFile(Path temporaryFile, Path destinationFile) throws IOException {
      try {
         Files.move(temporaryFile, destinationFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException var3) {
         Files.move(temporaryFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
      }

      return true;
   }

   private static void completeDownload(JavaPlugin ownerPlugin, Player player, BooleanSupplier operationCurrent, boolean success, Consumer<Boolean> onComplete) {
      if (operationCurrent.getAsBoolean()) {
         try {
            Bukkit.getScheduler().runTask(ownerPlugin, () -> {
               if (operationCurrent.getAsBoolean()) {
                  if (success) {
                     if (player != null && player.isOnline()) {
                        player.sendMessage("§a[Nightbreak] Download complete! File saved to imports folder.");
                     }
                  } else if (player != null && player.isOnline()) {
                     player.sendMessage("§c[Nightbreak] Download failed. Please try again later.");
                  }

                  onComplete.accept(success);
               }
            });
         } catch (RuntimeException var6) {
         }
      }
   }

   public static void showAccessLinks(Player player, NightbreakAccount.AccessInfo accessInfo) {
      player.sendMessage("§6----------------------------------------------------");
      player.sendMessage("§eYou can get access to this content through:");
      if (accessInfo.patreonLink != null && !accessInfo.patreonLink.isEmpty()) {
         player.sendMessage("§6• Patreon: §9" + accessInfo.patreonLink);
      }

      if (accessInfo.itchLink != null && !accessInfo.itchLink.isEmpty()) {
         player.sendMessage("§6• itch.io: §9" + accessInfo.itchLink);
      }

      if ((accessInfo.patreonLink == null || accessInfo.patreonLink.isEmpty()) && (accessInfo.itchLink == null || accessInfo.itchLink.isEmpty())) {
         player.sendMessage("§6• Visit: §9https://nightbreak.io");
      }

      player.sendMessage("§6----------------------------------------------------");
   }

   private static String formatBytes(long bytes) {
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         return bytes < 1048576L ? String.format("%.1f KB", bytes / 1024.0) : String.format("%.1f MB", bytes / 1048576.0);
      }
   }

   public static void shutdown() {
      clearCache();
   }

   @Generated
   public static Map<String, NightbreakAccount.VersionInfo> getVersionCache() {
      return versionCache;
   }

   private record CachedAccess(NightbreakAccount account, NightbreakAccount.AccessInfo info, long fetchedAt) {
      boolean isFresh(NightbreakAccount currentAccount, long now) {
         return this.account == currentAccount && now - this.fetchedAt < 300000L;
      }
   }

   interface OperationGate {
      boolean isCurrent();

      boolean publish(Path var1, Path var2) throws IOException;
   }
}
