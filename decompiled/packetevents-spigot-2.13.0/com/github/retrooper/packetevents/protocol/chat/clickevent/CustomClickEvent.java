package com.github.retrooper.packetevents.protocol.chat.clickevent;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTEnd;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.adventure.NbtTagHolder;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class CustomClickEvent implements ClickEvent {
   private final ResourceLocation id;
   @Nullable
   private final NBT payload;

   public CustomClickEvent(ResourceLocation id, @Nullable NBT payload) {
      this.id = id;
      this.payload = payload;
   }

   public static CustomClickEvent decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      ResourceLocation id = compound.getOrThrow("id", ResourceLocation::decode, wrapper);
      NBT payload = compound.getTagOrNull("payload");
      return new CustomClickEvent(id, payload);
   }

   public static void encode(NBTCompound compound, PacketWrapper<?> wrapper, CustomClickEvent clickEvent) {
      compound.set("id", clickEvent.id, ResourceLocation::encode, wrapper);
      if (clickEvent.payload != null) {
         compound.setTag("payload", clickEvent.payload);
      }
   }

   @Override
   public ClickEventAction<?> getAction() {
      return ClickEventActions.CUSTOM;
   }

   @Override
   public net.kyori.adventure.text.event.ClickEvent asAdventure() {
      return net.kyori.adventure.text.event.ClickEvent.custom(this.id.key(), new NbtTagHolder((NBT)(this.payload != null ? this.payload : NBTEnd.INSTANCE)));
   }

   public ResourceLocation getId() {
      return this.id;
   }

   @Nullable
   public NBT getPayload() {
      return this.payload;
   }
}
