package github.nighter.smartspawner.spawner.config;

import java.util.Locale;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public record SpawnerHead(Material material, String texture) {
   public boolean hasTexture() {
      return this.material == Material.PLAYER_HEAD && this.texture != null && !this.texture.isBlank() && !this.texture.equalsIgnoreCase("null");
   }

   public static SpawnerHead parse(ConfigurationSection entry, Material fallback, String label, Logger logger) {
      ConfigurationSection section = entry.getConfigurationSection("mob_head");
      if (section == null) {
         return null;
      } else {
         String materialName = section.getString("item", fallback.name());
         Material material = Material.matchMaterial(materialName == null ? "" : materialName.toUpperCase(Locale.ROOT));
         if (material == null || !material.isItem()) {
            logger.warning("Invalid mob_head item '" + materialName + "' for " + label + ", using " + fallback.name());
            material = fallback;
         }

         return new SpawnerHead(material, section.getString("hash_texture"));
      }
   }
}
