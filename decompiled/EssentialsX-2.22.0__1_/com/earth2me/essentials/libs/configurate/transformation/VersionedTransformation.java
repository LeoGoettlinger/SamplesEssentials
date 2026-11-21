package com.earth2me.essentials.libs.configurate.transformation;

import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.NodePath;
import java.util.NavigableMap;
import java.util.Map.Entry;

class VersionedTransformation implements ConfigurationTransformation.Versioned {
   private final NodePath versionPath;
   private final NavigableMap<Integer, ConfigurationTransformation> versionTransformations;

   VersionedTransformation(final NodePath versionPath, final NavigableMap<Integer, ConfigurationTransformation> versionTransformations) {
      this.versionPath = versionPath;
      this.versionTransformations = versionTransformations;
   }

   @Override
   public void apply(final ConfigurationNode node) throws ConfigurateException {
      ConfigurateException thrown = null;
      ConfigurationNode versionNode = node.node(this.versionPath);
      int currentVersion = versionNode.getInt(-1);

      for (Entry<Integer, ConfigurationTransformation> entry : this.versionTransformations.entrySet()) {
         if (entry.getKey() > currentVersion) {
            try {
               entry.getValue().apply(node);
            } catch (ConfigurateException var8) {
               if (thrown == null) {
                  thrown = var8;
               } else {
                  thrown.addSuppressed(var8);
               }
            }

            currentVersion = entry.getKey();
         }
      }

      if (thrown != null) {
         throw thrown;
      } else {
         versionNode.set(currentVersion);
      }
   }

   @Override
   public NodePath versionKey() {
      return this.versionPath;
   }

   @Override
   public int latestVersion() {
      return this.versionTransformations.lastKey();
   }
}
