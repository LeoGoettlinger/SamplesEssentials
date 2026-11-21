package com.artillexstudios.axtrade.libs.axapi.particle.option;

import com.artillexstudios.axtrade.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axtrade.libs.axapi.utils.Vector3f;

public record DustParticleOption(Vector3f color, float scale) implements ParticleOption {
}
