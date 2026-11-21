package com.artillexstudios.axtrade.libs.axapi.particle.option;

import com.artillexstudios.axtrade.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axtrade.libs.axapi.utils.position.BlockPosition;

public record VibrationParticleOption(int source, BlockPosition position, int entityId, float eyeHeight, int ticks) implements ParticleOption {
}
