package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32Bios {
   private static final String WIN32_BIOS_WHERE_PRIMARY_BIOS_TRUE = "Win32_BIOS where PrimaryBIOS=true";

   private Win32Bios() {
   }

   public static WmiResult<Win32Bios.BiosSerialProperty> querySerialNumber() {
      WmiQuery<Win32Bios.BiosSerialProperty> serialNumQuery = new WmiQuery("Win32_BIOS where PrimaryBIOS=true", Win32Bios.BiosSerialProperty.class);
      return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(serialNumQuery);
   }

   public static WmiResult<Win32Bios.BiosProperty> queryBiosInfo() {
      WmiQuery<Win32Bios.BiosProperty> biosQuery = new WmiQuery("Win32_BIOS where PrimaryBIOS=true", Win32Bios.BiosProperty.class);
      return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(biosQuery);
   }

   public static enum BiosProperty {
      MANUFACTURER,
      NAME,
      DESCRIPTION,
      VERSION,
      RELEASEDATE;
   }

   public static enum BiosSerialProperty {
      SERIALNUMBER;
   }
}
