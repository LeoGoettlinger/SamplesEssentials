package com.fastasyncworldedit.core.extent.transform;

import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.math.random.SimpleRandom;
import com.fastasyncworldedit.core.math.random.TrueRandom;
import com.fastasyncworldedit.core.util.collection.RandomCollection;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RandomTransform extends SelectTransform {
   private final SimpleRandom random;
   private final Map<ResettableExtent, Double> weights = new HashMap<>();
   private transient RandomCollection<ResettableExtent> collection;
   private transient LinkedHashSet<ResettableExtent> extents = new LinkedHashSet<>();

   public RandomTransform() {
      this(new TrueRandom());
   }

   public RandomTransform(SimpleRandom random) {
      this.random = random;
   }

   @Override
   public AbstractDelegateExtent getExtent(int x, int y, int z) {
      return this.collection.next(x, y, z);
   }

   @Override
   public AbstractDelegateExtent getExtent(int x, int z) {
      return this.collection.next(x, 0, z);
   }

   @Override
   public ResettableExtent setExtent(Extent extent) {
      if (this.collection == null) {
         this.collection = RandomCollection.of(this.weights, this.random);
         this.extents = new LinkedHashSet<>(this.weights.keySet());
      }

      super.setExtent(extent);

      for (ResettableExtent current : this.extents) {
         current.setExtent(extent);
      }

      return this;
   }

   public void add(ResettableExtent extent, double chance) {
      Preconditions.checkNotNull(extent);
      this.weights.put(extent, chance);
      this.collection = RandomCollection.of(this.weights, this.random);
      this.extents.add(extent);
   }

   public Set<ResettableExtent> getExtents() {
      return this.extents;
   }

   public RandomCollection<ResettableExtent> getCollection() {
      return this.collection;
   }
}
