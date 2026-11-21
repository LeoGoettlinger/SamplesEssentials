package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.json;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.Version;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.Versioned;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.util.VersionUtil;

public final class PackageVersion implements Versioned {
   public static final Version VERSION = VersionUtil.parseVersion(
      "2.15.2", "org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core", "jackson-core"
   );

   @Override
   public Version version() {
      return VERSION;
   }
}
