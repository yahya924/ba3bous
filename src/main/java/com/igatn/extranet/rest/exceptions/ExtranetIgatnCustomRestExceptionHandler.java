package com.igatn.extranet.rest.exceptions;

import com.igatn.extranet.rest.notifications.model.send.SendNotificationPayload;
import com.igatn.extranet.utils.ExtranetUtils;
import io.micrometer.core.lang.NonNullApi;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * FRE - custom error handler, you can add any exception here and override its default value
 */
@NonNullApi
@ControllerAdvice
public class ExtranetIgatnCustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        //
        final String error = "'"+ ex.getParameterName() + "' parameter is missing";
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST.value(),  error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getCode());
    }
    
    // Common
    @ExceptionHandler({ Exception.class})
    public ResponseEntity<Object> handleAll(final Exception ex) {

        String errorMsg = ex.getLocalizedMessage();
        
        logger.error(ex.getClass().getName() + ":" + errorMsg);

        final ApiError apiError = ExtranetUtils.getApiError(errorMsg);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getCode());
        
    }

    @ExceptionHandler(value = {WsExternalApiNotFoundException.class})
    public ResponseEntity<Object> handleWsExternalApiNotFound(WsExternalApiNotFoundException wsExternalApiNotFoundException, WebRequest request) {
        
        // OJA - Had to change this because HTTP_NO_CONTENT does not return an error, it returns a success response with no content.
        // In the front-end project, this status code does not trigger "isError" in queries and mutations, it triggers "isSuccess", which can be ambiguious.
        return handleExceptionInternal(wsExternalApiNotFoundException,
                wsExternalApiNotFoundException.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // FRE- specific: useful for json parse exceptions
    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String errorMsg = ex.getLocalizedMessage();

        logger.error(ex.getClass().getName() + ":" + errorMsg);

        ApiError apiError = ExtranetUtils.getApiError(errorMsg);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getCode());
    }

    /**
     * global handler for MethodArgumentNotValid exceptions
     * (this is useful when combined with {@link javax.validation.Valid})
     * 
     * @param ex
     * @param headers
     * @param status
     * @param request
     * @return
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        
        String errorMsg = ex.getLocalizedMessage();
        
        // check the case of sending notification
        Class<?> exceptionParamType = ex.getParameter().getParameterType();
        
        if(exceptionParamType.isAssignableFrom(SendNotificationPayload.class)) {
            errorMsg = "/notifications/send: error while reading request body parameters";
        }
        
        logger.error(ex.getClass().getName() + ":" + errorMsg);
        
        ApiError apiError = ExtranetUtils.getApiError(errorMsg);
        
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getCode());
    }
}
