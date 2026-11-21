package github.nighter.smartspawner.api.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

public class GuiButtonBuilder {
   private String buttonType;
   private int slot = 0;
   private Material material = Material.STONE;
   private boolean enabled = true;
   private String condition = null;
   private final Map<String, String> actions = new HashMap<>();
   private boolean infoButton = false;
   private String customTexture = null;
   private long cooldownTicks = 0L;
   private final Map<String, List<GuiButtonSoundData>> clickSounds = new HashMap<>();
   private final Map<String, List<GuiButtonSoundData>> successSounds = new HashMap<>();
   private final Map<String, List<GuiButtonSoundData>> failSounds = new HashMap<>();

   public GuiButtonBuilder buttonType(String buttonType) {
      this.buttonType = buttonType;
      return this;
   }

   public GuiButtonBuilder slot(int slot) {
      this.slot = slot;
      return this;
   }

   public GuiButtonBuilder material(Material material) {
      this.material = material;
      return this;
   }

   public GuiButtonBuilder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
   }

   public GuiButtonBuilder condition(String condition) {
      this.condition = condition;
      return this;
   }

   public GuiButtonBuilder action(String clickType, String action) {
      this.actions.put(clickType, action);
      return this;
   }

   public GuiButtonBuilder infoButton() {
      this.infoButton = true;
      return this;
   }

   public GuiButtonBuilder infoButton(boolean infoButton) {
      this.infoButton = infoButton;
      return this;
   }

   public GuiButtonBuilder customTexture(String customTexture) {
      this.customTexture = customTexture;
      return this;
   }

   public GuiButtonBuilder cooldownTicks(long cooldownTicks) {
      if (cooldownTicks < 0L) {
         throw new IllegalArgumentException("Button cooldown cannot be negative");
      } else {
         this.cooldownTicks = cooldownTicks;
         return this;
      }
   }

   public GuiButtonBuilder cooldown(String duration) {
      return this.cooldownTicks(GuiDurationParser.parseToTicks(duration));
   }

   public GuiButtonBuilder sound(String soundName) {
      return this.sound(soundName, 1.0F, 1.0F);
   }

   public GuiButtonBuilder sound(String soundName, float volume, float pitch) {
      this.clickSounds.put("click", List.of(new GuiButtonSoundData(soundName, volume, pitch)));
      return this;
   }

   public GuiButtonBuilder noSound() {
      this.clickSounds.remove("click");
      return this;
   }

   public GuiButtonBuilder successSound(String soundName) {
      return this.successSound(soundName, 1.0F, 1.0F);
   }

   public GuiButtonBuilder successSound(String soundName, float volume, float pitch) {
      this.successSounds.put("click", List.of(new GuiButtonSoundData(soundName, volume, pitch)));
      return this;
   }

   public GuiButtonBuilder failSound(String soundName) {
      return this.failSound(soundName, 1.0F, 1.0F);
   }

   public GuiButtonBuilder failSound(String soundName, float volume, float pitch) {
      this.failSounds.put("click", List.of(new GuiButtonSoundData(soundName, volume, pitch)));
      return this;
   }

   public GuiButtonBuilder sound(String clickType, String soundName, float volume, float pitch) {
      return this.addSound(this.clickSounds, clickType, soundName, volume, pitch);
   }

   public GuiButtonBuilder sound(String clickType, String soundName) {
      return this.sound(clickType, soundName, 1.0F, 1.0F);
   }

   public GuiButtonBuilder successSound(String clickType, String soundName, float volume, float pitch) {
      return this.addSound(this.successSounds, clickType, soundName, volume, pitch);
   }

   public GuiButtonBuilder successSound(String clickType, String soundName) {
      return this.successSound(clickType, soundName, 1.0F, 1.0F);
   }

   public GuiButtonBuilder failSound(String clickType, String soundName, float volume, float pitch) {
      return this.addSound(this.failSounds, clickType, soundName, volume, pitch);
   }

   public GuiButtonBuilder failSound(String clickType, String soundName) {
      return this.failSound(clickType, soundName, 1.0F, 1.0F);
   }

   public GuiButtonData build() {
      return new GuiButtonData(
         this.buttonType,
         this.slot,
         this.material,
         this.enabled,
         this.condition,
         new HashMap<>(this.actions),
         this.infoButton,
         this.customTexture,
         this.cooldownTicks,
         this.copySounds(this.clickSounds),
         this.copySounds(this.successSounds),
         this.copySounds(this.failSounds)
      );
   }

   private GuiButtonBuilder addSound(Map<String, List<GuiButtonSoundData>> target, String clickType, String soundName, float volume, float pitch) {
      if (clickType != null && !clickType.isBlank()) {
         target.computeIfAbsent(clickType, ignored -> new ArrayList<>()).add(new GuiButtonSoundData(soundName, volume, pitch));
         return this;
      } else {
         throw new IllegalArgumentException("Click type cannot be blank");
      }
   }

   private Map<String, List<GuiButtonSoundData>> copySounds(Map<String, List<GuiButtonSoundData>> source) {
      Map<String, List<GuiButtonSoundData>> copy = new HashMap<>();
      source.forEach((clickType, sounds) -> copy.put(clickType, List.copyOf(sounds)));
      return copy;
   }
}
