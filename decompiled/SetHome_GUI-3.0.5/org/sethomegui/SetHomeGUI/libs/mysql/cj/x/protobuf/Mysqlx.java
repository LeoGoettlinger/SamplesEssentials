package org.sethomegui.SetHomeGUI.libs.mysql.cj.x.protobuf;

import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.RuntimeVersion;
import com.google.protobuf.UninitializedMessageException;
import com.google.protobuf.AbstractMessage.BuilderParent;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.GeneratedMessage.FieldAccessorTable;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.Internal.EnumLiteMap;
import com.google.protobuf.RuntimeVersion.RuntimeDomain;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class Mysqlx {
   public static final int CLIENT_MESSAGE_ID_FIELD_NUMBER = 100001;
   public static final GeneratedExtension<MessageOptions, Mysqlx.ClientMessages.Type> clientMessageId = GeneratedMessage.newFileScopedGeneratedExtension(
      Mysqlx.ClientMessages.Type.class, null
   );
   public static final int SERVER_MESSAGE_ID_FIELD_NUMBER = 100002;
   public static final GeneratedExtension<MessageOptions, Mysqlx.ServerMessages.Type> serverMessageId = GeneratedMessage.newFileScopedGeneratedExtension(
      Mysqlx.ServerMessages.Type.class, null
   );
   private static final Descriptor internal_static_Mysqlx_ClientMessages_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static final FieldAccessorTable internal_static_Mysqlx_ClientMessages_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_ClientMessages_descriptor, new String[0]
   );
   private static final Descriptor internal_static_Mysqlx_ServerMessages_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static final FieldAccessorTable internal_static_Mysqlx_ServerMessages_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_ServerMessages_descriptor, new String[0]
   );
   private static final Descriptor internal_static_Mysqlx_Ok_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(2);
   private static final FieldAccessorTable internal_static_Mysqlx_Ok_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Ok_descriptor, new String[]{"Msg"}
   );
   private static final Descriptor internal_static_Mysqlx_Error_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(3);
   private static final FieldAccessorTable internal_static_Mysqlx_Error_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Error_descriptor, new String[]{"Severity", "Code", "SqlState", "Msg"}
   );
   private static FileDescriptor descriptor;

   private Mysqlx() {
   }

   public static void registerAllExtensions(ExtensionRegistryLite registry) {
      registry.add(clientMessageId);
      registry.add(serverMessageId);
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
      registerAllExtensions((ExtensionRegistryLite)registry);
   }

   public static FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", Mysqlx.class.getName());
      String[] descriptorData = new String[]{
         "\n\fmysqlx.proto\u0012\u0006Mysqlx\u001a google/protobuf/descriptor.proto\"ü\u0003\n\u000eClientMessages\"é\u0003\n\u0004Type\u0012\u0018\n\u0014CON_CAPABILITIES_GET\u0010\u0001\u0012\u0018\n\u0014CON_CAPABILITIES_SET\u0010\u0002\u0012\r\n\tCON_CLOSE\u0010\u0003\u0012\u001b\n\u0017SESS_AUTHENTICATE_START\u0010\u0004\u0012\u001e\n\u001aSESS_AUTHENTICATE_CONTINUE\u0010\u0005\u0012\u000e\n\nSESS_RESET\u0010\u0006\u0012\u000e\n\nSESS_CLOSE\u0010\u0007\u0012\u0014\n\u0010SQL_STMT_EXECUTE\u0010\f\u0012\r\n\tCRUD_FIND\u0010\u0011\u0012\u000f\n\u000bCRUD_INSERT\u0010\u0012\u0012\u000f\n\u000bCRUD_UPDATE\u0010\u0013\u0012\u000f\n\u000bCRUD_DELETE\u0010\u0014\u0012\u000f\n\u000bEXPECT_OPEN\u0010\u0018\u0012\u0010\n\fEXPECT_CLOSE\u0010\u0019\u0012\u0014\n\u0010CRUD_CREATE_VIEW\u0010\u001e\u0012\u0014\n\u0010CRUD_MODIFY_VIEW\u0010\u001f\u0012\u0012\n\u000eCRUD_DROP_VIEW\u0010 \u0012\u0013\n\u000fPREPARE_PREPARE\u0010(\u0012\u0013\n\u000fPREPARE_EXECUTE\u0010)\u0012\u0016\n\u0012PREPARE_DEALLOCATE\u0010*\u0012\u000f\n\u000bCURSOR_OPEN\u0010+\u0012\u0010\n\fCURSOR_CLOSE\u0010,\u0012\u0010\n\fCURSOR_FETCH\u0010-\u0012\u000f\n\u000bCOMPRESSION\u0010.\"ó\u0002\n\u000eServerMessages\"à\u0002\n\u0004Type\u0012\u0006\n\u0002OK\u0010\u0000\u0012\t\n\u0005ERROR\u0010\u0001\u0012\u0015\n\u0011CONN_CAPABILITIES\u0010\u0002\u0012\u001e\n\u001aSESS_AUTHENTICATE_CONTINUE\u0010\u0003\u0012\u0018\n\u0014SESS_AUTHENTICATE_OK\u0010\u0004\u0012\n\n\u0006NOTICE\u0010\u000b\u0012\u001e\n\u001aRESULTSET_COLUMN_META_DATA\u0010\f\u0012\u0011\n\rRESULTSET_ROW\u0010\r\u0012\u0018\n\u0014RESULTSET_FETCH_DONE\u0010\u000e\u0012\u001d\n\u0019RESULTSET_FETCH_SUSPENDED\u0010\u000f\u0012(\n$RESULTSET_FETCH_DONE_MORE_RESULTSETS\u0010\u0010\u0012\u0017\n\u0013SQL_STMT_EXECUTE_OK\u0010\u0011\u0012(\n$RESULTSET_FETCH_DONE_MORE_OUT_PARAMS\u0010\u0012\u0012\u000f\n\u000bCOMPRESSION\u0010\u0013\"\u0017\n\u0002Ok\u0012\u000b\n\u0003msg\u0018\u0001 \u0001(\t:\u0004\u0090ê0\u0000\"\u008e\u0001\n\u0005Error\u0012/\n\bseverity\u0018\u0001 \u0001(\u000e2\u0016.Mysqlx.Error.Severity:\u0005ERROR\u0012\f\n\u0004code\u0018\u0002 \u0002(\r\u0012\u0011\n\tsql_state\u0018\u0004 \u0002(\t\u0012\u000b\n\u0003msg\u0018\u0003 \u0002(\t\" \n\bSeverity\u0012\t\n\u0005ERROR\u0010\u0000\u0012\t\n\u0005FATAL\u0010\u0001:\u0004\u0090ê0\u0001:Y\n\u0011client_message_id\u0012\u001f.google.protobuf.MessageOptions\u0018¡\u008d\u0006 \u0001(\u000e2\u001b.Mysqlx.ClientMessages.Type:Y\n\u0011server_message_id\u0012\u001f.google.protobuf.MessageOptions\u0018¢\u008d\u0006 \u0001(\u000e2\u001b.Mysqlx.ServerMessages.TypeB\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      descriptor = FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new FileDescriptor[]{DescriptorProtos.getDescriptor()});
      clientMessageId.internalInit((FieldDescriptor)descriptor.getExtensions().get(0));
      serverMessageId.internalInit((FieldDescriptor)descriptor.getExtensions().get(1));
      descriptor.resolveAllFeaturesImmutable();
      DescriptorProtos.getDescriptor();
      ExtensionRegistry registry = ExtensionRegistry.newInstance();
      registry.add(serverMessageId);
      FileDescriptor.internalUpdateFileDescriptor(descriptor, registry);
   }

   public static final class ClientMessages extends GeneratedMessage implements Mysqlx.ClientMessagesOrBuilder {
      private static final long serialVersionUID = 0L;
      private byte memoizedIsInitialized = -1;
      private static final Mysqlx.ClientMessages DEFAULT_INSTANCE = new Mysqlx.ClientMessages();
      private static final Parser<Mysqlx.ClientMessages> PARSER = new AbstractParser<Mysqlx.ClientMessages>() {
         public Mysqlx.ClientMessages parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            Mysqlx.ClientMessages.Builder builder = Mysqlx.ClientMessages.newBuilder();

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

      private ClientMessages(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private ClientMessages() {
      }

      public static final Descriptor getDescriptor() {
         return Mysqlx.internal_static_Mysqlx_ClientMessages_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return Mysqlx.internal_static_Mysqlx_ClientMessages_fieldAccessorTable
            .ensureFieldAccessorsInitialized(Mysqlx.ClientMessages.class, Mysqlx.ClientMessages.Builder.class);
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSize;
         if (size != -1) {
            return size;
         } else {
            int var2 = 0;
            var2 += this.getUnknownFields().getSerializedSize();
            this.memoizedSize = var2;
            return var2;
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof Mysqlx.ClientMessages)) {
            return super.equals(obj);
         } else {
            Mysqlx.ClientMessages other = (Mysqlx.ClientMessages)obj;
            return this.getUnknownFields().equals(other.getUnknownFields());
         }
      }

      @Override
      public int hashCode() {
         if (this.memoizedHashCode != 0) {
            return this.memoizedHashCode;
         } else {
            int hash = 41;
            hash = 19 * hash + getDescriptor().hashCode();
            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static Mysqlx.ClientMessages parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(data);
      }

      public static Mysqlx.ClientMessages parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.ClientMessages parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(data);
      }

      public static Mysqlx.ClientMessages parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.ClientMessages parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(data);
      }

      public static Mysqlx.ClientMessages parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.ClientMessages parseFrom(InputStream input) throws IOException {
         return (Mysqlx.ClientMessages)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static Mysqlx.ClientMessages parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ClientMessages)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static Mysqlx.ClientMessages parseDelimitedFrom(InputStream input) throws IOException {
         return (Mysqlx.ClientMessages)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static Mysqlx.ClientMessages parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ClientMessages)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static Mysqlx.ClientMessages parseFrom(CodedInputStream input) throws IOException {
         return (Mysqlx.ClientMessages)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static Mysqlx.ClientMessages parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ClientMessages)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public Mysqlx.ClientMessages.Builder newBuilderForType() {
         return newBuilder();
      }

      public static Mysqlx.ClientMessages.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static Mysqlx.ClientMessages.Builder newBuilder(Mysqlx.ClientMessages prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public Mysqlx.ClientMessages.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new Mysqlx.ClientMessages.Builder() : new Mysqlx.ClientMessages.Builder().mergeFrom(this);
      }

      protected Mysqlx.ClientMessages.Builder newBuilderForType(BuilderParent parent) {
         return new Mysqlx.ClientMessages.Builder(parent);
      }

      public static Mysqlx.ClientMessages getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<Mysqlx.ClientMessages> parser() {
         return PARSER;
      }

      public Parser<Mysqlx.ClientMessages> getParserForType() {
         return PARSER;
      }

      public Mysqlx.ClientMessages getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", Mysqlx.ClientMessages.class.getName());
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<Mysqlx.ClientMessages.Builder>
         implements Mysqlx.ClientMessagesOrBuilder {
         public static final Descriptor getDescriptor() {
            return Mysqlx.internal_static_Mysqlx_ClientMessages_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return Mysqlx.internal_static_Mysqlx_ClientMessages_fieldAccessorTable
               .ensureFieldAccessorsInitialized(Mysqlx.ClientMessages.class, Mysqlx.ClientMessages.Builder.class);
         }

         private Builder() {
         }

         private Builder(BuilderParent parent) {
            super(parent);
         }

         public Mysqlx.ClientMessages.Builder clear() {
            super.clear();
            return this;
         }

         public Descriptor getDescriptorForType() {
            return Mysqlx.internal_static_Mysqlx_ClientMessages_descriptor;
         }

         public Mysqlx.ClientMessages getDefaultInstanceForType() {
            return Mysqlx.ClientMessages.getDefaultInstance();
         }

         public Mysqlx.ClientMessages build() {
            Mysqlx.ClientMessages result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public Mysqlx.ClientMessages buildPartial() {
            Mysqlx.ClientMessages result = new Mysqlx.ClientMessages(this);
            this.onBuilt();
            return result;
         }

         public Mysqlx.ClientMessages.Builder mergeFrom(Message other) {
            if (other instanceof Mysqlx.ClientMessages) {
               return this.mergeFrom((Mysqlx.ClientMessages)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public Mysqlx.ClientMessages.Builder mergeFrom(Mysqlx.ClientMessages other) {
            if (other == Mysqlx.ClientMessages.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public Mysqlx.ClientMessages.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
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
      }

      public static enum Type implements ProtocolMessageEnum {
         CON_CAPABILITIES_GET(1),
         CON_CAPABILITIES_SET(2),
         CON_CLOSE(3),
         SESS_AUTHENTICATE_START(4),
         SESS_AUTHENTICATE_CONTINUE(5),
         SESS_RESET(6),
         SESS_CLOSE(7),
         SQL_STMT_EXECUTE(12),
         CRUD_FIND(17),
         CRUD_INSERT(18),
         CRUD_UPDATE(19),
         CRUD_DELETE(20),
         EXPECT_OPEN(24),
         EXPECT_CLOSE(25),
         CRUD_CREATE_VIEW(30),
         CRUD_MODIFY_VIEW(31),
         CRUD_DROP_VIEW(32),
         PREPARE_PREPARE(40),
         PREPARE_EXECUTE(41),
         PREPARE_DEALLOCATE(42),
         CURSOR_OPEN(43),
         CURSOR_CLOSE(44),
         CURSOR_FETCH(45),
         COMPRESSION(46);

         public static final int CON_CAPABILITIES_GET_VALUE = 1;
         public static final int CON_CAPABILITIES_SET_VALUE = 2;
         public static final int CON_CLOSE_VALUE = 3;
         public static final int SESS_AUTHENTICATE_START_VALUE = 4;
         public static final int SESS_AUTHENTICATE_CONTINUE_VALUE = 5;
         public static final int SESS_RESET_VALUE = 6;
         public static final int SESS_CLOSE_VALUE = 7;
         public static final int SQL_STMT_EXECUTE_VALUE = 12;
         public static final int CRUD_FIND_VALUE = 17;
         public static final int CRUD_INSERT_VALUE = 18;
         public static final int CRUD_UPDATE_VALUE = 19;
         public static final int CRUD_DELETE_VALUE = 20;
         public static final int EXPECT_OPEN_VALUE = 24;
         public static final int EXPECT_CLOSE_VALUE = 25;
         public static final int CRUD_CREATE_VIEW_VALUE = 30;
         public static final int CRUD_MODIFY_VIEW_VALUE = 31;
         public static final int CRUD_DROP_VIEW_VALUE = 32;
         public static final int PREPARE_PREPARE_VALUE = 40;
         public static final int PREPARE_EXECUTE_VALUE = 41;
         public static final int PREPARE_DEALLOCATE_VALUE = 42;
         public static final int CURSOR_OPEN_VALUE = 43;
         public static final int CURSOR_CLOSE_VALUE = 44;
         public static final int CURSOR_FETCH_VALUE = 45;
         public static final int COMPRESSION_VALUE = 46;
         private static final EnumLiteMap<Mysqlx.ClientMessages.Type> internalValueMap = new EnumLiteMap<Mysqlx.ClientMessages.Type>() {
            public Mysqlx.ClientMessages.Type findValueByNumber(int number) {
               return Mysqlx.ClientMessages.Type.forNumber(number);
            }
         };
         private static final Mysqlx.ClientMessages.Type[] VALUES = values();
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         @Deprecated
         public static Mysqlx.ClientMessages.Type valueOf(int value) {
            return forNumber(value);
         }

         public static Mysqlx.ClientMessages.Type forNumber(int value) {
            switch (value) {
               case 1:
                  return CON_CAPABILITIES_GET;
               case 2:
                  return CON_CAPABILITIES_SET;
               case 3:
                  return CON_CLOSE;
               case 4:
                  return SESS_AUTHENTICATE_START;
               case 5:
                  return SESS_AUTHENTICATE_CONTINUE;
               case 6:
                  return SESS_RESET;
               case 7:
                  return SESS_CLOSE;
               case 8:
               case 9:
               case 10:
               case 11:
               case 13:
               case 14:
               case 15:
               case 16:
               case 21:
               case 22:
               case 23:
               case 26:
               case 27:
               case 28:
               case 29:
               case 33:
               case 34:
               case 35:
               case 36:
               case 37:
               case 38:
               case 39:
               default:
                  return null;
               case 12:
                  return SQL_STMT_EXECUTE;
               case 17:
                  return CRUD_FIND;
               case 18:
                  return CRUD_INSERT;
               case 19:
                  return CRUD_UPDATE;
               case 20:
                  return CRUD_DELETE;
               case 24:
                  return EXPECT_OPEN;
               case 25:
                  return EXPECT_CLOSE;
               case 30:
                  return CRUD_CREATE_VIEW;
               case 31:
                  return CRUD_MODIFY_VIEW;
               case 32:
                  return CRUD_DROP_VIEW;
               case 40:
                  return PREPARE_PREPARE;
               case 41:
                  return PREPARE_EXECUTE;
               case 42:
                  return PREPARE_DEALLOCATE;
               case 43:
                  return CURSOR_OPEN;
               case 44:
                  return CURSOR_CLOSE;
               case 45:
                  return CURSOR_FETCH;
               case 46:
                  return COMPRESSION;
            }
         }

         public static EnumLiteMap<Mysqlx.ClientMessages.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.ordinal());
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)Mysqlx.ClientMessages.getDescriptor().getEnumTypes().get(0);
         }

         public static Mysqlx.ClientMessages.Type valueOf(EnumValueDescriptor desc) {
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
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", Mysqlx.ClientMessages.Type.class.getName());
         }
      }
   }

   public interface ClientMessagesOrBuilder extends MessageOrBuilder {
   }

   public static final class Error extends GeneratedMessage implements Mysqlx.ErrorOrBuilder {
      private static final long serialVersionUID = 0L;
      private int bitField0_;
      public static final int SEVERITY_FIELD_NUMBER = 1;
      private int severity_ = 0;
      public static final int CODE_FIELD_NUMBER = 2;
      private int code_ = 0;
      public static final int SQL_STATE_FIELD_NUMBER = 4;
      private volatile Object sqlState_ = "";
      public static final int MSG_FIELD_NUMBER = 3;
      private volatile Object msg_ = "";
      private byte memoizedIsInitialized = -1;
      private static final Mysqlx.Error DEFAULT_INSTANCE = new Mysqlx.Error();
      private static final Parser<Mysqlx.Error> PARSER = new AbstractParser<Mysqlx.Error>() {
         public Mysqlx.Error parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            Mysqlx.Error.Builder builder = Mysqlx.Error.newBuilder();

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

      private Error(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private Error() {
         this.severity_ = 0;
         this.sqlState_ = "";
         this.msg_ = "";
      }

      public static final Descriptor getDescriptor() {
         return Mysqlx.internal_static_Mysqlx_Error_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return Mysqlx.internal_static_Mysqlx_Error_fieldAccessorTable.ensureFieldAccessorsInitialized(Mysqlx.Error.class, Mysqlx.Error.Builder.class);
      }

      @Override
      public boolean hasSeverity() {
         return (this.bitField0_ & 1) != 0;
      }

      @Override
      public Mysqlx.Error.Severity getSeverity() {
         Mysqlx.Error.Severity result = Mysqlx.Error.Severity.forNumber(this.severity_);
         return result == null ? Mysqlx.Error.Severity.ERROR : result;
      }

      @Override
      public boolean hasCode() {
         return (this.bitField0_ & 2) != 0;
      }

      @Override
      public int getCode() {
         return this.code_;
      }

      @Override
      public boolean hasSqlState() {
         return (this.bitField0_ & 4) != 0;
      }

      @Override
      public String getSqlState() {
         Object ref = this.sqlState_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.sqlState_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getSqlStateBytes() {
         Object ref = this.sqlState_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.sqlState_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public boolean hasMsg() {
         return (this.bitField0_ & 8) != 0;
      }

      @Override
      public String getMsg() {
         Object ref = this.msg_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.msg_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getMsgBytes() {
         Object ref = this.msg_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.msg_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasCode()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (!this.hasSqlState()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (!this.hasMsg()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         if ((this.bitField0_ & 1) != 0) {
            output.writeEnum(1, this.severity_);
         }

         if ((this.bitField0_ & 2) != 0) {
            output.writeUInt32(2, this.code_);
         }

         if ((this.bitField0_ & 8) != 0) {
            GeneratedMessage.writeString(output, 3, this.msg_);
         }

         if ((this.bitField0_ & 4) != 0) {
            GeneratedMessage.writeString(output, 4, this.sqlState_);
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
               size += CodedOutputStream.computeEnumSize(1, this.severity_);
            }

            if ((this.bitField0_ & 2) != 0) {
               size += CodedOutputStream.computeUInt32Size(2, this.code_);
            }

            if ((this.bitField0_ & 8) != 0) {
               size += GeneratedMessage.computeStringSize(3, this.msg_);
            }

            if ((this.bitField0_ & 4) != 0) {
               size += GeneratedMessage.computeStringSize(4, this.sqlState_);
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
         } else if (!(obj instanceof Mysqlx.Error)) {
            return super.equals(obj);
         } else {
            Mysqlx.Error other = (Mysqlx.Error)obj;
            if (this.hasSeverity() != other.hasSeverity()) {
               return false;
            } else if (this.hasSeverity() && this.severity_ != other.severity_) {
               return false;
            } else if (this.hasCode() != other.hasCode()) {
               return false;
            } else if (this.hasCode() && this.getCode() != other.getCode()) {
               return false;
            } else if (this.hasSqlState() != other.hasSqlState()) {
               return false;
            } else if (this.hasSqlState() && !this.getSqlState().equals(other.getSqlState())) {
               return false;
            } else if (this.hasMsg() != other.hasMsg()) {
               return false;
            } else {
               return this.hasMsg() && !this.getMsg().equals(other.getMsg()) ? false : this.getUnknownFields().equals(other.getUnknownFields());
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
            if (this.hasSeverity()) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.severity_;
            }

            if (this.hasCode()) {
               hash = 37 * hash + 2;
               hash = 53 * hash + this.getCode();
            }

            if (this.hasSqlState()) {
               hash = 37 * hash + 4;
               hash = 53 * hash + this.getSqlState().hashCode();
            }

            if (this.hasMsg()) {
               hash = 37 * hash + 3;
               hash = 53 * hash + this.getMsg().hashCode();
            }

            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static Mysqlx.Error parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (Mysqlx.Error)PARSER.parseFrom(data);
      }

      public static Mysqlx.Error parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.Error)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.Error parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (Mysqlx.Error)PARSER.parseFrom(data);
      }

      public static Mysqlx.Error parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.Error)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.Error parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (Mysqlx.Error)PARSER.parseFrom(data);
      }

      public static Mysqlx.Error parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.Error)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.Error parseFrom(InputStream input) throws IOException {
         return (Mysqlx.Error)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static Mysqlx.Error parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Error)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static Mysqlx.Error parseDelimitedFrom(InputStream input) throws IOException {
         return (Mysqlx.Error)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static Mysqlx.Error parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Error)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static Mysqlx.Error parseFrom(CodedInputStream input) throws IOException {
         return (Mysqlx.Error)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static Mysqlx.Error parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Error)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public Mysqlx.Error.Builder newBuilderForType() {
         return newBuilder();
      }

      public static Mysqlx.Error.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static Mysqlx.Error.Builder newBuilder(Mysqlx.Error prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public Mysqlx.Error.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new Mysqlx.Error.Builder() : new Mysqlx.Error.Builder().mergeFrom(this);
      }

      protected Mysqlx.Error.Builder newBuilderForType(BuilderParent parent) {
         return new Mysqlx.Error.Builder(parent);
      }

      public static Mysqlx.Error getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<Mysqlx.Error> parser() {
         return PARSER;
      }

      public Parser<Mysqlx.Error> getParserForType() {
         return PARSER;
      }

      public Mysqlx.Error getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", Mysqlx.Error.class.getName());
      }

      public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Mysqlx.Error.Builder> implements Mysqlx.ErrorOrBuilder {
         private int bitField0_;
         private int severity_ = 0;
         private int code_;
         private Object sqlState_ = "";
         private Object msg_ = "";

         public static final Descriptor getDescriptor() {
            return Mysqlx.internal_static_Mysqlx_Error_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return Mysqlx.internal_static_Mysqlx_Error_fieldAccessorTable.ensureFieldAccessorsInitialized(Mysqlx.Error.class, Mysqlx.Error.Builder.class);
         }

         private Builder() {
         }

         private Builder(BuilderParent parent) {
            super(parent);
         }

         public Mysqlx.Error.Builder clear() {
            super.clear();
            this.bitField0_ = 0;
            this.severity_ = 0;
            this.code_ = 0;
            this.sqlState_ = "";
            this.msg_ = "";
            return this;
         }

         public Descriptor getDescriptorForType() {
            return Mysqlx.internal_static_Mysqlx_Error_descriptor;
         }

         public Mysqlx.Error getDefaultInstanceForType() {
            return Mysqlx.Error.getDefaultInstance();
         }

         public Mysqlx.Error build() {
            Mysqlx.Error result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public Mysqlx.Error buildPartial() {
            Mysqlx.Error result = new Mysqlx.Error(this);
            if (this.bitField0_ != 0) {
               this.buildPartial0(result);
            }

            this.onBuilt();
            return result;
         }

         private void buildPartial0(Mysqlx.Error result) {
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) != 0) {
               result.severity_ = this.severity_;
               to_bitField0_ |= 1;
            }

            if ((from_bitField0_ & 2) != 0) {
               result.code_ = this.code_;
               to_bitField0_ |= 2;
            }

            if ((from_bitField0_ & 4) != 0) {
               result.sqlState_ = this.sqlState_;
               to_bitField0_ |= 4;
            }

            if ((from_bitField0_ & 8) != 0) {
               result.msg_ = this.msg_;
               to_bitField0_ |= 8;
            }

            result.bitField0_ = result.bitField0_ | to_bitField0_;
         }

         public Mysqlx.Error.Builder mergeFrom(Message other) {
            if (other instanceof Mysqlx.Error) {
               return this.mergeFrom((Mysqlx.Error)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public Mysqlx.Error.Builder mergeFrom(Mysqlx.Error other) {
            if (other == Mysqlx.Error.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasSeverity()) {
                  this.setSeverity(other.getSeverity());
               }

               if (other.hasCode()) {
                  this.setCode(other.getCode());
               }

               if (other.hasSqlState()) {
                  this.sqlState_ = other.sqlState_;
                  this.bitField0_ |= 4;
                  this.onChanged();
               }

               if (other.hasMsg()) {
                  this.msg_ = other.msg_;
                  this.bitField0_ |= 8;
                  this.onChanged();
               }

               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasCode()) {
               return false;
            } else {
               return !this.hasSqlState() ? false : this.hasMsg();
            }
         }

         public Mysqlx.Error.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
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
                           Mysqlx.Error.Severity tmpValue = Mysqlx.Error.Severity.forNumber(tmpRaw);
                           if (tmpValue == null) {
                              this.mergeUnknownVarintField(1, tmpRaw);
                           } else {
                              this.severity_ = tmpRaw;
                              this.bitField0_ |= 1;
                           }
                           break;
                        case 16:
                           this.code_ = input.readUInt32();
                           this.bitField0_ |= 2;
                           break;
                        case 26:
                           this.msg_ = input.readBytes();
                           this.bitField0_ |= 8;
                           break;
                        case 34:
                           this.sqlState_ = input.readBytes();
                           this.bitField0_ |= 4;
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
         public boolean hasSeverity() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public Mysqlx.Error.Severity getSeverity() {
            Mysqlx.Error.Severity result = Mysqlx.Error.Severity.forNumber(this.severity_);
            return result == null ? Mysqlx.Error.Severity.ERROR : result;
         }

         public Mysqlx.Error.Builder setSeverity(Mysqlx.Error.Severity value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.severity_ = value.getNumber();
               this.onChanged();
               return this;
            }
         }

         public Mysqlx.Error.Builder clearSeverity() {
            this.bitField0_ &= -2;
            this.severity_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasCode() {
            return (this.bitField0_ & 2) != 0;
         }

         @Override
         public int getCode() {
            return this.code_;
         }

         public Mysqlx.Error.Builder setCode(int value) {
            this.code_ = value;
            this.bitField0_ |= 2;
            this.onChanged();
            return this;
         }

         public Mysqlx.Error.Builder clearCode() {
            this.bitField0_ &= -3;
            this.code_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasSqlState() {
            return (this.bitField0_ & 4) != 0;
         }

         @Override
         public String getSqlState() {
            Object ref = this.sqlState_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.sqlState_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getSqlStateBytes() {
            Object ref = this.sqlState_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.sqlState_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public Mysqlx.Error.Builder setSqlState(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.sqlState_ = value;
               this.bitField0_ |= 4;
               this.onChanged();
               return this;
            }
         }

         public Mysqlx.Error.Builder clearSqlState() {
            this.sqlState_ = Mysqlx.Error.getDefaultInstance().getSqlState();
            this.bitField0_ &= -5;
            this.onChanged();
            return this;
         }

         public Mysqlx.Error.Builder setSqlStateBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.sqlState_ = value;
               this.bitField0_ |= 4;
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasMsg() {
            return (this.bitField0_ & 8) != 0;
         }

         @Override
         public String getMsg() {
            Object ref = this.msg_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.msg_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getMsgBytes() {
            Object ref = this.msg_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.msg_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public Mysqlx.Error.Builder setMsg(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.msg_ = value;
               this.bitField0_ |= 8;
               this.onChanged();
               return this;
            }
         }

         public Mysqlx.Error.Builder clearMsg() {
            this.msg_ = Mysqlx.Error.getDefaultInstance().getMsg();
            this.bitField0_ &= -9;
            this.onChanged();
            return this;
         }

         public Mysqlx.Error.Builder setMsgBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.msg_ = value;
               this.bitField0_ |= 8;
               this.onChanged();
               return this;
            }
         }
      }

      public static enum Severity implements ProtocolMessageEnum {
         ERROR(0),
         FATAL(1);

         public static final int ERROR_VALUE = 0;
         public static final int FATAL_VALUE = 1;
         private static final EnumLiteMap<Mysqlx.Error.Severity> internalValueMap = new EnumLiteMap<Mysqlx.Error.Severity>() {
            public Mysqlx.Error.Severity findValueByNumber(int number) {
               return Mysqlx.Error.Severity.forNumber(number);
            }
         };
         private static final Mysqlx.Error.Severity[] VALUES = values();
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         @Deprecated
         public static Mysqlx.Error.Severity valueOf(int value) {
            return forNumber(value);
         }

         public static Mysqlx.Error.Severity forNumber(int value) {
            switch (value) {
               case 0:
                  return ERROR;
               case 1:
                  return FATAL;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<Mysqlx.Error.Severity> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.ordinal());
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)Mysqlx.Error.getDescriptor().getEnumTypes().get(0);
         }

         public static Mysqlx.Error.Severity valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private Severity(int value) {
            this.value = value;
         }

         static {
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", Mysqlx.Error.Severity.class.getName());
         }
      }
   }

   public interface ErrorOrBuilder extends MessageOrBuilder {
      boolean hasSeverity();

      Mysqlx.Error.Severity getSeverity();

      boolean hasCode();

      int getCode();

      boolean hasSqlState();

      String getSqlState();

      ByteString getSqlStateBytes();

      boolean hasMsg();

      String getMsg();

      ByteString getMsgBytes();
   }

   public static final class Ok extends GeneratedMessage implements Mysqlx.OkOrBuilder {
      private static final long serialVersionUID = 0L;
      private int bitField0_;
      public static final int MSG_FIELD_NUMBER = 1;
      private volatile Object msg_ = "";
      private byte memoizedIsInitialized = -1;
      private static final Mysqlx.Ok DEFAULT_INSTANCE = new Mysqlx.Ok();
      private static final Parser<Mysqlx.Ok> PARSER = new AbstractParser<Mysqlx.Ok>() {
         public Mysqlx.Ok parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            Mysqlx.Ok.Builder builder = Mysqlx.Ok.newBuilder();

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

      private Ok(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private Ok() {
         this.msg_ = "";
      }

      public static final Descriptor getDescriptor() {
         return Mysqlx.internal_static_Mysqlx_Ok_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return Mysqlx.internal_static_Mysqlx_Ok_fieldAccessorTable.ensureFieldAccessorsInitialized(Mysqlx.Ok.class, Mysqlx.Ok.Builder.class);
      }

      @Override
      public boolean hasMsg() {
         return (this.bitField0_ & 1) != 0;
      }

      @Override
      public String getMsg() {
         Object ref = this.msg_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.msg_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getMsgBytes() {
         Object ref = this.msg_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.msg_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         if ((this.bitField0_ & 1) != 0) {
            GeneratedMessage.writeString(output, 1, this.msg_);
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
               size += GeneratedMessage.computeStringSize(1, this.msg_);
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
         } else if (!(obj instanceof Mysqlx.Ok)) {
            return super.equals(obj);
         } else {
            Mysqlx.Ok other = (Mysqlx.Ok)obj;
            if (this.hasMsg() != other.hasMsg()) {
               return false;
            } else {
               return this.hasMsg() && !this.getMsg().equals(other.getMsg()) ? false : this.getUnknownFields().equals(other.getUnknownFields());
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
            if (this.hasMsg()) {
               hash = 37 * hash + 1;
               hash = 53 * hash + this.getMsg().hashCode();
            }

            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static Mysqlx.Ok parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (Mysqlx.Ok)PARSER.parseFrom(data);
      }

      public static Mysqlx.Ok parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.Ok)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.Ok parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (Mysqlx.Ok)PARSER.parseFrom(data);
      }

      public static Mysqlx.Ok parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.Ok)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.Ok parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (Mysqlx.Ok)PARSER.parseFrom(data);
      }

      public static Mysqlx.Ok parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.Ok)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.Ok parseFrom(InputStream input) throws IOException {
         return (Mysqlx.Ok)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static Mysqlx.Ok parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Ok)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static Mysqlx.Ok parseDelimitedFrom(InputStream input) throws IOException {
         return (Mysqlx.Ok)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static Mysqlx.Ok parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Ok)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static Mysqlx.Ok parseFrom(CodedInputStream input) throws IOException {
         return (Mysqlx.Ok)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static Mysqlx.Ok parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Ok)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public Mysqlx.Ok.Builder newBuilderForType() {
         return newBuilder();
      }

      public static Mysqlx.Ok.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static Mysqlx.Ok.Builder newBuilder(Mysqlx.Ok prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public Mysqlx.Ok.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new Mysqlx.Ok.Builder() : new Mysqlx.Ok.Builder().mergeFrom(this);
      }

      protected Mysqlx.Ok.Builder newBuilderForType(BuilderParent parent) {
         return new Mysqlx.Ok.Builder(parent);
      }

      public static Mysqlx.Ok getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<Mysqlx.Ok> parser() {
         return PARSER;
      }

      public Parser<Mysqlx.Ok> getParserForType() {
         return PARSER;
      }

      public Mysqlx.Ok getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", Mysqlx.Ok.class.getName());
      }

      public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Mysqlx.Ok.Builder> implements Mysqlx.OkOrBuilder {
         private int bitField0_;
         private Object msg_ = "";

         public static final Descriptor getDescriptor() {
            return Mysqlx.internal_static_Mysqlx_Ok_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return Mysqlx.internal_static_Mysqlx_Ok_fieldAccessorTable.ensureFieldAccessorsInitialized(Mysqlx.Ok.class, Mysqlx.Ok.Builder.class);
         }

         private Builder() {
         }

         private Builder(BuilderParent parent) {
            super(parent);
         }

         public Mysqlx.Ok.Builder clear() {
            super.clear();
            this.bitField0_ = 0;
            this.msg_ = "";
            return this;
         }

         public Descriptor getDescriptorForType() {
            return Mysqlx.internal_static_Mysqlx_Ok_descriptor;
         }

         public Mysqlx.Ok getDefaultInstanceForType() {
            return Mysqlx.Ok.getDefaultInstance();
         }

         public Mysqlx.Ok build() {
            Mysqlx.Ok result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public Mysqlx.Ok buildPartial() {
            Mysqlx.Ok result = new Mysqlx.Ok(this);
            if (this.bitField0_ != 0) {
               this.buildPartial0(result);
            }

            this.onBuilt();
            return result;
         }

         private void buildPartial0(Mysqlx.Ok result) {
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) != 0) {
               result.msg_ = this.msg_;
               to_bitField0_ |= 1;
            }

            result.bitField0_ = result.bitField0_ | to_bitField0_;
         }

         public Mysqlx.Ok.Builder mergeFrom(Message other) {
            if (other instanceof Mysqlx.Ok) {
               return this.mergeFrom((Mysqlx.Ok)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public Mysqlx.Ok.Builder mergeFrom(Mysqlx.Ok other) {
            if (other == Mysqlx.Ok.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasMsg()) {
                  this.msg_ = other.msg_;
                  this.bitField0_ |= 1;
                  this.onChanged();
               }

               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public Mysqlx.Ok.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
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
                           this.msg_ = input.readBytes();
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
         public boolean hasMsg() {
            return (this.bitField0_ & 1) != 0;
         }

         @Override
         public String getMsg() {
            Object ref = this.msg_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.msg_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getMsgBytes() {
            Object ref = this.msg_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.msg_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public Mysqlx.Ok.Builder setMsg(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.msg_ = value;
               this.bitField0_ |= 1;
               this.onChanged();
               return this;
            }
         }

         public Mysqlx.Ok.Builder clearMsg() {
            this.msg_ = Mysqlx.Ok.getDefaultInstance().getMsg();
            this.bitField0_ &= -2;
            this.onChanged();
            return this;
         }

         public Mysqlx.Ok.Builder setMsgBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.msg_ = value;
               this.bitField0_ |= 1;
               this.onChanged();
               return this;
            }
         }
      }
   }

   public interface OkOrBuilder extends MessageOrBuilder {
      boolean hasMsg();

      String getMsg();

      ByteString getMsgBytes();
   }

   public static final class ServerMessages extends GeneratedMessage implements Mysqlx.ServerMessagesOrBuilder {
      private static final long serialVersionUID = 0L;
      private byte memoizedIsInitialized = -1;
      private static final Mysqlx.ServerMessages DEFAULT_INSTANCE = new Mysqlx.ServerMessages();
      private static final Parser<Mysqlx.ServerMessages> PARSER = new AbstractParser<Mysqlx.ServerMessages>() {
         public Mysqlx.ServerMessages parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            Mysqlx.ServerMessages.Builder builder = Mysqlx.ServerMessages.newBuilder();

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

      private ServerMessages(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
      }

      private ServerMessages() {
      }

      public static final Descriptor getDescriptor() {
         return Mysqlx.internal_static_Mysqlx_ServerMessages_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return Mysqlx.internal_static_Mysqlx_ServerMessages_fieldAccessorTable
            .ensureFieldAccessorsInitialized(Mysqlx.ServerMessages.class, Mysqlx.ServerMessages.Builder.class);
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSize;
         if (size != -1) {
            return size;
         } else {
            int var2 = 0;
            var2 += this.getUnknownFields().getSerializedSize();
            this.memoizedSize = var2;
            return var2;
         }
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (!(obj instanceof Mysqlx.ServerMessages)) {
            return super.equals(obj);
         } else {
            Mysqlx.ServerMessages other = (Mysqlx.ServerMessages)obj;
            return this.getUnknownFields().equals(other.getUnknownFields());
         }
      }

      @Override
      public int hashCode() {
         if (this.memoizedHashCode != 0) {
            return this.memoizedHashCode;
         } else {
            int hash = 41;
            hash = 19 * hash + getDescriptor().hashCode();
            hash = 29 * hash + this.getUnknownFields().hashCode();
            this.memoizedHashCode = hash;
            return hash;
         }
      }

      public static Mysqlx.ServerMessages parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(data);
      }

      public static Mysqlx.ServerMessages parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.ServerMessages parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(data);
      }

      public static Mysqlx.ServerMessages parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.ServerMessages parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(data);
      }

      public static Mysqlx.ServerMessages parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.ServerMessages parseFrom(InputStream input) throws IOException {
         return (Mysqlx.ServerMessages)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static Mysqlx.ServerMessages parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ServerMessages)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public static Mysqlx.ServerMessages parseDelimitedFrom(InputStream input) throws IOException {
         return (Mysqlx.ServerMessages)GeneratedMessage.parseDelimitedWithIOException(PARSER, input);
      }

      public static Mysqlx.ServerMessages parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ServerMessages)GeneratedMessage.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
      }

      public static Mysqlx.ServerMessages parseFrom(CodedInputStream input) throws IOException {
         return (Mysqlx.ServerMessages)GeneratedMessage.parseWithIOException(PARSER, input);
      }

      public static Mysqlx.ServerMessages parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ServerMessages)GeneratedMessage.parseWithIOException(PARSER, input, extensionRegistry);
      }

      public Mysqlx.ServerMessages.Builder newBuilderForType() {
         return newBuilder();
      }

      public static Mysqlx.ServerMessages.Builder newBuilder() {
         return DEFAULT_INSTANCE.toBuilder();
      }

      public static Mysqlx.ServerMessages.Builder newBuilder(Mysqlx.ServerMessages prototype) {
         return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }

      public Mysqlx.ServerMessages.Builder toBuilder() {
         return this == DEFAULT_INSTANCE ? new Mysqlx.ServerMessages.Builder() : new Mysqlx.ServerMessages.Builder().mergeFrom(this);
      }

      protected Mysqlx.ServerMessages.Builder newBuilderForType(BuilderParent parent) {
         return new Mysqlx.ServerMessages.Builder(parent);
      }

      public static Mysqlx.ServerMessages getDefaultInstance() {
         return DEFAULT_INSTANCE;
      }

      public static Parser<Mysqlx.ServerMessages> parser() {
         return PARSER;
      }

      public Parser<Mysqlx.ServerMessages> getParserForType() {
         return PARSER;
      }

      public Mysqlx.ServerMessages getDefaultInstanceForType() {
         return DEFAULT_INSTANCE;
      }

      static {
         RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", Mysqlx.ServerMessages.class.getName());
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<Mysqlx.ServerMessages.Builder>
         implements Mysqlx.ServerMessagesOrBuilder {
         public static final Descriptor getDescriptor() {
            return Mysqlx.internal_static_Mysqlx_ServerMessages_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return Mysqlx.internal_static_Mysqlx_ServerMessages_fieldAccessorTable
               .ensureFieldAccessorsInitialized(Mysqlx.ServerMessages.class, Mysqlx.ServerMessages.Builder.class);
         }

         private Builder() {
         }

         private Builder(BuilderParent parent) {
            super(parent);
         }

         public Mysqlx.ServerMessages.Builder clear() {
            super.clear();
            return this;
         }

         public Descriptor getDescriptorForType() {
            return Mysqlx.internal_static_Mysqlx_ServerMessages_descriptor;
         }

         public Mysqlx.ServerMessages getDefaultInstanceForType() {
            return Mysqlx.ServerMessages.getDefaultInstance();
         }

         public Mysqlx.ServerMessages build() {
            Mysqlx.ServerMessages result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public Mysqlx.ServerMessages buildPartial() {
            Mysqlx.ServerMessages result = new Mysqlx.ServerMessages(this);
            this.onBuilt();
            return result;
         }

         public Mysqlx.ServerMessages.Builder mergeFrom(Message other) {
            if (other instanceof Mysqlx.ServerMessages) {
               return this.mergeFrom((Mysqlx.ServerMessages)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public Mysqlx.ServerMessages.Builder mergeFrom(Mysqlx.ServerMessages other) {
            if (other == Mysqlx.ServerMessages.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               this.onChanged();
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public Mysqlx.ServerMessages.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
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
      }

      public static enum Type implements ProtocolMessageEnum {
         OK(0),
         ERROR(1),
         CONN_CAPABILITIES(2),
         SESS_AUTHENTICATE_CONTINUE(3),
         SESS_AUTHENTICATE_OK(4),
         NOTICE(11),
         RESULTSET_COLUMN_META_DATA(12),
         RESULTSET_ROW(13),
         RESULTSET_FETCH_DONE(14),
         RESULTSET_FETCH_SUSPENDED(15),
         RESULTSET_FETCH_DONE_MORE_RESULTSETS(16),
         SQL_STMT_EXECUTE_OK(17),
         RESULTSET_FETCH_DONE_MORE_OUT_PARAMS(18),
         COMPRESSION(19);

         public static final int OK_VALUE = 0;
         public static final int ERROR_VALUE = 1;
         public static final int CONN_CAPABILITIES_VALUE = 2;
         public static final int SESS_AUTHENTICATE_CONTINUE_VALUE = 3;
         public static final int SESS_AUTHENTICATE_OK_VALUE = 4;
         public static final int NOTICE_VALUE = 11;
         public static final int RESULTSET_COLUMN_META_DATA_VALUE = 12;
         public static final int RESULTSET_ROW_VALUE = 13;
         public static final int RESULTSET_FETCH_DONE_VALUE = 14;
         public static final int RESULTSET_FETCH_SUSPENDED_VALUE = 15;
         public static final int RESULTSET_FETCH_DONE_MORE_RESULTSETS_VALUE = 16;
         public static final int SQL_STMT_EXECUTE_OK_VALUE = 17;
         public static final int RESULTSET_FETCH_DONE_MORE_OUT_PARAMS_VALUE = 18;
         public static final int COMPRESSION_VALUE = 19;
         private static final EnumLiteMap<Mysqlx.ServerMessages.Type> internalValueMap = new EnumLiteMap<Mysqlx.ServerMessages.Type>() {
            public Mysqlx.ServerMessages.Type findValueByNumber(int number) {
               return Mysqlx.ServerMessages.Type.forNumber(number);
            }
         };
         private static final Mysqlx.ServerMessages.Type[] VALUES = values();
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         @Deprecated
         public static Mysqlx.ServerMessages.Type valueOf(int value) {
            return forNumber(value);
         }

         public static Mysqlx.ServerMessages.Type forNumber(int value) {
            switch (value) {
               case 0:
                  return OK;
               case 1:
                  return ERROR;
               case 2:
                  return CONN_CAPABILITIES;
               case 3:
                  return SESS_AUTHENTICATE_CONTINUE;
               case 4:
                  return SESS_AUTHENTICATE_OK;
               case 5:
               case 6:
               case 7:
               case 8:
               case 9:
               case 10:
               default:
                  return null;
               case 11:
                  return NOTICE;
               case 12:
                  return RESULTSET_COLUMN_META_DATA;
               case 13:
                  return RESULTSET_ROW;
               case 14:
                  return RESULTSET_FETCH_DONE;
               case 15:
                  return RESULTSET_FETCH_SUSPENDED;
               case 16:
                  return RESULTSET_FETCH_DONE_MORE_RESULTSETS;
               case 17:
                  return SQL_STMT_EXECUTE_OK;
               case 18:
                  return RESULTSET_FETCH_DONE_MORE_OUT_PARAMS;
               case 19:
                  return COMPRESSION;
            }
         }

         public static EnumLiteMap<Mysqlx.ServerMessages.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.ordinal());
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)Mysqlx.ServerMessages.getDescriptor().getEnumTypes().get(0);
         }

         public static Mysqlx.ServerMessages.Type valueOf(EnumValueDescriptor desc) {
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
            RuntimeVersion.validateProtobufGencodeVersion(RuntimeDomain.PUBLIC, 4, 26, 1, "", Mysqlx.ServerMessages.Type.class.getName());
         }
      }
   }

   public interface ServerMessagesOrBuilder extends MessageOrBuilder {
   }
}
