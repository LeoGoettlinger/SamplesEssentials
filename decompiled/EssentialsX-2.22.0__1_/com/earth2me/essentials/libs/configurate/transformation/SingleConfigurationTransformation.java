package com.earth2me.essentials.libs.configurate.transformation;

import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.NodePath;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

final class SingleConfigurationTransformation implements ConfigurationTransformation {
   private final MoveStrategy strategy;
   private final Map<NodePath, TransformAction> actions;
   private final ThreadLocal<MutableNodePath> sharedPath = ThreadLocal.withInitial(MutableNodePath::new);

   SingleConfigurationTransformation(final Map<NodePath, TransformAction> actions, final MoveStrategy strategy) {
      this.actions = actions;
      this.strategy = strategy;
   }

   @Override
   public void apply(final ConfigurationNode node) throws ConfigurateException {
      ConfigurateException thrown = null;

      for (Entry<NodePath, TransformAction> ent : this.actions.entrySet()) {
         try {
            this.applySingleAction(node, ent.getKey().array(), 0, node, ent.getValue());
         } catch (ConfigurateException var6) {
            if (thrown == null) {
               thrown = var6;
            } else {
               thrown.addSuppressed(var6);
            }
         }
      }

      if (thrown != null) {
         throw thrown;
      }
   }

   private void applySingleAction(final ConfigurationNode start, final Object[] path, final int startIdx, ConfigurationNode node, final TransformAction action) throws ConfigurateException {
      ConfigurateException thrown = null;

      for (int i = startIdx; i < path.length; i++) {
         if (path[i] == WILDCARD_OBJECT) {
            if (node.isList()) {
               List<? extends ConfigurationNode> children = node.childrenList();

               for (int di = 0; di < children.size(); di++) {
                  path[i] = di;

                  try {
                     this.applySingleAction(start, path, i + 1, children.get(di), action);
                  } catch (ConfigurateException var12) {
                     if (thrown == null) {
                        thrown = var12;
                     } else {
                        thrown.addSuppressed(var12);
                     }
                  }
               }

               path[i] = WILDCARD_OBJECT;
            } else {
               if (!node.isMap()) {
                  return;
               }

               for (Entry<Object, ? extends ConfigurationNode> ent : node.childrenMap().entrySet()) {
                  path[i] = ent.getKey();

                  try {
                     this.applySingleAction(start, path, i + 1, ent.getValue(), action);
                  } catch (ConfigurateException var11) {
                     if (thrown == null) {
                        thrown = var11;
                     } else {
                        thrown.addSuppressed(var11);
                     }
                  }
               }

               path[i] = WILDCARD_OBJECT;
            }

            return;
         }

         node = node.node(path[i]);
         if (node.virtual()) {
            return;
         }
      }

      MutableNodePath nodePath = this.sharedPath.get();
      nodePath.arr = path;
      Object[] transformedPath = action.visitPath(nodePath, node);
      if (transformedPath != null && !Arrays.equals(path, transformedPath)) {
         this.strategy.move(node, start.node(transformedPath));
         node.raw(null);
      }
   }
}
