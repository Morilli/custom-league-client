package com.hawolt.ui.github;

import java.net.MalformedURLException;
import java.net.URL;

public class HtmlBuilder {

    private StringBuilder builder;

    public HtmlBuilder() {
        // it'll do html builder
        this.builder = new StringBuilder("<html><head></head><body><div style=\"color:white\">");
        builder.append("<p>");
    }

    public String build() {
        this.builder.append("</div></body</html>");
        return this.builder.toString();
    }

    public HtmlBuilder add(String text) {

        if (isLink(text)) {
            this.addLink(text, text);
        } else {
            builder.append(text).append(" ");
        }

        return this;
    }

    public HtmlBuilder addLink(String link, String text) {
        builder.append("<a href='").append(link).append("'>").append(text).append("</a> ");
        return this;
    }

    private boolean isLink(String str) {
        try {
            new URL(str);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public HtmlBuilder endLine() {
        // not perfect because end of html has open <p> tag, doesn't matter in the pane tho...
        builder.append("</p><p>");
        return this;
    }
}
