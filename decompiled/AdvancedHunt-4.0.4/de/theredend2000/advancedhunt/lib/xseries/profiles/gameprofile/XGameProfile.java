package de.theredend2000.advancedhunt.lib.xseries.profiles.gameprofile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import de.theredend2000.advancedhunt.lib.xseries.reflection.XReflection;
import java.util.UUID;
import javax.annotation.Nullable;

public class XGameProfile {
   private static final boolean USE_RECORDS = XReflection.isRecord(GameProfile.class);

   private XGameProfile() {
   }

   @Nullable
   public static MojangGameProfile of(GameProfile gameProfile) {
      if (gameProfile == null) {
         return null;
      } else {
         return (MojangGameProfile)(USE_RECORDS ? new NewGameProfile(gameProfile) : new OldGameProfile(gameProfile));
      }
   }

   public static MojangGameProfile create(UUID id, String name, PropertyMap properties) {
      if (USE_RECORDS) {
         return of(new GameProfile(id, name, properties));
      } else {
         GameProfile gameProfile = new GameProfile(id, name);
         MojangGameProfile converted = of(gameProfile);
         converted.properties().putAll(properties);
         return converted;
      }
   }

   public static MojangGameProfile create(UUID id, String name) {
      return of(new GameProfile(id, name));
   }
}
