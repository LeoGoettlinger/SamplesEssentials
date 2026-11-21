package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Types;

public class SoundRewriter<C extends ClientboundPacketType> {
   protected final Protocol<C, ?, ?, ?> protocol;

   public SoundRewriter(Protocol<C, ?, ?, ?> protocol) {
      this.protocol = protocol;
   }

   public void registerSound(C packetType) {
      if (this.protocol.getMappingData() != null && !Mappings.isIntIdIdentity(this.protocol.getMappingData().getSoundMappings())) {
         this.protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT);
            this.getSoundHandler().handle(wrapper);
         });
      }
   }

   public void registerSound1_19_3(C packetType) {
      if (this.protocol.getMappingData() != null && !Mappings.isFullIdentity(this.protocol.getMappingData().getFullSoundMappings())) {
         this.protocol.registerClientbound(packetType, this.soundHolderHandler());
      }
   }

   public PacketHandler soundHolderHandler() {
      return wrapper -> {
         Holder<SoundEvent> soundEventHolder = wrapper.read(Types.SOUND_EVENT);
         if (soundEventHolder.isDirect()) {
            wrapper.write(Types.SOUND_EVENT, this.rewriteSoundEvent(wrapper, soundEventHolder));
         } else {
            int mappedId = this.protocol.getMappingData().getSoundMappings().getNewId(soundEventHolder.id());
            if (mappedId == -1) {
               wrapper.cancel();
            } else {
               if (mappedId != soundEventHolder.id()) {
                  soundEventHolder = Holder.of(mappedId);
               }

               wrapper.write(Types.SOUND_EVENT, soundEventHolder);
            }
         }
      };
   }

   public Holder<SoundEvent> rewriteSoundEvent(PacketWrapper wrapper, Holder<SoundEvent> soundEventHolder) {
      SoundEvent soundEvent = soundEventHolder.value();
      String mappedIdentifier = this.protocol.getMappingData().getFullSoundMappings().mappedIdentifier(soundEvent.identifier());
      if (mappedIdentifier != null) {
         if (!mappedIdentifier.isEmpty()) {
            return Holder.of(soundEvent.withIdentifier(mappedIdentifier));
         }

         wrapper.cancel();
      }

      return soundEventHolder;
   }

   public PacketHandler getSoundHandler() {
      return wrapper -> {
         int soundId = wrapper.get(Types.VAR_INT, 0);
         int mappedId = this.protocol.getMappingData().getSoundMappings().getNewId(soundId);
         if (mappedId == -1) {
            wrapper.cancel();
         } else if (soundId != mappedId) {
            wrapper.set(Types.VAR_INT, 0, mappedId);
         }
      };
   }
}
