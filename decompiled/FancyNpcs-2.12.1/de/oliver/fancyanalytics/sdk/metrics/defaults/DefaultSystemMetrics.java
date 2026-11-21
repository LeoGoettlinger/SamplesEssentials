package de.oliver.fancyanalytics.sdk.metrics.defaults;

import com.sun.management.OperatingSystemMXBean;
import java.io.File;
import java.lang.management.ManagementFactory;

public class DefaultSystemMetrics {
   public static String operatingSystem() {
      return System.getProperty("os.name");
   }

   public static String operatingSystemVersion() {
      return System.getProperty("os.version");
   }

   public static String operatingSystemArchitecture() {
      return System.getProperty("os.arch");
   }

   public static double cpuCoreCount() {
      return Runtime.getRuntime().availableProcessors();
   }

   public static String javaVersion() {
      return System.getProperty("java.version");
   }

   public static String jvmName() {
      return System.getProperty("java.vm.name");
   }

   public static double maxMemoryGB() {
      return Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 / 1024.0;
   }

   public static double usedMemoryGB() {
      return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0 / 1024.0;
   }

   public static double usedMemoryPercentage() {
      return (double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / Runtime.getRuntime().maxMemory();
   }

   public static double cpuUsage() {
      return ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean sunOsBean ? sunOsBean.getCpuLoad() : 0.0;
   }

   public static double freeDiskSpaceGB() {
      return new File("/").getFreeSpace() / 1024.0 / 1024.0 / 1024.0;
   }

   public static double usedDiskSpaceGB() {
      return (new File("/").getTotalSpace() - new File("/").getFreeSpace()) / 1024.0 / 1024.0 / 1024.0;
   }

   public static double usedDiskSpacePercentage() {
      return (double)(new File("/").getTotalSpace() - new File("/").getFreeSpace()) / new File("/").getTotalSpace();
   }

   public static double uptimeSeconds() {
      return ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0;
   }
}
