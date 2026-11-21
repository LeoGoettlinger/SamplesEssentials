package com.github.retrooper.packetevents.util;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class Vector3f {
   private static final Vector3f ZERO = new Vector3f(0.0F, 0.0F, 0.0F);
   public final float x;
   public final float y;
   public final float z;

   public Vector3f() {
      this.x = 0.0F;
      this.y = 0.0F;
      this.z = 0.0F;
   }

   public Vector3f(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vector3f(Vector3d vector) {
      this((float)vector.x, (float)vector.y, (float)vector.z);
   }

   public Vector3f(float[] array) {
      if (array.length > 0) {
         this.x = array[0];
         if (array.length > 1) {
            this.y = array[1];
            if (array.length > 2) {
               this.z = array[2];
            } else {
               this.z = 0.0F;
            }
         } else {
            this.y = 0.0F;
            this.z = 0.0F;
         }
      } else {
         this.x = 0.0F;
         this.y = 0.0F;
         this.z = 0.0F;
      }
   }

   public static Vector3f read(PacketWrapper<?> wrapper) {
      float x = wrapper.readFloat();
      float y = wrapper.readFloat();
      float z = wrapper.readFloat();
      return new Vector3f(x, y, z);
   }

   public static void write(PacketWrapper<?> wrapper, Vector3f vector) {
      wrapper.writeFloat(vector.x);
      wrapper.writeFloat(vector.y);
      wrapper.writeFloat(vector.z);
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getZ() {
      return this.z;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Vector3f) {
         Vector3f vec = (Vector3f)obj;
         return this.x == vec.x && this.y == vec.y && this.z == vec.z;
      } else if (obj instanceof Vector3d) {
         Vector3d vec = (Vector3d)obj;
         return this.x == vec.x && this.y == vec.y && this.z == vec.z;
      } else if (!(obj instanceof Vector3i)) {
         return false;
      } else {
         Vector3i vec = (Vector3i)obj;
         return this.x == vec.x && this.y == vec.y && this.z == vec.z;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }

   public Vector3f add(float x, float y, float z) {
      return new Vector3f(this.x + x, this.y + y, this.z + z);
   }

   public Vector3f add(Vector3f other) {
      return this.add(other.x, other.y, other.z);
   }

   public Vector3f offset(BlockFace face) {
      return this.add(face.getModX(), face.getModY(), face.getModZ());
   }

   public Vector3f offset(BlockFace face, int distance) {
      return distance == 0 ? this : this.add(face.getModX() * distance, face.getModY() * distance, face.getModZ() * distance);
   }

   public Vector3f subtract(float x, float y, float z) {
      return new Vector3f(this.x - x, this.y - y, this.z - z);
   }

   public Vector3f subtract(Vector3f other) {
      return this.subtract(other.x, other.y, other.z);
   }

   public Vector3f multiply(float x, float y, float z) {
      return new Vector3f(this.x * x, this.y * y, this.z * z);
   }

   public Vector3f multiply(Vector3f other) {
      return this.multiply(other.x, other.y, other.z);
   }

   public Vector3f multiply(float value) {
      return this.multiply(value, value, value);
   }

   public Vector3f crossProduct(Vector3f other) {
      float newX = this.y * other.z - other.y * this.z;
      float newY = this.z * other.x - other.z * this.x;
      float newZ = this.x * other.y - other.x * this.y;
      return new Vector3f(newX, newY, newZ);
   }

   public float dot(Vector3f other) {
      return this.x * other.x + this.y * other.y + this.z * other.z;
   }

   public Vector3f with(Float x, Float y, Float z) {
      return new Vector3f(x == null ? this.x : x, y == null ? this.y : y, z == null ? this.z : z);
   }

   public Vector3f withX(float x) {
      return new Vector3f(x, this.y, this.z);
   }

   public Vector3f withY(float y) {
      return new Vector3f(this.x, y, this.z);
   }

   public Vector3f withZ(float z) {
      return new Vector3f(this.x, this.y, z);
   }

   @Override
   public String toString() {
      return "X: " + this.x + ", Y: " + this.y + ", Z: " + this.z;
   }

   public static Vector3f zero() {
      return ZERO;
   }
}
