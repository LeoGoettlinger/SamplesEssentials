package org.mineskin.response;

import java.util.List;
import java.util.Optional;
import org.mineskin.data.Breadcrumbed;
import org.mineskin.data.CodeAndMessage;

public interface MineSkinResponse<T> extends Breadcrumbed {
   boolean isSuccess();

   int getStatus();

   List<CodeAndMessage> getMessages();

   Optional<CodeAndMessage> getFirstMessage();

   List<CodeAndMessage> getErrors();

   boolean hasErrors();

   Optional<CodeAndMessage> getFirstError();

   Optional<CodeAndMessage> getErrorOrMessage();

   List<CodeAndMessage> getWarnings();

   Optional<CodeAndMessage> getFirstWarning();

   String getServer();

   @Override
   String getBreadcrumb();

   T getBody();
}
