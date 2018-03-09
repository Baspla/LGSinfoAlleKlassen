package de.baspla.lgsinfo;

import com.sun.org.apache.regexp.internal.RE;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiLoader;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updateshandlers.SentCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.telegram.abilitybots.api.objects.Flag.*;
import static org.telegram.abilitybots.api.objects.Locality.*;
import static org.telegram.abilitybots.api.objects.Privacy.*;

public class LGSInfoBot extends AbilityBot {
    private final ScheduledFuture<?> future;
    private Plan plan;
    private static String[] motivation = {"Heute ist ein prima Tag.", "Du siehst toll aus.", "Ich akzeptiere dich voll und ganz so, wie du bist.", "Du kannst dich (fast) immer auf mich verlassen.", "Ich bin immer für dich da.", "Du hast wunderschöne Augen", "Du siehst gut aus", "Alles wird ok.", "Alles wird wieder gut.", "Das geht vorbei.", "Dein Lächeln ist bezaubernd", "Ich mag dich", "Füll deine Leere mit Essen.", "Wenns dir schlecht geht: Essen."};

    public int creatorId() {
        return 67025299;
    }


    public LGSInfoBot(String token, String name) {
        super(token, name);
        plan = new Plan("https://lgsit.de/plan");
        ScheduledExecutorService notifyService = Executors.newSingleThreadScheduledExecutor();
        Runnable notifyRunnable = () -> {
            Map<Long, Benutzer> benutzerMap = db.getMap("BENUTZER");
            for (Map.Entry<Long, Benutzer> entry : benutzerMap.entrySet()) {
                Benutzer benutzer = entry.getValue();
                if (benutzer.notify) {
                    ArrayList<Eintrag> out = new ArrayList<>();
                    ArrayList<Eintrag> eintraege = plan.getVetretrungen(benutzer.getKlasse());
                    if (benutzer.letzte != null) {
                        for (Eintrag eintrag : eintraege) {
                            for (Eintrag s : benutzer.letzte) {
                                if (s.toString() == eintraege.toString()) {
                                    out.add(eintrag);
                                    break;
                                }
                            }

                        }
                    } else {
                        out.addAll(eintraege);
                    }
                    benutzer.letzte = eintraege;
                    msgPlan(out, benutzer.getFormat(), benutzer);
                }
            }
        };
        future = notifyService.scheduleAtFixedRate(notifyRunnable, 5, 30, TimeUnit.MINUTES);
    }


    private void sendSilentMessage(long chatId, String s) {
        silent.execute(new SendMessage(chatId, s).enableHtml(true));
    }

    private void sendMessage(Long l, String s) {
        try {
            sender.execute(new SendMessage(l, s).enableHtml(true));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendKeyboardMessage(Long l, String s, ReplyKeyboard rk) {
        try {
            sender.execute(new SendMessage(l, s).setReplyMarkup(rk));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Ability cmdStart() {
        return Ability.builder()
                .name("start")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> sendMessage(ctx.chatId(), "Willkommen zum LGSinfo Bot.\nWähle deine Klasse mit /options aus.\nDieser Bot wird von @TimMorgner entwickelt."))
                .build();
    }

    public Ability cmdHelp() {
        return Ability.builder()
                .name("help")
                .info("zeigt die Hilfe an")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> sendMessage(ctx.chatId(), "/help - zeigt die Hilfe an\n/plan - zeigt euren Plan an\n/notify - ändert deine Benachrichtigungs-Einstellungen\n/options - zeigt deine Einstellungen an\n/klassen - zeigt euch alle Klassen an\n/klasse <i>[Klasse]</i> - wählt die Klasse aus\n/format <i>[1-3]</i> - wählt das Format aus\n/happy - schreibt motivierende Nachrichten"))
                .build();
    }

    public Ability cmdTime() {
        return Ability.builder()
                .name("time")
                .info("zeigt die verbleibende Zeit an")
                .privacy(ADMIN)
                .locality(ALL)
                .input(0)
                .action(ctx -> sendMessage(ctx.chatId(), "Noch " + future.getDelay(TimeUnit.MINUTES) + " Minuten."))
                .build();
    }

    public Ability cmdOptions() {
        return Ability.builder()
                .name("options")
                .info("zeigt deine Einstellungen an")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> sendOptions(ctx.chatId()))
                .build();
    }

    public Ability cmdNotify() {
        return Ability.builder()
                .name("notify")
                .info("ändert deine Benachrichtigungs-Einstellungen")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> sendNotify(ctx.chatId()))
                .build();
    }


    public Ability cmdKlasse() {
        return Ability.builder()
                .name("klasse")
                .info("<Klassenname> - legt die Klasse fest")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(1)
                .action(ctx -> {
                    if (plan.getKlassen().contains(ctx.firstArg())) {
                        setBenutzer(ctx.chatId(), getBenutzer(ctx.chatId()).setKlasse(ctx.firstArg()));
                        sendMessage(ctx.chatId(), "Diese Gruppe ist jetzt in der Klasse " + ctx.firstArg() + ".");
                    } else {
                        sendMessage(ctx.chatId(), "Diese Klasse gibt es nicht.");
                    }
                })
                .build();
    }

    public Ability cmdKlassen() {
        return Ability.builder()
                .name("klassen")
                .info("listet alle Klassen auf")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {
                    String msg = "<b>Alle Klassen:</b>\n";
                    ArrayList<String> klassen = plan.getKlassen();
                    for (String k : klassen) {
                        msg = msg + k + "\n";
                    }
                    sendMessage(ctx.chatId(), msg);
                })
                .build();
    }

    public Ability cmdFormat() {
        return Ability.builder()
                .name("format")
                .info("<1-3> - legt das Format fest")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(1)
                .action(ctx -> {
                    if (ctx.firstArg().equals("1") || ctx.firstArg().equals("2") || ctx.firstArg().equals("3")) {
                        setBenutzer(ctx.chatId(), getBenutzer(ctx.chatId()).setFormat((ctx.firstArg().equals("1") ? 1 : (ctx.firstArg().equals("2") ? 2 : 3))));
                        sendMessage(ctx.chatId(), "Alle Vertretungen werden jetzt im Format " + ctx.firstArg() + " gesendet");
                    } else {
                        sendMessage(ctx.chatId(), "Dieses Format gibt es nicht.");
                    }
                })
                .build();
    }

    public Ability cmdHappy() {
        return Ability.builder()
                .name("happy")
                .info("schreibt motivierende Nachrichten")
                .privacy(PUBLIC)
                .locality(USER)
                .input(0)
                .action(ctx -> {
                    Random rnd = new Random();
                    sendMessage(ctx.chatId(), motivation[rnd.nextInt(motivation.length)]);
                })
                .build();
    }


    public Ability cmdAdminBroadcast() {
        return Ability.builder()
                .name("broadcast")
                .info("Broadcast an Alle")
                .privacy(ADMIN)
                .locality(USER)
                .input(0)
                .action(ctx -> {
                    if (ctx.arguments().length <= 0) {
                        sendMessage(ctx.chatId(), "Gib eine Nachricht an.");
                        return;
                    }
                    String msg = "";
                    for (String s : ctx.arguments()) {
                        msg = msg + " " + s;
                    }
                    Map<Long, Benutzer> benutzerMap = db.getMap("BENUTZER");
                    for (Map.Entry<Long, Benutzer> entry : benutzerMap.entrySet()) {
                        try {
                            sendMessage(entry.getValue().getChatId(), msg);
                        } catch (ClassCastException ex) {
                            ex.printStackTrace();
                        }
                    }
                })
                .build();
    }

    public Ability cmdAdminMessage() {
        return Ability.builder()
                .name("message")
                .info("Nachricht an Nutzer")
                .privacy(ADMIN)
                .locality(USER)
                .input(0)
                .action(ctx -> {
                    if (ctx.arguments().length <= 1) {
                        sendMessage(ctx.chatId(), "Gib eine Nachricht und Nutzer an.");
                        return;
                    }
                    String msg = "";
                    for (int i = 1; i < ctx.arguments().length; i++) {
                        msg = msg + " " + ctx.arguments()[i];
                    }
                    try {
                        long l = Long.valueOf(ctx.firstArg());
                        sendMessage(l, msg);
                        sendMessage(ctx.chatId(), "Nachricht gesendet.");
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        sendMessage(ctx.chatId(), "Diesen Nutzer gibt es nicht.");
                    }
                })
                .build();
    }


    private void sendEditOptions(Long chatId, Integer messageId) {
        generateOptionsKeyboard(chatId);
        try {
            sender.execute(new EditMessageText()
                    .setChatId(chatId)
                    .setMessageId(messageId)
                    .setReplyMarkup(generateOptionsKeyboard(chatId))
                    .setText(getOptionsMessage(chatId))
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void sendOptions(Long chatId) {
        sendKeyboardMessage(chatId, getOptionsMessage(chatId), generateOptionsKeyboard(chatId));
    }

    private void sendNotify(Long chatId) {
        Benutzer benutzer = getBenutzer(chatId);
        sendMessage(chatId, "Du wirst ab sofort " + ((benutzer.changeNotify()) ? "" : "nicht mehr ") + "benachrichtigt.");
        setBenutzer(benutzer.chatId, benutzer);
    }

    private String getOptionsMessage(Long chatId) {
        return "Wähle aus welche Einstellung du ändern willst." + (getBenutzer(chatId).getKlasse().isEmpty() ? "" : "\nDu bist aktuell in Klasse " + getBenutzer(chatId).getKlasse() + ".");
    }

    public Reply onCallback() {
        return Reply.of(update -> {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery().setCallbackQueryId(update.getCallbackQuery().getId());
            Message message = update.getCallbackQuery().getMessage();
            if (!message.getChat().isUserChat()) return;//KEINE CALLBACKS IN GRUPPEN
            String data = update.getCallbackQuery().getData().toLowerCase();
            if (data.startsWith("klasse_")) {
                int seite = getSeite(data);
                try {
                    sender.execute(new EditMessageText()
                            .setChatId(message.getChatId())
                            .setMessageId(message.getMessageId())
                            .setReplyMarkup(generateKlassenKeyboard(seite))
                            .setText("Wähle deine Klasse aus dem Menü aus.\nFalls du sie nicht finden kannst wende dich an @TimMorgner.")
                    );
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (data.startsWith("wahl_")) {
                String klasse = data.substring(5);
                String k = null;
                for (String s : plan.getKlassen()) {
                    if (s.equalsIgnoreCase(klasse)) k = s;
                }
                if (k != null) {
                    setBenutzer(message.getChatId(), getBenutzer(message.getChatId()).setKlasse(k));
                    sendEditOptions(message.getChatId(), message.getMessageId());
                    answerCallbackQuery.setText(k + " wurde als Klasse ausgewählt.");
                } else {
                    answerCallbackQuery.setText("Diese Klasse gibt es nicht.");
                }
            } else if (data.startsWith("notify")) {
                Benutzer benutzer = getBenutzer(message.getChatId());
                answerCallbackQuery.setText("Du wirst ab sofort " + ((benutzer.changeNotify()) ? "" : "nicht mehr ") + "benachrichtigt.");
                setBenutzer(benutzer.chatId, benutzer);
                sendEditOptions(message.getChatId(), message.getMessageId());
            } else if (data.startsWith("formatmenu")) {
                try {
                    sender.executeAsync(new EditMessageText()
                                    .setChatId(message.getChatId())
                                    .setMessageId(message.getMessageId())
                                    .setReplyMarkup(generateFormatKeyboard())
                                    .setText("Wähle das Format aus in dem die Vertretungen dir zugesendet werden sollen."), new SentCallback<Serializable>() {
                                @Override
                                public void onResult(BotApiMethod<Serializable> method, Serializable response) {

                                }

                                @Override
                                public void onError(BotApiMethod<Serializable> method, TelegramApiRequestException apiException) {

                                }

                                @Override
                                public void onException(BotApiMethod<Serializable> method, Exception exception) {

                                }
                            }
                    );
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (data.startsWith("format_")) {
                String type = data.substring(7);
                Benutzer benutzer = getBenutzer(message.getChatId());
                switch (type.toLowerCase()) {
                    case "short":
                        benutzer.setFormat(1);
                        break;
                    case "long":
                        benutzer.setFormat(2);
                        break;
                    case "all":
                        benutzer.setFormat(3);
                        break;
                    default:
                        type = "ERROR";
                        break;
                }
                setBenutzer(message.getChatId(), benutzer);
                answerCallbackQuery.setText("Alle Vertretungen werden jetzt im '" + type + "'-Format gesendet");
                sendEditOptions(message.getChatId(), message.getMessageId());
            } else {
                System.out.println("??? Callback Query mit: " + data);
            }

            try {
                sender.execute(answerCallbackQuery);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }, CALLBACK_QUERY);
    }

    private InlineKeyboardMarkup generateFormatKeyboard() {
        ArrayList<String> klassen = plan.getKlassen();
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row1.add(new InlineKeyboardButton("Kurz").setCallbackData("format_short"));
        row2.add(new InlineKeyboardButton("Lang").setCallbackData("format_long"));
        row3.add(new InlineKeyboardButton("Alles").setCallbackData("format_all"));
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private InlineKeyboardMarkup generateOptionsKeyboard(long chatId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row1.add(new InlineKeyboardButton("Klasse").setCallbackData("klasse_0"));
        row1.add(new InlineKeyboardButton("Format").setCallbackData("formatmenu"));
        rows.add(row1);
        row2.add(new InlineKeyboardButton("Benachrichtigungen " + EmojiManager.getForAlias(getBenutzer(chatId).notify ? ":heavy_check_mark:" : ":x:").getUnicode()).setCallbackData("notify"));
        rows.add(row2);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private int getSeite(String context) {
        int seite = 0;
        if (context.toLowerCase().contains("klasse_")) {
            try {
                seite = new Integer(context.toLowerCase().substring(7));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return seite;
    }

    private InlineKeyboardMarkup generateKlassenKeyboard(int seite) {
        ArrayList<String> klassen = plan.getKlassen();
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        addKlasseToRow(row1, seite, 0, klassen);
        addKlasseToRow(row1, seite, 1, klassen);
        addKlasseToRow(row2, seite, 2, klassen);
        addKlasseToRow(row2, seite, 3, klassen);
        addKlasseToRow(row3, seite, 4, klassen);
        addKlasseToRow(row3, seite, 5, klassen);
        if (!row1.isEmpty()) rows.add(row1);
        if (!row2.isEmpty()) rows.add(row2);
        if (!row3.isEmpty()) rows.add(row3);
        if (seite > 0) row4.add(new InlineKeyboardButton("<").setCallbackData("klasse_" + (seite - 1)));
        if (((seite + 1) * 6) < klassen.size())
            row4.add(new InlineKeyboardButton(">").setCallbackData("klasse_" + (seite + 1)));
        if (!row4.isEmpty()) rows.add(row4);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void addKlasseToRow(List<InlineKeyboardButton> row, int seite, int i, ArrayList<String> klassen) {
        if (klassen.size() - 1 >= (seite * 6) + i)
            row.add(new InlineKeyboardButton(klassen.get((seite * 6) + i)).setCallbackData("wahl_" + klassen.get((seite * 6) + i).toLowerCase()));
    }

    private Benutzer getBenutzer(Long chatId) {
        if (chatId == null) return null;
        Map<Long, Benutzer> benutzerMap = db.getMap("BENUTZER");
        if (!benutzerMap.containsKey(chatId)) {
            benutzerMap.put(chatId, new Benutzer(chatId));
            db.commit();
        }
        return benutzerMap.get(chatId);
    }

    private void setBenutzer(long chatId, Benutzer benutzer) {
        if (benutzer == null) return;
        Map<Long, Benutzer> benutzerMap = db.getMap("BENUTZER");
        benutzerMap.put(chatId, benutzer);
        db.commit();
    }


    public Ability cmdPlan() {
        return Ability.builder()
                .name("plan")
                .info("zeigt deinen Vertretungsplan an")
                .privacy(PUBLIC)
                .locality(ALL)
                .input(0)
                .action(ctx -> {
                    Benutzer b = getBenutzer(ctx.chatId());
                    if (b == null) {
                        sendMessage(ctx.chatId(), "Du wurdest nicht erkannt. Bitte gib /start ein.");
                        return;
                    }
                    if (!b.hasKlasse()) {
                        sendMessage(ctx.chatId(), "Du hast noch keine Klasse gewählt. Wähle deine Klasse mit /options.");
                        return;
                    }
                    sendPlan(b);
                })
                .build();
    }

    private void sendPlan(Benutzer benutzer) {
        int format = benutzer.getFormat();
        System.out.println("Plan fuer Benutzer: " + benutzer.getChatId());
        if (benutzer.getKlasse() == null || benutzer.getKlasse().isEmpty()) {
            sendMessage(benutzer.getChatId(), "Bitte gib deine Klasse mit /options an.");
            return;
        }
        if (format == 0) {
            sendMessage(benutzer.getChatId(), "Bitte wähle ein Format mit /options aus.");
            return;
        }
        if (!plan.getKlassen().contains(benutzer.getKlasse())) {
            sendMessage(benutzer.getChatId(), "Deine Klasse gibt es nicht mehr.");
            return;
        }
        ArrayList<Eintrag> eintraege = plan.getVetretrungen(benutzer.getKlasse());
        if (eintraege == null || eintraege.isEmpty()) {
            sendMessage(benutzer.getChatId(), "Aktuell gibt es keine Vertretungen für dich.");
            return;
        }
        System.out.println("Plan existiert");
        msgPlan(eintraege, format, benutzer);
        benutzer.letzte = eintraege;
    }

    private void msgPlan(ArrayList<Eintrag> eintraege, int format, Benutzer benutzer) {
        String msg = "";
        int seite = 1;
        for (int i = 0; i < eintraege.size(); i++) {
            msg = msg.concat(eintraege.get(i).toString(format) + "\n");
            if (i % 6 == 0 && i != 0) {
                msg = "<b>Vertretungsplan Seite " + seite + "</b>\n\n" + msg;
                sendSilentMessage(benutzer.getChatId(), msg);
                seite++;
                msg = "";
            }
        }
        if (!msg.isEmpty()) {
            msg = "<b>Vertretungsplan Seite " + seite + "</b>\n\n" + msg;
            sendSilentMessage(benutzer.getChatId(), msg);
        }
    }


    /**
     * This is very important to override for . By default, any update that does not have a message will not pass through abilities.
     * To customize that, you can just override this global flag and make it return true at every update.
     * This way, the ability flags will be the only ones responsible for checking the update's validity.
     */
    @Override
    public boolean checkGlobalFlags(Update update) {
        return true;
    }

}