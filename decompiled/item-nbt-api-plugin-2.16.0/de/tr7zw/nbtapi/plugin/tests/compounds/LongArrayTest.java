package de.tr7zw.nbtapi.plugin.tests.compounds;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import java.util.Arrays;

public class LongArrayTest implements Test {
   @Override
   public void test() throws Exception {
      NBTContainer comp = new NBTContainer();
      comp.setLongArray("test", new long[]{1L, 2L, 3L, 4L, Long.MAX_VALUE});
      if (!comp.hasTag("test")) {
         throw new NbtApiException("Wasn't able to check a key! The Item-NBT-API may not work!");
      } else if (!Arrays.equals(comp.getLongArray("test"), new long[]{1L, 2L, 3L, 4L, Long.MAX_VALUE})) {
         throw new NbtApiException("The long key does not equal the original value! The Item-NBT-API may not work!");
      }
   }
}
