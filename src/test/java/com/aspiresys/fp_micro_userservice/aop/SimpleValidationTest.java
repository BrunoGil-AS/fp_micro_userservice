package com.aspiresys.fp_micro_userservice.aop;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.aspiresys.fp_micro_userservice.user.User;
import com.aspiresys.fp_micro_userservice.user.UserService;

/**
 * Test simple para verificar que los aspectos de validación funcionan.
 */
@SpringBootTest
public class SimpleValidationTest {

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
        assertNotNull(userService);
    }

    @Test
    void testSaveUserWithValidUser() {
        // Arrange
        User validUser = createValidUser();

        // Act & Assert - No debería lanzar excepción
        assertDoesNotThrow(() -> {
            userService.saveUser(validUser);
        });
    }

    @Test
    void testSaveUserWithNullUser() {
        // Act & Assert - Debería lanzar IllegalArgumentException
        Exception exception = assertThrows(Exception.class, () -> {
            userService.saveUser(null);
        });
        
        // Verificar que se lanzó alguna excepción
        assertNotNull(exception);
        System.out.println("Exception type: " + exception.getClass().getSimpleName());
        System.out.println("Exception message: " + exception.getMessage());
    }

    @Test
    void testGetUserByIdWithNullId() {
        // Act & Assert - Debería lanzar IllegalArgumentException
        Exception exception = assertThrows(Exception.class, () -> {
            userService.getUserById(null);
        });
        
        // Verificar que se lanzó alguna excepción
        assertNotNull(exception);
        System.out.println("Exception type: " + exception.getClass().getSimpleName());
        System.out.println("Exception message: " + exception.getMessage());
    }

    @Test
    void testGetUserByEmailWithInvalidEmail() {
        // Act & Assert - Debería lanzar IllegalArgumentException
        Exception exception = assertThrows(Exception.class, () -> {
            userService.getUserByEmail("invalid-email-format");
        });
        
        // Verificar que se lanzó alguna excepción
        assertNotNull(exception);
        System.out.println("Exception type: " + exception.getClass().getSimpleName());
        System.out.println("Exception message: " + exception.getMessage());
    }

    private User createValidUser() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        return user;
    }
}
