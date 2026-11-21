package com.ranull.graves.util;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.module.ModuleManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ServerUtil {
   private ServerUtil() {
   }

   public static String getServerDumpInfo(Graves plugin) {
      List<String> stringList = new ArrayList<>();
      stringList.add("=================");
      stringList.add("Java Information:");
      stringList.add("=================");
      stringList.add("Java Version: " + getSystemProperty("java.version"));
      stringList.add("Java Vendor: " + getSystemProperty("java.vendor"));
      stringList.add("Java Home: " + getSystemProperty("java.home"));
      stringList.add("Java VM Name: " + getSystemProperty("java.vm.name"));
      stringList.add("");
      stringList.add("=============================");
      stringList.add("Operating System Information:");
      stringList.add("=============================");
      stringList.add("OS Name: " + getSystemProperty("os.name"));
      stringList.add("OS Version: " + getSystemProperty("os.version"));
      stringList.add("OS Architecture: " + getSystemProperty("os.arch"));
      stringList.add("Available Processors: " + Runtime.getRuntime().availableProcessors());
      stringList.add("Total Memory (JVM max): " + formatBytes(Runtime.getRuntime().maxMemory()));
      stringList.add("Free Memory (JVM): " + formatBytes(Runtime.getRuntime().freeMemory()));
      stringList.add("Docker Container: " + isRunningInDocker());
      stringList.add("Running as root: " + isRunningAsRoot());
      stringList.add("");
      stringList.add("================");
      stringList.add("CPU Information:");
      stringList.add("================");
      stringList.add("CPU Cores: " + Runtime.getRuntime().availableProcessors());
      stringList.add("CPU Load (System Average, 1 min): " + getSystemLoad());
      stringList.add("");
      stringList.add("==============================");
      stringList.add("Disk Space Information:");
      stringList.add("==============================");
      File[] roots = File.listRoots();
      if (roots != null) {
         for (File root : roots) {
            stringList.add("Mount Point: " + root.getAbsolutePath());
            stringList.add("Total Space: " + formatBytes(root.getTotalSpace()));
            stringList.add("Free Space: " + formatBytes(root.getFreeSpace()));
            stringList.add("Usable Space: " + formatBytes(root.getUsableSpace()));
            stringList.add("");
         }
      }

      stringList.add("=============================");
      stringList.add("Minecraft Server Information:");
      stringList.add("=============================");
      stringList.add("Implementation Name: " + plugin.getServer().getName());
      stringList.add("Implementation Version: " + plugin.getServer().getVersion());
      stringList.add("Bukkit Version: " + plugin.getServer().getBukkitVersion());

      try {
         stringList.add("NMS Version: " + getNmsVersion(plugin.getServer()));
      } catch (Exception var15) {
         stringList.add("NMS Version: " + Bukkit.getServer().getVersion());
      }

      stringList.add("Plugin Count: " + plugin.getServer().getPluginManager().getPlugins().length);
      stringList.add("Plugin List: " + getPluginList());
      stringList.add("Worlds:");

      for (World world : plugin.getServer().getWorlds()) {
         stringList.add("- " + world.getName() + " (keepInventory=" + hasKeepInventory(world) + ")");
      }

      stringList.add("Players Online: " + plugin.getServer().getOnlinePlayers().size() + "/" + plugin.getServer().getMaxPlayers());

      for (Player player : plugin.getServer().getOnlinePlayers()) {
         int ping = getPlayerPing(player);
         stringList.add("- " + player.getName() + " UUID: " + player.getUniqueId() + " Ping: " + ping + "ms");
      }

      stringList.add("");
      stringList.add("===================");
      stringList.add("Graves Information:");
      stringList.add("===================");
      stringList.add(plugin.getDescription().getName() + " Version: " + plugin.getDescription().getVersion());
      if (plugin.getVersionManager().hasAPIVersion()) {
         stringList.add(plugin.getDescription().getName() + " API Version: " + plugin.getDescription().getAPIVersion());
      }

      String databaseTypefromConfig = plugin.getConfig().getString("settings.storage.type", "SQLITE").toUpperCase();
      stringList.add(plugin.getDescription().getName() + " Database Type: " + databaseTypefromConfig);
      if (databaseTypefromConfig.equals("MYSQL") || databaseTypefromConfig.equals("MARIADB") || databaseTypefromConfig.equals("POSTGRESQL")) {
         try {
            stringList.add(plugin.getDescription().getName() + " Database Version: " + plugin.getDataManager().getDatabaseVersion());
         } catch (Exception var14) {
            stringList.add(plugin.getDescription().getName() + " Database Version: Unknown");
         }
      }

      if (plugin.getIntegrationManager().hasLuckPermsHandler()) {
         stringList.add(plugin.getDescription().getName() + " Permissions Provider: LuckPerms");
      } else if (plugin.getIntegrationManager().hasVaultPermProvider()) {
         stringList.add(plugin.getDescription().getName() + " Permissions Provider: Vault");
      } else {
         stringList.add(plugin.getDescription().getName() + " Permissions Provider: Bukkit");
      }

      stringList.add(plugin.getDescription().getName() + " Plugin Release: " + plugin.getPluginReleaseType());

      try {
         ModuleManager mm = plugin.getModuleManager();
         if (mm != null) {
            List<String> enabledMods = new ArrayList<>();
            List<String> disabledMods = new ArrayList<>();

            for (ModuleManager.LoadedModule lm : mm.modules()) {
               String n = lm.info.name() + " v" + lm.info.version();
               if (lm.enabled) {
                  enabledMods.add(n);
               } else {
                  disabledMods.add(n);
               }
            }

            stringList.add(plugin.getDescription().getName() + " Modules Enabled: " + (enabledMods.isEmpty() ? "None" : String.join(", ", enabledMods)));
            stringList.add(plugin.getDescription().getName() + " Modules Disabled: " + (disabledMods.isEmpty() ? "None" : String.join(", ", disabledMods)));
         } else {
            stringList.add(plugin.getDescription().getName() + " Modules: Not available");
         }
      } catch (Throwable var16) {
         stringList.add(plugin.getDescription().getName() + " Modules: Not available");
      }

      stringList.add(plugin.getDescription().getName() + " Config Version: " + plugin.getConfig().getInt("config-version"));
      File configDir = new File("plugins/GravesX/config");
      if (configDir.exists() && configDir.isDirectory()) {
         File[] files = configDir.listFiles();
         if (files != null) {
            for (File file : files) {
               if (file.isFile() && file.getName().endsWith(".yml")) {
                  try {
                     String configContent = readFileToString(file);
                     String maskedConfigContent = maskPasswords(configContent, plugin.getConfig());
                     String configBase64 = Base64.getEncoder().encodeToString(maskedConfigContent.getBytes(StandardCharsets.UTF_8));
                     stringList.add("Config " + file.getName() + " Base64: " + configBase64);
                  } catch (IOException var13) {
                     stringList.add("Config " + file.getName() + " could not be read.");
                  }
               }
            }
         }
      }

      return joinLines(stringList);
   }

   private static double getSystemLoad() {
      try {
         OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
         Method loadMethod = osBean.getClass().getMethod("getSystemLoadAverage");
         return (Double)loadMethod.invoke(osBean);
      } catch (Exception var2) {
         return -1.0;
      }
   }

   private static String readFileToString(File file) throws IOException {
      String var4;
      try (FileInputStream fis = new FileInputStream(file)) {
         byte[] data = new byte[(int)file.length()];
         int read = fis.read(data);
         if (read < 0) {
            return "";
         }

         var4 = new String(data, 0, read, StandardCharsets.UTF_8);
      }

      return var4;
   }

   private static String getNmsVersion(Object server) throws Exception {
      Class<?> serverClass = server.getClass();
      Method method = serverClass.getMethod("getVersion");
      return (String)method.invoke(server);
   }

   private static String formatBytes(long bytes) {
      if (bytes < 1024L) {
         return bytes + " B";
      } else {
         int exp = (int)(Math.log(bytes) / Math.log(1024.0));
         char pre = "KMGTPE".charAt(exp - 1);
         return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp), pre);
      }
   }

   private static boolean hasKeepInventory(World world) {
      return Boolean.TRUE.equals(world.getGameRuleValue(GameRule.KEEP_INVENTORY));
   }

   private static String getSystemProperty(String key) {
      String value = System.getProperty(key);
      return value != null ? value : "Unknown";
   }

   private static String getOsName() {
      String osName = System.getProperty("os.name");
      return osName.toLowerCase(Locale.ROOT).contains("linux") ? getLinuxOsName() : osName;
   }

   private static String getLinuxOsName() {
      String osReleaseFile = "/etc/os-release";

      try {
         String line;
         try (BufferedReader reader = new BufferedReader(new FileReader(osReleaseFile, StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
               if (line.startsWith("PRETTY_NAME=")) {
                  return line.substring("PRETTY_NAME=".length()).replace("\"", "");
               }
            }
         }

         return "Linux (Unknown)";
      } catch (IOException var6) {
         return "Linux (Unknown)";
      }
   }

   private static boolean isRunningInDocker() {
      File cgroupFile = new File("/proc/self/cgroup");

      try {
         boolean var3;
         try (BufferedReader reader = new BufferedReader(new FileReader(cgroupFile))) {
            String line;
            do {
               if ((line = reader.readLine()) == null) {
                  return false;
               }
            } while (!line.contains("/docker/"));

            var3 = true;
         }

         return var3;
      } catch (IOException var6) {
         return false;
      }
   }

   private static boolean isRunningAsRoot() {
      return "root".equals(System.getProperty("user.name"));
   }

   private static String maskPasswords(String configString, FileConfiguration config) {
      String maskedConfigString = configString;

      for (String path : new HashSet(config.getKeys(true))) {
         Object value = config.get(path);
         if (value instanceof String && isPasswordField(path)) {
            String password = (String)value;
            if (!password.isEmpty()) {
               maskedConfigString = maskedConfigString.replace(password, repeat('*', password.length()));
            }
         }
      }

      return maskedConfigString;
   }

   private static boolean isPasswordField(String path) {
      String p = path.toLowerCase(Locale.ROOT);
      return p.contains("password") || p.contains("secret");
   }

   private static String joinLines(List<String> lines) {
      return String.join("\n", lines);
   }

   private static String repeat(char ch, int times) {
      char[] chars = new char[times];
      Arrays.fill(chars, ch);
      return new String(chars);
   }

   private static int getPlayerPing(Player player) {
      try {
         return player.getPing();
      } catch (Exception var2) {
         return -1;
      }
   }

   private static String getPluginList() {
      StringBuilder sb = new StringBuilder();

      for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
         sb.append(plugin.getName()).append(' ').append(plugin.getDescription().getVersion()).append(", ");
      }

      return sb.length() > 2 ? sb.substring(0, sb.length() - 2) : "";
   }
}
