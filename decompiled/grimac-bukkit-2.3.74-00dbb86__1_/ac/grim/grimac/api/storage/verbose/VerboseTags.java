package ac.grim.grimac.api.storage.verbose;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApiStatus.Experimental
public final class VerboseTags {
   public static final int ENUM_NULL = 0;
   private static final int MC_XZ_BITS = 26;
   private static final int MC_XZ_MASK = 67108863;
   private static final ConcurrentMap<String, VerboseTags.Tag> TAGS = new ConcurrentHashMap<>();

   private VerboseTags() {
   }

   public static void register(@NotNull String name, @NotNull List<VerboseSchema.TypeTag> wire, @NotNull VerboseTags.Renderer renderer) {
      if (name.isEmpty()) {
         throw new IllegalArgumentException("name");
      } else if (wire.isEmpty()) {
         throw new IllegalArgumentException("tag " + name + " declares no wire fields");
      } else {
         VerboseTags.Tag tag = new VerboseTags.Tag(name, List.copyOf(wire), renderer);
         VerboseTags.Tag previous = TAGS.putIfAbsent(name, tag);
         if (previous != null && !previous.wire().equals(tag.wire())) {
            throw new IllegalStateException(
               "verbose tag " + name + " already registered with wire " + previous.wire() + "; register a new tag name instead of changing " + tag.wire()
            );
         }
      }
   }

   public static void registerEnum(@NotNull String name, @NotNull Enum<?>[] values) {
      register(name, List.of(VerboseSchema.TypeTag.VI), (in, ctx, out, fmt) -> out.append(enumName(in.rvi(), values)));
   }

   public static void registerEnumLower(@NotNull String name, @NotNull Enum<?>[] values) {
      register(name, List.of(VerboseSchema.TypeTag.VI), (in, ctx, out, fmt) -> out.append(enumName(in.rvi(), values).toLowerCase(Locale.ROOT)));
   }

   public static int enumId(@Nullable Enum<?> value) {
      return value == null ? 0 : value.ordinal() + 1;
   }

   @NotNull
   public static String enumName(int encoded, @NotNull Enum<?>[] values) {
      if (encoded == 0) {
         return "null";
      } else {
         int ordinal = encoded - 1;
         return ordinal >= 0 && ordinal < values.length ? values[ordinal].name() : "unknown(" + encoded + ")";
      }
   }

   @Nullable
   static VerboseTags.Tag get(@NotNull String name) {
      return TAGS.get(name);
   }

   static long packMcBlockXZ(int x, int z) {
      return (long)(x & 67108863) << 26 | z & 67108863;
   }

   static int unpackMcBlockX(long xz) {
      return signExtend26((int)(xz >>> 26));
   }

   static int unpackMcBlockZ(long xz) {
      return signExtend26((int)xz & 67108863);
   }

   private static int signExtend26(int value) {
      return value << 6 >> 6;
   }

   private static void registerPrimitive(@NotNull String name, @NotNull VerboseSchema.TypeTag wire, @NotNull VerboseTags.Renderer renderer) {
      register(name, List.of(wire), renderer);
   }

   private static void appendFormatted(@NotNull StringBuilder out, @Nullable String fmt, double value) {
      if (fmt == null) {
         out.append(value);
      } else {
         out.append(String.format(Locale.ROOT, fmt, value));
      }
   }

   private static void appendFormatted(@NotNull StringBuilder out, @Nullable String fmt, float value) {
      if (fmt == null) {
         out.append(value);
      } else {
         out.append(String.format(Locale.ROOT, fmt, value));
      }
   }

   private static void appendFormatted(@NotNull StringBuilder out, @Nullable String fmt, int value) {
      if (fmt == null) {
         out.append(value);
      } else {
         out.append(String.format(Locale.ROOT, fmt, value));
      }
   }

   private static void appendFormatted(@NotNull StringBuilder out, @Nullable String fmt, long value) {
      if (fmt == null) {
         out.append(value);
      } else {
         out.append(String.format(Locale.ROOT, fmt, value));
      }
   }

   static {
      registerPrimitive("f64", VerboseSchema.TypeTag.F64, (in, ctx, out, fmt) -> appendFormatted(out, fmt, in.rf64()));
      registerPrimitive("f32", VerboseSchema.TypeTag.F32, (in, ctx, out, fmt) -> appendFormatted(out, fmt, in.rf32()));
      registerPrimitive("uint", VerboseSchema.TypeTag.VI, (in, ctx, out, fmt) -> appendFormatted(out, fmt, in.rvi()));
      registerPrimitive("sint", VerboseSchema.TypeTag.ZZ, (in, ctx, out, fmt) -> appendFormatted(out, fmt, in.rzz()));
      registerPrimitive("ulong", VerboseSchema.TypeTag.VL, (in, ctx, out, fmt) -> appendFormatted(out, fmt, in.rvl()));
      registerPrimitive("bool", VerboseSchema.TypeTag.BOOL, (in, ctx, out, fmt) -> out.append(in.rbool()));
      registerPrimitive("str", VerboseSchema.TypeTag.STR, (in, ctx, out, fmt) -> out.append(in.rstr()));
      register("mcpos", List.of(VerboseSchema.TypeTag.VL, VerboseSchema.TypeTag.ZZ), (in, ctx, out, fmt) -> {
         long xz = in.rvl();
         int y = in.rzz();
         out.append(unpackMcBlockX(xz)).append(", ").append(y).append(", ").append(unpackMcBlockZ(xz));
      });
      register(
         "cursor",
         List.of(VerboseSchema.TypeTag.F32, VerboseSchema.TypeTag.F32, VerboseSchema.TypeTag.F32),
         (in, ctx, out, fmt) -> out.append(in.rf32()).append(", ").append(in.rf32()).append(", ").append(in.rf32())
      );
      register("slong", List.of(VerboseSchema.TypeTag.ZZ, VerboseSchema.TypeTag.ZZ), (in, ctx, out, fmt) -> {
         int high = in.rzz();
         int low = in.rzz();
         appendFormatted(out, fmt, (long)high << 32 | low & 4294967295L);
      });
   }

   @FunctionalInterface
   public interface Renderer {
      void render(@NotNull VerboseBuf var1, @NotNull VerboseRenderContext var2, @NotNull StringBuilder var3, @Nullable String var4);
   }

   record Tag(@NotNull String name, @NotNull List<VerboseSchema.TypeTag> wire, @NotNull VerboseTags.Renderer renderer) {
   }
}
