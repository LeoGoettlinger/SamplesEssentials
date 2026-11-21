package com.viaversion.nbt.stringified;

import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.IntStream.Builder;
import org.jetbrains.annotations.Nullable;

final class TagStringReader {
   private static final int MAX_DEPTH = 512;
   private static final int HEX_RADIX = 16;
   private static final int BINARY_RADIX = 2;
   private static final int DECIMAL_RADIX = 10;
   private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
   private static final int[] EMPTY_INT_ARRAY = new int[0];
   private static final long[] EMPTY_LONG_ARRAY = new long[0];
   private final CharBuffer buffer;
   private boolean acceptLegacy = true;
   private int depth;

   TagStringReader(CharBuffer buffer) {
      this.buffer = buffer;
   }

   public CompoundTag compound() throws StringifiedTagParseException {
      this.buffer.expect('{');
      CompoundTag compoundTag = new CompoundTag();
      if (this.buffer.takeIf('}')) {
         return compoundTag;
      } else {
         while (this.buffer.hasMore()) {
            compoundTag.put(this.key(), this.tag());
            if (this.separatorOrCompleteWith('}')) {
               return compoundTag;
            }
         }

         throw this.buffer.makeError("Unterminated compound tag!");
      }
   }

   public ListTag<?> list() throws StringifiedTagParseException {
      List<Tag> list = new ArrayList<>();
      this.buffer.expect('[');
      boolean prefixedIndex = this.acceptLegacy && this.buffer.peek() == '0' && this.buffer.peek(1) == ':';
      if (!prefixedIndex && this.buffer.takeIf(']')) {
         return ListTag.of(list);
      } else {
         while (this.buffer.hasMore()) {
            if (prefixedIndex) {
               this.buffer.takeUntil(':');
            }

            Tag next = this.tag();
            list.add(next);
            if (this.separatorOrCompleteWith(']')) {
               return ListTag.of(list);
            }
         }

         throw this.buffer.makeError("Reached end of file without end of list tag!");
      }
   }

   public Tag array(char elementType) throws StringifiedTagParseException {
      this.buffer.expect('[').expect(elementType).expect(';');
      elementType = Character.toLowerCase(elementType);
      if (elementType == 'b') {
         return new ByteArrayTag(this.byteArray());
      } else if (elementType == 'i') {
         return new IntArrayTag(this.intArray());
      } else if (elementType == 'l') {
         return new LongArrayTag(this.longArray());
      } else {
         throw this.buffer.makeError("Type " + elementType + " is not a valid element type in an array!");
      }
   }

   private byte[] byteArray() throws StringifiedTagParseException {
      if (this.buffer.takeIf(']')) {
         return EMPTY_BYTE_ARRAY;
      } else {
         IntList bytes = new IntArrayList();

         while (this.buffer.hasMore()) {
            CharSequence value = this.buffer.skipWhitespace().takeUntil('b');

            try {
               bytes.add(Byte.parseByte(value.toString()));
            } catch (NumberFormatException var5) {
               throw this.buffer.makeError("All elements of a byte array must be bytes!");
            }

            if (this.separatorOrCompleteWith(']')) {
               byte[] result = new byte[bytes.size()];

               for (int i = 0; i < bytes.size(); i++) {
                  result[i] = (byte)bytes.getInt(i);
               }

               return result;
            }
         }

         throw this.buffer.makeError("Reached end of document without array close");
      }
   }

   private int[] intArray() throws StringifiedTagParseException {
      if (this.buffer.takeIf(']')) {
         return EMPTY_INT_ARRAY;
      } else {
         Builder builder = IntStream.builder();

         while (this.buffer.hasMore()) {
            Tag value = this.tag();
            if (!(value instanceof IntTag)) {
               throw this.buffer.makeError("All elements of an int array must be ints!");
            }

            builder.add(((NumberTag)value).asInt());
            if (this.separatorOrCompleteWith(']')) {
               return builder.build().toArray();
            }
         }

         throw this.buffer.makeError("Reached end of document without array close");
      }
   }

   private long[] longArray() throws StringifiedTagParseException {
      if (this.buffer.takeIf(']')) {
         return EMPTY_LONG_ARRAY;
      } else {
         java.util.stream.LongStream.Builder longs = LongStream.builder();

         while (this.buffer.hasMore()) {
            CharSequence value = this.buffer.skipWhitespace().takeUntil('l');

            try {
               longs.add(Long.parseLong(value.toString()));
            } catch (NumberFormatException var4) {
               throw this.buffer.makeError("All elements of a long array must be longs!");
            }

            if (this.separatorOrCompleteWith(']')) {
               return longs.build().toArray();
            }
         }

         throw this.buffer.makeError("Reached end of document without array close");
      }
   }

   public String key() throws StringifiedTagParseException {
      this.buffer.skipWhitespace();
      char starChar = this.buffer.peek();

      String var8;
      try {
         if (starChar == '\'' || starChar == '"') {
            return unescape(this.buffer.takeUntil(this.buffer.take()).toString());
         }

         StringBuilder builder = new StringBuilder();

         while (this.buffer.hasMore()) {
            char peek = this.buffer.peek();
            if (!Tokens.id(peek)) {
               if (!this.acceptLegacy) {
                  break;
               }

               if (peek == '\\') {
                  this.buffer.take();
               } else {
                  if (peek == ':') {
                     break;
                  }

                  builder.append(this.buffer.take());
               }
            } else {
               builder.append(this.buffer.take());
            }
         }

         var8 = builder.toString();
      } finally {
         this.buffer.expect(':');
      }

      return var8;
   }

   public Tag tag() throws StringifiedTagParseException {
      if (this.depth++ > 512) {
         throw this.buffer.makeError("Exceeded maximum allowed depth of 512 when reading tag");
      } else {
         try {
            char startToken = this.buffer.skipWhitespace().peek();
            switch (startToken) {
               case '"':
               case '\'':
                  this.buffer.advance();
                  return new StringTag(unescape(this.buffer.takeUntil(startToken).toString()));
               case '[':
                  if (!this.buffer.hasMore(2) || this.buffer.peek(2) != ';') {
                     return this.list();
                  }

                  return this.array(this.buffer.peek(1));
               case '{':
                  return this.compound();
               default:
                  return this.scalar();
            }
         } finally {
            this.depth--;
         }
      }
   }

   private Tag scalar() throws StringifiedTagParseException {
      StringBuilder builder = new StringBuilder();

      while (this.buffer.hasMore()) {
         char current = this.buffer.peek();
         if (current == '\\') {
            this.buffer.advance();
            current = this.buffer.take();
         } else {
            if (!Tokens.id(current)) {
               break;
            }

            this.buffer.advance();
         }

         builder.append(current);
      }

      if (builder.length() == 0) {
         throw this.buffer.makeError("Expected a value but got nothing");
      } else {
         String original = builder.toString();
         int radix = this.extractRadix(builder, original);
         char last = builder.charAt(builder.length() - 1);
         boolean hasSignToken = false;
         boolean signed = radix != 16;
         if (builder.length() > 2) {
            char signChar = builder.charAt(builder.length() - 2);
            if (signChar == 's' || signChar == 'u') {
               hasSignToken = true;
               signed = signChar == 's';
               builder.deleteCharAt(builder.length() - 2);
            }
         }

         boolean hasTypeToken = false;
         char typeToken = 'i';
         if (Tokens.numericType(last) && (hasSignToken || radix != 16)) {
            hasTypeToken = true;
            typeToken = Character.toLowerCase(last);
            builder.deleteCharAt(builder.length() - 1);
         }

         if (signed || typeToken != 'f' && typeToken != 'd') {
            String strippedString = builder.toString().replace("_", "");
            if (hasTypeToken) {
               try {
                  NumberTag tag = this.parseNumberTag(strippedString, typeToken, radix, signed);
                  if (tag != null) {
                     return tag;
                  }
               } catch (NumberFormatException var12) {
               }
            } else {
               try {
                  return new IntTag(this.parseInt(strippedString, radix, signed));
               } catch (NumberFormatException var14) {
                  if (strippedString.indexOf(46) != -1) {
                     try {
                        return new DoubleTag(Double.parseDouble(strippedString));
                     } catch (NumberFormatException var13) {
                     }
                  }
               }
            }

            if (original.equalsIgnoreCase("true")) {
               return new ByteTag((byte)1);
            } else {
               return (Tag)(original.equalsIgnoreCase("false") ? new ByteTag((byte)0) : new StringTag(original));
            }
         } else {
            throw this.buffer.makeError("Cannot create unsigned floating point numbers");
         }
      }
   }

   private int extractRadix(StringBuilder builder, String original) {
      int radixPrefixOffset = 0;
      char first = builder.charAt(0);
      if (first == '+' || first == '-') {
         radixPrefixOffset = 1;
      }

      if (builder.length() < 3 + radixPrefixOffset) {
         return 10;
      } else {
         int radix;
         if (!original.startsWith("0b", radixPrefixOffset) && !original.startsWith("0B", radixPrefixOffset)) {
            if (!original.startsWith("0x", radixPrefixOffset) && !original.startsWith("0X", radixPrefixOffset)) {
               return 10;
            }

            radix = 16;
         } else {
            radix = 2;
         }

         builder.delete(radixPrefixOffset, 2 + radixPrefixOffset);
         return radix;
      }
   }

   @Nullable
   private NumberTag parseNumberTag(String s, char typeToken, int radix, boolean signed) {
      switch (typeToken) {
         case 'b':
            return new ByteTag(this.parseByte(s, radix, signed));
         case 'c':
         case 'e':
         case 'g':
         case 'h':
         case 'j':
         case 'k':
         case 'm':
         case 'n':
         case 'o':
         case 'p':
         case 'q':
         case 'r':
         default:
            break;
         case 'd':
            double doubleValue = Double.parseDouble(s);
            if (Double.isFinite(doubleValue)) {
               return new DoubleTag(doubleValue);
            }
            break;
         case 'f':
            float floatValue = Float.parseFloat(s);
            if (Float.isFinite(floatValue)) {
               return new FloatTag(floatValue);
            }
            break;
         case 'i':
            return new IntTag(this.parseInt(s, radix, signed));
         case 'l':
            return new LongTag(this.parseLong(s, radix, signed));
         case 's':
            return new ShortTag(this.parseShort(s, radix, signed));
      }

      return null;
   }

   private byte parseByte(String s, int radix, boolean signed) {
      if (signed) {
         return Byte.parseByte(s, radix);
      } else {
         int parsedInt = Integer.parseInt(s, radix);
         if (parsedInt >> 8 == 0) {
            return (byte)parsedInt;
         } else {
            throw new NumberFormatException();
         }
      }
   }

   private short parseShort(String s, int radix, boolean signed) {
      if (signed) {
         return Short.parseShort(s, radix);
      } else {
         int parsedInt = Integer.parseInt(s, radix);
         if (parsedInt >> 16 == 0) {
            return (short)parsedInt;
         } else {
            throw new NumberFormatException();
         }
      }
   }

   private int parseInt(String s, int radix, boolean signed) {
      return signed ? Integer.parseInt(s, radix) : Integer.parseUnsignedInt(s, radix);
   }

   private long parseLong(String s, int radix, boolean signed) {
      return signed ? Long.parseLong(s, radix) : Long.parseUnsignedLong(s, radix);
   }

   private boolean separatorOrCompleteWith(char endCharacter) throws StringifiedTagParseException {
      if (this.buffer.takeIf(endCharacter)) {
         return true;
      } else {
         this.buffer.expect(',');
         return this.buffer.takeIf(endCharacter);
      }
   }

   private static String unescape(String withEscapes) {
      int escapeIdx = withEscapes.indexOf(92);
      if (escapeIdx == -1) {
         return withEscapes;
      } else {
         int lastEscape = 0;
         StringBuilder output = new StringBuilder(withEscapes.length());

         do {
            output.append(withEscapes, lastEscape, escapeIdx);
            lastEscape = escapeIdx + 1;
         } while ((escapeIdx = withEscapes.indexOf(92, lastEscape + 1)) != -1);

         output.append(withEscapes.substring(lastEscape));
         return output.toString();
      }
   }

   public void legacy(boolean acceptLegacy) {
      this.acceptLegacy = acceptLegacy;
   }
}
