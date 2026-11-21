package com.sk89q.worldedit.util;

import com.google.common.base.Verify;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SideEffectSet {
   private static final SideEffectSet DEFAULT = new SideEffectSet(
      Arrays.stream(SideEffect.values()).collect(Collectors.toMap(Function.identity(), SideEffect::getDefaultValue))
   );
   private static final SideEffectSet NONE = new SideEffectSet(
      Arrays.stream(SideEffect.values()).collect(Collectors.toMap(Function.identity(), state -> SideEffect.State.OFF))
   );
   private final int sideEffectsBitmap;
   private final Set<SideEffect> sideEffectsToApply;

   private static int shift(SideEffect effect) {
      return effect.ordinal() * 2;
   }

   private static int computeSideEffectsBitmap(Map<SideEffect, SideEffect.State> sideEffects) {
      int sideEffectsBitmap = 0;

      for (SideEffect effect : SideEffect.values()) {
         SideEffect.State state = sideEffects.getOrDefault(effect, effect.getDefaultValue());
         sideEffectsBitmap |= state.ordinal() << shift(effect);
      }

      return sideEffectsBitmap;
   }

   public SideEffectSet(Map<SideEffect, SideEffect.State> sideEffects) {
      this(computeSideEffectsBitmap(sideEffects));
   }

   private SideEffectSet(int sideEffectsBitmap) {
      this.sideEffectsBitmap = sideEffectsBitmap;
      EnumSet<SideEffect> sideEffectsToApply = EnumSet.noneOf(SideEffect.class);

      for (SideEffect effect : SideEffect.values()) {
         if (this.shouldApply(effect)) {
            sideEffectsToApply.add(effect);
         }
      }

      this.sideEffectsToApply = Sets.immutableEnumSet(sideEffectsToApply);
   }

   public SideEffectSet with(SideEffect sideEffect) {
      return this.with(sideEffect, SideEffect.State.ON);
   }

   public SideEffectSet without(SideEffect sideEffect) {
      return this.with(sideEffect, SideEffect.State.OFF);
   }

   public SideEffectSet with(SideEffect sideEffect, SideEffect.State state) {
      int mask = 3 << shift(sideEffect);
      int newState = state.ordinal() << shift(sideEffect) & mask;
      int newBitmap = this.sideEffectsBitmap & ~mask | newState;
      return new SideEffectSet(newBitmap);
   }

   public boolean doesApplyAny() {
      return this.sideEffectsBitmap != 0;
   }

   public SideEffect.State getState(SideEffect effect) {
      return SideEffect.State.values()[this.getRawState(effect)];
   }

   private int getRawState(SideEffect effect) {
      return this.sideEffectsBitmap >> shift(effect) & 3;
   }

   public boolean shouldApply(SideEffect effect) {
      return this.getRawState(effect) != 0;
   }

   public Set<SideEffect> getSideEffectsToApply() {
      return this.sideEffectsToApply;
   }

   public static SideEffectSet defaults() {
      return DEFAULT;
   }

   public static SideEffectSet none() {
      return NONE;
   }

   public static SideEffectSet api() {
      return defaults().without(SideEffect.HISTORY);
   }

   static {
      Verify.verify(SideEffect.State.values().length == 3, "Implementation requires specifically 3 values in the SideEffect.State enum", new Object[0]);
      int maxEffects = 16;
      Verify.verify(SideEffect.values().length <= maxEffects, "Implementation requires less than " + maxEffects + " side effects", new Object[0]);
      Verify.verify(SideEffect.State.OFF.ordinal() == 0, "Implementation requires SideEffect.State.OFF to be the first value", new Object[0]);
   }
}
