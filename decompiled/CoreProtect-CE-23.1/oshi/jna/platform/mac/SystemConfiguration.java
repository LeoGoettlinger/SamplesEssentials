package oshi.jna.platform.mac;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.platform.mac.CoreFoundation.CFTypeRef;

public interface SystemConfiguration extends Library {
   SystemConfiguration INSTANCE = (SystemConfiguration)Native.load("SystemConfiguration", SystemConfiguration.class);

   CFArrayRef SCNetworkInterfaceCopyAll();

   CFStringRef SCNetworkInterfaceGetBSDName(SystemConfiguration.SCNetworkInterfaceRef var1);

   CFStringRef SCNetworkInterfaceGetLocalizedDisplayName(SystemConfiguration.SCNetworkInterfaceRef var1);

   public static class SCNetworkInterfaceRef extends CFTypeRef {
      public SCNetworkInterfaceRef() {
      }

      public SCNetworkInterfaceRef(Pointer p) {
         super(p);
      }
   }
}
