package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;

class SILSMS<K, V> extends SILS<K, V> {
   static final LocalCacheFactory FACTORY = SILSMS::new;
   protected static final VarHandle MAXIMUM;
   protected static final VarHandle WEIGHTED_SIZE;
   volatile long maximum;
   volatile long weightedSize;
   long windowMaximum;
   long windowWeightedSize;
   long mainProtectedMaximum;
   long mainProtectedWeightedSize;
   double stepSize;
   long adjustment;
   long hitsInSample;
   long missesInSample;
   double previousSampleHitRate;
   final FrequencySketch sketch = new FrequencySketch();
   final AccessOrderDeque<Node<K, V>> accessOrderWindowDeque;
   final AccessOrderDeque<Node<K, V>> accessOrderProbationDeque;
   final AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque;

   SILSMS(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      if (var1.hasInitialCapacity()) {
         long var4 = Math.min(var1.getMaximum(), (long)var1.getInitialCapacity());
         this.sketch.ensureCapacity(var4);
      }

      this.accessOrderWindowDeque = !var1.evicts() && !var1.expiresAfterAccess() ? null : new AccessOrderDeque<>();
      this.accessOrderProbationDeque = new AccessOrderDeque<>();
      this.accessOrderProtectedDeque = new AccessOrderDeque<>();
   }

   @Override
   protected final boolean evicts() {
      return true;
   }

   @Override
   protected final long maximum() {
      return (long)MAXIMUM.get((SILSMS)this);
   }

   @Override
   protected final long maximumAcquire() {
      return (long)MAXIMUM.getAcquire((SILSMS)this);
   }

   @Override
   protected final void setMaximum(long var1) {
      MAXIMUM.setRelease((SILSMS)this, (long)var1);
   }

   @Override
   protected final long weightedSize() {
      return (long)WEIGHTED_SIZE.get((SILSMS)this);
   }

   @Override
   protected final long weightedSizeAcquire() {
      return (long)WEIGHTED_SIZE.getAcquire((SILSMS)this);
   }

   @Override
   protected final void setWeightedSize(long var1) {
      WEIGHTED_SIZE.setRelease((SILSMS)this, (long)var1);
   }

   @Override
   protected final long windowMaximum() {
      return this.windowMaximum;
   }

   @Override
   protected final void setWindowMaximum(long var1) {
      this.windowMaximum = var1;
   }

   @Override
   protected final long windowWeightedSize() {
      return this.windowWeightedSize;
   }

   @Override
   protected final void setWindowWeightedSize(long var1) {
      this.windowWeightedSize = var1;
   }

   @Override
   protected final long mainProtectedMaximum() {
      return this.mainProtectedMaximum;
   }

   @Override
   protected final void setMainProtectedMaximum(long var1) {
      this.mainProtectedMaximum = var1;
   }

   @Override
   protected final long mainProtectedWeightedSize() {
      return this.mainProtectedWeightedSize;
   }

   @Override
   protected final void setMainProtectedWeightedSize(long var1) {
      this.mainProtectedWeightedSize = var1;
   }

   @Override
   protected final double stepSize() {
      return this.stepSize;
   }

   @Override
   protected final void setStepSize(double var1) {
      this.stepSize = var1;
   }

   @Override
   protected final long adjustment() {
      return this.adjustment;
   }

   @Override
   protected final void setAdjustment(long var1) {
      this.adjustment = var1;
   }

   @Override
   protected final long hitsInSample() {
      return this.hitsInSample;
   }

   @Override
   protected final void setHitsInSample(long var1) {
      this.hitsInSample = var1;
   }

   @Override
   protected final long missesInSample() {
      return this.missesInSample;
   }

   @Override
   protected final void setMissesInSample(long var1) {
      this.missesInSample = var1;
   }

   @Override
   protected final double previousSampleHitRate() {
      return this.previousSampleHitRate;
   }

   @Override
   protected final void setPreviousSampleHitRate(double var1) {
      this.previousSampleHitRate = var1;
   }

   @Override
   protected final FrequencySketch frequencySketch() {
      return this.sketch;
   }

   @Override
   protected final AccessOrderDeque<Node<K, V>> accessOrderWindowDeque() {
      return this.accessOrderWindowDeque;
   }

   @Override
   protected final AccessOrderDeque<Node<K, V>> accessOrderProbationDeque() {
      return this.accessOrderProbationDeque;
   }

   @Override
   protected final AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque() {
      return this.accessOrderProtectedDeque;
   }

   static {
      Lookup var0 = MethodHandles.lookup();

      try {
         MAXIMUM = var0.findVarHandle(SILSMS.class, "maximum", long.class);
         WEIGHTED_SIZE = var0.findVarHandle(SILSMS.class, "weightedSize", long.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
