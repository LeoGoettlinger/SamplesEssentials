package com.github.retrooper.packetevents.protocol.item;

import com.github.retrooper.packetevents.protocol.component.ComponentType;
import com.github.retrooper.packetevents.protocol.component.HashedComponentPatchMap;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import org.jetbrains.annotations.Nullable;

public final class HashedStack {
   private final ItemType item;
   private final int count;
   private final HashedComponentPatchMap components;

   public HashedStack(ItemType item, int count, HashedComponentPatchMap components) {
      this.item = item;
      this.count = count;
      this.components = components;
   }

   public static Optional<HashedStack> readOptional(PacketWrapper<?> wrapper) {
      return Optional.ofNullable(read(wrapper));
   }

   public static Optional<HashedStack> toOptionalFromItemStack(ItemStack itemStack) {
      return Optional.ofNullable(fromItemStack(itemStack));
   }

   @Nullable
   public static HashedStack read(PacketWrapper<?> wrapper) {
      if (!wrapper.readBoolean()) {
         return null;
      } else {
         ItemType item = wrapper.readMappedEntity(ItemTypes.getRegistry());
         int count = wrapper.readVarInt();
         HashedComponentPatchMap components = HashedComponentPatchMap.read(wrapper);
         return new HashedStack(item, count, components);
      }
   }

   public static void writeOptional(PacketWrapper<?> wrapper, Optional<HashedStack> stack) {
      write(wrapper, stack.orElse(null));
   }

   public static void write(PacketWrapper<?> wrapper, HashedStack stack) {
      if (stack == null) {
         wrapper.writeBoolean(false);
      } else {
         wrapper.writeBoolean(true);
         wrapper.writeMappedEntity(stack.item);
         wrapper.writeVarInt(stack.count);
         HashedComponentPatchMap.write(wrapper, stack.components);
      }
   }

   public static HashedStack fromItemStack(ItemStack stack) {
      if (stack == null) {
         return null;
      } else {
         Map<ComponentType<?>, Optional<?>> patches = stack.getComponents().getPatches();
         Map<ComponentType<?>, Integer> addedComponents = new HashMap<>(patches.size());
         Set<ComponentType<?>> removedComponents = new HashSet<>(patches.size());

         for (Entry<ComponentType<?>, Optional<?>> patch : patches.entrySet()) {
            if (patch.getValue().isPresent()) {
               addedComponents.put(patch.getKey(), 0);
            } else {
               removedComponents.add(patch.getKey());
            }
         }

         HashedComponentPatchMap map = new HashedComponentPatchMap(addedComponents, removedComponents);
         return new HashedStack(stack.getType(), stack.getAmount(), map);
      }
   }

   public ItemStack asItemStack() {
      ItemStack stack = ItemStack.builder().type(this.item).amount(this.count).build();

      for (ComponentType<?> component : this.components.getRemovedComponents()) {
         stack.unsetComponent(component);
      }

      return stack;
   }

   public ItemType getItem() {
      return this.item;
   }

   public int getCount() {
      return this.count;
   }

   public HashedComponentPatchMap getComponents() {
      return this.components;
   }
}
