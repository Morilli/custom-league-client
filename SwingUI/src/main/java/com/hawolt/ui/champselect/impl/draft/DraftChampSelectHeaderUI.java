package com.hawolt.ui.champselect.impl.draft;

import com.hawolt.async.ExecutorManager;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.champselect.util.ChampSelectPhase;
import com.hawolt.util.ColorPalette;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created: 31/08/2023 17:17
 * Author: Twitter @hawolt
 **/

public class DraftChampSelectHeaderUI extends ChampSelectUIComponent {
    private final ScheduledExecutorService scheduler = ExecutorManager.getScheduledService(getClass().getSimpleName());
    private double currentTotalTimeMillis, currentTimeRemainingMillis, timestamp;
    private ChampSelectPhase phase;

    public DraftChampSelectHeaderUI() {
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.setPreferredSize(new Dimension(0, 60));
        this.scheduler.scheduleAtFixedRate(this::repaint, 0, 20, TimeUnit.MILLISECONDS);
        this.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 1, 0, 1, Color.BLACK),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dimension = getSize();

        if (index != null && timestamp != 0L) {
            double difference = currentTotalTimeMillis - currentTimeRemainingMillis + (System.currentTimeMillis() - timestamp);
            double percentage = difference / currentTotalTimeMillis;
            if (percentage < 0) percentage = 0;
            int centerX = (dimension.width >> 1);
            int extend = (int) Math.ceil(centerX * percentage);
            g.setColor(Color.WHITE);
            g.fillRect(centerX, dimension.height - 5, extend, 5);
            g.fillRect(centerX - extend, dimension.height - 5, extend, 5);
        }

        if (phase == null) return;
        String status = switch (phase) {
            case PLAN -> "DECLARE YOUR CHAMPION";
            case IDLE -> "WAITING FOR OTHERS";
            case BAN -> "BAN A CHAMPION";
            case PICK -> "SELECT YOUR CHAMPION";
            case FINALIZE -> "PREPARE FOR BATTLE";
        };
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setColor(Color.WHITE);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics metrics = graphics2D.getFontMetrics();
        int width = metrics.stringWidth(status);
        graphics2D.drawString(status, (dimension.width >> 1) - (width >> 1), (dimension.height >> 1) + (metrics.getAscent() >> 1));
    }

    @Override
    public void update() {
        this.currentTimeRemainingMillis = index.getCurrentTimeRemainingMillis();
        this.currentTotalTimeMillis = index.getCurrentTotalTimeMillis();
        this.timestamp = System.currentTimeMillis();
        if (index.isFinalizing()) {
            this.phase = ChampSelectPhase.FINALIZE;
        } else {
            switch (index.getCurrentActionSetIndex()) {
                case -1 -> this.phase = ChampSelectPhase.PLAN;
                case 0 -> index.getOwnBanPhase().ifPresent(actionObject -> {
                    this.phase = actionObject.isCompleted() ? ChampSelectPhase.PLAN : ChampSelectPhase.BAN;
                });
                default -> {
                    boolean pick = index.getActionSetMapping().get(index.getCurrentActionSetIndex())
                            .stream()
                            .anyMatch(o -> o.getActorCellId() == index.getLocalPlayerCellId() && !o.isCompleted());
                    this.phase = pick ? ChampSelectPhase.PICK : ChampSelectPhase.IDLE;
                }
            }
        }
    }
}
