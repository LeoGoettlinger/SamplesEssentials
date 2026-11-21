package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.translations.TranslationCsvParser;
import com.magmaguy.elitemobs.config.translations.TranslationsConfigFields;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

public class LanguageCommand extends AdvancedCommand {
   private static final List<String> REMOTE_LANGUAGES = List.of(
      "french",
      "german",
      "spanish",
      "italian",
      "portuguese",
      "portugueseBrazilian",
      "russian",
      "chineseSimplified",
      "chineseTraditional",
      "japanese",
      "korean",
      "polish",
      "dutch",
      "czech",
      "hungarian",
      "romanian",
      "turkish",
      "vietnamese",
      "indonesian"
   );
   private final List<String> suggestions;
   private static LanguageCommand.Request activeRequest;

   public LanguageCommand() {
      super(List.of("language"));
      Set<String> langs = new LinkedHashSet<>();
      langs.add("english");
      langs.add("custom");
      langs.addAll(REMOTE_LANGUAGES);
      Path folder = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(), "translations");
      if (Files.isDirectory(folder)) {
         try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder, "*.csv")) {
            for (Path p : ds) {
               String filename = p.getFileName().toString();
               if (!filename.endsWith("_data.csv")) {
                  langs.add(filename.replace(".csv", ""));
               }
            }
         } catch (Exception var9) {
            Logger.warn("Could not list translations folder: " + var9.getMessage());
         }
      }

      this.suggestions = new ArrayList<>(langs);
      this.addArgument("language", new ListStringCommandArgument(this.suggestions, "<language>"));
      this.setUsage("/em language <language>");
      this.setPermission("elitemobs.language");
      this.setDescription("Sets the language for EliteMobs (downloads from remote if needed).");
   }

   @Override
   public void execute(CommandData commandData) {
      String language = commandData.getStringArgument("language");
      CommandSender sender = commandData.getCommandSender();
      language = language.trim();
      if (language.toLowerCase(Locale.ROOT).endsWith(".csv") || language.toLowerCase(Locale.ROOT).endsWith(".yml")) {
         language = language.substring(0, language.length() - 4);
      }

      String requestedLanguage = language;
      language = this.suggestions.stream().filter(s -> s.equalsIgnoreCase(requestedLanguage)).findFirst().orElse(null);
      if (language == null) {
         Logger.sendMessage(sender, CommandMessagesConfig.getLanguageNotFoundMessage());
         this.suggestions.forEach(s -> Logger.sendMessage(sender, CommandMessagesConfig.getLanguageListPrefix() + s));
      } else {
         shutdown();
         if (language.equals("english")) {
            DefaultConfig.setLanguage(sender, language);
            Logger.sendMessage(sender, CommandMessagesConfig.getLanguageSetEnglishMessage());
         } else if (language.equals("custom")) {
            Path folder = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(), "translations");
            Path target = folder.resolve("custom.csv");
            if (!Files.exists(target)) {
               Logger.sendMessage(sender, CommandMessagesConfig.getLanguageGeneratingCustomMessage());
               if (!this.generateCustomCsv(target)) {
                  Logger.sendMessage(sender, CommandMessagesConfig.getLanguageGenerateFailedMessage());
                  return;
               }

               TranslationsConfigFields.discardBaseline(folder, language);
               Logger.sendMessage(sender, CommandMessagesConfig.getLanguageGenerateSuccessMessage());
            }

            DefaultConfig.setLanguage(sender, language);
            Logger.sendMessage(sender, CommandMessagesConfig.getLanguageSetCustomMessage());
         } else {
            Path folder = Paths.get(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath(), "translations");
            Path target = folder.resolve(language + ".csv");
            if (!Files.exists(target)) {
               Logger.sendMessage(sender, CommandMessagesConfig.getLanguageDownloadingMessage().replace("$language", language));
               LanguageCommand.Request request = new LanguageCommand.Request(language, target, sender);
               activeRequest = request;

               try {
                  request.worker = Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> prepare(request));
               } catch (RuntimeException var9) {
                  request.cancel();
                  activeRequest = null;
                  Logger.sendMessage(sender, CommandMessagesConfig.getLanguageDownloadFailedMessage().replace("$language", language));
               }
            } else {
               DefaultConfig.setLanguage(sender, language);
               Logger.sendMessage(sender, CommandMessagesConfig.getLanguageSetMessage().replace("$language", language));
            }
         }
      }
   }

   private boolean generateCustomCsv(Path outPath) {
      try {
         Files.createDirectories(outPath.getParent());
         String header = "\ufeff\"key\",\"en\",\"custom\"\n";
         Files.writeString(outPath, header, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
         return true;
      } catch (Exception var3) {
         Logger.warn("Error generating custom.csv: " + var3.getMessage());
         return false;
      }
   }

   private static void prepare(LanguageCommand.Request request) {
      boolean queued = false;

      try {
         request.download();
         synchronized (request) {
            if (!request.cancelled) {
               request.completion = Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> complete(request));
               queued = true;
               return;
            }
         }
      } catch (Exception var15) {
         Exception failure = var15;
         synchronized (request) {
            if (!request.cancelled) {
               Logger.warn("Error downloading " + request.language + ".csv: " + failure.getMessage());

               try {
                  request.completion = Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                     if (activeRequest == request && !request.cancelled) {
                        activeRequest = null;
                        Logger.sendMessage(request.sender, CommandMessagesConfig.getLanguageDownloadFailedMessage().replace("$language", request.language));
                     }
                  });
               } catch (RuntimeException var12) {
               }

               return;
            }

            return;
         }
      } finally {
         if (!queued) {
            request.removeTemporary();
         }
      }
   }

   private static void complete(LanguageCommand.Request request) {
      try {
         if (activeRequest == request && !request.cancelled) {
            if (!Files.exists(request.target)) {
               Files.move(request.temporary, request.target, StandardCopyOption.ATOMIC_MOVE);
               TranslationsConfigFields.discardBaseline(request.target.getParent(), request.language);
            }

            activeRequest = null;
            Logger.sendMessage(request.sender, CommandMessagesConfig.getLanguageDownloadSuccessMessage().replace("$language", request.language));
            DefaultConfig.setLanguage(request.sender, request.language);
            Logger.sendMessage(request.sender, CommandMessagesConfig.getLanguageSetMessage().replace("$language", request.language));
            return;
         }
      } catch (Exception var5) {
         Logger.warn("Could not activate language " + request.language + ": " + var5.getMessage());
         Logger.sendMessage(request.sender, CommandMessagesConfig.getLanguageDownloadFailedMessage().replace("$language", request.language));
         return;
      } finally {
         if (activeRequest == request) {
            activeRequest = null;
         }

         request.removeTemporary();
      }
   }

   public static void shutdown() {
      LanguageCommand.Request previous = activeRequest;
      activeRequest = null;
      if (previous != null) {
         previous.cancel();
      }
   }

   private static final class Request {
      final String language;
      final Path target;
      final Path temporary;
      final CommandSender sender;
      volatile boolean cancelled;
      BukkitTask worker;
      BukkitTask completion;

      Request(String language, Path target, CommandSender sender) {
         this.language = language;
         this.target = target;
         this.sender = sender;
         this.temporary = target.resolveSibling(target.getFileName() + "." + UUID.randomUUID() + ".download");
      }

      void download() throws Exception {
         HttpURLConnection current = (HttpURLConnection)new URL("https://magmaguy.com/api/elitemobs_translations/" + this.language + ".csv").openConnection();
         if (this.cancelled) {
            current.disconnect();
         } else {
            try {
               current.setRequestMethod("GET");
               current.setConnectTimeout(10000);
               current.setReadTimeout(30000);
               int status = current.getResponseCode();
               if (status != 200) {
                  throw new IOException("HTTP " + status);
               }

               Files.createDirectories(this.target.getParent());

               try (
                  InputStream in = current.getInputStream();
                  OutputStream out = Files.newOutputStream(this.temporary, StandardOpenOption.CREATE_NEW);
               ) {
                  byte[] buffer = new byte[8192];

                  int count;
                  while (!this.cancelled && (count = in.read(buffer)) != -1) {
                     out.write(buffer, 0, count);
                  }
               }

               if (!this.cancelled) {
                  TranslationCsvParser.parse(this.temporary);
               }
            } finally {
               current.disconnect();
            }
         }
      }

      synchronized void cancel() {
         this.cancelled = true;
         if (this.worker != null) {
            this.worker.cancel();
         }

         if (this.completion != null) {
            this.completion.cancel();
         }

         this.removeTemporary();
      }

      void removeTemporary() {
         try {
            Files.deleteIfExists(this.temporary);
         } catch (IOException var2) {
            if (!this.cancelled) {
               Logger.warn("Could not remove translation download " + this.temporary.getFileName() + ": " + var2.getMessage());
            }
         }
      }
   }
}
