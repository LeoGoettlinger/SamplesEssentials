package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public abstract class GeneratedMessageV3 extends AbstractMessage implements Serializable {
   private static final long serialVersionUID = 1L;
   protected static boolean alwaysUseFieldBuilders = false;
   protected UnknownFieldSet unknownFields;

   protected GeneratedMessageV3() {
      this.unknownFields = UnknownFieldSet.getDefaultInstance();
   }

   protected GeneratedMessageV3(GeneratedMessageV3.Builder<?> builder) {
      this.unknownFields = builder.getUnknownFields();
   }

   @Override
   public Parser<? extends GeneratedMessageV3> getParserForType() {
      throw new UnsupportedOperationException("This is supposed to be overridden by subclasses.");
   }

   static void enableAlwaysUseFieldBuildersForTesting() {
      setAlwaysUseFieldBuildersForTesting(true);
   }

   static void setAlwaysUseFieldBuildersForTesting(boolean useBuilders) {
      alwaysUseFieldBuilders = useBuilders;
   }

   protected abstract GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable();

   @Override
   public Descriptors.Descriptor getDescriptorForType() {
      return this.internalGetFieldAccessorTable().descriptor;
   }

   @Deprecated
   protected void mergeFromAndMakeImmutableInternal(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      Schema<GeneratedMessageV3> schema = Protobuf.getInstance().schemaFor(this);

      try {
         schema.mergeFrom(this, CodedInputStreamReader.forCodedInput(input), extensionRegistry);
      } catch (InvalidProtocolBufferException var5) {
         throw var5.setUnfinishedMessage(this);
      } catch (IOException var6) {
         throw new InvalidProtocolBufferException(var6).setUnfinishedMessage(this);
      }

      schema.makeImmutable(this);
   }

   private Map<Descriptors.FieldDescriptor, Object> getAllFieldsMutable(boolean getBytesForString) {
      TreeMap<Descriptors.FieldDescriptor, Object> result = new TreeMap<>();
      Descriptors.Descriptor descriptor = this.internalGetFieldAccessorTable().descriptor;
      List<Descriptors.FieldDescriptor> fields = descriptor.getFields();

      for (int i = 0; i < fields.size(); i++) {
         Descriptors.FieldDescriptor field = fields.get(i);
         Descriptors.OneofDescriptor oneofDescriptor = field.getContainingOneof();
         if (oneofDescriptor != null) {
            i += oneofDescriptor.getFieldCount() - 1;
            if (!this.hasOneof(oneofDescriptor)) {
               continue;
            }

            field = this.getOneofFieldDescriptor(oneofDescriptor);
         } else {
            if (field.isRepeated()) {
               List<?> value = (List<?>)this.getField(field);
               if (!value.isEmpty()) {
                  result.put(field, value);
               }
               continue;
            }

            if (!this.hasField(field)) {
               continue;
            }
         }

         if (getBytesForString && field.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING) {
            result.put(field, this.getFieldRaw(field));
         } else {
            result.put(field, this.getField(field));
         }
      }

      return result;
   }

   @Override
   public boolean isInitialized() {
      for (Descriptors.FieldDescriptor field : this.getDescriptorForType().getFields()) {
         if (field.isRequired() && !this.hasField(field)) {
            return false;
         }

         if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            if (field.isRepeated()) {
               for (Message element : (List)this.getField(field)) {
                  if (!element.isInitialized()) {
                     return false;
                  }
               }
            } else if (this.hasField(field) && !((Message)this.getField(field)).isInitialized()) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
      return Collections.unmodifiableMap(this.getAllFieldsMutable(false));
   }

   Map<Descriptors.FieldDescriptor, Object> getAllFieldsRaw() {
      return Collections.unmodifiableMap(this.getAllFieldsMutable(true));
   }

   @Override
   public boolean hasOneof(final Descriptors.OneofDescriptor oneof) {
      return this.internalGetFieldAccessorTable().getOneof(oneof).has(this);
   }

   @Override
   public Descriptors.FieldDescriptor getOneofFieldDescriptor(final Descriptors.OneofDescriptor oneof) {
      return this.internalGetFieldAccessorTable().getOneof(oneof).get(this);
   }

   @Override
   public boolean hasField(final Descriptors.FieldDescriptor field) {
      return this.internalGetFieldAccessorTable().getField(field).has(this);
   }

   @Override
   public Object getField(final Descriptors.FieldDescriptor field) {
      return this.internalGetFieldAccessorTable().getField(field).get(this);
   }

   Object getFieldRaw(final Descriptors.FieldDescriptor field) {
      return this.internalGetFieldAccessorTable().getField(field).getRaw(this);
   }

   @Override
   public int getRepeatedFieldCount(final Descriptors.FieldDescriptor field) {
      return this.internalGetFieldAccessorTable().getField(field).getRepeatedCount(this);
   }

   @Override
   public Object getRepeatedField(final Descriptors.FieldDescriptor field, final int index) {
      return this.internalGetFieldAccessorTable().getField(field).getRepeated(this, index);
   }

   @Override
   public UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   void setUnknownFields(UnknownFieldSet unknownFields) {
      this.unknownFields = unknownFields;
   }

   protected boolean parseUnknownField(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
      return input.shouldDiscardUnknownFields() ? input.skipField(tag) : unknownFields.mergeFieldFrom(tag, input);
   }

   protected boolean parseUnknownFieldProto3(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
      return this.parseUnknownField(input, unknownFields, extensionRegistry, tag);
   }

   protected static <M extends Message> M parseWithIOException(Parser<M> parser, InputStream input) throws IOException {
      try {
         return parser.parseFrom(input);
      } catch (InvalidProtocolBufferException var3) {
         throw var3.unwrapIOException();
      }
   }

   protected static <M extends Message> M parseWithIOException(Parser<M> parser, InputStream input, ExtensionRegistryLite extensions) throws IOException {
      try {
         return parser.parseFrom(input, extensions);
      } catch (InvalidProtocolBufferException var4) {
         throw var4.unwrapIOException();
      }
   }

   protected static <M extends Message> M parseWithIOException(Parser<M> parser, CodedInputStream input) throws IOException {
      try {
         return parser.parseFrom(input);
      } catch (InvalidProtocolBufferException var3) {
         throw var3.unwrapIOException();
      }
   }

   protected static <M extends Message> M parseWithIOException(Parser<M> parser, CodedInputStream input, ExtensionRegistryLite extensions) throws IOException {
      try {
         return parser.parseFrom(input, extensions);
      } catch (InvalidProtocolBufferException var4) {
         throw var4.unwrapIOException();
      }
   }

   protected static <M extends Message> M parseDelimitedWithIOException(Parser<M> parser, InputStream input) throws IOException {
      try {
         return parser.parseDelimitedFrom(input);
      } catch (InvalidProtocolBufferException var3) {
         throw var3.unwrapIOException();
      }
   }

   protected static <M extends Message> M parseDelimitedWithIOException(Parser<M> parser, InputStream input, ExtensionRegistryLite extensions) throws IOException {
      try {
         return parser.parseDelimitedFrom(input, extensions);
      } catch (InvalidProtocolBufferException var4) {
         throw var4.unwrapIOException();
      }
   }

   protected static boolean canUseUnsafe() {
      return UnsafeUtil.hasUnsafeArrayOperations() && UnsafeUtil.hasUnsafeByteBufferOperations();
   }

   protected static Internal.IntList emptyIntList() {
      return IntArrayList.emptyList();
   }

   protected static Internal.IntList newIntList() {
      return new IntArrayList();
   }

   protected static Internal.IntList mutableCopy(Internal.IntList list) {
      return makeMutableCopy(list);
   }

   protected static Internal.LongList mutableCopy(Internal.LongList list) {
      return makeMutableCopy(list);
   }

   protected static Internal.FloatList mutableCopy(Internal.FloatList list) {
      return makeMutableCopy(list);
   }

   protected static Internal.DoubleList mutableCopy(Internal.DoubleList list) {
      return makeMutableCopy(list);
   }

   protected static Internal.BooleanList mutableCopy(Internal.BooleanList list) {
      return makeMutableCopy(list);
   }

   protected static Internal.LongList emptyLongList() {
      return LongArrayList.emptyList();
   }

   protected static Internal.LongList newLongList() {
      return new LongArrayList();
   }

   protected static Internal.FloatList emptyFloatList() {
      return FloatArrayList.emptyList();
   }

   protected static Internal.FloatList newFloatList() {
      return new FloatArrayList();
   }

   protected static Internal.DoubleList emptyDoubleList() {
      return DoubleArrayList.emptyList();
   }

   protected static Internal.DoubleList newDoubleList() {
      return new DoubleArrayList();
   }

   protected static Internal.BooleanList emptyBooleanList() {
      return BooleanArrayList.emptyList();
   }

   protected static Internal.BooleanList newBooleanList() {
      return new BooleanArrayList();
   }

   protected static <ListT extends Internal.ProtobufList<?>> ListT makeMutableCopy(ListT list) {
      return makeMutableCopy(list, 0);
   }

   protected static <ListT extends Internal.ProtobufList<?>> ListT makeMutableCopy(ListT list, int minCapacity) {
      int size = list.size();
      if (minCapacity <= size) {
         minCapacity = size * 2;
      }

      if (minCapacity <= 0) {
         minCapacity = 10;
      }

      return (ListT)list.mutableCopyWithCapacity(minCapacity);
   }

   protected static <T> Internal.ProtobufList<T> emptyList(Class<T> elementType) {
      return ProtobufArrayList.emptyList();
   }

   @Override
   public void writeTo(final CodedOutputStream output) throws IOException {
      MessageReflection.writeMessageTo(this, this.getAllFieldsRaw(), output, false);
   }

   @Override
   public int getSerializedSize() {
      int size = this.memoizedSize;
      if (size != -1) {
         return size;
      } else {
         this.memoizedSize = MessageReflection.getSerializedSize(this, this.getAllFieldsRaw());
         return this.memoizedSize;
      }
   }

   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      throw new UnsupportedOperationException("This method must be overridden by the subclass.");
   }

   protected void makeExtensionsImmutable() {
   }

   protected abstract Message.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent);

   @Override
   protected Message.Builder newBuilderForType(final AbstractMessage.BuilderParent parent) {
      return this.newBuilderForType(new GeneratedMessageV3.BuilderParent() {
         @Override
         public void markDirty() {
            parent.markDirty();
         }
      });
   }

   private static java.lang.reflect.Method getMethodOrDie(final Class<?> clazz, final String name, final Class<?>... params) {
      try {
         return clazz.getMethod(name, params);
      } catch (NoSuchMethodException var4) {
         throw new IllegalStateException("Generated message class \"" + clazz.getName() + "\" missing method \"" + name + "\".", var4);
      }
   }

   @CanIgnoreReturnValue
   private static Object invokeOrDie(final java.lang.reflect.Method method, final Object object, final Object... params) {
      try {
         return method.invoke(object, params);
      } catch (IllegalAccessException var5) {
         throw new IllegalStateException("Couldn't use Java reflection to implement protocol message reflection.", var5);
      } catch (InvocationTargetException var6) {
         Throwable cause = var6.getCause();
         if (cause instanceof RuntimeException) {
            throw (RuntimeException)cause;
         } else if (cause instanceof Error) {
            throw (Error)cause;
         } else {
            throw new IllegalStateException("Unexpected exception thrown by generated accessor method.", cause);
         }
      }
   }

   protected MapFieldReflectionAccessor internalGetMapFieldReflection(int fieldNumber) {
      return this.internalGetMapField(fieldNumber);
   }

   @Deprecated
   protected MapField internalGetMapField(int fieldNumber) {
      throw new IllegalArgumentException("No map fields found in " + this.getClass().getName());
   }

   protected Object writeReplace() throws ObjectStreamException {
      return new GeneratedMessageLite.SerializedForm(this);
   }

   private static <MessageT extends GeneratedMessageV3.ExtendableMessage<MessageT>, T> Extension<MessageT, T> checkNotLite(ExtensionLite<MessageT, T> extension) {
      if (extension.isLite()) {
         throw new IllegalArgumentException("Expected non-lite extension.");
      } else {
         return (Extension<MessageT, T>)extension;
      }
   }

   protected static boolean isStringEmpty(final Object value) {
      return value instanceof String ? ((String)value).isEmpty() : ((ByteString)value).isEmpty();
   }

   protected static int computeStringSize(final int fieldNumber, final Object value) {
      return value instanceof String
         ? CodedOutputStream.computeStringSize(fieldNumber, (String)value)
         : CodedOutputStream.computeBytesSize(fieldNumber, (ByteString)value);
   }

   protected static int computeStringSizeNoTag(final Object value) {
      return value instanceof String ? CodedOutputStream.computeStringSizeNoTag((String)value) : CodedOutputStream.computeBytesSizeNoTag((ByteString)value);
   }

   protected static void writeString(CodedOutputStream output, final int fieldNumber, final Object value) throws IOException {
      if (value instanceof String) {
         output.writeString(fieldNumber, (String)value);
      } else {
         output.writeBytes(fieldNumber, (ByteString)value);
      }
   }

   protected static void writeStringNoTag(CodedOutputStream output, final Object value) throws IOException {
      if (value instanceof String) {
         output.writeStringNoTag((String)value);
      } else {
         output.writeBytesNoTag((ByteString)value);
      }
   }

   protected static <V> void serializeIntegerMapTo(CodedOutputStream out, MapField<Integer, V> field, MapEntry<Integer, V> defaultEntry, int fieldNumber) throws IOException {
      Map<Integer, V> m = field.getMap();
      if (!out.isSerializationDeterministic()) {
         serializeMapTo(out, m, defaultEntry, fieldNumber);
      } else {
         int[] keys = new int[m.size()];
         int index = 0;

         for (int k : m.keySet()) {
            keys[index++] = k;
         }

         Arrays.sort(keys);

         for (int key : keys) {
            out.writeMessage(fieldNumber, defaultEntry.newBuilderForType().setKey(key).setValue(m.get(key)).build());
         }
      }
   }

   protected static <V> void serializeLongMapTo(CodedOutputStream out, MapField<Long, V> field, MapEntry<Long, V> defaultEntry, int fieldNumber) throws IOException {
      Map<Long, V> m = field.getMap();
      if (!out.isSerializationDeterministic()) {
         serializeMapTo(out, m, defaultEntry, fieldNumber);
      } else {
         long[] keys = new long[m.size()];
         int index = 0;

         for (long k : m.keySet()) {
            keys[index++] = k;
         }

         Arrays.sort(keys);

         for (long key : keys) {
            out.writeMessage(fieldNumber, defaultEntry.newBuilderForType().setKey(key).setValue(m.get(key)).build());
         }
      }
   }

   protected static <V> void serializeStringMapTo(CodedOutputStream out, MapField<String, V> field, MapEntry<String, V> defaultEntry, int fieldNumber) throws IOException {
      Map<String, V> m = field.getMap();
      if (!out.isSerializationDeterministic()) {
         serializeMapTo(out, m, defaultEntry, fieldNumber);
      } else {
         String[] keys = new String[m.size()];
         keys = m.keySet().toArray(keys);
         Arrays.sort((Object[])keys);

         for (String key : keys) {
            out.writeMessage(fieldNumber, defaultEntry.newBuilderForType().setKey(key).setValue(m.get(key)).build());
         }
      }
   }

   protected static <V> void serializeBooleanMapTo(CodedOutputStream out, MapField<Boolean, V> field, MapEntry<Boolean, V> defaultEntry, int fieldNumber) throws IOException {
      Map<Boolean, V> m = field.getMap();
      if (!out.isSerializationDeterministic()) {
         serializeMapTo(out, m, defaultEntry, fieldNumber);
      } else {
         maybeSerializeBooleanEntryTo(out, m, defaultEntry, fieldNumber, false);
         maybeSerializeBooleanEntryTo(out, m, defaultEntry, fieldNumber, true);
      }
   }

   private static <V> void maybeSerializeBooleanEntryTo(
      CodedOutputStream out, Map<Boolean, V> m, MapEntry<Boolean, V> defaultEntry, int fieldNumber, boolean key
   ) throws IOException {
      if (m.containsKey(key)) {
         out.writeMessage(fieldNumber, defaultEntry.newBuilderForType().setKey(key).setValue(m.get(key)).build());
      }
   }

   private static <K, V> void serializeMapTo(CodedOutputStream out, Map<K, V> m, MapEntry<K, V> defaultEntry, int fieldNumber) throws IOException {
      for (Entry<K, V> entry : m.entrySet()) {
         out.writeMessage(fieldNumber, defaultEntry.newBuilderForType().setKey(entry.getKey()).setValue(entry.getValue()).build());
      }
   }

   public abstract static class Builder<BuilderT extends GeneratedMessageV3.Builder<BuilderT>> extends AbstractMessage.Builder<BuilderT> {
      private GeneratedMessageV3.BuilderParent builderParent;
      private GeneratedMessageV3.Builder<BuilderT>.BuilderParentImpl meAsParent;
      private boolean isClean;
      private Object unknownFieldsOrBuilder = UnknownFieldSet.getDefaultInstance();

      protected Builder() {
         this(null);
      }

      protected Builder(GeneratedMessageV3.BuilderParent builderParent) {
         this.builderParent = builderParent;
      }

      @Override
      void dispose() {
         this.builderParent = null;
      }

      protected void onBuilt() {
         if (this.builderParent != null) {
            this.markClean();
         }
      }

      @Override
      protected void markClean() {
         this.isClean = true;
      }

      protected boolean isClean() {
         return this.isClean;
      }

      public BuilderT clone() {
         BuilderT builder = (BuilderT)this.getDefaultInstanceForType().newBuilderForType();
         builder.mergeFrom(this.buildPartial());
         return builder;
      }

      public BuilderT clear() {
         this.unknownFieldsOrBuilder = UnknownFieldSet.getDefaultInstance();
         this.onChanged();
         return (BuilderT)this;
      }

      protected abstract GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable();

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return this.internalGetFieldAccessorTable().descriptor;
      }

      @Override
      public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
         return Collections.unmodifiableMap(this.getAllFieldsMutable());
      }

      private Map<Descriptors.FieldDescriptor, Object> getAllFieldsMutable() {
         TreeMap<Descriptors.FieldDescriptor, Object> result = new TreeMap<>();
         Descriptors.Descriptor descriptor = this.internalGetFieldAccessorTable().descriptor;
         List<Descriptors.FieldDescriptor> fields = descriptor.getFields();

         for (int i = 0; i < fields.size(); i++) {
            Descriptors.FieldDescriptor field = fields.get(i);
            Descriptors.OneofDescriptor oneofDescriptor = field.getContainingOneof();
            if (oneofDescriptor != null) {
               i += oneofDescriptor.getFieldCount() - 1;
               if (!this.hasOneof(oneofDescriptor)) {
                  continue;
               }

               field = this.getOneofFieldDescriptor(oneofDescriptor);
            } else {
               if (field.isRepeated()) {
                  List<?> value = (List<?>)this.getField(field);
                  if (!value.isEmpty()) {
                     result.put(field, value);
                  }
                  continue;
               }

               if (!this.hasField(field)) {
                  continue;
               }
            }

            result.put(field, this.getField(field));
         }

         return result;
      }

      @Override
      public Message.Builder newBuilderForField(final Descriptors.FieldDescriptor field) {
         return this.internalGetFieldAccessorTable().getField(field).newBuilder();
      }

      @Override
      public Message.Builder getFieldBuilder(final Descriptors.FieldDescriptor field) {
         return this.internalGetFieldAccessorTable().getField(field).getBuilder(this);
      }

      @Override
      public Message.Builder getRepeatedFieldBuilder(final Descriptors.FieldDescriptor field, int index) {
         return this.internalGetFieldAccessorTable().getField(field).getRepeatedBuilder(this, index);
      }

      @Override
      public boolean hasOneof(final Descriptors.OneofDescriptor oneof) {
         return this.internalGetFieldAccessorTable().getOneof(oneof).has(this);
      }

      @Override
      public Descriptors.FieldDescriptor getOneofFieldDescriptor(final Descriptors.OneofDescriptor oneof) {
         return this.internalGetFieldAccessorTable().getOneof(oneof).get(this);
      }

      @Override
      public boolean hasField(final Descriptors.FieldDescriptor field) {
         return this.internalGetFieldAccessorTable().getField(field).has(this);
      }

      @Override
      public Object getField(final Descriptors.FieldDescriptor field) {
         Object object = this.internalGetFieldAccessorTable().getField(field).get(this);
         return field.isRepeated() ? Collections.unmodifiableList((List)object) : object;
      }

      public BuilderT setField(final Descriptors.FieldDescriptor field, final Object value) {
         this.internalGetFieldAccessorTable().getField(field).set(this, value);
         return (BuilderT)this;
      }

      public BuilderT clearField(final Descriptors.FieldDescriptor field) {
         this.internalGetFieldAccessorTable().getField(field).clear(this);
         return (BuilderT)this;
      }

      public BuilderT clearOneof(final Descriptors.OneofDescriptor oneof) {
         this.internalGetFieldAccessorTable().getOneof(oneof).clear(this);
         return (BuilderT)this;
      }

      @Override
      public int getRepeatedFieldCount(final Descriptors.FieldDescriptor field) {
         return this.internalGetFieldAccessorTable().getField(field).getRepeatedCount(this);
      }

      @Override
      public Object getRepeatedField(final Descriptors.FieldDescriptor field, final int index) {
         return this.internalGetFieldAccessorTable().getField(field).getRepeated(this, index);
      }

      public BuilderT setRepeatedField(final Descriptors.FieldDescriptor field, final int index, final Object value) {
         this.internalGetFieldAccessorTable().getField(field).setRepeated(this, index, value);
         return (BuilderT)this;
      }

      public BuilderT addRepeatedField(final Descriptors.FieldDescriptor field, final Object value) {
         this.internalGetFieldAccessorTable().getField(field).addRepeated(this, value);
         return (BuilderT)this;
      }

      private BuilderT setUnknownFieldsInternal(final UnknownFieldSet unknownFields) {
         this.unknownFieldsOrBuilder = unknownFields;
         this.onChanged();
         return (BuilderT)this;
      }

      public BuilderT setUnknownFields(final UnknownFieldSet unknownFields) {
         return this.setUnknownFieldsInternal(unknownFields);
      }

      protected BuilderT setUnknownFieldsProto3(final UnknownFieldSet unknownFields) {
         return this.setUnknownFieldsInternal(unknownFields);
      }

      public BuilderT mergeUnknownFields(final UnknownFieldSet unknownFields) {
         if (UnknownFieldSet.getDefaultInstance().equals(unknownFields)) {
            return (BuilderT)this;
         } else if (UnknownFieldSet.getDefaultInstance().equals(this.unknownFieldsOrBuilder)) {
            this.unknownFieldsOrBuilder = unknownFields;
            this.onChanged();
            return (BuilderT)this;
         } else {
            this.getUnknownFieldSetBuilder().mergeFrom(unknownFields);
            this.onChanged();
            return (BuilderT)this;
         }
      }

      @Override
      public boolean isInitialized() {
         for (Descriptors.FieldDescriptor field : this.getDescriptorForType().getFields()) {
            if (field.isRequired() && !this.hasField(field)) {
               return false;
            }

            if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
               if (field.isRepeated()) {
                  for (Message element : (List)this.getField(field)) {
                     if (!element.isInitialized()) {
                        return false;
                     }
                  }
               } else if (this.hasField(field) && !((Message)this.getField(field)).isInitialized()) {
                  return false;
               }
            }
         }

         return true;
      }

      @Override
      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFieldsOrBuilder instanceof UnknownFieldSet
            ? (UnknownFieldSet)this.unknownFieldsOrBuilder
            : ((UnknownFieldSet.Builder)this.unknownFieldsOrBuilder).buildPartial();
      }

      protected boolean parseUnknownField(CodedInputStream input, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
         return input.shouldDiscardUnknownFields() ? input.skipField(tag) : this.getUnknownFieldSetBuilder().mergeFieldFrom(tag, input);
      }

      protected final void mergeUnknownLengthDelimitedField(int number, ByteString bytes) {
         this.getUnknownFieldSetBuilder().mergeLengthDelimitedField(number, bytes);
      }

      protected final void mergeUnknownVarintField(int number, int value) {
         this.getUnknownFieldSetBuilder().mergeVarintField(number, value);
      }

      @Override
      protected UnknownFieldSet.Builder getUnknownFieldSetBuilder() {
         if (this.unknownFieldsOrBuilder instanceof UnknownFieldSet) {
            this.unknownFieldsOrBuilder = ((UnknownFieldSet)this.unknownFieldsOrBuilder).toBuilder();
         }

         this.onChanged();
         return (UnknownFieldSet.Builder)this.unknownFieldsOrBuilder;
      }

      @Override
      protected void setUnknownFieldSetBuilder(UnknownFieldSet.Builder builder) {
         this.unknownFieldsOrBuilder = builder;
         this.onChanged();
      }

      protected GeneratedMessageV3.BuilderParent getParentForChildren() {
         if (this.meAsParent == null) {
            this.meAsParent = new GeneratedMessageV3.Builder.BuilderParentImpl();
         }

         return this.meAsParent;
      }

      protected final void onChanged() {
         if (this.isClean && this.builderParent != null) {
            this.builderParent.markDirty();
            this.isClean = false;
         }
      }

      protected MapFieldReflectionAccessor internalGetMapFieldReflection(int fieldNumber) {
         return this.internalGetMapField(fieldNumber);
      }

      @Deprecated
      protected MapField internalGetMapField(int fieldNumber) {
         throw new IllegalArgumentException("No map fields found in " + this.getClass().getName());
      }

      protected MapFieldReflectionAccessor internalGetMutableMapFieldReflection(int fieldNumber) {
         return this.internalGetMutableMapField(fieldNumber);
      }

      @Deprecated
      protected MapField internalGetMutableMapField(int fieldNumber) {
         throw new IllegalArgumentException("No map fields found in " + this.getClass().getName());
      }

      private class BuilderParentImpl implements GeneratedMessageV3.BuilderParent {
         private BuilderParentImpl() {
         }

         @Override
         public void markDirty() {
            Builder.this.onChanged();
         }
      }
   }

   protected interface BuilderParent extends AbstractMessage.BuilderParent {
   }

   public abstract static class ExtendableBuilder<MessageT extends GeneratedMessageV3.ExtendableMessage<MessageT>, BuilderT extends GeneratedMessageV3.ExtendableBuilder<MessageT, BuilderT>>
      extends GeneratedMessageV3.Builder<BuilderT>
      implements GeneratedMessageV3.ExtendableMessageOrBuilder<MessageT> {
      private FieldSet.Builder<Descriptors.FieldDescriptor> extensions;

      protected ExtendableBuilder() {
      }

      protected ExtendableBuilder(GeneratedMessageV3.BuilderParent parent) {
         super(parent);
      }

      void internalSetExtensionSet(FieldSet<Descriptors.FieldDescriptor> extensions) {
         this.extensions = FieldSet.Builder.fromFieldSet(extensions);
      }

      public BuilderT clear() {
         this.extensions = null;
         return super.clear();
      }

      private void ensureExtensionsIsMutable() {
         if (this.extensions == null) {
            this.extensions = FieldSet.newBuilder();
         }
      }

      private void verifyExtensionContainingType(final Extension<MessageT, ?> extension) {
         if (extension.getDescriptor().getContainingType() != this.getDescriptorForType()) {
            throw new IllegalArgumentException(
               "Extension is for type \""
                  + extension.getDescriptor().getContainingType().getFullName()
                  + "\" which does not match message type \""
                  + this.getDescriptorForType().getFullName()
                  + "\"."
            );
         }
      }

      @Override
      public final <T> boolean hasExtension(final ExtensionLite<MessageT, T> extensionLite) {
         Extension<MessageT, T> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         return this.extensions != null && this.extensions.hasField(extension.getDescriptor());
      }

      @Override
      public final <T> int getExtensionCount(final ExtensionLite<MessageT, List<T>> extensionLite) {
         Extension<MessageT, List<T>> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         return this.extensions == null ? 0 : this.extensions.getRepeatedFieldCount(descriptor);
      }

      @Override
      public final <T> T getExtension(final ExtensionLite<MessageT, T> extensionLite) {
         Extension<MessageT, T> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         Object value = this.extensions == null ? null : this.extensions.getField(descriptor);
         if (value == null) {
            if (descriptor.isRepeated()) {
               return (T)Collections.emptyList();
            } else {
               return (T)(descriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                  ? extension.getMessageDefaultInstance()
                  : extension.fromReflectionType(descriptor.getDefaultValue()));
            }
         } else {
            return (T)extension.fromReflectionType(value);
         }
      }

      @Override
      public final <T> T getExtension(final ExtensionLite<MessageT, List<T>> extensionLite, final int index) {
         Extension<MessageT, List<T>> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         if (this.extensions == null) {
            throw new IndexOutOfBoundsException();
         } else {
            return (T)extension.singularFromReflectionType(this.extensions.getRepeatedField(descriptor, index));
         }
      }

      public final <T> BuilderT setExtension(final ExtensionLite<MessageT, T> extensionLite, final T value) {
         Extension<MessageT, T> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         this.ensureExtensionsIsMutable();
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         this.extensions.setField(descriptor, extension.toReflectionType(value));
         this.onChanged();
         return (BuilderT)this;
      }

      public final <T> BuilderT setExtension(final ExtensionLite<MessageT, List<T>> extensionLite, final int index, final T value) {
         Extension<MessageT, List<T>> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         this.ensureExtensionsIsMutable();
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         this.extensions.setRepeatedField(descriptor, index, extension.singularToReflectionType(value));
         this.onChanged();
         return (BuilderT)this;
      }

      public final <T> BuilderT addExtension(final ExtensionLite<MessageT, List<T>> extensionLite, final T value) {
         Extension<MessageT, List<T>> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         this.ensureExtensionsIsMutable();
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         this.extensions.addRepeatedField(descriptor, extension.singularToReflectionType(value));
         this.onChanged();
         return (BuilderT)this;
      }

      public final <T> BuilderT clearExtension(final ExtensionLite<MessageT, T> extensionLite) {
         Extension<MessageT, T> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         this.ensureExtensionsIsMutable();
         this.extensions.clearField(extension.getDescriptor());
         this.onChanged();
         return (BuilderT)this;
      }

      @Override
      public final <T> boolean hasExtension(final Extension<MessageT, T> extension) {
         return this.hasExtension(extension);
      }

      @Override
      public final <T> boolean hasExtension(final GeneratedMessage.GeneratedExtension<MessageT, T> extension) {
         return this.hasExtension(extension);
      }

      @Override
      public final <T> int getExtensionCount(final Extension<MessageT, List<T>> extension) {
         return this.getExtensionCount(extension);
      }

      @Override
      public final <T> int getExtensionCount(final GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension) {
         return this.getExtensionCount(extension);
      }

      @Override
      public final <T> T getExtension(final Extension<MessageT, T> extension) {
         return this.getExtension(extension);
      }

      @Override
      public final <T> T getExtension(final GeneratedMessage.GeneratedExtension<MessageT, T> extension) {
         return this.getExtension(extension);
      }

      @Override
      public final <T> T getExtension(final Extension<MessageT, List<T>> extension, final int index) {
         return this.getExtension(extension, index);
      }

      @Override
      public final <T> T getExtension(final GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension, final int index) {
         return this.getExtension(extension, index);
      }

      public final <T> BuilderT setExtension(final Extension<MessageT, T> extension, final T value) {
         return this.setExtension(extension, value);
      }

      public <T> BuilderT setExtension(final GeneratedMessage.GeneratedExtension<MessageT, T> extension, final T value) {
         return this.setExtension(extension, value);
      }

      public final <T> BuilderT setExtension(final Extension<MessageT, List<T>> extension, final int index, final T value) {
         return this.setExtension(extension, index, value);
      }

      public <T> BuilderT setExtension(final GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension, final int index, final T value) {
         return this.setExtension(extension, index, value);
      }

      public final <T> BuilderT addExtension(final Extension<MessageT, List<T>> extension, final T value) {
         return this.addExtension(extension, value);
      }

      public <T> BuilderT addExtension(final GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension, final T value) {
         return this.addExtension(extension, value);
      }

      public final <T> BuilderT clearExtension(final Extension<MessageT, T> extension) {
         return this.clearExtension(extension);
      }

      public <T> BuilderT clearExtension(final GeneratedMessage.GeneratedExtension<MessageT, T> extension) {
         return this.clearExtension(extension);
      }

      protected boolean extensionsAreInitialized() {
         return this.extensions == null || this.extensions.isInitialized();
      }

      private FieldSet<Descriptors.FieldDescriptor> buildExtensions() {
         return this.extensions == null ? FieldSet.emptySet() : this.extensions.buildPartial();
      }

      @Override
      public boolean isInitialized() {
         return super.isInitialized() && this.extensionsAreInitialized();
      }

      @Override
      public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
         Map<Descriptors.FieldDescriptor, Object> result = super.getAllFieldsMutable();
         if (this.extensions != null) {
            result.putAll(this.extensions.getAllFields());
         }

         return Collections.unmodifiableMap(result);
      }

      @Override
      public Object getField(final Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            Object value = this.extensions == null ? null : this.extensions.getField(field);
            if (value == null) {
               return field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                  ? DynamicMessage.getDefaultInstance(field.getMessageType())
                  : field.getDefaultValue();
            } else {
               return value;
            }
         } else {
            return super.getField(field);
         }
      }

      @Override
      public Message.Builder getFieldBuilder(final Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {
               throw new UnsupportedOperationException("getFieldBuilder() called on a non-Message type.");
            } else {
               this.ensureExtensionsIsMutable();
               Object value = this.extensions.getFieldAllowBuilders(field);
               if (value == null) {
                  Message.Builder builder = DynamicMessage.newBuilder(field.getMessageType());
                  this.extensions.setField(field, builder);
                  this.onChanged();
                  return builder;
               } else if (value instanceof Message.Builder) {
                  return (Message.Builder)value;
               } else if (value instanceof Message) {
                  Message.Builder builder = ((Message)value).toBuilder();
                  this.extensions.setField(field, builder);
                  this.onChanged();
                  return builder;
               } else {
                  throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
               }
            }
         } else {
            return super.getFieldBuilder(field);
         }
      }

      @Override
      public int getRepeatedFieldCount(final Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            return this.extensions == null ? 0 : this.extensions.getRepeatedFieldCount(field);
         } else {
            return super.getRepeatedFieldCount(field);
         }
      }

      @Override
      public Object getRepeatedField(final Descriptors.FieldDescriptor field, final int index) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            if (this.extensions == null) {
               throw new IndexOutOfBoundsException();
            } else {
               return this.extensions.getRepeatedField(field, index);
            }
         } else {
            return super.getRepeatedField(field, index);
         }
      }

      @Override
      public Message.Builder getRepeatedFieldBuilder(final Descriptors.FieldDescriptor field, final int index) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            this.ensureExtensionsIsMutable();
            if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {
               throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
            } else {
               Object value = this.extensions.getRepeatedFieldAllowBuilders(field, index);
               if (value instanceof Message.Builder) {
                  return (Message.Builder)value;
               } else if (value instanceof Message) {
                  Message.Builder builder = ((Message)value).toBuilder();
                  this.extensions.setRepeatedField(field, index, builder);
                  this.onChanged();
                  return builder;
               } else {
                  throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
               }
            }
         } else {
            return super.getRepeatedFieldBuilder(field, index);
         }
      }

      @Override
      public boolean hasField(final Descriptors.FieldDescriptor field) {
         if (!field.isExtension()) {
            return super.hasField(field);
         } else {
            this.verifyContainingType(field);
            return this.extensions != null && this.extensions.hasField(field);
         }
      }

      public BuilderT setField(final Descriptors.FieldDescriptor field, final Object value) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            this.ensureExtensionsIsMutable();
            this.extensions.setField(field, value);
            this.onChanged();
            return (BuilderT)this;
         } else {
            return super.setField(field, value);
         }
      }

      public BuilderT clearField(final Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            this.ensureExtensionsIsMutable();
            this.extensions.clearField(field);
            this.onChanged();
            return (BuilderT)this;
         } else {
            return super.clearField(field);
         }
      }

      public BuilderT setRepeatedField(final Descriptors.FieldDescriptor field, final int index, final Object value) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            this.ensureExtensionsIsMutable();
            this.extensions.setRepeatedField(field, index, value);
            this.onChanged();
            return (BuilderT)this;
         } else {
            return super.setRepeatedField(field, index, value);
         }
      }

      public BuilderT addRepeatedField(final Descriptors.FieldDescriptor field, final Object value) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            this.ensureExtensionsIsMutable();
            this.extensions.addRepeatedField(field, value);
            this.onChanged();
            return (BuilderT)this;
         } else {
            return super.addRepeatedField(field, value);
         }
      }

      @Override
      public Message.Builder newBuilderForField(final Descriptors.FieldDescriptor field) {
         return (Message.Builder)(field.isExtension() ? DynamicMessage.newBuilder(field.getMessageType()) : super.newBuilderForField(field));
      }

      protected final void mergeExtensionFields(final GeneratedMessageV3.ExtendableMessage<?> other) {
         if (other.extensions != null) {
            this.ensureExtensionsIsMutable();
            this.extensions.mergeFrom(other.extensions);
            this.onChanged();
         }
      }

      @Override
      protected boolean parseUnknownField(CodedInputStream input, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
         this.ensureExtensionsIsMutable();
         return MessageReflection.mergeFieldFrom(
            input,
            input.shouldDiscardUnknownFields() ? null : this.getUnknownFieldSetBuilder(),
            extensionRegistry,
            this.getDescriptorForType(),
            new MessageReflection.ExtensionBuilderAdapter(this.extensions),
            tag
         );
      }

      private void verifyContainingType(final Descriptors.FieldDescriptor field) {
         if (field.getContainingType() != this.getDescriptorForType()) {
            throw new IllegalArgumentException("FieldDescriptor does not match message type.");
         }
      }
   }

   public abstract static class ExtendableMessage<MessageT extends GeneratedMessageV3.ExtendableMessage<MessageT>>
      extends GeneratedMessageV3
      implements GeneratedMessageV3.ExtendableMessageOrBuilder<MessageT> {
      private static final long serialVersionUID = 1L;
      private final FieldSet<Descriptors.FieldDescriptor> extensions;

      protected ExtendableMessage() {
         this.extensions = FieldSet.newFieldSet();
      }

      protected ExtendableMessage(GeneratedMessageV3.ExtendableBuilder<MessageT, ?> builder) {
         super(builder);
         this.extensions = builder.buildExtensions();
      }

      private void verifyExtensionContainingType(final Extension<MessageT, ?> extension) {
         if (extension.getDescriptor().getContainingType() != this.getDescriptorForType()) {
            throw new IllegalArgumentException(
               "Extension is for type \""
                  + extension.getDescriptor().getContainingType().getFullName()
                  + "\" which does not match message type \""
                  + this.getDescriptorForType().getFullName()
                  + "\"."
            );
         }
      }

      @Override
      public final <T> boolean hasExtension(final ExtensionLite<MessageT, T> extensionLite) {
         Extension<MessageT, T> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         return this.extensions.hasField(extension.getDescriptor());
      }

      @Override
      public final <T> int getExtensionCount(final ExtensionLite<MessageT, List<T>> extensionLite) {
         Extension<MessageT, List<T>> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         return this.extensions.getRepeatedFieldCount(descriptor);
      }

      @Override
      public final <T> T getExtension(final ExtensionLite<MessageT, T> extensionLite) {
         Extension<MessageT, T> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         Object value = this.extensions.getField(descriptor);
         if (value == null) {
            if (descriptor.isRepeated()) {
               return (T)Collections.emptyList();
            } else {
               return (T)(descriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                  ? extension.getMessageDefaultInstance()
                  : extension.fromReflectionType(descriptor.getDefaultValue()));
            }
         } else {
            return (T)extension.fromReflectionType(value);
         }
      }

      @Override
      public final <T> T getExtension(final ExtensionLite<MessageT, List<T>> extensionLite, final int index) {
         Extension<MessageT, List<T>> extension = GeneratedMessageV3.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         return (T)extension.singularFromReflectionType(this.extensions.getRepeatedField(descriptor, index));
      }

      @Override
      public final <T> boolean hasExtension(final Extension<MessageT, T> extension) {
         return this.hasExtension(extension);
      }

      @Override
      public final <T> boolean hasExtension(final GeneratedMessage.GeneratedExtension<MessageT, T> extension) {
         return this.hasExtension(extension);
      }

      @Override
      public final <T> int getExtensionCount(final Extension<MessageT, List<T>> extension) {
         return this.getExtensionCount(extension);
      }

      @Override
      public final <T> int getExtensionCount(final GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension) {
         return this.getExtensionCount(extension);
      }

      @Override
      public final <T> T getExtension(final Extension<MessageT, T> extension) {
         return this.getExtension(extension);
      }

      @Override
      public final <T> T getExtension(final GeneratedMessage.GeneratedExtension<MessageT, T> extension) {
         return this.getExtension(extension);
      }

      @Override
      public final <T> T getExtension(final Extension<MessageT, List<T>> extension, final int index) {
         return this.getExtension(extension, index);
      }

      @Override
      public final <T> T getExtension(final GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension, final int index) {
         return this.getExtension(extension, index);
      }

      protected boolean extensionsAreInitialized() {
         return this.extensions.isInitialized();
      }

      @Override
      public boolean isInitialized() {
         return super.isInitialized() && this.extensionsAreInitialized();
      }

      @Override
      protected boolean parseUnknownField(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
         return MessageReflection.mergeFieldFrom(
            input,
            input.shouldDiscardUnknownFields() ? null : unknownFields,
            extensionRegistry,
            this.getDescriptorForType(),
            new MessageReflection.ExtensionAdapter(this.extensions),
            tag
         );
      }

      @Override
      protected boolean parseUnknownFieldProto3(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
         return this.parseUnknownField(input, unknownFields, extensionRegistry, tag);
      }

      @Override
      protected void makeExtensionsImmutable() {
         this.extensions.makeImmutable();
      }

      protected GeneratedMessageV3.ExtendableMessage<MessageT>.ExtensionWriter newExtensionWriter() {
         return new GeneratedMessageV3.ExtendableMessage.ExtensionWriter(false);
      }

      protected GeneratedMessageV3.ExtendableMessage<MessageT>.ExtensionWriter newMessageSetExtensionWriter() {
         return new GeneratedMessageV3.ExtendableMessage.ExtensionWriter(true);
      }

      protected int extensionsSerializedSize() {
         return this.extensions.getSerializedSize();
      }

      protected int extensionsSerializedSizeAsMessageSet() {
         return this.extensions.getMessageSetSerializedSize();
      }

      protected Map<Descriptors.FieldDescriptor, Object> getExtensionFields() {
         return this.extensions.getAllFields();
      }

      @Override
      public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
         Map<Descriptors.FieldDescriptor, Object> result = super.getAllFieldsMutable(false);
         result.putAll(this.getExtensionFields());
         return Collections.unmodifiableMap(result);
      }

      @Override
      public Map<Descriptors.FieldDescriptor, Object> getAllFieldsRaw() {
         Map<Descriptors.FieldDescriptor, Object> result = super.getAllFieldsMutable(false);
         result.putAll(this.getExtensionFields());
         return Collections.unmodifiableMap(result);
      }

      @Override
      public boolean hasField(final Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            return this.extensions.hasField(field);
         } else {
            return super.hasField(field);
         }
      }

      @Override
      public Object getField(final Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            Object value = this.extensions.getField(field);
            if (value == null) {
               if (field.isRepeated()) {
                  return Collections.emptyList();
               } else {
                  return field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                     ? DynamicMessage.getDefaultInstance(field.getMessageType())
                     : field.getDefaultValue();
               }
            } else {
               return value;
            }
         } else {
            return super.getField(field);
         }
      }

      @Override
      public int getRepeatedFieldCount(final Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            return this.extensions.getRepeatedFieldCount(field);
         } else {
            return super.getRepeatedFieldCount(field);
         }
      }

      @Override
      public Object getRepeatedField(final Descriptors.FieldDescriptor field, final int index) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            return this.extensions.getRepeatedField(field, index);
         } else {
            return super.getRepeatedField(field, index);
         }
      }

      private void verifyContainingType(final Descriptors.FieldDescriptor field) {
         if (field.getContainingType() != this.getDescriptorForType()) {
            throw new IllegalArgumentException("FieldDescriptor does not match message type.");
         }
      }

      protected class ExtensionWriter {
         private final Iterator<Entry<Descriptors.FieldDescriptor, Object>> iter = ExtendableMessage.this.extensions.iterator();
         private Entry<Descriptors.FieldDescriptor, Object> next;
         private final boolean messageSetWireFormat;

         private ExtensionWriter(final boolean messageSetWireFormat) {
            if (this.iter.hasNext()) {
               this.next = this.iter.next();
            }

            this.messageSetWireFormat = messageSetWireFormat;
         }

         public void writeUntil(final int end, final CodedOutputStream output) throws IOException {
            while (this.next != null && this.next.getKey().getNumber() < end) {
               Descriptors.FieldDescriptor descriptor = this.next.getKey();
               if (!this.messageSetWireFormat || descriptor.getLiteJavaType() != WireFormat.JavaType.MESSAGE || descriptor.isRepeated()) {
                  FieldSet.writeField(descriptor, this.next.getValue(), output);
               } else if (this.next instanceof LazyField.LazyEntry) {
                  output.writeRawMessageSetExtension(descriptor.getNumber(), ((LazyField.LazyEntry)this.next).getField().toByteString());
               } else {
                  output.writeMessageSetExtension(descriptor.getNumber(), (Message)this.next.getValue());
               }

               if (this.iter.hasNext()) {
                  this.next = this.iter.next();
               } else {
                  this.next = null;
               }
            }
         }
      }
   }

   public interface ExtendableMessageOrBuilder<MessageT extends GeneratedMessageV3.ExtendableMessage<MessageT>> extends MessageOrBuilder {
      @Override
      Message getDefaultInstanceForType();

      <T> boolean hasExtension(ExtensionLite<MessageT, T> extension);

      <T> int getExtensionCount(ExtensionLite<MessageT, List<T>> extension);

      <T> T getExtension(ExtensionLite<MessageT, T> extension);

      <T> T getExtension(ExtensionLite<MessageT, List<T>> extension, int index);

      <T> boolean hasExtension(Extension<MessageT, T> extension);

      <T> boolean hasExtension(GeneratedMessage.GeneratedExtension<MessageT, T> extension);

      <T> int getExtensionCount(Extension<MessageT, List<T>> extension);

      <T> int getExtensionCount(GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension);

      <T> T getExtension(Extension<MessageT, T> extension);

      <T> T getExtension(GeneratedMessage.GeneratedExtension<MessageT, T> extension);

      <T> T getExtension(Extension<MessageT, List<T>> extension, int index);

      <T> T getExtension(GeneratedMessage.GeneratedExtension<MessageT, List<T>> extension, int index);
   }

   interface ExtensionDescriptorRetriever {
      Descriptors.FieldDescriptor getDescriptor();
   }

   public static final class FieldAccessorTable {
      private final Descriptors.Descriptor descriptor;
      private final GeneratedMessageV3.FieldAccessorTable.FieldAccessor[] fields;
      private String[] camelCaseNames;
      private final GeneratedMessageV3.FieldAccessorTable.OneofAccessor[] oneofs;
      private volatile boolean initialized;

      public FieldAccessorTable(
         final Descriptors.Descriptor descriptor,
         final String[] camelCaseNames,
         final Class<? extends GeneratedMessageV3> messageClass,
         final Class<? extends GeneratedMessageV3.Builder<?>> builderClass
      ) {
         this(descriptor, camelCaseNames);
         this.ensureFieldAccessorsInitialized(messageClass, builderClass);
      }

      public FieldAccessorTable(final Descriptors.Descriptor descriptor, final String[] camelCaseNames) {
         this.descriptor = descriptor;
         this.camelCaseNames = camelCaseNames;
         this.fields = new GeneratedMessageV3.FieldAccessorTable.FieldAccessor[descriptor.getFields().size()];
         this.oneofs = new GeneratedMessageV3.FieldAccessorTable.OneofAccessor[descriptor.getOneofs().size()];
         this.initialized = false;
      }

      public GeneratedMessageV3.FieldAccessorTable ensureFieldAccessorsInitialized(
         Class<? extends GeneratedMessageV3> messageClass, Class<? extends GeneratedMessageV3.Builder<?>> builderClass
      ) {
         if (this.initialized) {
            return this;
         } else {
            synchronized (this) {
               if (this.initialized) {
                  return this;
               } else {
                  int fieldsSize = this.fields.length;

                  for (int i = 0; i < fieldsSize; i++) {
                     Descriptors.FieldDescriptor field = this.descriptor.getFields().get(i);
                     String containingOneofCamelCaseName = null;
                     if (field.getContainingOneof() != null) {
                        int index = fieldsSize + field.getContainingOneof().getIndex();
                        if (index < this.camelCaseNames.length) {
                           containingOneofCamelCaseName = this.camelCaseNames[index];
                        }
                     }

                     if (field.isRepeated()) {
                        if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                           if (field.isMapField()) {
                              this.fields[i] = new GeneratedMessageV3.FieldAccessorTable.MapFieldAccessor(field, messageClass);
                           } else {
                              this.fields[i] = new GeneratedMessageV3.FieldAccessorTable.RepeatedMessageFieldAccessor(
                                 field, this.camelCaseNames[i], messageClass, builderClass
                              );
                           }
                        } else if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
                           this.fields[i] = new GeneratedMessageV3.FieldAccessorTable.RepeatedEnumFieldAccessor(
                              field, this.camelCaseNames[i], messageClass, builderClass
                           );
                        } else {
                           this.fields[i] = new GeneratedMessageV3.FieldAccessorTable.RepeatedFieldAccessor(
                              field, this.camelCaseNames[i], messageClass, builderClass
                           );
                        }
                     } else if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                        this.fields[i] = new GeneratedMessageV3.FieldAccessorTable.SingularMessageFieldAccessor(
                           field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName
                        );
                     } else if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
                        this.fields[i] = new GeneratedMessageV3.FieldAccessorTable.SingularEnumFieldAccessor(
                           field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName
                        );
                     } else if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING) {
                        this.fields[i] = new GeneratedMessageV3.FieldAccessorTable.SingularStringFieldAccessor(
                           field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName
                        );
                     } else {
                        this.fields[i] = new GeneratedMessageV3.FieldAccessorTable.SingularFieldAccessor(
                           field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName
                        );
                     }
                  }

                  for (int i = 0; i < this.descriptor.getOneofs().size(); i++) {
                     if (i < this.descriptor.getRealOneofs().size()) {
                        this.oneofs[i] = new GeneratedMessageV3.FieldAccessorTable.RealOneofAccessor(
                           this.descriptor, i, this.camelCaseNames[i + fieldsSize], messageClass, builderClass
                        );
                     } else {
                        this.oneofs[i] = new GeneratedMessageV3.FieldAccessorTable.SyntheticOneofAccessor(this.descriptor, i);
                     }
                  }

                  this.initialized = true;
                  this.camelCaseNames = null;
                  return this;
               }
            }
         }
      }

      private GeneratedMessageV3.FieldAccessorTable.FieldAccessor getField(final Descriptors.FieldDescriptor field) {
         if (field.getContainingType() != this.descriptor) {
            throw new IllegalArgumentException("FieldDescriptor does not match message type.");
         } else if (field.isExtension()) {
            throw new IllegalArgumentException("This type does not have extensions.");
         } else {
            return this.fields[field.getIndex()];
         }
      }

      private GeneratedMessageV3.FieldAccessorTable.OneofAccessor getOneof(final Descriptors.OneofDescriptor oneof) {
         if (oneof.getContainingType() != this.descriptor) {
            throw new IllegalArgumentException("OneofDescriptor does not match message type.");
         } else {
            return this.oneofs[oneof.getIndex()];
         }
      }

      private interface FieldAccessor {
         Object get(GeneratedMessageV3 message);

         Object get(GeneratedMessageV3.Builder<?> builder);

         Object getRaw(GeneratedMessageV3 message);

         void set(GeneratedMessageV3.Builder<?> builder, Object value);

         Object getRepeated(GeneratedMessageV3 message, int index);

         Object getRepeated(GeneratedMessageV3.Builder<?> builder, int index);

         void setRepeated(GeneratedMessageV3.Builder<?> builder, int index, Object value);

         void addRepeated(GeneratedMessageV3.Builder<?> builder, Object value);

         boolean has(GeneratedMessageV3 message);

         boolean has(GeneratedMessageV3.Builder<?> builder);

         int getRepeatedCount(GeneratedMessageV3 message);

         int getRepeatedCount(GeneratedMessageV3.Builder<?> builder);

         void clear(GeneratedMessageV3.Builder<?> builder);

         Message.Builder newBuilder();

         Message.Builder getBuilder(GeneratedMessageV3.Builder<?> builder);

         Message.Builder getRepeatedBuilder(GeneratedMessageV3.Builder<?> builder, int index);
      }

      private static class MapFieldAccessor implements GeneratedMessageV3.FieldAccessorTable.FieldAccessor {
         private final Descriptors.FieldDescriptor field;
         private final Message mapEntryMessageDefaultInstance;

         MapFieldAccessor(final Descriptors.FieldDescriptor descriptor, final Class<? extends GeneratedMessageV3> messageClass) {
            this.field = descriptor;
            java.lang.reflect.Method getDefaultInstanceMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "getDefaultInstance");
            MapFieldReflectionAccessor defaultMapField = this.getMapField((GeneratedMessageV3)GeneratedMessageV3.invokeOrDie(getDefaultInstanceMethod, null));
            this.mapEntryMessageDefaultInstance = defaultMapField.getMapEntryMessageDefaultInstance();
         }

         private MapFieldReflectionAccessor getMapField(GeneratedMessageV3 message) {
            return message.internalGetMapFieldReflection(this.field.getNumber());
         }

         private MapFieldReflectionAccessor getMapField(GeneratedMessageV3.Builder<?> builder) {
            return builder.internalGetMapFieldReflection(this.field.getNumber());
         }

         private MapFieldReflectionAccessor getMutableMapField(GeneratedMessageV3.Builder<?> builder) {
            return builder.internalGetMutableMapFieldReflection(this.field.getNumber());
         }

         private Message coerceType(Message value) {
            if (value == null) {
               return null;
            } else {
               return this.mapEntryMessageDefaultInstance.getClass().isInstance(value)
                  ? value
                  : this.mapEntryMessageDefaultInstance.toBuilder().mergeFrom(value).build();
            }
         }

         @Override
         public Object get(GeneratedMessageV3 message) {
            List<Object> result = new ArrayList<>();

            for (int i = 0; i < this.getRepeatedCount(message); i++) {
               result.add(this.getRepeated(message, i));
            }

            return Collections.unmodifiableList(result);
         }

         @Override
         public Object get(GeneratedMessageV3.Builder<?> builder) {
            List<Object> result = new ArrayList<>();

            for (int i = 0; i < this.getRepeatedCount(builder); i++) {
               result.add(this.getRepeated(builder, i));
            }

            return Collections.unmodifiableList(result);
         }

         @Override
         public Object getRaw(GeneratedMessageV3 message) {
            return this.get(message);
         }

         @Override
         public void set(GeneratedMessageV3.Builder<?> builder, Object value) {
            this.clear(builder);

            for (Object entry : (List)value) {
               this.addRepeated(builder, entry);
            }
         }

         @Override
         public Object getRepeated(GeneratedMessageV3 message, int index) {
            return this.getMapField(message).getList().get(index);
         }

         @Override
         public Object getRepeated(GeneratedMessageV3.Builder<?> builder, int index) {
            return this.getMapField(builder).getList().get(index);
         }

         @Override
         public void setRepeated(GeneratedMessageV3.Builder<?> builder, int index, Object value) {
            this.getMutableMapField(builder).getMutableList().set(index, this.coerceType((Message)value));
         }

         @Override
         public void addRepeated(GeneratedMessageV3.Builder<?> builder, Object value) {
            this.getMutableMapField(builder).getMutableList().add(this.coerceType((Message)value));
         }

         @Override
         public boolean has(GeneratedMessageV3 message) {
            throw new UnsupportedOperationException("hasField() is not supported for repeated fields.");
         }

         @Override
         public boolean has(GeneratedMessageV3.Builder<?> builder) {
            throw new UnsupportedOperationException("hasField() is not supported for repeated fields.");
         }

         @Override
         public int getRepeatedCount(GeneratedMessageV3 message) {
            return this.getMapField(message).getList().size();
         }

         @Override
         public int getRepeatedCount(GeneratedMessageV3.Builder<?> builder) {
            return this.getMapField(builder).getList().size();
         }

         @Override
         public void clear(GeneratedMessageV3.Builder<?> builder) {
            this.getMutableMapField(builder).getMutableList().clear();
         }

         @Override
         public Message.Builder newBuilder() {
            return this.mapEntryMessageDefaultInstance.newBuilderForType();
         }

         @Override
         public Message.Builder getBuilder(GeneratedMessageV3.Builder<?> builder) {
            throw new UnsupportedOperationException("Nested builder not supported for map fields.");
         }

         @Override
         public Message.Builder getRepeatedBuilder(GeneratedMessageV3.Builder<?> builder, int index) {
            throw new UnsupportedOperationException("Map fields cannot be repeated");
         }
      }

      private interface OneofAccessor {
         boolean has(final GeneratedMessageV3 message);

         boolean has(GeneratedMessageV3.Builder<?> builder);

         Descriptors.FieldDescriptor get(final GeneratedMessageV3 message);

         Descriptors.FieldDescriptor get(GeneratedMessageV3.Builder<?> builder);

         void clear(final GeneratedMessageV3.Builder<?> builder);
      }

      private static class RealOneofAccessor implements GeneratedMessageV3.FieldAccessorTable.OneofAccessor {
         private final Descriptors.Descriptor descriptor;
         private final java.lang.reflect.Method caseMethod;
         private final java.lang.reflect.Method caseMethodBuilder;
         private final java.lang.reflect.Method clearMethod;

         RealOneofAccessor(
            final Descriptors.Descriptor descriptor,
            final int oneofIndex,
            final String camelCaseName,
            final Class<? extends GeneratedMessageV3> messageClass,
            final Class<? extends GeneratedMessageV3.Builder<?>> builderClass
         ) {
            this.descriptor = descriptor;
            this.caseMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "Case");
            this.caseMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Case");
            this.clearMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "clear" + camelCaseName);
         }

         @Override
         public boolean has(final GeneratedMessageV3 message) {
            return ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethod, message)).getNumber() != 0;
         }

         @Override
         public boolean has(GeneratedMessageV3.Builder<?> builder) {
            return ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethodBuilder, builder)).getNumber() != 0;
         }

         @Override
         public Descriptors.FieldDescriptor get(final GeneratedMessageV3 message) {
            int fieldNumber = ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethod, message)).getNumber();
            return fieldNumber > 0 ? this.descriptor.findFieldByNumber(fieldNumber) : null;
         }

         @Override
         public Descriptors.FieldDescriptor get(GeneratedMessageV3.Builder<?> builder) {
            int fieldNumber = ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethodBuilder, builder)).getNumber();
            return fieldNumber > 0 ? this.descriptor.findFieldByNumber(fieldNumber) : null;
         }

         @Override
         public void clear(final GeneratedMessageV3.Builder<?> builder) {
            Object unused = GeneratedMessageV3.invokeOrDie(this.clearMethod, builder);
         }
      }

      private static final class RepeatedEnumFieldAccessor extends GeneratedMessageV3.FieldAccessorTable.RepeatedFieldAccessor {
         private final Descriptors.EnumDescriptor enumDescriptor;
         private final java.lang.reflect.Method valueOfMethod;
         private final java.lang.reflect.Method getValueDescriptorMethod;
         private final boolean supportUnknownEnumValue;
         private java.lang.reflect.Method getRepeatedValueMethod;
         private java.lang.reflect.Method getRepeatedValueMethodBuilder;
         private java.lang.reflect.Method setRepeatedValueMethod;
         private java.lang.reflect.Method addRepeatedValueMethod;

         RepeatedEnumFieldAccessor(
            final Descriptors.FieldDescriptor descriptor,
            final String camelCaseName,
            final Class<? extends GeneratedMessageV3> messageClass,
            final Class<? extends GeneratedMessageV3.Builder<?>> builderClass
         ) {
            super(descriptor, camelCaseName, messageClass, builderClass);
            this.enumDescriptor = descriptor.getEnumType();
            this.valueOfMethod = GeneratedMessageV3.getMethodOrDie(this.type, "valueOf", Descriptors.EnumValueDescriptor.class);
            this.getValueDescriptorMethod = GeneratedMessageV3.getMethodOrDie(this.type, "getValueDescriptor");
            this.supportUnknownEnumValue = !descriptor.legacyEnumFieldTreatedAsClosed();
            if (this.supportUnknownEnumValue) {
               this.getRepeatedValueMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "Value", int.class);
               this.getRepeatedValueMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Value", int.class);
               this.setRepeatedValueMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "set" + camelCaseName + "Value", int.class, int.class);
               this.addRepeatedValueMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "add" + camelCaseName + "Value", int.class);
            }
         }

         @Override
         public Object get(final GeneratedMessageV3 message) {
            List<Object> newList = new ArrayList<>();
            int size = this.getRepeatedCount(message);

            for (int i = 0; i < size; i++) {
               newList.add(this.getRepeated(message, i));
            }

            return Collections.unmodifiableList(newList);
         }

         @Override
         public Object get(final GeneratedMessageV3.Builder<?> builder) {
            List<Object> newList = new ArrayList<>();
            int size = this.getRepeatedCount(builder);

            for (int i = 0; i < size; i++) {
               newList.add(this.getRepeated(builder, i));
            }

            return Collections.unmodifiableList(newList);
         }

         @Override
         public Object getRepeated(final GeneratedMessageV3 message, final int index) {
            if (this.supportUnknownEnumValue) {
               int value = (Integer)GeneratedMessageV3.invokeOrDie(this.getRepeatedValueMethod, message, index);
               return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
            } else {
               return GeneratedMessageV3.invokeOrDie(this.getValueDescriptorMethod, super.getRepeated(message, index));
            }
         }

         @Override
         public Object getRepeated(final GeneratedMessageV3.Builder<?> builder, final int index) {
            if (this.supportUnknownEnumValue) {
               int value = (Integer)GeneratedMessageV3.invokeOrDie(this.getRepeatedValueMethodBuilder, builder, index);
               return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
            } else {
               return GeneratedMessageV3.invokeOrDie(this.getValueDescriptorMethod, super.getRepeated(builder, index));
            }
         }

         @Override
         public void setRepeated(final GeneratedMessageV3.Builder<?> builder, final int index, final Object value) {
            if (this.supportUnknownEnumValue) {
               Object unused = GeneratedMessageV3.invokeOrDie(this.setRepeatedValueMethod, builder, index, ((Descriptors.EnumValueDescriptor)value).getNumber());
            } else {
               super.setRepeated(builder, index, GeneratedMessageV3.invokeOrDie(this.valueOfMethod, null, value));
            }
         }

         @Override
         public void addRepeated(final GeneratedMessageV3.Builder<?> builder, final Object value) {
            if (this.supportUnknownEnumValue) {
               Object unused = GeneratedMessageV3.invokeOrDie(this.addRepeatedValueMethod, builder, ((Descriptors.EnumValueDescriptor)value).getNumber());
            } else {
               super.addRepeated(builder, GeneratedMessageV3.invokeOrDie(this.valueOfMethod, null, value));
            }
         }
      }

      private static class RepeatedFieldAccessor implements GeneratedMessageV3.FieldAccessorTable.FieldAccessor {
         protected final Class<?> type;
         protected final GeneratedMessageV3.FieldAccessorTable.RepeatedFieldAccessor.MethodInvoker invoker;

         RepeatedFieldAccessor(
            final Descriptors.FieldDescriptor descriptor,
            final String camelCaseName,
            final Class<? extends GeneratedMessageV3> messageClass,
            final Class<? extends GeneratedMessageV3.Builder<?>> builderClass
         ) {
            GeneratedMessageV3.FieldAccessorTable.RepeatedFieldAccessor.ReflectionInvoker reflectionInvoker = new GeneratedMessageV3.FieldAccessorTable.RepeatedFieldAccessor.ReflectionInvoker(
               descriptor, camelCaseName, messageClass, builderClass
            );
            this.type = reflectionInvoker.getRepeatedMethod.getReturnType();
            this.invoker = getMethodInvoker(reflectionInvoker);
         }

         static GeneratedMessageV3.FieldAccessorTable.RepeatedFieldAccessor.MethodInvoker getMethodInvoker(
            GeneratedMessageV3.FieldAccessorTable.RepeatedFieldAccessor.ReflectionInvoker accessor
         ) {
            return accessor;
         }

         @Override
         public Object get(final GeneratedMessageV3 message) {
            return this.invoker.get(message);
         }

         @Override
         public Object get(GeneratedMessageV3.Builder<?> builder) {
            return this.invoker.get(builder);
         }

         @Override
         public Object getRaw(final GeneratedMessageV3 message) {
            return this.get(message);
         }

         @Override
         public void set(final GeneratedMessageV3.Builder<?> builder, final Object value) {
            this.clear(builder);

            for (Object element : (List)value) {
               this.addRepeated(builder, element);
            }
         }

         @Override
         public Object getRepeated(final GeneratedMessageV3 message, final int index) {
            return this.invoker.getRepeated(message, index);
         }

         @Override
         public Object getRepeated(GeneratedMessageV3.Builder<?> builder, int index) {
            return this.invoker.getRepeated(builder, index);
         }

         @Override
         public void setRepeated(final GeneratedMessageV3.Builder<?> builder, final int index, final Object value) {
            this.invoker.setRepeated(builder, index, value);
         }

         @Override
         public void addRepeated(final GeneratedMessageV3.Builder<?> builder, final Object value) {
            this.invoker.addRepeated(builder, value);
         }

         @Override
         public boolean has(final GeneratedMessageV3 message) {
            throw new UnsupportedOperationException("hasField() called on a repeated field.");
         }

         @Override
         public boolean has(GeneratedMessageV3.Builder<?> builder) {
            throw new UnsupportedOperationException("hasField() called on a repeated field.");
         }

         @Override
         public int getRepeatedCount(final GeneratedMessageV3 message) {
            return this.invoker.getRepeatedCount(message);
         }

         @Override
         public int getRepeatedCount(GeneratedMessageV3.Builder<?> builder) {
            return this.invoker.getRepeatedCount(builder);
         }

         @Override
         public void clear(final GeneratedMessageV3.Builder<?> builder) {
            this.invoker.clear(builder);
         }

         @Override
         public Message.Builder newBuilder() {
            throw new UnsupportedOperationException("newBuilderForField() called on a non-Message type.");
         }

         @Override
         public Message.Builder getBuilder(GeneratedMessageV3.Builder<?> builder) {
            throw new UnsupportedOperationException("getFieldBuilder() called on a non-Message type.");
         }

         @Override
         public Message.Builder getRepeatedBuilder(GeneratedMessageV3.Builder<?> builder, int index) {
            throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
         }

         interface MethodInvoker {
            Object get(final GeneratedMessageV3 message);

            Object get(GeneratedMessageV3.Builder<?> builder);

            Object getRepeated(final GeneratedMessageV3 message, final int index);

            Object getRepeated(GeneratedMessageV3.Builder<?> builder, int index);

            void setRepeated(final GeneratedMessageV3.Builder<?> builder, final int index, final Object value);

            void addRepeated(final GeneratedMessageV3.Builder<?> builder, final Object value);

            int getRepeatedCount(final GeneratedMessageV3 message);

            int getRepeatedCount(GeneratedMessageV3.Builder<?> builder);

            void clear(final GeneratedMessageV3.Builder<?> builder);
         }

         private static final class ReflectionInvoker implements GeneratedMessageV3.FieldAccessorTable.RepeatedFieldAccessor.MethodInvoker {
            private final java.lang.reflect.Method getMethod;
            private final java.lang.reflect.Method getMethodBuilder;
            private final java.lang.reflect.Method getRepeatedMethod;
            private final java.lang.reflect.Method getRepeatedMethodBuilder;
            private final java.lang.reflect.Method setRepeatedMethod;
            private final java.lang.reflect.Method addRepeatedMethod;
            private final java.lang.reflect.Method getCountMethod;
            private final java.lang.reflect.Method getCountMethodBuilder;
            private final java.lang.reflect.Method clearMethod;

            ReflectionInvoker(
               final Descriptors.FieldDescriptor descriptor,
               final String camelCaseName,
               final Class<? extends GeneratedMessageV3> messageClass,
               final Class<? extends GeneratedMessageV3.Builder<?>> builderClass
            ) {
               this.getMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "List");
               this.getMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "List");
               this.getRepeatedMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName, int.class);
               this.getRepeatedMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName, int.class);
               Class<?> type = this.getRepeatedMethod.getReturnType();
               this.setRepeatedMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "set" + camelCaseName, int.class, type);
               this.addRepeatedMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "add" + camelCaseName, type);
               this.getCountMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "Count");
               this.getCountMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Count");
               this.clearMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "clear" + camelCaseName);
            }

            @Override
            public Object get(final GeneratedMessageV3 message) {
               return GeneratedMessageV3.invokeOrDie(this.getMethod, message);
            }

            @Override
            public Object get(GeneratedMessageV3.Builder<?> builder) {
               return GeneratedMessageV3.invokeOrDie(this.getMethodBuilder, builder);
            }

            @Override
            public Object getRepeated(final GeneratedMessageV3 message, final int index) {
               return GeneratedMessageV3.invokeOrDie(this.getRepeatedMethod, message, index);
            }

            @Override
            public Object getRepeated(GeneratedMessageV3.Builder<?> builder, int index) {
               return GeneratedMessageV3.invokeOrDie(this.getRepeatedMethodBuilder, builder, index);
            }

            @Override
            public void setRepeated(final GeneratedMessageV3.Builder<?> builder, final int index, final Object value) {
               Object unused = GeneratedMessageV3.invokeOrDie(this.setRepeatedMethod, builder, index, value);
            }

            @Override
            public void addRepeated(final GeneratedMessageV3.Builder<?> builder, final Object value) {
               Object unused = GeneratedMessageV3.invokeOrDie(this.addRepeatedMethod, builder, value);
            }

            @Override
            public int getRepeatedCount(final GeneratedMessageV3 message) {
               return (Integer)GeneratedMessageV3.invokeOrDie(this.getCountMethod, message);
            }

            @Override
            public int getRepeatedCount(GeneratedMessageV3.Builder<?> builder) {
               return (Integer)GeneratedMessageV3.invokeOrDie(this.getCountMethodBuilder, builder);
            }

            @Override
            public void clear(final GeneratedMessageV3.Builder<?> builder) {
               Object unused = GeneratedMessageV3.invokeOrDie(this.clearMethod, builder);
            }
         }
      }

      private static final class RepeatedMessageFieldAccessor extends GeneratedMessageV3.FieldAccessorTable.RepeatedFieldAccessor {
         private final java.lang.reflect.Method newBuilderMethod = GeneratedMessageV3.getMethodOrDie(this.type, "newBuilder");
         private final java.lang.reflect.Method getBuilderMethodBuilder;

         RepeatedMessageFieldAccessor(
            final Descriptors.FieldDescriptor descriptor,
            final String camelCaseName,
            final Class<? extends GeneratedMessageV3> messageClass,
            final Class<? extends GeneratedMessageV3.Builder<?>> builderClass
         ) {
            super(descriptor, camelCaseName, messageClass, builderClass);
            this.getBuilderMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Builder", int.class);
         }

         private Object coerceType(final Object value) {
            return this.type.isInstance(value)
               ? value
               : ((Message.Builder)GeneratedMessageV3.invokeOrDie(this.newBuilderMethod, null)).mergeFrom((Message)value).build();
         }

         @Override
         public void setRepeated(final GeneratedMessageV3.Builder<?> builder, final int index, final Object value) {
            super.setRepeated(builder, index, this.coerceType(value));
         }

         @Override
         public void addRepeated(final GeneratedMessageV3.Builder<?> builder, final Object value) {
            super.addRepeated(builder, this.coerceType(value));
         }

         @Override
         public Message.Builder newBuilder() {
            return (Message.Builder)GeneratedMessageV3.invokeOrDie(this.newBuilderMethod, null);
         }

         @Override
         public Message.Builder getRepeatedBuilder(final GeneratedMessageV3.Builder<?> builder, final int index) {
            return (Message.Builder)GeneratedMessageV3.invokeOrDie(this.getBuilderMethodBuilder, builder, index);
         }
      }

      private static final class SingularEnumFieldAccessor extends GeneratedMessageV3.FieldAccessorTable.SingularFieldAccessor {
         private final Descriptors.EnumDescriptor enumDescriptor;
         private final java.lang.reflect.Method valueOfMethod;
         private final java.lang.reflect.Method getValueDescriptorMethod;
         private final boolean supportUnknownEnumValue;
         private java.lang.reflect.Method getValueMethod;
         private java.lang.reflect.Method getValueMethodBuilder;
         private java.lang.reflect.Method setValueMethod;

         SingularEnumFieldAccessor(
            final Descriptors.FieldDescriptor descriptor,
            final String camelCaseName,
            final Class<? extends GeneratedMessageV3> messageClass,
            final Class<? extends GeneratedMessageV3.Builder<?>> builderClass,
            final String containingOneofCamelCaseName
         ) {
            super(descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName);
            this.enumDescriptor = descriptor.getEnumType();
            this.valueOfMethod = GeneratedMessageV3.getMethodOrDie(this.type, "valueOf", Descriptors.EnumValueDescriptor.class);
            this.getValueDescriptorMethod = GeneratedMessageV3.getMethodOrDie(this.type, "getValueDescriptor");
            this.supportUnknownEnumValue = !descriptor.legacyEnumFieldTreatedAsClosed();
            if (this.supportUnknownEnumValue) {
               this.getValueMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "Value");
               this.getValueMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Value");
               this.setValueMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "set" + camelCaseName + "Value", int.class);
            }
         }

         @Override
         public Object get(final GeneratedMessageV3 message) {
            if (this.supportUnknownEnumValue) {
               int value = (Integer)GeneratedMessageV3.invokeOrDie(this.getValueMethod, message);
               return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
            } else {
               return GeneratedMessageV3.invokeOrDie(this.getValueDescriptorMethod, super.get(message));
            }
         }

         @Override
         public Object get(final GeneratedMessageV3.Builder<?> builder) {
            if (this.supportUnknownEnumValue) {
               int value = (Integer)GeneratedMessageV3.invokeOrDie(this.getValueMethodBuilder, builder);
               return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
            } else {
               return GeneratedMessageV3.invokeOrDie(this.getValueDescriptorMethod, super.get(builder));
            }
         }

         @Override
         public void set(final GeneratedMessageV3.Builder<?> builder, final Object value) {
            if (this.supportUnknownEnumValue) {
               Object unused = GeneratedMessageV3.invokeOrDie(this.setValueMethod, builder, ((Descriptors.EnumValueDescriptor)value).getNumber());
            } else {
               super.set(builder, GeneratedMessageV3.invokeOrDie(this.valueOfMethod, null, value));
            }
         }
      }

      private static class SingularFieldAccessor implements GeneratedMessageV3.FieldAccessorTable.FieldAccessor {
         protected final Class<?> type;
         protected final Descriptors.FieldDescriptor field;
         protected final boolean isOneofField;
         protected final boolean hasHasMethod;
         protected final GeneratedMessageV3.FieldAccessorTable.SingularFieldAccessor.MethodInvoker invoker;

         SingularFieldAccessor(
            final Descriptors.FieldDescriptor descriptor,
            final String camelCaseName,
            final Class<? extends GeneratedMessageV3> messageClass,
            final Class<? extends GeneratedMessageV3.Builder<?>> builderClass,
            final String containingOneofCamelCaseName
         ) {
            this.isOneofField = descriptor.getRealContainingOneof() != null;
            this.hasHasMethod = descriptor.getFile().getSyntax() == Descriptors.FileDescriptor.Syntax.EDITIONS && descriptor.hasPresence()
               || descriptor.getFile().getSyntax() == Descriptors.FileDescriptor.Syntax.PROTO2
               || descriptor.hasOptionalKeyword()
               || !this.isOneofField && descriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE;
            GeneratedMessageV3.FieldAccessorTable.SingularFieldAccessor.ReflectionInvoker reflectionInvoker = new GeneratedMessageV3.FieldAccessorTable.SingularFieldAccessor.ReflectionInvoker(
               descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName, this.isOneofField, this.hasHasMethod
            );
            this.field = descriptor;
            this.type = reflectionInvoker.getMethod.getReturnType();
            this.invoker = getMethodInvoker(reflectionInvoker);
         }

         static GeneratedMessageV3.FieldAccessorTable.SingularFieldAccessor.MethodInvoker getMethodInvoker(
            GeneratedMessageV3.FieldAccessorTable.SingularFieldAccessor.ReflectionInvoker accessor
         ) {
            return accessor;
         }

         @Override
         public Object get(final GeneratedMessageV3 message) {
            return this.invoker.get(message);
         }

         @Override
         public Object get(GeneratedMessageV3.Builder<?> builder) {
            return this.invoker.get(builder);
         }

         @Override
         public Object getRaw(final GeneratedMessageV3 message) {
            return this.get(message);
         }

         @Override
         public void set(final GeneratedMessageV3.Builder<?> builder, final Object value) {
            this.invoker.set(builder, value);
         }

         @Override
         public Object getRepeated(final GeneratedMessageV3 message, final int index) {
            throw new UnsupportedOperationException("getRepeatedField() called on a singular field.");
         }

         @Override
         public Object getRepeated(GeneratedMessageV3.Builder<?> builder, int index) {
            throw new UnsupportedOperationException("getRepeatedField() called on a singular field.");
         }

         @Override
         public void setRepeated(final GeneratedMessageV3.Builder<?> builder, final int index, final Object value) {
            throw new UnsupportedOperationException("setRepeatedField() called on a singular field.");
         }

         @Override
         public void addRepeated(final GeneratedMessageV3.Builder<?> builder, final Object value) {
            throw new UnsupportedOperationException("addRepeatedField() called on a singular field.");
         }

         @Override
         public boolean has(final GeneratedMessageV3 message) {
            if (!this.hasHasMethod) {
               return this.isOneofField
                  ? this.invoker.getOneofFieldNumber(message) == this.field.getNumber()
                  : !this.get(message).equals(this.field.getDefaultValue());
            } else {
               return this.invoker.has(message);
            }
         }

         @Override
         public boolean has(GeneratedMessageV3.Builder<?> builder) {
            if (!this.hasHasMethod) {
               return this.isOneofField
                  ? this.invoker.getOneofFieldNumber(builder) == this.field.getNumber()
                  : !this.get(builder).equals(this.field.getDefaultValue());
            } else {
               return this.invoker.has(builder);
            }
         }

         @Override
         public int getRepeatedCount(final GeneratedMessageV3 message) {
            throw new UnsupportedOperationException("getRepeatedFieldSize() called on a singular field.");
         }

         @Override
         public int getRepeatedCount(GeneratedMessageV3.Builder<?> builder) {
            throw new UnsupportedOperationException("getRepeatedFieldSize() called on a singular field.");
         }

         @Override
         public void clear(final GeneratedMessageV3.Builder<?> builder) {
            this.invoker.clear(builder);
         }

         @Override
         public Message.Builder newBuilder() {
            throw new UnsupportedOperationException("newBuilderForField() called on a non-Message type.");
         }

         @Override
         public Message.Builder getBuilder(GeneratedMessageV3.Builder<?> builder) {
            throw new UnsupportedOperationException("getFieldBuilder() called on a non-Message type.");
         }

         @Override
         public Message.Builder getRepeatedBuilder(GeneratedMessageV3.Builder<?> builder, int index) {
            throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
         }

         private interface MethodInvoker {
            Object get(final GeneratedMessageV3 message);

            Object get(GeneratedMessageV3.Builder<?> builder);

            int getOneofFieldNumber(final GeneratedMessageV3 message);

            int getOneofFieldNumber(final GeneratedMessageV3.Builder<?> builder);

            void set(final GeneratedMessageV3.Builder<?> builder, final Object value);

            boolean has(final GeneratedMessageV3 message);

            boolean has(GeneratedMessageV3.Builder<?> builder);

            void clear(final GeneratedMessageV3.Builder<?> builder);
         }

         private static final class ReflectionInvoker implements GeneratedMessageV3.FieldAccessorTable.SingularFieldAccessor.MethodInvoker {
            private final java.lang.reflect.Method getMethod;
            private final java.lang.reflect.Method getMethodBuilder;
            private final java.lang.reflect.Method setMethod;
            private final java.lang.reflect.Method hasMethod;
            private final java.lang.reflect.Method hasMethodBuilder;
            private final java.lang.reflect.Method clearMethod;
            private final java.lang.reflect.Method caseMethod;
            private final java.lang.reflect.Method caseMethodBuilder;

            ReflectionInvoker(
               final Descriptors.FieldDescriptor descriptor,
               final String camelCaseName,
               final Class<? extends GeneratedMessageV3> messageClass,
               final Class<? extends GeneratedMessageV3.Builder<?>> builderClass,
               final String containingOneofCamelCaseName,
               boolean isOneofField,
               boolean hasHasMethod
            ) {
               this.getMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName);
               this.getMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName);
               Class<?> type = this.getMethod.getReturnType();
               this.setMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "set" + camelCaseName, type);
               this.hasMethod = hasHasMethod ? GeneratedMessageV3.getMethodOrDie(messageClass, "has" + camelCaseName) : null;
               this.hasMethodBuilder = hasHasMethod ? GeneratedMessageV3.getMethodOrDie(builderClass, "has" + camelCaseName) : null;
               this.clearMethod = GeneratedMessageV3.getMethodOrDie(builderClass, "clear" + camelCaseName);
               this.caseMethod = isOneofField ? GeneratedMessageV3.getMethodOrDie(messageClass, "get" + containingOneofCamelCaseName + "Case") : null;
               this.caseMethodBuilder = isOneofField ? GeneratedMessageV3.getMethodOrDie(builderClass, "get" + containingOneofCamelCaseName + "Case") : null;
            }

            @Override
            public Object get(final GeneratedMessageV3 message) {
               return GeneratedMessageV3.invokeOrDie(this.getMethod, message);
            }

            @Override
            public Object get(GeneratedMessageV3.Builder<?> builder) {
               return GeneratedMessageV3.invokeOrDie(this.getMethodBuilder, builder);
            }

            @Override
            public int getOneofFieldNumber(final GeneratedMessageV3 message) {
               return ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethod, message)).getNumber();
            }

            @Override
            public int getOneofFieldNumber(final GeneratedMessageV3.Builder<?> builder) {
               return ((Internal.EnumLite)GeneratedMessageV3.invokeOrDie(this.caseMethodBuilder, builder)).getNumber();
            }

            @Override
            public void set(final GeneratedMessageV3.Builder<?> builder, final Object value) {
               Object unused = GeneratedMessageV3.invokeOrDie(this.setMethod, builder, value);
            }

            @Override
            public boolean has(final GeneratedMessageV3 message) {
               return (Boolean)GeneratedMessageV3.invokeOrDie(this.hasMethod, message);
            }

            @Override
            public boolean has(GeneratedMessageV3.Builder<?> builder) {
               return (Boolean)GeneratedMessageV3.invokeOrDie(this.hasMethodBuilder, builder);
            }

            @Override
            public void clear(final GeneratedMessageV3.Builder<?> builder) {
               Object unused = GeneratedMessageV3.invokeOrDie(this.clearMethod, builder);
            }
         }
      }

      private static final class SingularMessageFieldAccessor extends GeneratedMessageV3.FieldAccessorTable.SingularFieldAccessor {
         private final java.lang.reflect.Method newBuilderMethod = GeneratedMessageV3.getMethodOrDie(this.type, "newBuilder");
         private final java.lang.reflect.Method getBuilderMethodBuilder;

         SingularMessageFieldAccessor(
            final Descriptors.FieldDescriptor descriptor,
            final String camelCaseName,
            final Class<? extends GeneratedMessageV3> messageClass,
            final Class<? extends GeneratedMessageV3.Builder<?>> builderClass,
            final String containingOneofCamelCaseName
         ) {
            super(descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName);
            this.getBuilderMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "get" + camelCaseName + "Builder");
         }

         private Object coerceType(final Object value) {
            return this.type.isInstance(value)
               ? value
               : ((Message.Builder)GeneratedMessageV3.invokeOrDie(this.newBuilderMethod, null)).mergeFrom((Message)value).buildPartial();
         }

         @Override
         public void set(final GeneratedMessageV3.Builder<?> builder, final Object value) {
            super.set(builder, this.coerceType(value));
         }

         @Override
         public Message.Builder newBuilder() {
            return (Message.Builder)GeneratedMessageV3.invokeOrDie(this.newBuilderMethod, null);
         }

         @Override
         public Message.Builder getBuilder(GeneratedMessageV3.Builder<?> builder) {
            return (Message.Builder)GeneratedMessageV3.invokeOrDie(this.getBuilderMethodBuilder, builder);
         }
      }

      private static final class SingularStringFieldAccessor extends GeneratedMessageV3.FieldAccessorTable.SingularFieldAccessor {
         private final java.lang.reflect.Method getBytesMethod;
         private final java.lang.reflect.Method setBytesMethodBuilder;

         SingularStringFieldAccessor(
            final Descriptors.FieldDescriptor descriptor,
            final String camelCaseName,
            final Class<? extends GeneratedMessageV3> messageClass,
            final Class<? extends GeneratedMessageV3.Builder<?>> builderClass,
            final String containingOneofCamelCaseName
         ) {
            super(descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName);
            this.getBytesMethod = GeneratedMessageV3.getMethodOrDie(messageClass, "get" + camelCaseName + "Bytes");
            this.setBytesMethodBuilder = GeneratedMessageV3.getMethodOrDie(builderClass, "set" + camelCaseName + "Bytes", ByteString.class);
         }

         @Override
         public Object getRaw(final GeneratedMessageV3 message) {
            return GeneratedMessageV3.invokeOrDie(this.getBytesMethod, message);
         }

         @Override
         public void set(GeneratedMessageV3.Builder<?> builder, Object value) {
            if (value instanceof ByteString) {
               Object var3 = GeneratedMessageV3.invokeOrDie(this.setBytesMethodBuilder, builder, value);
            } else {
               super.set(builder, value);
            }
         }
      }

      private static class SyntheticOneofAccessor implements GeneratedMessageV3.FieldAccessorTable.OneofAccessor {
         private final Descriptors.FieldDescriptor fieldDescriptor;

         SyntheticOneofAccessor(final Descriptors.Descriptor descriptor, final int oneofIndex) {
            Descriptors.OneofDescriptor oneofDescriptor = descriptor.getOneofs().get(oneofIndex);
            this.fieldDescriptor = oneofDescriptor.getFields().get(0);
         }

         @Override
         public boolean has(final GeneratedMessageV3 message) {
            return message.hasField(this.fieldDescriptor);
         }

         @Override
         public boolean has(GeneratedMessageV3.Builder<?> builder) {
            return builder.hasField(this.fieldDescriptor);
         }

         @Override
         public Descriptors.FieldDescriptor get(final GeneratedMessageV3 message) {
            return message.hasField(this.fieldDescriptor) ? this.fieldDescriptor : null;
         }

         @Override
         public Descriptors.FieldDescriptor get(GeneratedMessageV3.Builder<?> builder) {
            return builder.hasField(this.fieldDescriptor) ? this.fieldDescriptor : null;
         }

         @Override
         public void clear(final GeneratedMessageV3.Builder<?> builder) {
            builder.clearField(this.fieldDescriptor);
         }
      }
   }

   protected static final class UnusedPrivateParameter {
      static final GeneratedMessageV3.UnusedPrivateParameter INSTANCE = new GeneratedMessageV3.UnusedPrivateParameter();

      private UnusedPrivateParameter() {
      }
   }
}
