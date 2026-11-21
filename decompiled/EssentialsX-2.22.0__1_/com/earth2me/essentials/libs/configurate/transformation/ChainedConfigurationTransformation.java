package com.earth2me.essentials.libs.configurate.transformation;

import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import java.util.Arrays;

class ChainedConfigurationTransformation implements ConfigurationTransformation {
   private final ConfigurationTransformation[] transformations;

   ChainedConfigurationTransformation(final ConfigurationTransformation[] transformations) {
      this.transformations = Arrays.copyOf(transformations, transformations.length);
   }

   @Override
   public void apply(final ConfigurationNode node) throws ConfigurateException {
      ConfigurateException thrown = null;

      for (ConfigurationTransformation transformation : this.transformations) {
         try {
            transformation.apply(node);
         } catch (ConfigurateException var8) {
            if (thrown == null) {
               thrown = var8;
            } else {
               thrown.addSuppressed(var8);
            }
         }
      }

      if (thrown != null) {
         throw thrown;
      }
   }
}
