package github.nighter.smartspawner.api.gui;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.gui.layout.GuiButton;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ExternalGuiLayoutLoader {
   private static final int MIN_SLOT = 1;
   private static final int MAX_SLOT = 9;
   private static final int SLOT_OFFSET = 44;
   private static final int MAIN_GUI_SIZE = 27;
   private static final int SELL_CONFIRM_GUI_SIZE = 27;
   private static final String[] CLICK_TYPES = new String[]{"click", "left_click", "right_click", "shift_left_click", "shift_right_click"};
   private final SmartSpawner plugin;
   private final Logger logger;

   public ExternalGuiLayoutLoader(SmartSpawner plugin) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
   }

   public GuiLayout loadLayout(File file, String layoutType) {
      try {
         FileConfiguration config = YamlConfiguration.loadConfiguration(file);
         return this.loadLayout(config, layoutType);
      } catch (Exception var4) {
         if (this.logger != null) {
            this.logger.log(Level.WARNING, "Failed to load " + layoutType + " layout from " + file.getName() + ": " + var4.getMessage(), (Throwable)var4);
         }

         return null;
      }
   }

   public GuiLayout loadLayout(FileConfiguration config, String layoutType) {
      GuiLayout layout = new GuiLayout();
      Set<String> buttonKeys = config.getKeys(false);
      if (buttonKeys.isEmpty()) {
         if (this.logger != null) {
            this.logger.warning("No buttons found in GUI layout");
         }

         return layout;
      } else {
         for (String buttonKey : buttonKeys) {
            if (buttonKey.startsWith("slot_") && !this.loadButton(config, layout, buttonKey, layoutType) && this.logger != null) {
               this.logger.warning("Failed to load button: " + buttonKey);
            }
         }

         return layout;
      }
   }

   private boolean loadButton(FileConfiguration config, GuiLayout layout, String buttonKey, String layoutType) {
      int slot = this.parseSlotFromKey(buttonKey);
      if (slot == -1) {
         if (this.logger != null) {
            this.logger.warning("Invalid button key format: " + buttonKey);
         }

         return false;
      } else if (!config.getBoolean(buttonKey + ".enabled", true)) {
         return false;
      } else {
         String materialName = config.getString(buttonKey + ".material", "STONE");
         String condition = config.getString(buttonKey + ".condition", null);
         boolean infoButton = config.getBoolean(buttonKey + ".info_button", false);
         String customTexture = config.getString(buttonKey + ".custom_texture", null);
         long cooldownTicks = this.parseCooldown(config.getString(buttonKey + ".cooldown"), buttonKey);
         Map<String, List<GuiButtonSoundData>> clickSounds = new HashMap<>();
         Map<String, List<GuiButtonSoundData>> successSounds = new HashMap<>();
         Map<String, List<GuiButtonSoundData>> failSounds = new HashMap<>();
         this.parseLegacyButtonSounds(config, buttonKey, clickSounds, successSounds, failSounds);
         if (!this.isValidSlot(slot, layoutType)) {
            if (this.logger != null) {
               this.logger.warning(String.format("Invalid slot %d for button %s in %s layout", slot, buttonKey, layoutType));
            }

            return false;
         } else if (condition != null && !this.evaluateCondition(condition)) {
            return false;
         } else {
            Material material = this.parseMaterial(materialName, buttonKey);
            int actualSlot = this.calculateActualSlot(slot, layoutType);
            Map<String, String> actions = new HashMap<>();
            ConfigurationSection actionSection = config.getConfigurationSection(buttonKey);
            ConfigurationSection ifSection = config.getConfigurationSection(buttonKey + ".if");
            if (ifSection != null) {
               for (String conditionKey : ifSection.getKeys(false)) {
                  if (this.evaluateCondition(conditionKey)) {
                     ConfigurationSection conditionActions = ifSection.getConfigurationSection(conditionKey);
                     if (conditionActions != null) {
                        String conditionalMaterial = conditionActions.getString("material");
                        if (conditionalMaterial != null) {
                           material = this.parseMaterial(conditionalMaterial, buttonKey);
                        }

                        String conditionalCustomTexture = conditionActions.getString("custom_texture");
                        if (conditionalCustomTexture != null) {
                           customTexture = conditionalCustomTexture;
                        }

                        if (conditionActions.contains("cooldown")) {
                           cooldownTicks = this.parseCooldown(conditionActions.getString("cooldown"), buttonKey + ".if." + conditionKey);
                        }

                        this.parseLegacyButtonSounds(conditionActions, "", clickSounds, successSounds, failSounds);
                        this.parseClickActions(conditionActions, actions, clickSounds, successSounds, failSounds, buttonKey + ".if." + conditionKey);
                     }
                     break;
                  }
               }
            } else if (actionSection != null) {
               this.parseClickActions(actionSection, actions, clickSounds, successSounds, failSounds, buttonKey);
            }

            GuiButton button = new GuiButton(
               buttonKey, actualSlot, material, true, condition, actions, infoButton, customTexture, cooldownTicks, clickSounds, successSounds, failSounds
            );
            layout.addButton(buttonKey, button);
            return true;
         }
      }
   }

   private long parseCooldown(String value, String buttonKey) {
      if (value != null && !value.isBlank()) {
         try {
            return GuiDurationParser.parseToTicks(value);
         } catch (ArithmeticException | IllegalArgumentException var4) {
            if (this.logger != null) {
               this.logger.warning("Invalid cooldown '" + value + "' for button " + buttonKey + ". Cooldown disabled.");
            }

            return 0L;
         }
      } else {
         return 0L;
      }
   }

   private void parseClickActions(
      ConfigurationSection section,
      Map<String, String> actions,
      Map<String, List<GuiButtonSoundData>> clickSounds,
      Map<String, List<GuiButtonSoundData>> successSounds,
      Map<String, List<GuiButtonSoundData>> failSounds,
      String context
   ) {
      for (String clickType : CLICK_TYPES) {
         if (section.contains(clickType)) {
            ConfigurationSection clickSection = section.getConfigurationSection(clickType);
            if (clickSection == null) {
               String action = section.getString(clickType);
               if (action != null && !action.isBlank()) {
                  actions.put(clickType, action);
               }
            } else {
               String action = clickSection.getString("action");
               if (action != null && !action.isBlank()) {
                  actions.put(clickType, action);
               } else if (this.logger != null) {
                  this.logger.warning("Missing action for " + context + "." + clickType);
               }

               this.putSounds(clickSection, "sound", clickType, clickSounds, context + "." + clickType + ".sound");
               this.putSounds(clickSection, "sound_success", clickType, successSounds, context + "." + clickType + ".sound_success");
               this.putSounds(clickSection, "sound_fail", clickType, failSounds, context + "." + clickType + ".sound_fail");
            }
         }
      }
   }

   private void parseLegacyButtonSounds(
      ConfigurationSection section,
      String basePath,
      Map<String, List<GuiButtonSoundData>> clickSounds,
      Map<String, List<GuiButtonSoundData>> successSounds,
      Map<String, List<GuiButtonSoundData>> failSounds
   ) {
      String soundPath = this.path(basePath, "sound");
      if (section.contains(soundPath)) {
         ConfigurationSection soundSection = section.getConfigurationSection(soundPath);
         if (soundSection != null && (soundSection.contains("success") || soundSection.contains("fail"))) {
            if (soundSection.contains("name")) {
               clickSounds.put("click", this.parseSounds(soundSection, "", soundPath));
            }

            this.putSounds(soundSection, "success", "click", successSounds, soundPath + ".success");
            this.putSounds(soundSection, "fail", "click", failSounds, soundPath + ".fail");
         } else {
            clickSounds.put("click", this.parseSounds(section, soundPath, soundPath));
         }
      }
   }

   private void putSounds(ConfigurationSection section, String path, String clickType, Map<String, List<GuiButtonSoundData>> target, String context) {
      if (section.contains(path)) {
         target.put(clickType, this.parseSounds(section, path, context));
      }
   }

   private List<GuiButtonSoundData> parseSounds(ConfigurationSection section, String soundPath, String context) {
      Object raw = soundPath.isEmpty() ? section : section.get(soundPath);
      List<GuiButtonSoundData> sounds = new ArrayList<>();
      if (raw instanceof String soundName) {
         this.addSound(sounds, soundName, 1.0F, 1.0F, context);
      } else if (raw instanceof List) {
         for (Object value : (List)raw) {
            this.parseSoundValue(sounds, value, context);
         }
      } else if (raw instanceof ConfigurationSection soundSection) {
         this.addSound(
            sounds, soundSection.getString("name"), (float)soundSection.getDouble("volume", 1.0), (float)soundSection.getDouble("pitch", 1.0), context
         );
      } else if (raw instanceof Map<?, ?> soundMap) {
         this.addSound(
            sounds,
            this.stringValue(soundMap.get("name")),
            this.floatValue(soundMap.get("volume"), 1.0F),
            this.floatValue(soundMap.get("pitch"), 1.0F),
            context
         );
      } else if (raw != null && this.logger != null) {
         this.logger.warning("Invalid sound value at " + context);
      }

      return List.copyOf(sounds);
   }

   private void parseSoundValue(List<GuiButtonSoundData> sounds, Object value, String context) {
      if (value instanceof String soundName) {
         this.addSound(sounds, soundName, 1.0F, 1.0F, context);
      } else if (value instanceof Map<?, ?> soundMap) {
         this.addSound(
            sounds,
            this.stringValue(soundMap.get("name")),
            this.floatValue(soundMap.get("volume"), 1.0F),
            this.floatValue(soundMap.get("pitch"), 1.0F),
            context
         );
      } else if (value instanceof ConfigurationSection soundSection) {
         this.addSound(
            sounds, soundSection.getString("name"), (float)soundSection.getDouble("volume", 1.0), (float)soundSection.getDouble("pitch", 1.0), context
         );
      } else if (this.logger != null) {
         this.logger.warning("Invalid sound entry at " + context);
      }
   }

   private void addSound(List<GuiButtonSoundData> sounds, String name, float volume, float pitch, String context) {
      if (name != null && !name.isBlank() && !"none".equalsIgnoreCase(name)) {
         try {
            sounds.add(new GuiButtonSoundData(name, volume, pitch));
         } catch (IllegalArgumentException var7) {
            if (this.logger != null) {
               this.logger.warning("Invalid sound configuration at " + context + ": " + var7.getMessage());
            }
         }
      }
   }

   private String path(String basePath, String child) {
      return basePath != null && !basePath.isBlank() ? basePath + "." + child : child;
   }

   private String stringValue(Object value) {
      return value != null ? value.toString() : null;
   }

   private float floatValue(Object value, float fallback) {
      return value instanceof Number number ? number.floatValue() : fallback;
   }

   private int parseSlotFromKey(String buttonKey) {
      if (!buttonKey.startsWith("slot_")) {
         return -1;
      } else {
         try {
            String slotPart = buttonKey.substring(5);
            int underscoreIndex = slotPart.indexOf(95);
            if (underscoreIndex > 0) {
               slotPart = slotPart.substring(0, underscoreIndex);
            }

            return Integer.parseInt(slotPart);
         } catch (NumberFormatException var4) {
            return -1;
         }
      }
   }

   private boolean isValidSlot(int slot, String layoutType) {
      return slot >= this.getMinSlot(layoutType) && slot <= this.getMaxSlot(layoutType);
   }

   private int getMinSlot(String layoutType) {
      return "storage".equals(layoutType) ? 1 : 1;
   }

   private int getMaxSlot(String layoutType) {
      if ("storage".equals(layoutType)) {
         return 9;
      } else {
         return "sell_confirm".equals(layoutType) ? 27 : 27;
      }
   }

   private int calculateActualSlot(int slot, String layoutType) {
      return "storage".equals(layoutType) ? 44 + slot : slot - 1;
   }

   private Material parseMaterial(String materialName, String buttonKey) {
      try {
         return Material.valueOf(materialName.toUpperCase());
      } catch (IllegalArgumentException var4) {
         if (this.logger != null) {
            this.logger.warning(String.format("Invalid material %s for button %s. Using STONE.", materialName, buttonKey));
         }

         return Material.STONE;
      }
   }

   private boolean evaluateCondition(String condition) {
      if (this.plugin == null) {
         return true;
      } else {
         switch (condition) {
            case "sell_integration":
               return this.plugin.hasSellIntegration();
            case "no_sell_integration":
               return !this.plugin.hasSellIntegration();
            default:
               if (this.logger != null) {
                  this.logger.warning("Unknown condition: " + condition);
               }

               return true;
         }
      }
   }
}
