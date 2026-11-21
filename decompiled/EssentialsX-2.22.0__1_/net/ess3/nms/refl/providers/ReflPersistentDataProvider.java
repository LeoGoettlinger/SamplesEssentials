package net.ess3.nms.refl.providers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.Locale;
import net.ess3.nms.refl.ReflUtil;
import net.ess3.provider.PersistentDataProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

@ProviderData(
   description = "Reflection Persistent Data Container Provider"
)
public class ReflPersistentDataProvider implements PersistentDataProvider {
   private static final String PDC_ROOT_TAG = "PublicBukkitValues";
   private static final String ROOT_TAG = "tag";
   private final String namespace;
   private final MethodHandle itemHandleGetterHandle;
   private final MethodHandle getTagHandle;
   private final MethodHandle tagSetterHandle;
   private final MethodHandle newCompoundHandle;
   private final MethodHandle getCompoundHandle;
   private final MethodHandle setCompoundHandle;
   private final MethodHandle setStringHandle;
   private final MethodHandle removeHandle;
   private final MethodHandle getStringHandle;

   public ReflPersistentDataProvider(Plugin plugin) {
      this.namespace = plugin.getName().toLowerCase(Locale.ROOT);
      MethodHandle itemHandleGetterHandle = null;
      MethodHandle getTagHandle = null;
      MethodHandle tagSetterHandle = null;
      MethodHandle newCompoundHandle = null;
      MethodHandle getCompoundHandle = null;
      MethodHandle setCompoundHandle = null;
      MethodHandle setStringHandle = null;
      MethodHandle removeHandle = null;
      MethodHandle getStringHandle = null;

      try {
         Lookup lookup = MethodHandles.lookup();
         Field handleGetter = ReflUtil.getOBCClass("inventory.CraftItemStack").getDeclaredField("handle");
         handleGetter.setAccessible(true);
         itemHandleGetterHandle = lookup.unreflectGetter(handleGetter);
         Field tagSetter = ReflUtil.getNMSClass("ItemStack").getDeclaredField("tag");
         tagSetter.setAccessible(true);
         tagSetterHandle = lookup.unreflectSetter(tagSetter);
         getTagHandle = lookup.findVirtual(ReflUtil.getNMSClass("ItemStack"), "getTag", MethodType.methodType(ReflUtil.getNMSClass("NBTTagCompound")));
         newCompoundHandle = lookup.findConstructor(ReflUtil.getNMSClass("NBTTagCompound"), MethodType.methodType(void.class));
         getCompoundHandle = lookup.findVirtual(
            ReflUtil.getNMSClass("NBTTagCompound"), "getCompound", MethodType.methodType(ReflUtil.getNMSClass("NBTTagCompound"), String.class)
         );
         setCompoundHandle = lookup.findVirtual(
            ReflUtil.getNMSClass("NBTTagCompound"), "set", MethodType.methodType(void.class, String.class, ReflUtil.getNMSClass("NBTBase"))
         );
         setStringHandle = lookup.findVirtual(
            ReflUtil.getNMSClass("NBTTagCompound"), "setString", MethodType.methodType(void.class, String.class, String.class)
         );
         removeHandle = lookup.findVirtual(ReflUtil.getNMSClass("NBTTagCompound"), "remove", MethodType.methodType(void.class, String.class));
         getStringHandle = lookup.findVirtual(ReflUtil.getNMSClass("NBTTagCompound"), "getString", MethodType.methodType(String.class, String.class));
      } catch (IllegalAccessException | NoSuchMethodException | NoSuchFieldException var14) {
         var14.printStackTrace();
      }

      this.itemHandleGetterHandle = itemHandleGetterHandle;
      this.getTagHandle = getTagHandle;
      this.tagSetterHandle = tagSetterHandle;
      this.newCompoundHandle = newCompoundHandle;
      this.getCompoundHandle = getCompoundHandle;
      this.setCompoundHandle = setCompoundHandle;
      this.setStringHandle = setStringHandle;
      this.removeHandle = removeHandle;
      this.getStringHandle = getStringHandle;
   }

   private String getPersistentString(ItemStack itemStack, String key) throws Throwable {
      Object nmsItem = (Object)this.itemHandleGetterHandle.invoke((ItemStack)itemStack);
      Object itemRootTag = (Object)this.getTagHandle.invoke((Object)nmsItem);
      if (itemRootTag == null) {
         return null;
      } else {
         Object tagCompound = (Object)this.getCompoundHandle.invoke((Object)itemRootTag, (String)"tag");
         Object publicBukkitValuesCompound = (Object)this.getCompoundHandle.invoke((Object)tagCompound, (String)"PublicBukkitValues");
         return (String)this.getStringHandle.invoke((Object)publicBukkitValuesCompound, (String)key);
      }
   }

   private void setPersistentString(ItemStack itemStack, String key, String value) throws Throwable {
      Object nmsItem = (Object)this.itemHandleGetterHandle.invoke((ItemStack)itemStack);
      Object itemRootTag = (Object)this.getTagHandle.invoke((Object)nmsItem);
      if (itemRootTag == null) {
         itemRootTag = (Object)this.newCompoundHandle.invoke();
         this.tagSetterHandle.invoke((Object)nmsItem, (Object)itemRootTag);
      }

      Object tagCompound = (Object)this.getCompoundHandle.invoke((Object)itemRootTag, (String)"tag");
      Object publicBukkitValuesCompound = (Object)this.getCompoundHandle.invoke((Object)tagCompound, (String)"PublicBukkitValues");
      if (value == null) {
         this.removeHandle.invoke((Object)publicBukkitValuesCompound, (String)key);
      } else {
         this.setStringHandle.invoke((Object)publicBukkitValuesCompound, (String)key, (String)value);
      }

      this.setCompoundHandle.invoke((Object)tagCompound, (String)"PublicBukkitValues", (Object)publicBukkitValuesCompound);
      this.setCompoundHandle.invoke((Object)itemRootTag, (String)"tag", (Object)tagCompound);
   }

   @Override
   public void set(ItemStack itemStack, String key, String value) {
      try {
         this.setPersistentString(itemStack, this.namespace + ":" + key, value);
      } catch (Throwable var5) {
      }
   }

   @Override
   public String getString(ItemStack itemStack, String key) {
      try {
         return this.getPersistentString(itemStack, this.namespace + ":" + key);
      } catch (Throwable var4) {
         return null;
      }
   }

   @Override
   public void remove(ItemStack itemStack, String key) {
      try {
         this.setPersistentString(itemStack, this.namespace + ":" + key, null);
      } catch (Throwable var4) {
      }
   }
}
