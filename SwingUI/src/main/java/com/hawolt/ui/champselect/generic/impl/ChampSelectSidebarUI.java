package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.async.ExecutorManager;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.data.ChampSelectTeamType;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.champselect.util.ChampSelectMember;
import com.hawolt.ui.champselect.util.ChampSelectTeamMember;
import com.hawolt.ui.champselect.util.MemberFunction;
import com.hawolt.ui.champselect.util.TeamMemberFunction;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created: 30/08/2023 16:07
 * Author: Twitter @hawolt
 **/

public class ChampSelectSidebarUI extends ChampSelectUIComponent {
    private final Map<Integer, ChampSelectMemberElement> map = new HashMap<>();
    private final ChildUIComponent display;

    protected final ChildUIComponent main;
    protected final ChampSelectTeam team;
    protected ChampSelectTeamType type;

    public ChampSelectSidebarUI(ChampSelectTeam team) {
        this.team = team;
        this.setLayout(new BorderLayout());
        this.display = new ChildUIComponent();
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.main = new ChildUIComponent(new BorderLayout());
        this.setPreferredSize(new Dimension(300, 0));
        this.display.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.main.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.main.add(display, BorderLayout.CENTER);
        this.add(main, BorderLayout.CENTER);
        this.display.setBorder(
                BorderFactory.createCompoundBorder(
                        new MatteBorder(1, 0, 0, 0, Color.BLACK),
                        new EmptyBorder(5, 5, 5, 5)
                )
        );
    }

    @Override
    public void init() {
        this.map.clear();
        this.display.removeAll();
        this.type = getChampSelectTeamType();
        ChampSelectMember[] members = get(type);
        this.display.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.display.setLayout(new GridLayout(members.length, 0, 0, 5));
        for (ChampSelectMember member : members) {
            ExecutorService loader = ExecutorManager.getService("name-loader");
            ChampSelectMemberElement element = new ChampSelectMemberElement(type, team, member);
            map.put(member.getCellId(), element);
            element.setIndex(index);
            loader.execute(element);
            this.display.add(element);
        }
        revalidate();
    }

    @Override
    public void update() {
        for (ChampSelectMember member : get(type)) {
            map.get(member.getCellId()).update(member);
        }
    }

    private ChampSelectMember[] get(ChampSelectTeamType type) {
        switch (type) {
            case ALLIED -> {
                return index.getCells(ChampSelectTeamType.ALLIED, TeamMemberFunction.INSTANCE);

            }
            case ENEMY -> {
                return index.getCells(ChampSelectTeamType.ENEMY, MemberFunction.INSTANCE);
            }
        }
        return new ChampSelectMember[0];
    }

    @NotNull
    private ChampSelectTeamType getChampSelectTeamType() {
        ChampSelectTeamMember self = index.getSelf();
        int alliedTeamId = self.getTeamId();
        ChampSelectTeamType type;
        switch (team) {
            case BLUE -> type = alliedTeamId == 1 ? ChampSelectTeamType.ALLIED : ChampSelectTeamType.ENEMY;
            case PURPLE -> type = alliedTeamId == 2 ? ChampSelectTeamType.ALLIED : ChampSelectTeamType.ENEMY;
            default -> throw new IllegalStateException("Unexpected value: " + team);
        }
        return type;
    }
}
