package de.tr7zw.nbtapi.plugin.tests.compounds;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.plugin.tests.Test;

public class GetterSetterTest implements Test {
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
      ReadWriteNBT comp = NBT.createNBTObject();
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
      if (!comp.hasTag("stringTest")) {
         throw new NbtApiException("Wasn't able to check a key! The Item-NBT-API may not work!");
      } else {
         int[] intArray = comp.getIntArray("intarrayTest");
         byte[] byteArray = comp.getByteArray("bytearrayTest");
         if (!"TestString".equals(comp.getString("stringTest"))
            || comp.getInteger("intTest") != 42
            || comp.getDouble("doubleTest") != 1.5
            || comp.getByte("byteTest") != 7
            || comp.getShort("shortTest") != 64
            || comp.getFloat("floatTest") != 13.37F
            || comp.getLong("longTest") != 2147483689L
            || intArray != null && intArray.length != INTARRAY_TEST_VALUE.length
            || byteArray != null && byteArray.length != BYTEARRAY_TEST_VALUE.length
            || !comp.getBoolean("booleanTest").equals(true)) {
            throw new NbtApiException("One key does not equal the original value! The Item-NBT-API may not work!");
         }
      }
   }
}
