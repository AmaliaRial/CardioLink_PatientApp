package functionalities.telegramBot;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class BotBuild {
    public static void main(String[] args) {
        String botToken = "8371106425:AAEeBuaIvHEhr_qGl53D3t4tsDEG21Wvqlk";
        // Using try-with-resources to allow autoclose to run upon finishing
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new CardioLinkBot(botToken));
            System.out.println("MyAmazingBot successfully started!");
            // Ensure this prcess wait forever
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


