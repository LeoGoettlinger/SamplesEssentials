package com.earth2me.essentials.commands.essentials;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.EssentialsUpgrade;
import com.earth2me.essentials.commands.EssentialsTreeNode;
import com.earth2me.essentials.userstorage.ModernUserMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;

public class UsermapCommand extends EssentialsTreeNode {
   public UsermapCommand() {
      super("usermap");
   }

   @Override
   protected void run(final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (sender.isAuthorized("essentials.usermap")) {
         ModernUserMap userMap = (ModernUserMap)this.ess.getUsers();
         sender.sendTl("usermapSize", userMap.getCachedCount(), userMap.getUserCount(), this.ess.getSettings().getMaxUserCacheCount());
         if (args.length > 0) {
            if (args[0].equals("full")) {
               for (Entry<String, UUID> entry : userMap.getNameCache().entrySet()) {
                  sender.sendTl("usermapEntry", entry.getKey(), entry.getValue().toString());
               }
            } else if (args[0].equals("purge")) {
               boolean seppuku = args.length > 1 && args[1].equals("iknowwhatimdoing");
               sender.sendTl("usermapPurge", String.valueOf(seppuku));
               Set<UUID> uuids = new HashSet<>(this.ess.getUsers().getAllUserUUIDs());
               this.ess.runTaskAsynchronously(() -> {
                  File userdataFolder = new File(this.ess.getDataFolder(), "userdata");
                  File backupFolder = new File(this.ess.getDataFolder(), "userdata-npc-backup-boogaloo-" + System.currentTimeMillis());
                  if (!userdataFolder.isDirectory()) {
                     this.ess.getLogger().warning("Missing userdata folder, aborting usermap purge.");
                  } else if (seppuku && !backupFolder.mkdir()) {
                     this.ess.getLogger().warning("Unable to create backup folder, aborting usermap purge.");
                  } else {
                     int total = 0;
                     File[] files = userdataFolder.listFiles(EssentialsUpgrade.YML_FILTER);
                     if (files != null) {
                        for (File file : files) {
                           try {
                              String fileName = file.getName();
                              UUID uuid = UUID.fromString(fileName.substring(0, fileName.length() - 4));
                              if (!uuids.contains(uuid)) {
                                 total++;
                                 this.ess.getLogger().warning("Found orphaned userdata file: " + file.getName());
                                 if (seppuku) {
                                    try {
                                       Files.move(file, new File(backupFolder, file.getName()));
                                    } catch (IOException var14) {
                                       this.ess.getLogger().log(Level.WARNING, "Unable to move orphaned userdata file: " + file.getName(), (Throwable)var14);
                                    }
                                 }
                              }
                           } catch (IllegalArgumentException var15) {
                           }
                        }
                     }

                     this.ess.getLogger().info("Found " + total + " orphaned userdata files.");
                  }
               });
            } else if (args[0].equalsIgnoreCase("cache")) {
               sender.sendTl("usermapKnown", this.ess.getUsers().getAllUserUUIDs().size(), this.ess.getUsers().getNameCache().size());
            } else {
               try {
                  UUID uuid = UUID.fromString(args[0]);

                  for (Entry<String, UUID> entry : userMap.getNameCache().entrySet()) {
                     if (entry.getValue().equals(uuid)) {
                        sender.sendTl("usermapEntry", entry.getKey(), args[0]);
                     }
                  }
               } catch (IllegalArgumentException var8) {
                  String sanitizedName = userMap.getSanitizedName(args[0]);
                  sender.sendTl("usermapEntry", sanitizedName, userMap.getNameCache().get(sanitizedName).toString());
               }
            }
         }
      }
   }

   @Override
   protected List<String> tabComplete(CommandSource sender, String commandLabel, String[] args) {
      return (List<String>)(args.length == 1 ? Lists.newArrayList(new String[]{"full", "purge", "cache"}) : Collections.emptyList());
   }
}
