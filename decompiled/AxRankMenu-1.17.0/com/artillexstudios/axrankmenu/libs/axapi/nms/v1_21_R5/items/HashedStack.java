package com.artillexstudios.axrankmenu.libs.axapi.nms.v1_21_R5.items;

import com.artillexstudios.axrankmenu.libs.axapi.items.HashGenerator;
import java.util.Objects;

public record HashedStack(net.minecraft.network.HashedStack stack) implements com.artillexstudios.axrankmenu.libs.axapi.items.HashedStack {
   public static HashedStack create(WrappedItemStack stack, HashGenerator generator) {
      return new HashedStack(net.minecraft.network.HashedStack.b(stack.itemStack, generator::apply));
   }

   @Override
   public boolean matches(com.artillexstudios.axrankmenu.libs.axapi.items.WrappedItemStack stack, HashGenerator generator) {
      return this.stack.a(((WrappedItemStack)stack).itemStack, generator::apply);
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof HashedStack(net.minecraft.network.HashedStack var4) ? Objects.equals(this.stack, var4) : false;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.stack);
   }
}
