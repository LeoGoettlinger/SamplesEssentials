package ch.njol.skript.entity;

import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import java.util.Objects;
import org.bukkit.Registry;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Cat.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CatData extends EntityData<Cat> {
   private static final Type[] TYPES;
   @Nullable
   private Type type = null;

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (exprs.length > 0 && exprs[0] != null) {
         this.type = (Type)exprs[0].getSingle();
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends Cat> entityClass, @Nullable Cat cat) {
      if (cat != null) {
         this.type = cat.getCatType();
      }

      return true;
   }

   public void set(Cat cat) {
      Type type = this.type;
      if (type == null) {
         type = CollectionUtils.getRandom(TYPES);
      }

      assert type != null;

      cat.setCatType(type);
   }

   protected boolean match(Cat cat) {
      return this.dataMatch(this.type, cat.getCatType());
   }

   @Override
   public Class<? extends Cat> getType() {
      return Cat.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new CatData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.type);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof CatData other ? this.type == other.type : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof CatData other ? this.dataMatch(this.type, other.type) : false;
   }

   static {
      ClassInfo<Type> catTypeClassInfo;
      if (BukkitUtils.registryExists("CAT_VARIANT")) {
         catTypeClassInfo = new RegistryClassInfo(Type.class, Registry.CAT_VARIANT, "cattype", "cat types");
      } else {
         catTypeClassInfo = new EnumClassInfo(Type.class, "cattype", "cat types");
      }

      Classes.registerClass(
         catTypeClassInfo.user("cat ?(type|race)s?")
            .name("Cat Type")
            .description("Represents the race/type of a cat entity.", "NOTE: Minecraft namespaces are supported, ex: 'minecraft:british_shorthair'.")
            .since("2.4")
            .requiredPlugins("Minecraft 1.14 or newer")
            .documentationId("CatType")
      );
      EntityData.register(CatData.class, "cat", Cat.class, "cat");
      TYPES = (Type[])Iterators.toArray(catTypeClassInfo.getSupplier().get(), Type.class);
   }
}
