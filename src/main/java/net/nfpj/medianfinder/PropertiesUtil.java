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
public final class PropertiesUtil {

    private PropertiesUtil() {
    }

    public static int get(String name, int def){
        String value = System.getProperty(name);
        if(value!=null){
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignore){
                throw new InvalidPropertyValueException(name, value);
            }
        }
        return def;
    }

    public static boolean get(String name, boolean def){
        String value = System.getProperty(name);
        if(value!=null){
            if(value.equalsIgnoreCase("true")){
                return true;
            } else if(value.equalsIgnoreCase("false")){
                return false;
            } else {
                throw new InvalidPropertyValueException(name, value);
            }
        }
        return def;
    }
}
