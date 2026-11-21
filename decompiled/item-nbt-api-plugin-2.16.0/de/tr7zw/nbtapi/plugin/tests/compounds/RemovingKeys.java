package de.tr7zw.nbtapi.plugin.tests.compounds;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;

public class RemovingKeys implements Test {
   private static final String STRING_TEST_KEY = "stringTest";
   private static final String INT_TEST_KEY = "intTest";
   private static final String DOUBLE_TEST_KEY = "doubleTest";
   private static final String BOOLEAN_TEST_KEY = "booleanTest";
   private static final String SHORT_TEST_KEY = "shortTest";
   private static final String BYTE_TEST_KEY = "byteTest";
   private static final String FLOAT_TEST_KEY = "floatTest";
   private static final String LONG_TEST_KEY = "longTest";
   private static final String INTARRAY_TEST_KEY = "intarrayTest";
   private static final String BYTEARRAY_TEST_KEY = "bytearrayTest";
   private static final String STRING_TEST_VALUE = "TestString";
   private static final int INT_TEST_VALUE = 42;
   private static final double DOUBLE_TEST_VALUE = 1.5;
   private static final boolean BOOLEAN_TEST_VALUE = true;
   private static final short SHORT_TEST_VALUE = 64;
   private static final byte BYTE_TEST_VALUE = 7;
   private static final float FLOAT_TEST_VALUE = 13.37F;
   private static final long LONG_TEST_VALUE = 2147483689L;
   private static final int[] INTARRAY_TEST_VALUE = new int[]{1337, 42, 69};
   private static final byte[] BYTEARRAY_TEST_VALUE = new byte[]{8, 7, 3, 2};

   @Override
   public void test() throws Exception {
      NBTContainer comp = new NBTContainer();
      comp.setString("stringTest", "TestString");
      comp.setInteger("intTest", 42);
      comp.setDouble("doubleTest", 1.5);
      comp.setBoolean("booleanTest", true);
      comp.setByte("byteTest", (byte)7);
      comp.setShort("shortTest", (short)64);
      comp.setLong("longTest", 2147483689L);
      comp.setFloat("floatTest", 13.37F);
      comp.setIntArray("intarrayTest", INTARRAY_TEST_VALUE);
      comp.setByteArray("bytearrayTest", BYTEARRAY_TEST_VALUE);
      if (comp.getKeys().size() != 10) {
         throw new NbtApiException("Key amount did not match after setting keys!");
      } else {
         comp.setString("stringTest", null);
         comp.setInteger("intTest", null);
         comp.setDouble("doubleTest", null);
         comp.setBoolean("booleanTest", null);
         comp.setByte("byteTest", null);
         comp.setShort("shortTest", null);
         comp.setLong("longTest", null);
         comp.setFloat("floatTest", null);
         comp.setIntArray("intarrayTest", null);
         comp.setByteArray("bytearrayTest", null);
         if (comp.getKeys().size() != 0) {
            throw new NbtApiException("Keys where not removed using the setter with null!");
         }
      }
   }
}
