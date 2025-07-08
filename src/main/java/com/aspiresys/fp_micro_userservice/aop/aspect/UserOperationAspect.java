package com.aspiresys.fp_micro_userservice.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.java.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Aspecto específico para operaciones del dominio de usuarios.
 * 
 * <p>Este aspecto proporciona funcionalidades transversales específicas para
 * el servicio de usuarios, incluyendo logging detallado, métricas de negocio
 * y manejo de casos especiales del dominio.</p>
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *   <li>Logging especializado para operaciones de usuarios</li>
 *   <li>Métricas de negocio (usuarios creados, eliminados, etc.)</li>
 *   <li>Validaciones específicas del dominio</li>
 *   <li>Alertas para operaciones críticas</li>
 *   <li>Seguimiento de patrones de uso</li>
 * </ul>
 * 
 * @author bruno.gil
 * @since 1.0
 */
@Aspect
@Component
@Log
public class UserOperationAspect {

    /**
     * Intercepta operaciones de guardado de usuarios para logging especializado.
     * 
     * @param joinPoint información del punto de unión
     * @return resultado de la operación
     * @throws Throwable si ocurre error durante la ejecución
     */
    @Around("execution(* com.aspiresys.fp_micro_userservice.user.UserServiceImpl.saveUser(..))")
    public Object logUserSaveOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Object[] args = joinPoint.getArgs();
        
        log.info(String.format("USER_OPERATION|timestamp=%s|operation=SAVE_USER|status=STARTING", timestamp));
        
        try {
            Object result = joinPoint.proceed();
            
            log.info(String.format(
                "USER_OPERATION|timestamp=%s|operation=SAVE_USER|status=SUCCESS|result_type=%s",
                timestamp, getResultType(result)
            ));
            
            // Log de métricas de negocio
            logBusinessMetric("USER_CREATED", args.length > 0 ? args[0] : null);
            
            return result;
            
        } catch (Exception e) {
            log.severe(String.format(
                "USER_OPERATION|timestamp=%s|operation=SAVE_USER|status=ERROR|exception=%s|message=%s",
                timestamp, e.getClass().getSimpleName(), e.getMessage()));
            throw e;
        }
    }

    /**
     * Intercepta operaciones de eliminación de usuarios.
     * 
     * @param joinPoint información del punto de unión
     * @return resultado de la operación
     * @throws Throwable si ocurre error durante la ejecución
     */
    @Around("execution(* com.aspiresys.fp_micro_userservice.user.UserServiceImpl.deleteUser*(..))")
    public Object logUserDeleteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.info(String.format("USER_OPERATION|timestamp=%s|operation=%s|status=STARTING", timestamp, methodName.toUpperCase()));
        
        try {
            Object result = joinPoint.proceed();
            
            log.info(String.format(
                "USER_OPERATION|timestamp=%s|operation=%s|status=SUCCESS|result=%s",
                timestamp, methodName.toUpperCase(), result
            ));
            
            // Alertar sobre eliminaciones (operación crítica)
            if (Boolean.TRUE.equals(result)) {
                log.warning(String.format(
                    "USER_ALERT|timestamp=%s|type=USER_DELETED|method=%s|params=%s",
                    timestamp, methodName, formatParameters(args)
                ));
            }
            
            return result;
            
        } catch (Exception e) {
            log.severe(String.format(
                "USER_OPERATION|timestamp=%s|operation=%s|status=ERROR|exception=%s|message=%s",
                timestamp, methodName.toUpperCase(), e.getClass().getSimpleName(), e.getMessage()));
            throw e;
        }
    }

    /**
     * Intercepta operaciones de búsqueda de usuarios para métricas.
     * 
     * @param joinPoint información del punto de unión
     * @param result resultado de la operación
     */
    @AfterReturning(pointcut = "execution(* com.aspiresys.fp_micro_userservice.user.UserServiceImpl.getUser*(..))", returning = "result")
    public void logUserSearchOperation(JoinPoint joinPoint, Object result) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String methodName = joinPoint.getSignature().getName();
        
        String resultInfo = getSearchResultInfo(result);
        
        log.info(String.format(
            "USER_OPERATION|timestamp=%s|operation=%s|status=SUCCESS|%s",
            timestamp, methodName.toUpperCase(), resultInfo
        ));
        
        // Métricas de búsqueda
        if (methodName.equals("getAllUsers") && result instanceof List) {
            int userCount = ((List<?>) result).size();
            logBusinessMetric("USERS_LISTED", userCount);
        }
    }

    /**
     * Intercepta operaciones de actualización de usuarios.
     * 
     * @param joinPoint información del punto de unión
     * @param result resultado de la operación
     */
    @AfterReturning(pointcut = "execution(* com.aspiresys.fp_micro_userservice.user.UserServiceImpl.updateUser(..))", returning = "result")
    public void logUserUpdateOperation(JoinPoint joinPoint, Object result) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        log.info(String.format(
            "USER_OPERATION|timestamp=%s|operation=UPDATE_USER|status=SUCCESS|result_type=%s",
            timestamp, getResultType(result)
        ));
        
        logBusinessMetric("USER_UPDATED", result);
    }

    /**
     * Maneja errores en operaciones de usuarios.
     * 
     * @param joinPoint información del punto de unión
     * @param exception excepción lanzada
     */
    @AfterThrowing(pointcut = "execution(* com.aspiresys.fp_micro_userservice.user.UserServiceImpl.*(..))", throwing = "exception")
    public void handleUserOperationError(JoinPoint joinPoint, Exception exception) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String methodName = joinPoint.getSignature().getName();
        
        log.severe(String.format(
            "USER_ERROR|timestamp=%s|method=%s|exception=%s|message=%s",
            timestamp, methodName, exception.getClass().getSimpleName(), exception.getMessage()
        ));
        
        // Alertas para errores críticos
        if (exception instanceof IllegalArgumentException && 
            (methodName.equals("saveUser") || methodName.equals("updateUser"))) {
            log.warning(String.format(
                "USER_ALERT|timestamp=%s|type=VALIDATION_ERROR|method=%s|error=%s",
                timestamp, methodName, exception.getMessage()
            ));
        }
    }

    /**
     * Determina el tipo de resultado para logging.
     * 
     * @param result resultado del método
     * @return descripción del tipo de resultado
     */
    private String getResultType(Object result) {
        if (result == null) {
            return "null";
        }
        
        String className = result.getClass().getSimpleName();
        
        if (className.equals("ResponseEntity")) {
            return "ResponseEntity";
        }
        
        if (result instanceof List) {
            return String.format("List[%d]", ((List<?>) result).size());
        }
        
        return className;
    }

    /**
     * Obtiene información sobre el resultado de búsquedas.
     * 
     * @param result resultado de la búsqueda
     * @return información formateada del resultado
     */
    private String getSearchResultInfo(Object result) {
        if (result == null) {
            return "result=null";
        }
        
        if (result instanceof List) {
            int size = ((List<?>) result).size();
            return String.format("result_type=List|count=%d", size);
        }
        
        return String.format("result_type=%s|found=true", result.getClass().getSimpleName());
    }

    /**
     * Formatea parámetros para logging seguro.
     * 
     * @param args parámetros del método
     * @return string formateado de parámetros
     */
    private String formatParameters(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        StringBuilder params = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) params.append(", ");
            
            if (args[i] == null) {
                params.append("null");
            } else if (args[i] instanceof String && args[i].toString().contains("@")) {
                // Enmascarar emails parcialmente para privacidad
                String email = args[i].toString();
                int atIndex = email.indexOf("@");
                if (atIndex > 2) {
                    params.append(email.substring(0, 2)).append("***@").append(email.substring(atIndex + 1));
                } else {
                    params.append("***@").append(email.substring(atIndex + 1));
                }
            } else {
                params.append(args[i].getClass().getSimpleName());
            }
        }
        params.append("]");
        return params.toString();
    }

    /**
     * Registra métricas de negocio para análisis posterior.
     * 
     * @param metric tipo de métrica
     * @param data datos asociados
     */
    private void logBusinessMetric(String metric, Object data) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        if (data instanceof Number) {
            log.info(String.format("USER_BUSINESS_METRIC|metric=%s|value=%s|timestamp=%s", 
                    metric, data, timestamp));
        } else {
            log.info(String.format("USER_BUSINESS_METRIC|metric=%s|timestamp=%s", 
                    metric, timestamp));
        }
    }
}
