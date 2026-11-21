package org.sethomegui.SetHomeGUI.libs.mysql.cj.x.protobuf;

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

public final class MysqlxPrepare {
   private static final Descriptor internal_static_Mysqlx_Prepare_Prepare_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static final FieldAccessorTable internal_static_Mysqlx_Prepare_Prepare_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Prepare_Prepare_descriptor, new String[]{"StmtId", "Stmt"}
   );
   private static final Descriptor internal_static_Mysqlx_Prepare_Prepare_OneOfMessage_descriptor = (Descriptor)internal_static_Mysqlx_Prepare_Prepare_descriptor.getNestedTypes()
      .get(0);
   private static final FieldAccessorTable internal_static_Mysqlx_Prepare_Prepare_OneOfMessage_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Prepare_Prepare_OneOfMessage_descriptor, new String[]{"Type", "Find", "Insert", "Update", "Delete", "StmtExecute"}
   );
   private static final Descriptor internal_static_Mysqlx_Prepare_Execute_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static final FieldAccessorTable internal_static_Mysqlx_Prepare_Execute_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Prepare_Execute_descriptor, new String[]{"StmtId", "Args", "CompactMetadata"}
   );
   private static final Descriptor internal_static_Mysqlx_Prepare_Deallocate_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(2);
   private static final FieldAccessorTable internal_static_Mysqlx_Prepare_Deallocate_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Prepare_Deallocate_descriptor, new String[]{"StmtId"}
   );
   private static FileDescriptor descriptor;

   private MysqlxPrepare() {
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
      RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxPrepare.class.getName());
      String[] descriptorData = new String[]{
         "\n\u0014mysqlx_prepare.proto\u0012\u000eMysqlx.Prepare\u001a\fmysqlx.proto\u001a\u0010mysqlx_sql.proto\u001a\u0011mysqlx_crud.proto\u001a\u0016mysqlx_datatypes.proto\"\u009d\u0003\n\u0007Prepare\u0012\u000f\n\u0007stmt_id\u0018\u0001 \u0002(\r\u00122\n\u0004stmt\u0018\u0002 \u0002(\u000b2$.Mysqlx.Prepare.Prepare.OneOfMessage\u001aÆ\u0002\n\fOneOfMessage\u00127\n\u0004type\u0018\u0001 \u0002(\u000e2).Mysqlx.Prepare.Prepare.OneOfMessage.Type\u0012\u001f\n\u0004find\u0018\u0002 \u0001(\u000b2\u0011.Mysqlx.Crud.Find\u0012#\n\u0006insert\u0018\u0003 \u0001(\u000b2\u0013.Mysqlx.Crud.Insert\u0012#\n\u0006update\u0018\u0004 \u0001(\u000b2\u0013.Mysqlx.Crud.Update\u0012#\n\u0006delete\u0018\u0005 \u0001(\u000b2\u0013.Mysqlx.Crud.Delete\u0012-\n\fstmt_execute\u0018\u0006 \u0001(\u000b2\u0017.Mysqlx.Sql.StmtExecute\">\n\u0004Type\u0012\b\n\u0004FIND\u0010\u0000\u0012\n\n\u0006INSERT\u0010\u0001\u0012\n\n\u0006UPDATE\u0010\u0002\u0012\n\n\u0006DELETE\u0010\u0004\u0012\b\n\u0004STMT\u0010\u0005:\u0004\u0088ê0(\"f\n\u0007Execute\u0012\u000f\n\u0007stmt_id\u0018\u0001 \u0002(\r\u0012#\n\u0004args\u0018\u0002 \u0003(\u000b2\u0015.Mysqlx.Datatypes.Any\u0012\u001f\n\u0010compact_metadata\u0018\u0003 \u0001(\b:\u0005false:\u0004\u0088ê0)\"#\n\nDeallocate\u0012\u000f\n\u0007stmt_id\u0018\u0001 \u0002(\r:\u0004\u0088ê0*B\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      descriptor = FileDescriptor.internalBuildGeneratedFileFrom(
         descriptorData, new FileDescriptor[]{Mysqlx.getDescriptor(), MysqlxSql.getDescriptor(), MysqlxCrud.getDescriptor(), MysqlxDatatypes.getDescriptor()}
      );
      descriptor.resolveAllFeaturesImmutable();
      Mysqlx.getDescriptor();
      MysqlxSql.getDescriptor();
      MysqlxCrud.getDescriptor();
      MysqlxDatatypes.getDescriptor();
      ExtensionRegistry registry = ExtensionRegistry.newInstance();
      registry.add(Mysqlx.clientMessageId);
      FileDescriptor.internalUpdateFileDescriptor(descriptor, registry);
   }

   public static final class Deallocate extends GeneratedMessage implements MysqlxPrepare.DeallocateOrBuilder {
      private static final long serialVersionUID = 0L;
      private int bitField0_;
      public static final int STMT_ID_FIELD_NUMBER = 1;
      private int stmtId_ = 0;
      private byte memoizedIsInitialized = -1;
      private static final MysqlxPrepare.Deallocate DEFAULT_INSTANCE = new MysqlxPrepare.Deallocate();
      private static final Parser<MysqlxPrepare.Deallocate> PARSER = new AbstractParser<MysqlxPrepare.Deallocate>() {
         public MysqlxPrepare.Deallocate parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            MysqlxPrepare.Deallocate.Builder builder = MysqlxPrepare.Deallocate.newBuilder();

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

      private Deallocate(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private Deallocate() {
      }

      public static final Descriptor getDescriptor() {
         return MysqlxPrepare.internal_static_Mysqlx_Prepare_Deallocate_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxPrepare.internal_static_Mysqlx_Prepare_Deallocate_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxPrepare.Deallocate.class, MysqlxPrepare.Deallocate.Builder.class);
      }

      @Override
      public boolean hasStmtId() {
         return (this.bitField0_ & 1) != 0;
      }

      @Override
      public int getStmtId() {
         return this.stmtId_;
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasStmtId()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         if ((this.bitField0_ & 1) != 0) {
            output.writeUInt32(1, this.stmtId_);
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
               size += CodedOutputStream.computeUInt32Size(1, this.stmtId_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSize = size;
            return size;
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof MysqlxPrepare.Deallocate)) {
            return super.equals(obj);
         } else {
            MysqlxPrepare.Deallocate other = (MysqlxPrepare.Deallocate)obj;
            if (this.hasStmtId() != other.hasStmtId()) {
               return false;
            } else {
               return this.hasStmtId() && this.getStmtId() != other.getStmtId() ? false : this.getUnknownFields().equals(other.getUnknownFields());
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
            if (this.hasStmtId()) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.getStmtId();
            }

            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static MysqlxPrepare.Deallocate parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Deallocate)PARSER.parseFrom(data);
      }

      public static MysqlxPrepare.Deallocate parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Deallocate)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxPrepare.Deallocate parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Deallocate)PARSER.parseFrom(data);
      }

      public static MysqlxPrepare.Deallocate parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Deallocate)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxPrepare.Deallocate parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Deallocate)PARSER.parseFrom(data);
      }

      public static MysqlxPrepare.Deallocate parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Deallocate)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxPrepare.Deallocate parseFrom(InputStream input) throws IOException {
         return (MysqlxPrepare.Deallocate)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxPrepare.Deallocate parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxPrepare.Deallocate)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxPrepare.Deallocate parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxPrepare.Deallocate)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static MysqlxPrepare.Deallocate parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxPrepare.Deallocate)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxPrepare.Deallocate parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxPrepare.Deallocate)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxPrepare.Deallocate parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxPrepare.Deallocate)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public MysqlxPrepare.Deallocate.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxPrepare.Deallocate.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static MysqlxPrepare.Deallocate.Builder newBuilder(MysqlxPrepare.Deallocate prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public MysqlxPrepare.Deallocate.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new MysqlxPrepare.Deallocate.Builder() : new MysqlxPrepare.Deallocate.Builder().mergeFrom(this);
      }

      protected MysqlxPrepare.Deallocate.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxPrepare.Deallocate.Builder(parent);
      }

      public static MysqlxPrepare.Deallocate getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<MysqlxPrepare.Deallocate> parser() {
         return PARSER;
      }

      public Parser<MysqlxPrepare.Deallocate> getParserForType() {
         return PARSER;
      }

      public MysqlxPrepare.Deallocate getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxPrepare.Deallocate.class.getName());
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxPrepare.Deallocate.Builder>
         implements MysqlxPrepare.DeallocateOrBuilder {
         private int bitField0_;
         private int stmtId_;

         public static final Descriptor getDescriptor() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Deallocate_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Deallocate_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxPrepare.Deallocate.class, MysqlxPrepare.Deallocate.Builder.class);
         }

         private Builder() {
         }

         private Builder(BuilderParent parent) {
            super(parent);
         }

         public MysqlxPrepare.Deallocate.Builder clear() {
            super.clear();
            this.bitField0_ = 0;
            this.stmtId_ = 0;
            return this;
         }

         public Descriptor getDescriptorForType() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Deallocate_descriptor;
         }

         public MysqlxPrepare.Deallocate getDefaultInstanceForType() {
            return MysqlxPrepare.Deallocate.getDefaultInstance();
         }

         public MysqlxPrepare.Deallocate build() {
            MysqlxPrepare.Deallocate result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxPrepare.Deallocate buildPartial() {
            MysqlxPrepare.Deallocate result = new MysqlxPrepare.Deallocate(this);
            if (this.bitField0_ != 0) {
               this.buildPartial0(result);
            }

            this.onBuilt();
            return result;
         }

         private void buildPartial0(MysqlxPrepare.Deallocate result) {
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) != 0) {
               result.stmtId_ = this.stmtId_;
               to_bitField0_ |= 1;
            }

            result.bitField0_ = result.bitField0_ | to_bitField0_;
         }

         public MysqlxPrepare.Deallocate.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxPrepare.Deallocate) {
               return this.mergeFrom((MysqlxPrepare.Deallocate)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxPrepare.Deallocate.Builder mergeFrom(MysqlxPrepare.Deallocate other) {
            if (other == MysqlxPrepare.Deallocate.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasStmtId()) {
                  this.setStmtId(other.getStmtId());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            return this.hasStmtId();
         }

         public MysqlxPrepare.Deallocate.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
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
                           this.stmtId_ = input.readUInt32();
                           this.bitField0_ |= 1;
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
         public boolean hasStmtId() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public int getStmtId() {
            return this.stmtId_;
         }

         public MysqlxPrepare.Deallocate.Builder setStmtId(int value) {
            this.stmtId_ = value;
            this.bitField0_ |= 1;
            this.onChanged();
            return this;
         }

         public MysqlxPrepare.Deallocate.Builder clearStmtId() {
            this.bitField0_ &= -2;
            this.stmtId_ = 0;
            this.onChanged();
            return this;
         }
      }
   }

   public interface DeallocateOrBuilder extends MessageOrBuilder {
      boolean hasStmtId();

      int getStmtId();
   }

   public static final class Execute extends GeneratedMessage implements MysqlxPrepare.ExecuteOrBuilder {
      private static final long serialVersionUID = 0L;
      private int bitField0_;
      public static final int STMT_ID_FIELD_NUMBER = 1;
      private int stmtId_ = 0;
      public static final int ARGS_FIELD_NUMBER = 2;
      private List<MysqlxDatatypes.Any> args_;
      public static final int COMPACT_METADATA_FIELD_NUMBER = 3;
      private boolean compactMetadata_ = false;
      private byte memoizedIsInitialized = -1;
      private static final MysqlxPrepare.Execute DEFAULT_INSTANCE = new MysqlxPrepare.Execute();
      private static final Parser<MysqlxPrepare.Execute> PARSER = new AbstractParser<MysqlxPrepare.Execute>() {
         public MysqlxPrepare.Execute parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            MysqlxPrepare.Execute.Builder builder = MysqlxPrepare.Execute.newBuilder();

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

      private Execute(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private Execute() {
         this.args_ = Collections.emptyList();
      }

      public static final Descriptor getDescriptor() {
         return MysqlxPrepare.internal_static_Mysqlx_Prepare_Execute_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxPrepare.internal_static_Mysqlx_Prepare_Execute_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxPrepare.Execute.class, MysqlxPrepare.Execute.Builder.class);
      }

      @Override
      public boolean hasStmtId() {
         // $VF: Couldn't be decompiled
         // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
         // java.lang.OutOfMemoryError: Java heap space
         //   at java.base/java.util.HashMap.resize(HashMap.java:710)
         //   at java.base/java.util.HashMap.putVal(HashMap.java:635)
         //   at java.base/java.util.HashMap.put(HashMap.java:618)
         //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.addDestination(FlattenStatementsHelper.java:64)
         //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.addDestination(FlattenStatementsHelper.java:58)
         //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:441)
         //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:474)
         //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.flattenStatement(FlattenStatementsHelper.java:474)
         //   at org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.buildDirectGraph(FlattenStatementsHelper.java:43)
         //   at org.jetbrains.java.decompiler.modules.decompiler.sforms.SFormsConstructor.splitVariables(SFormsConstructor.java:72)
         //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:52)
         //   at org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor.simplifyStackVars(StackVarsProcessor.java:40)
         //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:231)
         //
         // Bytecode:
         // 0: aload 0
         // 1: getfield org/sethomegui/SetHomeGUI/libs/mysql/cj/x/protobuf/MysqlxPrepare$Execute.bitField0_ I
         // 4: bipush 1
         // 5: iand
         // 6: ifeq d
         // 9: bipush 1
         // a: goto e
         // d: bipush 0
         // e: ireturn
      }

      @Override
      public int getStmtId() {
         return this.stmtId_;
      }

      @Override
      public List<MysqlxDatatypes.Any> getArgsList() {
         return this.args_;
      }

      @Override
      public List<? extends MysqlxDatatypes.AnyOrBuilder> getArgsOrBuilderList() {
         return this.args_;
      }

      @Override
      public int getArgsCount() {
         return this.args_.size();
      }

      @Override
      public MysqlxDatatypes.Any getArgs(int index) {
         return this.args_.get(index);
      }

      @Override
      public MysqlxDatatypes.AnyOrBuilder getArgsOrBuilder(int index) {
         return this.args_.get(index);
      }

      @Override
      public boolean hasCompactMetadata() {
         return (this.bitField0_ & 2) != 0;
      }

      @Override
      public boolean getCompactMetadata() {
         return this.compactMetadata_;
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasStmtId()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else {
            for (int i = 0; i < this.getArgsCount(); i++) {
               if (!this.getArgs(i).isInitialized()) {
                  this.memoizedIsInitialized = 0;
                  return false;
               }
            }

            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         if ((this.bitField0_ & 1) != 0) {
            output.writeUInt32(1, this.stmtId_);
         }

         for (int i = 0; i < this.args_.size(); i++) {
            output.writeMessage(2, (MessageLite)this.args_.get(i));
         }

         if ((this.bitField0_ & 2) != 0) {
            output.writeBool(3, this.compactMetadata_);
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
               size += CodedOutputStream.computeUInt32Size(1, this.stmtId_);
            }

            for (int i = 0; i < this.args_.size(); i++) {
               size += CodedOutputStream.computeMessageSize(2, (MessageLite)this.args_.get(i));
            }

            if ((this.bitField0_ & 2) != 0) {
               size += CodedOutputStream.computeBoolSize(3, this.compactMetadata_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSize = size;
            return size;
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof MysqlxPrepare.Execute)) {
            return super.equals(obj);
         } else {
            MysqlxPrepare.Execute other = (MysqlxPrepare.Execute)obj;
            if (this.hasStmtId() != other.hasStmtId()) {
               return false;
            } else if (this.hasStmtId() && this.getStmtId() != other.getStmtId()) {
               return false;
            } else if (!this.getArgsList().equals(other.getArgsList())) {
               return false;
            } else if (this.hasCompactMetadata() != other.hasCompactMetadata()) {
               return false;
            } else {
               return this.hasCompactMetadata() && this.getCompactMetadata() != other.getCompactMetadata()
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
            if (this.hasStmtId()) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.getStmtId();
            }

            if (this.getArgsCount() > 0) {
               hash = 37 * hash + 2;
               hash = 53 * hash + this.getArgsList().hashCode();
            }

            if (this.hasCompactMetadata()) {
               hash = 37 * hash + 3;
               hash = 53 * hash + Internal.hashBoolean(this.getCompactMetadata());
            }

            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static MysqlxPrepare.Execute parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Execute)PARSER.parseFrom(data);
      }

      public static MysqlxPrepare.Execute parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Execute)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxPrepare.Execute parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Execute)PARSER.parseFrom(data);
      }

      public static MysqlxPrepare.Execute parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Execute)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxPrepare.Execute parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Execute)PARSER.parseFrom(data);
      }

      public static MysqlxPrepare.Execute parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Execute)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxPrepare.Execute parseFrom(InputStream input) throws IOException {
         return (MysqlxPrepare.Execute)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxPrepare.Execute parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxPrepare.Execute)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxPrepare.Execute parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxPrepare.Execute)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static MysqlxPrepare.Execute parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxPrepare.Execute)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxPrepare.Execute parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxPrepare.Execute)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxPrepare.Execute parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxPrepare.Execute)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public MysqlxPrepare.Execute.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxPrepare.Execute.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static MysqlxPrepare.Execute.Builder newBuilder(MysqlxPrepare.Execute prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public MysqlxPrepare.Execute.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new MysqlxPrepare.Execute.Builder() : new MysqlxPrepare.Execute.Builder().mergeFrom(this);
      }

      protected MysqlxPrepare.Execute.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxPrepare.Execute.Builder(parent);
      }

      public static MysqlxPrepare.Execute getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<MysqlxPrepare.Execute> parser() {
         return PARSER;
      }

      public Parser<MysqlxPrepare.Execute> getParserForType() {
         return PARSER;
      }

      public MysqlxPrepare.Execute getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxPrepare.Execute.class.getName());
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxPrepare.Execute.Builder>
         implements MysqlxPrepare.ExecuteOrBuilder {
         private int bitField0_;
         private int stmtId_;
         private List<MysqlxDatatypes.Any> args_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> argsBuilder_;
         private boolean compactMetadata_;

         public static final Descriptor getDescriptor() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Execute_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Execute_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxPrepare.Execute.class, MysqlxPrepare.Execute.Builder.class);
         }

         private Builder() {
         }

         private Builder(BuilderParent parent) {
            super(parent);
         }

         public MysqlxPrepare.Execute.Builder clear() {
            super.clear();
            this.bitField0_ = 0;
            this.stmtId_ = 0;
            if (this.argsBuilder_ == null) {
               this.args_ = Collections.emptyList();
            } else {
               this.args_ = null;
               this.argsBuilder_.clear();
            }

            this.bitField0_ &= -3;
            this.compactMetadata_ = false;
            return this;
         }

         public Descriptor getDescriptorForType() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Execute_descriptor;
         }

         public MysqlxPrepare.Execute getDefaultInstanceForType() {
            return MysqlxPrepare.Execute.getDefaultInstance();
         }

         public MysqlxPrepare.Execute build() {
            MysqlxPrepare.Execute result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxPrepare.Execute buildPartial() {
            MysqlxPrepare.Execute result = new MysqlxPrepare.Execute(this);
            this.buildPartialRepeatedFields(result);
            if (this.bitField0_ != 0) {
               this.buildPartial0(result);
            }

            this.onBuilt();
            return result;
         }

         private void buildPartialRepeatedFields(MysqlxPrepare.Execute result) {
            if (this.argsBuilder_ == null) {
               if ((this.bitField0_ & 2) != 0) {
                  this.args_ = Collections.unmodifiableList(this.args_);
                  this.bitField0_ &= -3;
               }

               result.args_ = this.args_;
            } else {
               result.args_ = this.argsBuilder_.build();
            }
         }

         private void buildPartial0(MysqlxPrepare.Execute result) {
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) != 0) {
               result.stmtId_ = this.stmtId_;
               to_bitField0_ |= 1;
            }

            if ((from_bitField0_ & 4) != 0) {
               result.compactMetadata_ = this.compactMetadata_;
               to_bitField0_ |= 2;
            }

            result.bitField0_ = result.bitField0_ | to_bitField0_;
         }

         public MysqlxPrepare.Execute.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxPrepare.Execute) {
               return this.mergeFrom((MysqlxPrepare.Execute)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxPrepare.Execute.Builder mergeFrom(MysqlxPrepare.Execute other) {
            if (other == MysqlxPrepare.Execute.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasStmtId()) {
                  this.setStmtId(other.getStmtId());
               }

               if (this.argsBuilder_ == null) {
                  if (!other.args_.isEmpty()) {
                     if (this.args_.isEmpty()) {
                        this.args_ = other.args_;
                        this.bitField0_ &= -3;
                     } else {
                        this.ensureArgsIsMutable();
                        this.args_.addAll(other.args_);
                     }

                     this.onChanged();
                  }
               } else if (!other.args_.isEmpty()) {
                  if (this.argsBuilder_.isEmpty()) {
                     this.argsBuilder_.dispose();
                     this.argsBuilder_ = null;
                     this.args_ = other.args_;
                     this.bitField0_ &= -3;
                     this.argsBuilder_ = MysqlxPrepare.Execute.alwaysUseFieldBuilders ? this.getArgsFieldBuilder() : null;
                  } else {
                     this.argsBuilder_.addAllMessages(other.args_);
                  }
               }

               if (other.hasCompactMetadata()) {
                  this.setCompactMetadata(other.getCompactMetadata());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasStmtId()) {
               return false;
            } else {
               for (int i = 0; i < this.getArgsCount(); i++) {
                  if (!this.getArgs(i).isInitialized()) {
                     return false;
                  }
               }

               return true;
            }
         }

         public MysqlxPrepare.Execute.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
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
                           this.stmtId_ = input.readUInt32();
                           this.bitField0_ |= 1;
                           break;
                        case 18:
                           MysqlxDatatypes.Any m = (MysqlxDatatypes.Any)input.readMessage(MysqlxDatatypes.Any.parser(), extensionRegistry);
                           if (this.argsBuilder_ == null) {
                              this.ensureArgsIsMutable();
                              this.args_.add(m);
                           } else {
                              this.argsBuilder_.addMessage(m);
                           }
                           break;
                        case 24:
                           this.compactMetadata_ = input.readBool();
                           this.bitField0_ |= 4;
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

         @Override
         public boolean hasStmtId() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public int getStmtId() {
            return this.stmtId_;
         }

         public MysqlxPrepare.Execute.Builder setStmtId(int value) {
            this.stmtId_ = value;
            this.bitField0_ |= 1;
            this.onChanged();
            return this;
         }

         public MysqlxPrepare.Execute.Builder clearStmtId() {
            this.bitField0_ &= -2;
            this.stmtId_ = 0;
            this.onChanged();
            return this;
         }

         private void ensureArgsIsMutable() {
            if ((this.bitField0_ & 2) == 0) {
               this.args_ = new ArrayList<>(this.args_);
               this.bitField0_ |= 2;
            }
         }

         @Override
         public List<MysqlxDatatypes.Any> getArgsList() {
            return this.argsBuilder_ == null ? Collections.unmodifiableList(this.args_) : this.argsBuilder_.getMessageList();
         }

         @Override
         public int getArgsCount() {
            return this.argsBuilder_ == null ? this.args_.size() : this.argsBuilder_.getCount();
         }

         @Override
         public MysqlxDatatypes.Any getArgs(int index) {
            return this.argsBuilder_ == null ? this.args_.get(index) : (MysqlxDatatypes.Any)this.argsBuilder_.getMessage(index);
         }

         public MysqlxPrepare.Execute.Builder setArgs(int index, MysqlxDatatypes.Any value) {
            if (this.argsBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureArgsIsMutable();
               this.args_.set(index, value);
               this.onChanged();
            } else {
               this.argsBuilder_.setMessage(index, value);
            }

            return this;
         }

         public MysqlxPrepare.Execute.Builder setArgs(int index, MysqlxDatatypes.Any.Builder builderForValue) {
            if (this.argsBuilder_ == null) {
               this.ensureArgsIsMutable();
               this.args_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.argsBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxPrepare.Execute.Builder addArgs(MysqlxDatatypes.Any value) {
            if (this.argsBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureArgsIsMutable();
               this.args_.add(value);
               this.onChanged();
            } else {
               this.argsBuilder_.addMessage(value);
            }

            return this;
         }

         public MysqlxPrepare.Execute.Builder addArgs(int index, MysqlxDatatypes.Any value) {
            if (this.argsBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureArgsIsMutable();
               this.args_.add(index, value);
               this.onChanged();
            } else {
               this.argsBuilder_.addMessage(index, value);
            }

            return this;
         }

         public MysqlxPrepare.Execute.Builder addArgs(MysqlxDatatypes.Any.Builder builderForValue) {
            if (this.argsBuilder_ == null) {
               this.ensureArgsIsMutable();
               this.args_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.argsBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxPrepare.Execute.Builder addArgs(int index, MysqlxDatatypes.Any.Builder builderForValue) {
            if (this.argsBuilder_ == null) {
               this.ensureArgsIsMutable();
               this.args_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.argsBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxPrepare.Execute.Builder addAllArgs(Iterable<? extends MysqlxDatatypes.Any> values) {
            if (this.argsBuilder_ == null) {
               this.ensureArgsIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.args_);
               this.onChanged();
            } else {
               this.argsBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxPrepare.Execute.Builder clearArgs() {
            if (this.argsBuilder_ == null) {
               this.args_ = Collections.emptyList();
               this.bitField0_ &= -3;
               this.onChanged();
            } else {
               this.argsBuilder_.clear();
            }

            return this;
         }

         public MysqlxPrepare.Execute.Builder removeArgs(int index) {
            if (this.argsBuilder_ == null) {
               this.ensureArgsIsMutable();
               this.args_.remove(index);
               this.onChanged();
            } else {
               this.argsBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxDatatypes.Any.Builder getArgsBuilder(int index) {
            return (MysqlxDatatypes.Any.Builder)this.getArgsFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxDatatypes.AnyOrBuilder getArgsOrBuilder(int index) {
            return this.argsBuilder_ == null ? this.args_.get(index) : (MysqlxDatatypes.AnyOrBuilder)this.argsBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxDatatypes.AnyOrBuilder> getArgsOrBuilderList() {
            return this.argsBuilder_ != null ? this.argsBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.args_);
         }

         public MysqlxDatatypes.Any.Builder addArgsBuilder() {
            return (MysqlxDatatypes.Any.Builder)this.getArgsFieldBuilder().addBuilder(MysqlxDatatypes.Any.getDefaultInstance());
         }

         public MysqlxDatatypes.Any.Builder addArgsBuilder(int index) {
            return (MysqlxDatatypes.Any.Builder)this.getArgsFieldBuilder().addBuilder(index, MysqlxDatatypes.Any.getDefaultInstance());
         }

         public List<MysqlxDatatypes.Any.Builder> getArgsBuilderList() {
            return this.getArgsFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> getArgsFieldBuilder() {
            if (this.argsBuilder_ == null) {
               this.argsBuilder_ = new RepeatedFieldBuilder(this.args_, (this.bitField0_ & 2) != 0, this.getParentForChildren(), this.isClean());
               this.args_ = null;
            }

            return this.argsBuilder_;
         }

         @Override
         public boolean hasCompactMetadata() {
            return (this.bitField0_ & 4) != 0;
         }

         @Override
         public boolean getCompactMetadata() {
            return this.compactMetadata_;
         }

         public MysqlxPrepare.Execute.Builder setCompactMetadata(boolean value) {
            this.compactMetadata_ = value;
            this.bitField0_ |= 4;
            this.onChanged();
            return this;
         }

         public MysqlxPrepare.Execute.Builder clearCompactMetadata() {
            this.bitField0_ &= -5;
            this.compactMetadata_ = false;
            this.onChanged();
            return this;
         }
      }
   }

   public interface ExecuteOrBuilder extends MessageOrBuilder {
      boolean hasStmtId();

      int getStmtId();

      List<MysqlxDatatypes.Any> getArgsList();

      MysqlxDatatypes.Any getArgs(int var1);

      int getArgsCount();

      List<? extends MysqlxDatatypes.AnyOrBuilder> getArgsOrBuilderList();

      MysqlxDatatypes.AnyOrBuilder getArgsOrBuilder(int var1);

      boolean hasCompactMetadata();

      boolean getCompactMetadata();
   }

   public static final class Prepare extends GeneratedMessage implements MysqlxPrepare.PrepareOrBuilder {
      private static final long serialVersionUID = 0L;
      private int bitField0_;
      public static final int STMT_ID_FIELD_NUMBER = 1;
      private int stmtId_ = 0;
      public static final int STMT_FIELD_NUMBER = 2;
      private MysqlxPrepare.Prepare.OneOfMessage stmt_;
      private byte memoizedIsInitialized = -1;
      private static final MysqlxPrepare.Prepare DEFAULT_INSTANCE = new MysqlxPrepare.Prepare();
      private static final Parser<MysqlxPrepare.Prepare> PARSER = new AbstractParser<MysqlxPrepare.Prepare>() {
         public MysqlxPrepare.Prepare parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            MysqlxPrepare.Prepare.Builder builder = MysqlxPrepare.Prepare.newBuilder();

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

      private Prepare(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private Prepare() {
      }

      public static final Descriptor getDescriptor() {
         return MysqlxPrepare.internal_static_Mysqlx_Prepare_Prepare_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxPrepare.internal_static_Mysqlx_Prepare_Prepare_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxPrepare.Prepare.class, MysqlxPrepare.Prepare.Builder.class);
      }

      @Override
      public boolean hasStmtId() {
         return (this.bitField0_ & 1) != 0;
      }

      @Override
      public int getStmtId() {
         return this.stmtId_;
      }

      @Override
      public boolean hasStmt() {
         return (this.bitField0_ & 2) != 0;
      }

      @Override
      public MysqlxPrepare.Prepare.OneOfMessage getStmt() {
         return this.stmt_ == null ? MysqlxPrepare.Prepare.OneOfMessage.getDefaultInstance() : this.stmt_;
      }

      @Override
      public MysqlxPrepare.Prepare.OneOfMessageOrBuilder getStmtOrBuilder() {
         return this.stmt_ == null ? MysqlxPrepare.Prepare.OneOfMessage.getDefaultInstance() : this.stmt_;
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasStmtId()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (!this.hasStmt()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (!this.getStmt().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         if ((this.bitField0_ & 1) != 0) {
            output.writeUInt32(1, this.stmtId_);
         }

         if ((this.bitField0_ & 2) != 0) {
            output.writeMessage(2, this.getStmt());
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
               size += CodedOutputStream.computeUInt32Size(1, this.stmtId_);
            }

            if ((this.bitField0_ & 2) != 0) {
               size += CodedOutputStream.computeMessageSize(2, this.getStmt());
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSize = size;
            return size;
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof MysqlxPrepare.Prepare)) {
            return super.equals(obj);
         } else {
            MysqlxPrepare.Prepare other = (MysqlxPrepare.Prepare)obj;
            if (this.hasStmtId() != other.hasStmtId()) {
               return false;
            } else if (this.hasStmtId() && this.getStmtId() != other.getStmtId()) {
               return false;
            } else if (this.hasStmt() != other.hasStmt()) {
               return false;
            } else {
               return this.hasStmt() && !this.getStmt().equals(other.getStmt()) ? false : this.getUnknownFields().equals(other.getUnknownFields());
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
            if (this.hasStmtId()) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.getStmtId();
            }

            if (this.hasStmt()) {
               hash = 37 * hash + 2;
               hash = 53 * hash + this.getStmt().hashCode();
            }

            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static MysqlxPrepare.Prepare parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Prepare)PARSER.parseFrom(data);
      }

      public static MysqlxPrepare.Prepare parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Prepare)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxPrepare.Prepare parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Prepare)PARSER.parseFrom(data);
      }

      public static MysqlxPrepare.Prepare parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Prepare)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxPrepare.Prepare parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Prepare)PARSER.parseFrom(data);
      }

      public static MysqlxPrepare.Prepare parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxPrepare.Prepare)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxPrepare.Prepare parseFrom(InputStream input) throws IOException {
         return (MysqlxPrepare.Prepare)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxPrepare.Prepare parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxPrepare.Prepare)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxPrepare.Prepare parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxPrepare.Prepare)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static MysqlxPrepare.Prepare parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxPrepare.Prepare)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static MysqlxPrepare.Prepare parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxPrepare.Prepare)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static MysqlxPrepare.Prepare parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxPrepare.Prepare)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public MysqlxPrepare.Prepare.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxPrepare.Prepare.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static MysqlxPrepare.Prepare.Builder newBuilder(MysqlxPrepare.Prepare prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public MysqlxPrepare.Prepare.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new MysqlxPrepare.Prepare.Builder() : new MysqlxPrepare.Prepare.Builder().mergeFrom(this);
      }

      protected MysqlxPrepare.Prepare.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxPrepare.Prepare.Builder(parent);
      }

      public static MysqlxPrepare.Prepare getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<MysqlxPrepare.Prepare> parser() {
         return PARSER;
      }

      public Parser<MysqlxPrepare.Prepare> getParserForType() {
         return PARSER;
      }

      public MysqlxPrepare.Prepare getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxPrepare.Prepare.class.getName());
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxPrepare.Prepare.Builder>
         implements MysqlxPrepare.PrepareOrBuilder {
         private int bitField0_;
         private int stmtId_;
         private MysqlxPrepare.Prepare.OneOfMessage stmt_;
         private SingleFieldBuilder<MysqlxPrepare.Prepare.OneOfMessage, MysqlxPrepare.Prepare.OneOfMessage.Builder, MysqlxPrepare.Prepare.OneOfMessageOrBuilder> stmtBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Prepare_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Prepare_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxPrepare.Prepare.class, MysqlxPrepare.Prepare.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxPrepare.Prepare.alwaysUseFieldBuilders) {
               this.getStmtFieldBuilder();
            }
         }

         public MysqlxPrepare.Prepare.Builder clear() {
            super.clear();
            this.bitField0_ = 0;
            this.stmtId_ = 0;
            this.stmt_ = null;
            if (this.stmtBuilder_ != null) {
               this.stmtBuilder_.dispose();
               this.stmtBuilder_ = null;
            }

            return this;
         }

         public Descriptor getDescriptorForType() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Prepare_descriptor;
         }

         public MysqlxPrepare.Prepare getDefaultInstanceForType() {
            return MysqlxPrepare.Prepare.getDefaultInstance();
         }

         public MysqlxPrepare.Prepare build() {
            MysqlxPrepare.Prepare result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxPrepare.Prepare buildPartial() {
            MysqlxPrepare.Prepare result = new MysqlxPrepare.Prepare(this);
            if (this.bitField0_ != 0) {
               this.buildPartial0(result);
            }

            this.onBuilt();
            return result;
         }

         private void buildPartial0(MysqlxPrepare.Prepare result) {
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) != 0) {
               result.stmtId_ = this.stmtId_;
               to_bitField0_ |= 1;
            }

            if ((from_bitField0_ & 2) != 0) {
               result.stmt_ = this.stmtBuilder_ == null ? this.stmt_ : (MysqlxPrepare.Prepare.OneOfMessage)this.stmtBuilder_.build();
               to_bitField0_ |= 2;
            }

            result.bitField0_ = result.bitField0_ | to_bitField0_;
         }

         public MysqlxPrepare.Prepare.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxPrepare.Prepare) {
               return this.mergeFrom((MysqlxPrepare.Prepare)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxPrepare.Prepare.Builder mergeFrom(MysqlxPrepare.Prepare other) {
            if (other == MysqlxPrepare.Prepare.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasStmtId()) {
                  this.setStmtId(other.getStmtId());
               }

               if (other.hasStmt()) {
                  this.mergeStmt(other.getStmt());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasStmtId()) {
               return false;
            } else {
               return !this.hasStmt() ? false : this.getStmt().isInitialized();
            }
         }

         public MysqlxPrepare.Prepare.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
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
                           this.stmtId_ = input.readUInt32();
                           this.bitField0_ |= 1;
                           break;
                        case 18:
                           input.readMessage(this.getStmtFieldBuilder().getBuilder(), extensionRegistry);
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
         public boolean hasStmtId() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public int getStmtId() {
            return this.stmtId_;
         }

         public MysqlxPrepare.Prepare.Builder setStmtId(int value) {
            this.stmtId_ = value;
            this.bitField0_ |= 1;
            this.onChanged();
            return this;
         }

         public MysqlxPrepare.Prepare.Builder clearStmtId() {
            this.bitField0_ &= -2;
            this.stmtId_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasStmt() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public MysqlxPrepare.Prepare.OneOfMessage getStmt() {
            if (this.stmtBuilder_ == null) {
               return this.stmt_ == null ? MysqlxPrepare.Prepare.OneOfMessage.getDefaultInstance() : this.stmt_;
            } else {
               return (MysqlxPrepare.Prepare.OneOfMessage)this.stmtBuilder_.getMessage();
            }
         }

         public MysqlxPrepare.Prepare.Builder setStmt(MysqlxPrepare.Prepare.OneOfMessage value) {
            if (this.stmtBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.stmt_ = value;
            } else {
               this.stmtBuilder_.setMessage(value);
            }

            this.bitField0_ |= 2;
            this.onChanged();
            return this;
         }

         public MysqlxPrepare.Prepare.Builder setStmt(MysqlxPrepare.Prepare.OneOfMessage.Builder builderForValue) {
            if (this.stmtBuilder_ == null) {
               this.stmt_ = builderForValue.build();
            } else {
               this.stmtBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 2;
            this.onChanged();
            return this;
         }

         public MysqlxPrepare.Prepare.Builder mergeStmt(MysqlxPrepare.Prepare.OneOfMessage value) {
            if (this.stmtBuilder_ == null) {
               if ((this.bitField0_ & 2) != 0 && this.stmt_ != null && this.stmt_ != MysqlxPrepare.Prepare.OneOfMessage.getDefaultInstance()) {
                  this.getStmtBuilder().mergeFrom(value);
               } else {
                  this.stmt_ = value;
               }
            } else {
               this.stmtBuilder_.mergeFrom(value);
            }

            if (this.stmt_ != null) {
               this.bitField0_ |= 2;
               this.onChanged();
            }

            return this;
         }

         public MysqlxPrepare.Prepare.Builder clearStmt() {
            this.bitField0_ &= -3;
            this.stmt_ = null;
            if (this.stmtBuilder_ != null) {
               this.stmtBuilder_.dispose();
               this.stmtBuilder_ = null;
            }

            this.onChanged();
            return this;
         }

         public MysqlxPrepare.Prepare.OneOfMessage.Builder getStmtBuilder() {
            this.bitField0_ |= 2;
            this.onChanged();
            return (MysqlxPrepare.Prepare.OneOfMessage.Builder)this.getStmtFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxPrepare.Prepare.OneOfMessageOrBuilder getStmtOrBuilder() {
            if (this.stmtBuilder_ != null) {
               return (MysqlxPrepare.Prepare.OneOfMessageOrBuilder)this.stmtBuilder_.getMessageOrBuilder();
            } else {
               return this.stmt_ == null ? MysqlxPrepare.Prepare.OneOfMessage.getDefaultInstance() : this.stmt_;
            }
         }

         private SingleFieldBuilder<MysqlxPrepare.Prepare.OneOfMessage, MysqlxPrepare.Prepare.OneOfMessage.Builder, MysqlxPrepare.Prepare.OneOfMessageOrBuilder> getStmtFieldBuilder() {
            if (this.stmtBuilder_ == null) {
               this.stmtBuilder_ = new SingleFieldBuilder(this.getStmt(), this.getParentForChildren(), this.isClean());
               this.stmt_ = null;
            }

            return this.stmtBuilder_;
         }
      }

      public static final class OneOfMessage extends GeneratedMessage implements MysqlxPrepare.Prepare.OneOfMessageOrBuilder {
         private static final long serialVersionUID = 0L;
         private int bitField0_;
         public static final int TYPE_FIELD_NUMBER = 1;
         private int type_ = 0;
         public static final int FIND_FIELD_NUMBER = 2;
         private MysqlxCrud.Find find_;
         public static final int INSERT_FIELD_NUMBER = 3;
         private MysqlxCrud.Insert insert_;
         public static final int UPDATE_FIELD_NUMBER = 4;
         private MysqlxCrud.Update update_;
         public static final int DELETE_FIELD_NUMBER = 5;
         private MysqlxCrud.Delete delete_;
         public static final int STMT_EXECUTE_FIELD_NUMBER = 6;
         private MysqlxSql.StmtExecute stmtExecute_;
         private byte memoizedIsInitialized = -1;
         private static final MysqlxPrepare.Prepare.OneOfMessage DEFAULT_INSTANCE = new MysqlxPrepare.Prepare.OneOfMessage();
         private static final Parser<MysqlxPrepare.Prepare.OneOfMessage> PARSER = new AbstractParser<MysqlxPrepare.Prepare.OneOfMessage>() {
            public MysqlxPrepare.Prepare.OneOfMessage parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
               MysqlxPrepare.Prepare.OneOfMessage.Builder builder = MysqlxPrepare.Prepare.OneOfMessage.newBuilder();

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

         private OneOfMessage(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
         }

         private OneOfMessage() {
            this.type_ = 0;
         }

         public static final Descriptor getDescriptor() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Prepare_OneOfMessage_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxPrepare.internal_static_Mysqlx_Prepare_Prepare_OneOfMessage_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxPrepare.Prepare.OneOfMessage.class, MysqlxPrepare.Prepare.OneOfMessage.Builder.class);
         }

         @Override
         public boolean hasType() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public MysqlxPrepare.Prepare.OneOfMessage.Type getType() {
            MysqlxPrepare.Prepare.OneOfMessage.Type result = MysqlxPrepare.Prepare.OneOfMessage.Type.forNumber(this.type_);
            return result == null ? MysqlxPrepare.Prepare.OneOfMessage.Type.FIND : result;
         }

         @Override
         public boolean hasFind() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public MysqlxCrud.Find getFind() {
            return this.find_ == null ? MysqlxCrud.Find.getDefaultInstance() : this.find_;
         }

         @Override
         public MysqlxCrud.FindOrBuilder getFindOrBuilder() {
            return this.find_ == null ? MysqlxCrud.Find.getDefaultInstance() : this.find_;
         }

         @Override
         public boolean hasInsert() {
            return (this.bitField0_ & 4) != 0;
         }

         @Override
         public MysqlxCrud.Insert getInsert() {
            return this.insert_ == null ? MysqlxCrud.Insert.getDefaultInstance() : this.insert_;
         }

         @Override
         public MysqlxCrud.InsertOrBuilder getInsertOrBuilder() {
            return this.insert_ == null ? MysqlxCrud.Insert.getDefaultInstance() : this.insert_;
         }

         @Override
         public boolean hasUpdate() {
            return (this.bitField0_ & 8) != 0;
         }

         @Override
         public MysqlxCrud.Update getUpdate() {
            return this.update_ == null ? MysqlxCrud.Update.getDefaultInstance() : this.update_;
         }

         @Override
         public MysqlxCrud.UpdateOrBuilder getUpdateOrBuilder() {
            return this.update_ == null ? MysqlxCrud.Update.getDefaultInstance() : this.update_;
         }

         @Override
         public boolean hasDelete() {
            return (this.bitField0_ & 16) != 0;
         }

         @Override
         public MysqlxCrud.Delete getDelete() {
            return this.delete_ == null ? MysqlxCrud.Delete.getDefaultInstance() : this.delete_;
         }

         @Override
         public MysqlxCrud.DeleteOrBuilder getDeleteOrBuilder() {
            return this.delete_ == null ? MysqlxCrud.Delete.getDefaultInstance() : this.delete_;
         }

         @Override
         public boolean hasStmtExecute() {
            return (this.bitField0_ & 32) != 0;
         }

         @Override
         public MysqlxSql.StmtExecute getStmtExecute() {
            return this.stmtExecute_ == null ? MysqlxSql.StmtExecute.getDefaultInstance() : this.stmtExecute_;
         }

         @Override
         public MysqlxSql.StmtExecuteOrBuilder getStmtExecuteOrBuilder() {
            return this.stmtExecute_ == null ? MysqlxSql.StmtExecute.getDefaultInstance() : this.stmtExecute_;
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
            } else if (this.hasFind() && !this.getFind().isInitialized()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else if (this.hasInsert() && !this.getInsert().isInitialized()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else if (this.hasUpdate() && !this.getUpdate().isInitialized()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else if (this.hasDelete() && !this.getDelete().isInitialized()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else if (this.hasStmtExecute() && !this.getStmtExecute().isInitialized()) {
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
               output.writeMessage(2, this.getFind());
            }

            if ((this.bitField0_ & 4) != 0) {
               output.writeMessage(3, this.getInsert());
            }

            if ((this.bitField0_ & 8) != 0) {
               output.writeMessage(4, this.getUpdate());
            }

            if ((this.bitField0_ & 16) != 0) {
               output.writeMessage(5, this.getDelete());
            }

            if ((this.bitField0_ & 32) != 0) {
               output.writeMessage(6, this.getStmtExecute());
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
                  size += CodedOutputStream.computeMessageSize(2, this.getFind());
               }

               if ((this.bitField0_ & 4) != 0) {
                  size += CodedOutputStream.computeMessageSize(3, this.getInsert());
               }

               if ((this.bitField0_ & 8) != 0) {
                  size += CodedOutputStream.computeMessageSize(4, this.getUpdate());
               }

               if ((this.bitField0_ & 16) != 0) {
                  size += CodedOutputStream.computeMessageSize(5, this.getDelete());
               }

               if ((this.bitField0_ & 32) != 0) {
                  size += CodedOutputStream.computeMessageSize(6, this.getStmtExecute());
               }

               size += this.getUnknownFields().getSerializedSize();
               this.memoizedSize = size;
               return size;
            }
         }

         @Override
         public boolean equals(Object obj) {
            if (obj == this) {
               return true;
            } else if (!(obj instanceof MysqlxPrepare.Prepare.OneOfMessage)) {
               return super.equals(obj);
            } else {
               MysqlxPrepare.Prepare.OneOfMessage other = (MysqlxPrepare.Prepare.OneOfMessage)obj;
               if (this.hasType() != other.hasType()) {
                  return false;
               } else if (this.hasType() && this.type_ != other.type_) {
                  return false;
               } else if (this.hasFind() != other.hasFind()) {
                  return false;
               } else if (this.hasFind() && !this.getFind().equals(other.getFind())) {
                  return false;
               } else if (this.hasInsert() != other.hasInsert()) {
                  return false;
               } else if (this.hasInsert() && !this.getInsert().equals(other.getInsert())) {
                  return false;
               } else if (this.hasUpdate() != other.hasUpdate()) {
                  return false;
               } else if (this.hasUpdate() && !this.getUpdate().equals(other.getUpdate())) {
                  return false;
               } else if (this.hasDelete() != other.hasDelete()) {
                  return false;
               } else if (this.hasDelete() && !this.getDelete().equals(other.getDelete())) {
                  return false;
               } else if (this.hasStmtExecute() != other.hasStmtExecute()) {
                  return false;
               } else {
                  return this.hasStmtExecute() && !this.getStmtExecute().equals(other.getStmtExecute())
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
               if (this.hasType()) {
                  hash = 37 * hash + 1;
                  hash = 53 * hash + this.type_;
               }

               if (this.hasFind()) {
                  hash = 37 * hash + 2;
                  hash = 53 * hash + this.getFind().hashCode();
               }

               if (this.hasInsert()) {
                  hash = 37 * hash + 3;
                  hash = 53 * hash + this.getInsert().hashCode();
               }

               if (this.hasUpdate()) {
                  hash = 37 * hash + 4;
                  hash = 53 * hash + this.getUpdate().hashCode();
               }

               if (this.hasDelete()) {
                  hash = 37 * hash + 5;
                  hash = 53 * hash + this.getDelete().hashCode();
               }

               if (this.hasStmtExecute()) {
                  hash = 37 * hash + 6;
                  hash = 53 * hash + this.getStmtExecute().hashCode();
               }

               hash = 29 * hash + this.getUnknownFields().hashCode();
               this.memoizedHashCode = hash;
               return hash;
            }
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
            return (MysqlxPrepare.Prepare.OneOfMessage)PARSER.parseFrom(data);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxPrepare.Prepare.OneOfMessage)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (MysqlxPrepare.Prepare.OneOfMessage)PARSER.parseFrom(data);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxPrepare.Prepare.OneOfMessage)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (MysqlxPrepare.Prepare.OneOfMessage)PARSER.parseFrom(data);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxPrepare.Prepare.OneOfMessage)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseFrom(InputStream input) throws IOException {
            return (MysqlxPrepare.Prepare.OneOfMessage)GeneratedMessage.parseWithIOException(PARSER, input);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxPrepare.Prepare.OneOfMessage)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseDelimitedFrom(InputStream input) throws IOException {
            return (MysqlxPrepare.Prepare.OneOfMessage)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxPrepare.Prepare.OneOfMessage)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseFrom(CodedInputStream input) throws IOException {
            return (MysqlxPrepare.Prepare.OneOfMessage)GeneratedMessage.parseWithIOException(PARSER, input);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxPrepare.Prepare.OneOfMessage)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
         }

         public MysqlxPrepare.Prepare.OneOfMessage.Builder newBuilderForType() {
            return newBuilder();
         }

         public static MysqlxPrepare.Prepare.OneOfMessage.Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
         }

         public static MysqlxPrepare.Prepare.OneOfMessage.Builder newBuilder(MysqlxPrepare.Prepare.OneOfMessage prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
         }

         public MysqlxPrepare.Prepare.OneOfMessage.Builder toBuilder() {
            return this == DEFAULT_INSTANCE
               ? new MysqlxPrepare.Prepare.OneOfMessage.Builder()
               : new MysqlxPrepare.Prepare.OneOfMessage.Builder().mergeFrom(this);
         }

         protected MysqlxPrepare.Prepare.OneOfMessage.Builder newBuilderForType(BuilderParent parent) {
            return new MysqlxPrepare.Prepare.OneOfMessage.Builder(parent);
         }

         public static MysqlxPrepare.Prepare.OneOfMessage getDefaultInstance() {
            return DEFAULT_INSTANCE;
         }

         public static Parser<MysqlxPrepare.Prepare.OneOfMessage> parser() {
            return PARSER;
         }

         public Parser<MysqlxPrepare.Prepare.OneOfMessage> getParserForType() {
            return PARSER;
         }

         public MysqlxPrepare.Prepare.OneOfMessage getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
         }

         static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxPrepare.Prepare.OneOfMessage.class.getName());
         }

         public static final class Builder
            extends com.google.protobuf.GeneratedMessage.Builder<MysqlxPrepare.Prepare.OneOfMessage.Builder>
            implements MysqlxPrepare.Prepare.OneOfMessageOrBuilder {
            private int bitField0_;
            private int type_ = 0;
            private MysqlxCrud.Find find_;
            private SingleFieldBuilder<MysqlxCrud.Find, MysqlxCrud.Find.Builder, MysqlxCrud.FindOrBuilder> findBuilder_;
            private MysqlxCrud.Insert insert_;
            private SingleFieldBuilder<MysqlxCrud.Insert, MysqlxCrud.Insert.Builder, MysqlxCrud.InsertOrBuilder> insertBuilder_;
            private MysqlxCrud.Update update_;
            private SingleFieldBuilder<MysqlxCrud.Update, MysqlxCrud.Update.Builder, MysqlxCrud.UpdateOrBuilder> updateBuilder_;
            private MysqlxCrud.Delete delete_;
            private SingleFieldBuilder<MysqlxCrud.Delete, MysqlxCrud.Delete.Builder, MysqlxCrud.DeleteOrBuilder> deleteBuilder_;
            private MysqlxSql.StmtExecute stmtExecute_;
            private SingleFieldBuilder<MysqlxSql.StmtExecute, MysqlxSql.StmtExecute.Builder, MysqlxSql.StmtExecuteOrBuilder> stmtExecuteBuilder_;

            public static final Descriptor getDescriptor() {
               return MysqlxPrepare.internal_static_Mysqlx_Prepare_Prepare_OneOfMessage_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
               return MysqlxPrepare.internal_static_Mysqlx_Prepare_Prepare_OneOfMessage_fieldAccessorTable
                  .ensureFieldAccessorsInitialized(MysqlxPrepare.Prepare.OneOfMessage.class, MysqlxPrepare.Prepare.OneOfMessage.Builder.class);
            }

            private Builder() {
               this.maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
               super(parent);
               this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
               if (MysqlxPrepare.Prepare.OneOfMessage.alwaysUseFieldBuilders) {
                  this.getFindFieldBuilder();
                  this.getInsertFieldBuilder();
                  this.getUpdateFieldBuilder();
                  this.getDeleteFieldBuilder();
                  this.getStmtExecuteFieldBuilder();
               }
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder clear() {
               super.clear();
               this.bitField0_ = 0;
               this.type_ = 0;
               this.find_ = null;
               if (this.findBuilder_ != null) {
                  this.findBuilder_.dispose();
                  this.findBuilder_ = null;
               }

               this.insert_ = null;
               if (this.insertBuilder_ != null) {
                  this.insertBuilder_.dispose();
                  this.insertBuilder_ = null;
               }

               this.update_ = null;
               if (this.updateBuilder_ != null) {
                  this.updateBuilder_.dispose();
                  this.updateBuilder_ = null;
               }

               this.delete_ = null;
               if (this.deleteBuilder_ != null) {
                  this.deleteBuilder_.dispose();
                  this.deleteBuilder_ = null;
               }

               this.stmtExecute_ = null;
               if (this.stmtExecuteBuilder_ != null) {
                  this.stmtExecuteBuilder_.dispose();
                  this.stmtExecuteBuilder_ = null;
               }

               return this;
            }

            public Descriptor getDescriptorForType() {
               return MysqlxPrepare.internal_static_Mysqlx_Prepare_Prepare_OneOfMessage_descriptor;
            }

            public MysqlxPrepare.Prepare.OneOfMessage getDefaultInstanceForType() {
               return MysqlxPrepare.Prepare.OneOfMessage.getDefaultInstance();
            }

            public MysqlxPrepare.Prepare.OneOfMessage build() {
               MysqlxPrepare.Prepare.OneOfMessage result = this.buildPartial();
               if (!result.isInitialized()) {
                  throw newUninitializedMessageException(result);
               } else {
                  return result;
               }
            }

            public MysqlxPrepare.Prepare.OneOfMessage buildPartial() {
               MysqlxPrepare.Prepare.OneOfMessage result = new MysqlxPrepare.Prepare.OneOfMessage(this);
               if (this.bitField0_ != 0) {
                  this.buildPartial0(result);
               }

               this.onBuilt();
               return result;
            }

            private void buildPartial0(MysqlxPrepare.Prepare.OneOfMessage result) {
               int from_bitField0_ = this.bitField0_;
               int to_bitField0_ = 0;
               if ((from_bitField0_ & 1) != 0) {
                  result.type_ = this.type_;
                  to_bitField0_ |= 1;
               }

               if ((from_bitField0_ & 2) != 0) {
                  result.find_ = this.findBuilder_ == null ? this.find_ : (MysqlxCrud.Find)this.findBuilder_.build();
                  to_bitField0_ |= 2;
               }

               if ((from_bitField0_ & 4) != 0) {
                  result.insert_ = this.insertBuilder_ == null ? this.insert_ : (MysqlxCrud.Insert)this.insertBuilder_.build();
                  to_bitField0_ |= 4;
               }

               if ((from_bitField0_ & 8) != 0) {
                  result.update_ = this.updateBuilder_ == null ? this.update_ : (MysqlxCrud.Update)this.updateBuilder_.build();
                  to_bitField0_ |= 8;
               }

               if ((from_bitField0_ & 16) != 0) {
                  result.delete_ = this.deleteBuilder_ == null ? this.delete_ : (MysqlxCrud.Delete)this.deleteBuilder_.build();
                  to_bitField0_ |= 16;
               }

               if ((from_bitField0_ & 32) != 0) {
                  result.stmtExecute_ = this.stmtExecuteBuilder_ == null ? this.stmtExecute_ : (MysqlxSql.StmtExecute)this.stmtExecuteBuilder_.build();
                  to_bitField0_ |= 32;
               }

               result.bitField0_ = result.bitField0_ | to_bitField0_;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder mergeFrom(Message other) {
               if (other instanceof MysqlxPrepare.Prepare.OneOfMessage) {
                  return this.mergeFrom((MysqlxPrepare.Prepare.OneOfMessage)other);
               } else {
                  super.mergeFrom(other);
                  return this;
               }
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder mergeFrom(MysqlxPrepare.Prepare.OneOfMessage other) {
               if (other == MysqlxPrepare.Prepare.OneOfMessage.getDefaultInstance()) {
                  return this;
               } else {
                  if (other.hasType()) {
                     this.setType(other.getType());
                  }

                  if (other.hasFind()) {
                     this.mergeFind(other.getFind());
                  }

                  if (other.hasInsert()) {
                     this.mergeInsert(other.getInsert());
                  }

                  if (other.hasUpdate()) {
                     this.mergeUpdate(other.getUpdate());
                  }

                  if (other.hasDelete()) {
                     this.mergeDelete(other.getDelete());
                  }

                  if (other.hasStmtExecute()) {
                     this.mergeStmtExecute(other.getStmtExecute());
                  }

                  this.mergeUnknownFields(other.getUnknownFields());
                  this.onChanged();
                  return this;
               }
            }

            public final boolean isInitialized() {
               if (!this.hasType()) {
                  return false;
               } else if (this.hasFind() && !this.getFind().isInitialized()) {
                  return false;
               } else if (this.hasInsert() && !this.getInsert().isInitialized()) {
                  return false;
               } else if (this.hasUpdate() && !this.getUpdate().isInitialized()) {
                  return false;
               } else {
                  return this.hasDelete() && !this.getDelete().isInitialized() ? false : !this.hasStmtExecute() || this.getStmtExecute().isInitialized();
               }
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
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
                              MysqlxPrepare.Prepare.OneOfMessage.Type tmpValue = MysqlxPrepare.Prepare.OneOfMessage.Type.forNumber(tmpRaw);
                              if (tmpValue == null) {
                                 this.mergeUnknownVarintField(1, tmpRaw);
                              } else {
                                 this.type_ = tmpRaw;
                                 this.bitField0_ |= 1;
                              }
                              break;
                           case 18:
                              input.readMessage(this.getFindFieldBuilder().getBuilder(), extensionRegistry);
                              this.bitField0_ |= 2;
                              break;
                           case 26:
                              input.readMessage(this.getInsertFieldBuilder().getBuilder(), extensionRegistry);
                              this.bitField0_ |= 4;
                              break;
                           case 34:
                              input.readMessage(this.getUpdateFieldBuilder().getBuilder(), extensionRegistry);
                              this.bitField0_ |= 8;
                              break;
                           case 42:
                              input.readMessage(this.getDeleteFieldBuilder().getBuilder(), extensionRegistry);
                              this.bitField0_ |= 16;
                              break;
                           case 50:
                              input.readMessage(this.getStmtExecuteFieldBuilder().getBuilder(), extensionRegistry);
                              this.bitField0_ |= 32;
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
            public MysqlxPrepare.Prepare.OneOfMessage.Type getType() {
               MysqlxPrepare.Prepare.OneOfMessage.Type result = MysqlxPrepare.Prepare.OneOfMessage.Type.forNumber(this.type_);
               return result == null ? MysqlxPrepare.Prepare.OneOfMessage.Type.FIND : result;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setType(MysqlxPrepare.Prepare.OneOfMessage.Type value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 1;
                  this.type_ = value.getNumber();
                  this.onChanged();
                  return this;
               }
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder clearType() {
               this.bitField0_ &= -2;
               this.type_ = 0;
               this.onChanged();
               return this;
            }

            @Override
            public boolean hasFind() {
               return (this.bitField0_ & 2) != 0;
            }

            @Override
            public MysqlxCrud.Find getFind() {
               if (this.findBuilder_ == null) {
                  return this.find_ == null ? MysqlxCrud.Find.getDefaultInstance() : this.find_;
               } else {
                  return (MysqlxCrud.Find)this.findBuilder_.getMessage();
               }
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setFind(MysqlxCrud.Find value) {
               if (this.findBuilder_ == null) {
                  if (value == null) {
                     throw new NullPointerException();
                  }

                  this.find_ = value;
               } else {
                  this.findBuilder_.setMessage(value);
               }

               this.bitField0_ |= 2;
               this.onChanged();
               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setFind(MysqlxCrud.Find.Builder builderForValue) {
               if (this.findBuilder_ == null) {
                  this.find_ = builderForValue.build();
               } else {
                  this.findBuilder_.setMessage(builderForValue.build());
               }

               this.bitField0_ |= 2;
               this.onChanged();
               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder mergeFind(MysqlxCrud.Find value) {
               if (this.findBuilder_ == null) {
                  if ((this.bitField0_ & 2) != 0 && this.find_ != null && this.find_ != MysqlxCrud.Find.getDefaultInstance()) {
                     this.getFindBuilder().mergeFrom(value);
                  } else {
                     this.find_ = value;
                  }
               } else {
                  this.findBuilder_.mergeFrom(value);
               }

               if (this.find_ != null) {
                  this.bitField0_ |= 2;
                  this.onChanged();
               }

               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder clearFind() {
               this.bitField0_ &= -3;
               this.find_ = null;
               if (this.findBuilder_ != null) {
                  this.findBuilder_.dispose();
                  this.findBuilder_ = null;
               }

               this.onChanged();
               return this;
            }

            public MysqlxCrud.Find.Builder getFindBuilder() {
               this.bitField0_ |= 2;
               this.onChanged();
               return (MysqlxCrud.Find.Builder)this.getFindFieldBuilder().getBuilder();
            }

            @Override
            public MysqlxCrud.FindOrBuilder getFindOrBuilder() {
               if (this.findBuilder_ != null) {
                  return (MysqlxCrud.FindOrBuilder)this.findBuilder_.getMessageOrBuilder();
               } else {
                  return this.find_ == null ? MysqlxCrud.Find.getDefaultInstance() : this.find_;
               }
            }

            private SingleFieldBuilder<MysqlxCrud.Find, MysqlxCrud.Find.Builder, MysqlxCrud.FindOrBuilder> getFindFieldBuilder() {
               if (this.findBuilder_ == null) {
                  this.findBuilder_ = new SingleFieldBuilder(this.getFind(), this.getParentForChildren(), this.isClean());
                  this.find_ = null;
               }

               return this.findBuilder_;
            }

            @Override
            public boolean hasInsert() {
               return (this.bitField0_ & 4) != 0;
            }

            @Override
            public MysqlxCrud.Insert getInsert() {
               if (this.insertBuilder_ == null) {
                  return this.insert_ == null ? MysqlxCrud.Insert.getDefaultInstance() : this.insert_;
               } else {
                  return (MysqlxCrud.Insert)this.insertBuilder_.getMessage();
               }
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setInsert(MysqlxCrud.Insert value) {
               if (this.insertBuilder_ == null) {
                  if (value == null) {
                     throw new NullPointerException();
                  }

                  this.insert_ = value;
               } else {
                  this.insertBuilder_.setMessage(value);
               }

               this.bitField0_ |= 4;
               this.onChanged();
               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setInsert(MysqlxCrud.Insert.Builder builderForValue) {
               if (this.insertBuilder_ == null) {
                  this.insert_ = builderForValue.build();
               } else {
                  this.insertBuilder_.setMessage(builderForValue.build());
               }

               this.bitField0_ |= 4;
               this.onChanged();
               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder mergeInsert(MysqlxCrud.Insert value) {
               if (this.insertBuilder_ == null) {
                  if ((this.bitField0_ & 4) != 0 && this.insert_ != null && this.insert_ != MysqlxCrud.Insert.getDefaultInstance()) {
                     this.getInsertBuilder().mergeFrom(value);
                  } else {
                     this.insert_ = value;
                  }
               } else {
                  this.insertBuilder_.mergeFrom(value);
               }

               if (this.insert_ != null) {
                  this.bitField0_ |= 4;
                  this.onChanged();
               }

               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder clearInsert() {
               this.bitField0_ &= -5;
               this.insert_ = null;
               if (this.insertBuilder_ != null) {
                  this.insertBuilder_.dispose();
                  this.insertBuilder_ = null;
               }

               this.onChanged();
               return this;
            }

            public MysqlxCrud.Insert.Builder getInsertBuilder() {
               this.bitField0_ |= 4;
               this.onChanged();
               return (MysqlxCrud.Insert.Builder)this.getInsertFieldBuilder().getBuilder();
            }

            @Override
            public MysqlxCrud.InsertOrBuilder getInsertOrBuilder() {
               if (this.insertBuilder_ != null) {
                  return (MysqlxCrud.InsertOrBuilder)this.insertBuilder_.getMessageOrBuilder();
               } else {
                  return this.insert_ == null ? MysqlxCrud.Insert.getDefaultInstance() : this.insert_;
               }
            }

            private SingleFieldBuilder<MysqlxCrud.Insert, MysqlxCrud.Insert.Builder, MysqlxCrud.InsertOrBuilder> getInsertFieldBuilder() {
               if (this.insertBuilder_ == null) {
                  this.insertBuilder_ = new SingleFieldBuilder(this.getInsert(), this.getParentForChildren(), this.isClean());
                  this.insert_ = null;
               }

               return this.insertBuilder_;
            }

            @Override
            public boolean hasUpdate() {
               return (this.bitField0_ & 8) != 0;
            }

            @Override
            public MysqlxCrud.Update getUpdate() {
               if (this.updateBuilder_ == null) {
                  return this.update_ == null ? MysqlxCrud.Update.getDefaultInstance() : this.update_;
               } else {
                  return (MysqlxCrud.Update)this.updateBuilder_.getMessage();
               }
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setUpdate(MysqlxCrud.Update value) {
               if (this.updateBuilder_ == null) {
                  if (value == null) {
                     throw new NullPointerException();
                  }

                  this.update_ = value;
               } else {
                  this.updateBuilder_.setMessage(value);
               }

               this.bitField0_ |= 8;
               this.onChanged();
               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setUpdate(MysqlxCrud.Update.Builder builderForValue) {
               if (this.updateBuilder_ == null) {
                  this.update_ = builderForValue.build();
               } else {
                  this.updateBuilder_.setMessage(builderForValue.build());
               }

               this.bitField0_ |= 8;
               this.onChanged();
               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder mergeUpdate(MysqlxCrud.Update value) {
               if (this.updateBuilder_ == null) {
                  if ((this.bitField0_ & 8) != 0 && this.update_ != null && this.update_ != MysqlxCrud.Update.getDefaultInstance()) {
                     this.getUpdateBuilder().mergeFrom(value);
                  } else {
                     this.update_ = value;
                  }
               } else {
                  this.updateBuilder_.mergeFrom(value);
               }

               if (this.update_ != null) {
                  this.bitField0_ |= 8;
                  this.onChanged();
               }

               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder clearUpdate() {
               this.bitField0_ &= -9;
               this.update_ = null;
               if (this.updateBuilder_ != null) {
                  this.updateBuilder_.dispose();
                  this.updateBuilder_ = null;
               }

               this.onChanged();
               return this;
            }

            public MysqlxCrud.Update.Builder getUpdateBuilder() {
               this.bitField0_ |= 8;
               this.onChanged();
               return (MysqlxCrud.Update.Builder)this.getUpdateFieldBuilder().getBuilder();
            }

            @Override
            public MysqlxCrud.UpdateOrBuilder getUpdateOrBuilder() {
               if (this.updateBuilder_ != null) {
                  return (MysqlxCrud.UpdateOrBuilder)this.updateBuilder_.getMessageOrBuilder();
               } else {
                  return this.update_ == null ? MysqlxCrud.Update.getDefaultInstance() : this.update_;
               }
            }

            private SingleFieldBuilder<MysqlxCrud.Update, MysqlxCrud.Update.Builder, MysqlxCrud.UpdateOrBuilder> getUpdateFieldBuilder() {
               if (this.updateBuilder_ == null) {
                  this.updateBuilder_ = new SingleFieldBuilder(this.getUpdate(), this.getParentForChildren(), this.isClean());
                  this.update_ = null;
               }

               return this.updateBuilder_;
            }

            @Override
            public boolean hasDelete() {
               return (this.bitField0_ & 16) != 0;
            }

            @Override
            public MysqlxCrud.Delete getDelete() {
               if (this.deleteBuilder_ == null) {
                  return this.delete_ == null ? MysqlxCrud.Delete.getDefaultInstance() : this.delete_;
               } else {
                  return (MysqlxCrud.Delete)this.deleteBuilder_.getMessage();
               }
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setDelete(MysqlxCrud.Delete value) {
               if (this.deleteBuilder_ == null) {
                  if (value == null) {
                     throw new NullPointerException();
                  }

                  this.delete_ = value;
               } else {
                  this.deleteBuilder_.setMessage(value);
               }

               this.bitField0_ |= 16;
               this.onChanged();
               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setDelete(MysqlxCrud.Delete.Builder builderForValue) {
               if (this.deleteBuilder_ == null) {
                  this.delete_ = builderForValue.build();
               } else {
                  this.deleteBuilder_.setMessage(builderForValue.build());
               }

               this.bitField0_ |= 16;
               this.onChanged();
               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder mergeDelete(MysqlxCrud.Delete value) {
               if (this.deleteBuilder_ == null) {
                  if ((this.bitField0_ & 16) != 0 && this.delete_ != null && this.delete_ != MysqlxCrud.Delete.getDefaultInstance()) {
                     this.getDeleteBuilder().mergeFrom(value);
                  } else {
                     this.delete_ = value;
                  }
               } else {
                  this.deleteBuilder_.mergeFrom(value);
               }

               if (this.delete_ != null) {
                  this.bitField0_ |= 16;
                  this.onChanged();
               }

               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder clearDelete() {
               this.bitField0_ &= -17;
               this.delete_ = null;
               if (this.deleteBuilder_ != null) {
                  this.deleteBuilder_.dispose();
                  this.deleteBuilder_ = null;
               }

               this.onChanged();
               return this;
            }

            public MysqlxCrud.Delete.Builder getDeleteBuilder() {
               this.bitField0_ |= 16;
               this.onChanged();
               return (MysqlxCrud.Delete.Builder)this.getDeleteFieldBuilder().getBuilder();
            }

            @Override
            public MysqlxCrud.DeleteOrBuilder getDeleteOrBuilder() {
               if (this.deleteBuilder_ != null) {
                  return (MysqlxCrud.DeleteOrBuilder)this.deleteBuilder_.getMessageOrBuilder();
               } else {
                  return this.delete_ == null ? MysqlxCrud.Delete.getDefaultInstance() : this.delete_;
               }
            }

            private SingleFieldBuilder<MysqlxCrud.Delete, MysqlxCrud.Delete.Builder, MysqlxCrud.DeleteOrBuilder> getDeleteFieldBuilder() {
               if (this.deleteBuilder_ == null) {
                  this.deleteBuilder_ = new SingleFieldBuilder(this.getDelete(), this.getParentForChildren(), this.isClean());
                  this.delete_ = null;
               }

               return this.deleteBuilder_;
            }

            @Override
            public boolean hasStmtExecute() {
               return (this.bitField0_ & 32) != 0;
            }

            @Override
            public MysqlxSql.StmtExecute getStmtExecute() {
               if (this.stmtExecuteBuilder_ == null) {
                  return this.stmtExecute_ == null ? MysqlxSql.StmtExecute.getDefaultInstance() : this.stmtExecute_;
               } else {
                  return (MysqlxSql.StmtExecute)this.stmtExecuteBuilder_.getMessage();
               }
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setStmtExecute(MysqlxSql.StmtExecute value) {
               if (this.stmtExecuteBuilder_ == null) {
                  if (value == null) {
                     throw new NullPointerException();
                  }

                  this.stmtExecute_ = value;
               } else {
                  this.stmtExecuteBuilder_.setMessage(value);
               }

               this.bitField0_ |= 32;
               this.onChanged();
               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder setStmtExecute(MysqlxSql.StmtExecute.Builder builderForValue) {
               if (this.stmtExecuteBuilder_ == null) {
                  this.stmtExecute_ = builderForValue.build();
               } else {
                  this.stmtExecuteBuilder_.setMessage(builderForValue.build());
               }

               this.bitField0_ |= 32;
               this.onChanged();
               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder mergeStmtExecute(MysqlxSql.StmtExecute value) {
               if (this.stmtExecuteBuilder_ == null) {
                  if ((this.bitField0_ & 32) != 0 && this.stmtExecute_ != null && this.stmtExecute_ != MysqlxSql.StmtExecute.getDefaultInstance()) {
                     this.getStmtExecuteBuilder().mergeFrom(value);
                  } else {
                     this.stmtExecute_ = value;
                  }
               } else {
                  this.stmtExecuteBuilder_.mergeFrom(value);
               }

               if (this.stmtExecute_ != null) {
                  this.bitField0_ |= 32;
                  this.onChanged();
               }

               return this;
            }

            public MysqlxPrepare.Prepare.OneOfMessage.Builder clearStmtExecute() {
               this.bitField0_ &= -33;
               this.stmtExecute_ = null;
               if (this.stmtExecuteBuilder_ != null) {
                  this.stmtExecuteBuilder_.dispose();
                  this.stmtExecuteBuilder_ = null;
               }

               this.onChanged();
               return this;
            }

            public MysqlxSql.StmtExecute.Builder getStmtExecuteBuilder() {
               this.bitField0_ |= 32;
               this.onChanged();
               return (MysqlxSql.StmtExecute.Builder)this.getStmtExecuteFieldBuilder().getBuilder();
            }

            @Override
            public MysqlxSql.StmtExecuteOrBuilder getStmtExecuteOrBuilder() {
               if (this.stmtExecuteBuilder_ != null) {
                  return (MysqlxSql.StmtExecuteOrBuilder)this.stmtExecuteBuilder_.getMessageOrBuilder();
               } else {
                  return this.stmtExecute_ == null ? MysqlxSql.StmtExecute.getDefaultInstance() : this.stmtExecute_;
               }
            }

            private SingleFieldBuilder<MysqlxSql.StmtExecute, MysqlxSql.StmtExecute.Builder, MysqlxSql.StmtExecuteOrBuilder> getStmtExecuteFieldBuilder() {
               if (this.stmtExecuteBuilder_ == null) {
                  this.stmtExecuteBuilder_ = new SingleFieldBuilder(this.getStmtExecute(), this.getParentForChildren(), this.isClean());
                  this.stmtExecute_ = null;
               }

               return this.stmtExecuteBuilder_;
            }
         }

         public static enum Type implements ProtocolMessageEnum {
            FIND(0),
            INSERT(1),
            UPDATE(2),
            DELETE(4),
            STMT(5);

            public static final int FIND_VALUE = 0;
            public static final int INSERT_VALUE = 1;
            public static final int UPDATE_VALUE = 2;
            public static final int DELETE_VALUE = 4;
            public static final int STMT_VALUE = 5;
            private static final EnumLiteMap<MysqlxPrepare.Prepare.OneOfMessage.Type> internalValueMap = new EnumLiteMap<MysqlxPrepare.Prepare.OneOfMessage.Type>() {
               public MysqlxPrepare.Prepare.OneOfMessage.Type findValueByNumber(int number) {
                  return MysqlxPrepare.Prepare.OneOfMessage.Type.forNumber(number);
               }
            };
            private static final MysqlxPrepare.Prepare.OneOfMessage.Type[] VALUES = values();
            private final int value;

            public final int getNumber() {
               return this.value;
            }

            @Deprecated
            public static MysqlxPrepare.Prepare.OneOfMessage.Type valueOf(int value) {
               return forNumber(value);
            }

            public static MysqlxPrepare.Prepare.OneOfMessage.Type forNumber(int value) {
               switch (value) {
                  case 0:
                     return FIND;
                  case 1:
                     return INSERT;
                  case 2:
                     return UPDATE;
                  case 3:
                  default:
                     return null;
                  case 4:
                     return DELETE;
                  case 5:
                     return STMT;
               }
            }

            public static EnumLiteMap<MysqlxPrepare.Prepare.OneOfMessage.Type> internalGetValueMap() {
               return internalValueMap;
            }

            public final EnumValueDescriptor getValueDescriptor() {
               return (EnumValueDescriptor)getDescriptor().getValues().get(this.ordinal());
            }

            public final EnumDescriptor getDescriptorForType() {
               return getDescriptor();
            }

            public static final EnumDescriptor getDescriptor() {
               return (EnumDescriptor)MysqlxPrepare.Prepare.OneOfMessage.getDescriptor().getEnumTypes().get(0);
            }

            public static MysqlxPrepare.Prepare.OneOfMessage.Type valueOf(EnumValueDescriptor desc) {
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
               RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", MysqlxPrepare.Prepare.OneOfMessage.Type.class.getName());
            }
         }
      }

      public interface OneOfMessageOrBuilder extends MessageOrBuilder {
         boolean hasType();

         MysqlxPrepare.Prepare.OneOfMessage.Type getType();

         boolean hasFind();

         MysqlxCrud.Find getFind();

         MysqlxCrud.FindOrBuilder getFindOrBuilder();

         boolean hasInsert();

         MysqlxCrud.Insert getInsert();

         MysqlxCrud.InsertOrBuilder getInsertOrBuilder();

         boolean hasUpdate();

         MysqlxCrud.Update getUpdate();

         MysqlxCrud.UpdateOrBuilder getUpdateOrBuilder();

         boolean hasDelete();

         MysqlxCrud.Delete getDelete();

         MysqlxCrud.DeleteOrBuilder getDeleteOrBuilder();

         boolean hasStmtExecute();

         MysqlxSql.StmtExecute getStmtExecute();

         MysqlxSql.StmtExecuteOrBuilder getStmtExecuteOrBuilder();
      }
   }

   public interface PrepareOrBuilder extends MessageOrBuilder {
      boolean hasStmtId();

      int getStmtId();

      boolean hasStmt();

      MysqlxPrepare.Prepare.OneOfMessage getStmt();

      MysqlxPrepare.Prepare.OneOfMessageOrBuilder getStmtOrBuilder();
   }
}
