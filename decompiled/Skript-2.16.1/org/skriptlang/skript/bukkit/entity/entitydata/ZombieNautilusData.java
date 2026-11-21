package org.skriptlang.skript.bukkit.entity.entitydata;

import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Objects;
import org.bukkit.Registry;
import org.bukkit.entity.ZombieNautilus;
import org.bukkit.entity.ZombieNautilus.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZombieNautilusData extends EntityData<ZombieNautilus> {
   private static Variant[] VARIANTS;
   private Kleenean isTamed = Kleenean.UNKNOWN;
   @Nullable
   private Variant variant = null;

   public static void register() {
      EntityData.register(ZombieNautilusData.class, "zombie nautilus", ZombieNautilus.class, 0, "zombie nautilus");
      Variables.yggdrasil.registerSingleClass(Variant.class, "ZombieNautilus.Variant");
      Registry<Variant> variantRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ZOMBIE_NAUTILUS_VARIANT);
      VARIANTS = variantRegistry.stream().toArray(Variant[]::new);
      Classes.registerClass(
         new RegistryClassInfo(Variant.class, variantRegistry, "zombienautilusvariant", "zombie nautilus variants")
            .user(new String[]{"zombie ?nautilus ?variants?"})
            .name("Zombie Nautilus Variant")
            .description("Represents the variant of a zombie nautilus.")
            .since("2.14")
            .documentationId("ZombieNautilusVariant")
      );
   }

   public ZombieNautilusData() {
   }

   public ZombieNautilusData(@Nullable Variant variant) {
      this.variant = variant;
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (parseResult.hasTag("tamed")) {
         this.isTamed = Kleenean.TRUE;
      }

      if (exprs[0] != null) {
         this.variant = (Variant)exprs[0].getSingle();
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends ZombieNautilus> entityClass, @Nullable ZombieNautilus zombieNautilus) {
      if (zombieNautilus != null) {
         this.isTamed = Kleenean.get(zombieNautilus.isTamed());
         this.variant = zombieNautilus.getVariant();
      }

      return true;
   }

   public void set(ZombieNautilus zombieNautilus) {
      zombieNautilus.setTamed(this.isTamed.isTrue());
      Variant variant = this.variant;
      if (variant == null) {
         variant = CollectionUtils.getRandom(VARIANTS);
      }

      assert variant != null;

      zombieNautilus.setVariant(variant);
   }

   protected boolean match(ZombieNautilus zombieNautilus) {
      return this.kleeneanMatch(this.isTamed, zombieNautilus.isTamed()) && this.dataMatch(this.variant, zombieNautilus.getVariant());
   }

   @Override
   public Class<? extends ZombieNautilus> getType() {
      return ZombieNautilus.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new ZombieNautilusData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hash(this.isTamed, this.variant);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return !(entityData instanceof ZombieNautilusData other) ? false : this.isTamed == other.isTamed && this.variant == other.variant;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return !(entityData instanceof ZombieNautilusData other)
         ? false
         : this.kleeneanMatch(this.isTamed, other.isTamed) && this.dataMatch(this.variant, other.variant);
   }
}
