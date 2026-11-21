package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32BaseBoard {
   private static final String WIN32_BASEBOARD = "Win32_BaseBoard";

   private Win32BaseBoard() {
   }

   public static WmiResult<Win32BaseBoard.BaseBoardProperty> queryBaseboardInfo() {
      WmiQuery<Win32BaseBoard.BaseBoardProperty> baseboardQuery = new WmiQuery("Win32_BaseBoard", Win32BaseBoard.BaseBoardProperty.class);
      return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(baseboardQuery);
   }

   public static enum BaseBoardProperty {
      MANUFACTURER,
      MODEL,
      VERSION,
      SERIALNUMBER;
   }
}
