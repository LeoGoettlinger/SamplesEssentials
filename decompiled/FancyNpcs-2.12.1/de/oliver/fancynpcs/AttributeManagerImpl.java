package de.oliver.fancynpcs;

import de.oliver.fancynpcs.api.AttributeManager;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.v1_21_11.attributes.Attributes_1_21_11;
import de.oliver.fancynpcs.v1_21_6.attributes.Attributes_1_21_6;
import de.oliver.fancynpcs.v1_21_9.attributes.Attributes_1_21_9;
import de.oliver.fancynpcs.v26_1_1.attributes.Attributes_26_1_1;
import de.oliver.fancynpcs.v26_2.attributes.Attributes_26_2;
import de.oliver.fancynpcs.v26_3.attributes.Attributes_26_3;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

public class AttributeManagerImpl implements AttributeManager {
   private List<NpcAttribute> attributes = new ArrayList<>();

   public AttributeManagerImpl() {
      this.init();
   }

   private void init() {
      String mcVersion = Bukkit.getMinecraftVersion();
      switch (mcVersion) {
         case "26.3":
            this.attributes = Attributes_26_3.getAllAttributes();
            break;
         case "26.2":
            this.attributes = Attributes_26_2.getAllAttributes();
            break;
         case "26.1.2":
            this.attributes = Attributes_26_1_1.getAllAttributes();
            break;
         case "1.21.11":
            this.attributes = Attributes_1_21_11.getAllAttributes();
            break;
         case "1.21.9":
         case "1.21.10":
            this.attributes = Attributes_1_21_9.getAllAttributes();
            break;
         case "1.21.6":
         case "1.21.7":
         case "1.21.8":
            this.attributes = Attributes_1_21_6.getAllAttributes();
      }
   }

   @Override
   public NpcAttribute getAttributeByName(EntityType type, String name) {
      for (NpcAttribute attribute : this.attributes) {
         if (attribute.getTypes().contains(type) && attribute.getName().equalsIgnoreCase(name)) {
            return attribute;
         }
      }

      return null;
   }

   @Override
   public void registerAttribute(NpcAttribute attribute) {
      this.attributes.add(attribute);
   }

   @Override
   public List<NpcAttribute> getAllAttributes() {
      return this.attributes;
   }

   @Override
   public List<NpcAttribute> getAllAttributesForEntityType(EntityType type) {
      return this.attributes.stream().filter(attribute -> attribute.getTypes().contains(type)).toList();
   }
}
