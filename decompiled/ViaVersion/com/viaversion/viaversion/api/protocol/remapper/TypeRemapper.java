package com.viaversion.viaversion.api.protocol.remapper;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.exception.InformativeException;

public record TypeRemapper<T>(Type<T> type) implements ValueReader<T>, ValueWriter<T> {
   @Override
   public T read(PacketWrapper wrapper) throws InformativeException {
      return wrapper.read(this.type);
   }

   @Override
   public void write(PacketWrapper output, T inputValue) throws InformativeException {
      output.write(this.type, inputValue);
   }
}
