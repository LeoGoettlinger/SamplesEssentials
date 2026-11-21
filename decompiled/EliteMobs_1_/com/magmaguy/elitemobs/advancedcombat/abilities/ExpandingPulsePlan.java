package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ExpandingPulsePlan(List<ExpandingPulsePlan.Pulse> pulses) {
   public ExpandingPulsePlan(List<ExpandingPulsePlan.Pulse> pulses) {
      pulses = List.copyOf(pulses);
      if (pulses.isEmpty()) {
         throw new IllegalArgumentException("At least one pulse is required");
      } else {
         this.pulses = pulses;
      }
   }

   public static ExpandingPulsePlan create(double maximumRadius, int pulseCount, int durationTicks) {
      if (Double.isFinite(maximumRadius) && !(maximumRadius <= 0.0)) {
         if (pulseCount < 1) {
            throw new IllegalArgumentException("pulseCount must be positive");
         } else if (durationTicks < 0) {
            throw new IllegalArgumentException("durationTicks cannot be negative");
         } else {
            List<ExpandingPulsePlan.Pulse> pulses = new ArrayList<>(pulseCount);

            for (int index = 0; index < pulseCount; index++) {
               double innerRadius = maximumRadius * index / pulseCount;
               double outerRadius = maximumRadius * (index + 1.0) / pulseCount;
               int delay = pulseCount == 1 ? 0 : (int)Math.round((double)(durationTicks * index) / (pulseCount - 1));
               pulses.add(new ExpandingPulsePlan.Pulse(index, delay, innerRadius, outerRadius));
            }

            return new ExpandingPulsePlan(pulses);
         }
      } else {
         throw new IllegalArgumentException("maximumRadius must be positive and finite");
      }
   }

   public Optional<ExpandingPulsePlan.Pulse> pulseForDistance(double distance) {
      return Double.isFinite(distance) && !(distance < 0.0) ? this.pulses.stream().filter(pulse -> pulse.contains(distance)).findFirst() : Optional.empty();
   }

   public record Pulse(int index, int delayTicks, double innerRadiusExclusive, double outerRadiusInclusive) {
      public Pulse(int index, int delayTicks, double innerRadiusExclusive, double outerRadiusInclusive) {
         if (index < 0 || delayTicks < 0) {
            throw new IllegalArgumentException("Pulse index and delay cannot be negative");
         } else if (Double.isFinite(innerRadiusExclusive)
            && Double.isFinite(outerRadiusInclusive)
            && !(innerRadiusExclusive < 0.0)
            && !(outerRadiusInclusive <= innerRadiusExclusive)) {
            this.index = index;
            this.delayTicks = delayTicks;
            this.innerRadiusExclusive = innerRadiusExclusive;
            this.outerRadiusInclusive = outerRadiusInclusive;
         } else {
            throw new IllegalArgumentException("Pulse radii must define a positive ring");
         }
      }

      public boolean contains(double distance) {
         return Double.isFinite(distance) && !(distance < 0.0)
            ? (this.index == 0 ? distance >= 0.0 : distance > this.innerRadiusExclusive) && distance <= this.outerRadiusInclusive
            : false;
      }
   }
}
