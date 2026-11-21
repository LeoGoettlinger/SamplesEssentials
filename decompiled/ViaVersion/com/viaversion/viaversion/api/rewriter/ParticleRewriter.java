package com.viaversion.viaversion.api.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Particle;

public interface ParticleRewriter {
   void rewriteParticle(UserConnection var1, Particle var2);
}
