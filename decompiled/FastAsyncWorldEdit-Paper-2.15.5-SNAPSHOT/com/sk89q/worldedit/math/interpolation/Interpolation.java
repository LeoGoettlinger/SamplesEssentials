package com.sk89q.worldedit.math.interpolation;

import com.sk89q.worldedit.math.Vector3;
import java.util.List;

public interface Interpolation {
   void setNodes(List<Node> nodes);

   Vector3 getPosition(double position);

   Vector3 get1stDerivative(double position);

   double arcLength(double positionA, double positionB);

   int getSegment(double position);
}
