package com.hawolt.ui.queue;

import com.hawolt.async.ExecutorManager;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.util.ui.Label;
import com.hawolt.util.ui.TextAlign;

import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created: 11/08/2023 20:41
 * Author: Twitter @hawolt
 **/

public class QueueState extends ChildUIComponent {
    private final ScheduledExecutorService service = ExecutorManager.getScheduledService("queue-state");
    private final Font font = new Font("Arial", Font.BOLD, 20);
    private long currentTimeMillis, estimatedMatchmakingTimeMillis;
    private String estimate;
    private boolean lpq;

    public QueueState() {
        super(new BorderLayout());
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.setPreferredSize(new Dimension(0, 30));
        this.setBorder(new MatteBorder(0, 0, 2, 0, Color.DARK_GRAY));
        this.service.scheduleAtFixedRate(this::repaint, 0, 20, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;

        long elapsed = System.currentTimeMillis() - currentTimeMillis;
        String total = convertMStoTimestamp(elapsed);

        Label elapsedLabel = new Label(total, TextAlign.RIGHT, true);
        elapsedLabel.setBounds(0, 0, getWidth(), getHeight());
        elapsedLabel.setFontSize(25);
        elapsedLabel.drawTextStandalone(graphics2D);

        Label estimateLabel = new Label(String.format("Ø %s", estimate), TextAlign.LEFT, true);
        estimateLabel.setBounds(0, 0, getWidth(), getHeight());
        estimateLabel.setFontSize(25);
        estimateLabel.drawTextStandalone(graphics2D);
    }

    private String convertMStoTimestamp(long elapsed) {
        long hours = TimeUnit.MILLISECONDS.toHours(elapsed);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsed));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed));
        String secondsAsText = String.format("%02d", seconds);
        String minutesAsText = hours > 0 ? String.format("%02d", minutes) : String.valueOf(minutes);
        StringBuilder duration = new StringBuilder();
        if (hours > 0) duration.append(hours).append(":");
        duration.append(minutesAsText).append(":");
        duration.append(secondsAsText);
        return duration.toString();
    }

    public boolean isLPQ() {
        return lpq;
    }

    public void updateLPQ(long estimatedMatchmakingTimeMillis) {
        long currentEstimate = this.estimatedMatchmakingTimeMillis;
        this.estimatedMatchmakingTimeMillis = estimatedMatchmakingTimeMillis + currentEstimate;
        this.setEstimate();
        this.lpq = false;
    }

    private void setEstimate() {
        if (estimatedMatchmakingTimeMillis != -1) {
            this.estimate = convertMStoTimestamp(estimatedMatchmakingTimeMillis);
        } else {
            this.estimate = null;
        }
    }

    public void setTimer(long currentTimeMillis, long estimatedMatchmakingTimeMillis, boolean lpq) {
        this.estimatedMatchmakingTimeMillis = estimatedMatchmakingTimeMillis;
        this.currentTimeMillis = currentTimeMillis;
        this.setEstimate();
        this.lpq = lpq;
    }
}
