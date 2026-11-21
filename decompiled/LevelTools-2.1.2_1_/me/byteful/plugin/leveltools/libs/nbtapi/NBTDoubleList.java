package me.byteful.plugin.leveltools.libs.nbtapi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.nmsmappings.ClassWrapper;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.nmsmappings.ReflectionMethod;

public class NBTDoubleList extends NBTList<Double> {
   protected NBTDoubleList(NBTCompound owner, String name, NBTType type, Object list) {
      super(owner, name, type, list);
   }

   protected Object asTag(Double object) {
      try {
         Constructor<?> con = ClassWrapper.NMS_NBTTAGDOUBLE.getClazz().getDeclaredConstructor(double.class);
         con.setAccessible(true);
         return con.newInstance(object);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException var3) {
         throw new NbtApiException("Error while wrapping the Object " + object + " to it's NMS object!", var3);
      }
   }

   public Double get(int index) {
      try {
         Object obj = ReflectionMethod.LIST_GET.run(this.listObject, index);
         return Double.valueOf(obj.toString());
      } catch (NumberFormatException var3) {
         return 0.0;
      } catch (Exception var4) {
         throw new NbtApiException(var4);
      }
   }
}
