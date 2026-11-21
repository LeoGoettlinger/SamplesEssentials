package com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R1.items;

import com.artillexstudios.axtrade.libs.axapi.items.HashGenerator;
import com.artillexstudios.axtrade.libs.axapi.items.HashedStack;
import com.artillexstudios.axtrade.libs.axapi.items.components.DataComponent;
import com.artillexstudios.axtrade.libs.axapi.nms.v1_21_R1.ItemStackSerializer;
import com.artillexstudios.axtrade.libs.axapi.reflection.FieldAccessor;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WrappedItemStack implements com.artillexstudios.axtrade.libs.axapi.items.WrappedItemStack {
   private static final FieldAccessor handleAccessor = FieldAccessor.builder().withClass(CraftItemStack.class).withField("handle").build();
   public ItemStack itemStack;
   private org.bukkit.inventory.ItemStack bukkitStack;
   private boolean dirty = false;

   public WrappedItemStack(org.bukkit.inventory.ItemStack itemStack) {
      this(
         itemStack.getType().isAir()
            ? ItemStack.l
            : (itemStack instanceof CraftItemStack cr ? handleAccessor.get(cr, ItemStack.class) : CraftItemStack.asNMSCopy(itemStack))
      );
      this.bukkitStack = itemStack;
   }

   public WrappedItemStack(ItemStack itemStack) {
      this.itemStack = itemStack;
      this.bukkitStack = null;
   }

   @Override
   public <T> void set(DataComponent<T> component, T value) {
      if (component != null) {
         this.dirty = true;
         component.apply(this, value);
      }
   }

   @Override
   public int getAmount() {
      return this.itemStack.H();
   }

   @Override
   public void setAmount(int amount) {
      this.itemStack.e(amount);
   }

   @Override
   public org.bukkit.inventory.ItemStack toBukkit() {
      return CraftItemStack.asBukkitCopy(this.itemStack);
   }

   @Override
   public HashedStack toHashedStack(HashGenerator hashGenerator) {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toSNBT() {
      return ItemStackSerializer.INSTANCE.serializeAsSnbt(this.toBukkit());
   }

   @Override
   public byte[] serialize() {
      return ItemStackSerializer.INSTANCE.serializeAsBytes(this.toBukkit());
   }

   @Override
   public void finishEdit() {
      if (this.dirty) {
         ItemMeta meta = CraftItemStack.getItemMeta(this.itemStack);
         if (this.bukkitStack != null) {
            this.bukkitStack.setItemMeta(meta);
         } else {
            CraftItemStack.setItemMeta(this.itemStack, meta);
         }
      }
   }

   public WrappedItemStack copy() {
      this.finishEdit();
      return new WrappedItemStack(this.itemStack.s());
   }

   @Override
   public void editFrom(com.artillexstudios.axtrade.libs.axapi.items.WrappedItemStack stack) {
      this.itemStack = ((WrappedItemStack)stack).itemStack;
      this.bukkitStack = ((WrappedItemStack)stack).bukkitStack;
   }

   @Override
   public void update(boolean force) {
   }

   public org.bukkit.inventory.ItemStack wrapped() {
      return this.toBukkit();
   }

   public ItemStack asMinecraft() {
      return this.itemStack;
   }

   @Override
   public final boolean equals(Object object) {
      return !(object instanceof WrappedItemStack that)
         ? false
         : Objects.equals(this.itemStack, that.itemStack) && Objects.equals(this.bukkitStack, that.bukkitStack);
   }

   @Override
   public int hashCode() {
      int result = Objects.hashCode(this.itemStack);
      return 31 * result + Objects.hashCode(this.bukkitStack);
   }
}
