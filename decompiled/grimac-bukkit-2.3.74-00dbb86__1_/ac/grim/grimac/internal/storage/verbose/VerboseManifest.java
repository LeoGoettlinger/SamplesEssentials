package ac.grim.grimac.internal.storage.verbose;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

@ApiStatus.Internal
public final class VerboseManifest {
   public static final int FORMAT_VERSION = 1;
   public static final int FLAVOR_UNKNOWN = 0;
   public static final int FLAVOR_V2_PUBLIC = 1;
   public static final int FLAVOR_V3_PREMIUM = 2;

   private VerboseManifest() {
   }

   public static byte[] textOnly(int flavor) {
      return encode(flavor, Map.of());
   }

   public static byte[] encode(int flavor, @NotNull Map<Integer, Integer> checkCodecVersions) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(4 + checkCodecVersions.size() * 3);
      out.write(1);
      out.write(flavor & 0xFF);
      writeUVarInt(out, 0);
      writeUVarInt(out, checkCodecVersions.size());
      int previousCheckId = 0;

      for (Entry<Integer, Integer> entry : new TreeMap<>(checkCodecVersions).entrySet()) {
         int checkId = entry.getKey();
         if (checkId < previousCheckId) {
            throw new IllegalArgumentException("check ids must be non-decreasing");
         }

         int codecVersion = entry.getValue();
         if (codecVersion < 1) {
            throw new IllegalArgumentException("codec version must be positive");
         }

         writeUVarInt(out, checkId - previousCheckId);
         writeUVarInt(out, codecVersion);
         previousCheckId = checkId;
      }

      return out.toByteArray();
   }

   @NotNull
   public static VerboseManifest.Decoded decode(byte[] bytes) {
      VerboseManifest.Reader in = new VerboseManifest.Reader(bytes);
      int format = in.readU8();
      if (format != 1) {
         return new VerboseManifest.Decoded(format, 0, Map.of(), false);
      } else {
         int flavor = in.readU8();
         int featureBits = in.readUVarInt();
         if (featureBits != 0) {
            return new VerboseManifest.Decoded(format, flavor, Map.of(), false);
         } else {
            int count = in.readUVarInt();
            Map<Integer, Integer> codecs = new LinkedHashMap<>(Math.max(1, count));
            int checkId = 0;

            for (int i = 0; i < count; i++) {
               checkId += in.readUVarInt();
               codecs.put(checkId, in.readUVarInt());
            }

            return new VerboseManifest.Decoded(format, flavor, Map.copyOf(codecs), true);
         }
      }
   }

   private static void writeUVarInt(@NotNull ByteArrayOutputStream out, int value) {
      if (value < 0) {
         throw new IllegalArgumentException("uvarint cannot encode negative value " + value);
      } else {
         int v;
         for (v = value; (v & -128) != 0; v >>>= 7) {
            out.write(v & 127 | 128);
         }

         out.write(v);
      }
   }

   public record Decoded(int formatVersion, int flavor, @NotNull Map<Integer, Integer> checkCodecVersions, boolean supported) {
      public int codecVersionOrText(int checkId) {
         return this.checkCodecVersions.getOrDefault(checkId, 0);
      }
   }

   private static final class Reader {
      private final byte[] data;
      private int offset;

      private Reader(byte[] data) {
         this.data = data;
      }

      private int readU8() {
         if (this.offset >= this.data.length) {
            throw new IllegalArgumentException("truncated verbose manifest");
         } else {
            return this.data[this.offset++] & 0xFF;
         }
      }

      private int readUVarInt() {
         int value = 0;

         for (int shift = 0; shift < 35; shift += 7) {
            int b = this.readU8();
            value |= (b & 127) << shift;
            if ((b & 128) == 0) {
               return value;
            }
         }

         throw new IllegalArgumentException("verbose manifest varint is too long");
      }
   }
}
