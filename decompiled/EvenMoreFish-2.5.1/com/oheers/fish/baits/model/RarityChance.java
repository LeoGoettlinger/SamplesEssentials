package com.oheers.fish.baits.model;

import com.oheers.fish.api.fishing.items.IRarity;
import java.util.List;
import org.jspecify.annotations.NonNull;

public record RarityChance(
   @NonNull IRarity rarity, double baseWeight, double effectiveWeight, double chance, @NonNull WeightModifier modifier, @NonNull List<FishChance> fishChances
) {
}
