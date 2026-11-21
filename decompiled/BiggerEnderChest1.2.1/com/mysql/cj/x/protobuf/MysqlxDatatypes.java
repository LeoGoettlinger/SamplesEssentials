package com.mysql.cj.x.protobuf;

import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.RepeatedFieldBuilder;
import com.google.protobuf.RuntimeVersion;
import com.google.protobuf.SingleFieldBuilder;
import com.google.protobuf.UninitializedMessageException;
import com.google.protobuf.AbstractMessage.BuilderParent;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage.FieldAccessorTable;
import com.google.protobuf.Internal.EnumLiteMap;
import com.google.protobuf.RuntimeVersion.RuntimeDomain;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MysqlxDatatypes {
   private static final Descriptor internal_static_Mysqlx_Datatypes_Scalar_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static final FieldAccessorTable internal_static_Mysqlx_Datatypes_Scalar_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Scalar_descriptor,
      new java.lang.String[]{"Type", "VSignedInt", "VUnsignedInt", "VOctets", "VDouble", "VFloat", "VBool", "VString"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Scalar_String_descriptor = (Descriptor)internal_static_Mysqlx_Datatypes_Scalar_descriptor.getNestedTypes()
      .get(0);
   private static final FieldAccessorTable internal_static_Mysqlx_Datatypes_Scalar_String_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Scalar_String_descriptor, new java.lang.String[]{"Value", "Collation"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Scalar_Octets_descriptor = (Descriptor)internal_static_Mysqlx_Datatypes_Scalar_descriptor.getNestedTypes()
      .get(1);
   private static final FieldAccessorTable internal_static_Mysqlx_Datatypes_Scalar_Octets_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Scalar_Octets_descriptor, new java.lang.String[]{"Value", "ContentType"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Object_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static final FieldAccessorTable internal_static_Mysqlx_Datatypes_Object_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Object_descriptor, new java.lang.String[]{"Fld"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Object_ObjectField_descriptor = (Descriptor)internal_static_Mysqlx_Datatypes_Object_descriptor.getNestedTypes()
      .get(0);
   private static final FieldAccessorTable internal_static_Mysqlx_Datatypes_Object_ObjectField_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Object_ObjectField_descriptor, new java.lang.String[]{"Key", "Value"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Array_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(2);
   private static final FieldAccessorTable internal_static_Mysqlx_Datatypes_Array_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Array_descriptor, new java.lang.String[]{"Value"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Any_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(3);
   private static final FieldAccessorTable internal_static_Mysqlx_Datatypes_Any_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Any_descriptor, new java.lang.String[]{"Type", "Scalar", "Obj", "Array"}
   );
   private static FileDescriptor descriptor;

   private MysqlxDatatypes() {
   }

   public static void registerAllExtensions(ExtensionRegistryLite registry) {
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
      registerAllExtensions((ExtensionRegistryLite)registry);
   }

   public static FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxDatatypes.class.getName());
      java.lang.String[] descriptorData = new java.lang.String[]{
         "\n\u0016mysqlx_datatypes.proto\u0012\u0010Mysqlx.Datatypes\"Æ\u0003\n\u0006Scalar\u0012+\n\u0004type\u0018\u0001 \u0002(\u000e2\u001d.Mysqlx.Datatypes.Scalar.Type\u0012\u0014\n\fv_signed_int\u0018\u0002 \u0001(\u0012\u0012\u0016\n\u000ev_unsigned_int\u0018\u0003 \u0001(\u0004\u00121\n\bv_octets\u0018\u0005 \u0001(\u000b2\u001f.Mysqlx.Datatypes.Scalar.Octets\u0012\u0010\n\bv_double\u0018\u0006 \u0001(\u0001\u0012\u000f\n\u0007v_float\u0018\u0007 \u0001(\u0002\u0012\u000e\n\u0006v_bool\u0018\b \u0001(\b\u00121\n\bv_string\u0018\t \u0001(\u000b2\u001f.Mysqlx.Datatypes.Scalar.String\u001a*\n\u0006String\u0012\r\n\u0005value\u0018\u0001 \u0002(\f\u0012\u0011\n\tcollation\u0018\u0002 \u0001(\u0004\u001a-\n\u0006Octets\u0012\r\n\u0005value\u0018\u0001 \u0002(\f\u0012\u0014\n\fcontent_type\u0018\u0002 \u0001(\r\"m\n\u0004Type\u0012\n\n\u0006V_SINT\u0010\u0001\u0012\n\n\u0006V_UINT\u0010\u0002\u0012\n\n\u0006V_NULL\u0010\u0003\u0012\f\n\bV_OCTETS\u0010\u0004\u0012\f\n\bV_DOUBLE\u0010\u0005\u0012\u000b\n\u0007V_FLOAT\u0010\u0006\u0012\n\n\u0006V_BOOL\u0010\u0007\u0012\f\n\bV_STRING\u0010\b\"}\n\u0006Object\u00121\n\u0003fld\u0018\u0001 \u0003(\u000b2$.Mysqlx.Datatypes.Object.ObjectField\u001a@\n\u000bObjectField\u0012\u000b\n\u0003key\u0018\u0001 \u0002(\t\u0012$\n\u0005value\u0018\u0002 \u0002(\u000b2\u0015.Mysqlx.Datatypes.Any\"-\n\u0005Array\u0012$\n\u0005value\u0018\u0001 \u0003(\u000b2\u0015.Mysqlx.Datatypes.Any\"Ó\u0001\n\u0003Any\u0012(\n\u0004type\u0018\u0001 \u0002(\u000e2\u001a.Mysqlx.Datatypes.Any.Type\u0012(\n\u0006scalar\u0018\u0002 \u0001(\u000b2\u0018.Mysqlx.Datatypes.Scalar\u0012%\n\u0003obj\u0018\u0003 \u0001(\u000b2\u0018.Mysqlx.Datatypes.Object\u0012&\n\u0005array\u0018\u0004 \u0001(\u000b2\u0017.Mysqlx.Datatypes.Array\")\n\u0004Type\u0012\n\n\u0006SCALAR\u0010\u0001\u0012\n\n\u0006OBJECT\u0010\u0002\u0012\t\n\u0005ARRAY\u0010\u0003B\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      descriptor = FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new FileDescriptor[0]);
      descriptor.resolveAllFeaturesImmutable();
   }

   public static final class Any extends GeneratedMessage implements MysqlxDatatypes.AnyOrBuilder {
      private static final long serialVersionUID = 0L;
      private int bitField0_;
      public static final int TYPE_FIELD_NUMBER = 1;
      private int type_ = 1;
      public static final int SCALAR_FIELD_NUMBER = 2;
      private MysqlxDatatypes.Scalar scalar_;
      public static final int OBJ_FIELD_NUMBER = 3;
      private MysqlxDatatypes.Object obj_;
      public static final int ARRAY_FIELD_NUMBER = 4;
      private MysqlxDatatypes.Array array_;
      private byte memoizedIsInitialized = -1;
      private static final MysqlxDatatypes.Any DEFAULT_INSTANCE = new MysqlxDatatypes.Any();
      private static final Parser<MysqlxDatatypes.Any> PARSER = new AbstractParser<MysqlxDatatypes.Any>() {
         public MysqlxDatatypes.Any parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            MysqlxDatatypes.Any.Builder builder = MysqlxDatatypes.Any.newBuilder();

            try {
               builder.mergeFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var5) {
               throw var5.setUnfinishedMessage(builder.buildPartial());
            } catch (UninitializedMessageException var6) {
               throw var6.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
            } catch (IOException var7) {
               throw new InvalidProtocolBufferException(var7).setUnfinishedMessage(builder.buildPartial());
            }

            return builder.buildPartial();
         }
      };

      private Any(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private Any() {
         this.type_ = 1;
      }

      public static final Descriptor getDescriptor() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Any_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Any_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxDatatypes.Any.class, MysqlxDatatypes.Any.Builder.class);
      }

      @Override
      public boolean hasType() {
         return (this.bitField0_ & 1) != 0;
      }

      @Override
      public MysqlxDatatypes.Any.Type getType() {
         MysqlxDatatypes.Any.Type result = MysqlxDatatypes.Any.Type.forNumber(this.type_);
         return result == null ? MysqlxDatatypes.Any.Type.SCALAR : result;
      }

      @Override
      public boolean hasScalar() {
         return (this.bitField0_ & 2) != 0;
      }

      @Override
      public MysqlxDatatypes.Scalar getScalar() {
         return this.scalar_ == null ? MysqlxDatatypes.Scalar.getDefaultInstance() : this.scalar_;
      }

      @Override
      public MysqlxDatatypes.ScalarOrBuilder getScalarOrBuilder() {
         return this.scalar_ == null ? MysqlxDatatypes.Scalar.getDefaultInstance() : this.scalar_;
      }

      @Override
      public boolean hasObj() {
         return (this.bitField0_ & 4) != 0;
      }

      @Override
      public MysqlxDatatypes.Object getObj() {
         return this.obj_ == null ? MysqlxDatatypes.Object.getDefaultInstance() : this.obj_;
      }

      @Override
      public MysqlxDatatypes.ObjectOrBuilder getObjOrBuilder() {
         return this.obj_ == null ? MysqlxDatatypes.Object.getDefaultInstance() : this.obj_;
      }

      @Override
      public boolean hasArray() {
         return (this.bitField0_ & 8) != 0;
      }

      @Override
      public MysqlxDatatypes.Array getArray() {
         return this.array_ == null ? MysqlxDatatypes.Array.getDefaultInstance() : this.array_;
      }

      @Override
      public MysqlxDatatypes.ArrayOrBuilder getArrayOrBuilder() {
         return this.array_ == null ? MysqlxDatatypes.Array.getDefaultInstance() : this.array_;
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasType()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasScalar() && !this.getScalar().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasObj() && !this.getObj().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasArray() && !this.getArray().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         if ((this.bitField0_ & 1) != 0) {
            output.writeEnum(1, this.type_);
         }

         if ((this.bitField0_ & 2) != 0) {
            output.writeMessage(2, this.getScalar());
         }

         if ((this.bitField0_ & 4) != 0) {
            output.writeMessage(3, this.getObj());
         }

         if ((this.bitField0_ & 8) != 0) {
            output.writeMessage(4, this.getArray());
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
               size += CodedOutputStream.computeEnumSize(1, this.type_);
            }

            if ((this.bitField0_ & 2) != 0) {
               size += CodedOutputStream.computeMessageSize(2, this.getScalar());
            }

            if ((this.bitField0_ & 4) != 0) {
               size += CodedOutputStream.computeMessageSize(3, this.getObj());
            }

            if ((this.bitField0_ & 8) != 0) {
               size += CodedOutputStream.computeMessageSize(4, this.getArray());
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSize = size;
            return size;
         }
      }

      @Override
      public boolean equals(java.lang.Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof MysqlxDatatypes.Any)) {
            return super.equals(obj);
         } else {
            MysqlxDatatypes.Any other = (MysqlxDatatypes.Any)obj;
            if (this.hasType() != other.hasType()) {
               return false;
            } else if (this.hasType() && this.type_ != other.type_) {
               return false;
            } else if (this.hasScalar() != other.hasScalar()) {
               return false;
            } else if (this.hasScalar() && !this.getScalar().equals(other.getScalar())) {
               return false;
            } else if (this.hasObj() != other.hasObj()) {
               return false;
            } else if (this.hasObj() && !this.getObj().equals(other.getObj())) {
               return false;
            } else if (this.hasArray() != other.hasArray()) {
               return false;
            } else {
               return this.hasArray() && !this.getArray().equals(other.getArray()) ? false : this.getUnknownFields().equals(other.getUnknownFields());
            }
         }
      }

      @Override
      public int hashCode() {
         if (this.memoizedHashCode != 0) {
            return this.memoizedHashCode;
         } else {
            int hash = 41;
            hash = 19 * hash + getDescriptor().hashCode();
            if (this.hasType()) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.type_;
            }

            if (this.hasScalar()) {
               hash = 37 * hash + 2;
               hash = 53 * hash + this.getScalar().hashCode();
            }

            if (this.hasObj()) {
               hash = 37 * hash + 3;
               hash = 53 * hash + this.getObj().hashCode();
            }

            if (this.hasArray()) {
               hash = 37 * hash + 4;
               hash = 53 * hash + this.getArray().hashCode();
            }

            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static MysqlxDatatypes.Any parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Any parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Any parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Any parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Any parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Any parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Any parseFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Any)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Any parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Any)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxDatatypes.Any parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Any)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Any parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Any)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxDatatypes.Any parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxDatatypes.Any)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Any parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Any)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public MysqlxDatatypes.Any.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxDatatypes.Any.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static MysqlxDatatypes.Any.Builder newBuilder(MysqlxDatatypes.Any prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public MysqlxDatatypes.Any.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new MysqlxDatatypes.Any.Builder() : new MysqlxDatatypes.Any.Builder().mergeFrom(this);
      }

      protected MysqlxDatatypes.Any.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxDatatypes.Any.Builder(parent);
      }

      public static MysqlxDatatypes.Any getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<MysqlxDatatypes.Any> parser() {
         return PARSER;
      }

      public Parser<MysqlxDatatypes.Any> getParserForType() {
         return PARSER;
      }

      public MysqlxDatatypes.Any getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxDatatypes.Any.class.getName());
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Any.Builder>
         implements MysqlxDatatypes.AnyOrBuilder {
         private int bitField0_;
         private int type_ = 1;
         private MysqlxDatatypes.Scalar scalar_;
         private SingleFieldBuilder<MysqlxDatatypes.Scalar, MysqlxDatatypes.Scalar.Builder, MysqlxDatatypes.ScalarOrBuilder> scalarBuilder_;
         private MysqlxDatatypes.Object obj_;
         private SingleFieldBuilder<MysqlxDatatypes.Object, MysqlxDatatypes.Object.Builder, MysqlxDatatypes.ObjectOrBuilder> objBuilder_;
         private MysqlxDatatypes.Array array_;
         private SingleFieldBuilder<MysqlxDatatypes.Array, MysqlxDatatypes.Array.Builder, MysqlxDatatypes.ArrayOrBuilder> arrayBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Any_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Any_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Any.class, MysqlxDatatypes.Any.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxDatatypes.Any.alwaysUseFieldBuilders) {
               this.getScalarFieldBuilder();
               this.getObjFieldBuilder();
               this.getArrayFieldBuilder();
            }
         }

         public MysqlxDatatypes.Any.Builder clear() {
            super.clear();
            this.bitField0_ = 0;
            this.type_ = 1;
            this.scalar_ = null;
            if (this.scalarBuilder_ != null) {
               this.scalarBuilder_.dispose();
               this.scalarBuilder_ = null;
            }

            this.obj_ = null;
            if (this.objBuilder_ != null) {
               this.objBuilder_.dispose();
               this.objBuilder_ = null;
            }

            this.array_ = null;
            if (this.arrayBuilder_ != null) {
               this.arrayBuilder_.dispose();
               this.arrayBuilder_ = null;
            }

            return this;
         }

         public Descriptor getDescriptorForType() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Any_descriptor;
         }

         public MysqlxDatatypes.Any getDefaultInstanceForType() {
            return MysqlxDatatypes.Any.getDefaultInstance();
         }

         public MysqlxDatatypes.Any build() {
            MysqlxDatatypes.Any result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxDatatypes.Any buildPartial() {
            MysqlxDatatypes.Any result = new MysqlxDatatypes.Any(this);
            if (this.bitField0_ != 0) {
               this.buildPartial0(result);
            }

            this.onBuilt();
            return result;
         }

         private void buildPartial0(MysqlxDatatypes.Any result) {
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) != 0) {
               result.type_ = this.type_;
               to_bitField0_ |= 1;
            }

            if ((from_bitField0_ & 2) != 0) {
               result.scalar_ = this.scalarBuilder_ == null ? this.scalar_ : (MysqlxDatatypes.Scalar)this.scalarBuilder_.build();
               to_bitField0_ |= 2;
            }

            if ((from_bitField0_ & 4) != 0) {
               result.obj_ = this.objBuilder_ == null ? this.obj_ : (MysqlxDatatypes.Object)this.objBuilder_.build();
               to_bitField0_ |= 4;
            }

            if ((from_bitField0_ & 8) != 0) {
               result.array_ = this.arrayBuilder_ == null ? this.array_ : (MysqlxDatatypes.Array)this.arrayBuilder_.build();
               to_bitField0_ |= 8;
            }

            result.bitField0_ = result.bitField0_ | to_bitField0_;
         }

         public MysqlxDatatypes.Any.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxDatatypes.Any) {
               return this.mergeFrom((MysqlxDatatypes.Any)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxDatatypes.Any.Builder mergeFrom(MysqlxDatatypes.Any other) {
            if (other == MysqlxDatatypes.Any.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasType()) {
                  this.setType(other.getType());
               }

               if (other.hasScalar()) {
                  this.mergeScalar(other.getScalar());
               }

               if (other.hasObj()) {
                  this.mergeObj(other.getObj());
               }

               if (other.hasArray()) {
                  this.mergeArray(other.getArray());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasType()) {
               return false;
            } else if (this.hasScalar() && !this.getScalar().isInitialized()) {
               return false;
            } else {
               return this.hasObj() && !this.getObj().isInitialized() ? false : !this.hasArray() || this.getArray().isInitialized();
            }
         }

         public MysqlxDatatypes.Any.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            if (extensionRegistry == null) {
               throw new NullPointerException();
            } else {
               try {
                  boolean done = false;

                  while (!done) {
                     int tag = input.readTag();
                     switch (tag) {
                        case 0:
                           done = true;
                           break;
                        case 8:
                           int tmpRaw = input.readEnum();
                           MysqlxDatatypes.Any.Type tmpValue = MysqlxDatatypes.Any.Type.forNumber(tmpRaw);
                           if (tmpValue == null) {
                              this.mergeUnknownVarintField(1, tmpRaw);
                           } else {
                              this.type_ = tmpRaw;
                              this.bitField0_ |= 1;
                           }
                           break;
                        case 18:
                           input.readMessage(this.getScalarFieldBuilder().getBuilder(), extensionRegistry);
                           this.bitField0_ |= 2;
                           break;
                        case 26:
                           input.readMessage(this.getObjFieldBuilder().getBuilder(), extensionRegistry);
                           this.bitField0_ |= 4;
                           break;
                        case 34:
                           input.readMessage(this.getArrayFieldBuilder().getBuilder(), extensionRegistry);
                           this.bitField0_ |= 8;
                           break;
                        default:
                           if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                              done = true;
                           }
                     }
                  }
               } catch (InvalidProtocolBufferException var10) {
                  throw var10.unwrapIOException();
               } finally {
                  this.onChanged();
               }

               return this;
            }
         }

         @Override
         public boolean hasType() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public MysqlxDatatypes.Any.Type getType() {
            MysqlxDatatypes.Any.Type result = MysqlxDatatypes.Any.Type.forNumber(this.type_);
            return result == null ? MysqlxDatatypes.Any.Type.SCALAR : result;
         }

         public MysqlxDatatypes.Any.Builder setType(MysqlxDatatypes.Any.Type value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.type_ = value.getNumber();
               this.onChanged();
               return this;
            }
         }

         public MysqlxDatatypes.Any.Builder clearType() {
            this.bitField0_ &= -2;
            this.type_ = 1;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasScalar() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public MysqlxDatatypes.Scalar getScalar() {
            if (this.scalarBuilder_ == null) {
               return this.scalar_ == null ? MysqlxDatatypes.Scalar.getDefaultInstance() : this.scalar_;
            } else {
               return (MysqlxDatatypes.Scalar)this.scalarBuilder_.getMessage();
            }
         }

         public MysqlxDatatypes.Any.Builder setScalar(MysqlxDatatypes.Scalar value) {
            if (this.scalarBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.scalar_ = value;
            } else {
               this.scalarBuilder_.setMessage(value);
            }

            this.bitField0_ |= 2;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Any.Builder setScalar(MysqlxDatatypes.Scalar.Builder builderForValue) {
            if (this.scalarBuilder_ == null) {
               this.scalar_ = builderForValue.build();
            } else {
               this.scalarBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 2;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Any.Builder mergeScalar(MysqlxDatatypes.Scalar value) {
            if (this.scalarBuilder_ == null) {
               if ((this.bitField0_ & 2) != 0 && this.scalar_ != null && this.scalar_ != MysqlxDatatypes.Scalar.getDefaultInstance()) {
                  this.getScalarBuilder().mergeFrom(value);
               } else {
                  this.scalar_ = value;
               }
            } else {
               this.scalarBuilder_.mergeFrom(value);
            }

            if (this.scalar_ != null) {
               this.bitField0_ |= 2;
               this.onChanged();
            }

            return this;
         }

         public MysqlxDatatypes.Any.Builder clearScalar() {
            this.bitField0_ &= -3;
            this.scalar_ = null;
            if (this.scalarBuilder_ != null) {
               this.scalarBuilder_.dispose();
               this.scalarBuilder_ = null;
            }

            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder getScalarBuilder() {
            this.bitField0_ |= 2;
            this.onChanged();
            return (MysqlxDatatypes.Scalar.Builder)this.getScalarFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.ScalarOrBuilder getScalarOrBuilder() {
            if (this.scalarBuilder_ != null) {
               return (MysqlxDatatypes.ScalarOrBuilder)this.scalarBuilder_.getMessageOrBuilder();
            } else {
               return this.scalar_ == null ? MysqlxDatatypes.Scalar.getDefaultInstance() : this.scalar_;
            }
         }

         private SingleFieldBuilder<MysqlxDatatypes.Scalar, MysqlxDatatypes.Scalar.Builder, MysqlxDatatypes.ScalarOrBuilder> getScalarFieldBuilder() {
            if (this.scalarBuilder_ == null) {
               this.scalarBuilder_ = new SingleFieldBuilder(this.getScalar(), this.getParentForChildren(), this.isClean());
               this.scalar_ = null;
            }

            return this.scalarBuilder_;
         }

         @Override
         public boolean hasObj() {
            return (this.bitField0_ & 4) != 0;
         }

         @Override
         public MysqlxDatatypes.Object getObj() {
            if (this.objBuilder_ == null) {
               return this.obj_ == null ? MysqlxDatatypes.Object.getDefaultInstance() : this.obj_;
            } else {
               return (MysqlxDatatypes.Object)this.objBuilder_.getMessage();
            }
         }

         public MysqlxDatatypes.Any.Builder setObj(MysqlxDatatypes.Object value) {
            if (this.objBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.obj_ = value;
            } else {
               this.objBuilder_.setMessage(value);
            }

            this.bitField0_ |= 4;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Any.Builder setObj(MysqlxDatatypes.Object.Builder builderForValue) {
            if (this.objBuilder_ == null) {
               this.obj_ = builderForValue.build();
            } else {
               this.objBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 4;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Any.Builder mergeObj(MysqlxDatatypes.Object value) {
            if (this.objBuilder_ == null) {
               if ((this.bitField0_ & 4) != 0 && this.obj_ != null && this.obj_ != MysqlxDatatypes.Object.getDefaultInstance()) {
                  this.getObjBuilder().mergeFrom(value);
               } else {
                  this.obj_ = value;
               }
            } else {
               this.objBuilder_.mergeFrom(value);
            }

            if (this.obj_ != null) {
               this.bitField0_ |= 4;
               this.onChanged();
            }

            return this;
         }

         public MysqlxDatatypes.Any.Builder clearObj() {
            this.bitField0_ &= -5;
            this.obj_ = null;
            if (this.objBuilder_ != null) {
               this.objBuilder_.dispose();
               this.objBuilder_ = null;
            }

            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Object.Builder getObjBuilder() {
            this.bitField0_ |= 4;
            this.onChanged();
            return (MysqlxDatatypes.Object.Builder)this.getObjFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.ObjectOrBuilder getObjOrBuilder() {
            if (this.objBuilder_ != null) {
               return (MysqlxDatatypes.ObjectOrBuilder)this.objBuilder_.getMessageOrBuilder();
            } else {
               return this.obj_ == null ? MysqlxDatatypes.Object.getDefaultInstance() : this.obj_;
            }
         }

         private SingleFieldBuilder<MysqlxDatatypes.Object, MysqlxDatatypes.Object.Builder, MysqlxDatatypes.ObjectOrBuilder> getObjFieldBuilder() {
            if (this.objBuilder_ == null) {
               this.objBuilder_ = new SingleFieldBuilder(this.getObj(), this.getParentForChildren(), this.isClean());
               this.obj_ = null;
            }

            return this.objBuilder_;
         }

         @Override
         public boolean hasArray() {
            return (this.bitField0_ & 8) != 0;
         }

         @Override
         public MysqlxDatatypes.Array getArray() {
            if (this.arrayBuilder_ == null) {
               return this.array_ == null ? MysqlxDatatypes.Array.getDefaultInstance() : this.array_;
            } else {
               return (MysqlxDatatypes.Array)this.arrayBuilder_.getMessage();
            }
         }

         public MysqlxDatatypes.Any.Builder setArray(MysqlxDatatypes.Array value) {
            if (this.arrayBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.array_ = value;
            } else {
               this.arrayBuilder_.setMessage(value);
            }

            this.bitField0_ |= 8;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Any.Builder setArray(MysqlxDatatypes.Array.Builder builderForValue) {
            if (this.arrayBuilder_ == null) {
               this.array_ = builderForValue.build();
            } else {
               this.arrayBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 8;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Any.Builder mergeArray(MysqlxDatatypes.Array value) {
            if (this.arrayBuilder_ == null) {
               if ((this.bitField0_ & 8) != 0 && this.array_ != null && this.array_ != MysqlxDatatypes.Array.getDefaultInstance()) {
                  this.getArrayBuilder().mergeFrom(value);
               } else {
                  this.array_ = value;
               }
            } else {
               this.arrayBuilder_.mergeFrom(value);
            }

            if (this.array_ != null) {
               this.bitField0_ |= 8;
               this.onChanged();
            }

            return this;
         }

         public MysqlxDatatypes.Any.Builder clearArray() {
            this.bitField0_ &= -9;
            this.array_ = null;
            if (this.arrayBuilder_ != null) {
               this.arrayBuilder_.dispose();
               this.arrayBuilder_ = null;
            }

            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Array.Builder getArrayBuilder() {
            this.bitField0_ |= 8;
            this.onChanged();
            return (MysqlxDatatypes.Array.Builder)this.getArrayFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.ArrayOrBuilder getArrayOrBuilder() {
            if (this.arrayBuilder_ != null) {
               return (MysqlxDatatypes.ArrayOrBuilder)this.arrayBuilder_.getMessageOrBuilder();
            } else {
               return this.array_ == null ? MysqlxDatatypes.Array.getDefaultInstance() : this.array_;
            }
         }

         private SingleFieldBuilder<MysqlxDatatypes.Array, MysqlxDatatypes.Array.Builder, MysqlxDatatypes.ArrayOrBuilder> getArrayFieldBuilder() {
            if (this.arrayBuilder_ == null) {
               this.arrayBuilder_ = new SingleFieldBuilder(this.getArray(), this.getParentForChildren(), this.isClean());
               this.array_ = null;
            }

            return this.arrayBuilder_;
         }
      }

      public static enum Type implements ProtocolMessageEnum {
         SCALAR(1),
         OBJECT(2),
         ARRAY(3);

         public static final int SCALAR_VALUE = 1;
         public static final int OBJECT_VALUE = 2;
         public static final int ARRAY_VALUE = 3;
         private static final EnumLiteMap<MysqlxDatatypes.Any.Type> internalValueMap = new EnumLiteMap<MysqlxDatatypes.Any.Type>() {
            public MysqlxDatatypes.Any.Type findValueByNumber(int number) {
               return MysqlxDatatypes.Any.Type.forNumber(number);
            }
         };
         private static final MysqlxDatatypes.Any.Type[] VALUES = values();
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         @Deprecated
         public static MysqlxDatatypes.Any.Type valueOf(int value) {
            return forNumber(value);
         }

         public static MysqlxDatatypes.Any.Type forNumber(int value) {
            switch (value) {
               case 1:
                  return SCALAR;
               case 2:
                  return OBJECT;
               case 3:
                  return ARRAY;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<MysqlxDatatypes.Any.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.ordinal());
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxDatatypes.Any.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxDatatypes.Any.Type valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private Type(int value) {
            this.value = value;
         }

         static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxDatatypes.Any.Type.class.getName());
         }
      }
   }

   public interface AnyOrBuilder extends MessageOrBuilder {
      boolean hasType();

      MysqlxDatatypes.Any.Type getType();

      boolean hasScalar();

      MysqlxDatatypes.Scalar getScalar();

      MysqlxDatatypes.ScalarOrBuilder getScalarOrBuilder();

      boolean hasObj();

      MysqlxDatatypes.Object getObj();

      MysqlxDatatypes.ObjectOrBuilder getObjOrBuilder();

      boolean hasArray();

      MysqlxDatatypes.Array getArray();

      MysqlxDatatypes.ArrayOrBuilder getArrayOrBuilder();
   }

   public static final class Array extends GeneratedMessage implements MysqlxDatatypes.ArrayOrBuilder {
      private static final long serialVersionUID = 0L;
      public static final int VALUE_FIELD_NUMBER = 1;
      private List<MysqlxDatatypes.Any> value_;
      private byte memoizedIsInitialized = -1;
      private static final MysqlxDatatypes.Array DEFAULT_INSTANCE = new MysqlxDatatypes.Array();
      private static final Parser<MysqlxDatatypes.Array> PARSER = new AbstractParser<MysqlxDatatypes.Array>() {
         public MysqlxDatatypes.Array parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            MysqlxDatatypes.Array.Builder builder = MysqlxDatatypes.Array.newBuilder();

            try {
               builder.mergeFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var5) {
               throw var5.setUnfinishedMessage(builder.buildPartial());
            } catch (UninitializedMessageException var6) {
               throw var6.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
            } catch (IOException var7) {
               throw new InvalidProtocolBufferException(var7).setUnfinishedMessage(builder.buildPartial());
            }

            return builder.buildPartial();
         }
      };

      private Array(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private Array() {
         this.value_ = Collections.emptyList();
      }

      public static final Descriptor getDescriptor() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Array_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Array_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxDatatypes.Array.class, MysqlxDatatypes.Array.Builder.class);
      }

      @Override
      public List<MysqlxDatatypes.Any> getValueList() {
         return this.value_;
      }

      @Override
      public List<? extends MysqlxDatatypes.AnyOrBuilder> getValueOrBuilderList() {
         return this.value_;
      }

      @Override
      public int getValueCount() {
         return this.value_.size();
      }

      @Override
      public MysqlxDatatypes.Any getValue(int index) {
         return this.value_.get(index);
      }

      @Override
      public MysqlxDatatypes.AnyOrBuilder getValueOrBuilder(int index) {
         return this.value_.get(index);
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            for (int i = 0; i < this.getValueCount(); i++) {
               if (!this.getValue(i).isInitialized()) {
                  this.memoizedIsInitialized = 0;
                  return false;
               }
            }

            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         for (int i = 0; i < this.value_.size(); i++) {
            output.writeMessage(1, (MessageLite)this.value_.get(i));
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;

            for (int i = 0; i < this.value_.size(); i++) {
               size += CodedOutputStream.computeMessageSize(1, (MessageLite)this.value_.get(i));
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSize = size;
            return size;
         }
      }

      @Override
      public boolean equals(java.lang.Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof MysqlxDatatypes.Array)) {
            return super.equals(obj);
         } else {
            MysqlxDatatypes.Array other = (MysqlxDatatypes.Array)obj;
            return !this.getValueList().equals(other.getValueList()) ? false : this.getUnknownFields().equals(other.getUnknownFields());
         }
      }

      @Override
      public int hashCode() {
         if (this.memoizedHashCode != 0) {
            return this.memoizedHashCode;
         } else {
            int hash = 41;
            hash = 19 * hash + getDescriptor().hashCode();
            if (this.getValueCount() > 0) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.getValueList().hashCode();
            }

            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static MysqlxDatatypes.Array parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Array parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Array parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Array parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Array parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Array parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Array parseFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Array)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Array parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Array)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxDatatypes.Array parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Array)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Array parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Array)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxDatatypes.Array parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxDatatypes.Array)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Array parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Array)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public MysqlxDatatypes.Array.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxDatatypes.Array.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static MysqlxDatatypes.Array.Builder newBuilder(MysqlxDatatypes.Array prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public MysqlxDatatypes.Array.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new MysqlxDatatypes.Array.Builder() : new MysqlxDatatypes.Array.Builder().mergeFrom(this);
      }

      protected MysqlxDatatypes.Array.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxDatatypes.Array.Builder(parent);
      }

      public static MysqlxDatatypes.Array getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<MysqlxDatatypes.Array> parser() {
         return PARSER;
      }

      public Parser<MysqlxDatatypes.Array> getParserForType() {
         return PARSER;
      }

      public MysqlxDatatypes.Array getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxDatatypes.Array.class.getName());
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Array.Builder>
         implements MysqlxDatatypes.ArrayOrBuilder {
         private int bitField0_;
         private List<MysqlxDatatypes.Any> value_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> valueBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Array_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Array_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Array.class, MysqlxDatatypes.Array.Builder.class);
         }

         private Builder() {
         }

         private Builder(BuilderParent parent) {
            super(parent);
         }

         public MysqlxDatatypes.Array.Builder clear() {
            super.clear();
            this.bitField0_ = 0;
            if (this.valueBuilder_ == null) {
               this.value_ = Collections.emptyList();
            } else {
               this.value_ = null;
               this.valueBuilder_.clear();
            }

            this.bitField0_ &= -2;
            return this;
         }

         public Descriptor getDescriptorForType() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Array_descriptor;
         }

         public MysqlxDatatypes.Array getDefaultInstanceForType() {
            return MysqlxDatatypes.Array.getDefaultInstance();
         }

         public MysqlxDatatypes.Array build() {
            MysqlxDatatypes.Array result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxDatatypes.Array buildPartial() {
            MysqlxDatatypes.Array result = new MysqlxDatatypes.Array(this);
            this.buildPartialRepeatedFields(result);
            if (this.bitField0_ != 0) {
               this.buildPartial0(result);
            }

            this.onBuilt();
            return result;
         }

         private void buildPartialRepeatedFields(MysqlxDatatypes.Array result) {
            if (this.valueBuilder_ == null) {
               if ((this.bitField0_ & 1) != 0) {
                  this.value_ = Collections.unmodifiableList(this.value_);
                  this.bitField0_ &= -2;
               }

               result.value_ = this.value_;
            } else {
               result.value_ = this.valueBuilder_.build();
            }
         }

         private void buildPartial0(MysqlxDatatypes.Array result) {
            int from_bitField0_ = this.bitField0_;
         }

         public MysqlxDatatypes.Array.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxDatatypes.Array) {
               return this.mergeFrom((MysqlxDatatypes.Array)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxDatatypes.Array.Builder mergeFrom(MysqlxDatatypes.Array other) {
            if (other == MysqlxDatatypes.Array.getDefaultInstance()) {
               return this;
            } else {
               if (this.valueBuilder_ == null) {
                  if (!other.value_.isEmpty()) {
                     if (this.value_.isEmpty()) {
                        this.value_ = other.value_;
                        this.bitField0_ &= -2;
                     } else {
                        this.ensureValueIsMutable();
                        this.value_.addAll(other.value_);
                     }

                     this.onChanged();
                  }
               } else if (!other.value_.isEmpty()) {
                  if (this.valueBuilder_.isEmpty()) {
                     this.valueBuilder_.dispose();
                     this.valueBuilder_ = null;
                     this.value_ = other.value_;
                     this.bitField0_ &= -2;
                     this.valueBuilder_ = MysqlxDatatypes.Array.alwaysUseFieldBuilders ? this.getValueFieldBuilder() : null;
                  } else {
                     this.valueBuilder_.addAllMessages(other.value_);
                  }
               }

               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            for (int i = 0; i < this.getValueCount(); i++) {
               if (!this.getValue(i).isInitialized()) {
                  return false;
               }
            }

            return true;
         }

         public MysqlxDatatypes.Array.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            if (extensionRegistry == null) {
               throw new NullPointerException();
            } else {
               try {
                  boolean done = false;

                  while (!done) {
                     int tag = input.readTag();
                     switch (tag) {
                        case 0:
                           done = true;
                           break;
                        case 10:
                           MysqlxDatatypes.Any m = (MysqlxDatatypes.Any)input.readMessage(MysqlxDatatypes.Any.parser(), extensionRegistry);
                           if (this.valueBuilder_ == null) {
                              this.ensureValueIsMutable();
                              this.value_.add(m);
                           } else {
                              this.valueBuilder_.addMessage(m);
                           }
                           break;
                        default:
                           if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                              done = true;
                           }
                     }
                  }
               } catch (InvalidProtocolBufferException var9) {
                  throw var9.unwrapIOException();
               } finally {
                  this.onChanged();
               }

               return this;
            }
         }

         private void ensureValueIsMutable() {
            if ((this.bitField0_ & 1) == 0) {
               this.value_ = new ArrayList<>(this.value_);
               this.bitField0_ |= 1;
            }
         }

         @Override
         public List<MysqlxDatatypes.Any> getValueList() {
            return this.valueBuilder_ == null ? Collections.unmodifiableList(this.value_) : this.valueBuilder_.getMessageList();
         }

         @Override
         public int getValueCount() {
            return this.valueBuilder_ == null ? this.value_.size() : this.valueBuilder_.getCount();
         }

         @Override
         public MysqlxDatatypes.Any getValue(int index) {
            return this.valueBuilder_ == null ? this.value_.get(index) : (MysqlxDatatypes.Any)this.valueBuilder_.getMessage(index);
         }

         public MysqlxDatatypes.Array.Builder setValue(int index, MysqlxDatatypes.Any value) {
            if (this.valueBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureValueIsMutable();
               this.value_.set(index, value);
               this.onChanged();
            } else {
               this.valueBuilder_.setMessage(index, value);
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder setValue(int index, MysqlxDatatypes.Any.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder addValue(MysqlxDatatypes.Any value) {
            if (this.valueBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureValueIsMutable();
               this.value_.add(value);
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(value);
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder addValue(int index, MysqlxDatatypes.Any value) {
            if (this.valueBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureValueIsMutable();
               this.value_.add(index, value);
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(index, value);
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder addValue(MysqlxDatatypes.Any.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder addValue(int index, MysqlxDatatypes.Any.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder addAllValue(Iterable<? extends MysqlxDatatypes.Any> values) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.value_);
               this.onChanged();
            } else {
               this.valueBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder clearValue() {
            if (this.valueBuilder_ == null) {
               this.value_ = Collections.emptyList();
               this.bitField0_ &= -2;
               this.onChanged();
            } else {
               this.valueBuilder_.clear();
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder removeValue(int index) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.remove(index);
               this.onChanged();
            } else {
               this.valueBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxDatatypes.Any.Builder getValueBuilder(int index) {
            return (MysqlxDatatypes.Any.Builder)this.getValueFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxDatatypes.AnyOrBuilder getValueOrBuilder(int index) {
            return this.valueBuilder_ == null ? this.value_.get(index) : (MysqlxDatatypes.AnyOrBuilder)this.valueBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxDatatypes.AnyOrBuilder> getValueOrBuilderList() {
            return this.valueBuilder_ != null ? this.valueBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.value_);
         }

         public MysqlxDatatypes.Any.Builder addValueBuilder() {
            return (MysqlxDatatypes.Any.Builder)this.getValueFieldBuilder().addBuilder(MysqlxDatatypes.Any.getDefaultInstance());
         }

         public MysqlxDatatypes.Any.Builder addValueBuilder(int index) {
            return (MysqlxDatatypes.Any.Builder)this.getValueFieldBuilder().addBuilder(index, MysqlxDatatypes.Any.getDefaultInstance());
         }

         public List<MysqlxDatatypes.Any.Builder> getValueBuilderList() {
            return this.getValueFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> getValueFieldBuilder() {
            if (this.valueBuilder_ == null) {
               this.valueBuilder_ = new RepeatedFieldBuilder(this.value_, (this.bitField0_ & 1) != 0, this.getParentForChildren(), this.isClean());
               this.value_ = null;
            }

            return this.valueBuilder_;
         }
      }
   }

   public interface ArrayOrBuilder extends MessageOrBuilder {
      List<MysqlxDatatypes.Any> getValueList();

      MysqlxDatatypes.Any getValue(int var1);

      int getValueCount();

      List<? extends MysqlxDatatypes.AnyOrBuilder> getValueOrBuilderList();

      MysqlxDatatypes.AnyOrBuilder getValueOrBuilder(int var1);
   }

   public static final class Object extends GeneratedMessage implements MysqlxDatatypes.ObjectOrBuilder {
      private static final long serialVersionUID = 0L;
      public static final int FLD_FIELD_NUMBER = 1;
      private List<MysqlxDatatypes.Object.ObjectField> fld_;
      private byte memoizedIsInitialized = -1;
      private static final MysqlxDatatypes.Object DEFAULT_INSTANCE = new MysqlxDatatypes.Object();
      private static final Parser<MysqlxDatatypes.Object> PARSER = new AbstractParser<MysqlxDatatypes.Object>() {
         public MysqlxDatatypes.Object parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            MysqlxDatatypes.Object.Builder builder = MysqlxDatatypes.Object.newBuilder();

            try {
               builder.mergeFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var5) {
               throw var5.setUnfinishedMessage(builder.buildPartial());
            } catch (UninitializedMessageException var6) {
               throw var6.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
            } catch (IOException var7) {
               throw new InvalidProtocolBufferException(var7).setUnfinishedMessage(builder.buildPartial());
            }

            return builder.buildPartial();
         }
      };

      private Object(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private Object() {
         this.fld_ = Collections.emptyList();
      }

      public static final Descriptor getDescriptor() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxDatatypes.Object.class, MysqlxDatatypes.Object.Builder.class);
      }

      @Override
      public List<MysqlxDatatypes.Object.ObjectField> getFldList() {
         return this.fld_;
      }

      @Override
      public List<? extends MysqlxDatatypes.Object.ObjectFieldOrBuilder> getFldOrBuilderList() {
         return this.fld_;
      }

      @Override
      public int getFldCount() {
         return this.fld_.size();
      }

      @Override
      public MysqlxDatatypes.Object.ObjectField getFld(int index) {
         return this.fld_.get(index);
      }

      @Override
      public MysqlxDatatypes.Object.ObjectFieldOrBuilder getFldOrBuilder(int index) {
         return this.fld_.get(index);
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            for (int i = 0; i < this.getFldCount(); i++) {
               if (!this.getFld(i).isInitialized()) {
                  this.memoizedIsInitialized = 0;
                  return false;
               }
            }

            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         for (int i = 0; i < this.fld_.size(); i++) {
            output.writeMessage(1, (MessageLite)this.fld_.get(i));
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;

            for (int i = 0; i < this.fld_.size(); i++) {
               size += CodedOutputStream.computeMessageSize(1, (MessageLite)this.fld_.get(i));
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSize = size;
            return size;
         }
      }

      @Override
      public boolean equals(java.lang.Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof MysqlxDatatypes.Object)) {
            return super.equals(obj);
         } else {
            MysqlxDatatypes.Object other = (MysqlxDatatypes.Object)obj;
            return !this.getFldList().equals(other.getFldList()) ? false : this.getUnknownFields().equals(other.getUnknownFields());
         }
      }

      @Override
      public int hashCode() {
         if (this.memoizedHashCode != 0) {
            return this.memoizedHashCode;
         } else {
            int hash = 41;
            hash = 19 * hash + getDescriptor().hashCode();
            if (this.getFldCount() > 0) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.getFldList().hashCode();
            }

            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static MysqlxDatatypes.Object parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Object parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Object parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Object parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Object parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Object parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Object parseFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Object)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Object parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Object)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxDatatypes.Object parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Object)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Object parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Object)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxDatatypes.Object parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxDatatypes.Object)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Object parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Object)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public MysqlxDatatypes.Object.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxDatatypes.Object.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static MysqlxDatatypes.Object.Builder newBuilder(MysqlxDatatypes.Object prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public MysqlxDatatypes.Object.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new MysqlxDatatypes.Object.Builder() : new MysqlxDatatypes.Object.Builder().mergeFrom(this);
      }

      protected MysqlxDatatypes.Object.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxDatatypes.Object.Builder(parent);
      }

      public static MysqlxDatatypes.Object getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<MysqlxDatatypes.Object> parser() {
         return PARSER;
      }

      public Parser<MysqlxDatatypes.Object> getParserForType() {
         return PARSER;
      }

      public MysqlxDatatypes.Object getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxDatatypes.Object.class.getName());
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Object.Builder>
         implements MysqlxDatatypes.ObjectOrBuilder {
         private int bitField0_;
         private List<MysqlxDatatypes.Object.ObjectField> fld_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxDatatypes.Object.ObjectField, MysqlxDatatypes.Object.ObjectField.Builder, MysqlxDatatypes.Object.ObjectFieldOrBuilder> fldBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Object.class, MysqlxDatatypes.Object.Builder.class);
         }

         private Builder() {
         }

         private Builder(BuilderParent parent) {
            super(parent);
         }

         public MysqlxDatatypes.Object.Builder clear() {
            super.clear();
            this.bitField0_ = 0;
            if (this.fldBuilder_ == null) {
               this.fld_ = Collections.emptyList();
            } else {
               this.fld_ = null;
               this.fldBuilder_.clear();
            }

            this.bitField0_ &= -2;
            return this;
         }

         public Descriptor getDescriptorForType() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_descriptor;
         }

         public MysqlxDatatypes.Object getDefaultInstanceForType() {
            return MysqlxDatatypes.Object.getDefaultInstance();
         }

         public MysqlxDatatypes.Object build() {
            MysqlxDatatypes.Object result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxDatatypes.Object buildPartial() {
            MysqlxDatatypes.Object result = new MysqlxDatatypes.Object(this);
            this.buildPartialRepeatedFields(result);
            if (this.bitField0_ != 0) {
               this.buildPartial0(result);
            }

            this.onBuilt();
            return result;
         }

         private void buildPartialRepeatedFields(MysqlxDatatypes.Object result) {
            if (this.fldBuilder_ == null) {
               if ((this.bitField0_ & 1) != 0) {
                  this.fld_ = Collections.unmodifiableList(this.fld_);
                  this.bitField0_ &= -2;
               }

               result.fld_ = this.fld_;
            } else {
               result.fld_ = this.fldBuilder_.build();
            }
         }

         private void buildPartial0(MysqlxDatatypes.Object result) {
            int from_bitField0_ = this.bitField0_;
         }

         public MysqlxDatatypes.Object.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxDatatypes.Object) {
               return this.mergeFrom((MysqlxDatatypes.Object)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxDatatypes.Object.Builder mergeFrom(MysqlxDatatypes.Object other) {
            if (other == MysqlxDatatypes.Object.getDefaultInstance()) {
               return this;
            } else {
               if (this.fldBuilder_ == null) {
                  if (!other.fld_.isEmpty()) {
                     if (this.fld_.isEmpty()) {
                        this.fld_ = other.fld_;
                        this.bitField0_ &= -2;
                     } else {
                        this.ensureFldIsMutable();
                        this.fld_.addAll(other.fld_);
                     }

                     this.onChanged();
                  }
               } else if (!other.fld_.isEmpty()) {
                  if (this.fldBuilder_.isEmpty()) {
                     this.fldBuilder_.dispose();
                     this.fldBuilder_ = null;
                     this.fld_ = other.fld_;
                     this.bitField0_ &= -2;
                     this.fldBuilder_ = MysqlxDatatypes.Object.alwaysUseFieldBuilders ? this.getFldFieldBuilder() : null;
                  } else {
                     this.fldBuilder_.addAllMessages(other.fld_);
                  }
               }

               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            for (int i = 0; i < this.getFldCount(); i++) {
               if (!this.getFld(i).isInitialized()) {
                  return false;
               }
            }

            return true;
         }

         public MysqlxDatatypes.Object.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            if (extensionRegistry == null) {
               throw new NullPointerException();
            } else {
               try {
                  boolean done = false;

                  while (!done) {
                     int tag = input.readTag();
                     switch (tag) {
                        case 0:
                           done = true;
                           break;
                        case 10:
                           MysqlxDatatypes.Object.ObjectField m = (MysqlxDatatypes.Object.ObjectField)input.readMessage(
                              MysqlxDatatypes.Object.ObjectField.parser(), extensionRegistry
                           );
                           if (this.fldBuilder_ == null) {
                              this.ensureFldIsMutable();
                              this.fld_.add(m);
                           } else {
                              this.fldBuilder_.addMessage(m);
                           }
                           break;
                        default:
                           if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                              done = true;
                           }
                     }
                  }
               } catch (InvalidProtocolBufferException var9) {
                  throw var9.unwrapIOException();
               } finally {
                  this.onChanged();
               }

               return this;
            }
         }

         private void ensureFldIsMutable() {
            if ((this.bitField0_ & 1) == 0) {
               this.fld_ = new ArrayList<>(this.fld_);
               this.bitField0_ |= 1;
            }
         }

         @Override
         public List<MysqlxDatatypes.Object.ObjectField> getFldList() {
            return this.fldBuilder_ == null ? Collections.unmodifiableList(this.fld_) : this.fldBuilder_.getMessageList();
         }

         @Override
         public int getFldCount() {
            return this.fldBuilder_ == null ? this.fld_.size() : this.fldBuilder_.getCount();
         }

         @Override
         public MysqlxDatatypes.Object.ObjectField getFld(int index) {
            return this.fldBuilder_ == null ? this.fld_.get(index) : (MysqlxDatatypes.Object.ObjectField)this.fldBuilder_.getMessage(index);
         }

         public MysqlxDatatypes.Object.Builder setFld(int index, MysqlxDatatypes.Object.ObjectField value) {
            if (this.fldBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureFldIsMutable();
               this.fld_.set(index, value);
               this.onChanged();
            } else {
               this.fldBuilder_.setMessage(index, value);
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder setFld(int index, MysqlxDatatypes.Object.ObjectField.Builder builderForValue) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.fldBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder addFld(MysqlxDatatypes.Object.ObjectField value) {
            if (this.fldBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureFldIsMutable();
               this.fld_.add(value);
               this.onChanged();
            } else {
               this.fldBuilder_.addMessage(value);
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder addFld(int index, MysqlxDatatypes.Object.ObjectField value) {
            if (this.fldBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureFldIsMutable();
               this.fld_.add(index, value);
               this.onChanged();
            } else {
               this.fldBuilder_.addMessage(index, value);
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder addFld(MysqlxDatatypes.Object.ObjectField.Builder builderForValue) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.fldBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder addFld(int index, MysqlxDatatypes.Object.ObjectField.Builder builderForValue) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.fldBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder addAllFld(Iterable<? extends MysqlxDatatypes.Object.ObjectField> values) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.fld_);
               this.onChanged();
            } else {
               this.fldBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder clearFld() {
            if (this.fldBuilder_ == null) {
               this.fld_ = Collections.emptyList();
               this.bitField0_ &= -2;
               this.onChanged();
            } else {
               this.fldBuilder_.clear();
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder removeFld(int index) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.remove(index);
               this.onChanged();
            } else {
               this.fldBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxDatatypes.Object.ObjectField.Builder getFldBuilder(int index) {
            return (MysqlxDatatypes.Object.ObjectField.Builder)this.getFldFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxDatatypes.Object.ObjectFieldOrBuilder getFldOrBuilder(int index) {
            return this.fldBuilder_ == null ? this.fld_.get(index) : (MysqlxDatatypes.Object.ObjectFieldOrBuilder)this.fldBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxDatatypes.Object.ObjectFieldOrBuilder> getFldOrBuilderList() {
            return this.fldBuilder_ != null ? this.fldBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.fld_);
         }

         public MysqlxDatatypes.Object.ObjectField.Builder addFldBuilder() {
            return (MysqlxDatatypes.Object.ObjectField.Builder)this.getFldFieldBuilder().addBuilder(MysqlxDatatypes.Object.ObjectField.getDefaultInstance());
         }

         public MysqlxDatatypes.Object.ObjectField.Builder addFldBuilder(int index) {
            return (MysqlxDatatypes.Object.ObjectField.Builder)this.getFldFieldBuilder()
               .addBuilder(index, MysqlxDatatypes.Object.ObjectField.getDefaultInstance());
         }

         public List<MysqlxDatatypes.Object.ObjectField.Builder> getFldBuilderList() {
            return this.getFldFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxDatatypes.Object.ObjectField, MysqlxDatatypes.Object.ObjectField.Builder, MysqlxDatatypes.Object.ObjectFieldOrBuilder> getFldFieldBuilder() {
            if (this.fldBuilder_ == null) {
               this.fldBuilder_ = new RepeatedFieldBuilder(this.fld_, (this.bitField0_ & 1) != 0, this.getParentForChildren(), this.isClean());
               this.fld_ = null;
            }

            return this.fldBuilder_;
         }
      }

      public static final class ObjectField extends GeneratedMessage implements MysqlxDatatypes.Object.ObjectFieldOrBuilder {
         private static final long serialVersionUID = 0L;
         private int bitField0_;
         public static final int KEY_FIELD_NUMBER = 1;
         private volatile java.lang.Object key_ = "";
         public static final int VALUE_FIELD_NUMBER = 2;
         private MysqlxDatatypes.Any value_;
         private byte memoizedIsInitialized = -1;
         private static final MysqlxDatatypes.Object.ObjectField DEFAULT_INSTANCE = new MysqlxDatatypes.Object.ObjectField();
         private static final Parser<MysqlxDatatypes.Object.ObjectField> PARSER = new AbstractParser<MysqlxDatatypes.Object.ObjectField>() {
            public MysqlxDatatypes.Object.ObjectField parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
               MysqlxDatatypes.Object.ObjectField.Builder builder = MysqlxDatatypes.Object.ObjectField.newBuilder();

               try {
                  builder.mergeFrom(input, extensionRegistry);
               } catch (InvalidProtocolBufferException var5) {
                  throw var5.setUnfinishedMessage(builder.buildPartial());
               } catch (UninitializedMessageException var6) {
                  throw var6.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
               } catch (IOException var7) {
                  throw new InvalidProtocolBufferException(var7).setUnfinishedMessage(builder.buildPartial());
               }

               return builder.buildPartial();
            }
         };

         private ObjectField(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
         }

         private ObjectField() {
            this.key_ = "";
         }

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_ObjectField_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_ObjectField_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Object.ObjectField.class, MysqlxDatatypes.Object.ObjectField.Builder.class);
         }

         @Override
         public boolean hasKey() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public java.lang.String getKey() {
            java.lang.Object ref = this.key_;
            if (ref instanceof java.lang.String) {
               return (java.lang.String)ref;
            } else {
               ByteString bs = (ByteString)ref;
               java.lang.String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.key_ = s;
               }

               return s;
            }
         }

         @Override
         public ByteString getKeyBytes() {
            java.lang.Object ref = this.key_;
            if (ref instanceof java.lang.String) {
               ByteString b = ByteString.copyFromUtf8((java.lang.String)ref);
               this.key_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         @Override
         public boolean hasValue() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public MysqlxDatatypes.Any getValue() {
            return this.value_ == null ? MysqlxDatatypes.Any.getDefaultInstance() : this.value_;
         }

         @Override
         public MysqlxDatatypes.AnyOrBuilder getValueOrBuilder() {
            return this.value_ == null ? MysqlxDatatypes.Any.getDefaultInstance() : this.value_;
         }

         public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
               return true;
            } else if (isInitialized == 0) {
               return false;
            } else if (!this.hasKey()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else if (!this.hasValue()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else if (!this.getValue().isInitialized()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else {
               this.memoizedIsInitialized = 1;
               return true;
            }
         }

         public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
               GeneratedMessage.writeString(output, 1, this.key_);
            }

            if ((this.bitField0_ & 2) != 0) {
               output.writeMessage(2, this.getValue());
            }

            this.getUnknownFields().writeTo(output);
         }

         public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
               return size;
            } else {
               size = 0;
               if ((this.bitField0_ & 1) != 0) {
                  size += GeneratedMessage.computeStringSize(1, this.key_);
               }

               if ((this.bitField0_ & 2) != 0) {
                  size += CodedOutputStream.computeMessageSize(2, this.getValue());
               }

               size += this.getUnknownFields().getSerializedSize();
               this.memoizedSize = size;
               return size;
            }
         }

         @Override
         public boolean equals(java.lang.Object obj) {
            if (obj == this) {
               return true;
            } else if (!(obj instanceof MysqlxDatatypes.Object.ObjectField)) {
               return super.equals(obj);
            } else {
               MysqlxDatatypes.Object.ObjectField other = (MysqlxDatatypes.Object.ObjectField)obj;
               if (this.hasKey() != other.hasKey()) {
                  return false;
               } else if (this.hasKey() && !this.getKey().equals(other.getKey())) {
                  return false;
               } else if (this.hasValue() != other.hasValue()) {
                  return false;
               } else {
                  return this.hasValue() && !this.getValue().equals(other.getValue()) ? false : this.getUnknownFields().equals(other.getUnknownFields());
               }
            }
         }

         @Override
         public int hashCode() {
            if (this.memoizedHashCode != 0) {
               return this.memoizedHashCode;
            } else {
               int hash = 41;
               hash = 19 * hash + getDescriptor().hashCode();
               if (this.hasKey()) {
                  hash = 37 * hash + 1;
                  hash = 53 * hash + this.getKey().hashCode();
               }

               if (this.hasValue()) {
                  hash = 37 * hash + 2;
                  hash = 53 * hash + this.getValue().hashCode();
               }

               hash = 29 * hash + this.getUnknownFields().hashCode();
               this.memoizedHashCode = hash;
               return hash;
            }
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)GeneratedMessage.parseWithIOException(PARSER, input);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
         }

         public static MysqlxDatatypes.Object.ObjectField parseDelimitedFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
         }

         public static MysqlxDatatypes.Object.ObjectField parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(CodedInputStream input) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)GeneratedMessage.parseWithIOException(PARSER, input);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
         }

         public MysqlxDatatypes.Object.ObjectField.Builder newBuilderForType() {
            return newBuilder();
         }

         public static MysqlxDatatypes.Object.ObjectField.Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
         }

         public static MysqlxDatatypes.Object.ObjectField.Builder newBuilder(MysqlxDatatypes.Object.ObjectField prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
         }

         public MysqlxDatatypes.Object.ObjectField.Builder toBuilder() {
            return this == DEFAULT_INSTANCE
               ? new MysqlxDatatypes.Object.ObjectField.Builder()
               : new MysqlxDatatypes.Object.ObjectField.Builder().mergeFrom(this);
         }

         protected MysqlxDatatypes.Object.ObjectField.Builder newBuilderForType(BuilderParent parent) {
            return new MysqlxDatatypes.Object.ObjectField.Builder(parent);
         }

         public static MysqlxDatatypes.Object.ObjectField getDefaultInstance() {
            return DEFAULT_INSTANCE;
         }

         public static Parser<MysqlxDatatypes.Object.ObjectField> parser() {
            return PARSER;
         }

         public Parser<MysqlxDatatypes.Object.ObjectField> getParserForType() {
            return PARSER;
         }

         public MysqlxDatatypes.Object.ObjectField getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
         }

         static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxDatatypes.Object.ObjectField.class.getName());
         }

         public static final class Builder
            extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Object.ObjectField.Builder>
            implements MysqlxDatatypes.Object.ObjectFieldOrBuilder {
            private int bitField0_;
            private java.lang.Object key_ = "";
            private MysqlxDatatypes.Any value_;
            private SingleFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> valueBuilder_;

            public static final Descriptor getDescriptor() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_ObjectField_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_ObjectField_fieldAccessorTable
                  .ensureFieldAccessorsInitialized(MysqlxDatatypes.Object.ObjectField.class, MysqlxDatatypes.Object.ObjectField.Builder.class);
            }

            private Builder() {
               this.maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
               super(parent);
               this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
               if (MysqlxDatatypes.Object.ObjectField.alwaysUseFieldBuilders) {
                  this.getValueFieldBuilder();
               }
            }

            public MysqlxDatatypes.Object.ObjectField.Builder clear() {
               super.clear();
               this.bitField0_ = 0;
               this.key_ = "";
               this.value_ = null;
               if (this.valueBuilder_ != null) {
                  this.valueBuilder_.dispose();
                  this.valueBuilder_ = null;
               }

               return this;
            }

            public Descriptor getDescriptorForType() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_ObjectField_descriptor;
            }

            public MysqlxDatatypes.Object.ObjectField getDefaultInstanceForType() {
               return MysqlxDatatypes.Object.ObjectField.getDefaultInstance();
            }

            public MysqlxDatatypes.Object.ObjectField build() {
               MysqlxDatatypes.Object.ObjectField result = this.buildPartial();
               if (!result.isInitialized()) {
                  throw newUninitializedMessageException(result);
               } else {
                  return result;
               }
            }

            public MysqlxDatatypes.Object.ObjectField buildPartial() {
               MysqlxDatatypes.Object.ObjectField result = new MysqlxDatatypes.Object.ObjectField(this);
               if (this.bitField0_ != 0) {
                  this.buildPartial0(result);
               }

               this.onBuilt();
               return result;
            }

            private void buildPartial0(MysqlxDatatypes.Object.ObjectField result) {
               int from_bitField0_ = this.bitField0_;
               int to_bitField0_ = 0;
               if ((from_bitField0_ & 1) != 0) {
                  result.key_ = this.key_;
                  to_bitField0_ |= 1;
               }

               if ((from_bitField0_ & 2) != 0) {
                  result.value_ = this.valueBuilder_ == null ? this.value_ : (MysqlxDatatypes.Any)this.valueBuilder_.build();
                  to_bitField0_ |= 2;
               }

               result.bitField0_ = result.bitField0_ | to_bitField0_;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder mergeFrom(Message other) {
               if (other instanceof MysqlxDatatypes.Object.ObjectField) {
                  return this.mergeFrom((MysqlxDatatypes.Object.ObjectField)other);
               } else {
                  super.mergeFrom(other);
                  return this;
               }
            }

            public MysqlxDatatypes.Object.ObjectField.Builder mergeFrom(MysqlxDatatypes.Object.ObjectField other) {
               if (other == MysqlxDatatypes.Object.ObjectField.getDefaultInstance()) {
                  return this;
               } else {
                  if (other.hasKey()) {
                     this.key_ = other.key_;
                     this.bitField0_ |= 1;
                     this.onChanged();
                  }

                  if (other.hasValue()) {
                     this.mergeValue(other.getValue());
                  }

                  this.mergeUnknownFields(other.getUnknownFields());
                  this.onChanged();
                  return this;
               }
            }

            public final boolean isInitialized() {
               if (!this.hasKey()) {
                  return false;
               } else {
                  return !this.hasValue() ? false : this.getValue().isInitialized();
               }
            }

            public MysqlxDatatypes.Object.ObjectField.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
               if (extensionRegistry == null) {
                  throw new NullPointerException();
               } else {
                  try {
                     boolean done = false;

                     while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                           case 0:
                              done = true;
                              break;
                           case 10:
                              this.key_ = input.readBytes();
                              this.bitField0_ |= 1;
                              break;
                           case 18:
                              input.readMessage(this.getValueFieldBuilder().getBuilder(), extensionRegistry);
                              this.bitField0_ |= 2;
                              break;
                           default:
                              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                                 done = true;
                              }
                        }
                     }
                  } catch (InvalidProtocolBufferException var8) {
                     throw var8.unwrapIOException();
                  } finally {
                     this.onChanged();
                  }

                  return this;
               }
            }

            @Override
            public boolean hasKey() {
               return (this.bitField0_ & 1) != 0;
            }

            @Override
            public java.lang.String getKey() {
               java.lang.Object ref = this.key_;
               if (!(ref instanceof java.lang.String)) {
                  ByteString bs = (ByteString)ref;
                  java.lang.String s = bs.toStringUtf8();
                  if (bs.isValidUtf8()) {
                     this.key_ = s;
                  }

                  return s;
               } else {
                  return (java.lang.String)ref;
               }
            }

            @Override
            public ByteString getKeyBytes() {
               java.lang.Object ref = this.key_;
               if (ref instanceof java.lang.String) {
                  ByteString b = ByteString.copyFromUtf8((java.lang.String)ref);
                  this.key_ = b;
                  return b;
               } else {
                  return (ByteString)ref;
               }
            }

            public MysqlxDatatypes.Object.ObjectField.Builder setKey(java.lang.String value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.key_ = value;
                  this.bitField0_ |= 1;
                  this.onChanged();
                  return this;
               }
            }

            public MysqlxDatatypes.Object.ObjectField.Builder clearKey() {
               this.key_ = MysqlxDatatypes.Object.ObjectField.getDefaultInstance().getKey();
               this.bitField0_ &= -2;
               this.onChanged();
               return this;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder setKeyBytes(ByteString value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.key_ = value;
                  this.bitField0_ |= 1;
                  this.onChanged();
                  return this;
               }
            }

            @Override
            public boolean hasValue() {
               return (this.bitField0_ & 2) != 0;
            }

            @Override
            public MysqlxDatatypes.Any getValue() {
               if (this.valueBuilder_ == null) {
                  return this.value_ == null ? MysqlxDatatypes.Any.getDefaultInstance() : this.value_;
               } else {
                  return (MysqlxDatatypes.Any)this.valueBuilder_.getMessage();
               }
            }

            public MysqlxDatatypes.Object.ObjectField.Builder setValue(MysqlxDatatypes.Any value) {
               if (this.valueBuilder_ == null) {
                  if (value == null) {
                     throw new NullPointerException();
                  }

                  this.value_ = value;
               } else {
                  this.valueBuilder_.setMessage(value);
               }

               this.bitField0_ |= 2;
               this.onChanged();
               return this;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder setValue(MysqlxDatatypes.Any.Builder builderForValue) {
               if (this.valueBuilder_ == null) {
                  this.value_ = builderForValue.build();
               } else {
                  this.valueBuilder_.setMessage(builderForValue.build());
               }

               this.bitField0_ |= 2;
               this.onChanged();
               return this;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder mergeValue(MysqlxDatatypes.Any value) {
               if (this.valueBuilder_ == null) {
                  if ((this.bitField0_ & 2) != 0 && this.value_ != null && this.value_ != MysqlxDatatypes.Any.getDefaultInstance()) {
                     this.getValueBuilder().mergeFrom(value);
                  } else {
                     this.value_ = value;
                  }
               } else {
                  this.valueBuilder_.mergeFrom(value);
               }

               if (this.value_ != null) {
                  this.bitField0_ |= 2;
                  this.onChanged();
               }

               return this;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder clearValue() {
               this.bitField0_ &= -3;
               this.value_ = null;
               if (this.valueBuilder_ != null) {
                  this.valueBuilder_.dispose();
                  this.valueBuilder_ = null;
               }

               this.onChanged();
               return this;
            }

            public MysqlxDatatypes.Any.Builder getValueBuilder() {
               this.bitField0_ |= 2;
               this.onChanged();
               return (MysqlxDatatypes.Any.Builder)this.getValueFieldBuilder().getBuilder();
            }

            @Override
            public MysqlxDatatypes.AnyOrBuilder getValueOrBuilder() {
               if (this.valueBuilder_ != null) {
                  return (MysqlxDatatypes.AnyOrBuilder)this.valueBuilder_.getMessageOrBuilder();
               } else {
                  return this.value_ == null ? MysqlxDatatypes.Any.getDefaultInstance() : this.value_;
               }
            }

            private SingleFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> getValueFieldBuilder() {
               if (this.valueBuilder_ == null) {
                  this.valueBuilder_ = new SingleFieldBuilder(this.getValue(), this.getParentForChildren(), this.isClean());
                  this.value_ = null;
               }

               return this.valueBuilder_;
            }
         }
      }

      public interface ObjectFieldOrBuilder extends MessageOrBuilder {
         boolean hasKey();

         java.lang.String getKey();

         ByteString getKeyBytes();

         boolean hasValue();

         MysqlxDatatypes.Any getValue();

         MysqlxDatatypes.AnyOrBuilder getValueOrBuilder();
      }
   }

   public interface ObjectOrBuilder extends MessageOrBuilder {
      List<MysqlxDatatypes.Object.ObjectField> getFldList();

      MysqlxDatatypes.Object.ObjectField getFld(int var1);

      int getFldCount();

      List<? extends MysqlxDatatypes.Object.ObjectFieldOrBuilder> getFldOrBuilderList();

      MysqlxDatatypes.Object.ObjectFieldOrBuilder getFldOrBuilder(int var1);
   }

   public static final class Scalar extends GeneratedMessage implements MysqlxDatatypes.ScalarOrBuilder {
      private static final long serialVersionUID = 0L;
      private int bitField0_;
      public static final int TYPE_FIELD_NUMBER = 1;
      private int type_ = 1;
      public static final int V_SIGNED_INT_FIELD_NUMBER = 2;
      private long vSignedInt_ = 0L;
      public static final int V_UNSIGNED_INT_FIELD_NUMBER = 3;
      private long vUnsignedInt_ = 0L;
      public static final int V_OCTETS_FIELD_NUMBER = 5;
      private MysqlxDatatypes.Scalar.Octets vOctets_;
      public static final int V_DOUBLE_FIELD_NUMBER = 6;
      private double vDouble_ = 0.0;
      public static final int V_FLOAT_FIELD_NUMBER = 7;
      private float vFloat_ = 0.0F;
      public static final int V_BOOL_FIELD_NUMBER = 8;
      private boolean vBool_ = false;
      public static final int V_STRING_FIELD_NUMBER = 9;
      private MysqlxDatatypes.Scalar.String vString_;
      private byte memoizedIsInitialized = -1;
      private static final MysqlxDatatypes.Scalar DEFAULT_INSTANCE = new MysqlxDatatypes.Scalar();
      private static final Parser<MysqlxDatatypes.Scalar> PARSER = new AbstractParser<MysqlxDatatypes.Scalar>() {
         public MysqlxDatatypes.Scalar parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            MysqlxDatatypes.Scalar.Builder builder = MysqlxDatatypes.Scalar.newBuilder();

            try {
               builder.mergeFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var5) {
               throw var5.setUnfinishedMessage(builder.buildPartial());
            } catch (UninitializedMessageException var6) {
               throw var6.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
            } catch (IOException var7) {
               throw new InvalidProtocolBufferException(var7).setUnfinishedMessage(builder.buildPartial());
            }

            return builder.buildPartial();
         }
      };

      private Scalar(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private Scalar() {
         this.type_ = 1;
      }

      public static final Descriptor getDescriptor() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.class, MysqlxDatatypes.Scalar.Builder.class);
      }

      @Override
      public boolean hasType() {
         return (this.bitField0_ & 1) != 0;
      }

      @Override
      public MysqlxDatatypes.Scalar.Type getType() {
         MysqlxDatatypes.Scalar.Type result = MysqlxDatatypes.Scalar.Type.forNumber(this.type_);
         return result == null ? MysqlxDatatypes.Scalar.Type.V_SINT : result;
      }

      @Override
      public boolean hasVSignedInt() {
         return (this.bitField0_ & 2) != 0;
      }

      @Override
      public long getVSignedInt() {
         return this.vSignedInt_;
      }

      @Override
      public boolean hasVUnsignedInt() {
         return (this.bitField0_ & 4) != 0;
      }

      @Override
      public long getVUnsignedInt() {
         return this.vUnsignedInt_;
      }

      @Override
      public boolean hasVOctets() {
         return (this.bitField0_ & 8) != 0;
      }

      @Override
      public MysqlxDatatypes.Scalar.Octets getVOctets() {
         return this.vOctets_ == null ? MysqlxDatatypes.Scalar.Octets.getDefaultInstance() : this.vOctets_;
      }

      @Override
      public MysqlxDatatypes.Scalar.OctetsOrBuilder getVOctetsOrBuilder() {
         return this.vOctets_ == null ? MysqlxDatatypes.Scalar.Octets.getDefaultInstance() : this.vOctets_;
      }

      @Override
      public boolean hasVDouble() {
         return (this.bitField0_ & 16) != 0;
      }

      @Override
      public double getVDouble() {
         return this.vDouble_;
      }

      @Override
      public boolean hasVFloat() {
         return (this.bitField0_ & 32) != 0;
      }

      @Override
      public float getVFloat() {
         return this.vFloat_;
      }

      @Override
      public boolean hasVBool() {
         return (this.bitField0_ & 64) != 0;
      }

      @Override
      public boolean getVBool() {
         return this.vBool_;
      }

      @Override
      public boolean hasVString() {
         return (this.bitField0_ & 128) != 0;
      }

      @Override
      public MysqlxDatatypes.Scalar.String getVString() {
         return this.vString_ == null ? MysqlxDatatypes.Scalar.String.getDefaultInstance() : this.vString_;
      }

      @Override
      public MysqlxDatatypes.Scalar.StringOrBuilder getVStringOrBuilder() {
         return this.vString_ == null ? MysqlxDatatypes.Scalar.String.getDefaultInstance() : this.vString_;
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasType()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasVOctets() && !this.getVOctets().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasVString() && !this.getVString().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         if ((this.bitField0_ & 1) != 0) {
            output.writeEnum(1, this.type_);
         }

         if ((this.bitField0_ & 2) != 0) {
            output.writeSInt64(2, this.vSignedInt_);
         }

         if ((this.bitField0_ & 4) != 0) {
            output.writeUInt64(3, this.vUnsignedInt_);
         }

         if ((this.bitField0_ & 8) != 0) {
            output.writeMessage(5, this.getVOctets());
         }

         if ((this.bitField0_ & 16) != 0) {
            output.writeDouble(6, this.vDouble_);
         }

         if ((this.bitField0_ & 32) != 0) {
            output.writeFloat(7, this.vFloat_);
         }

         if ((this.bitField0_ & 64) != 0) {
            output.writeBool(8, this.vBool_);
         }

         if ((this.bitField0_ & 128) != 0) {
            output.writeMessage(9, this.getVString());
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;
            if ((this.bitField0_ & 1) != 0) {
               size += CodedOutputStream.computeEnumSize(1, this.type_);
            }

            if ((this.bitField0_ & 2) != 0) {
               size += CodedOutputStream.computeSInt64Size(2, this.vSignedInt_);
            }

            if ((this.bitField0_ & 4) != 0) {
               size += CodedOutputStream.computeUInt64Size(3, this.vUnsignedInt_);
            }

            if ((this.bitField0_ & 8) != 0) {
               size += CodedOutputStream.computeMessageSize(5, this.getVOctets());
            }

            if ((this.bitField0_ & 16) != 0) {
               size += CodedOutputStream.computeDoubleSize(6, this.vDouble_);
            }

            if ((this.bitField0_ & 32) != 0) {
               size += CodedOutputStream.computeFloatSize(7, this.vFloat_);
            }

            if ((this.bitField0_ & 64) != 0) {
               size += CodedOutputStream.computeBoolSize(8, this.vBool_);
            }

            if ((this.bitField0_ & 128) != 0) {
               size += CodedOutputStream.computeMessageSize(9, this.getVString());
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSize = size;
            return size;
         }
      }

      @Override
      public boolean equals(java.lang.Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof MysqlxDatatypes.Scalar)) {
            return super.equals(obj);
         } else {
            MysqlxDatatypes.Scalar other = (MysqlxDatatypes.Scalar)obj;
            if (this.hasType() != other.hasType()) {
               return false;
            } else if (this.hasType() && this.type_ != other.type_) {
               return false;
            } else if (this.hasVSignedInt() != other.hasVSignedInt()) {
               return false;
            } else if (this.hasVSignedInt() && this.getVSignedInt() != other.getVSignedInt()) {
               return false;
            } else if (this.hasVUnsignedInt() != other.hasVUnsignedInt()) {
               return false;
            } else if (this.hasVUnsignedInt() && this.getVUnsignedInt() != other.getVUnsignedInt()) {
               return false;
            } else if (this.hasVOctets() != other.hasVOctets()) {
               return false;
            } else if (this.hasVOctets() && !this.getVOctets().equals(other.getVOctets())) {
               return false;
            } else if (this.hasVDouble() != other.hasVDouble()) {
               return false;
            } else if (this.hasVDouble() && Double.doubleToLongBits(this.getVDouble()) != Double.doubleToLongBits(other.getVDouble())) {
               return false;
            } else if (this.hasVFloat() != other.hasVFloat()) {
               return false;
            } else if (this.hasVFloat() && Float.floatToIntBits(this.getVFloat()) != Float.floatToIntBits(other.getVFloat())) {
               return false;
            } else if (this.hasVBool() != other.hasVBool()) {
               return false;
            } else if (this.hasVBool() && this.getVBool() != other.getVBool()) {
               return false;
            } else if (this.hasVString() != other.hasVString()) {
               return false;
            } else {
               return this.hasVString() && !this.getVString().equals(other.getVString()) ? false : this.getUnknownFields().equals(other.getUnknownFields());
            }
         }
      }

      @Override
      public int hashCode() {
         if (this.memoizedHashCode != 0) {
            return this.memoizedHashCode;
         } else {
            int hash = 41;
            hash = 19 * hash + getDescriptor().hashCode();
            if (this.hasType()) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.type_;
            }

            if (this.hasVSignedInt()) {
               hash = 37 * hash + 2;
               hash = 53 * hash + Internal.hashLong(this.getVSignedInt());
            }

            if (this.hasVUnsignedInt()) {
               hash = 37 * hash + 3;
               hash = 53 * hash + Internal.hashLong(this.getVUnsignedInt());
            }

            if (this.hasVOctets()) {
               hash = 37 * hash + 5;
               hash = 53 * hash + this.getVOctets().hashCode();
            }

            if (this.hasVDouble()) {
               hash = 37 * hash + 6;
               hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getVDouble()));
            }

            if (this.hasVFloat()) {
               hash = 37 * hash + 7;
               hash = 53 * hash + Float.floatToIntBits(this.getVFloat());
            }

            if (this.hasVBool()) {
               hash = 37 * hash + 8;
               hash = 53 * hash + Internal.hashBoolean(this.getVBool());
            }

            if (this.hasVString()) {
               hash = 37 * hash + 9;
               hash = 53 * hash + this.getVString().hashCode();
            }

            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static MysqlxDatatypes.Scalar parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Scalar parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Scalar parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Scalar parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Scalar parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Scalar parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Scalar parseFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Scalar)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Scalar parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Scalar)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxDatatypes.Scalar parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Scalar)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Scalar parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Scalar)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxDatatypes.Scalar parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxDatatypes.Scalar)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxDatatypes.Scalar parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Scalar)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public MysqlxDatatypes.Scalar.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxDatatypes.Scalar.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static MysqlxDatatypes.Scalar.Builder newBuilder(MysqlxDatatypes.Scalar prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public MysqlxDatatypes.Scalar.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new MysqlxDatatypes.Scalar.Builder() : new MysqlxDatatypes.Scalar.Builder().mergeFrom(this);
      }

      protected MysqlxDatatypes.Scalar.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxDatatypes.Scalar.Builder(parent);
      }

      public static MysqlxDatatypes.Scalar getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<MysqlxDatatypes.Scalar> parser() {
         return PARSER;
      }

      public Parser<MysqlxDatatypes.Scalar> getParserForType() {
         return PARSER;
      }

      public MysqlxDatatypes.Scalar getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxDatatypes.Scalar.class.getName());
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Scalar.Builder>
         implements MysqlxDatatypes.ScalarOrBuilder {
         private int bitField0_;
         private int type_ = 1;
         private long vSignedInt_;
         private long vUnsignedInt_;
         private MysqlxDatatypes.Scalar.Octets vOctets_;
         private SingleFieldBuilder<MysqlxDatatypes.Scalar.Octets, MysqlxDatatypes.Scalar.Octets.Builder, MysqlxDatatypes.Scalar.OctetsOrBuilder> vOctetsBuilder_;
         private double vDouble_;
         private float vFloat_;
         private boolean vBool_;
         private MysqlxDatatypes.Scalar.String vString_;
         private SingleFieldBuilder<MysqlxDatatypes.Scalar.String, MysqlxDatatypes.Scalar.String.Builder, MysqlxDatatypes.Scalar.StringOrBuilder> vStringBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.class, MysqlxDatatypes.Scalar.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxDatatypes.Scalar.alwaysUseFieldBuilders) {
               this.getVOctetsFieldBuilder();
               this.getVStringFieldBuilder();
            }
         }

         public MysqlxDatatypes.Scalar.Builder clear() {
            super.clear();
            this.bitField0_ = 0;
            this.type_ = 1;
            this.vSignedInt_ = 0L;
            this.vUnsignedInt_ = 0L;
            this.vOctets_ = null;
            if (this.vOctetsBuilder_ != null) {
               this.vOctetsBuilder_.dispose();
               this.vOctetsBuilder_ = null;
            }

            this.vDouble_ = 0.0;
            this.vFloat_ = 0.0F;
            this.vBool_ = false;
            this.vString_ = null;
            if (this.vStringBuilder_ != null) {
               this.vStringBuilder_.dispose();
               this.vStringBuilder_ = null;
            }

            return this;
         }

         public Descriptor getDescriptorForType() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_descriptor;
         }

         public MysqlxDatatypes.Scalar getDefaultInstanceForType() {
            return MysqlxDatatypes.Scalar.getDefaultInstance();
         }

         public MysqlxDatatypes.Scalar build() {
            MysqlxDatatypes.Scalar result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxDatatypes.Scalar buildPartial() {
            MysqlxDatatypes.Scalar result = new MysqlxDatatypes.Scalar(this);
            if (this.bitField0_ != 0) {
               this.buildPartial0(result);
            }

            this.onBuilt();
            return result;
         }

         private void buildPartial0(MysqlxDatatypes.Scalar result) {
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) != 0) {
               result.type_ = this.type_;
               to_bitField0_ |= 1;
            }

            if ((from_bitField0_ & 2) != 0) {
               result.vSignedInt_ = this.vSignedInt_;
               to_bitField0_ |= 2;
            }

            if ((from_bitField0_ & 4) != 0) {
               result.vUnsignedInt_ = this.vUnsignedInt_;
               to_bitField0_ |= 4;
            }

            if ((from_bitField0_ & 8) != 0) {
               result.vOctets_ = this.vOctetsBuilder_ == null ? this.vOctets_ : (MysqlxDatatypes.Scalar.Octets)this.vOctetsBuilder_.build();
               to_bitField0_ |= 8;
            }

            if ((from_bitField0_ & 16) != 0) {
               result.vDouble_ = this.vDouble_;
               to_bitField0_ |= 16;
            }

            if ((from_bitField0_ & 32) != 0) {
               result.vFloat_ = this.vFloat_;
               to_bitField0_ |= 32;
            }

            if ((from_bitField0_ & 64) != 0) {
               result.vBool_ = this.vBool_;
               to_bitField0_ |= 64;
            }

            if ((from_bitField0_ & 128) != 0) {
               result.vString_ = this.vStringBuilder_ == null ? this.vString_ : (MysqlxDatatypes.Scalar.String)this.vStringBuilder_.build();
               to_bitField0_ |= 128;
            }

            result.bitField0_ = result.bitField0_ | to_bitField0_;
         }

         public MysqlxDatatypes.Scalar.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxDatatypes.Scalar) {
               return this.mergeFrom((MysqlxDatatypes.Scalar)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxDatatypes.Scalar.Builder mergeFrom(MysqlxDatatypes.Scalar other) {
            if (other == MysqlxDatatypes.Scalar.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasType()) {
                  this.setType(other.getType());
               }

               if (other.hasVSignedInt()) {
                  this.setVSignedInt(other.getVSignedInt());
               }

               if (other.hasVUnsignedInt()) {
                  this.setVUnsignedInt(other.getVUnsignedInt());
               }

               if (other.hasVOctets()) {
                  this.mergeVOctets(other.getVOctets());
               }

               if (other.hasVDouble()) {
                  this.setVDouble(other.getVDouble());
               }

               if (other.hasVFloat()) {
                  this.setVFloat(other.getVFloat());
               }

               if (other.hasVBool()) {
                  this.setVBool(other.getVBool());
               }

               if (other.hasVString()) {
                  this.mergeVString(other.getVString());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasType()) {
               return false;
            } else {
               return this.hasVOctets() && !this.getVOctets().isInitialized() ? false : !this.hasVString() || this.getVString().isInitialized();
            }
         }

         public MysqlxDatatypes.Scalar.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            if (extensionRegistry == null) {
               throw new NullPointerException();
            } else {
               try {
                  boolean done = false;

                  while (!done) {
                     int tag = input.readTag();
                     switch (tag) {
                        case 0:
                           done = true;
                           break;
                        case 8:
                           int tmpRaw = input.readEnum();
                           MysqlxDatatypes.Scalar.Type tmpValue = MysqlxDatatypes.Scalar.Type.forNumber(tmpRaw);
                           if (tmpValue == null) {
                              this.mergeUnknownVarintField(1, tmpRaw);
                           } else {
                              this.type_ = tmpRaw;
                              this.bitField0_ |= 1;
                           }
                           break;
                        case 16:
                           this.vSignedInt_ = input.readSInt64();
                           this.bitField0_ |= 2;
                           break;
                        case 24:
                           this.vUnsignedInt_ = input.readUInt64();
                           this.bitField0_ |= 4;
                           break;
                        case 42:
                           input.readMessage(this.getVOctetsFieldBuilder().getBuilder(), extensionRegistry);
                           this.bitField0_ |= 8;
                           break;
                        case 49:
                           this.vDouble_ = input.readDouble();
                           this.bitField0_ |= 16;
                           break;
                        case 61:
                           this.vFloat_ = input.readFloat();
                           this.bitField0_ |= 32;
                           break;
                        case 64:
                           this.vBool_ = input.readBool();
                           this.bitField0_ |= 64;
                           break;
                        case 74:
                           input.readMessage(this.getVStringFieldBuilder().getBuilder(), extensionRegistry);
                           this.bitField0_ |= 128;
                           break;
                        default:
                           if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                              done = true;
                           }
                     }
                  }
               } catch (InvalidProtocolBufferException var10) {
                  throw var10.unwrapIOException();
               } finally {
                  this.onChanged();
               }

               return this;
            }
         }

         @Override
         public boolean hasType() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public MysqlxDatatypes.Scalar.Type getType() {
            MysqlxDatatypes.Scalar.Type result = MysqlxDatatypes.Scalar.Type.forNumber(this.type_);
            return result == null ? MysqlxDatatypes.Scalar.Type.V_SINT : result;
         }

         public MysqlxDatatypes.Scalar.Builder setType(MysqlxDatatypes.Scalar.Type value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.type_ = value.getNumber();
               this.onChanged();
               return this;
            }
         }

         public MysqlxDatatypes.Scalar.Builder clearType() {
            this.bitField0_ &= -2;
            this.type_ = 1;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVSignedInt() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public long getVSignedInt() {
            return this.vSignedInt_;
         }

         public MysqlxDatatypes.Scalar.Builder setVSignedInt(long value) {
            this.vSignedInt_ = value;
            this.bitField0_ |= 2;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVSignedInt() {
            this.bitField0_ &= -3;
            this.vSignedInt_ = 0L;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVUnsignedInt() {
            return (this.bitField0_ & 4) != 0;
         }

         @Override
         public long getVUnsignedInt() {
            return this.vUnsignedInt_;
         }

         public MysqlxDatatypes.Scalar.Builder setVUnsignedInt(long value) {
            this.vUnsignedInt_ = value;
            this.bitField0_ |= 4;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVUnsignedInt() {
            this.bitField0_ &= -5;
            this.vUnsignedInt_ = 0L;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVOctets() {
            return (this.bitField0_ & 8) != 0;
         }

         @Override
         public MysqlxDatatypes.Scalar.Octets getVOctets() {
            if (this.vOctetsBuilder_ == null) {
               return this.vOctets_ == null ? MysqlxDatatypes.Scalar.Octets.getDefaultInstance() : this.vOctets_;
            } else {
               return (MysqlxDatatypes.Scalar.Octets)this.vOctetsBuilder_.getMessage();
            }
         }

         public MysqlxDatatypes.Scalar.Builder setVOctets(MysqlxDatatypes.Scalar.Octets value) {
            if (this.vOctetsBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.vOctets_ = value;
            } else {
               this.vOctetsBuilder_.setMessage(value);
            }

            this.bitField0_ |= 8;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder setVOctets(MysqlxDatatypes.Scalar.Octets.Builder builderForValue) {
            if (this.vOctetsBuilder_ == null) {
               this.vOctets_ = builderForValue.build();
            } else {
               this.vOctetsBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 8;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder mergeVOctets(MysqlxDatatypes.Scalar.Octets value) {
            if (this.vOctetsBuilder_ == null) {
               if ((this.bitField0_ & 8) != 0 && this.vOctets_ != null && this.vOctets_ != MysqlxDatatypes.Scalar.Octets.getDefaultInstance()) {
                  this.getVOctetsBuilder().mergeFrom(value);
               } else {
                  this.vOctets_ = value;
               }
            } else {
               this.vOctetsBuilder_.mergeFrom(value);
            }

            if (this.vOctets_ != null) {
               this.bitField0_ |= 8;
               this.onChanged();
            }

            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVOctets() {
            this.bitField0_ &= -9;
            this.vOctets_ = null;
            if (this.vOctetsBuilder_ != null) {
               this.vOctetsBuilder_.dispose();
               this.vOctetsBuilder_ = null;
            }

            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Octets.Builder getVOctetsBuilder() {
            this.bitField0_ |= 8;
            this.onChanged();
            return (MysqlxDatatypes.Scalar.Octets.Builder)this.getVOctetsFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.Scalar.OctetsOrBuilder getVOctetsOrBuilder() {
            if (this.vOctetsBuilder_ != null) {
               return (MysqlxDatatypes.Scalar.OctetsOrBuilder)this.vOctetsBuilder_.getMessageOrBuilder();
            } else {
               return this.vOctets_ == null ? MysqlxDatatypes.Scalar.Octets.getDefaultInstance() : this.vOctets_;
            }
         }

         private SingleFieldBuilder<MysqlxDatatypes.Scalar.Octets, MysqlxDatatypes.Scalar.Octets.Builder, MysqlxDatatypes.Scalar.OctetsOrBuilder> getVOctetsFieldBuilder() {
            if (this.vOctetsBuilder_ == null) {
               this.vOctetsBuilder_ = new SingleFieldBuilder(this.getVOctets(), this.getParentForChildren(), this.isClean());
               this.vOctets_ = null;
            }

            return this.vOctetsBuilder_;
         }

         @Override
         public boolean hasVDouble() {
            return (this.bitField0_ & 16) != 0;
         }

         @Override
         public double getVDouble() {
            return this.vDouble_;
         }

         public MysqlxDatatypes.Scalar.Builder setVDouble(double value) {
            this.vDouble_ = value;
            this.bitField0_ |= 16;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVDouble() {
            this.bitField0_ &= -17;
            this.vDouble_ = 0.0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVFloat() {
            return (this.bitField0_ & 32) != 0;
         }

         @Override
         public float getVFloat() {
            return this.vFloat_;
         }

         public MysqlxDatatypes.Scalar.Builder setVFloat(float value) {
            this.vFloat_ = value;
            this.bitField0_ |= 32;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVFloat() {
            this.bitField0_ &= -33;
            this.vFloat_ = 0.0F;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVBool() {
            return (this.bitField0_ & 64) != 0;
         }

         @Override
         public boolean getVBool() {
            return this.vBool_;
         }

         public MysqlxDatatypes.Scalar.Builder setVBool(boolean value) {
            this.vBool_ = value;
            this.bitField0_ |= 64;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVBool() {
            this.bitField0_ &= -65;
            this.vBool_ = false;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVString() {
            return (this.bitField0_ & 128) != 0;
         }

         @Override
         public MysqlxDatatypes.Scalar.String getVString() {
            if (this.vStringBuilder_ == null) {
               return this.vString_ == null ? MysqlxDatatypes.Scalar.String.getDefaultInstance() : this.vString_;
            } else {
               return (MysqlxDatatypes.Scalar.String)this.vStringBuilder_.getMessage();
            }
         }

         public MysqlxDatatypes.Scalar.Builder setVString(MysqlxDatatypes.Scalar.String value) {
            if (this.vStringBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.vString_ = value;
            } else {
               this.vStringBuilder_.setMessage(value);
            }

            this.bitField0_ |= 128;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder setVString(MysqlxDatatypes.Scalar.String.Builder builderForValue) {
            if (this.vStringBuilder_ == null) {
               this.vString_ = builderForValue.build();
            } else {
               this.vStringBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 128;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder mergeVString(MysqlxDatatypes.Scalar.String value) {
            if (this.vStringBuilder_ == null) {
               if ((this.bitField0_ & 128) != 0 && this.vString_ != null && this.vString_ != MysqlxDatatypes.Scalar.String.getDefaultInstance()) {
                  this.getVStringBuilder().mergeFrom(value);
               } else {
                  this.vString_ = value;
               }
            } else {
               this.vStringBuilder_.mergeFrom(value);
            }

            if (this.vString_ != null) {
               this.bitField0_ |= 128;
               this.onChanged();
            }

            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVString() {
            this.bitField0_ &= -129;
            this.vString_ = null;
            if (this.vStringBuilder_ != null) {
               this.vStringBuilder_.dispose();
               this.vStringBuilder_ = null;
            }

            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.String.Builder getVStringBuilder() {
            this.bitField0_ |= 128;
            this.onChanged();
            return (MysqlxDatatypes.Scalar.String.Builder)this.getVStringFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.Scalar.StringOrBuilder getVStringOrBuilder() {
            if (this.vStringBuilder_ != null) {
               return (MysqlxDatatypes.Scalar.StringOrBuilder)this.vStringBuilder_.getMessageOrBuilder();
            } else {
               return this.vString_ == null ? MysqlxDatatypes.Scalar.String.getDefaultInstance() : this.vString_;
            }
         }

         private SingleFieldBuilder<MysqlxDatatypes.Scalar.String, MysqlxDatatypes.Scalar.String.Builder, MysqlxDatatypes.Scalar.StringOrBuilder> getVStringFieldBuilder() {
            if (this.vStringBuilder_ == null) {
               this.vStringBuilder_ = new SingleFieldBuilder(this.getVString(), this.getParentForChildren(), this.isClean());
               this.vString_ = null;
            }

            return this.vStringBuilder_;
         }
      }

      public static final class Octets extends GeneratedMessage implements MysqlxDatatypes.Scalar.OctetsOrBuilder {
         private static final long serialVersionUID = 0L;
         private int bitField0_;
         public static final int VALUE_FIELD_NUMBER = 1;
         private ByteString value_ = ByteString.EMPTY;
         public static final int CONTENT_TYPE_FIELD_NUMBER = 2;
         private int contentType_ = 0;
         private byte memoizedIsInitialized = -1;
         private static final MysqlxDatatypes.Scalar.Octets DEFAULT_INSTANCE = new MysqlxDatatypes.Scalar.Octets();
         private static final Parser<MysqlxDatatypes.Scalar.Octets> PARSER = new AbstractParser<MysqlxDatatypes.Scalar.Octets>() {
            public MysqlxDatatypes.Scalar.Octets parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
               MysqlxDatatypes.Scalar.Octets.Builder builder = MysqlxDatatypes.Scalar.Octets.newBuilder();

               try {
                  builder.mergeFrom(input, extensionRegistry);
               } catch (InvalidProtocolBufferException var5) {
                  throw var5.setUnfinishedMessage(builder.buildPartial());
               } catch (UninitializedMessageException var6) {
                  throw var6.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
               } catch (IOException var7) {
                  throw new InvalidProtocolBufferException(var7).setUnfinishedMessage(builder.buildPartial());
               }

               return builder.buildPartial();
            }
         };

         private Octets(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
         }

         private Octets() {
            this.value_ = ByteString.EMPTY;
         }

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_Octets_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_Octets_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.Octets.class, MysqlxDatatypes.Scalar.Octets.Builder.class);
         }

         @Override
         public boolean hasValue() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public ByteString getValue() {
            return this.value_;
         }

         @Override
         public boolean hasContentType() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public int getContentType() {
            return this.contentType_;
         }

         public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
               return true;
            } else if (isInitialized == 0) {
               return false;
            } else if (!this.hasValue()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else {
               this.memoizedIsInitialized = 1;
               return true;
            }
         }

         public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
               output.writeBytes(1, this.value_);
            }

            if ((this.bitField0_ & 2) != 0) {
               output.writeUInt32(2, this.contentType_);
            }

            this.getUnknownFields().writeTo(output);
         }

         public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
               return size;
            } else {
               size = 0;
               if ((this.bitField0_ & 1) != 0) {
                  size += CodedOutputStream.computeBytesSize(1, this.value_);
               }

               if ((this.bitField0_ & 2) != 0) {
                  size += CodedOutputStream.computeUInt32Size(2, this.contentType_);
               }

               size += this.getUnknownFields().getSerializedSize();
               this.memoizedSize = size;
               return size;
            }
         }

         @Override
         public boolean equals(java.lang.Object obj) {
            if (obj == this) {
               return true;
            } else if (!(obj instanceof MysqlxDatatypes.Scalar.Octets)) {
               return super.equals(obj);
            } else {
               MysqlxDatatypes.Scalar.Octets other = (MysqlxDatatypes.Scalar.Octets)obj;
               if (this.hasValue() != other.hasValue()) {
                  return false;
               } else if (this.hasValue() && !this.getValue().equals(other.getValue())) {
                  return false;
               } else if (this.hasContentType() != other.hasContentType()) {
                  return false;
               } else {
                  return this.hasContentType() && this.getContentType() != other.getContentType()
                     ? false
                     : this.getUnknownFields().equals(other.getUnknownFields());
               }
            }
         }

         @Override
         public int hashCode() {
            if (this.memoizedHashCode != 0) {
               return this.memoizedHashCode;
            } else {
               int hash = 41;
               hash = 19 * hash + getDescriptor().hashCode();
               if (this.hasValue()) {
                  hash = 37 * hash + 1;
                  hash = 53 * hash + this.getValue().hashCode();
               }

               if (this.hasContentType()) {
                  hash = 37 * hash + 2;
                  hash = 53 * hash + this.getContentType();
               }

               hash = 29 * hash + this.getUnknownFields().hashCode();
               this.memoizedHashCode = hash;
               return hash;
            }
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)GeneratedMessage.parseWithIOException(PARSER, input);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.Octets parseDelimitedFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
         }

         public static MysqlxDatatypes.Scalar.Octets parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(CodedInputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)GeneratedMessage.parseWithIOException(PARSER, input);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
         }

         public MysqlxDatatypes.Scalar.Octets.Builder newBuilderForType() {
            return newBuilder();
         }

         public static MysqlxDatatypes.Scalar.Octets.Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
         }

         public static MysqlxDatatypes.Scalar.Octets.Builder newBuilder(MysqlxDatatypes.Scalar.Octets prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
         }

         public MysqlxDatatypes.Scalar.Octets.Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new MysqlxDatatypes.Scalar.Octets.Builder() : new MysqlxDatatypes.Scalar.Octets.Builder().mergeFrom(this);
         }

         protected MysqlxDatatypes.Scalar.Octets.Builder newBuilderForType(BuilderParent parent) {
            return new MysqlxDatatypes.Scalar.Octets.Builder(parent);
         }

         public static MysqlxDatatypes.Scalar.Octets getDefaultInstance() {
            return DEFAULT_INSTANCE;
         }

         public static Parser<MysqlxDatatypes.Scalar.Octets> parser() {
            return PARSER;
         }

         public Parser<MysqlxDatatypes.Scalar.Octets> getParserForType() {
            return PARSER;
         }

         public MysqlxDatatypes.Scalar.Octets getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
         }

         static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxDatatypes.Scalar.Octets.class.getName());
         }

         public static final class Builder
            extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Scalar.Octets.Builder>
            implements MysqlxDatatypes.Scalar.OctetsOrBuilder {
            private int bitField0_;
            private ByteString value_ = ByteString.EMPTY;
            private int contentType_;

            public static final Descriptor getDescriptor() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_Octets_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_Octets_fieldAccessorTable
                  .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.Octets.class, MysqlxDatatypes.Scalar.Octets.Builder.class);
            }

            private Builder() {
            }

            private Builder(BuilderParent parent) {
               super(parent);
            }

            public MysqlxDatatypes.Scalar.Octets.Builder clear() {
               super.clear();
               this.bitField0_ = 0;
               this.value_ = ByteString.EMPTY;
               this.contentType_ = 0;
               return this;
            }

            public Descriptor getDescriptorForType() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_Octets_descriptor;
            }

            public MysqlxDatatypes.Scalar.Octets getDefaultInstanceForType() {
               return MysqlxDatatypes.Scalar.Octets.getDefaultInstance();
            }

            public MysqlxDatatypes.Scalar.Octets build() {
               MysqlxDatatypes.Scalar.Octets result = this.buildPartial();
               if (!result.isInitialized()) {
                  throw newUninitializedMessageException(result);
               } else {
                  return result;
               }
            }

            public MysqlxDatatypes.Scalar.Octets buildPartial() {
               MysqlxDatatypes.Scalar.Octets result = new MysqlxDatatypes.Scalar.Octets(this);
               if (this.bitField0_ != 0) {
                  this.buildPartial0(result);
               }

               this.onBuilt();
               return result;
            }

            private void buildPartial0(MysqlxDatatypes.Scalar.Octets result) {
               int from_bitField0_ = this.bitField0_;
               int to_bitField0_ = 0;
               if ((from_bitField0_ & 1) != 0) {
                  result.value_ = this.value_;
                  to_bitField0_ |= 1;
               }

               if ((from_bitField0_ & 2) != 0) {
                  result.contentType_ = this.contentType_;
                  to_bitField0_ |= 2;
               }

               result.bitField0_ = result.bitField0_ | to_bitField0_;
            }

            public MysqlxDatatypes.Scalar.Octets.Builder mergeFrom(Message other) {
               if (other instanceof MysqlxDatatypes.Scalar.Octets) {
                  return this.mergeFrom((MysqlxDatatypes.Scalar.Octets)other);
               } else {
                  super.mergeFrom(other);
                  return this;
               }
            }

            public MysqlxDatatypes.Scalar.Octets.Builder mergeFrom(MysqlxDatatypes.Scalar.Octets other) {
               if (other == MysqlxDatatypes.Scalar.Octets.getDefaultInstance()) {
                  return this;
               } else {
                  if (other.hasValue()) {
                     this.setValue(other.getValue());
                  }

                  if (other.hasContentType()) {
                     this.setContentType(other.getContentType());
                  }

                  this.mergeUnknownFields(other.getUnknownFields());
                  this.onChanged();
                  return this;
               }
            }

            public final boolean isInitialized() {
               return this.hasValue();
            }

            public MysqlxDatatypes.Scalar.Octets.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
               if (extensionRegistry == null) {
                  throw new NullPointerException();
               } else {
                  try {
                     boolean done = false;

                     while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                           case 0:
                              done = true;
                              break;
                           case 10:
                              this.value_ = input.readBytes();
                              this.bitField0_ |= 1;
                              break;
                           case 16:
                              this.contentType_ = input.readUInt32();
                              this.bitField0_ |= 2;
                              break;
                           default:
                              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                                 done = true;
                              }
                        }
                     }
                  } catch (InvalidProtocolBufferException var8) {
                     throw var8.unwrapIOException();
                  } finally {
                     this.onChanged();
                  }

                  return this;
               }
            }

            @Override
            public boolean hasValue() {
               return (this.bitField0_ & 1) != 0;
            }

            @Override
            public ByteString getValue() {
               return this.value_;
            }

            public MysqlxDatatypes.Scalar.Octets.Builder setValue(ByteString value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.value_ = value;
                  this.bitField0_ |= 1;
                  this.onChanged();
                  return this;
               }
            }

            public MysqlxDatatypes.Scalar.Octets.Builder clearValue() {
               this.bitField0_ &= -2;
               this.value_ = MysqlxDatatypes.Scalar.Octets.getDefaultInstance().getValue();
               this.onChanged();
               return this;
            }

            @Override
            public boolean hasContentType() {
               return (this.bitField0_ & 2) != 0;
            }

            @Override
            public int getContentType() {
               return this.contentType_;
            }

            public MysqlxDatatypes.Scalar.Octets.Builder setContentType(int value) {
               this.contentType_ = value;
               this.bitField0_ |= 2;
               this.onChanged();
               return this;
            }

            public MysqlxDatatypes.Scalar.Octets.Builder clearContentType() {
               this.bitField0_ &= -3;
               this.contentType_ = 0;
               this.onChanged();
               return this;
            }
         }
      }

      public interface OctetsOrBuilder extends MessageOrBuilder {
         boolean hasValue();

         ByteString getValue();

         boolean hasContentType();

         int getContentType();
      }

      public static final class String extends GeneratedMessage implements MysqlxDatatypes.Scalar.StringOrBuilder {
         private static final long serialVersionUID = 0L;
         private int bitField0_;
         public static final int VALUE_FIELD_NUMBER = 1;
         private ByteString value_ = ByteString.EMPTY;
         public static final int COLLATION_FIELD_NUMBER = 2;
         private long collation_ = 0L;
         private byte memoizedIsInitialized = -1;
         private static final MysqlxDatatypes.Scalar.String DEFAULT_INSTANCE = new MysqlxDatatypes.Scalar.String();
         private static final Parser<MysqlxDatatypes.Scalar.String> PARSER = new AbstractParser<MysqlxDatatypes.Scalar.String>() {
            public MysqlxDatatypes.Scalar.String parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
               MysqlxDatatypes.Scalar.String.Builder builder = MysqlxDatatypes.Scalar.String.newBuilder();

               try {
                  builder.mergeFrom(input, extensionRegistry);
               } catch (InvalidProtocolBufferException var5) {
                  throw var5.setUnfinishedMessage(builder.buildPartial());
               } catch (UninitializedMessageException var6) {
                  throw var6.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
               } catch (IOException var7) {
                  throw new InvalidProtocolBufferException(var7).setUnfinishedMessage(builder.buildPartial());
               }

               return builder.buildPartial();
            }
         };

         private String(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
         }

         private String() {
            this.value_ = ByteString.EMPTY;
         }

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_String_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_String_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.String.class, MysqlxDatatypes.Scalar.String.Builder.class);
         }

         @Override
         public boolean hasValue() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public ByteString getValue() {
            return this.value_;
         }

         @Override
         public boolean hasCollation() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public long getCollation() {
            return this.collation_;
         }

         public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
               return true;
            } else if (isInitialized == 0) {
               return false;
            } else if (!this.hasValue()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else {
               this.memoizedIsInitialized = 1;
               return true;
            }
         }

         public void writeTo(CodedOutputStream output) throws IOException {
            if ((this.bitField0_ & 1) != 0) {
               output.writeBytes(1, this.value_);
            }

            if ((this.bitField0_ & 2) != 0) {
               output.writeUInt64(2, this.collation_);
            }

            this.getUnknownFields().writeTo(output);
         }

         public int getSerializedSize() {
            int size = this.memoizedSize;
            if (size != -1) {
               return size;
            } else {
               size = 0;
               if ((this.bitField0_ & 1) != 0) {
                  size += CodedOutputStream.computeBytesSize(1, this.value_);
               }

               if ((this.bitField0_ & 2) != 0) {
                  size += CodedOutputStream.computeUInt64Size(2, this.collation_);
               }

               size += this.getUnknownFields().getSerializedSize();
               this.memoizedSize = size;
               return size;
            }
         }

         @Override
         public boolean equals(java.lang.Object obj) {
            if (obj == this) {
               return true;
            } else if (!(obj instanceof MysqlxDatatypes.Scalar.String)) {
               return super.equals(obj);
            } else {
               MysqlxDatatypes.Scalar.String other = (MysqlxDatatypes.Scalar.String)obj;
               if (this.hasValue() != other.hasValue()) {
                  return false;
               } else if (this.hasValue() && !this.getValue().equals(other.getValue())) {
                  return false;
               } else if (this.hasCollation() != other.hasCollation()) {
                  return false;
               } else {
                  return this.hasCollation() && this.getCollation() != other.getCollation() ? false : this.getUnknownFields().equals(other.getUnknownFields());
               }
            }
         }

         @Override
         public int hashCode() {
            if (this.memoizedHashCode != 0) {
               return this.memoizedHashCode;
            } else {
               int hash = 41;
               hash = 19 * hash + getDescriptor().hashCode();
               if (this.hasValue()) {
                  hash = 37 * hash + 1;
                  hash = 53 * hash + this.getValue().hashCode();
               }

               if (this.hasCollation()) {
                  hash = 37 * hash + 2;
                  hash = 53 * hash + Internal.hashLong(this.getCollation());
               }

               hash = 29 * hash + this.getUnknownFields().hashCode();
               this.memoizedHashCode = hash;
               return hash;
            }
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.String)GeneratedMessage.parseWithIOException(PARSER, input);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.String)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.String parseDelimitedFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.String)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
         }

         public static MysqlxDatatypes.Scalar.String parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.String)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(CodedInputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.String)GeneratedMessage.parseWithIOException(PARSER, input);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.String)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
         }

         public MysqlxDatatypes.Scalar.String.Builder newBuilderForType() {
            return newBuilder();
         }

         public static MysqlxDatatypes.Scalar.String.Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
         }

         public static MysqlxDatatypes.Scalar.String.Builder newBuilder(MysqlxDatatypes.Scalar.String prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
         }

         public MysqlxDatatypes.Scalar.String.Builder toBuilder() {
            return this == DEFAULT_INSTANCE ? new MysqlxDatatypes.Scalar.String.Builder() : new MysqlxDatatypes.Scalar.String.Builder().mergeFrom(this);
         }

         protected MysqlxDatatypes.Scalar.String.Builder newBuilderForType(BuilderParent parent) {
            return new MysqlxDatatypes.Scalar.String.Builder(parent);
         }

         public static MysqlxDatatypes.Scalar.String getDefaultInstance() {
            return DEFAULT_INSTANCE;
         }

         public static Parser<MysqlxDatatypes.Scalar.String> parser() {
            return PARSER;
         }

         public Parser<MysqlxDatatypes.Scalar.String> getParserForType() {
            return PARSER;
         }

         public MysqlxDatatypes.Scalar.String getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
         }

         static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxDatatypes.Scalar.String.class.getName());
         }

         public static final class Builder
            extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Scalar.String.Builder>
            implements MysqlxDatatypes.Scalar.StringOrBuilder {
            private int bitField0_;
            private ByteString value_ = ByteString.EMPTY;
            private long collation_;

            public static final Descriptor getDescriptor() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_String_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_String_fieldAccessorTable
                  .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.String.class, MysqlxDatatypes.Scalar.String.Builder.class);
            }

            private Builder() {
            }

            private Builder(BuilderParent parent) {
               super(parent);
            }

            public MysqlxDatatypes.Scalar.String.Builder clear() {
               super.clear();
               this.bitField0_ = 0;
               this.value_ = ByteString.EMPTY;
               this.collation_ = 0L;
               return this;
            }

            public Descriptor getDescriptorForType() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_String_descriptor;
            }

            public MysqlxDatatypes.Scalar.String getDefaultInstanceForType() {
               return MysqlxDatatypes.Scalar.String.getDefaultInstance();
            }

            public MysqlxDatatypes.Scalar.String build() {
               MysqlxDatatypes.Scalar.String result = this.buildPartial();
               if (!result.isInitialized()) {
                  throw newUninitializedMessageException(result);
               } else {
                  return result;
               }
            }

            public MysqlxDatatypes.Scalar.String buildPartial() {
               MysqlxDatatypes.Scalar.String result = new MysqlxDatatypes.Scalar.String(this);
               if (this.bitField0_ != 0) {
                  this.buildPartial0(result);
               }

               this.onBuilt();
               return result;
            }

            private void buildPartial0(MysqlxDatatypes.Scalar.String result) {
               int from_bitField0_ = this.bitField0_;
               int to_bitField0_ = 0;
               if ((from_bitField0_ & 1) != 0) {
                  result.value_ = this.value_;
                  to_bitField0_ |= 1;
               }

               if ((from_bitField0_ & 2) != 0) {
                  result.collation_ = this.collation_;
                  to_bitField0_ |= 2;
               }

               result.bitField0_ = result.bitField0_ | to_bitField0_;
            }

            public MysqlxDatatypes.Scalar.String.Builder mergeFrom(Message other) {
               if (other instanceof MysqlxDatatypes.Scalar.String) {
                  return this.mergeFrom((MysqlxDatatypes.Scalar.String)other);
               } else {
                  super.mergeFrom(other);
                  return this;
               }
            }

            public MysqlxDatatypes.Scalar.String.Builder mergeFrom(MysqlxDatatypes.Scalar.String other) {
               if (other == MysqlxDatatypes.Scalar.String.getDefaultInstance()) {
                  return this;
               } else {
                  if (other.hasValue()) {
                     this.setValue(other.getValue());
                  }

                  if (other.hasCollation()) {
                     this.setCollation(other.getCollation());
                  }

                  this.mergeUnknownFields(other.getUnknownFields());
                  this.onChanged();
                  return this;
               }
            }

            public final boolean isInitialized() {
               return this.hasValue();
            }

            public MysqlxDatatypes.Scalar.String.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
               if (extensionRegistry == null) {
                  throw new NullPointerException();
               } else {
                  try {
                     boolean done = false;

                     while (!done) {
                        int tag = input.readTag();
                        switch (tag) {
                           case 0:
                              done = true;
                              break;
                           case 10:
                              this.value_ = input.readBytes();
                              this.bitField0_ |= 1;
                              break;
                           case 16:
                              this.collation_ = input.readUInt64();
                              this.bitField0_ |= 2;
                              break;
                           default:
                              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                                 done = true;
                              }
                        }
                     }
                  } catch (InvalidProtocolBufferException var8) {
                     throw var8.unwrapIOException();
                  } finally {
                     this.onChanged();
                  }

                  return this;
               }
            }

            @Override
            public boolean hasValue() {
               return (this.bitField0_ & 1) != 0;
            }

            @Override
            public ByteString getValue() {
               return this.value_;
            }

            public MysqlxDatatypes.Scalar.String.Builder setValue(ByteString value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.value_ = value;
                  this.bitField0_ |= 1;
                  this.onChanged();
                  return this;
               }
            }

            public MysqlxDatatypes.Scalar.String.Builder clearValue() {
               this.bitField0_ &= -2;
               this.value_ = MysqlxDatatypes.Scalar.String.getDefaultInstance().getValue();
               this.onChanged();
               return this;
            }

            @Override
            public boolean hasCollation() {
               return (this.bitField0_ & 2) != 0;
            }

            @Override
            public long getCollation() {
               return this.collation_;
            }

            public MysqlxDatatypes.Scalar.String.Builder setCollation(long value) {
               this.collation_ = value;
               this.bitField0_ |= 2;
               this.onChanged();
               return this;
            }

            public MysqlxDatatypes.Scalar.String.Builder clearCollation() {
               this.bitField0_ &= -3;
               this.collation_ = 0L;
               this.onChanged();
               return this;
            }
         }
      }

      public interface StringOrBuilder extends MessageOrBuilder {
         boolean hasValue();

         ByteString getValue();

         boolean hasCollation();

         long getCollation();
      }

      public static enum Type implements ProtocolMessageEnum {
         V_SINT(1),
         V_UINT(2),
         V_NULL(3),
         V_OCTETS(4),
         V_DOUBLE(5),
         V_FLOAT(6),
         V_BOOL(7),
         V_STRING(8);

         public static final int V_SINT_VALUE = 1;
         public static final int V_UINT_VALUE = 2;
         public static final int V_NULL_VALUE = 3;
         public static final int V_OCTETS_VALUE = 4;
         public static final int V_DOUBLE_VALUE = 5;
         public static final int V_FLOAT_VALUE = 6;
         public static final int V_BOOL_VALUE = 7;
         public static final int V_STRING_VALUE = 8;
         private static final EnumLiteMap<MysqlxDatatypes.Scalar.Type> internalValueMap = new EnumLiteMap<MysqlxDatatypes.Scalar.Type>() {
            public MysqlxDatatypes.Scalar.Type findValueByNumber(int number) {
               return MysqlxDatatypes.Scalar.Type.forNumber(number);
            }
         };
         private static final MysqlxDatatypes.Scalar.Type[] VALUES = values();
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         @Deprecated
         public static MysqlxDatatypes.Scalar.Type valueOf(int value) {
            return forNumber(value);
         }

         public static MysqlxDatatypes.Scalar.Type forNumber(int value) {
            switch (value) {
               case 1:
                  return V_SINT;
               case 2:
                  return V_UINT;
               case 3:
                  return V_NULL;
               case 4:
                  return V_OCTETS;
               case 5:
                  return V_DOUBLE;
               case 6:
                  return V_FLOAT;
               case 7:
                  return V_BOOL;
               case 8:
                  return V_STRING;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<MysqlxDatatypes.Scalar.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.ordinal());
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxDatatypes.Scalar.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxDatatypes.Scalar.Type valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private Type(int value) {
            this.value = value;
         }

         static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxDatatypes.Scalar.Type.class.getName());
         }
      }
   }

   public interface ScalarOrBuilder extends MessageOrBuilder {
      boolean hasType();

      MysqlxDatatypes.Scalar.Type getType();

      boolean hasVSignedInt();

      long getVSignedInt();

      boolean hasVUnsignedInt();

      long getVUnsignedInt();

      boolean hasVOctets();

      MysqlxDatatypes.Scalar.Octets getVOctets();

      MysqlxDatatypes.Scalar.OctetsOrBuilder getVOctetsOrBuilder();

      boolean hasVDouble();

      double getVDouble();

      boolean hasVFloat();

      float getVFloat();

      boolean hasVBool();

      boolean getVBool();

      boolean hasVString();

      MysqlxDatatypes.Scalar.String getVString();

      MysqlxDatatypes.Scalar.StringOrBuilder getVStringOrBuilder();
   }
}
