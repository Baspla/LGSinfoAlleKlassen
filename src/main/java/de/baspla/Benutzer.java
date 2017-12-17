package de.baspla;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Benutzer implements Serializable {

    private static final long serialVersionUID = 133742426913374242L;
    @JsonProperty("klasse")
    String klasse;
    @JsonProperty("chatId")
    long chatId;
    @JsonProperty("format")
    int format;

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

    public Benutzer(String klasse, long chatId) {
        this.klasse = klasse;
        this.chatId = chatId;
        format = 2;
    }

    public Benutzer(long chatid) {
        this("", chatid);
    }

    public String getKlasse() {
        return klasse;
    }

    public Benutzer setKlasse(String klasse) {
        this.klasse = klasse;
        return this;
    }

    public long getChatId() {
        return chatId;
    }

    public Benutzer setChatId(long chatid) {
        this.chatId = chatid;
        return this;
    }

    public int getFormat() {
        return format;
    }

    public Benutzer setFormat(int format) {
        this.format = format;
        return this;
    }

    public boolean hasKlasse() {
        return klasse != null && !klasse.isEmpty();
    }

}
