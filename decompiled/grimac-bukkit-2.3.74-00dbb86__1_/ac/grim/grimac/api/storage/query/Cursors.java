package ac.grim.grimac.api.storage.query;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.nio.ByteBuffer;
import java.util.Base64;

@ApiStatus.Experimental
public final class Cursors {
   public static final byte SCHEMA_ORDERED_PAIR = 1;
   public static final byte SCHEMA_TYPED_PAIR = 2;
   public static final byte TYPE_STRING = 1;
   public static final byte TYPE_BINARY = 2;
   public static final byte TYPE_DOUBLE = 3;
   public static final byte TYPE_DATE = 4;
   public static final int MAX_TYPED_ORDERED_LEN = 65535;
   public static final int MAX_PAYLOAD_BYTES = 4096;

   private Cursors() {
   }

   @NotNull
   public static Cursor encode(long orderedKey, @NotNull byte[] idBytes) {
      if (idBytes.length > 255) {
         throw new IllegalArgumentException("id payload > 255 bytes: " + idBytes.length);
      } else {
         ByteBuffer buf = ByteBuffer.allocate(10 + idBytes.length);
         buf.put((byte)1);
         buf.putLong(orderedKey);
         buf.put((byte)idBytes.length);
         buf.put(idBytes);
         return new Cursor(Base64.getUrlEncoder().withoutPadding().encodeToString(buf.array()));
      }
   }

   @NotNull
   public static Cursor encodeTyped(byte typeTag, @NotNull byte[] orderedBytes, @NotNull byte[] idBytes) {
      if (!isKnownTypeTag(typeTag)) {
         throw new IllegalArgumentException("unknown typed-cursor type tag 0x" + Integer.toHexString(typeTag & 255));
      } else if (orderedBytes.length > 65535) {
         throw new IllegalArgumentException("typed ordered payload > 65535 bytes: " + orderedBytes.length);
      } else if (idBytes.length > 255) {
         throw new IllegalArgumentException("id payload > 255 bytes: " + idBytes.length);
      } else {
         int total = 5 + orderedBytes.length + idBytes.length;
         if (total > 4096) {
            throw new IllegalArgumentException("encoded typed cursor exceeds MAX_PAYLOAD_BYTES (4096): " + total);
         } else {
            ByteBuffer buf = ByteBuffer.allocate(5 + orderedBytes.length + idBytes.length);
            buf.put((byte)2);
            buf.put(typeTag);
            buf.putShort((short)orderedBytes.length);
            buf.put(orderedBytes);
            buf.put((byte)idBytes.length);
            buf.put(idBytes);
            return new Cursor(Base64.getUrlEncoder().withoutPadding().encodeToString(buf.array()));
         }
      }
   }

   @NotNull
   public static Cursors.Decoded decode(@NotNull Cursor cursor) {
      byte[] raw = base64(cursor);
      if (raw.length < 10) {
         throw new IllegalArgumentException("cursor payload too short (" + raw.length + " bytes)");
      } else {
         ByteBuffer buf = ByteBuffer.wrap(raw);
         byte tag = buf.get();
         if (tag != 1) {
            throw new IllegalArgumentException("unknown cursor schema tag: " + tag);
         } else {
            long orderedKey = buf.getLong();
            int idLen = Byte.toUnsignedInt(buf.get());
            if (buf.remaining() != idLen) {
               throw new IllegalArgumentException("cursor id length mismatch: header=" + idLen + " remaining=" + buf.remaining());
            } else {
               byte[] idBytes = new byte[idLen];
               buf.get(idBytes);
               return new Cursors.Decoded(orderedKey, idBytes);
            }
         }
      }
   }

   @NotNull
   public static Cursors.DecodedTyped decodeTyped(@NotNull Cursor cursor) {
      byte[] raw = base64(cursor);
      if (raw.length < 5) {
         throw new IllegalArgumentException("typed cursor payload too short (" + raw.length + " bytes)");
      } else {
         ByteBuffer buf = ByteBuffer.wrap(raw);
         byte schema = buf.get();
         if (schema != 2) {
            throw new IllegalArgumentException("expected typed-pair schema (0x02), got 0x" + Integer.toHexString(schema & 255));
         } else {
            byte typeTag = buf.get();
            if (!isKnownTypeTag(typeTag)) {
               throw new IllegalArgumentException("unknown typed-cursor type tag 0x" + Integer.toHexString(typeTag & 255));
            } else {
               int orderedLen = Short.toUnsignedInt(buf.getShort());
               if (buf.remaining() < orderedLen + 1) {
                  throw new IllegalArgumentException("typed cursor truncated: ordered=" + orderedLen + " remaining=" + buf.remaining());
               } else {
                  byte[] orderedBytes = new byte[orderedLen];
                  buf.get(orderedBytes);
                  int idLen = Byte.toUnsignedInt(buf.get());
                  if (buf.remaining() != idLen) {
                     throw new IllegalArgumentException("typed cursor id length mismatch: header=" + idLen + " remaining=" + buf.remaining());
                  } else {
                     byte[] idBytes = new byte[idLen];
                     buf.get(idBytes);
                     return new Cursors.DecodedTyped(typeTag, orderedBytes, idBytes);
                  }
               }
            }
         }
      }
   }

   public static byte peekSchema(@NotNull Cursor cursor) {
      byte[] raw = base64(cursor);
      if (raw.length < 1) {
         throw new IllegalArgumentException("empty cursor payload");
      } else {
         return raw[0];
      }
   }

   @NotNull
   private static byte[] base64(@NotNull Cursor cursor) {
      String token = cursor.token();
      if (token.length() * 3L / 4L > 4096L) {
         throw new IllegalArgumentException("cursor payload exceeds max 4096 bytes (token length " + token.length() + ")");
      } else {
         try {
            byte[] raw = Base64.getUrlDecoder().decode(token);
            if (raw.length > 4096) {
               throw new IllegalArgumentException("cursor payload exceeds max 4096 bytes: " + raw.length);
            } else {
               return raw;
            }
         } catch (IllegalArgumentException var4) {
            String safe = token.length() > 64 ? token.substring(0, 64) + "..." : token;
            throw new IllegalArgumentException("cursor is not valid base64-url: " + safe, var4);
         }
      }
   }

   private static boolean isKnownTypeTag(byte t) {
      return t == 1 || t == 2 || t == 3 || t == 4;
   }

   public record Decoded(long orderedKey, @NotNull byte[] idBytes) {
   }

   public record DecodedTyped(byte typeTag, @NotNull byte[] orderedBytes, @NotNull byte[] idBytes) {
   }
}
