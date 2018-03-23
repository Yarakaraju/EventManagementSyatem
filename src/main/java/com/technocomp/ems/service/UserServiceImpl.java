package com.technocomp.ems.service;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.technocomp.ems.model.Role;
import com.technocomp.ems.model.User;
import com.technocomp.ems.repository.RoleRepository;
import com.technocomp.ems.repository.UserRepository;

import javax.persistence.EntityManager; 

/**
 * Created by Ravi Varma Yarakaraj on 12/28/2017.
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    //@Autowired
    //private BCryptPasswordEncoder bCryptPasswordEncoder;

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
       
    @Override
    public void saveUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        short enabled = 0;
        user.setActive(enabled);
        Role userRole = roleRepository.findByRole("ADMIN");
        user.setRole(userRole.getRole());
        userRepository.save(user);
    }

    public User addUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        short enabled = 1;
        user.setActive(enabled);
        Role userRole = roleRepository.findByRole("ADMIN");
        user.setRole(userRole.getRole());
        userRepository.save(user);
        return user;
    }

    public Iterable<User> findNoticeEnrolledPeople(int id) {
        return userRepository.findUsersEnrolledByNoticeID(id);
    }

    @Override
    public User findUserByMobile(String mobile) {
        return userRepository.findUserByMobile(mobile);
    }

    @Override
    public User findUserByMobileAndPassword(String mobile, String otp) {
        return userRepository.findUserByMobileAndPassword(mobile,bCryptPasswordEncoder.encode(otp));
    }

    @Override
    public void enableUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void updateOTPPassword(User user) {
        userRepository.save(user);
    }

   
}
