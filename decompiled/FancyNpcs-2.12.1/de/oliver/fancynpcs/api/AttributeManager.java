package de.oliver.fancynpcs.api;

import java.util.List;
import org.bukkit.entity.EntityType;

public interface AttributeManager {
   NpcAttribute getAttributeByName(EntityType var1, String var2);

   List<NpcAttribute> getAllAttributes();

   List<NpcAttribute> getAllAttributesForEntityType(EntityType var1);

   void registerAttribute(NpcAttribute var1);
}
