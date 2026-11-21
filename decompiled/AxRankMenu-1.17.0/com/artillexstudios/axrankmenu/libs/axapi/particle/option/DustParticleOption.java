package com.artillexstudios.axrankmenu.libs.axapi.particle.option;

import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.utils.Vector3f;

public record DustParticleOption(Vector3f color, float scale) implements ParticleOption {
}
