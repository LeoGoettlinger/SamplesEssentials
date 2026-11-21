package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32VideoController {
   private static final String WIN32_VIDEO_CONTROLLER = "Win32_VideoController";

   private Win32VideoController() {
   }

   public static WmiResult<Win32VideoController.VideoControllerProperty> queryVideoController() {
      WmiQuery<Win32VideoController.VideoControllerProperty> videoControllerQuery = new WmiQuery(
         "Win32_VideoController", Win32VideoController.VideoControllerProperty.class
      );
      return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(videoControllerQuery);
   }

   public static enum VideoControllerProperty {
      ADAPTERCOMPATIBILITY,
      ADAPTERRAM,
      DRIVERVERSION,
      NAME,
      PNPDEVICEID;
   }
}
