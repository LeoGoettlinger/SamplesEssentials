package com.fastasyncworldedit.paster;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IncendoPaster {
   public static final String UPLOAD_PATH = "https://athion.net/ISPaster/paste/upload";
   public static final Collection<String> VALID_APPLICATIONS = Arrays.asList("plotsquared", "fastasyncworldedit", "incendopermissions", "kvantum");
   private final Collection<IncendoPaster.PasteFile> files = new ArrayList<>();
   private final String pasteApplication;

   public IncendoPaster(String pasteApplication) {
      if (pasteApplication != null && !pasteApplication.isEmpty()) {
         if (!VALID_APPLICATIONS.contains(pasteApplication.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(String.format("Unknown application name: %s", pasteApplication));
         } else {
            this.pasteApplication = pasteApplication;
         }
      } else {
         throw new IllegalArgumentException("paste application cannot be null, nor empty");
      }
   }

   public static String debugPaste(@Nonnull File logFile, @Nullable String debugInfo, @Nullable File... extraFiles) throws IOException {
      return debugPaste(logFile, debugInfo, extraFiles == null ? null : Arrays.asList(extraFiles));
   }

   public static String debugPaste(@Nonnull File logFile, @Nullable String debugInfo, @Nullable List<File> extraFiles) throws IOException {
      IncendoPaster incendoPaster = new IncendoPaster("fastasyncworldedit");
      StringBuilder b = new StringBuilder();
      b.append("# Welcome to this paste\n# It is meant to provide us at IntellectualSites with better information about your problem\n");
      b.append("\n# Server Information\n");
      b.append(debugInfo);
      b.append("\n# YAY! Now, let's see what we can find in your JVM\n");
      Runtime runtime = Runtime.getRuntime();
      RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
      b.append("Uptime: ").append(TimeUnit.MINUTES.convert(rb.getUptime(), TimeUnit.MILLISECONDS)).append(" minutes").append('\n');
      b.append("JVM Flags: ").append(rb.getInputArguments()).append('\n');
      b.append("Free Memory: ").append(runtime.freeMemory() / 1024L / 1024L).append(" MB").append('\n');
      b.append("Max Memory: ").append(runtime.maxMemory() / 1024L / 1024L).append(" MB").append('\n');
      b.append("Total Memory: ").append(runtime.totalMemory() / 1024L / 1024L).append(" MB").append('\n');
      b.append("Available Processors: ").append(runtime.availableProcessors()).append('\n');
      b.append("Java Name: ").append(rb.getVmName()).append('\n');
      b.append("Java Version: '").append(System.getProperty("java.version")).append("'\n");
      b.append("Java Vendor: '").append(System.getProperty("java.vendor")).append("'\n");
      b.append("Operating System: '").append(System.getProperty("os.name")).append("'\n");
      b.append("OS Version: ").append(System.getProperty("os.version")).append('\n');
      b.append("OS Arch: ").append(System.getProperty("os.arch")).append('\n');
      b.append("# Okay :D Great. The paste has been created successfully.");
      incendoPaster.addFile(new IncendoPaster.PasteFile("information", b.toString()));

      try {
         String file;
         if (Files.size(logFile.toPath()) > 14000000L) {
            file = "latest.log is larger than 14 MB. Not uploading.";
         } else {
            file = readFile(logFile, true);
         }

         incendoPaster.addFile(new IncendoPaster.PasteFile("latest.log", file));
      } catch (IOException var11) {
      }

      if (extraFiles != null) {
         for (File f : extraFiles) {
            incendoPaster.addFile(f);
         }
      }

      String rawResponse;
      try {
         rawResponse = incendoPaster.upload();
      } catch (Throwable var10) {
         throw new IOException(String.format("Failed to upload files: %s", var10.getMessage()), var10);
      }

      JsonObject jsonObject = new JsonParser().parse(rawResponse).getAsJsonObject();
      if (jsonObject.has("created")) {
         String pasteId = jsonObject.get("paste_id").getAsString();
         return String.format("https://athion.net/ISPaster/paste/view/%s", pasteId);
      } else {
         throw new IOException(String.format("Failed to upload files: %s", jsonObject.get("response").getAsString()));
      }
   }

   private static String readFile(File file, boolean cleanIPs) throws IOException {
      StringBuilder content = new StringBuilder();
      List<String> lines = new ArrayList<>();

      String line;
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
         while ((line = reader.readLine()) != null) {
            lines.add(line);
         }
      }

      for (int var9 = Math.max(0, lines.size() - 1000); var9 < lines.size(); var9++) {
         content.append(lines.get(var9)).append("\n");
      }

      String contentStr = content.toString();
      if (cleanIPs) {
         contentStr = contentStr.replaceAll(
            "\\b(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\b",
            "*"
         );
      }

      return contentStr;
   }

   public final Collection<IncendoPaster.PasteFile> getFiles() {
      return Collections.unmodifiableCollection(this.files);
   }

   public void addFile(File file) throws IOException {
      this.addFile(file, null);
   }

   public void addFile(File file, @Nullable String name) throws IOException {
      String fileName = name != null ? name : file.getName();
      boolean cleanIPS = fileName.endsWith(".log") || fileName.endsWith(".txt") || !fileName.contains(".");
      this.addFile(new IncendoPaster.PasteFile(fileName, readFile(file, cleanIPS)));
   }

   public void addFile(IncendoPaster.PasteFile file) {
      if (file == null) {
         throw new IllegalArgumentException("File cannot be null");
      } else {
         for (IncendoPaster.PasteFile pasteFile : this.files) {
            if (pasteFile.fileName.equalsIgnoreCase(file.getFileName())) {
               throw new IllegalArgumentException(String.format("Found duplicate file with name %s", file.getFileName()));
            }
         }

         this.files.add(file);
      }
   }

   private String toJsonString() {
      StringBuilder builder = new StringBuilder("{\n");
      builder.append("\"paste_application\": \"").append(this.pasteApplication).append("\",\n\"files\": \"");
      Iterator<IncendoPaster.PasteFile> fileIterator = this.files.iterator();

      while (fileIterator.hasNext()) {
         IncendoPaster.PasteFile file = fileIterator.next();
         builder.append(file.getFileName());
         if (fileIterator.hasNext()) {
            builder.append(",");
         }
      }

      builder.append("\",\n");
      fileIterator = this.files.iterator();

      while (fileIterator.hasNext()) {
         IncendoPaster.PasteFile file = fileIterator.next();
         builder.append("\"file-").append(file.getFileName()).append("\": \"").append(file.getContent().replaceAll("\"", "\\\\\"")).append("\"");
         if (fileIterator.hasNext()) {
            builder.append(",\n");
         }
      }

      builder.append("\n}");
      return builder.toString();
   }

   public final String upload() throws Throwable {
      URL url = new URL("https://athion.net/ISPaster/paste/upload");
      URLConnection connection = url.openConnection();
      HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
      httpURLConnection.setRequestMethod("POST");
      httpURLConnection.setDoOutput(true);
      byte[] content = this.toJsonString().getBytes(Charsets.UTF_8);
      httpURLConnection.setFixedLengthStreamingMode(content.length);
      httpURLConnection.setRequestProperty("Content-Type", "application/json");
      httpURLConnection.setRequestProperty("Accept", "*/*");
      httpURLConnection.connect();

      try (OutputStream stream = httpURLConnection.getOutputStream()) {
         stream.write(content);
      }

      if (!httpURLConnection.getResponseMessage().contains("OK")) {
         throw new IllegalStateException(
            String.format("Server returned status: %d %s", httpURLConnection.getResponseCode(), httpURLConnection.getResponseMessage())
         );
      } else {
         StringBuilder input = new StringBuilder();

         String line;
         try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
            while ((line = inputStream.readLine()) != null) {
               input.append(line).append("\n");
            }
         }

         return input.toString();
      }
   }

   public static class PasteFile {
      private final String fileName;
      private final String content;

      public PasteFile(String fileName, String content) {
         if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("file name cannot be null, nor empty");
         } else if (content != null && !content.isEmpty()) {
            this.fileName = fileName;
            this.content = content;
         } else {
            throw new IllegalArgumentException("content cannot be null, nor empty");
         }
      }

      public String getFileName() {
         return this.fileName;
      }

      public String getContent() {
         return this.content;
      }
   }
}
