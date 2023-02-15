package com.igatn.extranet.rest.mailing;

import com.igatn.extranet.domainjpa.api.data.AttachmentRepository;
import com.igatn.extranet.domainjpa.api.data.demand.DemandRepository;
import com.igatn.extranet.domainjpa.api.data.demand.TypeDemandRepository;
import com.igatn.extranet.domainjpa.impl.domain.attachment.Attachment;
import com.igatn.extranet.domainjpa.impl.domain.demand.Demand;
import com.igatn.extranet.domainjpa.impl.domain.demand.TypeDemand;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.ApiError;
import com.igatn.extranet.rest.exceptions.InvalidInternetAddressException;
import com.igatn.extranet.utils.MailUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * OJA - Mailing Service, rest controller 
 */
@Slf4j
@RestController
@RequestMapping(path = "/mails", produces = "application/json")
public class MailController {

    @Autowired
    private JavaMailSender javaMailSender;
    
    @Autowired
    private MailUtils mailUtils;
    
    @Autowired
    private DemandRepository historyActivityDAO;

    @Autowired
    private TypeDemandRepository historyActivityTypeDAO;
    
    @Autowired
    private AttachmentRepository fileDAO;

    @Value("${igatn.extranet.mails.recipient}")
    private String recipient;

    @Value("${igatn.extranet.mails.side-recipient}")
    private String sideRecipient;

    @Value("${igatn.extranet.files.static.resources}")
    private String resource;

    public void setMailSender(JavaMailSender mailSender) {
        this.javaMailSender = mailSender;
    }

    /**
     * test the controller
     *
     * @return
     */
    @GetMapping
    public String helloMailing() {
        log.info("/mails");

        return "Hello from /mails";
    }

    /**
     * sendMail endpoint
     *
     * @param parameters
     * @return
     */
    @PostMapping(path = "send", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> send(@ModelAttribute MailParameters parameters, Authentication auth) {

        log.info("processing /mails/send in progress.");
        
        try {
            ApiError apiError = new ApiError();

            User user = (User) auth.getPrincipal();
            
            String subjectData = parameters.getType().equals(ActivityTypes.ECL) ? parameters.getPatient() : parameters.getSubject();
            
            InternetAddress[] recipients = new InternetAddress[] { new InternetAddress(recipient), new InternetAddress(sideRecipient) };
            String subject = prepareSubject(parameters.getType(), subjectData, parameters.getSenderId());
            String content = prepareContent(parameters.getType(), parameters.getContent(), parameters.getSenderName());
            MultipartFile[] attachedFiles = parameters.getFiles();

            MimeMessagePreparator preparator = mailUtils.prepareMail(recipients, subject, content, attachedFiles);

            this.javaMailSender.send(preparator);

            log.info("mail sent.");

            // History Activity Log Start
            HashSet<Attachment> files = saveFileToSystem(parameters.getFiles(), apiError);
            insertHistoryActivityData(parameters, files, user);

            if (apiError.getCode() == 500) {
                return new ResponseEntity<>(apiError,null, apiError.getCode());
            }

            SuccessResponse response = new SuccessResponse();
            response.setSuccess(true);

            log.info("Sending /mails/send response.");
            log.info(response.toString());

            return ResponseEntity.ok(response);
        } 
        catch (AddressException e) {
            throw new InvalidInternetAddressException();
        }
    }

    public String prepareSubject(ActivityTypes type, String subject, String senderId) {
        switch (type) {
            case ECL -> { return "E-remboursement pour le bénéficiaire: " + subject; }
            case MDU -> { return "Fourniture des pièces justificatives manquantes pour le dossier: " + subject; }
            case CPL -> { return "Réclamation: " + subject + " - " + "Assuré: " + senderId; }
            default -> { return subject; }
        }
    }

    public String prepareContent(ActivityTypes type, String content, String senderName) {
        switch (type) {
            case ECL -> { return "Note de l'assuré: " + content; }
            case CPL -> { return "Réclamation de la part de: " + senderName + " \n\n" + content; }
            default -> { return content; }
        }
    }

    public void insertHistoryActivityData(MailParameters parameters, HashSet<Attachment> files, User user) {
        log.info("logging activity to database history.");

        fileDAO.saveAll(files);

        String historyActivitySubject = parameters.getType().equals(ActivityTypes.ECL) ? parameters.getPatient() : parameters.getSubject();

        Demand demand = new Demand();
        
        TypeDemand typeDemand = historyActivityTypeDAO.findByCode(parameters.getType().getType());
        
        if (typeDemand != null) {
            demand.setType(typeDemand);
            demand.setSubject(historyActivitySubject);
            demand.setMessage(parameters.getContent());
            demand.setAttachments(files);
            demand.setSender(user);
        }

        historyActivityDAO.save(demand);

        log.info("logging successfully done.");
    }

    public HashSet<Attachment> saveFileToSystem(MultipartFile[] files, ApiError apiError) {
        HashSet<Attachment> filesToSave = new HashSet<>();

        if (files != null) {
            Arrays.stream(files).forEach((file) -> {
                try {
                    String fileName = file.getOriginalFilename();

                    if (fileName != null) {
                        if (!fileName.trim().isEmpty()) {
                            String fileNameFormatted = "";
                            boolean fileCreated = false;
                            int trials = 0;

                            while (!fileCreated) {
                                String[] fileNameParts = fileName.split("\\.");
                                fileNameFormatted = "/" + fileNameParts[0].replaceAll("[^a-zA-Z0-9 ._-]", "")
                                        .replaceAll(" ", "-")
                                        .toLowerCase() +
                                        (trials == 0 ? "." : " (" + trials + ").") +
                                        fileNameParts[fileNameParts.length - 1];
                                String filePath = resource + fileNameFormatted;

                                java.io.File path = new java.io.File(filePath);
                                fileCreated = path.createNewFile();
                                trials++;

                                if (fileCreated) {
                                    FileOutputStream output = new FileOutputStream(path);
                                    output.write(file.getBytes());
                                    output.close();
                                }
                            }

                            if (!fileNameFormatted.trim().isEmpty()) {
                                Attachment f = new Attachment();
                                f.setFileName(file.getOriginalFilename());
                                f.setFileType(file.getContentType());
                                f.setFileUrl(fileNameFormatted);
                                f.setFileSize(file.getSize()); 

                                filesToSave.add(f);
                            }
                        }
                    }
                }
                catch (IOException e) {
                    apiError.setCode(500);
                    apiError.setMessage(e.getMessage());
                }
            });
        }

        return filesToSave;
    }
}

@ToString(includeFieldNames=false)
@AllArgsConstructor
enum ActivityTypes {
    ECL("ECL"), MDU("MDU"), CPL("CPL");
    
    @Getter
    private String type;
}

/**
 * Mail Parameters Request Body
 */
@Data
class MailParameters {
    ActivityTypes type;
    String subject;
    String patient;
    String content;
    String senderId;
    String senderName;
    MultipartFile[] files;
}

/**
 * Success Response
 */
@Data
class SuccessResponse {
    boolean success;
}
