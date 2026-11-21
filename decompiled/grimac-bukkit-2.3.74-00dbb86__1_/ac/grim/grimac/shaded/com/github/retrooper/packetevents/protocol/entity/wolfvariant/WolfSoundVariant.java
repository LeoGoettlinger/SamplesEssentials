package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.wolfvariant;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.CopyableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.DeepComparableEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBT;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.sound.Sound;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodec;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface WolfSoundVariant extends MappedEntity, CopyableEntity<WolfSoundVariant>, DeepComparableEntity {
   NbtCodec<WolfSoundVariant> CODEC = new NbtCodec<WolfSoundVariant>() {
      public WolfSoundVariant decode(NBT tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)) {
            WolfSoundSet sounds = WolfSoundSet.CODEC.decode(tag, wrapper);
            return new StaticWolfSoundVariant(sounds, sounds);
         } else {
            NBTCompound compound = (NBTCompound)tag;
            WolfSoundSet adultSounds = compound.getOrThrow("adult_sounds", WolfSoundSet.CODEC, wrapper);
            WolfSoundSet babySounds = compound.getOrThrow("baby_sounds", WolfSoundSet.CODEC, wrapper);
            return new StaticWolfSoundVariant(adultSounds, babySounds);
         }
      }

      public NBT encode(PacketWrapper<?> wrapper, WolfSoundVariant value) throws NbtCodecException {
         if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_26_1)) {
            return WolfSoundSet.CODEC.encode(wrapper, value.getAdultSounds());
         } else {
            NBTCompound tag = new NBTCompound();
            tag.set("adult_sounds", value.getAdultSounds(), WolfSoundSet.CODEC, wrapper);
            tag.set("baby_sounds", value.getBabySounds(), WolfSoundSet.CODEC, wrapper);
            return tag;
         }
      }
   };

   WolfSoundSet getAdultSounds();

   WolfSoundSet getBabySounds();

   @Deprecated
   default Sound getAmbientSound() {
      return this.getAdultSounds().getAmbientSound();
   }

   @Deprecated
   default Sound getDeathSound() {
      return this.getAdultSounds().getDeathSound();
   }

   @Deprecated
   default Sound getGrowlSound() {
      return this.getAdultSounds().getGrowlSound();
   }

   @Deprecated
   default Sound getHurtSound() {
      return this.getAdultSounds().getHurtSound();
   }

   @Deprecated
   default Sound getPantSound() {
      return this.getAdultSounds().getPantSound();
   }

   @Deprecated
   default Sound getWhineSound() {
      return this.getAdultSounds().getWhineSound();
   }

   static WolfSoundVariant read(PacketWrapper<?> wrapper) {
      return wrapper.readMappedEntity(WolfSoundVariants.getRegistry());
   }

   static void write(PacketWrapper<?> wrapper, WolfSoundVariant variant) {
      wrapper.writeMappedEntity(variant);
   }

   @Deprecated
   static WolfSoundVariant decode(NBT tag, ClientVersion version, @Nullable TypesBuilderData data) {
      return CODEC.decode(tag, PacketWrapper.createDummyWrapper(version)).copy(data);
   }

   @Deprecated
   static NBT encode(WolfSoundVariant variant, ClientVersion version) {
      return CODEC.encode(PacketWrapper.createDummyWrapper(version), variant);
   }
}
