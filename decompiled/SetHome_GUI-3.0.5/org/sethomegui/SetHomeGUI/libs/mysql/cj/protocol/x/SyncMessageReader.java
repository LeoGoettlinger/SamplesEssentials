package org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.x;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.CJCommunicationsException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.WrongArgumentException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.FullReadInputStream;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.MessageListener;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.MessageReader;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.Protocol;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.x.protobuf.Mysqlx;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.x.protobuf.MysqlxNotice;

public class SyncMessageReader implements MessageReader<XMessageHeader, XMessage> {
   private FullReadInputStream inputStream;
   LinkedList<XMessageHeader> headersQueue = new LinkedList<>();
   LinkedList<Message> messagesQueue = new LinkedList<>();
   BlockingQueue<MessageListener<XMessage>> messageListenerQueue = new LinkedBlockingQueue<>();
   final Lock dispatchingThreadLock = new ReentrantLock();
   final Lock syncOperationLock = new ReentrantLock();
   Thread dispatchingThread = null;
   private Protocol.ProtocolEventHandler protocolEventHandler = null;

   public SyncMessageReader(FullReadInputStream inputStream, Protocol.ProtocolEventHandler protocolEventHandler) {
      this.inputStream = inputStream;
      this.protocolEventHandler = protocolEventHandler;
   }

   public XMessageHeader readHeader() throws IOException {
      this.syncOperationLock.lock();

      XMessageHeader var2;
      try {
         XMessageHeader header;
         if ((header = this.headersQueue.peek()) == null) {
            header = this.readHeaderLocal();
         }

         if (header.getMessageType() == 1) {
            throw new XProtocolError(this.readMessageLocal(Mysqlx.Error.class, true));
         }

         var2 = header;
      } finally {
         this.syncOperationLock.unlock();
      }

      return var2;
   }

   public int getNextNonNoticeMessageType() throws IOException {
      this.syncOperationLock.lock();

      try {
         if (!this.headersQueue.isEmpty()) {
            for (XMessageHeader hdr : this.headersQueue) {
               if (hdr.getMessageType() != 11) {
                  return hdr.getMessageType();
               }
            }
         }

         XMessageHeader header;
         do {
            header = this.readHeaderLocal();
            if (header.getMessageType() == 1) {
               Mysqlx.Error msg;
               this.messagesQueue.addLast(msg = this.readMessageLocal(Mysqlx.Error.class, false));
               throw new XProtocolError(msg);
            }

            if (header.getMessageType() == 11) {
               this.messagesQueue.addLast(this.readMessageLocal(MysqlxNotice.Frame.class, false));
            }
         } while (header.getMessageType() == 11);

         return header.getMessageType();
      } finally {
         this.syncOperationLock.unlock();
      }
   }

   private XMessageHeader readHeaderLocal() throws IOException {
      try {
         byte[] buf = new byte[5];
         this.inputStream.readFully(buf);
         XMessageHeader header = new XMessageHeader(buf);
         this.headersQueue.add(header);
         return header;
      } catch (IOException var3) {
         throw new CJCommunicationsException("Cannot read packet header", var3);
      }
   }

   private <T extends Message> T readMessageLocal(Class<T> messageClass, boolean fromQueue) {
      XMessageHeader header;
      if (fromQueue) {
         header = this.headersQueue.poll();
         T msg = (T)this.messagesQueue.poll();
         if (msg != null) {
            return msg;
         }
      } else {
         header = this.headersQueue.getLast();
      }

      Parser<T> parser = (Parser<T>)MessageConstants.MESSAGE_CLASS_TO_PARSER.get(messageClass);
      byte[] packet = new byte[header.getMessageSize()];

      try {
         this.inputStream.readFully(packet);
      } catch (IOException var10) {
         throw new CJCommunicationsException("Cannot read packet payload", var10);
      }

      try {
         T msg = (T)parser.parseFrom(packet);
         if (msg instanceof MysqlxNotice.Frame
            && ((MysqlxNotice.Frame)msg).getType() == 1
            && ((MysqlxNotice.Frame)msg).getScope() == MysqlxNotice.Frame.Scope.GLOBAL) {
            Notice.XWarning w = new Notice.XWarning((MysqlxNotice.Frame)msg);
            int code = (int)w.getCode();
            if (code == 1053 || code == 1810 || code == 3169) {
               CJCommunicationsException ex = new CJCommunicationsException(w.getMessage());
               ex.setVendorCode(code);
               if (this.protocolEventHandler != null) {
                  this.protocolEventHandler
                     .invokeListeners(
                        code == 1053
                           ? Protocol.ProtocolEventListener.EventType.SERVER_SHUTDOWN
                           : Protocol.ProtocolEventListener.EventType.SERVER_CLOSED_SESSION,
                        ex
                     );
               }

               throw ex;
            }
         }

         return msg;
      } catch (InvalidProtocolBufferException var11) {
         throw new WrongArgumentException(var11);
      }
   }

   public XMessage readMessage(Optional<XMessage> reuse, XMessageHeader hdr) throws IOException {
      return this.readMessage(reuse, hdr.getMessageType());
   }

   public XMessage readMessage(Optional<XMessage> reuse, int expectedType) throws IOException {
      this.syncOperationLock.lock();

      XMessage var7;
      try {
         Class<? extends Message> expectedClass = MessageConstants.getMessageClassForType(expectedType);

         List<Notice> notices;
         XMessageHeader hdr;
         for (notices = null;
            (hdr = this.readHeader()).getMessageType() == 11 && expectedType != 11;
            notices.add(Notice.getInstance(new XMessage(this.readMessageLocal(MessageConstants.getMessageClassForType(11), true))))
         ) {
            if (notices == null) {
               notices = new ArrayList<>();
            }
         }

         Class<? extends Message> messageClass = MessageConstants.getMessageClassForType(hdr.getMessageType());
         if (expectedClass != messageClass) {
            throw new WrongArgumentException(
               "Unexpected message class. Expected '" + expectedClass.getSimpleName() + "' but actually received '" + messageClass.getSimpleName() + "'"
            );
         }

         var7 = new XMessage(this.readMessageLocal(messageClass, true)).addNotices(notices);
      } catch (IOException var11) {
         throw new XProtocolError(var11.getMessage(), var11);
      } finally {
         this.syncOperationLock.unlock();
      }

      return var7;
   }

   @Override
   public void pushMessageListener(MessageListener<XMessage> listener) {
      try {
         this.messageListenerQueue.put(listener);
      } catch (InterruptedException var10) {
         throw new CJCommunicationsException("Cannot queue message listener.", var10);
      }

      this.dispatchingThreadLock.lock();

      try {
         if (this.dispatchingThread == null) {
            SyncMessageReader.ListenersDispatcher ld = new SyncMessageReader.ListenersDispatcher();
            this.dispatchingThread = new Thread(ld, "Message listeners dispatching thread");
            this.dispatchingThread.start();
            int millis = 5000;

            while (!ld.started) {
               try {
                  Thread.sleep(10L);
                  millis -= 10;
               } catch (InterruptedException var9) {
                  throw new XProtocolError(var9.getMessage(), var9);
               }

               if (millis <= 0) {
                  throw new XProtocolError("Timeout for starting ListenersDispatcher exceeded.");
               }
            }
         }
      } finally {
         this.dispatchingThreadLock.unlock();
      }
   }

   private class ListenersDispatcher implements Runnable {
      private static final long POLL_TIMEOUT = 100L;
      boolean started = false;

      public ListenersDispatcher() {
      }

      @Override
      public void run() {
         SyncMessageReader.this.syncOperationLock.lock();

         try {
            this.started = true;

            try {
               while (true) {
                  MessageListener<XMessage> l;
                  while ((l = SyncMessageReader.this.messageListenerQueue.poll(100L, TimeUnit.MILLISECONDS)) != null) {
                     try {
                        XMessage msg = null;

                        while (true) {
                           XMessageHeader hdr = SyncMessageReader.this.readHeader();
                           msg = SyncMessageReader.this.readMessage(null, hdr);
                           if (l.processMessage(msg)) {
                              break;
                           }
                        }
                     } catch (Throwable var13) {
                        l.error(var13);
                     }
                  }

                  SyncMessageReader.this.dispatchingThreadLock.lock();

                  try {
                     if (SyncMessageReader.this.messageListenerQueue.peek() == null) {
                        SyncMessageReader.this.dispatchingThread = null;
                        return;
                     }
                  } finally {
                     SyncMessageReader.this.dispatchingThreadLock.unlock();
                  }
               }
            } catch (InterruptedException var15) {
               throw new CJCommunicationsException("Read operation interrupted.", var15);
            }
         } finally {
            SyncMessageReader.this.syncOperationLock.unlock();
         }
      }
   }
}
