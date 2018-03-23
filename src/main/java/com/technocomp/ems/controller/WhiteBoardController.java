package com.technocomp.ems.controller;

import com.technocomp.ems.model.EnroleNotice;
import com.technocomp.ems.model.Messages;
import com.technocomp.ems.model.Notices;
import com.technocomp.ems.model.WhiteBoard;
import com.technocomp.ems.service.EnroleNoticeService;
import com.technocomp.ems.service.MessagesService;
import com.technocomp.ems.service.UserService;
import com.technocomp.ems.service.WhiteBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@Controller
public class WhiteBoardController {

    @Autowired
    WhiteBoardService whiteBoardService;

    @Autowired
    EnroleNoticeService enroleNoticeService;

    @Autowired
    UserService userService;

    @Autowired
    MessagesService messagesService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy h:mm");
        sdf.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    public String getUserdetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = "";
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            currentUserName = authentication.getName();
            return currentUserName;
        }
        return currentUserName;
    }

    @RequestMapping(value = "/noticesPostedByMe", method = RequestMethod.GET)
    public List<WhiteBoard> getBasicDetailsWhiteBoard() {
        return whiteBoardService.getNoticesByUserID(getUserdetails());
    }

    @RequestMapping(value = "/privateMessages", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<Messages>> getPrivateMessages(@RequestParam(value = "latitudefornotice") String latitudefornotice,
            @RequestParam(value = "longitudefornotice") String longitudefornotice,
            @RequestParam(value = "maxRadius") int maxRadius) {
        return new ResponseEntity<Iterable<Messages>>((messagesService.getMessages(getUserdetails(), "p")), HttpStatus.OK);
    }

    @RequestMapping(value = "/groupMessages", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<Messages>> getGroupMessages(@RequestParam(value = "latitudefornotice") String latitudefornotice,
            @RequestParam(value = "longitudefornotice") String longitudefornotice,
            @RequestParam(value = "maxRadius") int maxRadius) {
        return new ResponseEntity<Iterable<Messages>>((messagesService.getMessages(getUserdetails(), "g")), HttpStatus.OK);
    }

    @RequestMapping(value = "/registerNewNotice", method = RequestMethod.POST)
    public ResponseEntity<?> registerNewNotice(
            @RequestPart(value = "itemTitle", required = true) String itemTitle,
            @RequestPart(value = "itemDescription", required = true) String itemDescription,
            @RequestPart(value = "mobile", required = true) String mobile,
            @RequestPart(value = "maxPratispents", required = true) int maxPratispents,
            @RequestPart(value = "latitude", required = true) String latitude,
            @RequestPart(value = "longitude", required = true) String longitude,
            @RequestPart(value = "location", required = true) String location,
            @RequestPart(value = "city", required = true) String city,
            @RequestPart(value = "state", required = true) String state,
            @RequestPart(value = "allowedRadius", required = true) String allowedRadius,
            UriComponentsBuilder ucBuilder) {

        WhiteBoard whiteBoard = new WhiteBoard();
        whiteBoard.setAllowedRadius(allowedRadius);
        whiteBoard.setCity(city);
        whiteBoard.setState(state);
        whiteBoard.setItemDescription(itemDescription);
        whiteBoard.setItemTitle(itemTitle);
        whiteBoard.setLatitude(latitude);
        whiteBoard.setLongitude(longitude);
        whiteBoard.setMaxPratispents(maxPratispents);
        whiteBoard.setEmail(getUserdetails());
        whiteBoard.setLocation(location);
        
        whiteBoardService.addNotice(whiteBoard);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/user/{id}").buildAndExpand(whiteBoard.getId()).toUri());
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/myWhiteBoard", method = RequestMethod.POST)
    public ModelAndView createNewNotice(@Valid WhiteBoard whiteBoard, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("myWhiteBoard");
        } else {
            whiteBoard.setEmail(getUserdetails());
            whiteBoardService.addNotice(whiteBoard);
            modelAndView.addObject("successMessage", "Item  has been registered successfully");
            modelAndView.addObject("whiteBoard", new WhiteBoard());

        }
        modelAndView.addObject("myPrivateMessages", messagesService.getMessages(getUserdetails(), "p"));
        modelAndView.addObject("myGroupMessages", messagesService.getMessages(getUserdetails(), "g"));
        modelAndView.addObject("notices", new Notices());
        modelAndView.setViewName("myWhiteBoard");
        modelAndView.addObject("whiteBoardNotices", whiteBoardService.getNoticesByUserID(getUserdetails()));
        return modelAndView;
    }

    @RequestMapping(value = "/whiteBoard/delete/{id}", method = RequestMethod.POST)
    public ModelAndView deleteNotice(@PathVariable Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        WhiteBoard whiteBoard = whiteBoardService.findWhiteBoardByID(id);
        boolean delete = whiteBoardService.deleteWhiteBoard(whiteBoard);
        if (delete) {
            modelAndView.addObject("deleteMessage", "Notice has been deleted successfully");
        } else {
            modelAndView.addObject("deleteMessage", "Notice deletion failed");
        }
        modelAndView.addObject("myPrivateMessages", messagesService.getMessages(getUserdetails(), "p"));
        modelAndView.addObject("myGroupMessages", messagesService.getMessages(getUserdetails(), "g"));
        modelAndView.addObject("whiteBoard", whiteBoard);
        modelAndView.addObject("notices", new Notices());
        modelAndView.addObject("whiteBoardNotices", whiteBoardService.getNoticesByUserID(getUserdetails()));
        modelAndView.setViewName("myWhiteBoard");

        return modelAndView;
    }

    @RequestMapping(value = "/notices", method = RequestMethod.POST)
    public ModelAndView listOfNoticesToEnrole(HttpSession httpSession, HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "latitudefornotice") String latitudefornotice,
            @RequestParam(value = "longitudefornotice") String longitudefornotice,
            @RequestParam(value = "maxRadius") int maxRadius) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("notices", new Notices());
        httpSession.setAttribute("latitudefornotice", latitudefornotice);
        httpSession.setAttribute("longitudefornotice", longitudefornotice);
        httpSession.setAttribute("maxRadius", maxRadius);
        modelAndView.addObject("noticesToApprove", enroleNoticeService.findEnroleNoticeForApproval(getUserdetails()));
        modelAndView.addObject("noticesAlreadyEnrolled",
                enroleNoticeService.findEnroleNoticeByEmail(getUserdetails()));
        modelAndView.addObject("noticesNearByLocation",
                whiteBoardService.getNoticesByLocation(latitudefornotice, longitudefornotice, maxRadius, getUserdetails()));
        modelAndView.setViewName("enroleNotices");

        return modelAndView;
    }

    @RequestMapping(value = "/listUsersDetails/{id}", method = RequestMethod.GET)
    public ModelAndView individualNoticeDetails(
            @PathVariable(value = "id") int id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("individualNoticeDetails", userService.findNoticeEnrolledPeople(id));
        modelAndView.setViewName("noticeDetails");
        return modelAndView;
    }

    @RequestMapping(value = "/enroleNotices/enrole/{id}", method = RequestMethod.POST)
    public ModelAndView enrollNotice(@PathVariable Integer id, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        EnroleNotice enroleNotice = enroleNoticeService.findEnroleNoticeByItemIdAndEmail(id, getUserdetails());;
        WhiteBoard whiteBoard = whiteBoardService.findWhiteBoardByID(id);
        HttpSession httpSession = request.getSession();
        String latitudefornotice = httpSession.getAttribute("latitudefornotice").toString();
        String longitudefornotice = httpSession.getAttribute("longitudefornotice").toString();
        int maxRadius = (int) httpSession.getAttribute("maxRadius");
        if (whiteBoard.getTotalEnrolled() != whiteBoard.getMaxPratispents()) {
            if (enroleNotice == null) {
                enroleNotice = new EnroleNotice();
                enroleNotice.setItemId(whiteBoard.getId());
                enroleNotice.setItemTitle(whiteBoard.getItemTitle());
                enroleNotice.setItemDescription(whiteBoard.getItemDescription());
                enroleNotice.setEnrolled(false);
                enroleNotice.setEmail(getUserdetails());
                // whiteBoard.setTotalEnrolled(whiteBoard.getTotalEnrolled() + 1);
                // whiteBoardService.addNotice(whiteBoard);
                enroleNoticeService.save(enroleNotice);
                modelAndView.addObject("enroleMessage", "Notice has been enroled successfully waiting for approval by owner");
            } else {
                modelAndView.addObject("enroleMessage", "You already enrolled this event ");
            }
        } else {
            modelAndView.addObject("enroleMessage", "Max paricipeants reached! ");
        }

        modelAndView.addObject("noticesToApprove", enroleNoticeService.findEnroleNoticeForApproval(getUserdetails()));
        modelAndView.addObject("noticesAlreadyEnrolled",
                enroleNoticeService.findEnroleNoticeByEmail(getUserdetails()));
        modelAndView.addObject("noticesNearByLocation", whiteBoardService.getNoticesByLocation(latitudefornotice,
                longitudefornotice, maxRadius, getUserdetails()));
        modelAndView.setViewName("enroleNotices");
        return modelAndView;
    }

    @RequestMapping(value = "/enroleNotices/approve/{id}", method = RequestMethod.POST)
    public ModelAndView approveEnrollNotice(@PathVariable Integer id, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView();
        EnroleNotice enroleNotice = enroleNoticeService.findEnroleNoticeById(id);
        WhiteBoard whiteBoard = whiteBoardService.findWhiteBoardByID(enroleNotice.getItemId());
        HttpSession httpSession = request.getSession();
        String latitudefornotice = httpSession.getAttribute("latitudefornotice").toString();
        String longitudefornotice = httpSession.getAttribute("longitudefornotice").toString();
        int maxRadius = (int) httpSession.getAttribute("maxRadius");
        if (enroleNotice != null) {
            enroleNotice.setEnrolled(true);
            whiteBoard.setTotalEnrolled(whiteBoard.getTotalEnrolled() + 1);
            whiteBoardService.addNotice(whiteBoard);
            enroleNoticeService.save(enroleNotice);
            modelAndView.addObject("enroleMessage", "Notice has been approved successfully");
        } else {
            modelAndView.addObject("enroleMessage", "You already enrolled this Notice ");
        }

        modelAndView.addObject("noticesToApprove", enroleNoticeService.findEnroleNoticeForApproval(getUserdetails()));
        modelAndView.addObject("noticesAlreadyEnrolled", enroleNoticeService.findEnroleNoticeByEmail(getUserdetails()));
        modelAndView.addObject("noticesNearByLocation", whiteBoardService.getNoticesByLocation(latitudefornotice,
                longitudefornotice, maxRadius, getUserdetails()));
        modelAndView.setViewName("enroleNotices");
        return modelAndView;
    }

    @RequestMapping(value = "/enroleNotices/rejectWithPrivateMessage/{id}", method = RequestMethod.POST)
    public ModelAndView rejectEnrollNotice(@PathVariable Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        EnroleNotice enroleNotice = enroleNoticeService.findEnroleNoticeById(id);
        modelAndView.addObject("enroleNotice", enroleNotice);
        modelAndView.setViewName("rejectEnrole");
        return modelAndView;
    }

    @RequestMapping(value = "/approvalRejected/{id}", method = RequestMethod.POST)
    public ModelAndView rejectApprovalAndSendMessage(@PathVariable Integer id, HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "rejectMessage") String rejectMessage) {
        ModelAndView modelAndView = new ModelAndView();
        HttpSession httpSession = request.getSession();
        String latitudefornotice = httpSession.getAttribute("latitudefornotice").toString();
        String longitudefornotice = httpSession.getAttribute("longitudefornotice").toString();
        int maxRadius = (int) httpSession.getAttribute("maxRadius");
        EnroleNotice enroleNotice = enroleNoticeService.findEnroleNoticeById(id);
        Messages messages = new Messages();
        messages.setMessageFrom(getUserdetails());
        messages.setMessageType("r");
        messages.setMessageDescription(rejectMessage);
        messages.setMessageTo(enroleNotice.getEmail());
        messages.setNoticeId(enroleNotice.getItemId());
        messagesService.addMessage(messages);
        enroleNoticeService.delete(id);
        modelAndView.addObject("noticesToApprove", enroleNoticeService.findEnroleNoticeForApproval(getUserdetails()));
        modelAndView.addObject("noticesAlreadyEnrolled", enroleNoticeService.findEnroleNoticeByEmail(getUserdetails()));
        modelAndView.addObject("noticesNearByLocation", whiteBoardService.getNoticesByLocation(latitudefornotice,
                longitudefornotice, maxRadius, getUserdetails()));
        modelAndView.setViewName("enroleNotices");
        return modelAndView;
    }

    @RequestMapping(value = "/enroleNotices/privateMessage/{id}", method = RequestMethod.POST)
    public ModelAndView enrollNoticePrivateMessage(@PathVariable Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        WhiteBoard whiteBoard = whiteBoardService.findWhiteBoardByID(id);
        modelAndView.addObject("whiteBoard", whiteBoard);
        modelAndView.setViewName("privateMessage");
        return modelAndView;
    }

    @RequestMapping(value = "/enroleNotices/privateMessage/sendMessage/{id}", method = RequestMethod.POST)
    public ModelAndView enrollNoticeSendPrivateMessage(@PathVariable Integer id,
            @RequestParam(value = "privateMessage") String privateMessage) {
        ModelAndView modelAndView = new ModelAndView();
        WhiteBoard whiteBoard = whiteBoardService.findWhiteBoardByID(id);
        Messages messages = new Messages();
        messages.setMessageFrom(getUserdetails());
        messages.setMessageType("p");
        messages.setMessageDescription(privateMessage);
        messages.setMessageTo(whiteBoard.getEmail());
        messages.setNoticeId(id);
        messagesService.addMessage(messages);
        modelAndView.addObject("successMessage", "Message sent successfully");
        modelAndView.addObject("whiteBoard", whiteBoard);
        modelAndView.setViewName("privateMessage");
        return modelAndView;
    }

    @RequestMapping(value = "/myPrivateMessages/delete/{id}", method = RequestMethod.POST)
    public ModelAndView deletePrivateMessage(@PathVariable Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        Messages messages = messagesService.findMessagesByID(id);
        boolean delete = messagesService.deleteMessages(messages);
        if (delete) {
            modelAndView.addObject("deleteMessage", "Message has been deleted successfully");
        } else {
            modelAndView.addObject("deleteMessage", "Message deletion failed");
        }
        modelAndView.addObject("myPrivateMessages", messagesService.getMessages(getUserdetails(), "p"));
        modelAndView.addObject("myGroupMessages", messagesService.getMessages(getUserdetails(), "g"));
        modelAndView.addObject("whiteBoard", new WhiteBoard());
        modelAndView.addObject("notices", new Notices());
        modelAndView.addObject("whiteBoardNotices", whiteBoardService.getNoticesByUserID(getUserdetails()));
        modelAndView.setViewName("myWhiteBoard");

        return modelAndView;
    }

    @RequestMapping(value = "/myPrivateMessages/reply/{id}", method = RequestMethod.POST)
    public ModelAndView replyPrivateMessage(@PathVariable Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        Messages messages = messagesService.findMessagesByID(id);
        modelAndView.addObject("messages", messages);
        modelAndView.setViewName("replyMessage");

        return modelAndView;
    }

    @RequestMapping(value = "/myPrivateMessages/sendReply/{id}", method = RequestMethod.POST)
    public ModelAndView sendReplyToPrivateMessage(@PathVariable Integer id,
            @RequestParam(value = "replyPrivateMessage") String replyPrivateMessage) {
        ModelAndView modelAndView = new ModelAndView();
        Messages messages = messagesService.findMessagesByID(id);
        Messages replyMessage = new Messages();
        replyMessage.setMessageFrom(getUserdetails());
        replyMessage.setMessageType("rp");
        replyMessage.setMessageDescription(replyPrivateMessage);
        replyMessage.setMessageTo(messages.getMessageFrom());
        replyMessage.setNoticeId(messages.getNoticeId());
        messagesService.addMessage(replyMessage);

        modelAndView.addObject("replyMessage", "Message has been Replied successfully");

        modelAndView.addObject("myPrivateMessages", messagesService.getMessages(getUserdetails(), "p"));
        modelAndView.addObject("myGroupMessages", messagesService.getMessages(getUserdetails(), "g"));
        modelAndView.addObject("whiteBoard", new WhiteBoard());
        modelAndView.addObject("notices", new Notices());
        modelAndView.addObject("whiteBoardNotices", whiteBoardService.getNoticesByUserID(getUserdetails()));
        modelAndView.setViewName("myWhiteBoard");

        return modelAndView;
    }

    @RequestMapping(value = "/enroleNotices/sendGroupMessage/{id}", method = RequestMethod.POST)
    public ModelAndView groupMessage(@PathVariable Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        WhiteBoard whiteBoard = whiteBoardService.findWhiteBoardByID(id);
        modelAndView.addObject("whiteBoard", whiteBoard);
        modelAndView.setViewName("groupMessage");
        return modelAndView;
    }

    @RequestMapping(value = "/enroleNotices/groupMessage/sendMessage/{id}", method = RequestMethod.POST)
    public ModelAndView sendGroupMessage(@PathVariable Integer id,
            @RequestParam(value = "groupMessage") String groupMessage) {
        ModelAndView modelAndView = new ModelAndView();
        Iterable<EnroleNotice> noticeEnrolled = enroleNoticeService.findEnroleNoticeByItemId(id);
        WhiteBoard whiteBoard = whiteBoardService.findWhiteBoardByID(id);
        Messages messages = new Messages();
        messages.setMessageFrom(getUserdetails());
        messages.setMessageType("g");
        messages.setMessageDescription(groupMessage);
        messages.setMessageTo(whiteBoard.getEmail());
        messages.setNoticeId(id);
        messagesService.addMessage(messages);
        for (EnroleNotice enroleNotice : noticeEnrolled) {
            messages = new Messages();
            messages.setMessageFrom(getUserdetails());
            messages.setMessageType("g");
            messages.setMessageDescription(groupMessage);
            messages.setMessageTo(enroleNotice.getEmail());
            messages.setNoticeId(id);
            messagesService.addMessage(messages);
        }
        modelAndView.addObject("successMessage", "Group Message sent successfully");
        modelAndView.addObject("whiteBoard", whiteBoard);
        modelAndView.setViewName("groupMessage");
        return modelAndView;
    }

    @RequestMapping(value = "/myGroupMessages/reply/{id}", method = RequestMethod.POST)
    public ModelAndView replyGroupMessage(@PathVariable Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        Messages messages = messagesService.findMessagesByID(id);
        modelAndView.addObject("messages", messages);
        modelAndView.setViewName("replyGroupMessage");

        return modelAndView;
    }

    @RequestMapping(value = "/myGroupMessages/sendReply/{id}", method = RequestMethod.POST)
    public ModelAndView sendReplyToGroupMessage(@PathVariable Integer id,
            @RequestParam(value = "replyGroupMessage") String replyGroupMessage) {
        ModelAndView modelAndView = new ModelAndView();
        Messages messages = messagesService.findMessagesByID(id);
        Iterable<Messages> noticeEnrolled = messagesService.findGroupEmailsFromEnroleNoticeByNoticeId(messages.getNoticeId());
        for (Messages messages1 : noticeEnrolled) {
            Messages replyMessage = new Messages();
            replyMessage.setMessageFrom(getUserdetails());
            replyMessage.setMessageType("rg");
            replyMessage.setMessageDescription(replyGroupMessage);
            replyMessage.setMessageTo(messages1.getMessageTo());
            replyMessage.setNoticeId(messages.getNoticeId());
            messagesService.addMessage(replyMessage);
        }
        modelAndView.addObject("replyGroupMessage", "Group Message has been Replied successfully");
        modelAndView.addObject("myPrivateMessages", messagesService.getMessages(getUserdetails(), "p"));
        modelAndView.addObject("myGroupMessages", messagesService.getMessages(getUserdetails(), "g"));
        modelAndView.addObject("whiteBoard", new WhiteBoard());
        modelAndView.addObject("notices", new Notices());
        modelAndView.addObject("whiteBoardNotices", whiteBoardService.getNoticesByUserID(getUserdetails()));
        modelAndView.setViewName("myWhiteBoard");
        return modelAndView;
    }
}
