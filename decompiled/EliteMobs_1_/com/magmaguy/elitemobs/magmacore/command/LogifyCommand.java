package com.magmaguy.elitemobs.magmacore.command;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class LogifyCommand extends AdvancedCommand {
   private static final int UPLOAD_TIMEOUT_MS = 10000;
   private final JavaPlugin plugin;

   public LogifyCommand(JavaPlugin plugin) {
      super(new ArrayList<>());
      this.setUsage("/logify");
      this.setSenderType(SenderType.ANY);
      this.setPermission("logify.*");
      this.plugin = plugin;
   }

   @Override
   public void execute(CommandData commandData) {
      if (commandData.getCommandSender().hasPermission("logify.*")) {
         CommandSender sender = commandData.getCommandSender();
         File logsFolder = new File(this.plugin.getDataFolder().getParentFile().getParentFile(), "Logs");
         if (!logsFolder.exists()) {
            logsFolder = new File(this.plugin.getDataFolder().getParentFile().getParentFile(), "logs");
         }

         if (logsFolder.exists() && logsFolder.isDirectory()) {
            File logFile = new File(logsFolder, "latest.log");
            if (!logFile.exists()) {
               File[] logFiles = logsFolder.listFiles(filex -> filex.isFile() && filex.getName().endsWith(".log"));
               if (logFiles != null && logFiles.length > 0) {
                  File mostRecentLog = null;
                  long mostRecentTime = 0L;

                  for (File file : logFiles) {
                     if (file.lastModified() > mostRecentTime) {
                        mostRecentTime = file.lastModified();
                        mostRecentLog = file;
                     }
                  }

                  logFile = mostRecentLog;
               }
            }

            if (logFile != null && logFile.exists()) {
               File selectedLogFile = logFile;
               sender.sendMessage("§7Uploading latest log...");
               Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.uploadAndReport(selectedLogFile, sender));
            } else {
               sender.sendMessage("§cNo log file found!");
            }
         } else {
            sender.sendMessage("§cCould not find the logs folder on your server!");
         }
      }
   }

   private void uploadAndReport(File logFile, CommandSender sender) {
      try {
         byte[] fileBytes = Files.readAllBytes(logFile.toPath());
         String content = new String(fileBytes, StandardCharsets.UTF_8);
         content = this.anonymizeIPs(content);
         String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);
         String response = this.uploadLog(encodedContent);
         Bukkit.getScheduler().runTask(this.plugin, () -> this.sendUploadResult(sender, response));
      } catch (IOException var7) {
         Logger.warn("Error reading log file: " + var7.getMessage());
         Bukkit.getScheduler().runTask(this.plugin, () -> sender.sendMessage("§cAn error occurred while processing the log file."));
      }
   }

   private void sendUploadResult(CommandSender sender, String response) {
      if (response != null && response.contains("\"success\":true")) {
         String logUrl = this.extractLogUrl(response);
         sender.spigot()
            .sendMessage(
               new BaseComponent[]{
                  Logger.simpleMessage("&aLog uploaded successfully! View it here: "),
                  Logger.hoverLinkMessage("&9" + logUrl, "Click to go to link!", logUrl),
                  Logger.simpleMessage(" &a. "),
                  Logger.hoverCopyMessage("&6Click here to copy it!", "Click to copy link to clipboard!", logUrl)
               }
            );
      } else {
         Logger.sendMessage(sender, "&cFailed to upload log!");
      }
   }

   private String anonymizeIPs(String content) {
      Pattern ipPattern = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d+)?\\b");
      Matcher matcher = ipPattern.matcher(content);
      StringBuffer sb = new StringBuffer();

      while (matcher.find()) {
         String ipPart = matcher.group();
         String anonymized = ipPart.replaceAll("\\d", "*");
         matcher.appendReplacement(sb, Matcher.quoteReplacement(anonymized));
      }

      matcher.appendTail(sb);
      return sb.toString();
   }

   private String uploadLog(String encodedContent) {
      try {
         URL url = new URL("https://api.mclo.gs/1/log");
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         connection.setDoOutput(true);
         String postData = "content=" + encodedContent;

         try (OutputStream os = connection.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
            os.flush();
         }

         Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8);
         StringBuilder response = new StringBuilder();

         while (scanner.hasNext()) {
            response.append(scanner.nextLine());
         }

         scanner.close();
         return response.toString();
      } catch (IOException var10) {
         Logger.warn("Error uploading log: " + var10.getMessage());
         return null;
      }
   }

   private String extractLogUrl(String jsonResponse) {
      int urlIndex = jsonResponse.indexOf("\"url\":\"") + 7;
      int endIndex = jsonResponse.indexOf("\"", urlIndex);
      return jsonResponse.substring(urlIndex, endIndex).replace("\\/", "/");
   }
}
