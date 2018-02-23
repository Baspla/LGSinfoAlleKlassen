package de.baspla;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Settings {
    private Properties properties;
    private Properties defaultProperties;
    Settings() {
        properties = new Properties(defaultProperties);
        properties.setProperty("lgsinfo_botname", "");
        properties.setProperty("lgsinfo_bottoken", "");
        try {
            InputStream input = new FileInputStream("config.properties");
            properties.load(input);
        } catch (IOException e) {
            try {
                OutputStream outputStream = new FileOutputStream("config.properties");
                properties.store(outputStream, "LGSBot");
            } catch (IOException e1) {
                e.printStackTrace();
                System.err.println("SCHREIBFEHLER");
                System.exit(-1);
            }
        }
    }

    String getLGSinfoBotname() {
        return properties.getProperty("lgsinfo_botname");
    }

   String getLGSinfoBottoken() {
        return properties.getProperty("lgsinfo_bottoken");
    }

    public Properties getProperties() {
        return properties;
    }
}
