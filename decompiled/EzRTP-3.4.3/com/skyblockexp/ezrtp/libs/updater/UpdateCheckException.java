package com.skyblockexp.ezrtp.libs.updater;

public class UpdateCheckException extends Exception {
   private final UpdateCheckException.ErrorType errorType;

   public UpdateCheckException(UpdateCheckException.ErrorType errorType, String message) {
      super(message);
      this.errorType = errorType;
   }

   public UpdateCheckException(UpdateCheckException.ErrorType errorType, String message, Throwable cause) {
      super(message, cause);
      this.errorType = errorType;
   }

   public UpdateCheckException.ErrorType getErrorType() {
      return this.errorType;
   }

   public static enum ErrorType {
      NOT_FOUND,
      UNAUTHORIZED,
      RATE_LIMITED,
      NETWORK_ERROR,
      TIMEOUT,
      INVALID_RESPONSE,
      BAD_REQUEST,
      UNKNOWN_HTTP_ERROR;
   }
}
