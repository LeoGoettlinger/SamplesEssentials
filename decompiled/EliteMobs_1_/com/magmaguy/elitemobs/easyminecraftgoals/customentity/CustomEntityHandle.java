package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;

public interface CustomEntityHandle {
   CustomEntityDefinition definition();

   int getEntityId();

   Location getLocation();

   UUID getUniqueId();

   Set<UUID> getViewers();

   void setScale(float var1);

   void setColor(Integer var1);

   void setVariant(Integer var1);

   void setHitbox(float var1, float var2);

   void setProperties(Map<String, ?> var1);

   void setProperty(String var1, Object var2);
}
