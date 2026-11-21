package com.artillexstudios.axtrade.libs.axapi.particle.option;

import com.artillexstudios.axtrade.libs.axapi.items.WrappedItemStack;
import com.artillexstudios.axtrade.libs.axapi.particle.ParticleOption;

public record ItemStackParticleOption(WrappedItemStack stack) implements ParticleOption {
}
