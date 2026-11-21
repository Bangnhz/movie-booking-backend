package com.moviebooking.movie_booking.repository;

import com.moviebooking.movie_booking.entity.UserRoleEntity;
import com.moviebooking.movie_booking.entity.embedded_id.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId> {
    @Modifying
    @Query("DELETE FROM UserRoleEntity ur WHERE ur.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
