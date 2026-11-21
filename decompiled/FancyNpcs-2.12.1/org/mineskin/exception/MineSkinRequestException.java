package org.mineskin.exception;

import org.mineskin.response.MineSkinResponse;

public class MineSkinRequestException extends RuntimeException implements IBreadcrumbException {
   private final String code;
   private final MineSkinResponse<?> response;

   public MineSkinRequestException(String code, String message, MineSkinResponse<?> response) {
      super(message);
      this.code = code;
      this.response = response;
   }

   public MineSkinRequestException(String code, String message, MineSkinResponse<?> response, Throwable cause) {
      super(message, cause);
      this.code = code;
      this.response = response;
   }

   public MineSkinResponse<?> getResponse() {
      return this.response;
   }

   @Override
   public String getBreadcrumb() {
      return this.response == null ? null : this.response.getBreadcrumb();
   }
}
