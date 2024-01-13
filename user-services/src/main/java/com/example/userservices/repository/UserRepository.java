package com.example.userservices.repository;

import com.example.userservices.dto.UserEntityResponseDto;
import com.example.userservices.model.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Meta(comment = "find user by username")
    UserEntity findByUsername(String username);

    @Meta(comment = "find user by username ignore case")
    UserEntity findByUsernameIgnoreCase(String username);

    @Meta(comment = "find user by username ending with param")
    List<UserEntity> findUserEntitiesByNameContainingIgnoreCase(String username, Pageable pageable);
//    @Query("select u from #{#entityName} u where u.username like %:username%")
    @Meta(comment = "find by email")
    UserEntity findByEmail(String email);
    @Meta(comment = "find top 10 by username contain and order by username desc")
    Slice<UserEntityResponseDto> findTop10ByUsernameContainingOrderByUsernameDesc(String username, Pageable pageable);

    @Meta(comment = "call stored procedure find user by name")
    @Query(value = "CALL FIND_USER_BY_NAME(?1);", nativeQuery = true)
//    @Procedure(name = "User.getUserByName", procedureName = "FIND_USER_BY_NAME", value = "name") //Lỗi index .. out of bounds for length ..
    List<UserEntityResponseDto> findAllByName(@Param("name") String name);
}
