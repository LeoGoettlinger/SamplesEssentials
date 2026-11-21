package com.artillexstudios.axafkzone.libs.axapi.packet;

import com.artillexstudios.axafkzone.libs.axapi.packet.exception.PacketReadingException;
import com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.PacketWrapper;
import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.exception.IllegalFeatureFlagStateException;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public enum PacketEvents {
   INSTANCE;

   private final ObjectArrayList<PacketListener> listeners = new ObjectArrayList();
   private PacketListener[] baked = new PacketListener[0];
   private boolean listening = false;

   public void addListener(PacketListener listener) {
      if (!FeatureFlags.ENABLE_PACKET_LISTENERS.get()) {
         throw new IllegalFeatureFlagStateException("Packet listeners!");
      } else {
         this.listeners.add(listener);
         this.listening = true;
         this.baked = (PacketListener[])this.listeners.toArray(new PacketListener[0]);
      }
   }

   public void callEvent(PacketEvent event) {
      if (this.listening) {
         PacketListener[] baked = this.baked;

         for (PacketListener listener : baked) {
            try {
               if (event.side() == PacketSide.CLIENT_BOUND) {
                  listener.onPacketSending(event);
               } else {
                  listener.onPacketReceive(event);
               }
            } catch (PacketReadingException var9) {
               if (FeatureFlags.DEBUG.get()) {
                  LogUtils.info("An issue occurred while reading packet! Packet side: {}", event.side(), var9);
               }

               return;
            } catch (Throwable var10) {
               LogUtils.error("Failed to read {} packet!", event.side(), var10);
               continue;
            }

            PacketWrapper wrapper = event.wrapper();
            if (wrapper != null) {
               FriendlyByteBuf out = event.out();
               wrapper.write(out);
            }
         }
      }
   }

   public boolean listening() {
      return this.listening;
   }
}
