package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.representer;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.DumpSettings;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.RepresentToNode;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.FlowStyle;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.NonPrintableStyle;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.scanner.StreamReader;

public class StandardRepresenter extends BaseRepresenter {
   public static final Pattern MULTILINE_PATTERN = Pattern.compile("\n|\u0085");
   protected Map<Class<? extends Object>, Tag> classTags;
   protected DumpSettings settings;

   public StandardRepresenter(DumpSettings settings) {
      this.defaultFlowStyle = settings.getDefaultFlowStyle();
      this.defaultScalarStyle = settings.getDefaultScalarStyle();
      this.nullRepresenter = new StandardRepresenter.RepresentNull();
      this.representers.put(String.class, new StandardRepresenter.RepresentString());
      this.representers.put(Boolean.class, new StandardRepresenter.RepresentBoolean());
      this.representers.put(Character.class, new StandardRepresenter.RepresentString());
      this.representers.put(UUID.class, new StandardRepresenter.RepresentUuid());
      this.representers.put(Optional.class, new StandardRepresenter.RepresentOptional());
      this.representers.put(byte[].class, new StandardRepresenter.RepresentByteArray());
      RepresentToNode primitiveArray = new StandardRepresenter.RepresentPrimitiveArray();
      this.representers.put(short[].class, primitiveArray);
      this.representers.put(int[].class, primitiveArray);
      this.representers.put(long[].class, primitiveArray);
      this.representers.put(float[].class, primitiveArray);
      this.representers.put(double[].class, primitiveArray);
      this.representers.put(char[].class, primitiveArray);
      this.representers.put(boolean[].class, primitiveArray);
      this.parentClassRepresenters.put(Number.class, new StandardRepresenter.RepresentNumber());
      this.parentClassRepresenters.put(List.class, new StandardRepresenter.RepresentList());
      this.parentClassRepresenters.put(Map.class, new StandardRepresenter.RepresentMap());
      this.parentClassRepresenters.put(Set.class, new StandardRepresenter.RepresentSet());
      this.parentClassRepresenters.put(Iterator.class, new StandardRepresenter.RepresentIterator());
      this.parentClassRepresenters.put(new Object[0].getClass(), new StandardRepresenter.RepresentArray());
      this.parentClassRepresenters.put(Enum.class, new StandardRepresenter.RepresentEnum());
      this.classTags = new HashMap<>();
      this.settings = settings;
   }

   protected Tag getTag(Class<?> clazz, Tag defaultTag) {
      return this.classTags.getOrDefault(clazz, defaultTag);
   }

   @Deprecated
   public Tag addClassTag(Class<? extends Object> clazz, Tag tag) {
      if (tag == null) {
         throw new NullPointerException("Tag must be provided.");
      } else {
         return this.classTags.put(clazz, tag);
      }
   }

   private static class IteratorWrapper implements Iterable<Object> {
      private final Iterator<Object> iter;

      public IteratorWrapper(Iterator<Object> iter) {
         this.iter = iter;
      }

      @Override
      public Iterator<Object> iterator() {
         return this.iter;
      }
   }

   public class RepresentArray implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         Object[] array = (Object[])data;
         List<Object> list = Arrays.asList(array);
         return StandardRepresenter.this.representSequence(Tag.SEQ, list, StandardRepresenter.this.settings.getDefaultFlowStyle());
      }
   }

   public class RepresentBoolean implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         String value;
         if (Boolean.TRUE.equals(data)) {
            value = "true";
         } else {
            value = "false";
         }

         return StandardRepresenter.this.representScalar(Tag.BOOL, value);
      }
   }

   public class RepresentByteArray implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         return StandardRepresenter.this.representScalar(Tag.BINARY, Base64.getEncoder().encodeToString((byte[])data), ScalarStyle.LITERAL);
      }
   }

   public class RepresentEnum implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         Tag tag = new Tag((Class<? extends Object>)data.getClass());
         return StandardRepresenter.this.representScalar(StandardRepresenter.this.getTag(data.getClass(), tag), ((Enum)data).name());
      }
   }

   public class RepresentIterator implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         Iterator<Object> iter = (Iterator<Object>)data;
         return StandardRepresenter.this.representSequence(
            StandardRepresenter.this.getTag(data.getClass(), Tag.SEQ),
            new StandardRepresenter.IteratorWrapper(iter),
            StandardRepresenter.this.settings.getDefaultFlowStyle()
         );
      }
   }

   public class RepresentList implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         return StandardRepresenter.this.representSequence(
            StandardRepresenter.this.getTag(data.getClass(), Tag.SEQ), (List)data, StandardRepresenter.this.settings.getDefaultFlowStyle()
         );
      }
   }

   public class RepresentMap implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         return StandardRepresenter.this.representMapping(
            StandardRepresenter.this.getTag(data.getClass(), Tag.MAP), (Map<?, ?>)data, StandardRepresenter.this.settings.getDefaultFlowStyle()
         );
      }
   }

   protected class RepresentNull implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         return StandardRepresenter.this.representScalar(Tag.NULL, "null");
      }
   }

   public class RepresentNumber implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         Tag tag;
         String value;
         if (!(data instanceof Byte) && !(data instanceof Short) && !(data instanceof Integer) && !(data instanceof Long) && !(data instanceof BigInteger)) {
            Number number = (Number)data;
            tag = Tag.FLOAT;
            if (number.equals(Double.NaN) || number.equals(Float.NaN)) {
               value = ".nan";
            } else if (number.equals(Double.POSITIVE_INFINITY) || number.equals(Float.POSITIVE_INFINITY)) {
               value = ".inf";
            } else if (!number.equals(Double.NEGATIVE_INFINITY) && !number.equals(Float.NEGATIVE_INFINITY)) {
               value = number.toString();
            } else {
               value = "-.inf";
            }
         } else {
            tag = Tag.INT;
            value = data.toString();
         }

         return StandardRepresenter.this.representScalar(StandardRepresenter.this.getTag(data.getClass(), tag), value);
      }
   }

   public class RepresentOptional implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         Optional<?> opt = (Optional<?>)data;
         if (opt.isPresent()) {
            Node node = StandardRepresenter.this.represent(opt.get());
            node.setTag(new Tag(Optional.class));
            return node;
         } else {
            return StandardRepresenter.this.representScalar(Tag.NULL, "null");
         }
      }
   }

   public class RepresentPrimitiveArray implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         Class<?> type = data.getClass().getComponentType();
         FlowStyle style = StandardRepresenter.this.settings.getDefaultFlowStyle();
         if (byte.class == type) {
            return StandardRepresenter.this.representSequence(Tag.SEQ, this.asByteList(data), style);
         } else if (short.class == type) {
            return StandardRepresenter.this.representSequence(Tag.SEQ, this.asShortList(data), style);
         } else if (int.class == type) {
            return StandardRepresenter.this.representSequence(Tag.SEQ, this.asIntList(data), style);
         } else if (long.class == type) {
            return StandardRepresenter.this.representSequence(Tag.SEQ, this.asLongList(data), style);
         } else if (float.class == type) {
            return StandardRepresenter.this.representSequence(Tag.SEQ, this.asFloatList(data), style);
         } else if (double.class == type) {
            return StandardRepresenter.this.representSequence(Tag.SEQ, this.asDoubleList(data), style);
         } else if (char.class == type) {
            return StandardRepresenter.this.representSequence(Tag.SEQ, this.asCharList(data), style);
         } else if (boolean.class == type) {
            return StandardRepresenter.this.representSequence(Tag.SEQ, this.asBooleanList(data), style);
         } else {
            throw new YamlEngineException("Unexpected primitive '" + type.getCanonicalName() + "'");
         }
      }

      private List<Byte> asByteList(Object in) {
         byte[] array = (byte[])in;
         List<Byte> list = new ArrayList<>(array.length);

         for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
         }

         return list;
      }

      private List<Short> asShortList(Object in) {
         short[] array = (short[])in;
         List<Short> list = new ArrayList<>(array.length);

         for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
         }

         return list;
      }

      private List<Integer> asIntList(Object in) {
         int[] array = (int[])in;
         List<Integer> list = new ArrayList<>(array.length);

         for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
         }

         return list;
      }

      private List<Long> asLongList(Object in) {
         long[] array = (long[])in;
         List<Long> list = new ArrayList<>(array.length);

         for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
         }

         return list;
      }

      private List<Float> asFloatList(Object in) {
         float[] array = (float[])in;
         List<Float> list = new ArrayList<>(array.length);

         for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
         }

         return list;
      }

      private List<Double> asDoubleList(Object in) {
         double[] array = (double[])in;
         List<Double> list = new ArrayList<>(array.length);

         for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
         }

         return list;
      }

      private List<Character> asCharList(Object in) {
         char[] array = (char[])in;
         List<Character> list = new ArrayList<>(array.length);

         for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
         }

         return list;
      }

      private List<Boolean> asBooleanList(Object in) {
         boolean[] array = (boolean[])in;
         List<Boolean> list = new ArrayList<>(array.length);

         for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
         }

         return list;
      }
   }

   public class RepresentSet implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         Map<Object, Object> value = new LinkedHashMap<>();

         for (Object key : (Set)data) {
            value.put(key, null);
         }

         return StandardRepresenter.this.representMapping(
            StandardRepresenter.this.getTag(data.getClass(), Tag.SET), value, StandardRepresenter.this.settings.getDefaultFlowStyle()
         );
      }
   }

   public class RepresentString implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         Tag tag = Tag.STR;
         ScalarStyle style = ScalarStyle.PLAIN;
         String value = data.toString();
         if (StandardRepresenter.this.settings.getNonPrintableStyle() == NonPrintableStyle.BINARY && !StreamReader.isPrintable(value)) {
            tag = Tag.BINARY;
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            String checkValue = new String(bytes, StandardCharsets.UTF_8);
            if (!checkValue.equals(value)) {
               throw new YamlEngineException("invalid string value has occurred");
            }

            value = Base64.getEncoder().encodeToString(bytes);
            style = ScalarStyle.LITERAL;
         }

         if (StandardRepresenter.this.defaultScalarStyle == ScalarStyle.PLAIN && StandardRepresenter.MULTILINE_PATTERN.matcher(value).find()) {
            style = ScalarStyle.LITERAL;
         }

         return StandardRepresenter.this.representScalar(tag, value, style);
      }
   }

   public class RepresentUuid implements RepresentToNode {
      @Override
      public Node representData(Object data) {
         return StandardRepresenter.this.representScalar(StandardRepresenter.this.getTag(data.getClass(), new Tag(UUID.class)), data.toString());
      }
   }
}
