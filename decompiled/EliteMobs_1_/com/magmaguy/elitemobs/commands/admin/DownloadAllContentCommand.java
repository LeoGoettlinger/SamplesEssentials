package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.MetaPackage;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakContentManager;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakSetupMenuHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DownloadAllContentCommand extends AdvancedCommand {
   static final AtomicBoolean IS_BULK_DOWNLOADING = new AtomicBoolean(false);

   public DownloadAllContentCommand() {
      super(List.of("downloadallcontent"));
      this.setPermission("elitemobs.downloadall");
      this.setSenderType(SenderType.ANY);
      this.setDescription("Downloads all available content packages via Nightbreak");
      this.setUsage("/em downloadallcontent");
   }

   @Override
   public void execute(CommandData commandData) {
      CommandSender sender = commandData.getCommandSender();
      if (!NightbreakAccount.hasToken()) {
         sender.sendMessage(CommandMessagesConfig.getDownloadAllNoTokenMessage());
      } else if (NightbreakAccount.hasAuthFailure()) {
         NightbreakSetupMenuHelper.sendTokenUpdatePrompt(sender, "EliteMobs");
      } else if (!IS_BULK_DOWNLOADING.compareAndSet(false, true)) {
         sender.sendMessage(CommandMessagesConfig.getDownloadAllAlreadyRunningMessage());
      } else {
         Set<String> metaChildren = EMPackage.getMetaPackageChildFilenames();
         List<EMPackage> downloadable = new ArrayList<>();
         Set<String> seenSlugs = new HashSet<>();
         boolean hasAnySlug = false;

         for (EMPackage pkg : EMPackage.getEmPackages().values()) {
            if (!metaChildren.contains(pkg.getContentPackagesConfigFields().getFilename())) {
               if (pkg instanceof MetaPackage) {
                  pkg.refreshState();
               }

               String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
               if (slug != null && !slug.isEmpty()) {
                  hasAnySlug = true;
                  if (!seenSlugs.contains(slug)
                     && pkg.getCachedAccessInfo() != null
                     && pkg.getCachedAccessInfo().hasAccess
                     && (!pkg.isDownloaded() || pkg.isOutOfDate())) {
                     downloadable.add(pkg);
                     seenSlugs.add(slug);
                  }
               }
            }
         }

         if (downloadable.isEmpty()) {
            IS_BULK_DOWNLOADING.set(false);
            if (!hasAnySlug) {
               sender.sendMessage(CommandMessagesConfig.getDownloadAllNoAccessMessage());
            } else {
               sender.sendMessage(CommandMessagesConfig.getDownloadAllNothingMessage());
            }
         } else {
            sender.sendMessage(CommandMessagesConfig.getDownloadAllFoundMessage().replace("$count", String.valueOf(downloadable.size())));
            Player player = sender instanceof Player ? (Player)sender : null;
            File importsFolder = new File(MetadataHandler.PLUGIN.getDataFolder(), "imports");
            if (!importsFolder.exists()) {
               importsFolder.mkdirs();
            }

            AtomicInteger completed = new AtomicInteger(0);
            AtomicInteger failed = new AtomicInteger(0);
            List<String> failedNames = new ArrayList<>();
            this.downloadNextPackage(downloadable, 0, importsFolder, player, sender, completed, failed, failedNames);
         }
      }
   }

   private void downloadNextPackage(
      List<EMPackage> packages,
      int index,
      File importsFolder,
      Player player,
      CommandSender sender,
      AtomicInteger completed,
      AtomicInteger failed,
      List<String> failedNames
   ) {
      if (player != null && !player.isOnline()) {
         if (index >= packages.size()) {
            IS_BULK_DOWNLOADING.set(false);
            if (completed.get() > 0) {
               Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> ReloadCommand.reload(Bukkit.getConsoleSender()), 20L);
            }
         } else {
            EMPackage pkg = packages.get(index);
            String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
            String name = pkg.getContentPackagesConfigFields().getName();
            NightbreakContentManager.downloadAsync(MetadataHandler.PLUGIN, slug, importsFolder, null, success -> {
               if (success) {
                  completed.incrementAndGet();
               } else {
                  if (abortIfAuthFailure(sender, player)) {
                     return;
                  }

                  failed.incrementAndGet();
                  failedNames.add(name);
               }

               this.downloadNextPackage(packages, index + 1, importsFolder, player, sender, completed, failed, failedNames);
            });
         }
      } else if (index >= packages.size()) {
         IS_BULK_DOWNLOADING.set(false);
         sender.sendMessage(
            CommandMessagesConfig.getDownloadAllCompleteMessage()
               .replace("$completed", String.valueOf(completed.get()))
               .replace("$failed", String.valueOf(failed.get()))
         );
         if (!failedNames.isEmpty()) {
            sender.sendMessage(CommandMessagesConfig.getDownloadAllFailedListMessage().replace("$names", String.join(", ", failedNames)));
         }

         if (completed.get() > 0) {
            sender.sendMessage(CommandMessagesConfig.getDownloadAllReloadingMessage());
            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> ReloadCommand.reload(sender), 20L);
         }
      } else {
         EMPackage pkg = packages.get(index);
         String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
         String name = pkg.getContentPackagesConfigFields().getName();
         sender.sendMessage(
            CommandMessagesConfig.getDownloadAllProgressMessage()
               .replace("$current", String.valueOf(index + 1))
               .replace("$total", String.valueOf(packages.size()))
               .replace("$name", name)
         );
         NightbreakContentManager.downloadAsync(
            MetadataHandler.PLUGIN,
            slug,
            importsFolder,
            null,
            success -> {
               if (success) {
                  completed.incrementAndGet();
                  if (player == null || player.isOnline()) {
                     int remaining = packages.size() - (index + 1);
                     if (remaining > 0) {
                        sender.sendMessage(
                           CommandMessagesConfig.getDownloadAllDownloadedMoreMessage().replace("$name", name).replace("$remaining", String.valueOf(remaining))
                        );
                     } else {
                        sender.sendMessage(CommandMessagesConfig.getDownloadAllDownloadedMessage().replace("$name", name));
                     }
                  }
               } else {
                  if (abortIfAuthFailure(sender, player)) {
                     return;
                  }

                  failed.incrementAndGet();
                  failedNames.add(name);
                  if (player == null || player.isOnline()) {
                     sender.sendMessage(CommandMessagesConfig.getDownloadAllFailedMessage().replace("$name", name));
                  }
               }

               this.downloadNextPackage(packages, index + 1, importsFolder, player, sender, completed, failed, failedNames);
            }
         );
      }
   }

   private static boolean abortIfAuthFailure(CommandSender sender, Player player) {
      if (!NightbreakAccount.hasAuthFailure()) {
         return false;
      } else {
         IS_BULK_DOWNLOADING.set(false);
         CommandSender target = (CommandSender)(player != null && !player.isOnline() ? Bukkit.getConsoleSender() : sender);
         NightbreakSetupMenuHelper.sendTokenUpdatePrompt(target, "EliteMobs");
         return true;
      }
   }
}
