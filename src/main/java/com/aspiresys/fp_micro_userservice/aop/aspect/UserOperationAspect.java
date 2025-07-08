package com.aspiresys.fp_micro_userservice.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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
        
        // Log estructurado multilínea - inicio
        log.info("╔═══════════════════════════════════════════════════════════════════════════════════════");
        log.info("║ USER SAVE OPERATION - STARTING");
        log.info("╠═══════════════════════════════════════════════════════════════════════════════════════");
        log.info("├─ Timestamp: {}", timestamp);
        log.info("├─ Operation: SAVE_USER");
        log.info("├─ Method: {}", joinPoint.getSignature().getName());
        log.info("├─ Class: {}", joinPoint.getTarget().getClass().getSimpleName());
        log.info("├─ Arguments count: {}", args.length);
        if (args.length > 0 && args[0] != null) {
            log.info("├─ User data: {}", formatUserData(args[0]));
        }
        log.info("╚═══════════════════════════════════════════════════════════════════════════════════════");
        
        try {
            Object result = joinPoint.proceed();
            
            // Log estructurado multilínea - éxito
            log.info("╔═══════════════════════════════════════════════════════════════════════════════════════");
            log.info("║ USER SAVE OPERATION - SUCCESS");
            log.info("╠═══════════════════════════════════════════════════════════════════════════════════════");
            log.info("├─ Timestamp: {}", timestamp);
            log.info("├─ Operation: SAVE_USER");
            log.info("├─ Result type: {}", getResultType(result));
            log.info("├─ Status: SUCCESS");
            log.info("╚═══════════════════════════════════════════════════════════════════════════════════════");
            
            // Log de métricas de negocio
            logBusinessMetric("USER_CREATED", args.length > 0 ? args[0] : null);
            
            return result;
            
        } catch (Exception e) {
            // Log estructurado multilínea - error
            log.error("╔═══════════════════════════════════════════════════════════════════════════════════════");
            log.error("║ USER SAVE OPERATION - ERROR");
            log.error("╠═══════════════════════════════════════════════════════════════════════════════════════");
            log.error("├─ Timestamp: {}", timestamp);
            log.error("├─ Operation: SAVE_USER");
            log.error("├─ Exception: {}", e.getClass().getSimpleName());
            log.error("├─ Message: {}", e.getMessage());
            log.error("├─ Status: ERROR");
            log.error("╚═══════════════════════════════════════════════════════════════════════════════════════");
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
        
        // Log estructurado multilínea - inicio
        log.info("╔═══════════════════════════════════════════════════════════════════════════════════════");
        log.info("║ USER DELETE OPERATION - STARTING");
        log.info("╠═══════════════════════════════════════════════════════════════════════════════════════");
        log.info("├─ Timestamp: {}", timestamp);
        log.info("├─ Operation: {}", methodName.toUpperCase());
        log.info("├─ Method: {}", methodName);
        log.info("├─ Arguments: {}", formatParameters(args));
        log.info("╚═══════════════════════════════════════════════════════════════════════════════════════");
        
        try {
            Object result = joinPoint.proceed();
            
            // Log estructurado multilínea - éxito
            log.info("╔═══════════════════════════════════════════════════════════════════════════════════════");
            log.info("║ USER DELETE OPERATION - SUCCESS");
            log.info("╠═══════════════════════════════════════════════════════════════════════════════════════");
            log.info("├─ Timestamp: {}", timestamp);
            log.info("├─ Operation: {}", methodName.toUpperCase());
            log.info("├─ Result: {}", result);
            log.info("├─ Status: SUCCESS");
            log.info("╚═══════════════════════════════════════════════════════════════════════════════════════");
            
            // Alertar sobre eliminaciones (operación crítica)
            if (Boolean.TRUE.equals(result)) {
                log.warn("╔═══════════════════════════════════════════════════════════════════════════════════════");
                log.warn("║ USER ALERT - USER DELETED");
                log.warn("╠═══════════════════════════════════════════════════════════════════════════════════════");
                log.warn("├─ Timestamp: {}", timestamp);
                log.warn("├─ Type: USER_DELETED");
                log.warn("├─ Method: {}", methodName);
                log.warn("├─ Parameters: {}", formatParameters(args));
                log.warn("╚═══════════════════════════════════════════════════════════════════════════════════════");
            }
            
            return result;
            
        } catch (Exception e) {
            // Log estructurado multilínea - error
            log.error("╔═══════════════════════════════════════════════════════════════════════════════════════");
            log.error("║ USER DELETE OPERATION - ERROR");
            log.error("╠═══════════════════════════════════════════════════════════════════════════════════════");
            log.error("├─ Timestamp: {}", timestamp);
            log.error("├─ Operation: {}", methodName.toUpperCase());
            log.error("├─ Exception: {}", e.getClass().getSimpleName());
            log.error("├─ Message: {}", e.getMessage());
            log.error("├─ Status: ERROR");
            log.error("╚═══════════════════════════════════════════════════════════════════════════════════════");
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
        
        // Log estructurado multilínea - búsqueda
        log.info("╔═══════════════════════════════════════════════════════════════════════════════════════");
        log.info("║ USER SEARCH OPERATION - SUCCESS");
        log.info("╠═══════════════════════════════════════════════════════════════════════════════════════");
        log.info("├─ Timestamp: {}", timestamp);
        log.info("├─ Operation: {}", methodName.toUpperCase());
        log.info("├─ {}", resultInfo);
        log.info("├─ Status: SUCCESS");
        log.info("╚═══════════════════════════════════════════════════════════════════════════════════════");
        
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
        
        // Log estructurado multilínea - actualización
        log.info("╔═══════════════════════════════════════════════════════════════════════════════════════");
        log.info("║ USER UPDATE OPERATION - SUCCESS");
        log.info("╠═══════════════════════════════════════════════════════════════════════════════════════");
        log.info("├─ Timestamp: {}", timestamp);
        log.info("├─ Operation: UPDATE_USER");
        log.info("├─ Result type: {}", getResultType(result));
        log.info("├─ Status: SUCCESS");
        log.info("╚═══════════════════════════════════════════════════════════════════════════════════════");
        
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
        
        // Log estructurado multilínea - error general
        log.error("╔═══════════════════════════════════════════════════════════════════════════════════════");
        log.error("║ USER OPERATION ERROR");
        log.error("╠═══════════════════════════════════════════════════════════════════════════════════════");
        log.error("├─ Timestamp: {}", timestamp);
        log.error("├─ Method: {}", methodName);
        log.error("├─ Exception: {}", exception.getClass().getSimpleName());
        log.error("├─ Message: {}", exception.getMessage());
        log.error("╚═══════════════════════════════════════════════════════════════════════════════════════");
        
        // Alertas para errores críticos
        if (exception instanceof IllegalArgumentException && 
            (methodName.equals("saveUser") || methodName.equals("updateUser"))) {
            log.warn("╔═══════════════════════════════════════════════════════════════════════════════════════");
            log.warn("║ USER VALIDATION ALERT");
            log.warn("╠═══════════════════════════════════════════════════════════════════════════════════════");
            log.warn("├─ Timestamp: {}", timestamp);
            log.warn("├─ Type: VALIDATION_ERROR");
            log.warn("├─ Method: {}", methodName);
            log.warn("├─ Error: {}", exception.getMessage());
            log.warn("╚═══════════════════════════════════════════════════════════════════════════════════════");
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
     * Formatea datos de usuario para logging seguro.
     * 
     * @param userData datos del usuario
     * @return string formateado de datos de usuario
     */
    private String formatUserData(Object userData) {
        if (userData == null) {
            return "null";
        }
        
        try {
            // Usar reflection de forma segura para obtener información básica
            String className = userData.getClass().getSimpleName();
            String toString = userData.toString();
            
            // Si contiene información sensible, enmascararla
            if (toString.contains("@")) {
                toString = toString.replaceAll("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b", "***@***.***");
            }
            
            return String.format("%s: %s", className, toString.length() > 100 ? 
                toString.substring(0, 100) + "..." : toString);
                
        } catch (Exception e) {
            return userData.getClass().getSimpleName() + ": [data masked for security]";
        }
    }

    /**
     * Registra métricas de negocio para análisis posterior.
     * 
     * @param metric tipo de métrica
     * @param data datos asociados
     */
    private void logBusinessMetric(String metric, Object data) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // Log estructurado multilínea - métrica de negocio
        log.info("╔═══════════════════════════════════════════════════════════════════════════════════════");
        log.info("║ USER BUSINESS METRIC");
        log.info("╠═══════════════════════════════════════════════════════════════════════════════════════");
        log.info("├─ Timestamp: {}", timestamp);
        log.info("├─ Metric: {}", metric);
        if (data instanceof Number) {
            log.info("├─ Value: {}", data);
        } else if (data != null) {
            log.info("├─ Data Type: {}", data.getClass().getSimpleName());
        }
        log.info("╚═══════════════════════════════════════════════════════════════════════════════════════");
    }
}
