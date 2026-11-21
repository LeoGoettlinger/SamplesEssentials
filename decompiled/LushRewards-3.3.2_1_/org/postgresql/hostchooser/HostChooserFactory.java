package org.postgresql.hostchooser;

import java.util.Properties;
import org.postgresql.util.HostSpec;

public class HostChooserFactory {
   public static HostChooser createHostChooser(HostSpec[] hostSpecs, HostRequirement targetServerType, Properties info) {
      return (HostChooser)(hostSpecs.length == 1
         ? new SingleHostChooser(hostSpecs[0], targetServerType)
         : new MultiHostChooser(hostSpecs, targetServerType, info));
   }
}
