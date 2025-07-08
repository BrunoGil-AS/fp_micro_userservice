package com.aspiresys.fp_micro_userservice.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aspiresys.fp_micro_userservice.aop.annotation.Auditable;
import com.aspiresys.fp_micro_userservice.aop.annotation.ExecutionTime;
import com.aspiresys.fp_micro_userservice.aop.annotation.ValidateParameters;

/**
 * Service implementation for managing User entities.
 * Provides methods for saving, retrieving, updating, and deleting users,
 * as well as checking for user existence by email.
 *
 * <p>This class interacts with the {@link UserRepository} to perform CRUD operations
 * on {@link User} objects.</p>
 *
 * <ul>
 *   <li>{@code saveUser(User user)} - Saves a new user or updates an existing user.</li>
 *   <li>{@code getUserByEmail(String email)} - Retrieves a user by their email address.</li>
 *   <li>{@code getUserById(Long id)} - Retrieves a user by their unique ID.</li>
 *   <li>{@code getAllUsers()} - Retrieves all users.</li>
 *   <li>{@code deleteUserById(Long id)} - Deletes a user by their ID.</li>
 *   <li>{@code deleteUserByEmail(String email)} - Deletes a user by their email address.</li>
 *   <li>{@code updateUser(User user)} - Updates an existing user.</li>
 *   <li>{@code userExistsByEmail(String email)} - Checks if a user exists by email.</li>
 * </ul>
 *
 * @author bruno.gil
 * @see UserService
 * @see UserRepository
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Auditable(operation = "SAVE_USER", entityType = "User", logParameters = true, logResult = false)
    @ExecutionTime(operation = "Save User", warningThreshold = 500, detailed = true)
    @ValidateParameters(notNull = true, message = "User cannot be null")
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @ExecutionTime(operation = "Get User By Email", warningThreshold = 300)
    @ValidateParameters(notNull = true, validateEmail = true, message = "Email cannot be null and must be valid")
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    @ExecutionTime(operation = "Get User By ID", warningThreshold = 200)
    @ValidateParameters(notNull = true, message = "User ID cannot be null")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @ExecutionTime(operation = "Get All Users", warningThreshold = 1000)
    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    @Override
    @Auditable(operation = "DELETE_USER_BY_ID", entityType = "User", logParameters = true, logResult = true)
    @ExecutionTime(operation = "Delete User By ID", warningThreshold = 400)
    @ValidateParameters(notNull = true, message = "User ID cannot be null")
    public boolean deleteUserById(Long id) {
        userRepository.deleteById(id);
        return !userRepository.existsById(id);
    }
    @Override
    @Auditable(operation = "DELETE_USER_BY_EMAIL", entityType = "User", logParameters = true, logResult = true)
    @ExecutionTime(operation = "Delete User By Email", warningThreshold = 600)
    @ValidateParameters(notNull = true, validateEmail = true, message = "Email cannot be null and must be valid")
    public boolean deleteUserByEmail(String email) {
        User user = getUserByEmail(email);
        if (user != null) {
            userRepository.delete(user);
        }
        return !userRepository.findByEmail(email).isPresent();
    }
    @Override
    @Auditable(operation = "UPDATE_USER", entityType = "User", logParameters = true, logResult = false)
    @ExecutionTime(operation = "Update User", warningThreshold = 500, detailed = true)
    @ValidateParameters(notNull = true, message = "User cannot be null")
    public User updateUser(User user) {
        if (user.getId() == null || !userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User does not exist");
        }
        return userRepository.save(user);
    }
    @Override
    @ExecutionTime(operation = "Check User Exists By Email", warningThreshold = 200)
    @ValidateParameters(notNull = true, validateEmail = true, message = "Email cannot be null and must be valid")
    public boolean userExistsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
}
