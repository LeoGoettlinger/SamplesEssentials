package com.artillexstudios.axrankmenu.libs.axapi.particle.option;

import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.utils.position.BlockPosition;

public record VibrationParticleOption(int source, BlockPosition position, int entityId, float eyeHeight, int ticks) implements ParticleOption {
}
