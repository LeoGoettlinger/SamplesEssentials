package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32ComputerSystem {
   private static final String WIN32_COMPUTER_SYSTEM = "Win32_ComputerSystem";

   private Win32ComputerSystem() {
   }

   public static WmiResult<Win32ComputerSystem.ComputerSystemProperty> queryComputerSystem() {
      WmiQuery<Win32ComputerSystem.ComputerSystemProperty> computerSystemQuery = new WmiQuery(
         "Win32_ComputerSystem", Win32ComputerSystem.ComputerSystemProperty.class
      );
      return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(computerSystemQuery);
   }

   public static enum ComputerSystemProperty {
      MANUFACTURER,
      MODEL;
   }
}
