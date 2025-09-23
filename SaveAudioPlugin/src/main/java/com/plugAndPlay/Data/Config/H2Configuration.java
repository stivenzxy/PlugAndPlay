package com.plugAndPlay.Data.Config;

import java.util.ResourceBundle;

public class H2Configuration {
    public static String getProperty(String clave){
        ResourceBundle recursoBundle = ResourceBundle.getBundle("database-configuration");
        return recursoBundle.getString(clave);
    }
}