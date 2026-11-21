package dev.cwhead.GravesX.util;

import java.util.Collections;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

public class CustomModelDataUtil {
   public static void applyCustomModelData(ItemMeta meta, int customModelData) {
      if (customModelData > -1 && meta != null) {
         try {
            CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
            cmdComponent.setFloats(Collections.singletonList((float)customModelData));
            meta.setCustomModelDataComponent(cmdComponent);
         } catch (Throwable var5) {
            try {
               meta.setCustomModelData(customModelData);
            } catch (Throwable var4) {
            }
         }
      }
   }
}
