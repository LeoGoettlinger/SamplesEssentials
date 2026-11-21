package oshi.util.platform.mac;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class CFUtil {
   private CFUtil() {
   }

   public static String cfPointerToString(Pointer result) {
      return cfPointerToString(result, true);
   }

   public static String cfPointerToString(Pointer result, boolean returnUnknown) {
      String s = "";
      if (result != null) {
         CFStringRef cfs = new CFStringRef(result);
         s = cfs.stringValue();
      }

      return returnUnknown && s.isEmpty() ? "unknown" : s;
   }
}
