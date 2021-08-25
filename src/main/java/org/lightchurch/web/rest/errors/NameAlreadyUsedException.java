package org.lightchurch.web.rest.errors;

public class NameAlreadyUsedException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public NameAlreadyUsedException() {
        super(ErrorConstants.LOGIN_ALREADY_USED_TYPE, "Config name already used!", "configs-api", "config exists");
    }
}
