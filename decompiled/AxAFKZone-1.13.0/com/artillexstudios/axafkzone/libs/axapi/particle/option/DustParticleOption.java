package com.artillexstudios.axafkzone.libs.axapi.particle.option;

import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axafkzone.libs.axapi.utils.Vector3f;

public record DustParticleOption(Vector3f color, float scale) implements ParticleOption {
}
