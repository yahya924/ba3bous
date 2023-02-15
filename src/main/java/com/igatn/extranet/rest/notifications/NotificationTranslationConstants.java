package com.igatn.extranet.rest.notifications;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * FRE - notification translation code constants with handler(s)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationTranslationConstants {

    /**
     * private bases
     **/
    private static final String NOTIFICATION_BASE = "notification";
    private static final String PREMIUM = "premium";
    private static final String POLICY = "policy";
    private static final String REIMBURSEMENT = "reimbursement";

    /**
     * primary level keywords
     **/
    private static final String NEW = "new";
    private static final String RENEW = "renew";

    /**
     * second level keywords
     **/
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";

    /**
     * private sub-bases
     **/
    private static final String PREMIUM_BASE = generateCodeFor(NOTIFICATION_BASE, PREMIUM);
    private static final String POLICY_BASE = generateCodeFor(NOTIFICATION_BASE, POLICY);
    private static final String REIMBURSEMENT_BASE = generateCodeFor(NOTIFICATION_BASE, REIMBURSEMENT);

    /**
     * final public translation codes
     **/
    public static final String NEW_PREMIUM_TITLE = generateCodeFor(PREMIUM_BASE, NEW, TITLE);
    public static final String NEW_PREMIUM_DESCRIPTION = generateCodeFor(PREMIUM_BASE, NEW, DESCRIPTION);

    public static final String NEW_REIMBURSEMENT_TITLE = generateCodeFor(REIMBURSEMENT_BASE, NEW, TITLE);
    public static final String NEW_REIMBURSEMENT_DESCRIPTION = generateCodeFor(REIMBURSEMENT_BASE, NEW, DESCRIPTION);

    public static final String RENEW_POLICY_TITLE = generateCodeFor(POLICY_BASE, NEW, TITLE);
    public static final String RENEW_POLICY_DESCRIPTION = generateCodeFor(POLICY_BASE, NEW, DESCRIPTION);

    /**
     * FRE - Util method
     * 
     * @param args
     * @return
     */
    private static String generateCodeFor(String... args) {
        final char DELIMITER = '.';
        return String.join(String.valueOf(DELIMITER), args);
    }
}

