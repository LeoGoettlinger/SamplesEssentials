package de.tr7zw.nbtapi.plugin.tests.compounds;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTType;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import de.tr7zw.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.nbtapi.plugin.tests.Test;
import java.util.Arrays;
import java.util.UUID;

public class ListTest implements Test {
   @Override
   public void test() throws Exception {
      ReadWriteNBT comp = NBT.createNBTObject();
      ReadWriteNBTList<String> list = comp.getStringList("testlist");
      list.add("test1");
      list.add("test2");
      list.add("test3");
      list.add("test4");
      list.set(2, "test42");
      list.remove(1);
      if (list.get(1).equals("test42") && list.size() == 3) {
         ReadWriteNBTCompoundList taglist = comp.getCompoundList("complist");
         ReadWriteNBT lcomp = taglist.addCompound();
         lcomp.setDouble("double1", 0.3333);
         lcomp.setInteger("int1", 42);
         lcomp.setString("test1", "test1");
         lcomp.setString("test2", "test2");
         lcomp.removeKey("test1");
         ReadWriteNBT subsubcomp = lcomp.getOrCreateCompound("listsubkey");
         subsubcomp.setString("deep", "String");
         subsubcomp.getCompoundList("deeplist").addCompound().setString("test", "test");
         subsubcomp.getCompoundList("clonelist").addCompound(comp);
         ReadWriteNBTCompoundList var12 = null;
         ReadWriteNBT var14 = null;
         ReadWriteNBT var16 = null;
         var12 = comp.getCompoundList("complist");
         if (var12.size() == 1) {
            var14 = var12.get(0);
            ReadWriteNBT sublist = var14.getCompound("listsubkey");
            if (var14.getKeys().size() != 4) {
               throw new NbtApiException("Wrong key amount in Taglist (" + var14.getKeys().size() + ")!");
            } else if (sublist == null) {
               throw new NbtApiException("The sublist is null! The Item-NBT-API may not work!");
            } else if (var14.getDouble("double1") != 0.3333
               || var14.getInteger("int1") != 42
               || !var14.getString("test2").equals("test2")
               || var14.hasTag("test1")) {
               throw new NbtApiException("One key in the Taglist changed! The Item-NBT-API may not work!");
            } else if (!"String".equals(sublist.getString("deep"))) {
               throw new NbtApiException("The Compound nested in the listcompound was not correct! The Item-NBT-API may not work!");
            } else if (sublist.getType("deep") != NBTType.NBTTagString) {
               throw new NbtApiException("The nested key's type wasn't correct! The Item-NBT-API may not work!");
            } else if (sublist.getType("deeplist") != NBTType.NBTTagList) {
               throw new NbtApiException("The nested list's type wasn't correct '" + sublist.getType("deeplist") + "'! The Item-NBT-API may not work!");
            } else if (comp.getListType("complist") != NBTType.NBTTagCompound) {
               throw new NbtApiException("complist had the wrong type(" + comp.getListType("complist") + ")! The Item-NBT-API may not work!");
            } else {
               ReadWriteNBTList<Integer> intlist = comp.getIntegerList("inttest");
               intlist.add(42);
               intlist.add(69);
               if (intlist.size() != 2 || intlist.get(0) != 42 || intlist.get(1) != 69) {
                  throw new NbtApiException("IntList is not correct! " + Arrays.toString(intlist.toArray(new Integer[0])));
               } else if (comp.getListType("inttest") != NBTType.NBTTagInt) {
                  throw new NbtApiException("inttest had the wrong type! The Item-NBT-API may not work!");
               } else {
                  ReadWriteNBTList<Double> doublelist = comp.getDoubleList("doubletest");
                  doublelist.add(42.23);
                  doublelist.add(69.69);
                  if (doublelist.size() != 2 || doublelist.get(0) != 42.23 || doublelist.get(1) != 69.69) {
                     throw new NbtApiException("DoubleList is not correct! " + Arrays.toString(doublelist.toArray(new Double[0])));
                  } else if (comp.getListType("doubletest") != NBTType.NBTTagDouble) {
                     throw new NbtApiException("doubletest had the wrong type! The Item-NBT-API may not work!");
                  } else {
                     ReadWriteNBTList<Float> floatlist = comp.getFloatList("floattest");
                     floatlist.add(42.23F);
                     floatlist.add(69.69F);
                     if (floatlist.size() != 2 || floatlist.get(0) != 42.23F || floatlist.get(1) != 69.69F) {
                        throw new NbtApiException("FloatList is not correct! " + Arrays.toString(floatlist.toArray(new Float[0])));
                     } else if (comp.getListType("floattest") != NBTType.NBTTagFloat) {
                        throw new NbtApiException("floattest had the wrong type! The Item-NBT-API may not work!");
                     } else {
                        ReadWriteNBTList<Long> longlist = comp.getLongList("longtest");
                        longlist.add(1241234124124L);
                        longlist.add(1231454321312L);
                        if (longlist.size() != 2 || longlist.get(0) != 1241234124124L || longlist.get(1) != 1231454321312L) {
                           throw new NbtApiException("LongList is not correct! " + Arrays.toString(longlist.toArray(new Long[0])));
                        } else if (comp.getListType("longtest") != NBTType.NBTTagLong) {
                           throw new NbtApiException("longtest had the wrong type! The Item-NBT-API may not work!");
                        } else {
                           ReadWriteNBTList<int[]> intArrayList = comp.getIntArrayList("intatest");
                           intArrayList.add(new int[]{1, 2, 3});
                           intArrayList.add(new int[]{4, 2, 0});
                           if (intArrayList.size() != 2
                              || !Arrays.equals(new int[]{1, 2, 3}, intArrayList.get(0))
                              || !Arrays.equals(new int[]{4, 2, 0}, intArrayList.get(1))) {
                              throw new NbtApiException("IntArrayList is not correct! " + intArrayList);
                           } else if (comp.getListType("intatest") != NBTType.NBTTagIntArray) {
                              throw new NbtApiException("intatest had the wrong type! The Item-NBT-API may not work!");
                           } else {
                              ReadWriteNBTList<UUID> uuidList = comp.getUUIDList("uuidtest");
                              uuidList.add(UUID.fromString("fce0323d-7f50-4317-9720-5f6b14cf78ea"));
                              uuidList.add(UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6"));
                              if (uuidList.size() != 2
                                 || !UUID.fromString("fce0323d-7f50-4317-9720-5f6b14cf78ea").equals(uuidList.get(0))
                                 || !UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6").equals(uuidList.get(1))) {
                                 throw new NbtApiException("UUIDList is not correct! " + uuidList);
                              } else if (comp.getListType("uuidtest") != NBTType.NBTTagIntArray) {
                                 throw new NbtApiException("uuidtest had the wrong type! The Item-NBT-API may not work!");
                              }
                           }
                        }
                     }
                  }
               }
            }
         } else {
            throw new NbtApiException("Taglist is empty! The Item-NBT-API may not work!");
         }
      } else {
         throw new NbtApiException("The String-list did not match what it should have looked like.");
      }
   }
}
