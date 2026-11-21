package com.artillexstudios.axtrade.libs.axapi.items.components.data;

import java.util.List;
import java.util.Optional;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public record PotionContents(Optional<PotionType> type, Optional<Integer> color, List<PotionEffect> effects, Optional<String> name) {
}
