package com.technocomp.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.technocomp.ems.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by Ravi Varma Yarakaraj on 12/28/2017.
 */
@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    @Query(value = "SELECT * FROM user  where email in (select email from enrole_notice "
            + "where item_id = :id and enrolled=true)", nativeQuery = true)
    public Iterable<User> findUsersEnrolledByNoticeID(@Param("id") int id);

    public User findUserByMobile(String mobile);

    public User findUserByMobileAndPassword(String mobile, String encode);
}
