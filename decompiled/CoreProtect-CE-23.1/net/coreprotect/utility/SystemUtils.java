package net.coreprotect.utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class SystemUtils {
   private static boolean testMode = Boolean.getBoolean("net.coreprotect.test");
   private static String processorInfo = null;
   private static boolean log4jInitialized = false;
   private static Double appleProcessorSpeed = null;

   private SystemUtils() {
      throw new IllegalStateException("Utility class");
   }

   public static void setTestMode(boolean enabled) {
      testMode = enabled;
      if (enabled) {
         processorInfo = "Test Processor";
      }
   }

   public static boolean isAppleSilicon() {
      String osName = System.getProperty("os.name", "").toLowerCase();
      String osArch = System.getProperty("os.arch", "").toLowerCase();
      return osName.contains("mac") && (osArch.contains("aarch64") || osArch.contains("arm"));
   }

   public static Double getAppleSiliconSpeed() {
      if (appleProcessorSpeed != null) {
         return appleProcessorSpeed;
      } else if (testMode) {
         return 3.2;
      } else {
         String modelName = getProcessorModel();
         if (modelName != null && modelName.contains("GHz")) {
            try {
               Pattern pattern = Pattern.compile("(\\d+\\.\\d+)GHz");
               Matcher matcher = pattern.matcher(modelName);
               if (matcher.find()) {
                  appleProcessorSpeed = Double.parseDouble(matcher.group(1));
                  return appleProcessorSpeed;
               }
            } catch (Exception var10) {
            }
         }

         try {
            ProcessBuilder pb = new ProcessBuilder("sysctl", "-n", "machdep.cpu.brand_string");
            Process process = pb.start();
            if (process.waitFor(500L, TimeUnit.MILLISECONDS)) {
               String appleChipInfo = null;
               BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

               try {
                  appleChipInfo = reader.readLine();
               } catch (Throwable var8) {
                  try {
                     reader.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }

                  throw var8;
               }

               reader.close();
               if (appleChipInfo != null) {
                  if (appleChipInfo.contains("M1")) {
                     return getMSeriesSpeed("M1", appleChipInfo);
                  }

                  if (appleChipInfo.contains("M2")) {
                     return getMSeriesSpeed("M2", appleChipInfo);
                  }

                  if (appleChipInfo.contains("M3")) {
                     return getMSeriesSpeed("M3", appleChipInfo);
                  }

                  if (appleChipInfo.contains("M4")) {
                     return getMSeriesSpeed("M4", appleChipInfo);
                  }
               }
            }
         } catch (Exception var9) {
         }

         if (modelName != null) {
            if (modelName.contains("M1")) {
               appleProcessorSpeed = 3.2;
            } else if (modelName.contains("M2")) {
               appleProcessorSpeed = 3.7;
            } else if (modelName.contains("M3")) {
               appleProcessorSpeed = 4.05;
            } else if (modelName.contains("M4")) {
               appleProcessorSpeed = 4.5;
            } else {
               appleProcessorSpeed = 3.0;
            }

            return appleProcessorSpeed;
         } else {
            return 3.2;
         }
      }
   }

   private static Double getMSeriesSpeed(String series, String chipInfo) {
      switch (series) {
         case "M1":
            if (!chipInfo.contains("Pro") && !chipInfo.contains("Max") && !chipInfo.contains("Ultra")) {
               appleProcessorSpeed = 3.2;
            } else {
               appleProcessorSpeed = 3.23;
            }
            break;
         case "M2":
            if (!chipInfo.contains("Pro") && !chipInfo.contains("Max") && !chipInfo.contains("Ultra")) {
               appleProcessorSpeed = 3.5;
            } else {
               appleProcessorSpeed = 3.7;
            }
            break;
         case "M3":
            if (!chipInfo.contains("Pro") && !chipInfo.contains("Max")) {
               appleProcessorSpeed = 4.0;
            } else {
               appleProcessorSpeed = 4.05;
            }
            break;
         case "M4":
            if (chipInfo.contains("Max")) {
               appleProcessorSpeed = 4.5;
            } else if (chipInfo.contains("Pro")) {
               appleProcessorSpeed = 4.5;
            } else {
               appleProcessorSpeed = 4.3;
            }
            break;
         default:
            appleProcessorSpeed = 3.2;
      }

      return appleProcessorSpeed;
   }

   private static String getProcessorModel() {
      try {
         ProcessBuilder pb = new ProcessBuilder("sysctl", "-n", "machdep.cpu.brand_string");
         Process process = pb.start();
         if (process.waitFor(500L, TimeUnit.MILLISECONDS)) {
            String var3;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
               var3 = reader.readLine();
            }

            return var3;
         }
      } catch (Exception var7) {
      }

      return null;
   }

   public static CentralProcessor getProcessorInfo() {
      if (!testMode && !isLog4jDisabled()) {
         CentralProcessor result = null;

         try {
            Class.forName("com.sun.jna.Platform");
            if (System.getProperty("os.name").startsWith("Windows") && !System.getProperty("sun.arch.data.model").equals("64")) {
               Class.forName("com.sun.jna.platform.win32.Win32Exception");
            } else if (System.getProperty("os.name").toLowerCase().contains("android")
               || System.getProperty("java.runtime.name").toLowerCase().contains("android")) {
               return null;
            }

            try {
               if (!log4jInitialized) {
                  Configurator.setLevel("oshi.hardware.common.AbstractCentralProcessor", Level.OFF);
                  log4jInitialized = true;
               }
            } catch (Exception var2) {
            }

            SystemInfo systemInfo = new SystemInfo();
            result = systemInfo.getHardware().getProcessor();
         } catch (Exception var3) {
         }

         return result;
      } else {
         return null;
      }
   }

   public static String getProcessorInfoString() {
      if (processorInfo != null) {
         return processorInfo;
      } else {
         CentralProcessor processor = getProcessorInfo();
         if (processor != null) {
            processorInfo = processor.getProcessorIdentifier().getName();
            return processorInfo;
         } else {
            return "Unknown";
         }
      }
   }

   private static boolean isLog4jDisabled() {
      return Boolean.getBoolean("log4j2.disable")
         || Boolean.getBoolean("net.coreprotect.disable.log4j")
         || System.getProperty("log4j.configurationFile", "").contains("no-log4j2.xml");
   }
}
