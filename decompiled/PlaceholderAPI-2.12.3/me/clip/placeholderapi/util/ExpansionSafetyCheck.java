package me.clip.placeholderapi.util;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.manager.CloudExpansionManager;
import org.jetbrains.annotations.NotNull;

public final class ExpansionSafetyCheck {
   private static final String MESSAGE = "\n###############################################\n###############################################\nPlaceholderAPI performs checks at startup and /papi reload for known malicious expansions. If you're seeing this message, there are the following malicious expansions in plugins/PlaceholderAPI/expansions.\n%sTo prevent further infection PlaceholderAPI has stopped the server.\nIf you're seeing this message after updating PAPI, your server may have been infected for some time, so best practice is a complete system wipe and reinstall of your server software and plugins to be safe.\nIf you're seeing this after downloading an expansion however, PAPI hasn't loaded any of the malicious expansions above so you should be safe to simply delete the expansion in question.\n###############################################\n###############################################";
   private final PlaceholderAPIPlugin main;

   public ExpansionSafetyCheck(@NotNull PlaceholderAPIPlugin main) {
      this.main = main;
   }

   public boolean runChecks() {
      if (!this.main.getPlaceholderAPIConfig().detectMaliciousExpansions()) {
         return false;
      } else {
         File expansionsFolder = new File(this.main.getDataFolder(), "expansions");
         if (!expansionsFolder.exists()) {
            return false;
         } else {
            Set<String> knownMaliciousExpansions;
            try {
               URLConnection connection = new URI("https://check.placeholderapi.com").toURL().openConnection();
               connection.setRequestProperty("User-Agent", CloudExpansionManager.USER_AGENT);
               String hashes = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
                  .lines()
                  .collect(Collectors.joining(System.lineSeparator()));
               knownMaliciousExpansions = Arrays.stream(hashes.split("\n")).collect(Collectors.toSet());
            } catch (Exception var11) {
               this.main
                  .getLogger()
                  .log(Level.SEVERE, "Failed to download anti malware hash check list from https://check.placeholderapi.com", (Throwable)var11);
               return false;
            }

            Set<String> maliciousPaths = new HashSet<>();
            File[] files = expansionsFolder.listFiles();
            if (files == null) {
               return false;
            } else {
               for (File file : files) {
                  try {
                     if (file.isFile()) {
                        String hash = Hashing.sha256().hashBytes(Files.asByteSource(file).read()).toString();
                        if (knownMaliciousExpansions.contains(hash)) {
                           maliciousPaths.add(file.getAbsolutePath());
                        }
                     }
                  } catch (Exception var10) {
                     this.main.getLogger().log(Level.SEVERE, "Error occurred while trying to read " + file.getAbsolutePath(), (Throwable)var10);
                  }
               }

               if (maliciousPaths.isEmpty()) {
                  return false;
               } else {
                  this.main
                     .getLogger()
                     .severe(
                        String.format(
                           "\n###############################################\n###############################################\nPlaceholderAPI performs checks at startup and /papi reload for known malicious expansions. If you're seeing this message, there are the following malicious expansions in plugins/PlaceholderAPI/expansions.\n%sTo prevent further infection PlaceholderAPI has stopped the server.\nIf you're seeing this message after updating PAPI, your server may have been infected for some time, so best practice is a complete system wipe and reinstall of your server software and plugins to be safe.\nIf you're seeing this after downloading an expansion however, PAPI hasn't loaded any of the malicious expansions above so you should be safe to simply delete the expansion in question.\n###############################################\n###############################################",
                           maliciousPaths.stream()
                              .map(p -> "HASH OF " + p + " MATCHES KNOWN MALICIOUS EXPANSION DELETE IMMEDIATELY\n")
                              .collect(Collectors.joining())
                        )
                     );
                  this.main.getServer().shutdown();
                  return true;
               }
            }
         }
      }
   }
}
