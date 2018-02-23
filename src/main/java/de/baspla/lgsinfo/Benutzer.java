package de.baspla.lgsinfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Benutzer implements Serializable {

    private static final long serialVersionUID = 133742426913374242L;
    @JsonProperty("klasse")
    private String klasse;
    @JsonProperty("chatId")
    private long chatId;
    @JsonProperty("format")
    private int format;

    @Override
    public String toString() {
        return "Benutzer{" +
                "klasse='" + klasse + '\'' +
                ", chatId=" + chatId +
                ", format='" + format + '\'' +
                '}';
    }

    @JsonCreator
    public Benutzer(@JsonProperty("klasse") String klasse, @JsonProperty("chatId") long chatId, @JsonProperty("format") int format) {
        this.klasse = klasse;
        this.chatId = chatId;
        this.format = format;
    }

    private Benutzer(String klasse, long chatId) {
        this.klasse = klasse;
        this.chatId = chatId;
        format = 2;
    }

    Benutzer(long chatid) {
        this("", chatid);
    }

    String getKlasse() {
        return klasse;
    }

    Benutzer setKlasse(String klasse) {
        this.klasse = klasse;
        return this;
    }

    long getChatId() {
        return chatId;
    }

    public Benutzer setChatId(long chatid) {
        this.chatId = chatid;
        return this;
    }
    int getFormat() {
        return format;
    }

     Benutzer setFormat(int format) {
        this.format = format;
        return this;
    }

    boolean hasKlasse() {
        return klasse != null && !klasse.isEmpty();
    }

}
