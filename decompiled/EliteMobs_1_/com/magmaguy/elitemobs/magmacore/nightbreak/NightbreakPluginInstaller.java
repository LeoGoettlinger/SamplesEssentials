package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class NightbreakPluginInstaller {
   private static final Set<String> RUNNING_INSTALLS = ConcurrentHashMap.newKeySet();

   private NightbreakPluginInstaller() {
   }

   public static void downloadPluginAsync(
      JavaPlugin ownerPlugin, NightbreakPluginCatalog.Entry entry, CommandSender sender, Consumer<NightbreakPluginInstaller.InstallResult> callback
   ) {
      long generation = NightbreakPluginUpdater.lifecycleGeneration(ownerPlugin);
      NightbreakPluginAsyncWorkTracker.Work work = NightbreakPluginUpdater.registerAsyncWork(ownerPlugin, generation);
      if (work != null) {
         try {
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(ownerPlugin, () -> {
               if (work.start()) {
                  try {
                     if (!NightbreakPluginUpdater.isLifecycleCurrent(ownerPlugin, generation)) {
                        return;
                     }

                     NightbreakPluginInstaller.InstallResult result = downloadPlugin(ownerPlugin, entry, sender, generation);
                     if (NightbreakPluginUpdater.isLifecycleCurrent(ownerPlugin, generation)) {
                        Bukkit.getScheduler().runTask(ownerPlugin, () -> {
                           if (NightbreakPluginUpdater.isLifecycleCurrent(ownerPlugin, generation)) {
                              sendInstallResult(sender, result);
                              if (callback != null) {
                                 callback.accept(result);
                              }
                           }
                        });
                        return;
                     }
                  } finally {
                     work.close();
                  }
               }
            });
            work.attach(task);
         } catch (RuntimeException var8) {
            work.dispatchFailed();
            throw var8;
         }
      }
   }

   private static NightbreakPluginInstaller.InstallResult downloadPlugin(
      JavaPlugin ownerPlugin, NightbreakPluginCatalog.Entry entry, CommandSender sender, long generation
   ) {
      String runningKey = ownerPlugin.getName().toLowerCase(Locale.ROOT) + ":" + entry.slug();
      if (!RUNNING_INSTALLS.add(runningKey)) {
         return new NightbreakPluginInstaller.InstallResult(
            NightbreakPluginInstaller.InstallStatus.DOWNLOAD_FAILED, entry, null, null, "An install for this plugin is already in progress."
         );
      } else {
         NightbreakPluginInstaller.InstallResult var6;
         try {
            var6 = performDownloadPlugin(ownerPlugin, entry, sender, generation);
         } finally {
            RUNNING_INSTALLS.remove(runningKey);
         }

         return var6;
      }
   }

   private static NightbreakPluginInstaller.InstallResult performDownloadPlugin(
      JavaPlugin ownerPlugin, NightbreakPluginCatalog.Entry entry, CommandSender sender, long generation
   ) {
      if (!NightbreakPluginUpdater.isLifecycleCurrent(ownerPlugin, generation)) {
         return new NightbreakPluginInstaller.InstallResult(
            NightbreakPluginInstaller.InstallStatus.DOWNLOAD_FAILED, entry, null, null, "Plugin lifecycle changed before the install started."
         );
      } else if (entry.sourceType() == NightbreakPluginCatalog.SourceType.EXTERNAL) {
         return new NightbreakPluginInstaller.InstallResult(NightbreakPluginInstaller.InstallStatus.EXTERNAL_ONLY, entry, null, null, null);
      } else if (NightbreakPluginCatalog.isInstalled(entry)) {
         return new NightbreakPluginInstaller.InstallResult(NightbreakPluginInstaller.InstallStatus.ALREADY_INSTALLED, entry, null, null, null);
      } else if (!NightbreakAccount.hasToken()) {
         return new NightbreakPluginInstaller.InstallResult(NightbreakPluginInstaller.InstallStatus.NO_TOKEN, entry, null, null, null);
      } else if (NightbreakAccount.hasAuthFailure()) {
         return new NightbreakPluginInstaller.InstallResult(NightbreakPluginInstaller.InstallStatus.AUTH_FAILURE, entry, null, null, null);
      } else {
         NightbreakAccount.VersionInfo versionInfo = NightbreakAccount.getPublicPluginVersion(entry.slug());
         if (versionInfo != null && versionInfo.version != null && !versionInfo.version.isBlank()) {
            NightbreakAccount account = NightbreakAccount.getInstance();
            if (account == null) {
               return new NightbreakPluginInstaller.InstallResult(NightbreakPluginInstaller.InstallStatus.NO_TOKEN, entry, versionInfo.version, null, null);
            } else {
               File updateFolder = resolveUpdateFolder(ownerPlugin);
               if (!updateFolder.exists() && !updateFolder.mkdirs()) {
                  return new NightbreakPluginInstaller.InstallResult(
                     NightbreakPluginInstaller.InstallStatus.DOWNLOAD_FAILED,
                     entry,
                     versionInfo.version,
                     null,
                     "Could not create update folder: " + updateFolder.getPath()
                  );
               } else {
                  String targetFileName = targetFileName(entry, versionInfo.fileName);
                  if (targetFileName == null) {
                     return new NightbreakPluginInstaller.InstallResult(
                        NightbreakPluginInstaller.InstallStatus.DOWNLOAD_FAILED,
                        entry,
                        versionInfo.version,
                        null,
                        "The update service returned an unsafe plugin filename."
                     );
                  } else {
                     File tempFile = new File(updateFolder, targetFileName + "." + generation + "." + UUID.randomUUID() + ".download");
                     File targetFile = new File(updateFolder, targetFileName);
                     File legacyTempFile = new File(updateFolder, targetFileName + ".download");
                     if (legacyTempFile.exists() && !legacyTempFile.delete()) {
                        return new NightbreakPluginInstaller.InstallResult(
                           NightbreakPluginInstaller.InstallStatus.DOWNLOAD_FAILED,
                           entry,
                           versionInfo.version,
                           null,
                           "Could not clear a legacy temporary download."
                        );
                     } else {
                        long[] lastProgressMessage = new long[]{0L};
                        NightbreakAccount.PluginDownloadResult downloadResult = account.downloadPluginUpdate(
                           entry.slug(),
                           tempFile,
                           (bytesDownloaded, totalBytes) -> {
                              if (NightbreakPluginUpdater.isLifecycleCurrent(ownerPlugin, generation)) {
                                 if (canMessage(sender)) {
                                    long now = System.currentTimeMillis();
                                    if (now - lastProgressMessage[0] >= 2000L) {
                                       lastProgressMessage[0] = now;
                                       String progress = totalBytes > 0L
                                          ? String.format(Locale.ROOT, "%.1f%%", bytesDownloaded * 100.0 / totalBytes)
                                          : formatBytes(bytesDownloaded);
                                       Bukkit.getScheduler().runTask(ownerPlugin, () -> {
                                          if (NightbreakPluginUpdater.isLifecycleCurrent(ownerPlugin, generation) && canMessage(sender)) {
                                             Logger.sendSimpleMessage(sender, "&7[" + entry.displayName() + "] Downloading plugin... " + progress);
                                          }
                                       });
                                    }
                                 }
                              }
                           },
                           () -> NightbreakPluginUpdater.isLifecycleCurrent(ownerPlugin, generation)
                        );
                        if (downloadResult.success && tempFile.exists()) {
                           if (!NightbreakPluginUpdater.isLifecycleCurrent(ownerPlugin, generation)) {
                              tempFile.delete();
                              return new NightbreakPluginInstaller.InstallResult(
                                 NightbreakPluginInstaller.InstallStatus.DOWNLOAD_FAILED,
                                 entry,
                                 versionInfo.version,
                                 null,
                                 "Plugin lifecycle changed before the install could be validated."
                              );
                           } else {
                              if (versionInfo.checksum != null && !versionInfo.checksum.isBlank()) {
                                 String actualChecksum;
                                 try {
                                    actualChecksum = sha256(tempFile);
                                 } catch (IllegalStateException var16) {
                                    tempFile.delete();
                                    return new NightbreakPluginInstaller.InstallResult(
                                       NightbreakPluginInstaller.InstallStatus.CHECKSUM_FAILED, entry, versionInfo.version, null, var16.getMessage()
                                    );
                                 }

                                 if (!versionInfo.checksum.equalsIgnoreCase(actualChecksum)) {
                                    tempFile.delete();
                                    return new NightbreakPluginInstaller.InstallResult(
                                       NightbreakPluginInstaller.InstallStatus.CHECKSUM_FAILED,
                                       entry,
                                       versionInfo.version,
                                       null,
                                       "Expected " + versionInfo.checksum + ", got " + actualChecksum
                                    );
                                 }
                              }

                              DownloadedPluginJarValidator.ValidationResult jarValidation = DownloadedPluginJarValidator.validate(
                                 tempFile, entry.allPluginNames(), versionInfo.version
                              );
                              if (!jarValidation.valid()) {
                                 tempFile.delete();
                                 return new NightbreakPluginInstaller.InstallResult(
                                    NightbreakPluginInstaller.InstallStatus.DOWNLOAD_FAILED, entry, versionInfo.version, null, jarValidation.detail()
                                 );
                              } else {
                                 try {
                                    boolean published = NightbreakPluginUpdater.publishIfLifecycleCurrent(
                                       ownerPlugin, generation, () -> moveReplacing(tempFile, targetFile)
                                    );
                                    if (!published) {
                                       tempFile.delete();
                                       return new NightbreakPluginInstaller.InstallResult(
                                          NightbreakPluginInstaller.InstallStatus.DOWNLOAD_FAILED,
                                          entry,
                                          versionInfo.version,
                                          null,
                                          "Plugin lifecycle changed before the install could be published."
                                       );
                                    }
                                 } catch (IOException var17) {
                                    tempFile.delete();
                                    return new NightbreakPluginInstaller.InstallResult(
                                       NightbreakPluginInstaller.InstallStatus.DOWNLOAD_FAILED, entry, versionInfo.version, null, var17.getMessage()
                                    );
                                 }

                                 return new NightbreakPluginInstaller.InstallResult(
                                    NightbreakPluginInstaller.InstallStatus.DOWNLOADED, entry, versionInfo.version, targetFile, null
                                 );
                              }
                           }
                        } else {
                           if (tempFile.exists()) {
                              tempFile.delete();
                           }

                           return new NightbreakPluginInstaller.InstallResult(
                              statusFor(downloadResult), entry, versionInfo.version, null, downloadResult.displayDetail()
                           );
                        }
                     }
                  }
               }
            }
         } else {
            return new NightbreakPluginInstaller.InstallResult(NightbreakPluginInstaller.InstallStatus.REMOTE_UNREACHABLE, entry, null, null, null);
         }
      }
   }

   private static void sendInstallResult(CommandSender sender, NightbreakPluginInstaller.InstallResult result) {
      if (canMessage(sender)) {
         NightbreakPluginCatalog.Entry entry = result.entry();
         Logger.sendSimpleMessage(sender, NightbreakSetupMenuHelper.getSeparator());
         switch (result.status()) {
            case DOWNLOADED:
               Logger.sendSimpleMessage(sender, "<g:#77dd77:#d4ff9d>" + entry.displayName() + " downloaded</g>&7.");
               if (result.remoteVersion() != null) {
                  Logger.sendSimpleMessage(sender, "&7Version: &a" + result.remoteVersion());
               }

               Logger.sendSimpleMessage(sender, "&7Restart the server to use it.");
               break;
            case ALREADY_INSTALLED:
               Logger.sendSimpleMessage(sender, "&a" + entry.displayName() + " is already installed.");
               break;
            case NO_TOKEN:
               Logger.sendSimpleMessage(sender, "&eConnect this server before installing " + entry.displayName() + " from in-game.");
               sendAccountAndManualLinks(sender, entry);
               break;
            case AUTH_FAILURE:
               NightbreakSetupMenuHelper.sendTokenUpdatePrompt(sender, entry.displayName());
               sendManualDownload(sender, entry);
               break;
            case NO_ACCESS:
               Logger.sendSimpleMessage(sender, "&eThe server declined this in-game install for &f" + entry.displayName() + "&e.");
               Logger.sendSimpleMessage(sender, "&7In-game plugin installs are a supporter convenience.");
               if (result.detail() != null && !result.detail().isBlank()) {
                  Logger.sendSimpleMessage(sender, "&8Remote says: &7" + result.detail());
               }

               sendManualDownload(sender, entry);
               break;
            case REMOTE_UNREACHABLE:
               Logger.sendSimpleMessage(sender, "&cThe plugin download service could not be reached.");
               sendManualDownload(sender, entry);
               break;
            case DOWNLOAD_FAILED:
               Logger.sendSimpleMessage(sender, "&cThe in-game plugin install could not be completed.");
               if (result.detail() != null && !result.detail().isBlank()) {
                  Logger.sendSimpleMessage(sender, "&8Details: &7" + result.detail());
               }

               sendManualDownload(sender, entry);
               break;
            case CHECKSUM_FAILED:
               Logger.sendSimpleMessage(sender, "&cThe downloaded plugin failed checksum verification.");
               Logger.sendSimpleMessage(sender, "&7The file was not kept. Please use the manual page or try again later.");
               sendManualDownload(sender, entry);
               break;
            case EXTERNAL_ONLY:
               Logger.sendSimpleMessage(sender, "&e" + entry.displayName() + " is hosted externally.");
               sendManualDownload(sender, entry);
         }

         Logger.sendSimpleMessage(sender, NightbreakSetupMenuHelper.getSeparator());
      }
   }

   private static void sendAccountAndManualLinks(CommandSender sender, NightbreakPluginCatalog.Entry entry) {
      sendLink(sender, "&7Account token: ", "&9&nhttps://nightbreak.io/account/", "&7Click to open the account token page.", "https://nightbreak.io/account/");
      if (sender instanceof Player player) {
         player.spigot()
            .sendMessage(
               new BaseComponent[]{
                  SpigotMessage.simpleMessage("&7Link command: "),
                  SpigotMessage.commandHoverMessage("&a/nightbreaklogin <token>", "&7Click to prepare the token command.", "/nightbreaklogin ")
               }
            );
      } else {
         Logger.sendSimpleMessage(sender, "&7Link command: &a/nightbreaklogin <token>");
      }

      sendManualDownload(sender, entry);
   }

   private static void sendManualDownload(CommandSender sender, NightbreakPluginCatalog.Entry entry) {
      sendLink(sender, "&7Manual download page: ", "&9&n" + entry.downloadPageUrl(), "&7Click to open the public download page.", entry.downloadPageUrl());
   }

   private static void sendLink(CommandSender sender, String prefix, String label, String hover, String url) {
      if (sender instanceof Player player) {
         player.spigot().sendMessage(new BaseComponent[]{SpigotMessage.simpleMessage(prefix), SpigotMessage.hoverLinkMessage(label, hover, url)});
      } else {
         Logger.sendSimpleMessage(sender, prefix + label);
      }
   }

   private static NightbreakPluginInstaller.InstallStatus statusFor(NightbreakAccount.PluginDownloadResult downloadResult) {
      if (downloadResult.responseCode == 401 || NightbreakAccount.hasAuthFailure()) {
         return NightbreakPluginInstaller.InstallStatus.AUTH_FAILURE;
      } else if ("NO_TOKEN".equalsIgnoreCase(downloadResult.error)) {
         return NightbreakPluginInstaller.InstallStatus.NO_TOKEN;
      } else if (downloadResult.responseCode == 403) {
         return NightbreakPluginInstaller.InstallStatus.NO_ACCESS;
      } else {
         return downloadResult.responseCode != 0 && !"NETWORK_ERROR".equalsIgnoreCase(downloadResult.error)
            ? NightbreakPluginInstaller.InstallStatus.DOWNLOAD_FAILED
            : NightbreakPluginInstaller.InstallStatus.REMOTE_UNREACHABLE;
      }
   }

   private static String targetFileName(NightbreakPluginCatalog.Entry entry, String remoteFileName) {
      String catalogFileName = safeJarFileName(entry.jarFileName());
      if (catalogFileName != null) {
         return catalogFileName;
      } else {
         return remoteFileName != null && !remoteFileName.isBlank()
            ? safeJarFileName(remoteFileName)
            : safeJarFileName(entry.displayName().replace(" ", "") + ".jar");
      }
   }

   private static String safeJarFileName(String candidate) {
      String value = candidate == null ? "" : candidate.trim();
      return !value.isBlank()
            && value.toLowerCase(Locale.ROOT).endsWith(".jar")
            && value.indexOf(47) < 0
            && value.indexOf(92) < 0
            && value.indexOf(58) < 0
            && value.indexOf(0) < 0
            && !".".equals(value)
            && !"..".equals(value)
         ? value
         : null;
   }

   private static File resolveUpdateFolder(JavaPlugin plugin) {
      try {
         if (Bukkit.getServer().getClass().getMethod("getUpdateFolderFile").invoke(Bukkit.getServer()) instanceof File file) {
            return file;
         }
      } catch (ReflectiveOperationException var3) {
      }

      File pluginsFolder = plugin.getDataFolder().getParentFile();
      return new File(pluginsFolder, "update");
   }

   private static void moveReplacing(File source, File target) throws IOException {
      try {
         Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (IOException var3) {
         Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
   }

   private static String sha256(File file) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");

         try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];

            int read;
            while ((read = inputStream.read(buffer)) != -1) {
               digest.update(buffer, 0, read);
            }
         }

         StringBuilder var10 = new StringBuilder();

         for (byte b : digest.digest()) {
            var10.append(String.format("%02x", b));
         }

         return var10.toString();
      } catch (NoSuchAlgorithmException | IOException var9) {
         throw new IllegalStateException("Could not calculate checksum", var9);
      }
   }

   private static boolean canMessage(CommandSender sender) {
      return sender != null && !(sender instanceof Player player && !player.isOnline());
   }

   private static String formatBytes(long bytes) {
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         return bytes < 1048576L ? String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0) : String.format(Locale.ROOT, "%.1f MB", bytes / 1048576.0);
      }
   }

   public record InstallResult(
      NightbreakPluginInstaller.InstallStatus status, NightbreakPluginCatalog.Entry entry, String remoteVersion, File downloadedFile, String detail
   ) {
   }

   public static enum InstallStatus {
      DOWNLOADED,
      ALREADY_INSTALLED,
      NO_TOKEN,
      AUTH_FAILURE,
      NO_ACCESS,
      REMOTE_UNREACHABLE,
      DOWNLOAD_FAILED,
      CHECKSUM_FAILED,
      EXTERNAL_ONLY;
   }
}
