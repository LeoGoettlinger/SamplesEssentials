package com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents;

import com.artillexstudios.axtrade.libs.axapi.collections.RegistrationFailedException;
import com.artillexstudios.axtrade.libs.axapi.collections.Registry;
import com.artillexstudios.axtrade.libs.axapi.items.components.DataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.CustomDataDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.CustomModelDataDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.DataComponentHandler;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.DirectDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.DyedColorDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.EnchantmentGlintOverrideDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.EnchantmentsDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.LoreDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.MaterialDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.NameDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.PotionContentsDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.ProfileDataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.UnbreakableDataComponent;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

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

   public static <T, Z> void register(String id, DataComponentHandler<T> mapper) {
      register(id, com.artillexstudios.axtrade.libs.axapi.nms.v1_20_R2.items.datacomponents.impl.DataComponent.create(id, mapper));
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
      register("custom_data", new CustomDataDataComponent());
      registerUnsupported("max_stack_size");
      registerUnsupported("max_damage");
      register("damage", new DirectDataComponent(ItemStack::k, ItemStack::b, item -> item.b(0)));
      register("custom_name", new NameDataComponent());
      register("item_name", new NameDataComponent());
      registerUnsupported("item_model");
      registerUnsupported("tooltip_style");
      register("repair_cost", new DirectDataComponent(ItemStack::I, ItemStack::d, item -> item.d(0)));
      register("lore", new LoreDataComponent());
      registerUnsupported("rarity");
      register("enchantments", new EnchantmentsDataComponent("Enchantments"));
      register("stored_enchantments", new EnchantmentsDataComponent("StoredEnchantments"));
      register("custom_model_data", new CustomModelDataDataComponent());
      register("enchantment_glint_override", new EnchantmentGlintOverrideDataComponent());
      register("profile", new ProfileDataComponent());
      register("material", new MaterialDataComponent());
      register("dyed_color", new DyedColorDataComponent());
      register("potion_contents", new PotionContentsDataComponent());
      register("unbreakable", new UnbreakableDataComponent());
      registerUnsupported("minimum_attack_charge");
      registerUnsupported("tooltip_display");
   }
}
