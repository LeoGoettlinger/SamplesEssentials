package com.artillexstudios.axtrade.libs.axapi.particle.option;

import com.artillexstudios.axtrade.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axtrade.libs.axapi.utils.Vector3f;

public record DustColorTransitionParticleOption(Vector3f color1, Vector3f color2, float scale) implements ParticleOption {
}
