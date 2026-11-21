package com.sk89q.worldedit.regions.selector.limit;

import java.util.Optional;

public interface SelectorLimits {
   Optional<Integer> getPolygonVertexLimit();

   Optional<Integer> getPolyhedronVertexLimit();
}
