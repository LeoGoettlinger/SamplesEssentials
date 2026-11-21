package com.viaversion.nbt.tag;

import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.stringified.SNBT;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class ShortTag implements NumberTag {
   public static final int ID = 2;
   public static final ShortTag ZERO = new ShortTag((short)0);
   private final short value;

   public ShortTag(short value) {
      this.value = value;
   }

   public static ShortTag read(DataInput in, TagLimiter tagLimiter) throws IOException {
      tagLimiter.countShort();
      return new ShortTag(in.readShort());
   }

   @Deprecated
   public Short getValue() {
      return this.value;
   }

   @Override
   public String asRawString() {
      return Short.toString(this.value);
   }

   @Override
   public void write(DataOutput out) throws IOException {
      out.writeShort(this.value);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ShortTag shortTag = (ShortTag)o;
         return this.value == shortTag.value;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.value;
   }

   @Override
   public byte asByte() {
      return (byte)this.value;
   }

   @Override
   public short asShort() {
      return this.value;
   }

   @Override
   public int asInt() {
      return this.value;
   }

   @Override
   public long asLong() {
      return this.value;
   }

   @Override
   public float asFloat() {
      return this.value;
   }

   @Override
   public double asDouble() {
      return this.value;
   }

   @Override
   public int getTagId() {
      return 2;
   }

   @Override
   public String toString() {
      return SNBT.serialize(this);
   }
}
