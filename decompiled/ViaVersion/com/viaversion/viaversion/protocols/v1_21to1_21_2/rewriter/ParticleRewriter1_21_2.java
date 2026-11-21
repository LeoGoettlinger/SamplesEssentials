package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.rewriter.ParticleRewriter;

public final class ParticleRewriter1_21_2 extends ParticleRewriter<ClientboundPacket1_21> {
   public ParticleRewriter1_21_2(Protocol<ClientboundPacket1_21, ?, ?, ?> protocol) {
      super(protocol);
   }

   private void floatsToARGB(Particle particle, int fromIndex) {
      Particle.ParticleData<Float> r = particle.removeArgument(fromIndex);
      Particle.ParticleData<Float> g = particle.removeArgument(fromIndex);
      Particle.ParticleData<Float> b = particle.removeArgument(fromIndex);
      int rgb = 0xFF000000 | (int)(r.getValue() * 255.0F) << 16 | (int)(g.getValue() * 255.0F) << 8 | (int)(b.getValue() * 255.0F);
      particle.add(fromIndex, Types.INT, rgb);
   }

   @Override
   public void rewriteParticle(UserConnection connection, Particle particle) {
      super.rewriteParticle(connection, particle);
      String identifier = this.protocol.getMappingData().getParticleMappings().mappedIdentifier(particle.id());
      if ("minecraft:dust_color_transition".equals(identifier)) {
         this.floatsToARGB(particle, 0);
         this.floatsToARGB(particle, 1);
      } else if ("minecraft:dust".equals(identifier)) {
         this.floatsToARGB(particle, 0);
      }
   }
}
