package org.skriptlang.skript.bukkit.itemcomponents;

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.ItemSource;
import ch.njol.skript.util.slot.Slot;
import io.papermc.paper.datacomponent.DataComponentBuilder;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ComponentWrapper<T, B extends DataComponentBuilder<T>> implements Cloneable {
   @Nullable
   private final ItemSource<?> itemSource;
   private T component;

   public ComponentWrapper(ItemStack itemStack) {
      this(new ItemSource<>(itemStack));
   }

   public ComponentWrapper(ItemSource<?> itemSource) {
      if (itemSource.getItemStack() == null) {
         throw new IllegalArgumentException("ItemSource must have an ItemStack to retrieve");
      } else {
         this.itemSource = itemSource;
         this.component = this.getComponent(itemSource.getItemStack());
      }
   }

   public ComponentWrapper(T component) {
      this.component = component;
      this.itemSource = null;
   }

   public ComponentWrapper(B builder) {
      this.component = (T)builder.build();
      this.itemSource = null;
   }

   public T getComponent() {
      return this.itemSource != null ? this.getComponent(this.itemSource.getItemStack()) : this.component;
   }

   public B getBuilder() {
      return this.itemSource != null ? this.getBuilder(this.itemSource.getItemStack()) : this.getBuilder(this.component);
   }

   @Nullable
   public ItemStack getItemStack() {
      return this.itemSource == null ? null : this.itemSource.getItemStack();
   }

   @Nullable
   public ItemSource<?> getItemSource() {
      return this.itemSource;
   }

   public abstract Valued<T> getDataComponentType();

   protected abstract T getComponent(ItemStack var1);

   protected abstract B getBuilder(ItemStack var1);

   protected abstract void setComponent(ItemStack var1, T var2);

   protected void setBuilder(ItemStack itemStack, B builder) {
      this.setComponent(itemStack, (T)builder.build());
   }

   public void applyComponent() {
      this.applyComponent(this.getComponent());
   }

   public void applyComponent(@NotNull T component) {
      this.component = component;
      if (this.itemSource != null) {
         ItemStack itemStack = this.itemSource.getItemStack();
         this.setComponent(itemStack, component);
         Object var5 = this.itemSource.getSource();
         if (var5 instanceof ItemType) {
            for (ItemData itemData : (ItemType)var5) {
               ItemStack dataStack = itemData.getStack();
               if (dataStack != null) {
                  dataStack.setData(this.getDataComponentType(), component);
               }
            }
         } else if (this.itemSource.getSource() instanceof Slot slot) {
            slot.setItem(itemStack);
         }
      }
   }

   public void applyBuilder(@NotNull B builder) {
      this.applyComponent((T)builder.build());
   }

   public void editBuilder(Consumer<B> consumer) {
      B builder = this.getBuilder();
      consumer.accept(builder);
      this.applyComponent((T)builder.build());
   }

   protected abstract B getBuilder(T var1);

   public abstract ComponentWrapper<T, B> clone();

   public abstract T newComponent();

   public abstract B newBuilder();

   public abstract ComponentWrapper<T, B> newWrapper();

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ComponentWrapper<?, ?> other) {
         boolean relation = true;
         if (this.itemSource != null && other.itemSource != null) {
            relation = this.itemSource.getItemStack().equals(other.itemSource.getItemStack());
         }

         return relation & this.getComponent().equals(other.getComponent());
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return this.getComponent().toString();
   }
}
