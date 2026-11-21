package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Cow.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CowData extends EntityData<Cow> {
   private static final boolean VARIANTS_ENABLED;
   private static final Object[] VARIANTS;
   private static final Class<Cow> COW_CLASS;
   @Nullable
   private static final Method getVariantMethod;
   @Nullable
   private static final Method setVariantMethod;
   @Nullable
   private Object variant = null;

   public CowData() {
   }

   public CowData(@Nullable Object variant) {
      this.variant = variant;
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (VARIANTS_ENABLED && exprs[0] != null) {
         this.variant = exprs[0].getSingle();
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends Cow> entityClass, @Nullable Cow cow) {
      if (cow != null && VARIANTS_ENABLED) {
         this.variant = this.getVariant(cow);
      }

      return true;
   }

   public void set(Cow cow) {
      if (VARIANTS_ENABLED) {
         Variant variant = (Variant)this.variant;
         if (variant == null) {
            variant = CollectionUtils.getRandom(VARIANTS);
         }

         assert variant != null;

         this.setVariant(cow, variant);
      }
   }

   protected boolean match(Cow cow) {
      return this.variant == null || this.getVariant(cow) == this.variant;
   }

   @Override
   public Class<Cow> getType() {
      return COW_CLASS;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new CowData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.variant);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof CowData other ? this.variant == other.variant : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof CowData other ? this.dataMatch(this.variant, other.variant) : false;
   }

   public void setVariant(Cow cow) {
      this.setVariant(cow, this.variant);
   }

   public void setVariant(Cow cow, Object object) {
      if (VARIANTS_ENABLED && setVariantMethod != null) {
         Entity entity = (Entity)COW_CLASS.cast(cow);

         try {
            setVariantMethod.invoke(entity, (Variant)object);
         } catch (InvocationTargetException | IllegalAccessException var5) {
            throw new RuntimeException(var5);
         }
      }
   }

   @Nullable
   public Object getVariant(Cow cow) {
      if (VARIANTS_ENABLED && getVariantMethod != null) {
         Entity entity = (Entity)COW_CLASS.cast(cow);

         try {
            return getVariantMethod.invoke(entity);
         } catch (InvocationTargetException | IllegalAccessException var4) {
            throw new RuntimeException(var4);
         }
      } else {
         return null;
      }
   }

   static {
      ClassInfo<?> cowVariantClassInfo = BukkitUtils.getRegistryClassInfo("org.bukkit.entity.Cow$Variant", "COW_VARIANT", "cowvariant", "cow variants");
      if (cowVariantClassInfo == null) {
         cowVariantClassInfo = new ClassInfo<>(CowData.CowVariantDummy.class, "cowvariant");
      }

      Classes.registerClass(
         cowVariantClassInfo.user("cow ?variants?")
            .name("Cow Variant")
            .description("Represents the variant of a cow entity.", "NOTE: Minecraft namespaces are supported, ex: 'minecraft:warm'.")
            .since("2.12")
            .requiredPlugins("Minecraft 1.21.5+")
            .documentationId("CowVariant")
      );
      Class<Cow> cowClass = null;

      try {
         cowClass = (Class<Cow>)Class.forName("org.bukkit.entity.Cow");
      } catch (Exception var4) {
      }

      COW_CLASS = cowClass;
      register(CowData.class, "cow", COW_CLASS, 0, new String[]{"cow"});
      if (Skript.classExists("org.bukkit.entity.Cow$Variant")) {
         VARIANTS_ENABLED = true;
         VARIANTS = Iterators.toArray(Classes.getExactClassInfo(Variant.class).getSupplier().get(), Variant.class);

         try {
            getVariantMethod = COW_CLASS.getDeclaredMethod("getVariant");
            setVariantMethod = COW_CLASS.getDeclaredMethod("setVariant", Variant.class);
         } catch (Exception var3) {
            throw new RuntimeException("Could not retrieve get/set variant methods for Cow.", var3);
         }
      } else {
         VARIANTS_ENABLED = false;
         VARIANTS = null;
         getVariantMethod = null;
         setVariantMethod = null;
      }
   }

   public static class CowVariantDummy {
   }
}
