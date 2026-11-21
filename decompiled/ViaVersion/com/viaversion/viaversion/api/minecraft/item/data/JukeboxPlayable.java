package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.TransformingType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.EitherHolderType;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record JukeboxPlayable(EitherHolder<JukeboxPlayable.JukeboxSong> song, boolean showInTooltip) implements Rewritable {
   public static final Type<JukeboxPlayable> TYPE1_21 = new Type<JukeboxPlayable>(JukeboxPlayable.class) {
      public JukeboxPlayable read(ByteBuf buffer) {
         EitherHolder<JukeboxPlayable.JukeboxSong> song = EitherHolderType.read(buffer, JukeboxPlayable.JukeboxSong.TYPE);
         boolean showInTooltip = buffer.readBoolean();
         return new JukeboxPlayable(song, showInTooltip);
      }

      public void write(ByteBuf buffer, JukeboxPlayable value) {
         EitherHolderType.write(buffer, value.song, JukeboxPlayable.JukeboxSong.TYPE);
         buffer.writeBoolean(value.showInTooltip);
      }
   };
   public static final Type<JukeboxPlayable> TYPE1_21_5 = TransformingType.of(
      new EitherHolderType<>(JukeboxPlayable.JukeboxSong.TYPE), JukeboxPlayable.class, song -> new JukeboxPlayable(song, true), JukeboxPlayable::song
   );

   public JukeboxPlayable(Holder<JukeboxPlayable.JukeboxSong> song, boolean showInTooltip) {
      this(EitherHolder.of(song), showInTooltip);
   }

   public JukeboxPlayable(String resourceKey, boolean showInTooltip) {
      this(EitherHolder.of(resourceKey), showInTooltip);
   }

   public JukeboxPlayable rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      if (this.song.hasKey()) {
         return this;
      } else {
         Holder<JukeboxPlayable.JukeboxSong> songHolder = this.song.holder();
         if (songHolder.hasId()) {
            return this;
         } else {
            JukeboxPlayable.JukeboxSong rewrittenSong = songHolder.value().rewrite(connection, protocol, clientbound);
            return rewrittenSong == songHolder.value() ? this : new JukeboxPlayable(Holder.of(rewrittenSong), this.showInTooltip);
         }
      }
   }

   public record JukeboxSong(Holder<SoundEvent> soundEvent, Tag description, float lengthInSeconds, int comparatorOutput) implements Rewritable {
      public static final HolderType<JukeboxPlayable.JukeboxSong> TYPE = new HolderType<JukeboxPlayable.JukeboxSong>() {
         public JukeboxPlayable.JukeboxSong readDirect(ByteBuf buffer) {
            Holder<SoundEvent> soundEvent = Types.SOUND_EVENT.read(buffer);
            Tag description = Types.TAG.read(buffer);
            float lengthInSeconds = buffer.readFloat();
            int useDuration = Types.VAR_INT.readPrimitive(buffer);
            return new JukeboxPlayable.JukeboxSong(soundEvent, description, lengthInSeconds, useDuration);
         }

         public void writeDirect(ByteBuf buffer, JukeboxPlayable.JukeboxSong value) {
            Types.SOUND_EVENT.write(buffer, value.soundEvent);
            Types.TAG.write(buffer, value.description);
            buffer.writeFloat(value.lengthInSeconds);
            Types.VAR_INT.writePrimitive(buffer, value.comparatorOutput);
         }

         public void writeDirect(Ops ops, JukeboxPlayable.JukeboxSong value) {
            ops.writeMap(
               map -> map.write("sound_event", Types.SOUND_EVENT, value.soundEvent)
                  .write("description", Types.TAG, value.description)
                  .write("length_in_seconds", Types.FLOAT, value.lengthInSeconds)
                  .write("comparator_output", Types.INT, value.comparatorOutput)
            );
         }

         @Override
         protected Key identifier(Ops ops, int id) {
            return ops.context().registryAccess().registryKey("jukebox_song", id);
         }
      };

      public JukeboxPlayable.JukeboxSong rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
         Holder<SoundEvent> soundEvent = SoundEvent.rewriteHolder(this.soundEvent, Rewritable.soundRewriteFunction(protocol, clientbound));
         return soundEvent == this.soundEvent
            ? this
            : new JukeboxPlayable.JukeboxSong(soundEvent, this.description, this.lengthInSeconds, this.comparatorOutput);
      }
   }
}
