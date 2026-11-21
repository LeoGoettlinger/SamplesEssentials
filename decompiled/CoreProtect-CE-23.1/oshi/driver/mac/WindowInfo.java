package oshi.driver.mac;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFBooleanRef;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFNumberRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.jna.platform.mac.CoreGraphics;
import oshi.software.os.OSDesktopWindow;
import oshi.util.FormatUtil;
import oshi.util.platform.mac.CFUtil;

@ThreadSafe
public final class WindowInfo {
   private WindowInfo() {
   }

   public static List<OSDesktopWindow> queryDesktopWindows(boolean visibleOnly) {
      CFArrayRef windowInfo = CoreGraphics.INSTANCE.CGWindowListCopyWindowInfo(visibleOnly ? 17 : 0, 0);
      int numWindows = windowInfo.getCount();
      List<OSDesktopWindow> windowList = new ArrayList<>();
      CFStringRef kCGWindowIsOnscreen = CFStringRef.createCFString("kCGWindowIsOnscreen");
      CFStringRef kCGWindowNumber = CFStringRef.createCFString("kCGWindowNumber");
      CFStringRef kCGWindowOwnerPID = CFStringRef.createCFString("kCGWindowOwnerPID");
      CFStringRef kCGWindowLayer = CFStringRef.createCFString("kCGWindowLayer");
      CFStringRef kCGWindowBounds = CFStringRef.createCFString("kCGWindowBounds");
      CFStringRef kCGWindowName = CFStringRef.createCFString("kCGWindowName");
      CFStringRef kCGWindowOwnerName = CFStringRef.createCFString("kCGWindowOwnerName");

      try {
         for (int i = 0; i < numWindows; i++) {
            Pointer result = windowInfo.getValueAtIndex(i);
            CFDictionaryRef windowRef = new CFDictionaryRef(result);
            result = windowRef.getValue(kCGWindowIsOnscreen);
            boolean visible = result == null || new CFBooleanRef(result).booleanValue();
            if (!visibleOnly || visible) {
               result = windowRef.getValue(kCGWindowNumber);
               long windowNumber = new CFNumberRef(result).longValue();
               result = windowRef.getValue(kCGWindowOwnerPID);
               long windowOwnerPID = new CFNumberRef(result).longValue();
               result = windowRef.getValue(kCGWindowLayer);
               int windowLayer = new CFNumberRef(result).intValue();
               result = windowRef.getValue(kCGWindowBounds);
               CoreGraphics.CGRect rect = new CoreGraphics.CGRect();

               try {
                  CoreGraphics.INSTANCE.CGRectMakeWithDictionaryRepresentation(new CFDictionaryRef(result), rect);
                  Rectangle windowBounds = new Rectangle(
                     FormatUtil.roundToInt(rect.origin.x),
                     FormatUtil.roundToInt(rect.origin.y),
                     FormatUtil.roundToInt(rect.size.width),
                     FormatUtil.roundToInt(rect.size.height)
                  );
                  result = windowRef.getValue(kCGWindowName);
                  String windowName = CFUtil.cfPointerToString(result, false);
                  result = windowRef.getValue(kCGWindowOwnerName);
                  String windowOwnerName = CFUtil.cfPointerToString(result, false);
                  if (windowName.isEmpty()) {
                     windowName = windowOwnerName;
                  } else {
                     windowName = windowName + "(" + windowOwnerName + ")";
                  }

                  windowList.add(new OSDesktopWindow(windowNumber, windowName, windowOwnerName, windowBounds, windowOwnerPID, windowLayer, visible));
               } catch (Throwable var29) {
                  try {
                     rect.close();
                  } catch (Throwable var28) {
                     var29.addSuppressed(var28);
                  }

                  throw var29;
               }

               rect.close();
            }
         }
      } finally {
         kCGWindowIsOnscreen.release();
         kCGWindowNumber.release();
         kCGWindowOwnerPID.release();
         kCGWindowLayer.release();
         kCGWindowBounds.release();
         kCGWindowName.release();
         kCGWindowOwnerName.release();
         windowInfo.release();
      }

      return windowList;
   }
}
