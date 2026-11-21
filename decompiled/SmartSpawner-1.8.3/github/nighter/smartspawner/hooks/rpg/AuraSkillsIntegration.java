package github.nighter.smartspawner.hooks.rpg;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;
import github.nighter.smartspawner.SmartSpawner;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class AuraSkillsIntegration {
   private final SmartSpawner plugin;
   private final Map<EntityType, AuraSkillsIntegration.SkillConfig> entitySkillMap;
   private FileConfiguration config;
   private File configFile;
   private boolean enabled = false;
   private AuraSkillsApi auraSkillsApi;

   public AuraSkillsIntegration(SmartSpawner plugin) {
      this.plugin = plugin;
      this.entitySkillMap = new HashMap<>();
      if (this.initializeAuraSkills()) {
         this.loadConfig();
         this.loadEntityMappings();
      }
   }

   private boolean initializeAuraSkills() {
      try {
         if (this.plugin.getServer().getPluginManager().getPlugin("AuraSkills") == null) {
            return false;
         } else {
            this.auraSkillsApi = AuraSkillsApi.get();
            if (this.auraSkillsApi == null) {
               this.plugin.getLogger().warning("Failed to get AuraSkills API instance");
               return false;
            } else {
               this.plugin.getLogger().info("AuraSkills integration initialized successfully!");
               return true;
            }
         }
      } catch (Exception | NoClassDefFoundError var2) {
         return false;
      }
   }

   private void loadConfig() {
      this.configFile = new File(this.plugin.getDataFolder(), "auraskills.yml");
      if (!this.configFile.exists()) {
         this.plugin.saveResource("auraskills.yml", false);
      }

      this.config = YamlConfiguration.loadConfiguration(this.configFile);
      this.enabled = this.config.getBoolean("enabled", true);
   }

   private void loadEntityMappings() {
      if (this.enabled) {
         this.entitySkillMap.clear();
         ConfigurationSection entitySection = this.config.getConfigurationSection("entity_skills");
         if (entitySection == null) {
            this.plugin.getLogger().warning("No entity_skills section found in auraskills.yml");
         } else {
            for (String entityName : entitySection.getKeys(false)) {
               try {
                  EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                  ConfigurationSection entityConfig = entitySection.getConfigurationSection(entityName);
                  if (entityConfig != null) {
                     String skillName = entityConfig.getString("skill", "FIGHTING");
                     double ratio = entityConfig.getDouble("ratio", 0.5);
                     AuraSkillsIntegration.SkillConfig skillConfig = new AuraSkillsIntegration.SkillConfig(skillName, ratio);
                     this.entitySkillMap.put(entityType, skillConfig);
                  }
               } catch (IllegalArgumentException var10) {
                  this.plugin.getLogger().warning("Invalid entity type in auraskills.yml: " + entityName);
               }
            }

            this.plugin.getLogger().info("Loaded " + this.entitySkillMap.size() + " AuraSkills entity mappings");
         }
      }
   }

   public boolean isEnabled() {
      return this.enabled && this.auraSkillsApi != null;
   }

   public void giveSkillXp(Player player, EntityType entityType, int spawnerExp) {
      if (this.isEnabled()) {
         AuraSkillsIntegration.SkillConfig skillConfig = this.entitySkillMap.get(entityType);
         if (skillConfig != null) {
            try {
               SkillsUser user = this.auraSkillsApi.getUser(player.getUniqueId());
               if (user == null) {
                  return;
               }

               double skillXp = spawnerExp * skillConfig.ratio();
               String var8 = skillConfig.skillName().toUpperCase();
               switch (var8) {
                  case "FIGHTING":
                     user.addSkillXp(Skills.FIGHTING, skillXp);
                     break;
                  case "FARMING":
                     user.addSkillXp(Skills.FARMING, skillXp);
                     break;
                  case "MINING":
                     user.addSkillXp(Skills.MINING, skillXp);
                     break;
                  case "FORAGING":
                     user.addSkillXp(Skills.FORAGING, skillXp);
                     break;
                  case "FISHING":
                     user.addSkillXp(Skills.FISHING, skillXp);
                     break;
                  case "EXCAVATION":
                     user.addSkillXp(Skills.EXCAVATION, skillXp);
                     break;
                  case "ARCHERY":
                     user.addSkillXp(Skills.ARCHERY, skillXp);
                     break;
                  case "DEFENSE":
                     user.addSkillXp(Skills.DEFENSE, skillXp);
                     break;
                  case "ENDURANCE":
                     user.addSkillXp(Skills.ENDURANCE, skillXp);
                     break;
                  case "AGILITY":
                     user.addSkillXp(Skills.AGILITY, skillXp);
                     break;
                  case "ALCHEMY":
                     user.addSkillXp(Skills.ALCHEMY, skillXp);
                     break;
                  case "ENCHANTING":
                     user.addSkillXp(Skills.ENCHANTING, skillXp);
                     break;
                  case "SORCERY":
                     user.addSkillXp(Skills.SORCERY, skillXp);
                     break;
                  case "HEALING":
                     user.addSkillXp(Skills.HEALING, skillXp);
                     break;
                  case "FORGING":
                     user.addSkillXp(Skills.FORGING, skillXp);
                     break;
                  default:
                     this.plugin.getLogger().warning("Unknown skill type: " + skillConfig.skillName());
                     return;
               }
            } catch (Exception var10) {
               this.plugin.getLogger().log(Level.WARNING, "Error giving AuraSkills XP to player " + player.getName(), (Throwable)var10);
            }
         }
      }
   }

   public void reloadConfig() {
      this.loadConfig();
      this.loadEntityMappings();
   }

   public void saveConfig() {
      try {
         this.config.save(this.configFile);
      } catch (IOException var2) {
         this.plugin.getLogger().log(Level.SEVERE, "Could not save auraskills.yml", (Throwable)var2);
      }
   }

   private record SkillConfig(String skillName, double ratio) {
   }
}
