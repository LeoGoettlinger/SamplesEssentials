package de.oliver.fancynpcs.api.skins;

import java.util.UUID;

public interface SkinManager {
   SkinData getByIdentifier(String var1, SkinData.SkinVariant var2);

   SkinData getByUUID(UUID var1, SkinData.SkinVariant var2);

   SkinData getByUsername(String var1, SkinData.SkinVariant var2);

   SkinData getByURL(String var1, SkinData.SkinVariant var2);

   SkinData getByFile(String var1, SkinData.SkinVariant var2);
}
