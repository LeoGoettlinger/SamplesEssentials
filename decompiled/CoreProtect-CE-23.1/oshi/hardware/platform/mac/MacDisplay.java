package oshi.hardware.platform.mac;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.IOKitUtil;
import com.sun.jna.platform.mac.CoreFoundation.CFDataRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.platform.mac.CoreFoundation.CFTypeRef;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.Display;
import oshi.hardware.common.AbstractDisplay;

@Immutable
final class MacDisplay extends AbstractDisplay {
   private static final Logger LOG = LoggerFactory.getLogger(MacDisplay.class);

   MacDisplay(byte[] edid) {
      super(edid);
      LOG.debug("Initialized MacDisplay");
   }

   public static List<Display> getDisplays() {
      List<Display> displays = new ArrayList<>();
      IOIterator serviceIterator = IOKitUtil.getMatchingServices("IODisplayConnect");
      if (serviceIterator != null) {
         CFStringRef cfEdid = CFStringRef.createCFString("IODisplayEDID");

         for (IORegistryEntry sdService = serviceIterator.next(); sdService != null; sdService = serviceIterator.next()) {
            IORegistryEntry properties = sdService.getChildEntry("IOService");
            if (properties != null) {
               CFTypeRef edidRaw = properties.createCFProperty(cfEdid);
               if (edidRaw != null) {
                  CFDataRef edid = new CFDataRef(edidRaw.getPointer());
                  int length = edid.getLength();
                  Pointer p = edid.getBytePtr();
                  displays.add(new MacDisplay(p.getByteArray(0L, length)));
                  edid.release();
               }

               properties.release();
            }

            sdService.release();
         }

         serviceIterator.release();
         cfEdid.release();
      }

      return displays;
   }
}
