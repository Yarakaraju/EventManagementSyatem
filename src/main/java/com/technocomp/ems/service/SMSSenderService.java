/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.technocomp.ems.service;

import com.technocomp.ems.model.SMS;

/**
 *
 * @author Ravi Varma Yarakaraj
 */

public interface SMSSenderService {

    public boolean sendSMSMessage(SMS smsDTO);
    
}
