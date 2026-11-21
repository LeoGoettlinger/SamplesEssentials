package de.tr7zw.nbtapi.plugin.tests.compounds;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.plugin.tests.Test;

public class SubCompoundsTest implements Test {
   private static final String COMP_TEST_KEY = "componentTest";
   private static final String STRING_TEST_KEY = "stringTest";
   private static final String INT_TEST_KEY = "intTest";
   private static final String DOUBLE_TEST_KEY = "doubleTest";
   private static final String BOOLEAN_TEST_KEY = "booleanTest";
   private static final String STRING_TEST_VALUE = "TestString";
   private static final int INT_TEST_VALUE = 42;
   private static final double DOUBLE_TEST_VALUE = 1.5;
   private static final boolean BOOLEAN_TEST_VALUE = true;

   @Override
   public void test() throws Exception {
      ReadWriteNBT cont = NBT.createNBTObject();
      ReadWriteNBT comp = cont.getOrCreateCompound("componentTest");
      comp.setString("stringTest", "TestString2");
      comp.setInteger("intTest", 84);
      comp.setDouble("doubleTest", 3.0);
      if (cont.getCompound("invalide") != null) {
         throw new NbtApiException("An invalide compound did not return null!");
      } else {
         ReadWriteNBT var3 = null;
         var3 = cont.getCompound("componentTest");
         if (var3 == null) {
            throw new NbtApiException("Wasn't able to get the NBTCompound!");
         } else if (!((ReadWriteNBT)var3).hasTag("stringTest")) {
            throw new NbtApiException("Wasn't able to check a compound key!");
         } else if ("TestString2".equals(((ReadWriteNBT)var3).getString("stringTest"))
            && ((ReadWriteNBT)var3).getInteger("intTest") == 84
            && ((ReadWriteNBT)var3).getDouble("doubleTest") == 3.0
            && !((ReadWriteNBT)var3).getBoolean("booleanTest")) {
            ((ReadWriteNBT)var3).getOrCreateCompound("someName").setString("test", "abc");
            if (!((ReadWriteNBT)var3).getOrCreateCompound("someName").getString("test").equals("abc")) {
               throw new NbtApiException("getOrCreateCompound did not return the same compound!");
            }
         } else {
            throw new NbtApiException("One key does not equal the original compound value!");
         }
      }
   }
}
