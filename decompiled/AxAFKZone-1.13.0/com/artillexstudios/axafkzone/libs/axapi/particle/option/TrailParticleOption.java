package com.artillexstudios.axafkzone.libs.axapi.particle.option;

import com.artillexstudios.axafkzone.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axafkzone.libs.axapi.utils.Vector3f;

public record TrailParticleOption(double x, double y, double z, Vector3f color, int duration) implements ParticleOption {
}
