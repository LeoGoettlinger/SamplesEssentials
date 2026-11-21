package com.earth2me.essentials.libs.configurate.transformation;

import com.earth2me.essentials.libs.configurate.NodePath;
import java.util.Comparator;

final class NodePathComparator implements Comparator<NodePath> {
   static final NodePathComparator INSTANCE = new NodePathComparator();

   private NodePathComparator() {
   }

   public int compare(final NodePath pathA, final NodePath pathB) {
      for (int i = 0; i < Math.min(pathA.size(), pathB.size()); i++) {
         Object a = pathA.get(i);
         Object b = pathB.get(i);
         if (a != ConfigurationTransformation.WILDCARD_OBJECT && b != ConfigurationTransformation.WILDCARD_OBJECT) {
            if (!(a instanceof Comparable)) {
               return a.equals(b) ? 0 : Integer.compare(a.hashCode(), b.hashCode());
            }

            int comp = ((Comparable)a).compareTo(b);
            if (comp != 0) {
               return comp;
            }
         } else if (a != ConfigurationTransformation.WILDCARD_OBJECT || b != ConfigurationTransformation.WILDCARD_OBJECT) {
            return a == ConfigurationTransformation.WILDCARD_OBJECT ? 1 : -1;
         }
      }

      return Integer.compare(pathB.size(), pathA.size());
   }
}
