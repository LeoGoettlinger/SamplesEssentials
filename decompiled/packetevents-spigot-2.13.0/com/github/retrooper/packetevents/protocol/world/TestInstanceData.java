package com.github.retrooper.packetevents.protocol.world;

import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class TestInstanceData {
   @Nullable
   private ResourceLocation test;
   private Vector3i size;
   private StructureRotation rotation;
   private boolean ignoreEntities;
   private TestInstanceData.Status status;
   @Nullable
   private Component errorMessage;

   public TestInstanceData(
      @Nullable ResourceLocation test,
      Vector3i size,
      StructureRotation rotation,
      boolean ignoreEntities,
      TestInstanceData.Status status,
      @Nullable Component errorMessage
   ) {
      this.test = test;
      this.size = size;
      this.rotation = rotation;
      this.ignoreEntities = ignoreEntities;
      this.status = status;
      this.errorMessage = errorMessage;
   }

   public static TestInstanceData read(PacketWrapper<?> wrapper) {
      ResourceLocation test = wrapper.readOptional(ResourceLocation::read);
      Vector3i size = Vector3i.read(wrapper);
      StructureRotation rotation = wrapper.readEnum(StructureRotation.class);
      boolean ignoreEntities = wrapper.readBoolean();
      TestInstanceData.Status status = wrapper.readEnum(TestInstanceData.Status.class);
      Component errorMessage = wrapper.readOptional(PacketWrapper::readComponent);
      return new TestInstanceData(test, size, rotation, ignoreEntities, status, errorMessage);
   }

   public static void write(PacketWrapper<?> wrapper, TestInstanceData data) {
      wrapper.writeOptional(data.test, ResourceLocation::write);
      Vector3i.write(wrapper, data.size);
      wrapper.writeEnum(data.rotation);
      wrapper.writeBoolean(data.ignoreEntities);
      wrapper.writeEnum(data.status);
      wrapper.writeOptional(data.errorMessage, PacketWrapper::writeComponent);
   }

   @Nullable
   public ResourceLocation getTest() {
      return this.test;
   }

   public void setTest(@Nullable ResourceLocation test) {
      this.test = test;
   }

   public Vector3i getSize() {
      return this.size;
   }

   public void setSize(Vector3i size) {
      this.size = size;
   }

   public StructureRotation getRotation() {
      return this.rotation;
   }

   public void setRotation(StructureRotation rotation) {
      this.rotation = rotation;
   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   public void setIgnoreEntities(boolean ignoreEntities) {
      this.ignoreEntities = ignoreEntities;
   }

   public TestInstanceData.Status getStatus() {
      return this.status;
   }

   public void setStatus(TestInstanceData.Status status) {
      this.status = status;
   }

   @Nullable
   public Component getErrorMessage() {
      return this.errorMessage;
   }

   public void setErrorMessage(@Nullable Component errorMessage) {
      this.errorMessage = errorMessage;
   }

   public static enum Status {
      CLEARED,
      RUNNING,
      FINISHED;
   }
}
