package com.github.retrooper.packetevents.protocol.item.jukebox;

import com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTFloat;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IJukeboxSong extends MappedEntity, CopyableEntity<IJukeboxSong>, DeepComparableEntity {
   Sound getSound();

   Component getDescription();

   float getLengthInSeconds();

   int getComparatorOutput();

   @Deprecated
   static IJukeboxSong decode(NBT nbt, ClientVersion version, @Nullable TypesBuilderData data) {
      return decode(nbt, PacketWrapper.createDummyWrapper(version), data);
   }

   static IJukeboxSong decode(NBT nbt, PacketWrapper<?> wrapper, @Nullable TypesBuilderData data) {
      NBTCompound compound = (NBTCompound)nbt;
      Sound sound = compound.getOrThrow("sound_event", Sound.CODEC, wrapper);
      Component description = compound.getOrThrow("description", wrapper.getSerializers(), wrapper);
      float length = compound.getNumberTagOrThrow("length_in_seconds").getAsFloat();
      int comparator_output = compound.getNumberTagOrThrow("comparator_output").getAsInt();
      return new JukeboxSong(data, sound, description, length, comparator_output);
   }

   @Deprecated
   static NBT encode(IJukeboxSong jukeboxSong, ClientVersion version) {
      return encode(PacketWrapper.createDummyWrapper(version), jukeboxSong);
   }

   static NBT encode(PacketWrapper<?> wrapper, IJukeboxSong song) {
      NBTCompound compound = new NBTCompound();
      compound.set("sound_event", song.getSound(), Sound.CODEC, wrapper);
      compound.set("description", song.getDescription(), wrapper.getSerializers(), wrapper);
      compound.setTag("length_in_seconds", new NBTFloat(song.getLengthInSeconds()));
      compound.setTag("comparator_output", new NBTInt(song.getComparatorOutput()));
      return compound;
   }

   static IJukeboxSong read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntityOrDirect(JukeboxSongs.getRegistry(), IJukeboxSong::readDirect);
   }

   static IJukeboxSong readDirect(PacketWrapper<?> wrapper) {
      Sound sound = Sound.read(wrapper);
      Component description = wrapper.readComponent();
      float lengthInSeconds = wrapper.readFloat();
      int comparatorOutput = wrapper.readVarInt();
      return new JukeboxSong(null, sound, description, lengthInSeconds, comparatorOutput);
   }

   static void write(PacketWrapper<?> wrapper, IJukeboxSong song) {
      wrapper.writeMappedEntityOrDirect(song, IJukeboxSong::writeDirect);
   }

   static void writeDirect(PacketWrapper<?> wrapper, IJukeboxSong song) {
      Sound.write(wrapper, song.getSound());
      wrapper.writeComponent(song.getDescription());
      wrapper.writeFloat(song.getLengthInSeconds());
      wrapper.writeVarInt(song.getComparatorOutput());
   }
}
