package oshi.driver.linux;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.FileUtil;
import oshi.util.platform.linux.SysPath;

@ThreadSafe
public final class Devicetree {
   private Devicetree() {
   }

   public static String queryModel() {
      String modelStr = FileUtil.getStringFromFile(SysPath.MODEL);
      return !modelStr.isEmpty() ? modelStr.replace("Machine: ", "") : null;
   }
}
