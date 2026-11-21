package net.ess3.provider.providers;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.ess3.provider.MaterialTagProvider;
import net.essentialsx.providers.ProviderData;
import net.essentialsx.providers.ProviderTest;
import org.bukkit.Material;
import org.bukkit.Tag;

@ProviderData(
   description = "Bukkit Material Tag Provider"
)
public class BukkitMaterialTagProvider implements MaterialTagProvider {
   private final Map<String, Tag<Material>> stringToTagMap = new HashMap<>();

   @Override
   public boolean tagExists(String tagName) {
      return tagName == null ? false : this.getTag(tagName) != null;
   }

   @Override
   public boolean isTagged(String tagName, Material material) {
      if (tagName != null && material != null) {
         Tag<Material> tag = this.getTag(tagName);
         return tag != null && tag.isTagged(material);
      } else {
         return false;
      }
   }

   private Tag<Material> getTag(String tagName) {
      if (tagName == null) {
         return null;
      } else {
         tagName = tagName.toUpperCase();
         if (!this.stringToTagMap.containsKey(tagName)) {
            try {
               Field field = Tag.class.getDeclaredField(tagName.toUpperCase());
               this.stringToTagMap.put(tagName, (Tag<Material>)field.get(null));
            } catch (IllegalAccessException | ClassCastException | NoSuchFieldException var3) {
               this.stringToTagMap.put(tagName, null);
            }
         }

         return this.stringToTagMap.get(tagName);
      }
   }

   @ProviderTest
   public static boolean test() {
      try {
         Class.forName("org.bukkit.Tag");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }
}
