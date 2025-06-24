package com.aspiresys.fp_micro_userservice.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aspiresys.fp_micro_userservice.common.dto.AppResponse;

/**
 * UserController is a REST controller that handles user-related requests.
 * It provides endpoints for user management operations such as creating,
 * retrieving, updating, and deleting users.
 */
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * Hello endpoint.
     * This endpoint returns a simple greeting message.
     * @param email
     * @return
     */
    @GetMapping("/hello")
    public ResponseEntity<AppResponse<String>> hello(String email) {
        return ResponseEntity.ok(new AppResponse<>("Hello, " + email + "!", null));
    }
    
    /**
     * Get a user by their email.
     * This endpoint retrieves a user based on their email address.
     * * It validates the email format and returns a 400 Bad Request if the format is invalid.
     * * If the user is found, it returns a 200 OK response with the user data.
     * * If the user is not found, it returns a 404 Not Found response.
     * 
     * @param email the email of the user to retrieve
     * @return the user with the specified email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<AppResponse<User>> getUserByEmail(@PathVariable String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest()
                .body(new AppResponse<>("Invalid email format", null));
        }
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404)
                .body(new AppResponse<>("User not found", null));
        }
        return ResponseEntity.ok(new AppResponse<>("User found", user));
    }

    /**
     * Create a new user.
     * This endpoint allows the creation of a new user.
     * 
     * @param user the user to create
     * @return the created user
     */
    @PostMapping("/create")
    public ResponseEntity<AppResponse<User>> createUser(User user) {
        if (user == null || user.getEmail() == null || user.getFirstName() == null) {
            return ResponseEntity.badRequest()
                .body(new AppResponse<>("Invalid user data", null));
        }
        User createdUser = userService.saveUser(user);
        return ResponseEntity.ok(new AppResponse<>("User created successfully", createdUser));
    }

    /**
     * Update an existing user.
     * This endpoint allows updating an existing user's information.
     * 
     * @param user the user with updated information
     * @return the updated user
     */
    @GetMapping("/update")
    public ResponseEntity<AppResponse<User>> updateUser(User user) {
        if (user == null || user.getId() == null) {
            return ResponseEntity.badRequest()
                .body(new AppResponse<>("Invalid user data", null));
        }
        User updatedUser = userService.updateUser(user);
        if (updatedUser == null) {
            return ResponseEntity.status(404)
                .body(new AppResponse<>("User not found", null));
        }
        return ResponseEntity.ok(new AppResponse<>("User updated successfully", updatedUser));
    }

    /**
     * Delete a user by their ID.
     * This endpoint allows deleting a user based on their ID.
     * 
     * @param id the ID of the user to delete
     * @return a response indicating the result of the deletion
     */
    @GetMapping("/delete/{id}")
    public ResponseEntity<AppResponse<Boolean>> deleteUser(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest()
                .body(new AppResponse<>("Invalid user ID", Boolean.FALSE));
        }
        boolean isDeleted = userService.deleteUserById(id);
        if (!isDeleted) {
            return ResponseEntity.status(404)
                .body(new AppResponse<>("User not found", Boolean.FALSE));
        }
        return ResponseEntity.ok(new AppResponse<>(String.format("User %d deleted successfully", id), Boolean.TRUE));
    }
        

}
