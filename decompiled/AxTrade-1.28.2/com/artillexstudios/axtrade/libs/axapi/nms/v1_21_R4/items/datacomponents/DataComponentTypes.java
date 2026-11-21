package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents;

import com.artillexstudios.axtrade.libs.axapi.collections.RegistrationFailedException;
import com.artillexstudios.axtrade.libs.axapi.collections.Registry;
import com.artillexstudios.axtrade.libs.axapi.items.components.DataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.AdventureComponentDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.CustomDataDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.CustomModelDataDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.DataComponentHandler;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.DyedColorDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.EnchantmentsDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.IdentifierDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.IdentityDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.LoreDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.MaterialDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.PotionContentsDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.ProfileDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.RarityDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.TooltipDisplayDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.UnitDataComponent;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;

public final class DataComponentTypes {
   private static final Registry<String, DataComponent<?>> components = new Registry<>();
   private static final List<String> unsupportedComponents = new ArrayList<>();

   public static <T extends DataComponent<?>> void register(String id, T mapper) {
      try {
         components.register(id, mapper);
      } catch (RegistrationFailedException var3) {
         LogUtils.error("An exception occurred while registering component {}!", id, var3);
      }
   }

   public static <T, Z> void register(String id, DataComponentType<Z> type, DataComponentHandler<T, Z> mapper) {
      register(id, com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R4.items.datacomponents.impl.DataComponent.create(id, type, mapper));
   }

   public static void registerUnsupported(String id) {
      unsupportedComponents.add(id);
   }

   public static DataComponent<?> component(String id) {
      try {
         return components.get(id);
      } catch (RegistrationFailedException var2) {
         if (unsupportedComponents.contains(id)) {
            return null;
         } else {
            LogUtils.error("Failed to find component {}! This is an issue with the code, and it should be reported to the developer of the plugin!", id, var2);
            return null;
         }
      }
   }

   static {
      register("custom_data", DataComponents.b, new CustomDataDataComponent());
      register("max_stack_size", DataComponents.c, new IdentityDataComponent());
      register("max_damage", DataComponents.d, new IdentityDataComponent());
      register("damage", DataComponents.e, new IdentityDataComponent());
      register("custom_name", DataComponents.g, new AdventureComponentDataComponent());
      register("item_name", DataComponents.h, new AdventureComponentDataComponent());
      register("item_model", DataComponents.i, new IdentifierDataComponent());
      register("tooltip_style", DataComponents.G, new IdentifierDataComponent());
      register("repair_cost", DataComponents.r, new IdentityDataComponent());
      register("lore", DataComponents.j, new LoreDataComponent());
      register("rarity", DataComponents.k, new RarityDataComponent());
      register("enchantments", DataComponents.l, new EnchantmentsDataComponent());
      register("stored_enchantments", DataComponents.J, new EnchantmentsDataComponent());
      register("custom_model_data", DataComponents.p, new CustomModelDataDataComponent());
      register("enchantment_glint_override", DataComponents.t, new IdentityDataComponent());
      register("profile", DataComponents.ak, new ProfileDataComponent());
      register("material", new MaterialDataComponent());
      register("dyed_color", DataComponents.K, new DyedColorDataComponent());
      register("potion_contents", DataComponents.R, new PotionContentsDataComponent());
      register("unbreakable", DataComponents.f, new UnitDataComponent());
      registerUnsupported("minimum_attack_charge");
      register("tooltip_display", DataComponents.q, new TooltipDisplayDataComponent());
   }
}
