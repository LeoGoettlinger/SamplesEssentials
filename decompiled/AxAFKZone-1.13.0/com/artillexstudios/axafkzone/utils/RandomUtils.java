package com.artillexstudios.axafkzone.utils;

import com.artillexstudios.axafkzone.libs.axapi.libs.math3.distribution.EnumeratedDistribution;
import com.artillexstudios.axafkzone.libs.axapi.libs.math3.util.Pair;
import com.artillexstudios.axafkzone.reward.Reward;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class RandomUtils {
   public static Reward randomValue(@NotNull Map<Reward, Double> map) {
      List<Pair<Reward, Double>> list = new ArrayList<>();
      map.forEach((key, value) -> list.add(new Pair(key, value)));
      EnumeratedDistribution<Reward> e = new EnumeratedDistribution(list);
      return (Reward)e.sample();
   }
}
