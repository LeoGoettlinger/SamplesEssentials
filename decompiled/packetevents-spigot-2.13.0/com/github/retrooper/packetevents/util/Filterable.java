package com.github.retrooper.packetevents.util;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class Filterable<T> {
   private T raw;
   @Nullable
   private T filtered;

   public Filterable(T raw) {
      this(raw, null);
   }

   public Filterable(T raw, Optional<T> filtered) {
      this(raw, filtered.orElse(null));
   }

   public Filterable(T raw, @Nullable T filtered) {
      this.raw = raw;
      this.filtered = filtered;
   }

   public static <T> Filterable<T> read(PacketWrapper<?> wrapper, PacketWrapper.Reader<T> reader) {
      T raw = reader.apply(wrapper);
      T filtered = wrapper.readOptional(reader);
      return new Filterable<>(raw, filtered);
   }

   public static <T> void write(PacketWrapper<?> wrapper, Filterable<T> filterable, PacketWrapper.Writer<T> writer) {
      writer.accept(wrapper, filterable.raw);
      wrapper.writeOptional(filterable.filtered, writer);
   }

   public T getRaw() {
      return this.raw;
   }

   public void setRaw(T raw) {
      this.raw = raw;
   }

   @Nullable
   public T getFiltered() {
      return this.filtered;
   }

   public void setFiltered(@Nullable T filtered) {
      this.filtered = filtered;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof Filterable)) {
         return false;
      } else {
         Filterable<?> that = (Filterable<?>)obj;
         return !this.raw.equals(that.raw) ? false : Objects.equals(this.filtered, that.filtered);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.raw, this.filtered);
   }
}
