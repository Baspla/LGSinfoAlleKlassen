package de.baspla;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiException;


public class Main {
    private static Log LOG = LogFactory.getLog(Main.class.getName());

    public static void main(String[] args) {
        if(args.length<2||args.length>2){
            System.out.println("KEIN TOKEN/NAME!");
            return;
        }
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            String token = args[0];
            String name = args[1];
            TelegramBot bot = new TelegramBot(token, name);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            LOG.error(e);
        }
    }

}
