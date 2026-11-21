package com.artillexstudios.axafkzone.libs.axapi.particle.option;

import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axafkzone.libs.axapi.utils.position.BlockPosition;

public record VibrationParticleOption(int source, BlockPosition position, int entityId, float eyeHeight, int ticks) implements ParticleOption {
}
