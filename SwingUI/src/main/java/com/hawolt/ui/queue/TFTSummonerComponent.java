package com.hawolt.ui.queue;

import com.hawolt.async.loader.ResourceConsumer;
import com.hawolt.async.loader.ResourceLoader;
import com.hawolt.client.resources.ledge.parties.objects.PartyMember;
import com.hawolt.client.resources.ledge.parties.objects.PartyParticipant;
import com.hawolt.client.resources.ledge.parties.objects.PartyParticipantMetadata;
import com.hawolt.client.resources.ledge.summoner.objects.Summoner;
import com.hawolt.logger.Logger;
import com.hawolt.util.panel.ChildUIComponent;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Created: 21/08/2023 18:34
 * Author: Twitter @hawolt
 **/

public class TFTSummonerComponent extends ChildUIComponent implements ResourceConsumer<BufferedImage, byte[]> {
    private static final String CD_PP_BASE = "https://raw.communitydragon.org/latest/game/assets/ux/summonericons/profileicon%s.png";
    private static final Dimension IMAGE_DIMENSION = new Dimension(128, 128);
    private static final Font NAME_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font ROLE_FONT = new Font("Arial", Font.BOLD, 16);
    private PartyParticipant participant;
    private BufferedImage image;
    private Summoner summoner;

    public TFTSummonerComponent() {
        super(null);
    }

    public void update(PartyParticipant participant, Summoner summoner) {
        this.participant = participant;
        this.summoner = summoner;
        this.repaint();
        if (summoner == null) return;
        ResourceLoader.loadResource(String.format(CD_PP_BASE, summoner.getProfileIconId()), this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (summoner == null || participant == null) return;
        String role = participant.getRole();
        if (!role.equals("MEMBER") && !role.equals("LEADER")) return;
        Dimension dimension = getSize();
        g.setColor(Color.GRAY);
        g.fillRect(3, 3, dimension.width - 7, dimension.height - 7);
        g.setColor(Color.BLACK);
        g.drawRect(3, 3, dimension.width - 7, dimension.height - 7);
        int centeredX = dimension.width >> 1;
        int centeredY = dimension.height >> 1;
        if (image != null) {
            int imageX = centeredX - (image.getWidth() >> 1);
            int imageY = (dimension.height >> 1) - (image.getHeight() >> 1);
            g.setColor(Color.BLACK);
            int imageSpacing = 3;
            g.fillRect(
                    imageX - imageSpacing,
                    imageY - imageSpacing,
                    image.getWidth() + (imageSpacing << 1),
                    image.getHeight() + (imageSpacing << 1)
            );
            g.drawImage(image, imageX, imageY, null);
        }
        FontMetrics metrics;
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setFont(NAME_FONT);
        metrics = graphics2D.getFontMetrics();
        String name = summoner.getName().trim();
        int width = metrics.stringWidth(name);
        int nameX = centeredX - (width >> 1);
        g.drawString(name, nameX, centeredY - (IMAGE_DIMENSION.height >> 1) - 20);

    }


    @Override
    public void onException(Object o, Exception e) {
        Logger.fatal("Failed to load resource {}, {}", o);
        Logger.error(e);
    }

    @Override
    public void consume(Object o, BufferedImage image) {
        if (image.getHeight() != 128 || image.getWidth() != 128) {
            this.image = Scalr.resize(
                    image,
                    Scalr.Method.ULTRA_QUALITY,
                    Scalr.Mode.FIT_TO_HEIGHT,
                    IMAGE_DIMENSION.width,
                    IMAGE_DIMENSION.height
            );
        } else {
            this.image = image;
        }
        this.repaint();
    }

    @Override
    public BufferedImage transform(byte[] bytes) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
