package com.codinglk.photoapp.api.user.data;

import org.springframework.data.repository.CrudRepository;

public interface UsersRepository extends CrudRepository<UserEntity, Long> {

    // findBy and then field name by which you want to find, will generate the select query
    // This is how you can generate queries using JPA
    UserEntity findByEmail(String username);
    UserEntity findByUserId(String userId);
}
