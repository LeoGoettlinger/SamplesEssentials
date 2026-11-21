package com.github.retrooper.packetevents.protocol.item.instrument;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.util.MathUtil;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Instrument extends MappedEntity, CopyableEntity<Instrument>, DeepComparableEntity {
   Sound getSound();

   float getUseSeconds();

   default int getUseDuration() {
      return MathUtil.floor(this.getUseSeconds() * 20.0F);
   }

   float getRange();

   Component getDescription();

   static Instrument read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntityOrDirect(Instruments.getRegistry(), Instrument::readDirect);
   }

   static Instrument readDirect(PacketWrapper<?> wrapper) {
      Sound sound = Sound.read(wrapper);
      float useSeconds = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2) ? wrapper.readFloat() : wrapper.readVarInt() * 20.0F;
      float range = wrapper.readFloat();
      Component description = (Component)(wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2) ? wrapper.readComponent() : Component.empty());
      return new StaticInstrument(sound, useSeconds, range, description);
   }

   static void write(PacketWrapper<?> wrapper, Instrument instrument) {
      wrapper.writeMappedEntityOrDirect(instrument, Instrument::writeDirect);
   }

   static void writeDirect(PacketWrapper<?> wrapper, Instrument instrument) {
      Sound.write(wrapper, instrument.getSound());
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
         wrapper.writeFloat(instrument.getUseSeconds());
      } else {
         wrapper.writeVarInt(instrument.getUseDuration());
      }

      wrapper.writeFloat(instrument.getRange());
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2)) {
         wrapper.writeComponent(instrument.getDescription());
      }
   }

   @Deprecated
   static Instrument decode(NBT nbt, ClientVersion version, @Nullable TypesBuilderData data) {
      return decode(nbt, PacketWrapper.createDummyWrapper(version), data);
   }

   static Instrument decode(NBT nbt, PacketWrapper<?> wrapper, @Nullable TypesBuilderData data) {
      NBTCompound compound = (NBTCompound)nbt;
      Sound sound = compound.getOrThrow("sound_event", Sound.CODEC, wrapper);
      float useSeconds = compound.getNumberTagOrThrow("use_duration").getAsFloat();
      float range = compound.getNumberTagOrThrow("range").getAsFloat();
      Component description = compound.getOrThrow("description", wrapper.getSerializers(), wrapper);
      return new StaticInstrument(data, sound, useSeconds, range, description);
   }

   @Deprecated
   static NBT encode(Instrument instrument, ClientVersion version) {
      return encode(PacketWrapper.createDummyWrapper(version), instrument);
   }

   static NBT encode(PacketWrapper<?> wrapper, Instrument instrument) {
      NBTCompound compound = new NBTCompound();
      compound.set("sound_event", instrument.getSound(), Sound.CODEC, wrapper);
      compound.setTag("use_duration", new NBTFloat(instrument.getUseSeconds()));
      compound.setTag("range", new NBTFloat(instrument.getRange()));
      compound.set("description", instrument.getDescription(), wrapper.getSerializers(), wrapper);
      return compound;
   }
}
