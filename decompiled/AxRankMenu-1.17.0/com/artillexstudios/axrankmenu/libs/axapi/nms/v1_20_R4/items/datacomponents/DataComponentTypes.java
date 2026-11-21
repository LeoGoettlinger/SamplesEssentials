package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents;

import com.artillexstudios.axrankmenu.libs.axapi.collections.RegistrationFailedException;
import com.artillexstudios.axrankmenu.libs.axapi.collections.Registry;
import com.artillexstudios.axrankmenu.libs.axapi.items.components.DataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.AdventureComponentDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.CustomDataDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.CustomModelDataDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.DataComponentHandler;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.DyedColorDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.EnchantmentsDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.IdentityDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.LoreDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.MaterialDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.PotionContentsDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.ProfileDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.RarityDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.UnbreakableDataComponent;
import com.artillexstudios.axrankmenu.libs.axapi.utils.logging.LogUtils;
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
      register(id, com.artillexstudios.axrankmenu.libs.axapi.nms.v1_20_R4.items.datacomponents.impl.DataComponent.create(id, type, mapper));
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
      registerUnsupported("item_model");
      registerUnsupported("tooltip_style");
      register("repair_cost", DataComponents.r, new IdentityDataComponent());
      register("lore", DataComponents.i, new LoreDataComponent());
      register("rarity", DataComponents.j, new RarityDataComponent());
      register("enchantments", DataComponents.k, new EnchantmentsDataComponent());
      register("stored_enchantments", DataComponents.y, new EnchantmentsDataComponent());
      register("custom_model_data", DataComponents.o, new CustomModelDataDataComponent());
      register("enchantment_glint_override", DataComponents.t, new IdentityDataComponent());
      register("profile", DataComponents.V, new ProfileDataComponent());
      register("material", new MaterialDataComponent());
      register("dyed_color", DataComponents.z, new DyedColorDataComponent());
      register("potion_contents", DataComponents.G, new PotionContentsDataComponent());
      register("unbreakable", DataComponents.f, new UnbreakableDataComponent());
      registerUnsupported("minimum_attack_charge");
      registerUnsupported("tooltip_display");
   }
}
