package me.byteful.plugin.leveltools.libs.nbtapi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.nmsmappings.ClassWrapper;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.nmsmappings.ReflectionMethod;

public class NBTIntegerList extends NBTList<Integer> {
   protected NBTIntegerList(NBTCompound owner, String name, NBTType type, Object list) {
      super(owner, name, type, list);
   }

   protected Object asTag(Integer object) {
      try {
         Constructor<?> con = ClassWrapper.NMS_NBTTAGINT.getClazz().getDeclaredConstructor(int.class);
         con.setAccessible(true);
         return con.newInstance(object);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var3) {
         throw new NbtApiException("Error while wrapping the Object " + object + " to it's NMS object!", var3);
      }
   }

   public Integer get(int index) {
      try {
         Object obj = ReflectionMethod.LIST_GET.run(this.listObject, index);
         return Integer.valueOf(obj.toString());
      } catch (NumberFormatException var3) {
         return 0;
      } catch (Exception var4) {
         throw new NbtApiException(var4);
      }
   }
}
