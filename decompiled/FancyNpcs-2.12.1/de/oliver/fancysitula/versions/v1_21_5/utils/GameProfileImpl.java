package de.oliver.fancysitula.versions.v1_21_5.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.oliver.fancysitula.api.utils.FS_GameProfile;
import java.util.Map.Entry;

public class GameProfileImpl {
   public static GameProfile asVanilla(FS_GameProfile gameProfile) {
      GameProfile gf = new GameProfile(gameProfile.getUUID(), gameProfile.getName());

      for (Entry<String, FS_GameProfile.Property> entry : gameProfile.getProperties().entrySet()) {
         FS_GameProfile.Property property = entry.getValue();
         gf.getProperties().put(entry.getKey(), new Property(property.name(), property.value(), property.signature()));
      }

      return gf;
   }

   public static FS_GameProfile fromVanilla(GameProfile gameProfile) {
      FS_GameProfile fsGameProfile = new FS_GameProfile(gameProfile.getId(), gameProfile.getName());

      for (Entry<String, Property> entry : gameProfile.getProperties().entries()) {
         Property property = entry.getValue();
         fsGameProfile.getProperties().put(entry.getKey(), new FS_GameProfile.Property(property.name(), property.value(), property.signature()));
      }

      return fsGameProfile;
   }
}
