package oshi.hardware.platform.linux;

import com.sun.jna.platform.linux.Udev;
import com.sun.jna.platform.linux.Udev.UdevContext;
import com.sun.jna.platform.linux.Udev.UdevDevice;
import com.sun.jna.platform.linux.Udev.UdevEnumerate;
import com.sun.jna.platform.linux.Udev.UdevListEntry;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.PowerSource;
import oshi.hardware.common.AbstractPowerSource;
import oshi.software.os.linux.LinuxOperatingSystem;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.SysPath;

@ThreadSafe
public final class LinuxPowerSource extends AbstractPowerSource {
   public LinuxPowerSource(
      String psName,
      String psDeviceName,
      double psRemainingCapacityPercent,
      double psTimeRemainingEstimated,
      double psTimeRemainingInstant,
      double psPowerUsageRate,
      double psVoltage,
      double psAmperage,
      boolean psPowerOnLine,
      boolean psCharging,
      boolean psDischarging,
      PowerSource.CapacityUnits psCapacityUnits,
      int psCurrentCapacity,
      int psMaxCapacity,
      int psDesignCapacity,
      int psCycleCount,
      String psChemistry,
      LocalDate psManufactureDate,
      String psManufacturer,
      String psSerialNumber,
      double psTemperature
   ) {
      super(
         psName,
         psDeviceName,
         psRemainingCapacityPercent,
         psTimeRemainingEstimated,
         psTimeRemainingInstant,
         psPowerUsageRate,
         psVoltage,
         psAmperage,
         psPowerOnLine,
         psCharging,
         psDischarging,
         psCapacityUnits,
         psCurrentCapacity,
         psMaxCapacity,
         psDesignCapacity,
         psCycleCount,
         psChemistry,
         psManufactureDate,
         psManufacturer,
         psSerialNumber,
         psTemperature
      );
   }

   public static List<PowerSource> getPowerSources() {
      double psRemainingCapacityPercent = -1.0;
      double psTimeRemainingEstimated = -1.0;
      double psTimeRemainingInstant = -1.0;
      double psPowerUsageRate = 0.0;
      double psVoltage = -1.0;
      double psAmperage = 0.0;
      boolean psPowerOnLine = false;
      boolean psCharging = false;
      boolean psDischarging = false;
      PowerSource.CapacityUnits psCapacityUnits = PowerSource.CapacityUnits.RELATIVE;
      int psCurrentCapacity = -1;
      int psMaxCapacity = -1;
      int psDesignCapacity = -1;
      int psCycleCount = -1;
      LocalDate psManufactureDate = null;
      double psTemperature = 0.0;
      List<PowerSource> psList = new ArrayList<>();
      if (LinuxOperatingSystem.HAS_UDEV) {
         UdevContext udev = Udev.INSTANCE.udev_new();

         try {
            UdevEnumerate enumerate = udev.enumerateNew();

            try {
               enumerate.addMatchSubsystem("power_supply");
               enumerate.scanDevices();

               for (UdevListEntry entry = enumerate.getListEntry(); entry != null; entry = entry.getNext()) {
                  String syspath = entry.getName();
                  String name = syspath.substring(syspath.lastIndexOf(File.separatorChar) + 1);
                  if (!name.startsWith("ADP") && !name.startsWith("AC")) {
                     UdevDevice device = udev.deviceNewFromSyspath(syspath);
                     if (device != null) {
                        try {
                           if (ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_PRESENT"), 1) > 0
                              && ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_ONLINE"), 1) > 0) {
                              String psName = getOrDefault(device, "POWER_SUPPLY_NAME", name);
                              String status = device.getPropertyValue("POWER_SUPPLY_STATUS");
                              psCharging = "Charging".equals(status);
                              psDischarging = "Discharging".equals(status);
                              psRemainingCapacityPercent = ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_CAPACITY"), -100) / 100.0;
                              psCurrentCapacity = ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_ENERGY_NOW"), -1);
                              if (psCurrentCapacity < 0) {
                                 psCurrentCapacity = ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_CHARGE_NOW"), -1);
                              }

                              psMaxCapacity = ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_ENERGY_FULL"), 1);
                              if (psMaxCapacity < 0) {
                                 psMaxCapacity = ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_CHARGE_FULL"), 1);
                              }

                              psDesignCapacity = ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_ENERGY_FULL_DESIGN"), 1);
                              if (psDesignCapacity < 0) {
                                 psDesignCapacity = ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_CHARGE_FULL_DESIGN"), 1);
                              }

                              psVoltage = ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_VOLTAGE_NOW"), -1);
                              if (psVoltage > 0.0) {
                                 String power = device.getPropertyValue("POWER_SUPPLY_POWER_NOW");
                                 String current = device.getPropertyValue("POWER_SUPPLY_CURRENT_NOW");
                                 if (power == null) {
                                    psAmperage = ParseUtil.parseIntOrDefault(current, 0);
                                    psPowerUsageRate = psAmperage * psVoltage;
                                 } else if (current == null) {
                                    psPowerUsageRate = ParseUtil.parseIntOrDefault(power, 0);
                                    psAmperage = psPowerUsageRate / psVoltage;
                                 } else {
                                    psAmperage = ParseUtil.parseIntOrDefault(current, 0);
                                    psPowerUsageRate = ParseUtil.parseIntOrDefault(power, 0);
                                 }
                              }

                              psCycleCount = ParseUtil.parseIntOrDefault(device.getPropertyValue("POWER_SUPPLY_CYCLE_COUNT"), -1);
                              String psChemistry = getOrDefault(device, "POWER_SUPPLY_TECHNOLOGY", "unknown");
                              String psDeviceName = getOrDefault(device, "POWER_SUPPLY_MODEL_NAME", "unknown");
                              String psManufacturer = getOrDefault(device, "POWER_SUPPLY_MANUFACTURER", "unknown");
                              String psSerialNumber = getOrDefault(device, "POWER_SUPPLY_SERIAL_NUMBER", "unknown");
                              psList.add(
                                 new LinuxPowerSource(
                                    psName,
                                    psDeviceName,
                                    psRemainingCapacityPercent,
                                    psTimeRemainingEstimated,
                                    psTimeRemainingInstant,
                                    psPowerUsageRate,
                                    psVoltage,
                                    psAmperage,
                                    psPowerOnLine,
                                    psCharging,
                                    psDischarging,
                                    psCapacityUnits,
                                    psCurrentCapacity,
                                    psMaxCapacity,
                                    psDesignCapacity,
                                    psCycleCount,
                                    psChemistry,
                                    psManufactureDate,
                                    psManufacturer,
                                    psSerialNumber,
                                    psTemperature
                                 )
                              );
                           }
                        } finally {
                           device.unref();
                        }
                     }
                  }
               }
            } finally {
               enumerate.unref();
            }
         } finally {
            udev.unref();
         }
      } else {
         File psDir = new File(SysPath.POWER_SUPPLY);
         File[] psArr = psDir.listFiles();
         if (psArr == null) {
            return Collections.emptyList();
         }

         for (File ps : psArr) {
            String name = ps.getName();
            if (!name.startsWith("ADP") && !name.startsWith("AC")) {
               List<String> psInfo = FileUtil.readFile(SysPath.POWER_SUPPLY + "/" + name + "/uevent", false);
               Map<String, String> psMap = new HashMap<>();

               for (String line : psInfo) {
                  String[] split = line.split("=");
                  if (split.length > 1 && !split[1].isEmpty()) {
                     psMap.put(split[0], split[1]);
                  }
               }

               String psNamex = psMap.getOrDefault("POWER_SUPPLY_NAME", name);
               String statusx = psMap.get("POWER_SUPPLY_STATUS");
               psCharging = "Charging".equals(statusx);
               psDischarging = "Discharging".equals(statusx);
               if (psMap.containsKey("POWER_SUPPLY_CAPACITY")) {
                  psRemainingCapacityPercent = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_CAPACITY"), -100) / 100.0;
               }

               if (psMap.containsKey("POWER_SUPPLY_ENERGY_NOW")) {
                  psCurrentCapacity = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_ENERGY_NOW"), -1);
               } else if (psMap.containsKey("POWER_SUPPLY_CHARGE_NOW")) {
                  psCurrentCapacity = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_CHARGE_NOW"), -1);
               }

               if (psMap.containsKey("POWER_SUPPLY_ENERGY_FULL")) {
                  psCurrentCapacity = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_ENERGY_FULL"), 1);
               } else if (psMap.containsKey("POWER_SUPPLY_CHARGE_FULL")) {
                  psCurrentCapacity = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_CHARGE_FULL"), 1);
               }

               if (psMap.containsKey("POWER_SUPPLY_ENERGY_FULL_DESIGN")) {
                  psMaxCapacity = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_ENERGY_FULL_DESIGN"), 1);
               } else if (psMap.containsKey("POWER_SUPPLY_CHARGE_FULL_DESIGN")) {
                  psMaxCapacity = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_CHARGE_FULL_DESIGN"), 1);
               }

               if (psMap.containsKey("POWER_SUPPLY_VOLTAGE_NOW")) {
                  psVoltage = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_VOLTAGE_NOW"), -1);
               }

               if (psVoltage > 0.0) {
                  if (psMap.containsKey("POWER_SUPPLY_POWER_NOW")) {
                     psPowerUsageRate = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_POWER_NOW"), -1);
                  }

                  if (psMap.containsKey("POWER_SUPPLY_CURRENT_NOW")) {
                     psAmperage = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_CURRENT_NOW"), -1);
                  }

                  if (psPowerUsageRate < 0.0 && psAmperage >= 0.0) {
                     psPowerUsageRate = psAmperage * psVoltage;
                  } else if (psPowerUsageRate >= 0.0 && psAmperage < 0.0) {
                     psAmperage = psPowerUsageRate / psVoltage;
                  } else {
                     psAmperage = 0.0;
                     psPowerUsageRate = 0.0;
                  }
               }

               if (psMap.containsKey("POWER_SUPPLY_CYCLE_COUNT")) {
                  psCycleCount = ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_CYCLE_COUNT"), -1);
               }

               String psChemistry = psMap.getOrDefault("POWER_SUPPLY_TECHNOLOGY", "unknown");
               String psDeviceName = psMap.getOrDefault("POWER_SUPPLY_MODEL_NAME", "unknown");
               String psManufacturer = psMap.getOrDefault("POWER_SUPPLY_MANUFACTURER", "unknown");
               String psSerialNumber = psMap.getOrDefault("POWER_SUPPLY_SERIAL_NUMBER", "unknown");
               if (ParseUtil.parseIntOrDefault(psMap.get("POWER_SUPPLY_PRESENT"), 1) > 0) {
                  psList.add(
                     new LinuxPowerSource(
                        psNamex,
                        psDeviceName,
                        psRemainingCapacityPercent,
                        psTimeRemainingEstimated,
                        psTimeRemainingInstant,
                        psPowerUsageRate,
                        psVoltage,
                        psAmperage,
                        psPowerOnLine,
                        psCharging,
                        psDischarging,
                        psCapacityUnits,
                        psCurrentCapacity,
                        psMaxCapacity,
                        psDesignCapacity,
                        psCycleCount,
                        psChemistry,
                        psManufactureDate,
                        psManufacturer,
                        psSerialNumber,
                        psTemperature
                     )
                  );
               }
            }
         }
      }

      return psList;
   }

   private static String getOrDefault(UdevDevice device, String property, String def) {
      String value = device.getPropertyValue(property);
      return value == null ? def : value;
   }
}
