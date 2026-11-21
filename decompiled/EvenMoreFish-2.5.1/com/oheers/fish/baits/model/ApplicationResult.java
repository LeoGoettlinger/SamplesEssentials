package com.oheers.fish.baits.model;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public record ApplicationResult(@NonNull ItemStack fishingRod, int cursorItemModifier) {
}
