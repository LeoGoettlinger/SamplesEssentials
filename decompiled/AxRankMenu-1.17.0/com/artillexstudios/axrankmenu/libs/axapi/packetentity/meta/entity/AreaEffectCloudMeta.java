package com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.entity;

import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.EntityMeta;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.Metadata;
import com.artillexstudios.axrankmenu.libs.axapi.packetentity.meta.serializer.Accessors;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleData;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleOption;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleType;
import com.artillexstudios.axrankmenu.libs.axapi.particle.ParticleTypes;

public class AreaEffectCloudMeta extends EntityMeta {
   public AreaEffectCloudMeta(Metadata metadata) {
      super(metadata);
   }

   public void radius(float radius) {
      this.metadata.set(Accessors.AREA_EFFECT_CLOUD_RADIUS, radius);
   }

   public float radius() {
      return this.metadata.get(Accessors.AREA_EFFECT_CLOUD_RADIUS);
   }

   public void color(int color) {
      this.metadata.set(Accessors.AREA_EFFECT_CLOUD_COLOR, color);
   }

   public int color() {
      return this.metadata.get(Accessors.AREA_EFFECT_CLOUD_COLOR);
   }

   public void point(boolean point) {
      this.metadata.set(Accessors.AREA_EFFECT_CLOUD_POINT, point);
   }

   public boolean point() {
      return this.metadata.get(Accessors.AREA_EFFECT_CLOUD_POINT);
   }

   public void particle(ParticleData<?> particle) {
      this.metadata.set(Accessors.AREA_EFFECT_CLOUD_PARTICLE, (ParticleData<? extends ParticleOption>)particle);
   }

   public ParticleData<?> particle() {
      return this.metadata.get(Accessors.AREA_EFFECT_CLOUD_PARTICLE);
   }

   @Override
   protected void defineDefaults() {
      this.metadata.define(Accessors.AREA_EFFECT_CLOUD_RADIUS, 0.5F);
      this.metadata.define(Accessors.AREA_EFFECT_CLOUD_COLOR, 0);
      this.metadata.define(Accessors.AREA_EFFECT_CLOUD_POINT, false);
      this.metadata.define(Accessors.AREA_EFFECT_CLOUD_PARTICLE, new ParticleData<>((ParticleType<? extends ParticleOption>)ParticleTypes.CLOUD));
   }
}
