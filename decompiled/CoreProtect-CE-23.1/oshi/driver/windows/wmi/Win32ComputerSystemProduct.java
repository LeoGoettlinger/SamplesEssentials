package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32ComputerSystemProduct {
   private static final String WIN32_COMPUTER_SYSTEM_PRODUCT = "Win32_ComputerSystemProduct";

   private Win32ComputerSystemProduct() {
   }

   public static WmiResult<Win32ComputerSystemProduct.ComputerSystemProductProperty> queryIdentifyingNumberUUID() {
      WmiQuery<Win32ComputerSystemProduct.ComputerSystemProductProperty> identifyingNumberQuery = new WmiQuery(
         "Win32_ComputerSystemProduct", Win32ComputerSystemProduct.ComputerSystemProductProperty.class
      );
      return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(identifyingNumberQuery);
   }

   public static enum ComputerSystemProductProperty {
      IDENTIFYINGNUMBER,
      UUID;
   }
}
