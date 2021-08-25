package org.lightchurch.web.rest.errors;

public class MissingConfigException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public MissingConfigException() {
        super(ErrorConstants.LOGIN_ALREADY_USED_TYPE, "Missing Config!", "configs-api", "config missing");
    }
}
