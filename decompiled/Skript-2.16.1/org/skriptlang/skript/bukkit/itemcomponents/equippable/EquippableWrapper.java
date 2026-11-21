package org.skriptlang.skript.bukkit.itemcomponents.equippable;

import ch.njol.skript.Skript;
import ch.njol.skript.util.ItemSource;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.Equippable.Builder;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import java.lang.reflect.Method;
import java.util.Collection;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentUtils;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;

public class EquippableWrapper extends ComponentWrapper<Equippable, Builder> {
   private static final boolean HAS_MODEL_METHOD = Skript.methodExists(Equippable.class, "model");
   @Nullable
   private static final Method COMPONENT_MODEL_METHOD;
   @Nullable
   private static final Method BUILDER_MODEL_METHOD;
   public static final boolean HAS_EQUIP_ON_INTERACT = Skript.methodExists(Equippable.class, "equipOnInteract");
   public static final boolean HAS_CAN_BE_SHEARED = Skript.methodExists(Equippable.class, "canBeSheared");
   public static final boolean HAS_SHEAR_SOUND = Skript.methodExists(Equippable.class, "shearSound");

   public EquippableWrapper(ItemStack itemStack) {
      super(itemStack);
   }

   public EquippableWrapper(ItemSource<?> itemSource) {
      super(itemSource);
   }

   public EquippableWrapper(Equippable component) {
      super(component);
   }

   public EquippableWrapper(Builder builder) {
      super(builder);
   }

   @Override
   public Valued<Equippable> getDataComponentType() {
      return DataComponentTypes.EQUIPPABLE;
   }

   protected Equippable getComponent(ItemStack itemStack) {
      Equippable equippable = (Equippable)itemStack.getData(DataComponentTypes.EQUIPPABLE);
      return equippable != null ? equippable : (Equippable)Equippable.equippable(EquipmentSlot.HEAD).build();
   }

   protected Builder getBuilder(ItemStack itemStack) {
      Equippable equippable = (Equippable)itemStack.getData(DataComponentTypes.EQUIPPABLE);
      return equippable != null ? (Builder)equippable.toBuilder() : Equippable.equippable(EquipmentSlot.HEAD);
   }

   protected void setComponent(ItemStack itemStack, Equippable component) {
      itemStack.setData(DataComponentTypes.EQUIPPABLE, component);
   }

   protected Builder getBuilder(Equippable component) {
      return (Builder)component.toBuilder();
   }

   public EquippableWrapper clone() {
      EquippableWrapper clone = this.newWrapper();
      Equippable base = this.getComponent();
      clone.applyComponent(this.clone(base.slot()));
      return clone;
   }

   public Equippable clone(EquipmentSlot slot) {
      Equippable base = this.getComponent();
      Builder builder = Equippable.equippable(slot)
         .allowedEntities(base.allowedEntities())
         .cameraOverlay(base.cameraOverlay())
         .damageOnHurt(base.damageOnHurt())
         .dispensable(base.dispensable())
         .equipSound(base.equipSound())
         .swappable(base.swappable());
      setAssetId(builder, this.getAssetId());
      if (HAS_EQUIP_ON_INTERACT) {
         builder.equipOnInteract(base.equipOnInteract());
      }

      if (HAS_CAN_BE_SHEARED) {
         builder.canBeSheared(base.canBeSheared());
      }

      if (HAS_SHEAR_SOUND) {
         builder.shearSound(base.shearSound());
      }

      return (Equippable)builder.build();
   }

   public Equippable newComponent() {
      return (Equippable)this.newBuilder().build();
   }

   public Builder newBuilder() {
      return Equippable.equippable(EquipmentSlot.HEAD);
   }

   public EquippableWrapper newWrapper() {
      return newInstance();
   }

   public Collection<EntityType> getAllowedEntities() {
      return getAllowedEntities(this.getComponent());
   }

   public void setAssetId(Key key) {
      Builder builder = this.getBuilder();
      setAssetId(builder, key);
      this.applyBuilder(builder);
   }

   public Key getAssetId() {
      return getAssetId(this.getComponent());
   }

   public static EquippableWrapper newInstance() {
      return new EquippableWrapper(Equippable.equippable(EquipmentSlot.HEAD));
   }

   public static Key getAssetId(Equippable component) {
      if (!HAS_MODEL_METHOD) {
         return component.assetId();
      } else {
         assert COMPONENT_MODEL_METHOD != null;

         try {
            return (Key)COMPONENT_MODEL_METHOD.invoke(component);
         } catch (Exception var2) {
            return component.assetId();
         }
      }
   }

   public static Builder setAssetId(Builder builder, Key key) {
      if (HAS_MODEL_METHOD) {
         assert BUILDER_MODEL_METHOD != null;

         try {
            BUILDER_MODEL_METHOD.invoke(builder, key);
         } catch (Exception var3) {
         }
      } else {
         builder.assetId(key);
      }

      return builder;
   }

   public static Collection<EntityType> getAllowedEntities(Equippable component) {
      return ComponentUtils.registryKeySetToCollection(component.allowedEntities(), Registry.ENTITY_TYPE);
   }

   public static RegistryKeySet<EntityType> convertAllowedEntities(Collection<EntityType> entityTypes) {
      return ComponentUtils.collectionToRegistryKeySet(entityTypes, RegistryKey.ENTITY_TYPE);
   }

   static {
      Method componentModelMethod = null;
      Method builderModelMethod = null;
      if (HAS_MODEL_METHOD) {
         try {
            componentModelMethod = Equippable.class.getDeclaredMethod("model");
            builderModelMethod = Builder.class.getDeclaredMethod("model", Key.class);
         } catch (NoSuchMethodException var3) {
         }
      }

      COMPONENT_MODEL_METHOD = componentModelMethod;
      BUILDER_MODEL_METHOD = builderModelMethod;
   }
}
