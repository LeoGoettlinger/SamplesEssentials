package de.tr7zw.nbtapi.plugin.tests.compounds;

import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GsonTest implements Test {
   private static final String JSON_TEST_KEY = "jsonTest";
   private static final String STRING_TEST_VALUE = "TestString";
   private static final int INT_TEST_VALUE = 42;
   private static final double DOUBLE_TEST_VALUE = 1.5;
   private static final boolean BOOLEAN_TEST_VALUE = true;

   @Override
   public void test() throws Exception {
      if (MinecraftVersion.hasGsonSupport()) {
         try {
            ItemStack item = new ItemStack(Material.STONE, 1);
            NBTItem nbtItem = new NBTItem(item);
            nbtItem.setObject("jsonTest", new GsonTest.SimpleJsonTestObject());
            if (!nbtItem.hasTag("jsonTest")) {
               throw new NbtApiException("Wasn't able to find JSON key! The Item-NBT-API may not work with Json serialization/deserialization!");
            } else {
               GsonTest.SimpleJsonTestObject simpleObject = nbtItem.getObject("jsonTest", GsonTest.SimpleJsonTestObject.class);
               if (simpleObject == null) {
                  throw new NbtApiException("Wasn't able to check JSON key! The Item-NBT-API may not work with Json serialization/deserialization!");
               } else if (!"TestString".equals(simpleObject.getTestString())
                  || simpleObject.getTestInteger() != 42
                  || simpleObject.getTestDouble() != 1.5
                  || !simpleObject.isTestBoolean()) {
                  throw new NbtApiException(
                     "One key does not equal the original value in JSON! The Item-NBT-API may not work with Json serialization/deserialization!"
                  );
               }
            }
         } catch (Exception var4) {
            throw new NbtApiException("Exception during Gson check!", var4);
         }
      }
   }

   public static class SimpleJsonTestObject {
      private String testString = "TestString";
      private int testInteger = 42;
      private double testDouble = 1.5;
      private boolean testBoolean = true;

      public String getTestString() {
         return this.testString;
      }

      public void setTestString(String testString) {
         this.testString = testString;
      }

      public int getTestInteger() {
         return this.testInteger;
      }

      public void setTestInteger(int testInteger) {
         this.testInteger = testInteger;
      }

      public double getTestDouble() {
         return this.testDouble;
      }

      public void setTestDouble(double testDouble) {
         this.testDouble = testDouble;
      }

      public boolean isTestBoolean() {
         return this.testBoolean;
      }

      public void setTestBoolean(boolean testBoolean) {
         this.testBoolean = testBoolean;
      }
   }
}
