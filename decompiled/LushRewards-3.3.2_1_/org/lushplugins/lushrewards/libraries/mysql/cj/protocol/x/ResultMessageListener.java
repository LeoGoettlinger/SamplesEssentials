package org.lushplugins.lushrewards.libraries.mysql.cj.protocol.x;

import com.google.protobuf.GeneratedMessageV3;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.WrongArgumentException;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.MessageListener;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntity;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntityFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ResultBuilder;
import org.lushplugins.lushrewards.libraries.mysql.cj.x.protobuf.Mysqlx;

public class ResultMessageListener<R> implements MessageListener<XMessage> {
   private ResultBuilder<?> resultBuilder;
   private CompletableFuture<R> future;
   private Map<Class<? extends GeneratedMessageV3>, ProtocolEntityFactory<? extends ProtocolEntity, XMessage>> messageToProtocolEntityFactory = new HashMap<>();

   public ResultMessageListener(
      Map<Class<? extends GeneratedMessageV3>, ProtocolEntityFactory<? extends ProtocolEntity, XMessage>> messageToProtocolEntityFactory,
      ResultBuilder<R> resultBuilder,
      CompletableFuture<R> future
   ) {
      this.messageToProtocolEntityFactory = messageToProtocolEntityFactory;
      this.resultBuilder = resultBuilder;
      this.future = future;
   }

   public boolean processMessage(XMessage message) {
      Class<? extends GeneratedMessageV3> msgClass = (Class<? extends GeneratedMessageV3>)message.getMessage().getClass();
      if (Mysqlx.Error.class.equals(msgClass)) {
         this.future.completeExceptionally(new XProtocolError(Mysqlx.Error.class.cast(message.getMessage())));
      } else if (!this.messageToProtocolEntityFactory.containsKey(msgClass)) {
         this.future.completeExceptionally(new WrongArgumentException("Unhandled msg class (" + msgClass + ") + msg=" + message.getMessage()));
      } else {
         if (!this.resultBuilder.addProtocolEntity(this.messageToProtocolEntityFactory.get(msgClass).createFromMessage(message))) {
            return false;
         }

         this.future.complete((R)this.resultBuilder.build());
      }

      return true;
   }

   @Override
   public void error(Throwable ex) {
      this.future.completeExceptionally(ex);
   }
}
