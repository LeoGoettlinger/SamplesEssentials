package org.enginehub.linbus.stream.impl;

import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.stream.token.LinToken;

public class ValueCounter {
   private static final byte BYTE_ARRAY = 1;
   private static final byte INT_ARRAY = 2;
   private static final byte LONG_ARRAY = 3;
   private int count;
   private int compounds;
   private int lists;
   private byte arrayType;

   public void add(LinToken token) {
      if (token.isSimpleValue() && !this.isNested()) {
         this.count++;
      } else {
         switch (token) {
            case LinToken.CompoundStart compoundStart:
               this.compounds++;
               break;
            case LinToken.CompoundEnd compoundEnd:
               this.compounds--;
               if (this.compounds < 0) {
                  throw new NbtParseException("Compound end without start");
               }

               if (!this.isNested()) {
                  this.count++;
               }
               break;
            case LinToken.ListStart listStart:
               this.lists++;
               break;
            case LinToken.ListEnd listEnd:
               this.lists--;
               if (this.lists < 0) {
                  throw new NbtParseException("List end without start");
               }

               if (!this.isNested()) {
                  this.count++;
               }
               break;
            case LinToken.ByteArrayStart byteArrayStart:
               this.arrayType = 1;
               break;
            case LinToken.ByteArrayEnd byteArrayEnd:
               if (this.arrayType != 1) {
                  throw new NbtParseException("Byte array end without start");
               }

               this.arrayType = 0;
               if (!this.isNested()) {
                  this.count++;
               }
               break;
            case LinToken.IntArrayStart intArrayStart:
               this.arrayType = 2;
               break;
            case LinToken.IntArrayEnd intArrayEnd:
               if (this.arrayType != 2) {
                  throw new NbtParseException("Int array end without start");
               }

               this.arrayType = 0;
               if (!this.isNested()) {
                  this.count++;
               }
               break;
            case LinToken.LongArrayStart longArrayStart:
               this.arrayType = 3;
               break;
            case LinToken.LongArrayEnd longArrayEnd:
               label36: {
                  if (this.arrayType != 3) {
                     throw new NbtParseException("Long array end without start");
                  }

                  this.arrayType = 0;
                  if (!this.isNested()) {
                     this.count++;
                  }
                  break label36;
               }
            default:
         }
      }
   }

   public int count() {
      return this.count;
   }

   public boolean isNested() {
      return this.lists > 0 || this.compounds > 0;
   }
}
