package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32Fan {
   private static final String WIN32_FAN = "Win32_Fan";

   private Win32Fan() {
   }

   public static WmiResult<Win32Fan.SpeedProperty> querySpeed() {
      WmiQuery<Win32Fan.SpeedProperty> fanQuery = new WmiQuery("Win32_Fan", Win32Fan.SpeedProperty.class);
      return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(fanQuery);
   }

   public static enum SpeedProperty {
      DESIREDSPEED;
   }
}
