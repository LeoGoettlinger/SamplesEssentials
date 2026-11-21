package github.nighter.smartspawner.api.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.Material;

public class GuiButtonData {
   private final String buttonType;
   private final int slot;
   private final Material material;
   private final boolean enabled;
   private final String condition;
   private final Map<String, String> actions;
   private final boolean infoButton;
   private final String customTexture;
   private final long cooldownTicks;
   private final GuiButtonSoundData clickSound;
   private final GuiButtonSoundData successSound;
   private final GuiButtonSoundData failSound;
   private final Map<String, List<GuiButtonSoundData>> clickSounds;
   private final Map<String, List<GuiButtonSoundData>> successSounds;
   private final Map<String, List<GuiButtonSoundData>> failSounds;

   public GuiButtonData(
      String buttonType, int slot, Material material, boolean enabled, String condition, Map<String, String> actions, boolean infoButton, String customTexture
   ) {
      this(buttonType, slot, material, enabled, condition, actions, infoButton, customTexture, 0L, (GuiButtonSoundData)null, null, null);
   }

   public GuiButtonData(
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
      this(buttonType, slot, material, enabled, condition, actions, infoButton, customTexture, cooldownTicks, clickSound, null, null);
   }

   public GuiButtonData(
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
      if (cooldownTicks < 0L) {
         throw new IllegalArgumentException("Button cooldown cannot be negative");
      } else {
         this.buttonType = buttonType;
         this.slot = slot;
         this.material = material;
         this.enabled = enabled;
         this.condition = condition;
         this.actions = actions != null ? Collections.unmodifiableMap(new HashMap<>(actions)) : Collections.emptyMap();
         this.infoButton = infoButton;
         this.customTexture = customTexture;
         this.cooldownTicks = cooldownTicks;
         this.clickSound = clickSound;
         this.successSound = successSound;
         this.failSound = failSound;
         this.clickSounds = immutableSounds(clickSound);
         this.successSounds = immutableSounds(successSound);
         this.failSounds = immutableSounds(failSound);
      }
   }

   public GuiButtonData(
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
      if (cooldownTicks < 0L) {
         throw new IllegalArgumentException("Button cooldown cannot be negative");
      } else {
         this.buttonType = buttonType;
         this.slot = slot;
         this.material = material;
         this.enabled = enabled;
         this.condition = condition;
         this.actions = actions != null ? Collections.unmodifiableMap(new HashMap<>(actions)) : Collections.emptyMap();
         this.infoButton = infoButton;
         this.customTexture = customTexture;
         this.cooldownTicks = cooldownTicks;
         this.clickSounds = immutableSounds(clickSounds);
         this.successSounds = immutableSounds(successSounds);
         this.failSounds = immutableSounds(failSounds);
         this.clickSound = firstSound(this.clickSounds);
         this.successSound = firstSound(this.successSounds);
         this.failSound = firstSound(this.failSounds);
      }
   }

   public String getAction(String clickType) {
      return this.actions.get(clickType);
   }

   public String getDefaultAction() {
      String clickAction = this.actions.get("click");
      return clickAction != null && !clickAction.isEmpty() ? clickAction : this.actions.get("default");
   }

   public boolean hasCondition() {
      return this.condition != null && !this.condition.isEmpty();
   }

   public boolean hasCooldown() {
      return this.cooldownTicks > 0L;
   }

   public boolean hasClickSound() {
      return !this.clickSounds.isEmpty();
   }

   public boolean hasSuccessSound() {
      return !this.successSounds.isEmpty();
   }

   public boolean hasFailSound() {
      return !this.failSounds.isEmpty();
   }

   private static Map<String, List<GuiButtonSoundData>> immutableSounds(GuiButtonSoundData sound) {
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

   private static GuiButtonSoundData firstSound(Map<String, List<GuiButtonSoundData>> sounds) {
      List<GuiButtonSoundData> values = sounds.get("click");
      return values != null && !values.isEmpty() ? values.getFirst() : null;
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
   public GuiButtonSoundData getClickSound() {
      return this.clickSound;
   }

   @Generated
   public GuiButtonSoundData getSuccessSound() {
      return this.successSound;
   }

   @Generated
   public GuiButtonSoundData getFailSound() {
      return this.failSound;
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
