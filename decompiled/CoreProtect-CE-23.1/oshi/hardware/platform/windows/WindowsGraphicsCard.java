package oshi.hardware.platform.windows;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.Immutable;
import oshi.driver.windows.wmi.Win32VideoController;
import oshi.hardware.GraphicsCard;
import oshi.hardware.common.AbstractGraphicsCard;
import oshi.util.ParseUtil;
import oshi.util.Util;
import oshi.util.platform.windows.WmiUtil;
import oshi.util.tuples.Triplet;

@Immutable
final class WindowsGraphicsCard extends AbstractGraphicsCard {
   private static final Logger LOG = LoggerFactory.getLogger(WindowsGraphicsCard.class);
   private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();
   public static final String ADAPTER_STRING = "HardwareInformation.AdapterString";
   public static final String DRIVER_DESC = "DriverDesc";
   public static final String DRIVER_VERSION = "DriverVersion";
   public static final String VENDOR = "ProviderName";
   public static final String QW_MEMORY_SIZE = "HardwareInformation.qwMemorySize";
   public static final String MEMORY_SIZE = "HardwareInformation.MemorySize";
   public static final String DISPLAY_DEVICES_REGISTRY_PATH = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}\\";

   WindowsGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
      super(name, deviceId, vendor, versionInfo, vram);
   }

   public static List<GraphicsCard> getGraphicsCards() {
      List<GraphicsCard> cardList = new ArrayList<>();
      int index = 1;
      String[] keys = Advapi32Util.registryGetKeys(
         WinReg.HKEY_LOCAL_MACHINE, "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}\\"
      );

      for (String key : keys) {
         if (key.startsWith("0")) {
            try {
               String fullKey = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}\\" + key;
               if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, "HardwareInformation.AdapterString")) {
                  String name = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "DriverDesc");
                  String deviceId = "VideoController" + index++;
                  String vendor = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "ProviderName");
                  String versionInfo = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "DriverVersion");
                  long vram = 0L;
                  if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, "HardwareInformation.qwMemorySize")) {
                     vram = Advapi32Util.registryGetLongValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "HardwareInformation.qwMemorySize");
                  } else if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, "HardwareInformation.MemorySize")) {
                     Object genericValue = Advapi32Util.registryGetValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, "HardwareInformation.MemorySize");
                     if (genericValue instanceof Long) {
                        vram = (Long)genericValue;
                     } else if (genericValue instanceof Integer) {
                        vram = Integer.toUnsignedLong((Integer)genericValue);
                     } else if (genericValue instanceof byte[]) {
                        byte[] bytes = (byte[])genericValue;
                        vram = ParseUtil.byteArrayToLong(bytes, bytes.length, false);
                     }
                  }

                  cardList.add(
                     new WindowsGraphicsCard(
                        Util.isBlank(name) ? "unknown" : name,
                        Util.isBlank(deviceId) ? "unknown" : deviceId,
                        Util.isBlank(vendor) ? "unknown" : vendor,
                        Util.isBlank(versionInfo) ? "unknown" : versionInfo,
                        vram
                     )
                  );
               }
            } catch (Win32Exception var16) {
               if (var16.getErrorCode() != 5) {
                  throw var16;
               }
            }
         }
      }

      return cardList.isEmpty() ? getGraphicsCardsFromWmi() : cardList;
   }

   private static List<GraphicsCard> getGraphicsCardsFromWmi() {
      List<GraphicsCard> cardList = new ArrayList<>();
      if (IS_VISTA_OR_GREATER) {
         WmiResult<Win32VideoController.VideoControllerProperty> cards = Win32VideoController.queryVideoController();

         for (int index = 0; index < cards.getResultCount(); index++) {
            String name = WmiUtil.getString(cards, Win32VideoController.VideoControllerProperty.NAME, index);
            Triplet<String, String, String> idPair = ParseUtil.parseDeviceIdToVendorProductSerial(
               WmiUtil.getString(cards, Win32VideoController.VideoControllerProperty.PNPDEVICEID, index)
            );
            String deviceId = idPair == null ? "unknown" : idPair.getB();
            String vendor = WmiUtil.getString(cards, Win32VideoController.VideoControllerProperty.ADAPTERCOMPATIBILITY, index);
            if (idPair != null) {
               if (Util.isBlank(vendor)) {
                  deviceId = idPair.getA();
               } else {
                  vendor = vendor + " (" + idPair.getA() + ")";
               }
            }

            String versionInfo = WmiUtil.getString(cards, Win32VideoController.VideoControllerProperty.DRIVERVERSION, index);
            if (!Util.isBlank(versionInfo)) {
               versionInfo = "DriverVersion=" + versionInfo;
            } else {
               versionInfo = "unknown";
            }

            long vram = WmiUtil.getUint32asLong(cards, Win32VideoController.VideoControllerProperty.ADAPTERRAM, index);
            cardList.add(new WindowsGraphicsCard(Util.isBlank(name) ? "unknown" : name, deviceId, Util.isBlank(vendor) ? "unknown" : vendor, versionInfo, vram));
         }
      }

      return cardList;
   }
}
