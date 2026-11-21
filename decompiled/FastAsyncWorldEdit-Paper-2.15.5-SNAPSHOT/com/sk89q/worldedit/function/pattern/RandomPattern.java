package com.sk89q.worldedit.function.pattern;

import com.fastasyncworldedit.core.math.random.SimpleRandom;
import com.fastasyncworldedit.core.math.random.TrueRandom;
import com.fastasyncworldedit.core.util.collection.RandomCollection;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RandomPattern extends AbstractPattern {
   private final SimpleRandom random;
   private Map<Pattern, Double> weights = new LinkedHashMap<>();
   private RandomCollection<Pattern> collection;
   private LinkedHashSet<Pattern> patterns = new LinkedHashSet<>();

   public RandomPattern() {
      this(new TrueRandom());
   }

   public RandomPattern(SimpleRandom random) {
      this.random = random;
   }

   public RandomPattern(SimpleRandom random, RandomPattern parent) {
      this.random = random;
      this.weights = parent.weights;
      this.collection = RandomCollection.of(this.weights, random);
      this.patterns = parent.patterns;
   }

   private RandomPattern(SimpleRandom random, Map<Pattern, Double> weights) {
      this.random = random;
      this.weights = weights;
      this.collection = RandomCollection.of(weights, random);
      this.patterns = new LinkedHashSet<>(weights.keySet());
   }

   public void add(Pattern pattern, double chance) {
      Preconditions.checkNotNull(pattern);
      Double existingWeight = this.weights.get(pattern);
      if (existingWeight != null) {
         chance += existingWeight;
      }

      this.weights.put(pattern, chance);
      this.collection = RandomCollection.of(this.weights, this.random);
      this.patterns.add(pattern);
   }

   public Set<Pattern> getPatterns() {
      return this.patterns;
   }

   public RandomCollection<Pattern> getCollection() {
      return this.collection;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      return this.collection.next(position.x(), position.y(), position.z()).applyBlock(position);
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      return this.collection.next(get.x(), get.y(), get.z()).apply(extent, get, set);
   }

   @Override
   public Pattern fork() {
      LinkedHashMap<Pattern, Double> newWeights = new LinkedHashMap<>();
      this.weights.forEach((p, w) -> newWeights.put(p.fork(), w));
      return new RandomPattern(this.random, newWeights);
   }
}
