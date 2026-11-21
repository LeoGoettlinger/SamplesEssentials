package com.artillexstudios.axafkzone.libs.axapi.particle.option;

import com.artillexstudios.axafkzone.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleOption;

public record ItemStackParticleOption(WrappedItemStack stack) implements ParticleOption {
}
