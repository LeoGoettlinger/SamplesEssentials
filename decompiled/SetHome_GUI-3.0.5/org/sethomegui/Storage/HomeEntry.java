package org.sethomegui.Storage;

import java.util.ArrayList;
import java.util.List;

public class HomeEntry {
   private String name;
   private String world = "world";
   private double x;
   private double y;
   private double z;
   private double yaw;
   private double pitch;
   private String icon;
   private String access = "nobody";
   private List<String> allowedFriends = new ArrayList<>();

   public HomeEntry() {
   }

   public HomeEntry(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getWorld() {
      return this.world;
   }

   public void setWorld(String world) {
      this.world = world;
   }

   public double getX() {
      return this.x;
   }

   public void setX(double x) {
      this.x = x;
   }

   public double getY() {
      return this.y;
   }

   public void setY(double y) {
      this.y = y;
   }

   public double getZ() {
      return this.z;
   }

   public void setZ(double z) {
      this.z = z;
   }

   public double getYaw() {
      return this.yaw;
   }

   public void setYaw(double yaw) {
      this.yaw = yaw;
   }

   public double getPitch() {
      return this.pitch;
   }

   public void setPitch(double pitch) {
      this.pitch = pitch;
   }

   public String getIcon() {
      return this.icon;
   }

   public void setIcon(String icon) {
      this.icon = icon;
   }

   public String getAccess() {
      return this.access;
   }

   public void setAccess(String access) {
      this.access = access == null ? "nobody" : access;
   }

   public List<String> getAllowedFriends() {
      return this.allowedFriends;
   }

   public void setAllowedFriends(List<String> allowedFriends) {
      this.allowedFriends = (List<String>)(allowedFriends == null ? new ArrayList<>() : allowedFriends);
   }
}
