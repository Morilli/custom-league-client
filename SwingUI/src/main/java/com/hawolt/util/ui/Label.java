package com.hawolt.util.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;

public class Label extends JPanel {
    private String text;
    private TextAlign textAlign;
    private boolean useShadow;

    public Label(String text, TextAlign align) {
        this.text = text;
        textAlign = align;
        init();
    }

    public Label(String text, TextAlign align, boolean shadow) {
        this.text = text;
        textAlign = align;
        useShadow = shadow;
        init();
    }

    private void init() {
        setBackground(new Color(0, 0, 0, 0));
        setFont(new Font("Arial", Font.BOLD, 18));
        setForeground(Color.WHITE);
    }

    public void setFontSize(int size) {
        setFont(new Font(getFont().getFontName(), getFont().getStyle(), size));
    }

    public void useShadow(boolean use) {
        useShadow = use;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        customDraw(graphics);
    }

    private void customDraw(Graphics graphics) {
        //super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics.create();
        g2d.setFont(getFont());
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics metrics = getFontMetrics(getFont());
        int x = getBounds().x, y = getBounds().y;

        //Draw Label Background so that it's visible if colored
        Area backgroundArea = new Area(new Rectangle(x, y, getBounds().width, getBounds().height));
        g2d.setColor(getBackground());
        g2d.fill(backgroundArea);

        switch (textAlign) {
            case CENTER:
                x = getBounds().x + getBounds().width / 2 - metrics.stringWidth(text) / 2;
                break;
            case RIGHT:
                x = getBounds().x + getBounds().width - metrics.stringWidth(text);
                break;
        }
        y = getBounds().y + getBounds().height / 2 + (metrics.getAscent() - metrics.getDescent() - metrics.getLeading()) / 2;
        if (useShadow)
            drawShadowText(g2d, x, y);
        else
            drawText(g2d, x, y);

        g2d.dispose();
    }

    private void drawText(Graphics g, int x, int y) {
        g.setColor(getForeground());
        g.drawString(text, x, y);
    }

    private void drawShadowText(Graphics g, int x, int y) {
        g.setColor(Color.BLACK);
        g.drawString(text, x + 1, y + 1);
        g.setColor(getForeground());
        g.drawString(text, x, y);
    }

    public void drawTextStandalone(Graphics g) {
        customDraw(g);
    }
}
