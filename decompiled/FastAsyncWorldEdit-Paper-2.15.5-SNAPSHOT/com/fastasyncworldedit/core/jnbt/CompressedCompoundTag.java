package com.fastasyncworldedit.core.jnbt;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.jpountz.lz4.LZ4BlockInputStream;

public abstract class CompressedCompoundTag<T> extends CompoundTag {
   private T in;

   public CompressedCompoundTag(T in) {
      super(new HashMap<>());
      this.in = in;
   }

   @Override
   public Map<String, Tag<?, ?>> getValue() {
      if (this.in != null) {
         this.decompress();
      }

      return super.getValue();
   }

   public abstract LZ4BlockInputStream adapt(T src) throws IOException;

   public T getSource() {
      return this.in;
   }

   private void decompress() {
      try {
         try (NBTInputStream nbtIn = new NBTInputStream(this.adapt(this.in))) {
            this.in = null;
            CompoundTag tag = (CompoundTag)nbtIn.readTag();
            Map<String, Tag<?, ?>> value = tag.getValue();
            Map<String, Tag<?, ?>> raw = super.getValue();
            raw.putAll(value);
         }
      } catch (IOException var7) {
         throw new RuntimeException(var7);
      }
   }
}
