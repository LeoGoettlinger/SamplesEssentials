package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32DiskDriveToDiskPartition {
   private static final String WIN32_DISK_DRIVE_TO_DISK_PARTITION = "Win32_DiskDriveToDiskPartition";

   private Win32DiskDriveToDiskPartition() {
   }

   public static WmiResult<Win32DiskDriveToDiskPartition.DriveToPartitionProperty> queryDriveToPartition(WmiQueryHandler h) {
      WmiQuery<Win32DiskDriveToDiskPartition.DriveToPartitionProperty> driveToPartitionQuery = new WmiQuery(
         "Win32_DiskDriveToDiskPartition", Win32DiskDriveToDiskPartition.DriveToPartitionProperty.class
      );
      return h.queryWMI(driveToPartitionQuery, false);
   }

   public static enum DriveToPartitionProperty {
      ANTECEDENT,
      DEPENDENT;
   }
}
