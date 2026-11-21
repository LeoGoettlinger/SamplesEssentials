package org.skriptlang.skript.bukkit.entity.entitydata;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.Objects;
import org.bukkit.entity.Nautilus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NautilusData extends EntityData<Nautilus> {
   private Kleenean isTamed = Kleenean.UNKNOWN;

   public static void register() {
      EntityData.register(NautilusData.class, "nautilus", Nautilus.class, 0, "nautilus");
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (parseResult.hasTag("tamed")) {
         this.isTamed = Kleenean.TRUE;
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends Nautilus> entityClass, @Nullable Nautilus nautilus) {
      if (nautilus != null) {
         this.isTamed = Kleenean.get(nautilus.isTamed());
      }

      return true;
   }

   public void set(Nautilus nautilus) {
      nautilus.setTamed(this.isTamed.isTrue());
   }

   protected boolean match(Nautilus nautilus) {
      return this.kleeneanMatch(this.isTamed, nautilus.isTamed());
   }

   @Override
   public Class<? extends Nautilus> getType() {
      return Nautilus.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new NautilusData();
   }

   @Override
   protected int hashCode_i() {
      return Objects.hashCode(this.isTamed);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof NautilusData other ? this.isTamed == other.isTamed : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return entityData instanceof NautilusData other ? this.kleeneanMatch(this.isTamed, other.isTamed) : false;
   }
}
