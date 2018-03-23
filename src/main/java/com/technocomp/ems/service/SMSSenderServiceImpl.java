/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.technocomp.ems.service;

/**
 *
 * @author Ravi Varma Yarakaraj
 */
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.*;
import com.google.common.base.Strings;
import com.technocomp.ems.config.SNSConfiguration;
import com.technocomp.ems.model.SMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import org.springframework.util.CollectionUtils;

@Service
public class SMSSenderServiceImpl implements SMSSenderService {

    @Autowired
    SNSConfiguration snsConfiguration;
    private static final String MESSAGE_DEFAULT = "Default SMS message.";

    @Value("${aws.sns.SMSType}")
    private String smsType;
    @Value("${aws.sns.phoneNumberRegex}")
    private String phoneNumberRegex;
    @Value("${aws.sns.senderIDRegex}")
    private String senderIDRegex;
    @Value("${aws.sns.senderID}")
    private String senderID;

    String regex = "^\\+[0-9]{1,3}\\.[0-9]{4,14}(?:x.+)?$";
    Pattern pattern = Pattern.compile(regex);

    String regexStr = "^(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$";

    /* Phone Number formats: (nnn)nnn-nnnn; nnnnnnnnnn; nnn-nnn-nnnn
	^\\(? : May start with an option "(" .
	(\\d{3}): Followed by 3 digits.
	\\)? : May have an optional ")" 
	[- ]? : May have an optional "-" after the first 3 digits or after optional ) character. 
	(\\d{3}) : Followed by 3 digits. 
	 [- ]? : May have another optional "-" after numeric digits.
	 (\\d{4})$ : ends with four digits.
         Examples: Matches following phone numbers:
         (123)456-7890, 123-456-7890, 1234567890, (123)-456-7890
     */
//Initialize reg ex for phone number. 
    String expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";

    private static final Logger LOGGER = LoggerFactory.getLogger(SMSSenderServiceImpl.class);
    private final AmazonSNS mysnsClient;
    //private AmazonSNS snsClient = snsConfiguration.snsClient();

    @Autowired
    public SMSSenderServiceImpl(AmazonSNS snsClient) {
        this.mysnsClient = snsClient;
    }

    @Override
    public boolean sendSMSMessage(final SMS smsDTO) {
        System.out.println(" Inside Send SMS " + mysnsClient.getTopicAttributes("arn:aws:sns:us-east-2:518730092639:ems"));
        List<String> phoneNumbers = smsDTO.getTo();
        try {
            if (!CollectionUtils.isEmpty(phoneNumbers)) {

                // Filter valid phone numbers and prevent no duplicated. 
                /*phoneNumbers = phoneNumbers.stream()
                       .filter(p -> isValidPhoneNumber(p)).distinct().collect(toList());*/

                // Prepare for SNS Client environment like MessageAttributeValue default.
                Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
                //Map<String, MessageAttributeValue> smsAttributes = setSmsAttributes(smsDTO);
                 System.out.println("Current phone number: "+ smsDTO.getMessage() + " : number of phone numbers to send are  "+ phoneNumbers.size());
                // Send SMS message for each phone numbers.
                phoneNumbers.forEach(p -> sendSMSMessage(mysnsClient,
                        smsMessageBuilder(smsDTO), p, smsAttributes));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Got error while sending sms {}", e);
            return false;
        }
        return true;
    }

    private String smsMessageBuilder(final SMS smsDTO) {
        final StringBuilder builder = new StringBuilder();
        String message = smsDTO.getMessage();
        System.out.println("Inside message builder "+ message);
        if (Strings.isNullOrEmpty(message)) {
            builder.append(MessageFormat.format("{0}. ", MESSAGE_DEFAULT));
        } else {
            builder.append(MessageFormat.format("{0}.", message));
        }
        //TODO Put more message content here

        return builder.toString();
    }

    private void sendSMSMessage(AmazonSNS snsClient, String message,
            String phoneNumber, Map<String, MessageAttributeValue> smsAttributes) {
        System.out.println(" In The Send message and message is  is" + smsAttributes.toString());
        
        try {
            isValidPhoneNumber(phoneNumber);
            System.out.println(" the phone number   is" + phoneNumber);
            PublishResult result = snsClient.publish(new PublishRequest()
                    .withMessage(message)
                    .withPhoneNumber(phoneNumber)
                    .withMessageAttributes(smsAttributes));
            System.out.println(" The message ID is " + result.getMessageId());
            System.out.println("My result Value id " + result.toString());
            LOGGER.debug("The message ID {}", result);
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private Map<String, MessageAttributeValue> setSmsAttributes(SMS shareCampDTO) {
        Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();

        senderID = Strings.isNullOrEmpty(senderID)
                ? genSenderID(shareCampDTO.getFrom()) : senderID;
        // According to Amazon, SenderID must be 1-11 alpha-numeric characters
        smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
                .withStringValue(senderID)
                .withDataType("String"));

        smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
                .withStringValue(smsType)
                .withDataType("String"));
        return smsAttributes;
    }

    /**
     * E.164 is a standard for the phone number structure used for international
     * telecommunication. Phone numbers that follow this format can have a
     * maximum of 15 digits, and they are prefixed with the plus character (+)
     * and the country code. For example, a U.S. phone number in E.164 format
     * would appear as +1XXX5550100.
     *
     * @See at
     * http://docs.aws.amazon.com/sns/latest/dg/sms_publish-to-phone.html
     * @see
     * <a href="http://docs.aws.amazon.com/sns/latest/dg/sms_publish-to-phone.html">
     * Sending an SMS Message</a>
     * @param phoneNumber String
     * @return true if valid, otherwise false.
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        System.out.println("Inside a valid phone number and number is" + phoneNumber);
        return Pattern.matches(phoneNumberRegex, phoneNumber);
    }

    /**
     * Because Amazon SNS requires that SenderID must be 1-11 alpha-numeric
     * characters. So we try to generate a correct SenderID.
     *
     * @see
     * <a href="http://docs.aws.amazon.com/sns/latest/dg/sms_supported-countries.html"></a>
     */
    private String genSenderID(String phoneNumber) {
        String sid = phoneNumber.replaceAll(senderIDRegex, "");
        return sid.length() > 11 ? sid.substring(sid.length() - 11, sid.length()) : sid;
    }
}
