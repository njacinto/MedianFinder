/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nfpj.medianfinder;

/**
 *
 * @author njacinto
 */
public class InvalidPropertyValueException extends MedianFinderException {
    private final String property;
    private final String value;

    public InvalidPropertyValueException(String property, String value) {
        super("Invalid value for property '"+property+"': "+value);
        this.property = property;
        this.value = value;
    }

    public InvalidPropertyValueException(String property, String value, Throwable cause) {
        super("Invalid value for property '"+property+"': "+value, cause);
        this.property = property;
        this.value = value;
    }

    public InvalidPropertyValueException(String property, String value, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super("Invalid value for property '"+property+"': "+value, cause, enableSuppression, writableStackTrace);
        this.property = property;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }


}
