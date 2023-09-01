package com.hawolt.ui.chat.window;

import com.hawolt.rtmp.amf.Pair;
import com.hawolt.ui.impl.JHintTextField;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.util.ui.SmartScroller;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created: 08/08/2023 21:09
 * Author: Twitter @hawolt
 **/

public class ChatWindowContent extends ChildUIComponent {
    private final List<Pair<ChatPerspective, String>> queue = new ArrayList<>();
    private final JPanel history = new JPanel();
    private final Object lock = new Object();
    private final JHintTextField input;
    private final JScrollPane pane;

    public ChatWindowContent(String jid, VirtualRiotXMPPClient xmppClient, LayoutManager layout) {
        super(layout);
        history.setLayout(new BoxLayout(history, BoxLayout.Y_AXIS));
        ChildUIComponent component = new ChildUIComponent(new BorderLayout());
        component.add(history, BorderLayout.NORTH);
        this.pane = new JScrollPane(component);
        SmartScroller.configure(pane);
        pane.getVerticalScrollBar().setUnitIncrement(15);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(pane, BorderLayout.CENTER);
        this.add(input = new JHintTextField("Message..."), BorderLayout.SOUTH);
        this.input.addActionListener(listener -> {
            String message = input.getText();
            xmppClient.sendMessage(jid, message);
            addMessage(ChatPerspective.SELF, message);
            input.setText("");
        });
    }

    protected void drain() {
        synchronized (lock) {
            Dimension size = history.getSize();
            while (!queue.isEmpty()) {
                Pair<ChatPerspective, String> pair = queue.remove(0);
                ChatPerspective perspective = pair.getKey();
                String message = pair.getValue();
                Component[] components = history.getComponents();
                ChatMessage previous = null;
                if (components.length > 0) {
                    previous = (ChatMessage) components[components.length - 1];
                }
                if (previous != null && previous.getPerspective() == perspective) {
                    previous.append(System.lineSeparator() + message);
                } else {
                    ChatMessage component = new ChatMessage(perspective);
                    Dimension dimension = new Dimension(size.width, 10);
                    component.setPreferredSize(dimension);
                    if (previous != null) history.add(Box.createRigidArea(new Dimension(0, 5)));
                    component.append(message);
                    history.add(component);
                }
            }
            revalidate();
            repaint();
        }
    }

    public void addMessage(ChatPerspective perspective, String message) {
        Dimension size = history.getSize();
        if (size.width == 0) {
            queue.add(Pair.from(perspective, message));
        } else {
            queue.add(Pair.from(perspective, message));
            synchronized (lock) {
                if (!queue.isEmpty()) {
                    drain();
                }
            }
        }
    }
}
