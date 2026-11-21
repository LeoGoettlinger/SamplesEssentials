package oshi.driver.linux;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.FileUtil;
import oshi.util.ParseUtil;
import oshi.util.Util;
import oshi.util.platform.linux.SysPath;

@ThreadSafe
public final class Sysfs {
   private Sysfs() {
   }

   public static String querySystemVendor() {
      String sysVendor = FileUtil.getStringFromFile(SysPath.DMI_ID + "sys_vendor").trim();
      return !sysVendor.isEmpty() ? sysVendor : null;
   }

   public static String queryProductModel() {
      String productName = FileUtil.getStringFromFile(SysPath.DMI_ID + "product_name").trim();
      String productVersion = FileUtil.getStringFromFile(SysPath.DMI_ID + "product_version").trim();
      if (productName.isEmpty()) {
         return !productVersion.isEmpty() ? productVersion : null;
      } else {
         return !productVersion.isEmpty() && !"None".equals(productVersion) ? productName + " (version: " + productVersion + ")" : productName;
      }
   }

   public static String queryProductSerial() {
      String serial = FileUtil.getStringFromFile(SysPath.DMI_ID + "product_serial");
      return !serial.isEmpty() && !"None".equals(serial) ? serial : queryBoardSerial();
   }

   public static String queryUUID() {
      String uuid = FileUtil.getStringFromFile(SysPath.DMI_ID + "product_uuid");
      return !uuid.isEmpty() && !"None".equals(uuid) ? uuid : null;
   }

   public static String queryBoardVendor() {
      String boardVendor = FileUtil.getStringFromFile(SysPath.DMI_ID + "board_vendor").trim();
      return !boardVendor.isEmpty() ? boardVendor : null;
   }

   public static String queryBoardModel() {
      String boardName = FileUtil.getStringFromFile(SysPath.DMI_ID + "board_name").trim();
      return !boardName.isEmpty() ? boardName : null;
   }

   public static String queryBoardVersion() {
      String boardVersion = FileUtil.getStringFromFile(SysPath.DMI_ID + "board_version").trim();
      return !boardVersion.isEmpty() ? boardVersion : null;
   }

   public static String queryBoardSerial() {
      String boardSerial = FileUtil.getStringFromFile(SysPath.DMI_ID + "board_serial").trim();
      return !boardSerial.isEmpty() ? boardSerial : null;
   }

   public static String queryBiosVendor() {
      String biosVendor = FileUtil.getStringFromFile(SysPath.DMI_ID + "bios_vendor").trim();
      return biosVendor.isEmpty() ? biosVendor : null;
   }

   public static String queryBiosDescription() {
      String modalias = FileUtil.getStringFromFile(SysPath.DMI_ID + "modalias").trim();
      return !modalias.isEmpty() ? modalias : null;
   }

   public static String queryBiosVersion(String biosRevision) {
      String biosVersion = FileUtil.getStringFromFile(SysPath.DMI_ID + "bios_version").trim();
      return !biosVersion.isEmpty() ? biosVersion + (Util.isBlank(biosRevision) ? "" : " (revision " + biosRevision + ")") : null;
   }

   public static String queryBiosReleaseDate() {
      String biosDate = FileUtil.getStringFromFile(SysPath.DMI_ID + "bios_date").trim();
      return !biosDate.isEmpty() ? ParseUtil.parseMmDdYyyyToYyyyMmDD(biosDate) : null;
   }
}
