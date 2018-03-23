/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.technocomp.ems.controller;

import com.technocomp.ems.model.SMS;
import com.technocomp.ems.service.SMSSenderService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Ravi Varma Yarakaraj
 */
@RestController
public class SMSMessageResource{
 
    private static final String MESSAGE_SEND_SUCCESS = "Send SMS Message successfully";
    private static final String MESSAGE_SEND_ERROR = "Send SMS Message unsuccessfully";
    private static final String MESSAGE_RES = "message";
     
    @Autowired
    private SMSSenderService smsSenderService;
 
   
    @RequestMapping(value = "/api/sms", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> sendSMSMessage(@RequestBody final SMS smsDTO) {
        final Map<String, String> response = new HashMap<>();
 
        if (smsSenderService.sendSMSMessage(smsDTO)) {
            response.put(MESSAGE_RES, MESSAGE_SEND_SUCCESS);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put(MESSAGE_RES, MESSAGE_SEND_ERROR);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
 
}