package com.hawolt.util.ui;

import com.hawolt.util.ColorPalette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class ScrollPane extends JScrollPane {
    public ScrollPane(Component view) {
        getVerticalScrollBar().setUI(new com.hawolt.util.ui.ScrollBar());
        getHorizontalScrollBar().setUI(new com.hawolt.util.ui.ScrollBar());
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setViewportView(view);
        setBackground(ColorPalette.BACKGROUND_COLOR);
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        EventQueue.invokeLater(() -> {
            setComponentZOrder(getVerticalScrollBar(), 0);
            setComponentZOrder(getHorizontalScrollBar(), 1);
            setComponentZOrder(getViewport(), 2);
            getVerticalScrollBar().setOpaque(false);
            getHorizontalScrollBar().setOpaque(false);
        });
    }

    private class ScrollLayout extends ScrollPaneLayout {
        @Override
        public void layoutContainer(Container parent) {
            super.layoutContainer(parent);
            if (parent instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) parent;
                Rectangle rectangle = scroll.getViewport().getBounds();
                Insets insets = parent.getInsets();
                int headerHeigth = 0;
                if (scroll.getColumnHeader() != null) {
                    Rectangle header = scroll.getColumnHeader().getBounds();
                    headerHeigth = header.height;
                }
                rectangle.width = scroll.getBounds().width - (insets.left + insets.right);
                rectangle.height = scroll.getBounds().height - (insets.top + insets.bottom) - headerHeigth;
                if (Objects.nonNull(viewport)) {
                    viewport.setBounds(rectangle);
                }
                if (!Objects.isNull(hsb)) {
                    Rectangle hrc = hsb.getBounds();
                    hrc.width = rectangle.width;
                    hsb.setBounds(hrc);
                }
            }
        }
    }
}
