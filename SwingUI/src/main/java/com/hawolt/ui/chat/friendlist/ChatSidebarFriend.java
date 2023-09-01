package com.hawolt.ui.chat.friendlist;

import com.hawolt.LeagueClientUI;
import com.hawolt.client.resources.ledge.LedgeEndpoint;
import com.hawolt.client.resources.ledge.parties.PartiesLedge;
import com.hawolt.client.resources.ledge.parties.objects.PartyException;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.logger.Logger;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.ui.FlatButton;
import com.hawolt.util.ui.HighlightType;
import com.hawolt.util.ui.Label;
import com.hawolt.util.ui.TextAlign;
import com.hawolt.xmpp.core.VirtualRiotXMPPClient;
import com.hawolt.xmpp.event.objects.friends.GenericFriend;
import com.hawolt.xmpp.event.objects.friends.impl.OnlineFriend;
import com.hawolt.xmpp.event.objects.presence.AbstractPresence;
import com.hawolt.xmpp.event.objects.presence.ConnectionStatus;
import com.hawolt.xmpp.event.objects.presence.impl.MobilePresence;
import com.hawolt.xmpp.event.objects.presence.impl.OfflinePresence;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

/**
 * Created: 08/08/2023 18:15
 * Author: Twitter @hawolt
 **/

public class ChatSidebarFriend extends FlatButton {
    private final VirtualRiotXMPPClient xmppClient;
    private final GenericFriend friend;

    private AbstractPresence lastKnownPresence;
    private ConnectionStatus connectionStatus;
    private Runnable runnable;
    private int counter;
    private LeagueClientUI leagueClientUI;

    private String name;

    public ChatSidebarFriend(VirtualRiotXMPPClient xmppClient, GenericFriend friend, LeagueClientUI leagueClientUI) {
        super();
        setHighlightType(HighlightType.COMPONENT);
        this.setPreferredSize(new Dimension(0, 50));
        this.xmppClient = xmppClient;
        this.friend = friend;
        this.leagueClientUI = leagueClientUI;
        this.setFont(new Font("Dialog", Font.BOLD, 18));
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                clickEvent(evt);
            }
        });
    }

    public GenericFriend getFriend() {
        return friend;
    }

    public void setLastKnownPresence(AbstractPresence lastKnownPresence) {
        this.connectionStatus = lastKnownPresence instanceof OfflinePresence ?
                ConnectionStatus.OFFLINE : lastKnownPresence instanceof MobilePresence ?
                ConnectionStatus.MOBILE : ConnectionStatus.ONLINE;
        this.lastKnownPresence = lastKnownPresence;
        this.repaint();
    }

    public void executeOnClick(Runnable runnable) {
        this.runnable = runnable;
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus == null ? ConnectionStatus.OFFLINE : connectionStatus;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D graphics2D = (Graphics2D) g;

        Dimension dimension = getSize();

        name = friend instanceof OnlineFriend ?
                ((OnlineFriend) friend).getLOLName() :
                String.join(
                        "#",
                        friend.getName().toString(),
                        friend.getTagline().toString()
                );

        String parsedStatus = "";
        if (lastKnownPresence != null) {
            JSONObject raw = lastKnownPresence.getRaw();

            if (raw.has("games") && raw.getJSONObject("games").has("league_of_legends")) {
                JSONObject lolRaw = new JSONObject(raw.getJSONObject("games").getJSONObject("league_of_legends").getString("p"));

                String status = lolRaw.getString("gameStatus");
                String mode = lolRaw.getString("gameQueueType");
                mode = mode == null ? "" : mode;
                //parse status
                if (status.equals("outOfGame")) {
                    parsedStatus = "Online";
                    if (!mode.equals(""))
                        parsedStatus = "In Lobby " + parsedQueueType(mode);
                } else if (status.contains("hosting")) {
                    parsedStatus = "In Lobby" + parsedQueueType(mode);
                } else if (status.equals("championSelect"))
                    parsedStatus = "In Champ Select" + parsedQueueType(mode);
                else if (status.equals("inQueue"))
                    parsedStatus = "In Queue" + parsedQueueType(mode);
                else if (status.equals("inGame")) {
                    parsedStatus = "In Game" + parsedQueueType(mode);
                    if (!mode.contains("TFT"))
                        parsedStatus += " - " + lolRaw.getString("skinname");
                }
                if (raw.getString("show").contains("away"))
                    parsedStatus = "Away";
            } else if (raw.has("games") && raw.getJSONObject("games").has("valorant")) {
                parsedStatus = "Valorant";
            }
        }
        setText(name);

        Color color;
        if (connectionStatus == null) color = ColorPalette.FRIEND_OFFLINE;
        else {
            switch (connectionStatus) {
                case OFFLINE:
                    color = ColorPalette.FRIEND_OFFLINE;
                    break;
                case UNKNOWN:
                    color = ColorPalette.FRIEND_DND;
                    break;
                default:
                    color = ColorPalette.FRIEND_ONLINE;
                    break;
            }
            if (parsedStatus.contains("Champ") || parsedStatus.contains("Queue") || parsedStatus.contains("Game"))
                color = ColorPalette.FRIEND_IN_GAME;
            else if (parsedStatus.contains("Valorant"))
                color = ColorPalette.FRIEND_IN_OTHER_GAME;
            else if (parsedStatus.contains("Away"))
                color = ColorPalette.FRIEND_DND;
        }
        Label statusLabel = new Label(parsedStatus, TextAlign.LEFT, true);
        statusLabel.setForeground(color);
        statusLabel.setFontSize(12);
        statusLabel.setBounds(getWidth() / 20, 5, getWidth(), getHeight() / 6);
        statusLabel.drawTextStandalone(graphics2D);


        int width = getHeight() / 3;
        int height = getHeight() / 3;
        int computedX = getWidth() - width - getWidth() / 20;
        int computedY = getHeight() / 2 - height / 2;
        graphics2D.setColor(color);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.fill(new RoundRectangle2D.Float(computedX, computedY, width, height, 360, 360));

        if (counter == 0) return;

        FontMetrics metrics = graphics2D.getFontMetrics();
        int notificationWidth = metrics.stringWidth(String.valueOf(counter));
        int widthMargin = 8, heightMargin = 2, notificationOffset = 30;
        int computedWidth = notificationWidth + (widthMargin << 1);
        int computedHeight = metrics.getAscent() + (heightMargin << 1);
        int rectangleX = dimension.width - widthMargin - computedWidth - notificationOffset;
        int rectangleY = (dimension.height >> 1) - (computedHeight >> 1);

        graphics2D.setColor(Color.BLACK);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.fill(new RoundRectangle2D.Float(rectangleX + 1, rectangleY + 1, computedWidth, computedHeight, 5, 5));

        graphics2D.setColor(new Color(255, 175, 79));
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.fill(new RoundRectangle2D.Float(rectangleX, rectangleY, computedWidth, computedHeight, 5, 5));

        drawHighlightedText(graphics2D, new Rectangle(rectangleX, rectangleY, computedWidth, computedHeight), String.valueOf(counter));
    }

    private void drawHighlightedText(Graphics2D g, Rectangle rectangle, String text) {
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text);
        int x = rectangle.x + (rectangle.width >> 1) - (width >> 1);
        int y = rectangle.y + (rectangle.height >> 1) + (metrics.getAscent() >> 1) - 1;
        g.setColor(Color.BLACK);
        g.drawString(text, x + 1, y + 1);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    private String parsedQueueType(String type) {
        String returnString = "";
        if (type.contains("ARAM"))
            returnString = " - ARAM";
        else if (type.contains("SOLO"))
            returnString = " - Solo/Duo";
        else if (type.contains("FLEX"))
            returnString = " - Flex";
        else if (type.contains("NORMAL"))
            returnString = " - Normal";
        else if (type.contains("PRACTICETOOL"))
            returnString = " - Practice Tool";

        if (type.contains("TFT")) {
            if (returnString.isEmpty()) {
                if (type.contains("TURBO"))
                    returnString = " - Hyper Roll";
                else if (type.contains("DOUBLE"))
                    returnString = " - Double Up";
                else if (type.contains("RANKED"))
                    returnString = " - Ranked";
            }
            returnString += " TFT";
        }

        return returnString;
    }

    public void increment() {
        this.counter += 1;
        this.repaint();
    }

    public void opened() {
        this.counter = 0;
        this.repaint();
    }

    public void clickEvent(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            LeagueClientUI.service.execute(runnable);
            this.setBackground(Color.GRAY);
            this.repaint();
        } else if (SwingUtilities.isRightMouseButton(e)) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem invite = new JMenuItem(new AbstractAction("Invite Friend") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LedgeEndpoint ledges = leagueClientUI.getLeagueClient().getLedge();
                    SummonerLedge summonerLedge = ledges.getSummoner();
                    PartiesLedge partiesLedge = ledges.getParties();
                    try {
                        Summoner summoner = summonerLedge.resolveSummonerByName(name);
                        partiesLedge.invite(summoner.getPUUID());
                    } catch (IOException | PartyException ex) {
                        Logger.error(ex);
                    }
                }
            });
            JMenuItem remove = new JMenuItem(new AbstractAction("Remove Friend") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    xmppClient.removeFriend(friend.getJID());
                }
            });
            JMenuItem block = new JMenuItem(new AbstractAction("Block User") {
                @Override
                public void actionPerformed(ActionEvent e) {

                    xmppClient.blockUser(friend.getJID());
                    xmppClient.removeFriend(friend.getJID());
                }
            });
            JMenuItem close = new JMenuItem(new AbstractAction("Close Menu") {
                @Override
                public void actionPerformed(ActionEvent e) {

                }
            });
            menu.add(invite);
            menu.add(remove);
            menu.add(block);
            menu.add(close);
            menu.show(this, e.getX(), e.getY());
        }
    }
}
