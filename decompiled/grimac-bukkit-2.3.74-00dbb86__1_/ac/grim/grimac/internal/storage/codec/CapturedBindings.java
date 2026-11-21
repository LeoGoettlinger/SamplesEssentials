package ac.grim.grimac.internal.storage.codec;

import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.codec.FieldKind;
import ac.grim.grimac.api.storage.codec.MergeMode;
import ac.grim.grimac.api.storage.codec.SearchType;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public final class CapturedBindings {
   static final String RESOURCE = "/META-INF/grim/codec-bindings.tsv";
   private static final Map<String, CapturedBindings.Captured> BY_NAME = load();

   private CapturedBindings() {
   }

   @Nullable
   public static EncodeShape shape(@NotNull Class<?> recordType) {
      CapturedBindings.Captured c = BY_NAME.get(recordType.getName());
      return c == null ? null : c.shape();
   }

   @Nullable
   public static RecordLayout layout(@NotNull Class<?> recordType) {
      CapturedBindings.Captured c = BY_NAME.get(recordType.getName());
      return c == null ? null : c.layout();
   }

   public static int size() {
      return BY_NAME.size();
   }

   @NotNull
   private static Map<String, CapturedBindings.Captured> load() {
      Map<String, CapturedBindings.Captured> out = new ConcurrentHashMap<>();

      try {
         try (InputStream in = CapturedBindings.class.getResourceAsStream("/META-INF/grim/codec-bindings.tsv")) {
            if (in == null) {
               return out;
            }

            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
               parse(r, out);
            }
         }

         return out;
      } catch (IOException var9) {
         throw new IllegalStateException("failed to read captured codec bindings /META-INF/grim/codec-bindings.tsv", var9);
      }
   }

   private static void parse(@NotNull BufferedReader r, @NotNull Map<String, CapturedBindings.Captured> out) throws IOException {
      List<String> block = new ArrayList<>();

      String line;
      while ((line = r.readLine()) != null) {
         if (!line.isEmpty() && line.charAt(0) != '#') {
            block.add(line);
         } else if (!block.isEmpty()) {
            parseBlock(block, out);
            block.clear();
         }
      }

      if (!block.isEmpty()) {
         parseBlock(block, out);
      }
   }

   private static void parseBlock(@NotNull List<String> block, @NotNull Map<String, CapturedBindings.Captured> out) {
      String recordFqcn = null;
      int totalComponents = 0;
      int version = 1;
      Class<?>[] ctorParamTypes = new Class[0];
      String idField = null;
      String timestampField = null;
      List<String> partition = List.of();
      List<String> indexed = List.of();
      List<String> searchable = List.of();
      List<EncodeShape.FieldDef> fields = new ArrayList<>();
      List<String> accessorNames = new ArrayList<>();

      for (String l : block) {
         String[] t = l.split("\t", -1);
         String var16 = t[0];
         switch (var16) {
            case "R":
               recordFqcn = t[1];
               totalComponents = Integer.parseInt(t[2]);
               version = Integer.parseInt(t[3]);
               ctorParamTypes = resolveTypes(t[4]);
               break;
            case "S":
               idField = t[1];
               timestampField = t[2].isEmpty() ? null : t[2];
               partition = csv(t[3]);
               indexed = csv(t[4]);
               searchable = csv(t[5]);
               break;
            case "F":
               SearchType st = t[5].isEmpty() ? null : SearchType.valueOf(t[5]);
               fields.add(
                  new EncodeShape.FieldDef(
                     t[1],
                     resolveType(t[2]),
                     FieldKind.valueOf(t[3]),
                     Boolean.parseBoolean(t[4]),
                     st,
                     Integer.parseInt(t[6]),
                     Integer.parseInt(t[7]),
                     MergeMode.valueOf(t[8]),
                     Long.parseLong(t[9])
                  )
               );
               accessorNames.add(t[10]);
               break;
            default:
               throw new IllegalStateException("unexpected codec-binding line: " + l);
         }
      }

      if (recordFqcn == null) {
         throw new IllegalStateException("codec-binding block missing R line: " + block);
      } else {
         EncodeShape shape = new EncodeShape(idField, timestampField, partition, indexed, searchable, fields, totalComponents);
         RecordLayout layout = new RecordLayout(accessorNames.toArray(new String[0]), ctorParamTypes, version);
         out.put(recordFqcn, new CapturedBindings.Captured(shape, layout));
      }
   }

   @NotNull
   private static List<String> csv(@NotNull String s) {
      return s.isEmpty() ? List.of() : List.of(s.split(","));
   }

   @NotNull
   private static Class<?>[] resolveTypes(@NotNull String csv) {
      if (csv.isEmpty()) {
         return new Class[0];
      } else {
         String[] parts = csv.split(",");
         Class<?>[] types = new Class[parts.length];

         for (int i = 0; i < parts.length; i++) {
            types[i] = resolveType(parts[i]);
         }

         return types;
      }
   }

   @NotNull
   static Class<?> resolveType(@NotNull String name) {
      return switch (name) {
         case "int" -> {
            Class var15 = int.class;
            yield var15;
         }
         case "long" -> {
            Class var14 = long.class;
            yield var14;
         }
         case "double" -> {
            Class var13 = double.class;
            yield var13;
         }
         case "float" -> {
            Class var12 = float.class;
            yield var12;
         }
         case "boolean" -> {
            Class var11 = boolean.class;
            yield var11;
         }
         case "byte" -> {
            Class var10 = byte.class;
            yield var10;
         }
         case "short" -> {
            Class var9 = short.class;
            yield var9;
         }
         case "char" -> {
            Class var8 = char.class;
            yield var8;
         }
         case "byte[]" -> {
            Class<byte[]> var7 = byte[].class;
            yield var7;
         }
         case "float[]" -> {
            Class<float[]> var6 = float[].class;
            yield var6;
         }
         default -> {
            Class var3;
            try {
               var3 = Class.forName(name);
            } catch (ClassNotFoundException var5) {
               throw new IllegalStateException("captured codec binding references missing type: " + name, var5);
            }

            yield var3;
         }
      };
   }

   private record Captured(@NotNull EncodeShape shape, @NotNull RecordLayout layout) {
   }
}
