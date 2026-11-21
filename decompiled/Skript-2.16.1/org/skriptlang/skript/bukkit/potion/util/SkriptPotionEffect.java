package org.skriptlang.skript.bukkit.potion.util;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;
import java.io.StreamCorruptedException;
import java.util.StringJoiner;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffect;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffects;
import org.skriptlang.skript.bukkit.potion.providers.PotionEffectProvider;

public class SkriptPotionEffect implements Cloneable, YggdrasilSerializable.YggdrasilExtendedSerializable {
   private PotionEffectType potionEffectType;
   private Integer duration = null;
   private Integer amplifier = null;
   private Boolean ambient = null;
   private Boolean particles = null;
   private Boolean icon = null;
   @Nullable
   private PotionEffect lastEffect;
   @Nullable
   private PotionEffectProvider<?> sourceProvider;

   public static SkriptPotionEffect fromType(PotionEffectType potionEffectType) {
      return new SkriptPotionEffect().potionEffectType(potionEffectType);
   }

   public static SkriptPotionEffect fromBukkitEffect(PotionEffect potionEffect) {
      return fromType(potionEffect.getType())
         .duration(potionEffect.getDuration())
         .amplifier(potionEffect.getAmplifier())
         .ambient(potionEffect.isAmbient())
         .particles(potionEffect.hasParticles())
         .icon(potionEffect.hasIcon());
   }

   public static SkriptPotionEffect fromBukkitEffect(PotionEffect potionEffect, PotionEffectProvider<?> source) {
      SkriptPotionEffect skriptPotionEffect = fromBukkitEffect(potionEffect);
      skriptPotionEffect.sourceProvider = source;
      return skriptPotionEffect;
   }

   public PotionEffectType potionEffectType() {
      return this.potionEffectType;
   }

   @Contract("_ -> this")
   public SkriptPotionEffect potionEffectType(PotionEffectType potionEffectType) {
      this.lastEffect = null;
      this.withSource(() -> this.potionEffectType = potionEffectType);
      return this;
   }

   public boolean infinite() {
      return this.duration != null && this.duration == -1;
   }

   @Contract("_ -> this")
   public SkriptPotionEffect infinite(boolean infinite) {
      return this.duration(infinite ? -1 : 600);
   }

   public int duration() {
      return this.duration == null ? 600 : this.duration;
   }

   @Contract("_ -> this")
   public SkriptPotionEffect duration(int duration) {
      this.lastEffect = null;
      this.withSource(() -> this.duration = duration);
      return this;
   }

   public int amplifier() {
      return this.amplifier == null ? 0 : this.amplifier;
   }

   @Contract("_ -> this")
   public SkriptPotionEffect amplifier(int amplifier) {
      this.lastEffect = null;
      this.withSource(() -> this.amplifier = amplifier);
      return this;
   }

   public boolean ambient() {
      return this.ambient == null ? false : this.ambient;
   }

   @Contract("_ -> this")
   public SkriptPotionEffect ambient(boolean ambient) {
      this.lastEffect = null;
      this.withSource(() -> this.ambient = ambient);
      return this;
   }

   public boolean particles() {
      return this.particles == null ? true : this.particles;
   }

   @Contract("_ -> this")
   public SkriptPotionEffect particles(boolean particles) {
      this.lastEffect = null;
      this.withSource(() -> this.particles = particles);
      return this;
   }

   public boolean icon() {
      return this.icon == null ? true : this.icon;
   }

   @Contract("_ -> this")
   public SkriptPotionEffect icon(boolean icon) {
      this.lastEffect = null;
      this.withSource(() -> this.icon = icon);
      return this;
   }

   public PotionEffect asBukkitPotionEffect() {
      if (this.lastEffect == null) {
         this.lastEffect = new PotionEffect(this.potionEffectType(), this.duration(), this.amplifier(), this.ambient(), this.particles(), this.icon());
      }

      return this.lastEffect;
   }

   @Override
   public String toString() {
      return this.toString(0);
   }

   public String toString(int flags) {
      StringJoiner joiner = new StringJoiner(" ");
      boolean infinite = this.infinite();
      if (infinite) {
         joiner.add("infinite");
      }

      if (this.ambient()) {
         joiner.add("ambient");
      }

      joiner.add("potion effect of").add(Classes.toString(this.potionEffectType())).add(String.valueOf(this.amplifier() + 1));
      if (!this.particles()) {
         joiner.add("without particles");
      }

      if (!this.icon()) {
         joiner.add("without an icon");
      }

      if (this.duration != null && !infinite) {
         joiner.add("for").add(new Timespan(Timespan.TimePeriod.TICK, this.duration()).toString());
      }

      return joiner.toString();
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else {
         return !(other instanceof SkriptPotionEffect otherPotion)
            ? false
            : this.potionEffectType().equals(otherPotion.potionEffectType())
               && this.duration() == otherPotion.duration()
               && this.amplifier() == otherPotion.amplifier()
               && this.ambient() == otherPotion.ambient()
               && this.particles() == otherPotion.particles()
               && this.icon() == otherPotion.icon();
      }
   }

   public boolean matchesQualities(PotionEffect potionEffect) {
      return this.potionEffectType() == potionEffect.getType()
         && (this.duration == null || this.duration() == potionEffect.getDuration())
         && (this.amplifier == null || this.amplifier() == potionEffect.getAmplifier())
         && (this.ambient == null || this.ambient() == potionEffect.isAmbient())
         && (this.particles == null || this.particles() == potionEffect.hasParticles())
         && (this.icon == null || this.icon() == potionEffect.hasIcon());
   }

   private void withSource(Runnable runnable) {
      if (this.sourceProvider == null) {
         runnable.run();
      } else {
         this.sourceProvider.mirrorEffectChanges(this, runnable);
      }
   }

   @Override
   public Fields serialize() {
      Fields fields = new Fields();
      fields.putObject("type", this.potionEffectType);
      fields.putObject("duration", this.duration);
      fields.putObject("amplifier", this.amplifier);
      fields.putObject("ambient", this.ambient);
      fields.putObject("particles", this.particles);
      fields.putObject("icon", this.icon);
      return fields;
   }

   @Override
   public void deserialize(@NotNull Fields fields) throws StreamCorruptedException {
      this.potionEffectType = fields.getObject("type", PotionEffectType.class);
      this.duration = fields.getObject("duration", Integer.class);
      this.amplifier = fields.getObject("amplifier", Integer.class);
      this.ambient = fields.getObject("ambient", Boolean.class);
      this.particles = fields.getObject("particles", Boolean.class);
      this.icon = fields.getObject("icon", Boolean.class);
   }

   public SkriptPotionEffect clone() {
      try {
         SkriptPotionEffect skriptPotionEffect = (SkriptPotionEffect)super.clone();
         skriptPotionEffect.sourceProvider = null;
         return skriptPotionEffect;
      } catch (CloneNotSupportedException var2) {
         throw new AssertionError();
      }
   }

   public static boolean isChangeable(Expression<? extends SkriptPotionEffect> expression) {
      if (!(expression instanceof ExprPotionEffects exprPotionEffects && exprPotionEffects.getState().includesHidden())
         && !(expression instanceof ExprPotionEffect exprPotionEffect && exprPotionEffect.getState().includesHidden())) {
         return true;
      } else {
         Skript.error("Hidden potion effects cannot be changed");
         return false;
      }
   }
}
