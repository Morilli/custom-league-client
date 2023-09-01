package com.hawolt.ui.champselect;

import com.hawolt.util.ColorPalette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created: 01/09/2023 01:37
 * Author: Twitter @hawolt
 **/

public class LHintTextField extends JTextField {
    private final String hint;

    public LHintTextField(String hint) {
        this.hint = hint;
        this.setForeground(Color.WHITE);
        this.setCaretColor(Color.WHITE);
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.setFont(new Font("Arial", Font.PLAIN, 22));
        this.setBorder(new EmptyBorder(5, 5, 5, 5));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (!getText().isEmpty()) return;
        int height = getHeight();
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Insets insets = getInsets();
        FontMetrics fontMetrics = g.getFontMetrics();
        int background = getBackground().getRGB();
        int foreground = getForeground().getRGB();
        int mask = 0xFEFEFEFE;
        int blend = ((background & mask) >>> 1) + ((foreground & mask) >>> 1);
        g.setColor(new Color(blend, true));
        g.drawString(hint, insets.left, height / 2 + fontMetrics.getAscent() / 2 - 2);
    }
}
