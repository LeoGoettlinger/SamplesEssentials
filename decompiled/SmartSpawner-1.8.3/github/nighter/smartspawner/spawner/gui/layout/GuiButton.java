package github.nighter.smartspawner.spawner.gui.layout;

import github.nighter.smartspawner.api.gui.GuiButtonSoundData;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Material;

public class GuiButton {
   private final String buttonType;
   private final int slot;
   private final Material material;
   private final boolean enabled;
   private final String condition;
   private final Map<String, String> actions;
   private final boolean infoButton;
   private final String customTexture;
   private final long cooldownTicks;
   private final Map<String, List<GuiButtonSoundData>> clickSounds;
   private final Map<String, List<GuiButtonSoundData>> successSounds;
   private final Map<String, List<GuiButtonSoundData>> failSounds;

   public GuiButton(
      String buttonType, int slot, Material material, boolean enabled, String condition, Map<String, String> actions, boolean infoButton, String customTexture
   ) {
      this(buttonType, slot, material, enabled, condition, actions, infoButton, customTexture, 0L, Map.of(), Map.of(), Map.of());
   }

   public GuiButton(
      String buttonType,
      int slot,
      Material material,
      boolean enabled,
      String condition,
      Map<String, String> actions,
      boolean infoButton,
      String customTexture,
      long cooldownTicks,
      GuiButtonSoundData clickSound
   ) {
      this(buttonType, slot, material, enabled, condition, actions, infoButton, customTexture, cooldownTicks, singletonSoundMap(clickSound), Map.of(), Map.of());
   }

   public GuiButton(
      String buttonType,
      int slot,
      Material material,
      boolean enabled,
      String condition,
      Map<String, String> actions,
      boolean infoButton,
      String customTexture,
      long cooldownTicks,
      GuiButtonSoundData clickSound,
      GuiButtonSoundData successSound,
      GuiButtonSoundData failSound
   ) {
      this(
         buttonType,
         slot,
         material,
         enabled,
         condition,
         actions,
         infoButton,
         customTexture,
         cooldownTicks,
         singletonSoundMap(clickSound),
         singletonSoundMap(successSound),
         singletonSoundMap(failSound)
      );
   }

   public GuiButton(
      String buttonType,
      int slot,
      Material material,
      boolean enabled,
      String condition,
      Map<String, String> actions,
      boolean infoButton,
      String customTexture,
      long cooldownTicks,
      Map<String, List<GuiButtonSoundData>> clickSounds,
      Map<String, List<GuiButtonSoundData>> successSounds,
      Map<String, List<GuiButtonSoundData>> failSounds
   ) {
      this.buttonType = buttonType;
      this.slot = slot;
      this.material = material;
      this.enabled = enabled;
      this.condition = condition;
      this.actions = actions;
      this.infoButton = infoButton;
      this.customTexture = customTexture;
      this.cooldownTicks = Math.max(0L, cooldownTicks);
      this.clickSounds = immutableSounds(clickSounds);
      this.successSounds = immutableSounds(successSounds);
      this.failSounds = immutableSounds(failSounds);
   }

   public String getAction(String clickType) {
      return this.actions != null ? this.actions.get(clickType) : null;
   }

   public String getActionWithFallback(String clickType) {
      if (this.actions != null && !this.actions.isEmpty()) {
         String specificAction = this.actions.get(clickType);
         return specificAction != null && !specificAction.isEmpty() ? specificAction : this.actions.get("click");
      } else {
         return null;
      }
   }

   public String getDefaultAction() {
      if (this.actions != null && !this.actions.isEmpty()) {
         String clickAction = this.actions.get("click");
         return clickAction != null && !clickAction.isEmpty() ? clickAction : this.actions.get("default");
      } else {
         return null;
      }
   }

   public List<GuiButtonSoundData> getClickSounds(String clickType) {
      return this.getSoundsWithFallback(this.clickSounds, clickType);
   }

   public List<GuiButtonSoundData> getSuccessSounds(String clickType) {
      return this.getSoundsWithFallback(this.successSounds, clickType);
   }

   public List<GuiButtonSoundData> getFailSounds(String clickType) {
      return this.getSoundsWithFallback(this.failSounds, clickType);
   }

   public GuiButtonSoundData getClickSound() {
      return firstSound(this.getClickSounds("click"));
   }

   public GuiButtonSoundData getSuccessSound() {
      return firstSound(this.getSuccessSounds("click"));
   }

   public GuiButtonSoundData getFailSound() {
      return firstSound(this.getFailSounds("click"));
   }

   public boolean hasCondition() {
      return this.condition != null && !this.condition.isEmpty();
   }

   private List<GuiButtonSoundData> getSoundsWithFallback(Map<String, List<GuiButtonSoundData>> sounds, String clickType) {
      List<GuiButtonSoundData> configured = sounds.get(clickType);
      if (configured != null) {
         return configured;
      } else {
         configured = sounds.get("click");
         return configured != null ? configured : List.of();
      }
   }

   private static GuiButtonSoundData firstSound(List<GuiButtonSoundData> sounds) {
      return sounds.isEmpty() ? null : sounds.getFirst();
   }

   private static Map<String, List<GuiButtonSoundData>> singletonSoundMap(GuiButtonSoundData sound) {
      return sound == null ? Map.of() : Map.of("click", List.of(sound));
   }

   private static Map<String, List<GuiButtonSoundData>> immutableSounds(Map<String, List<GuiButtonSoundData>> sounds) {
      if (sounds != null && !sounds.isEmpty()) {
         Map<String, List<GuiButtonSoundData>> copy = new HashMap<>();
         sounds.forEach((clickType, values) -> {
            if (clickType != null && values != null) {
               copy.put(clickType, List.copyOf(values));
            }
         });
         return Collections.unmodifiableMap(copy);
      } else {
         return Map.of();
      }
   }

   @Generated
   public String getButtonType() {
      return this.buttonType;
   }

   @Generated
   public int getSlot() {
      return this.slot;
   }

   @Generated
   public Material getMaterial() {
      return this.material;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public String getCondition() {
      return this.condition;
   }

   @Generated
   public Map<String, String> getActions() {
      return this.actions;
   }

   @Generated
   public boolean isInfoButton() {
      return this.infoButton;
   }

   @Generated
   public String getCustomTexture() {
      return this.customTexture;
   }

   @Generated
   public long getCooldownTicks() {
      return this.cooldownTicks;
   }

   @Generated
   public Map<String, List<GuiButtonSoundData>> getClickSounds() {
      return this.clickSounds;
   }

   @Generated
   public Map<String, List<GuiButtonSoundData>> getSuccessSounds() {
      return this.successSounds;
   }

   @Generated
   public Map<String, List<GuiButtonSoundData>> getFailSounds() {
      return this.failSounds;
   }
}
