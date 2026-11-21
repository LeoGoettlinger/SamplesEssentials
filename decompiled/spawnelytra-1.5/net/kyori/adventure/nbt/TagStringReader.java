package net.kyori.adventure.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.IntStream.Builder;

final class TagStringReader {
   private static final int MAX_DEPTH = 512;
   private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
   private static final int[] EMPTY_INT_ARRAY = new int[0];
   private static final long[] EMPTY_LONG_ARRAY = new long[0];
   private final CharBuffer buffer;
   private boolean acceptLegacy;
   private int depth;

   TagStringReader(final CharBuffer buffer) {
      this.buffer = buffer;
   }

   public CompoundBinaryTag compound() throws StringTagParseException {
      this.buffer.expect('{');
      if (this.buffer.takeIf('}')) {
         return CompoundBinaryTag.empty();
      } else {
         CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();

         while (this.buffer.hasMore()) {
            builder.put(this.key(), this.tag());
            if (this.separatorOrCompleteWith('}')) {
               return builder.build();
            }
         }

         throw this.buffer.makeError("Unterminated compound tag!");
      }
   }

   public ListBinaryTag list() throws StringTagParseException {
      ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();
      this.buffer.expect('[');
      boolean prefixedIndex = this.acceptLegacy && this.buffer.peek() == '0' && this.buffer.peek(1) == ':';
      if (!prefixedIndex && this.buffer.takeIf(']')) {
         return ListBinaryTag.empty();
      } else {
         while (this.buffer.hasMore()) {
            if (prefixedIndex) {
               this.buffer.takeUntil(':');
            }

            BinaryTag next = this.tag();
            builder.add(next);
            if (this.separatorOrCompleteWith(']')) {
               return builder.build();
            }
         }

         throw this.buffer.makeError("Reached end of file without end of list tag!");
      }
   }

   public BinaryTag array(char elementType) throws StringTagParseException {
      this.buffer.expect('[').expect(elementType).expect(';');
      elementType = Character.toLowerCase(elementType);
      if (elementType == 'b') {
         return ByteArrayBinaryTag.byteArrayBinaryTag(this.byteArray());
      } else if (elementType == 'i') {
         return IntArrayBinaryTag.intArrayBinaryTag(this.intArray());
      } else if (elementType == 'l') {
         return LongArrayBinaryTag.longArrayBinaryTag(this.longArray());
      } else {
         throw this.buffer.makeError("Type " + elementType + " is not a valid element type in an array!");
      }
   }

   private byte[] byteArray() throws StringTagParseException {
      if (this.buffer.takeIf(']')) {
         return EMPTY_BYTE_ARRAY;
      } else {
         List<Byte> bytes = new ArrayList<>();

         while (this.buffer.hasMore()) {
            CharSequence value = this.buffer.skipWhitespace().takeUntil('b');

            try {
               bytes.add(Byte.valueOf(value.toString()));
            } catch (NumberFormatException var5) {
               throw this.buffer.makeError("All elements of a byte array must be bytes!");
            }

            if (this.separatorOrCompleteWith(']')) {
               byte[] result = new byte[bytes.size()];

               for (int i = 0; i < bytes.size(); i++) {
                  result[i] = bytes.get(i);
               }

               return result;
            }
         }

         throw this.buffer.makeError("Reached end of document without array close");
      }
   }

   private int[] intArray() throws StringTagParseException {
      if (this.buffer.takeIf(']')) {
         return EMPTY_INT_ARRAY;
      } else {
         Builder builder = IntStream.builder();

         while (this.buffer.hasMore()) {
            BinaryTag value = this.tag();
            if (!(value instanceof IntBinaryTag)) {
               throw this.buffer.makeError("All elements of an int array must be ints!");
            }

            builder.add(((IntBinaryTag)value).intValue());
            if (this.separatorOrCompleteWith(']')) {
               return builder.build().toArray();
            }
         }

         throw this.buffer.makeError("Reached end of document without array close");
      }
   }

   private long[] longArray() throws StringTagParseException {
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

   public String key() throws StringTagParseException {
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

   public BinaryTag tag() throws StringTagParseException {
      if (this.depth++ > 512) {
         throw this.buffer.makeError("Exceeded maximum allowed depth of 512 when reading tag");
      } else {
         try {
            char startToken = this.buffer.skipWhitespace().peek();
            switch (startToken) {
               case '"':
               case '\'':
                  this.buffer.advance();
                  return StringBinaryTag.stringBinaryTag(unescape(this.buffer.takeUntil(startToken).toString()));
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

   private BinaryTag scalar() {
      StringBuilder builder = new StringBuilder();
      int noLongerNumericAt = -1;

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
         if (noLongerNumericAt == -1 && !Tokens.numeric(current)) {
            noLongerNumericAt = builder.length();
         }
      }

      int length = builder.length();
      String built = builder.toString();
      if (noLongerNumericAt == length && length > 1) {
         char last = built.charAt(length - 1);

         try {
            switch (Character.toLowerCase(last)) {
               case 'b':
                  return ByteBinaryTag.byteBinaryTag(Byte.parseByte(built.substring(0, length - 1)));
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
                  double doubleValue = Double.parseDouble(built.substring(0, length - 1));
                  if (Double.isFinite(doubleValue)) {
                     return DoubleBinaryTag.doubleBinaryTag(doubleValue);
                  }
                  break;
               case 'f':
                  float floatValue = Float.parseFloat(built.substring(0, length - 1));
                  if (Float.isFinite(floatValue)) {
                     return FloatBinaryTag.floatBinaryTag(floatValue);
                  }
                  break;
               case 'i':
                  return IntBinaryTag.intBinaryTag(Integer.parseInt(built.substring(0, length - 1)));
               case 'l':
                  return LongBinaryTag.longBinaryTag(Long.parseLong(built.substring(0, length - 1)));
               case 's':
                  return ShortBinaryTag.shortBinaryTag(Short.parseShort(built.substring(0, length - 1)));
            }
         } catch (NumberFormatException var9) {
         }
      } else if (noLongerNumericAt == -1) {
         try {
            return IntBinaryTag.intBinaryTag(Integer.parseInt(built));
         } catch (NumberFormatException var11) {
            if (built.indexOf(46) != -1) {
               try {
                  return DoubleBinaryTag.doubleBinaryTag(Double.parseDouble(built));
               } catch (NumberFormatException var10) {
               }
            }
         }
      }

      if (built.equalsIgnoreCase("true")) {
         return ByteBinaryTag.ONE;
      } else {
         return (BinaryTag)(built.equalsIgnoreCase("false") ? ByteBinaryTag.ZERO : StringBinaryTag.stringBinaryTag(built));
      }
   }

   private boolean separatorOrCompleteWith(final char endCharacter) throws StringTagParseException {
      if (this.buffer.takeIf(endCharacter)) {
         return true;
      } else {
         this.buffer.expect(',');
         return this.buffer.takeIf(endCharacter);
      }
   }

   private static String unescape(final String withEscapes) {
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

   public void legacy(final boolean acceptLegacy) {
      this.acceptLegacy = acceptLegacy;
   }
}
