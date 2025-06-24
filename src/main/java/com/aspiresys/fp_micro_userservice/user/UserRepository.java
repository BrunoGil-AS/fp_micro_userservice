package com.aspiresys.fp_micro_userservice.user;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    
    // Custom query method to find a user by email
    Optional<User> findByEmail(String email);
    

}
