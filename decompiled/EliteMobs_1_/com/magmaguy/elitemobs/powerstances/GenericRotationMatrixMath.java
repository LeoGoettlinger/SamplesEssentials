package com.magmaguy.elitemobs.powerstances;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class GenericRotationMatrixMath {
   public static Vector applyRotation(double a, double b, double c, double numberOfPointsPerFullRotation, double x, double y, double z, int counter) {
      double theta = (Math.PI * 2) / numberOfPointsPerFullRotation;
      theta += theta * counter;
      double newX = x * (Math.cos(theta) + (1.0 - Math.cos(theta)) * Math.pow(a, 2.0))
         + y * ((1.0 - Math.cos(theta)) * a * b + Math.sin(theta) * c)
         + z * ((1.0 - Math.cos(theta)) * a * c - Math.sin(theta) * b);
      double newY = x * ((1.0 - Math.cos(theta)) * b * a - Math.sin(theta) * c)
         + y * (Math.cos(theta) + (1.0 - Math.cos(theta)) * Math.pow(b, 2.0))
         + z * ((1.0 - Math.cos(theta)) * b * c + Math.sin(theta) * a);
      double newZ = x * ((1.0 - Math.cos(theta)) * c * a + Math.sin(theta) * b)
         + y * ((1.0 - Math.cos(theta)) * c * b - Math.sin(theta) * a)
         + z * (Math.cos(theta) + (1.0 - Math.cos(theta)) * Math.pow(c, 2.0));
      return new Vector(newX, newY, newZ);
   }

   public static Location rotateLocationYAxis(double rotationAngleInDegrees, Location anchorPoint, Vector relativeLocation) {
      if (rotationAngleInDegrees == 90.0) {
         rotationAngleInDegrees = -90.0;
      } else if (rotationAngleInDegrees == -90.0) {
         rotationAngleInDegrees = 90.0;
      }

      Vector rotatedVector = relativeLocation.clone().rotateAroundY(Math.toRadians(rotationAngleInDegrees)).add(anchorPoint.toVector());
      return new Location(anchorPoint.getWorld(), rotatedVector.getX(), rotatedVector.getY(), rotatedVector.getZ());
   }

   public static Vector rotateVectorYAxis(double rotationAngleInDegrees, Location anchorPoint, Vector relativeLocation) {
      return rotateLocationYAxis(rotationAngleInDegrees, anchorPoint, relativeLocation).toVector();
   }
}
