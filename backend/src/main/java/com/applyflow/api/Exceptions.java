package com.applyflow.api;
import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.time.*; import java.util.*;
class NotFoundException extends RuntimeException { final String code; NotFoundException(String c,String m){super(m);code=c;} }
class ConflictException extends RuntimeException { final String code; ConflictException(String c,String m){super(m);code=c;} }
class UnauthorizedException extends RuntimeException { final String code; UnauthorizedException(String c,String m){super(m);code=c;} }
@RestControllerAdvice class ErrorHandler {
  @ExceptionHandler(NotFoundException.class) ResponseEntity<ApiError> notFound(NotFoundException e){return response(HttpStatus.NOT_FOUND,e.code,e.getMessage(),Map.of());}
  @ExceptionHandler(ConflictException.class) ResponseEntity<ApiError> conflict(ConflictException e){return response(HttpStatus.CONFLICT,e.code,e.getMessage(),Map.of());}
  @ExceptionHandler(UnauthorizedException.class) ResponseEntity<ApiError> unauthorized(UnauthorizedException e){return response(HttpStatus.UNAUTHORIZED,e.code,e.getMessage(),Map.of());}
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e){Map<String,String> fields=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(x->fields.put(x.getField(),x.getDefaultMessage()));return response(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR","Check the highlighted fields",fields);}
  private ResponseEntity<ApiError> response(HttpStatus s,String c,String m,Map<String,String> f){return ResponseEntity.status(s).body(new ApiError(c,m,f,Instant.now()));}
}
