package ac.grim.grimac.api.storage.verbose;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApiStatus.Experimental
public final class VerboseSchema {
   private static final String UTF_8 = "UTF-8";
   private static final boolean DRIFT_CHECKS = VerboseSchema.class.desiredAssertionStatus();
   private final int version;
   @NotNull
   private final List<VerboseSchema.Field> fields;
   private volatile byte[] layoutBytes;

   private VerboseSchema(int version, @NotNull List<VerboseSchema.Field> fields) {
      if (version < 1) {
         throw new IllegalArgumentException("version");
      } else {
         this.version = version;
         this.fields = List.copyOf(fields);
         if (this.fields.isEmpty()) {
            throw new IllegalArgumentException("schema must declare at least one field");
         }
      }
   }

   @NotNull
   public static VerboseSchema of(@NotNull String... declarations) {
      return of(1, declarations);
   }

   @NotNull
   public static VerboseSchema of(int version, @NotNull String... declarations) {
      if (declarations == null) {
         throw new IllegalArgumentException("declarations");
      } else {
         List<VerboseSchema.Field> fields = new ArrayList<>(declarations.length);

         for (String declaration : declarations) {
            parseDeclaration(fields, declaration);
         }

         return new VerboseSchema(version, fields);
      }
   }

   @NotNull
   public static VerboseSchema of(int version, @NotNull List<VerboseSchema.Field> fields) {
      return new VerboseSchema(version, fields);
   }

   @NotNull
   public static VerboseSchema fromLayoutBytes(@NotNull byte[] layout) {
      return new VerboseSchema(1, decodeLayout(layout).fields());
   }

   @NotNull
   public VerboseSchema withVersion(int version) {
      return new VerboseSchema(version, this.fields);
   }

   public int version() {
      return this.version;
   }

   @NotNull
   public List<VerboseSchema.Field> fields() {
      return this.fields;
   }

   @NotNull
   public byte[] layoutBytes() {
      byte[] cached = this.layoutBytes;
      if (cached == null) {
         cached = encodeLayout(this.fields);
         this.layoutBytes = cached;
      }

      return (byte[])cached.clone();
   }

   @NotNull
   public VerboseBuf write(@NotNull VerboseBuf out) {
      VerboseBuf cleared = out.clear();

      assert this.startDriftCheck(cleared);

      return cleared;
   }

   @NotNull
   public VerboseFormatter formatter() {
      return new VerboseSchema.SchemaFormatter(this.version, new VerboseSchema.Layout(this.fields));
   }

   @NotNull
   public static VerboseSchema.Layout decodeLayout(@NotNull byte[] layout) {
      if (layout == null) {
         throw new IllegalArgumentException("layout");
      } else {
         VerboseBuf in = VerboseBuf.wrap(layout);
         int count = in.rvi();
         List<VerboseSchema.Field> fields = new ArrayList<>(count);

         for (int i = 0; i < count; i++) {
            int nameLen = in.rvi();
            if (nameLen > 65535) {
               throw new IllegalArgumentException("field name exceeds 65535 bytes");
            }

            byte[] nameBytes = new byte[nameLen];

            for (int j = 0; j < nameLen; j++) {
               nameBytes[j] = (byte)in.readUnsignedByte();
            }

            VerboseSchema.TypeTag type = VerboseSchema.TypeTag.fromTag(in.readUnsignedByte());
            fields.add(new VerboseSchema.Field(utf8(nameBytes, 0, nameBytes.length), type));
         }

         String template = null;
         if (in.remaining() != 0) {
            template = in.rstr();
            if (in.remaining() != 0) {
               throw new IllegalArgumentException("layout has trailing bytes");
            }
         }

         return new VerboseSchema.Layout(fields, template);
      }
   }

   @NotNull
   private static byte[] encodeLayout(@NotNull List<VerboseSchema.Field> fields) {
      return encodeLayout(fields, null);
   }

   @NotNull
   public static byte[] encodeLayout(@NotNull List<VerboseSchema.Field> fields, @Nullable String template) {
      VerboseBuf out = new VerboseBuf(Math.max(8, fields.size() * 8));
      out.vi(fields.size());

      for (VerboseSchema.Field field : fields) {
         byte[] name = utf8(field.name());
         if (name.length > 65535) {
            throw new IllegalArgumentException("field name exceeds 65535 bytes");
         }

         out.vi(name.length);
         out.writeBytes(name);
         out.writeByte(field.type().tag());
      }

      if (template != null && !template.isEmpty()) {
         out.str(template);
      }

      return out.toByteArray();
   }

   private static void parseDeclaration(@NotNull List<VerboseSchema.Field> out, String declaration) {
      if (declaration == null) {
         throw new IllegalArgumentException("declaration");
      } else {
         int colon = declaration.indexOf(58);
         if (colon > 0 && colon != declaration.length() - 1 && declaration.indexOf(58, colon + 1) < 0) {
            String name = declaration.substring(0, colon).trim();
            String type = declaration.substring(colon + 1).trim().toLowerCase(Locale.ROOT);
            if (name.isEmpty()) {
               throw new IllegalArgumentException("field name");
            } else if ("pos".equals(type)) {
               out.add(new VerboseSchema.Field(name + ".x", VerboseSchema.TypeTag.F32));
               out.add(new VerboseSchema.Field(name + ".y", VerboseSchema.TypeTag.F32));
               out.add(new VerboseSchema.Field(name + ".z", VerboseSchema.TypeTag.F32));
            } else {
               out.add(new VerboseSchema.Field(name, VerboseSchema.TypeTag.fromName(type)));
            }
         } else {
            throw new IllegalArgumentException("verbose field declaration must be name:type: " + declaration);
         }
      }
   }

   @NotNull
   private static byte[] utf8(@NotNull String value) {
      try {
         return value.getBytes("UTF-8");
      } catch (UnsupportedEncodingException var2) {
         throw new AssertionError("UTF-8 unavailable", var2);
      }
   }

   @NotNull
   private static String utf8(@NotNull byte[] bytes, int offset, int length) {
      try {
         return new String(bytes, offset, length, "UTF-8");
      } catch (UnsupportedEncodingException var4) {
         throw new AssertionError("UTF-8 unavailable", var4);
      }
   }

   private boolean startDriftCheck(@NotNull VerboseBuf out) {
      if (!DRIFT_CHECKS) {
         return true;
      } else {
         VerboseSchema.DriftChecks.ACTIVE.set(new VerboseSchema.DriftGuard(out, this.fields));
         return true;
      }
   }

   static boolean recordWrite(@NotNull VerboseBuf out, @NotNull VerboseSchema.TypeTag type) {
      if (!DRIFT_CHECKS) {
         return true;
      } else {
         VerboseSchema.DriftGuard guard = VerboseSchema.DriftChecks.ACTIVE.get();
         if (guard != null && guard.out == out) {
            guard.record(type);
            return true;
         } else {
            return true;
         }
      }
   }

   static boolean completeWrite(@NotNull VerboseBuf out) {
      if (!DRIFT_CHECKS) {
         return true;
      } else {
         VerboseSchema.DriftGuard guard = VerboseSchema.DriftChecks.ACTIVE.get();
         if (guard != null && guard.out == out) {
            boolean var2;
            try {
               guard.complete();
               var2 = true;
            } finally {
               VerboseSchema.DriftChecks.ACTIVE.remove();
            }

            return var2;
         } else {
            return true;
         }
      }
   }

   private static void renderField(@NotNull VerboseSchema.Field field, @NotNull VerboseBuf in, @NotNull VerboseSink out) {
      out.key(field.name());
      switch (field.type()) {
         case F64:
            out.num(in.rf64());
            break;
         case F32:
            out.num(in.rf32());
            break;
         case VI:
         case ENUM:
            out.num(in.rvi());
            break;
         case ZZ:
            out.num(in.rzz());
            break;
         case VL:
            out.num(in.rvl());
            break;
         case BOOL:
            out.bool(in.rbool());
            break;
         case STR:
            out.text(in.rstr());
      }
   }

   private static final class DriftChecks {
      private static final ThreadLocal<VerboseSchema.DriftGuard> ACTIVE = new ThreadLocal<>();
   }

   private static final class DriftGuard {
      @NotNull
      private final VerboseBuf out;
      @NotNull
      private final List<VerboseSchema.Field> fields;
      private int written;

      private DriftGuard(@NotNull VerboseBuf out, @NotNull List<VerboseSchema.Field> fields) {
         this.out = out;
         this.fields = fields;
      }

      private void record(@NotNull VerboseSchema.TypeTag actual) {
         if (this.written >= this.fields.size()) {
            throw new AssertionError("verbose schema wrote extra " + actual.wireName() + " field after " + this.fields.size() + " declared fields");
         } else {
            VerboseSchema.Field expected = this.fields.get(this.written);
            if (!matches(expected.type(), actual)) {
               throw new AssertionError(
                  "verbose schema field "
                     + this.written
                     + " ("
                     + expected.name()
                     + ") declared "
                     + expected.type().wireName()
                     + " but wrote "
                     + actual.wireName()
               );
            } else {
               this.written++;
            }
         }
      }

      private void complete() {
         if (this.written != this.fields.size()) {
            throw new AssertionError("verbose schema wrote " + this.written + " fields but declared " + this.fields.size());
         }
      }

      private static boolean matches(@NotNull VerboseSchema.TypeTag expected, @NotNull VerboseSchema.TypeTag actual) {
         return expected == actual || expected == VerboseSchema.TypeTag.ENUM && actual == VerboseSchema.TypeTag.VI;
      }
   }

   public record Field(@NotNull String name, @NotNull VerboseSchema.TypeTag type) {
      public Field(@NotNull String name, @NotNull VerboseSchema.TypeTag type) {
         if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name");
         } else if (type == null) {
            throw new IllegalArgumentException("type");
         } else {
            this.name = name;
            this.type = type;
         }
      }
   }

   public record Layout(@NotNull List<VerboseSchema.Field> fields, @Nullable String template) {
      public Layout(@NotNull List<VerboseSchema.Field> fields, @Nullable String template) {
         fields = List.copyOf(fields);
         if (fields.isEmpty()) {
            throw new IllegalArgumentException("fields");
         } else {
            this.fields = fields;
            this.template = template;
         }
      }

      public Layout(@NotNull List<VerboseSchema.Field> fields) {
         this(fields, null);
      }

      @NotNull
      public byte[] layoutBytes() {
         return VerboseSchema.encodeLayout(this.fields, this.template);
      }
   }

   private static final class SchemaFormatter implements VerboseFormatter {
      private final int version;
      private final VerboseSchema.Layout layout;

      private SchemaFormatter(int version, @NotNull VerboseSchema.Layout layout) {
         this.version = version;
         this.layout = layout;
      }

      @Override
      public int version() {
         return this.version;
      }

      @Override
      public void render(@NotNull VerboseBuf in, @NotNull VerboseRenderContext ctx, @NotNull VerboseSink out) {
         for (VerboseSchema.Field field : this.layout.fields()) {
            VerboseSchema.renderField(field, in, out);
         }
      }
   }

   public static enum TypeTag {
      F64(1, "f64"),
      F32(2, "f32"),
      VI(3, "vi"),
      ZZ(4, "zz"),
      VL(5, "vl"),
      BOOL(6, "bool"),
      STR(7, "str"),
      ENUM(8, "enum");

      private final int tag;
      private final String wireName;

      private TypeTag(int tag, @NotNull String wireName) {
         this.tag = tag;
         this.wireName = wireName;
      }

      public int tag() {
         return this.tag;
      }

      @NotNull
      public String wireName() {
         return this.wireName;
      }

      @NotNull
      public static VerboseSchema.TypeTag fromTag(int tag) {
         for (VerboseSchema.TypeTag type : values()) {
            if (type.tag == tag) {
               return type;
            }
         }

         throw new IllegalArgumentException("unknown verbose type tag " + tag);
      }

      @NotNull
      public static VerboseSchema.TypeTag fromName(@NotNull String name) {
         for (VerboseSchema.TypeTag type : values()) {
            if (type.wireName.equals(name)) {
               return type;
            }
         }

         throw new IllegalArgumentException("unknown verbose type " + name);
      }
   }
}
