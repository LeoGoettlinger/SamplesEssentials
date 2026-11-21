package com.github.retrooper.packetevents.protocol.dialog.action;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Action {
   static Action decode(NBT nbt, PacketWrapper<?> wrapper) {
      NBTCompound compound = (NBTCompound)nbt;
      String typeName = compound.getStringTagValueOrThrow("type");
      ActionType<?> action = ActionTypes.getRegistry().getByNameOrThrow(typeName);
      return action.decode(compound, wrapper);
   }

   static NBT encode(PacketWrapper<?> wrapper, Action action) {
      NBTCompound compound = new NBTCompound();
      compound.set("type", action.getType().getName(), ResourceLocation::encode, wrapper);
      ((ActionType<Action>)action.getType()).encode(compound, wrapper, action);
      return compound;
   }

   ActionType<?> getType();
}
