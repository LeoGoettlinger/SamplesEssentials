package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Locale;

public final class ClientVersionResolver {
   private ClientVersionResolver() {
   }

   public static int legacyStringToPvn(@Nullable String versionString) {
      if (versionString == null) {
         return -1;
      } else {
         String trimmed = versionString.trim();
         if (trimmed.isEmpty()) {
            return -1;
         } else {
            String enumName = "V_" + trimmed.replace('.', '_').toUpperCase(Locale.ROOT);

            try {
               ClientVersion cv = ClientVersion.valueOf(enumName);
               return cv.getProtocolVersion();
            } catch (IllegalArgumentException var4) {
               return -1;
            }
         }
      }
   }
}
