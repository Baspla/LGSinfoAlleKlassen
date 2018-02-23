package de.baspla.lgsinfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;


public class Main {
    private static Log LOG = LogFactory.getLog(Main.class.getName());

    public static void main(String[] args) {
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        Settings settings = new Settings();
        try {
            String token = settings.getLGSinfoBottoken();
            String name = settings.getLGSinfoBotname();
            if(token.isEmpty()||name.isEmpty()){
                System.err.println("Kein Name/Token");
                System.exit(42);
            }
            LGSInfoBot bot = new LGSInfoBot(token,name);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            LOG.error(e);
        }
    }

}
