package com.igatn.extranet.rest.historyActivity.models;

import com.igatn.extranet.domainjpa.impl.domain.attachment.Attachment;
import lombok.Data;

import java.util.Set;

@Data
public class HistoryActivity {
    private int id;
    private String subject, message, type, createdAt;
    private Set<Attachment> attachments;
}
