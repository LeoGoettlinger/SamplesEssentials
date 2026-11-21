package dev.cwhead.GravesX.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.util.UUIDUtil;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class DebugManager {
   private static final AtomicReference<DebugManager> ACTIVE = new AtomicReference<>();
   private static final String[] INTERNAL_PACKAGES = new String[]{"com.ranull.graves.", "dev.cwhead.GravesX.", "me.jay.GravesX."};
   private final Graves plugin;
   private final AtomicBoolean unloaded = new AtomicBoolean(false);

   public DebugManager(Graves plugin) {
      this.plugin = plugin;
      DebugManager previous = ACTIVE.getAndSet(this);
      if (previous != null) {
         previous.unload();
      }
   }

   public void unload() {
      this.unloaded.set(true);
      ACTIVE.compareAndSet(this, null);
   }

   public boolean isUnloaded() {
      return this.unloaded.get();
   }

   public boolean isEnabled(int severity) {
      if (this.unloaded.get()) {
         return false;
      } else if (severity != 1 && severity != 2) {
         return false;
      } else {
         int level = this.plugin.getConfig().getInt("settings.debug.level", 0);
         if (level < 0) {
            level = 0;
         }

         if (level > 2) {
            level = 2;
         }

         return level != 0 && severity <= level;
      }
   }

   public void info(String message) {
      this.debug(message, 1, null);
   }

   public void warn(String message) {
      this.debug(message, 2, null);
   }

   public void debug(String message, int severity) {
      this.debug(message, severity, null);
   }

   public void debug(String message, int severity, Throwable throwable) {
      if (this.isEnabled(severity)) {
         boolean showCaller = this.plugin.getConfig().getBoolean("settings.debug.show-caller", true);
         boolean showCallerClass = this.plugin.getConfig().getBoolean("settings.debug.show-caller-class", false);
         DebugManager.CallerInfo caller = !showCaller && !showCallerClass ? null : this.resolveTriggeringPluginCaller(showCallerClass);
         if (caller != null && !showCallerClass) {
            caller = caller.toPluginOnly();
         }

         String consolePrefix = this.buildConsolePrefix(severity, caller);
         if (severity == 2) {
            this.plugin.getLogger().warning(consolePrefix + message);
         } else {
            this.plugin.getLogger().info(consolePrefix + message);
         }

         if (throwable != null) {
            this.plugin.getLogger().warning("Debug stacktrace:");
            this.plugin.logStackTrace(throwable);
         }

         List<String> admins = this.plugin.getConfig().getStringList("settings.debug.admin");
         if (!admins.isEmpty()) {
            if (Bukkit.isPrimaryThread()) {
               this.sendToAdmins(admins, message, severity, caller);
            } else {
               DebugManager.CallerInfo finalCaller = caller;
               Bukkit.getScheduler().runTask(this.plugin, () -> {
                  if (!this.unloaded.get()) {
                     this.sendToAdmins(admins, message, severity, finalCaller);
                  }
               });
            }
         }
      }
   }

   private void sendToAdmins(List<String> admins, String message, int severity, DebugManager.CallerInfo caller) {
      if (!this.unloaded.get()) {
         for (String admin : admins) {
            Player player = Bukkit.getPlayer(admin);
            UUID uuid = null;

            try {
               uuid = UUID.fromString(admin);
            } catch (IllegalArgumentException var13) {
            }

            if (uuid == null) {
               uuid = UUIDUtil.getUUID(admin);
            }

            if (uuid != null) {
               Player uuidPlayer = Bukkit.getPlayer(uuid);
               if (uuidPlayer != null) {
                  player = uuidPlayer;
               }
            }

            if (player != null) {
               String labelBase;
               if (this.plugin.getIntegrationManager() != null && this.plugin.getIntegrationManager().hasMultiPaper()) {
                  labelBase = "Debug (" + this.plugin.getIntegrationManager().getMultiPaper().getLocalServerName() + ")";
               } else {
                  labelBase = "Debug";
               }

               String sevLabel = severity == 2 ? "WARN" : "INFO";
               ChatColor sevColor = severity == 2 ? ChatColor.GOLD : ChatColor.RED;
               String callerSuffix = caller != null && caller.hasAny() ? ChatColor.DARK_GRAY + " [" + caller.toShortString() + "]" + ChatColor.RESET : "";
               player.sendMessage(
                  ChatColor.RED
                     + "☠"
                     + ChatColor.DARK_GRAY
                     + " » "
                     + sevColor
                     + labelBase
                     + " "
                     + ChatColor.DARK_GRAY
                     + "("
                     + sevLabel
                     + "):"
                     + ChatColor.RESET
                     + " "
                     + message
                     + callerSuffix
               );
            }
         }
      }
   }

   private String buildConsolePrefix(int severity, DebugManager.CallerInfo caller) {
      String mp = this.plugin.getIntegrationManager() != null && this.plugin.getIntegrationManager().hasMultiPaper()
         ? " (" + this.plugin.getIntegrationManager().getMultiPaper().getLocalServerName() + ")"
         : "";
      String sevLabel = severity == 2 ? "WARN" : "INFO";
      return caller != null && caller.hasAny() ? "Debug" + mp + " [" + sevLabel + " " + caller.toShortString() + "]: " : "Debug" + mp + " [" + sevLabel + "]: ";
   }

   private DebugManager.CallerInfo resolveTriggeringPluginCaller(boolean includeClassDetails) {
      StackTraceElement[] stack = Thread.currentThread().getStackTrace();

      for (StackTraceElement e : stack) {
         if (e != null) {
            String cn = e.getClassName();
            if (!this.isIgnorableFrame(cn) && !this.isInternalClass(cn)) {
               Class<?> clazz = this.tryLoadClass(cn);
               if (clazz != null) {
                  Plugin providing;
                  try {
                     providing = JavaPlugin.getProvidingPlugin(clazz);
                  } catch (IllegalArgumentException var11) {
                     continue;
                  } catch (Throwable var12) {
                     continue;
                  }

                  if (!providing.getName().equalsIgnoreCase(this.plugin.getName())) {
                     Integer line = includeClassDetails && e.getLineNumber() > 0 ? e.getLineNumber() : null;
                     return new DebugManager.CallerInfo(
                        providing.getName(), includeClassDetails ? cn : null, includeClassDetails ? e.getMethodName() : null, line
                     );
                  }
               }
            }
         }
      }

      return null;
   }

   private Class<?> tryLoadClass(String className) {
      try {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         if (cl != null) {
            return Class.forName(className, false, cl);
         }
      } catch (Throwable var5) {
      }

      try {
         return Class.forName(className, false, DebugManager.class.getClassLoader());
      } catch (Throwable var4) {
         try {
            return Class.forName(className);
         } catch (Throwable var3) {
            return null;
         }
      }
   }

   private boolean isIgnorableFrame(String className) {
      return className.equals(Thread.class.getName())
         || className.equals(DebugManager.class.getName())
         || className.startsWith("java.")
         || className.startsWith("jdk.")
         || className.startsWith("sun.")
         || className.startsWith("net.minecraft.")
         || className.startsWith("com.mojang.")
         || className.startsWith("org.bukkit.")
         || className.startsWith("org.bukkit.craftbukkit.")
         || className.startsWith("com.destroystokyo.paper.")
         || className.startsWith("io.papermc.");
   }

   private boolean isInternalClass(String className) {
      for (String p : INTERNAL_PACKAGES) {
         if (className.startsWith(p)) {
            return true;
         }
      }

      return false;
   }

   private static final class CallerInfo {
      private final String pluginName;
      private final String className;
      private final String methodName;
      private final Integer line;

      private CallerInfo(String pluginName, String className, String methodName, Integer line) {
         this.pluginName = pluginName;
         this.className = className;
         this.methodName = methodName;
         this.line = line;
      }

      private boolean hasAny() {
         return this.pluginName != null || this.className != null || this.methodName != null;
      }

      private DebugManager.CallerInfo toPluginOnly() {
         return this.pluginName == null ? null : new DebugManager.CallerInfo(this.pluginName, null, null, null);
      }

      private String toShortString() {
         StringBuilder sb = new StringBuilder();
         if (this.pluginName != null && !this.pluginName.isEmpty()) {
            sb.append(this.pluginName);
         }

         if (this.className != null) {
            sb.append(":").append(this.className);
         }

         if (this.methodName != null) {
            sb.append("#").append(this.methodName);
         }

         if (this.line != null) {
            sb.append(":").append(this.line);
         }

         return sb.toString();
      }
   }
}
