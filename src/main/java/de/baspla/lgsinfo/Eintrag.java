package de.baspla.lgsinfo;

import org.jsoup.nodes.Element;

public class Eintrag {
    private String datum, tag, klasse, stunde, vertretungs_fach, vertretungs_raum, fach, lehrer, info, art, url;

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getKlasse() {
        return klasse;
    }

    public void setKlasse(String klasse) {
        this.klasse = klasse;
    }

    public String getStunde() {
        return stunde;
    }

    public void setStunde(String stunde) {
        this.stunde = stunde;
    }

    public String getVertretungs_fach() {
        return vertretungs_fach;
    }

    public void setVertretungs_fach(String vertretungs_fach) {
        this.vertretungs_fach = vertretungs_fach;
    }

    public String getVertretungs_raum() {
        return vertretungs_raum;
    }

    public void setVertretungs_raum(String vertretungs_raum) {
        this.vertretungs_raum = vertretungs_raum;
    }

    public String getFach() {
        return fach;
    }

    public String getUrl() {
        return url;
    }

    public Eintrag(String datum, String tag, String klasse, String stunde, String vertretungs_fach, String vertretungs_raum, String fach, String lehrer, String info, String art, String url) {
        this.datum = datum;
        this.tag = tag;
        this.klasse = klasse;
        this.stunde = stunde;
        this.vertretungs_fach = vertretungs_fach;
        this.vertretungs_raum = vertretungs_raum;
        this.fach = fach;
        this.lehrer = lehrer;
        this.info = info;
        this.art = art;
        this.url = url;
    }

    Eintrag(Element element, String url) {
        try {
            datum = element.child(0).text();
            tag = element.child(1).text();
            klasse = element.child(2).text();
            stunde = element.child(3).text();
            vertretungs_fach = element.child(4).text();
            vertretungs_raum = element.child(5).text();
            fach = element.child(6).text();
            lehrer = element.child(7).text();
            info = element.child(8).text();
            art = element.child(9).text();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFach(String fach) {
        this.fach = fach;

    }

    public String getLehrer() {
        return lehrer;
    }

    public void setLehrer(String lehrer) {
        this.lehrer = lehrer;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }

    @Override
    public String toString() {
        return "Eintrag{" +
                "datum='" + datum + '\'' +
                ", tag='" + tag + '\'' +
                ", klasse='" + klasse + '\'' +
                ", stunde='" + stunde + '\'' +
                ", vertretungs_fach='" + vertretungs_fach + '\'' +
                ", vertretungs_raum='" + vertretungs_raum + '\'' +
                ", fach='" + fach + '\'' +
                ", lehrer='" + lehrer + '\'' +
                ", info='" + info + '\'' +
                ", art='" + art + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    String toString(int format) {
        String text = "";
        switch (format) {
            case 1:
                text = "<i>%tag%, %datum% - %stunde% Stunde</i> \n" +
                        "<b>Info:</b> %info%\n" +
                        "<b>Art:</b> %art%\n" +
                        "<b>Lehrer:</b> %lehrer%\n"+
                        "<b>Vertretungs Raum:</b> %vertretungs_raum%\n";
                break;
            case 2:
                text = "<b>Kurs:</b> <a href=\"%url%\">%klasse%</a>\n" +
                        "<b>Datum:</b> <i>%tag%, %datum%</i>\n" +
                        "<b>Stunde:</b> %stunde%\n" +
                        "<b>Info:</b> %info%\n" +
                        "<b>Art:</b> %art%\n" +
                        "<b>Lehrer:</b> %lehrer%\n"+
                        "<b>Vertretungs Raum:</b> %vertretungs_raum%\n";
                break;
            case 3:
                text = "<b>Kurs:</b> <a href=\"%url%\">%klasse%</a>\n" +
                        "<b>Datum:</b> <i>%tag%, %datum%</i>\n" +
                        "<b>Stunde:</b> %stunde%\n" +
                        "<b>Info:</b> %info%\n" +
                        "<b>Art:</b> %art%\n" +
                        "<pre>- Vertretung -</pre>\n" +
                        "<b>Fach:</b> %vertretungs_fach%\n" +
                        "<b>Raum:</b> %vertretungs_raum%\n" +
                        "<code>- Eigentlich -</code>\n" +
                        "<b>Fach:</b> %fach%\n" +
                        "<b>Lehrer:</b> %lehrer%\n";
                break;
            default:
                return "Unbekanntes Format";
        }
        return text.replace("%datum%", (datum == null || datum.isEmpty()) ? "N/A" : datum)
                .replace("%tag%", (tag == null || tag.isEmpty()) ? "N/A" : tag)
                .replace("%klasse%", (klasse == null || klasse.isEmpty()) ? "N/A" : klasse)
                .replace("%stunde%", (stunde == null || stunde.isEmpty()) ? "N/A" : stunde)
                .replace("%vertretungs_fach%", (vertretungs_fach == null || vertretungs_fach.isEmpty()) ? "N/A" : vertretungs_fach)
               .replace("%vertretungs_raum%", (vertretungs_raum == null || vertretungs_raum.isEmpty()) ? "N/A" : vertretungs_raum)
                .replace("%fach%", (fach == null || fach.isEmpty()) ? "N/A" : fach)
                .replace("%lehrer%", (lehrer == null || lehrer.isEmpty()) ? "N/A" : lehrer)
                .replace("%info%", (info == null || info.isEmpty()) ? "N/A" : info)
                .replace("%art%", (art == null || art.isEmpty()) ? "N/A" : art)
                .replace("%url%", (url == null || url.isEmpty()) ? "N/A" : url);
    }
}
