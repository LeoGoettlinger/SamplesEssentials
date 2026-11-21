package com.github.retrooper.packetevents.protocol.stream;

import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class NetStreamOutputWrapper extends NetStreamOutput {
   private final PacketWrapper<?> wrapper;

   public NetStreamOutputWrapper(PacketWrapper<?> wrapper) {
      super(null);
      this.wrapper = wrapper;
   }

   @Override
   public void write(int b) {
      this.wrapper.writeByte(b);
   }

   @Override
   public void write(byte[] b) {
      this.write(b, 0, b.length);
   }

   @Override
   public void write(byte[] b, int off, int len) {
      ByteBufHelper.writeBytes(this.wrapper.buffer, b, off, len);
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() {
   }
}
