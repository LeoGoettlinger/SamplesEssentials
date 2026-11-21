package net.ess3.provider.providers;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.ess3.provider.MaterialTagProvider;
import net.essentialsx.providers.ProviderData;
import net.essentialsx.providers.ProviderTest;
import org.bukkit.Material;
import org.bukkit.Tag;

@ProviderData(
   description = "Paper Material Tag Provider",
   weight = 1
)
public class PaperMaterialTagProvider implements MaterialTagProvider {
   private final Map<String, Tag<Material>> bukkitTagMap = new HashMap<>();
   private final Map<String, MaterialSetTag> paperTagMap = new HashMap<>();

   @Override
   public boolean tagExists(String tagName) {
      return tagName == null ? false : this.getBukkitTag(tagName) != null || this.getPaperTag(tagName) != null;
   }

   @Override
   public boolean isTagged(String tagName, Material material) {
      if (tagName == null) {
         return false;
      } else if (this.getBukkitTag(tagName) != null) {
         return this.getBukkitTag(tagName).isTagged(material);
      } else {
         return this.getPaperTag(tagName) != null ? this.getPaperTag(tagName).isTagged(material) : false;
      }
   }

   private MaterialSetTag getPaperTag(String tagName) {
      if (tagName == null) {
         return null;
      } else {
         tagName = tagName.toUpperCase();
         if (!this.paperTagMap.containsKey(tagName)) {
            try {
               Field field = MaterialTags.class.getDeclaredField(tagName.toUpperCase());
               this.paperTagMap.put(tagName, (MaterialSetTag)field.get(null));
            } catch (IllegalAccessException | ClassCastException | NoSuchFieldException var3) {
               this.paperTagMap.put(tagName, null);
            }
         }

         return this.paperTagMap.get(tagName);
      }
   }

   private Tag<Material> getBukkitTag(String tagName) {
      if (tagName == null) {
         return null;
      } else {
         tagName = tagName.toUpperCase();
         if (!this.bukkitTagMap.containsKey(tagName)) {
            try {
               Field field = Tag.class.getDeclaredField(tagName.toUpperCase());
               this.bukkitTagMap.put(tagName, (Tag<Material>)field.get(null));
            } catch (IllegalAccessException | ClassCastException | NoSuchFieldException var3) {
               this.bukkitTagMap.put(tagName, null);
            }
         }

         return this.bukkitTagMap.get(tagName);
      }
   }

   @ProviderTest
   public static boolean test() {
      try {
         Class.forName("org.bukkit.Tag");
         Class.forName("com.destroystokyo.paper.MaterialTags");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }
}
