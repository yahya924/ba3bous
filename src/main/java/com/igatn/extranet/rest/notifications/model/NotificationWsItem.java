package com.igatn.extranet.rest.notifications.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * FRE - insured notification model
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class NotificationWsItem {
  private long id;
  private String title;
  private String description;
  private String creationDate;
  private String type;
  private boolean seen;
}
