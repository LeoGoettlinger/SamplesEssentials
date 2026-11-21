package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.protocol.sound.StaticSound;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerSoundEffect extends PacketWrapper<WrapperPlayServerSoundEffect> {
   private static final float POSITION_MULTIPLIER = 8.0F;
   private Sound sound;
   private SoundCategory soundCategory;
   private Vector3d position;
   private float volume;
   private float pitch;
   private long seed;

   public WrapperPlayServerSoundEffect(PacketSendEvent event) {
      super(event);
   }

   @Deprecated
   public WrapperPlayServerSoundEffect(int soundId, SoundCategory soundCategory, Vector3i effectPosition, float volume, float pitch) {
      this(soundId, soundCategory, effectPosition, volume, pitch, ThreadLocalRandom.current().nextLong());
   }

   @Deprecated
   public WrapperPlayServerSoundEffect(int soundId, SoundCategory soundCategory, Vector3i effectPosition, float volume, float pitch, long seed) {
      this(Sounds.getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), soundId), soundCategory, effectPosition, volume, pitch, seed);
   }

   @Deprecated
   public WrapperPlayServerSoundEffect(Sound sound, SoundCategory soundCategory, Vector3i effectPosition, float volume, float pitch) {
      this(sound, soundCategory, effectPosition, volume, pitch, ThreadLocalRandom.current().nextLong());
   }

   @Deprecated
   public WrapperPlayServerSoundEffect(Sound sound, SoundCategory soundCategory, Vector3i effectPosition, float volume, float pitch, long seed) {
      super(PacketType.Play.Server.SOUND_EFFECT);
      this.sound = sound;
      this.soundCategory = soundCategory;
      this.position = new Vector3d(effectPosition.x / 8.0F, effectPosition.y / 8.0F, effectPosition.z / 8.0F);
      this.volume = volume;
      this.pitch = pitch;
      this.seed = seed;
   }

   public WrapperPlayServerSoundEffect(int soundId, SoundCategory soundCategory, Vector3d position, float volume, float pitch) {
      this(soundId, soundCategory, position, volume, pitch, ThreadLocalRandom.current().nextLong());
   }

   public WrapperPlayServerSoundEffect(int soundId, SoundCategory soundCategory, Vector3d position, float volume, float pitch, long seed) {
      this(Sounds.getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), soundId), soundCategory, position, volume, pitch, seed);
   }

   public WrapperPlayServerSoundEffect(Sound sound, SoundCategory soundCategory, Vector3d position, float volume, float pitch) {
      this(sound, soundCategory, position, volume, pitch, ThreadLocalRandom.current().nextLong());
   }

   public WrapperPlayServerSoundEffect(Sound sound, SoundCategory soundCategory, Vector3d position, float volume, float pitch, long seed) {
      super(PacketType.Play.Server.SOUND_EFFECT);
      this.sound = sound;
      this.soundCategory = soundCategory;
      this.position = position;
      this.volume = volume;
      this.pitch = pitch;
      this.seed = seed;
   }

   @Override
   public void read() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
         this.sound = Sound.read(this);
      } else if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.sound = Sounds.getById(this.serverVersion.toClientVersion(), this.readVarInt());
      } else {
         ResourceLocation soundName = this.readIdentifier();
         Sound sound = Sounds.getByName(soundName.toString());
         this.sound = (Sound)(sound == null ? new StaticSound(soundName, null) : sound);
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.soundCategory = SoundCategory.fromId(this.readVarInt());
      }

      this.position = new Vector3d(this.readInt() / 8.0F, this.readInt() / 8.0F, this.readInt() / 8.0F);
      this.volume = this.readFloat();
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_10)) {
         this.pitch = this.readFloat();
      } else {
         this.pitch = this.readUnsignedByte() / 63.0F;
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
         this.seed = this.readLong();
      }
   }

   @Override
   public void write() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
         Sound.write(this, this.sound);
      } else if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.writeVarInt(this.sound.getId(this.serverVersion.toClientVersion()));
      } else {
         this.writeString(this.sound.getSoundId().getKey());
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.writeVarInt(this.soundCategory.ordinal());
      }

      this.writeInt((int)(this.position.x * 8.0));
      this.writeInt((int)(this.position.y * 8.0));
      this.writeInt((int)(this.position.z * 8.0));
      this.writeFloat(this.volume);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_10)) {
         this.writeFloat(this.pitch);
      } else {
         this.writeByte((int)(this.pitch * 63.0F));
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19)) {
         this.writeLong(this.seed);
      }
   }

   public void copy(WrapperPlayServerSoundEffect wrapper) {
      this.sound = wrapper.sound;
      this.soundCategory = wrapper.soundCategory;
      this.position = wrapper.position;
      this.volume = wrapper.volume;
      this.pitch = wrapper.pitch;
      this.seed = wrapper.seed;
   }

   public Sound getSound() {
      return this.sound;
   }

   public void setSound(Sound sound) {
      this.sound = sound;
   }

   @Deprecated
   public int getSoundId() {
      return this.getSound().getId(this.serverVersion.toClientVersion());
   }

   @Deprecated
   public void setSoundId(int soundId) {
      this.setSound(Sounds.getById(this.serverVersion.toClientVersion(), soundId));
   }

   @Nullable
   public SoundCategory getSoundCategory() {
      return this.soundCategory;
   }

   public void setSoundCategory(SoundCategory soundCategory) {
      this.soundCategory = soundCategory;
   }

   public Vector3d getPosition() {
      return this.position;
   }

   public void setPosition(Vector3d position) {
      this.position = position;
   }

   @Deprecated
   public Vector3i getEffectPosition() {
      return new Vector3i((int)(this.position.x * 8.0), (int)(this.position.y * 8.0), (int)(this.position.z * 8.0));
   }

   @Deprecated
   public void setEffectPosition(Vector3i effectPosition) {
      this.position = new Vector3d(effectPosition.x / 8.0F, effectPosition.y / 8.0F, effectPosition.z / 8.0F);
   }

   public float getVolume() {
      return this.volume;
   }

   public void setVolume(float volume) {
      this.volume = volume;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long seed) {
      this.seed = seed;
   }
}
