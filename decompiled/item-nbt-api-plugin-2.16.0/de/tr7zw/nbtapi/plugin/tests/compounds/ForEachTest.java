package de.tr7zw.nbtapi.plugin.tests.compounds;

import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.plugin.tests.Test;
import java.util.ListIterator;

public class ForEachTest implements Test {
   @Override
   public void test() throws Exception {
      NBTContainer comp = new NBTContainer();
      NBTCompoundList compList = comp.getCompoundList("testkey");
      if (compList != null) {
         compList.addCompound().setInteger("id", 1);
         compList.addCompound().setInteger("id", 2);
         compList.addCompound().setInteger("id", 3);
         int count = 0;

         for (ReadWriteNBT listComp : compList) {
            count++;
         }

         if (count != compList.size()) {
            throw new NbtApiException("For loop did not get all Entries!");
         }

         count = 0;

         ListIterator<ReadWriteNBT> lit;
         for (lit = compList.listIterator(); lit.hasNext(); count++) {
            lit.next();
         }

         if (count != compList.size()) {
            throw new NbtApiException("ListIterator did not get all Entries!");
         }

         for (count = 0; lit.hasPrevious(); count++) {
            lit.previous();
         }

         if (count != compList.size()) {
            throw new NbtApiException("ListIterator previous did not get all Entries!");
         }
      }
   }
}
