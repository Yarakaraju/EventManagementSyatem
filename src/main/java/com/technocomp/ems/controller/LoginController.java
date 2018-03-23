package com.technocomp.ems.controller;

import com.technocomp.ems.config.SNSConfiguration;
import com.technocomp.ems.model.SMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.technocomp.ems.model.User;
import com.technocomp.ems.service.SMSSenderService;
import com.technocomp.ems.service.UserService;
import com.technocomp.ems.util.CustomErrorType;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by Ravi Varma Yarakaraj on 12/28/2017.
 */
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private SMSSenderService smsSenderService;

    public String getUserdetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = "";
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            currentUserName = authentication.getName();
            return currentUserName;
        }
        return currentUserName;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> login(Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByMobile(auth.getName());
        return new ResponseEntity<User>(
                user, HttpStatus.OK);
    }

    @RequestMapping(value = "/verifyOTP", method = RequestMethod.POST, consumes = {"multipart/form-data"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> verifyOTP(@RequestPart(value = "mobile", required = true) String mobile,
            @RequestPart(value = "otp", required = true) String otp) {
        User user = userService.findUserByMobileAndPassword(mobile, otp);
        if (!user.equals(null)) {
            user.setActive((short) 1);
            userService.enableUser(user);
            SMS sms = new SMS();
            List<String> smsTo = null;
            smsTo.add(mobile);
            sms.setFrom("From EMS APP");
            sms.setTo(smsTo);
            sms.setMessage("Your Mobile verified");
            smsSenderService.sendSMSMessage(sms);
        }
        return new ResponseEntity<User>(
                user, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/sendOTPForLogin", method = RequestMethod.POST, consumes = {"multipart/form-data"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity sendOTPForLogin(@RequestPart(value = "mobile", required = true) String mobile) {
        User user = userService.findUserByMobile(mobile);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (!user.equals(null) && user.getActive() == (short)1) {
            SMS sms = new SMS();
            List<String> smsTo = null;
            String otp = generateOTP();
            smsTo.add(mobile);
            sms.setFrom("From EMS APP");
            sms.setTo(smsTo);
            sms.setMessage("Your OTP for Login " + otp );
            user.setPassword(bCryptPasswordEncoder.encode(otp));
            userService.updateOTPPassword(user);
            smsSenderService.sendSMSMessage(sms);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public ResponseEntity<?> createUser(@RequestPart(value = "email", required = true) String email,
            @RequestPart(value = "name", required = true) String name,
            @RequestPart(value = "mobile", required = true) String mobile,
            @RequestPart(value = "latitude", required = true) String latitude,
            @RequestPart(value = "longitude", required = true) String longitude,
            @RequestPart(value = "last_name", required = true) String last_name,
            UriComponentsBuilder ucBuilder) {
        User user = new User();
        SMS sms = new SMS();
        List<String> smsTo = new ArrayList();
        smsTo.add(mobile);
        
        String otp = generateOTP();
        
        System.out.println("Te generated OTP is "+otp+"  : and SMS to Mobile is :" + smsTo.get(0)); 
        if (userService.findUserByMobile(mobile) != null) {
            return new ResponseEntity(new CustomErrorType("Unable to create. A User with mobile "
                    + mobile + " already exist."), HttpStatus.CONFLICT);
        }
        sms.setFrom("From EMS APP");
        sms.setTo(smsTo);
        sms.setMessage("OTP for EMS App : " + otp);
        smsSenderService.sendSMSMessage(sms);
        user.setName(name);
        user.setEmail(email);
        user.setLastName(last_name);
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        user.setMobile(mobile);
        user.setPassword(otp);
        userService.saveUser(user);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/user/{id}").buildAndExpand(user.getId()).toUri());
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

    /**
     * Method for Generate OTP String
     *
     * @return
     */
    public String generateOTP() {
        int randomPin = (int) (Math.random() * 9000) + 1000;
        String otp = String.valueOf(randomPin);
        return otp;
    }
}
