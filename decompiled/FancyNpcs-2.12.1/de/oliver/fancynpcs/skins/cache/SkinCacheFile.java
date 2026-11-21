package de.oliver.fancynpcs.skins.cache;

import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.skins.SkinData;
import de.oliver.jdb.JDB;
import java.io.IOException;
import java.util.Base64;

public class SkinCacheFile implements SkinCache {
   private final JDB storage = new JDB("plugins/FancyNpcs/.data");

   @Override
   public SkinCacheData getSkin(String identifier) {
      String b64ID = Base64.getEncoder().encodeToString(identifier.getBytes());
      SkinCacheData skinCacheData = null;

      try {
         skinCacheData = this.storage.get("skins/" + b64ID, SkinCacheData.class);
      } catch (IOException var5) {
         FancyNpcs.getInstance().getFancyLogger().error("Failed to load skin cache", ThrowableProperty.of(var5));
      }

      if (skinCacheData == null) {
         return null;
      } else if (skinCacheData.isExpired()) {
         this.storage.delete("skins/" + b64ID);
         return null;
      } else {
         return skinCacheData;
      }
   }

   @Override
   public void addSkin(SkinData skin) {
      SkinCacheData skinCacheData = new SkinCacheData(skin, System.currentTimeMillis(), 604800000L);

      try {
         String b64ID = Base64.getEncoder().encodeToString(skin.getIdentifier().getBytes());
         this.storage.set("skins/" + b64ID, skinCacheData);
      } catch (IOException var4) {
         FancyNpcs.getInstance().getFancyLogger().error("Failed to save skin cache", ThrowableProperty.of(var4));
      }
   }

   @Override
   public void removeSkin(String identifier) {
      String b64ID = Base64.getEncoder().encodeToString(identifier.getBytes());
      this.storage.delete("skins/" + b64ID);
   }

   @Override
   public void clear() {
      this.storage.delete("skins");
   }
}
