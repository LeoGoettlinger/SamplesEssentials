package github.nighter.smartspawner.api.gui;

import github.nighter.smartspawner.spawner.gui.layout.GuiButton;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

public class GuiLayoutAdapter {
   private static final int MAIN_GUI_SIZE = 27;
   private static final int SELL_CONFIRM_GUI_SIZE = 27;
   private static final int STORAGE_MIN_SLOT = 1;
   private static final int STORAGE_MAX_SLOT = 9;
   private static final int STORAGE_SLOT_OFFSET = 44;

   public static GuiButton toCoreButton(GuiButtonData data, GuiLayoutType layoutType) {
      String buttonType = null;
      if (data != null) {
         buttonType = data.getButtonType();
         if (buttonType == null || buttonType.isBlank()) {
            buttonType = "slot_" + data.getSlot();
         }
      }

      return toCoreButton(data, layoutType, buttonType);
   }

   private static GuiButton toCoreButton(GuiButtonData data, GuiLayoutType layoutType, String buttonType) {
      if (data == null) {
         return null;
      } else if (data.getMaterial() == null) {
         throw new IllegalArgumentException("Button material cannot be null");
      } else {
         return new GuiButton(
            buttonType,
            toCoreSlot(data.getSlot(), layoutType),
            data.getMaterial(),
            data.isEnabled(),
            data.getCondition(),
            new HashMap<>(data.getActions()),
            data.isInfoButton(),
            data.getCustomTexture(),
            data.getCooldownTicks(),
            data.getClickSounds(),
            data.getSuccessSounds(),
            data.getFailSounds()
         );
      }
   }

   public static GuiLayout toCoreLayout(GuiLayoutData data, GuiLayoutType expectedType) {
      if (data == null) {
         return null;
      } else if (data.getType() != expectedType) {
         throw new IllegalArgumentException("Expected layout type " + expectedType + " but received " + data.getType());
      } else {
         GuiLayout layout = new GuiLayout();
         Set<Integer> usedSlots = new HashSet<>();

         for (Entry<String, GuiButtonData> entry : data.getButtons().entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
               throw new IllegalArgumentException("Button key cannot be blank");
            }

            if (entry.getValue() == null) {
               throw new IllegalArgumentException("Button data cannot be null");
            }

            GuiButton button = toCoreButton(entry.getValue(), expectedType, entry.getKey());
            if (!usedSlots.add(button.getSlot())) {
               throw new IllegalArgumentException("Multiple buttons cannot use slot " + entry.getValue().getSlot());
            }

            layout.addButton(entry.getKey(), button);
         }

         return layout;
      }
   }

   private static int toCoreSlot(int apiSlot, GuiLayoutType layoutType) {
      return switch (layoutType) {
         case MAIN_GUI -> validateAndConvert(apiSlot, 1, 27, -1);
         case SELL_CONFIRM_GUI -> validateAndConvert(apiSlot, 1, 27, -1);
         case STORAGE_GUI -> validateAndConvert(apiSlot, 1, 9, 44);
      };
   }

   private static int validateAndConvert(int slot, int min, int max, int offset) {
      if (slot >= min && slot <= max) {
         return offset + slot;
      } else {
         throw new IllegalArgumentException("Slot " + slot + " is outside the allowed range " + min + "-" + max);
      }
   }
}
