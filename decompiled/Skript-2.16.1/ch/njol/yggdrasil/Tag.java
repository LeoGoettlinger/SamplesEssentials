package ch.njol.yggdrasil;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public enum Tag {
   T_NULL(0, null, "null"),
   T_BYTE(1, byte.class, "byte"),
   T_SHORT(2, short.class, "short"),
   T_INT(3, int.class, "int"),
   T_LONG(4, long.class, "long"),
   T_FLOAT(8, float.class, "float"),
   T_DOUBLE(9, double.class, "double"),
   T_CHAR(14, char.class, "char"),
   T_BOOLEAN(15, boolean.class, "boolean"),
   T_BYTE_OBJ,
   T_SHORT_OBJ,
   T_INT_OBJ,
   T_LONG_OBJ,
   T_FLOAT_OBJ,
   T_DOUBLE_OBJ,
   T_CHAR_OBJ,
   T_BOOLEAN_OBJ,
   T_STRING(32, String.class, "string"),
   T_ARRAY(48, null, "array"),
   T_ENUM(64, null, "enum"),
   T_CLASS(65, Class.class, "class"),
   T_OBJECT(128, Object.class, "object"),
   T_REFERENCE(255, null, "reference");

   public static final int MIN_PRIMITIVE;
   public static final int MAX_PRIMITIVE;
   public static final int MIN_WRAPPER;
   public static final int MAX_WRAPPER;
   public final byte tag;
   @Nullable
   public final Class<?> type;
   public final String name;
   private static final Map<Class<?>, Tag> types = new HashMap<>();
   private static final Tag[] byID = new Tag[256];
   private static final Map<String, Tag> byName = new HashMap<>();
   private static final HashMap<Class<?>, Tag> wrapperTypes;

   private Tag(int tag, @Nullable Class<?> type, String name) {
      assert 0 <= tag && tag <= 255 : tag;

      this.tag = (byte)tag;
      this.type = type;
      this.name = name;
   }

   @Override
   public String toString() {
      return this.name;
   }

   public boolean isPrimitive() {
      return MIN_PRIMITIVE <= this.tag && this.tag <= MAX_PRIMITIVE;
   }

   @Nullable
   public Tag getPrimitive() {
      if (!this.isWrapper()) {
         assert false;

         return null;
      } else {
         return byID[this.tag - MIN_WRAPPER + MIN_PRIMITIVE];
      }
   }

   public boolean isWrapper() {
      return MIN_WRAPPER <= this.tag && this.tag <= MAX_WRAPPER;
   }

   public Tag getWrapper() {
      if (!this.isPrimitive()) {
         assert false;

         return T_NULL;
      } else {
         return byID[this.tag - MIN_PRIMITIVE + MIN_WRAPPER];
      }
   }

   public static Tag getType(@Nullable Class<?> type) {
      if (type == null) {
         return T_NULL;
      } else {
         Tag t = types.get(type);
         if (t != null) {
            return t;
         } else {
            return type.isArray() ? T_ARRAY : (!Enum.class.isAssignableFrom(type) && !PseudoEnum.class.isAssignableFrom(type) ? T_OBJECT : T_ENUM);
         }
      }
   }

   @Nullable
   public static Tag byID(byte tag) {
      return byID[tag & 255];
   }

   @Nullable
   public static Tag byID(int tag) {
      return byID[tag];
   }

   @Nullable
   public static Tag byName(String name) {
      return byName.get(name);
   }

   public static boolean isWrapper(Class<?> type) {
      return wrapperTypes.containsKey(type);
   }

   public static Tag getPrimitiveFromWrapper(Class<?> wrapper) {
      Tag tag = wrapperTypes.get(wrapper);
      if (tag == null) {
         assert false : wrapper;

         return T_NULL;
      } else {
         return tag;
      }
   }

   public static Class<?> getWrapperClass(Class<?> primitive) {
      assert primitive.isPrimitive();

      Tag tag = types.get(primitive);
      if (tag == null) {
         assert false : primitive;

         return Object.class;
      } else {
         Class<?> wrapper = tag.getWrapper().type;

         assert wrapper != null : tag;

         return wrapper;
      }
   }

   // $VF: Failed to inline enum fields
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   static {
      T_BYTE_OBJ = new Tag(16 + T_BYTE.tag, Byte.class, "Byte");
      T_SHORT_OBJ = new Tag(16 + T_SHORT.tag, Short.class, "Short");
      T_INT_OBJ = new Tag(16 + T_INT.tag, Integer.class, "Integer");
      T_LONG_OBJ = new Tag(16 + T_LONG.tag, Long.class, "Long");
      T_FLOAT_OBJ = new Tag(16 + T_FLOAT.tag, Float.class, "Float");
      T_DOUBLE_OBJ = new Tag(16 + T_DOUBLE.tag, Double.class, "Double");
      T_CHAR_OBJ = new Tag(16 + T_CHAR.tag, Character.class, "Character");
      T_BOOLEAN_OBJ = new Tag(16 + T_BOOLEAN.tag, Boolean.class, "Boolean");
      MIN_PRIMITIVE = T_BYTE.tag;
      MAX_PRIMITIVE = T_BOOLEAN.tag;
      MIN_WRAPPER = T_BYTE_OBJ.tag;
      MAX_WRAPPER = T_BOOLEAN_OBJ.tag;

      for (Tag tag : values()) {
         types.put(tag.type, tag);
         byID[tag.tag & 255] = tag;
         byName.put(tag.name, tag);
      }

      wrapperTypes = new HashMap<>();
      wrapperTypes.put(Byte.class, T_BYTE);
      wrapperTypes.put(Short.class, T_SHORT);
      wrapperTypes.put(Integer.class, T_INT);
      wrapperTypes.put(Long.class, T_LONG);
      wrapperTypes.put(Float.class, T_FLOAT);
      wrapperTypes.put(Double.class, T_DOUBLE);
      wrapperTypes.put(Character.class, T_CHAR);
      wrapperTypes.put(Boolean.class, T_BOOLEAN);
   }
}
