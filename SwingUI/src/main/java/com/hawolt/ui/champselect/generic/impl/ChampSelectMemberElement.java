package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.async.loader.ResourceManager;
import com.hawolt.client.LeagueClient;
import com.hawolt.client.resources.communitydragon.DataTypeConverter;
import com.hawolt.client.resources.communitydragon.champion.ChampionIndex;
import com.hawolt.client.resources.communitydragon.champion.ChampionSource;
import com.hawolt.client.resources.communitydragon.skin.SkinIndex;
import com.hawolt.client.resources.communitydragon.skin.SkinSource;
import com.hawolt.client.resources.communitydragon.spell.SpellIndex;
import com.hawolt.client.resources.communitydragon.spell.SpellSource;
import com.hawolt.client.resources.ledge.summoner.SummonerLedge;
import com.hawolt.logger.Logger;
import com.hawolt.ui.champselect.ChampSelectIndex;
import com.hawolt.ui.champselect.data.ChampSelectTeam;
import com.hawolt.ui.champselect.data.ChampSelectTeamType;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.champselect.util.ChampSelectMember;
import com.hawolt.ui.champselect.util.ChampSelectTeamMember;
import com.hawolt.ui.impl.Debouncer;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.jhlab.GaussianFilter;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created: 30/08/2023 17:03
 * Author: Twitter @hawolt
 **/

public class ChampSelectMemberElement extends ChampSelectUIComponent implements Runnable, DataTypeConverter<byte[], BufferedImage> {
    private static final String SPRITE_PATH = "https://raw.communitydragon.org/pbe/plugins/rcp-be-lol-game-data/global/default/v1/champion-splashes/%s/%s.jpg";
    private static final Dimension SUMMONER_SPELL_DIMENSION = new Dimension(28, 28);
    private final static GaussianFilter filter = new GaussianFilter(10);

    private final ResourceManager<BufferedImage> manager = new ResourceManager<>(this);
    private final ChampSelectTeamType teamType;
    private final ChampSelectTeam team;

    private int championId, indicatorId, skinId, spell1Id, spell2Id;
    private BufferedImage sprite, spellOne, spellTwo, champion;
    private ChampSelectMember member;
    private String name, hero;

    @Override
    public void run() {
        if (!(member instanceof ChampSelectTeamMember teamMember)) return;
        LeagueClient client = index.getLeagueClient();
        this.name = String.valueOf(teamMember.getSummonerId());
        if (client != null) {
            SummonerLedge summonerLedge = client.getLedge().getSummoner();
            try {
                this.name = summonerLedge.resolveSummonerByPUUD(teamMember.getPUUID()).getName();
            } catch (IOException e) {
                Logger.error("Failed to retrieve name for {}", teamMember.getPUUID());
            }
        }
        index.cache(teamMember.getPUUID(), name);
    }

    private class ChampSelectSelectMemberResizeAdapter extends ComponentAdapter {
        private static final Debouncer debouncer = new Debouncer();

        @Override
        public void componentResized(ComponentEvent e) {
            if (sprite == null) return;
            debouncer.debounce(
                    String.valueOf(member.getCellId()),
                    ChampSelectMemberElement.this::adjust,
                    200, TimeUnit.MILLISECONDS
            );
        }
    }

    private void adjust() {
        Dimension dimension = getSize();
        champion = Scalr.resize(sprite, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, dimension.width);
        repaint();
    }

    public ChampSelectMemberElement(ChampSelectTeamType teamType, ChampSelectTeam team, ChampSelectMember member) {
        this.addComponentListener(new ChampSelectSelectMemberResizeAdapter());
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.teamType = teamType;
        this.member = member;
        this.team = team;
    }

    private void setChampion(int championId) {
        ChampionIndex championIndex = ChampionSource.CHAMPION_SOURCE_INSTANCE.get();
        this.hero = championIndex.getChampion(championId).getName();
    }

    private void setChampionId(int championId) {
        this.championId = championId;
        this.setChampion(championId);
        this.updateSprite(championId, championId * 1000);
    }

    private void updateChamp(int championId) {
        if (championId == 0) return;
        setChampionId(championId);
    }

    private void updateChamp(ChampSelectMember member) {
        if (this.championId == member.getChampionId()) return;
        if (member.getChampionId() == 0) return;
        setChampionId(member.getChampionId());
    }

    private void updateChampIndicator(ChampSelectTeamMember member) {
        if (this.indicatorId == member.getChampionPickIntent()) return;
        this.indicatorId = member.getChampionPickIntent();
        if (indicatorId != 0) {
            this.setChampion(indicatorId);
            this.updateSprite(indicatorId, indicatorId * 1000);
        } else if (member.getChampionId() == 0 && !isPicking()) {
            this.champion = null;
            this.sprite = null;
        }
    }

    //
    private void updateSkin(ChampSelectTeamMember member) {
        if (this.skinId == member.getSkinId()) return;
        if (member.getSkinId() != 0) {
            this.skinId = member.getSkinId();
            SkinIndex skinIndex = SkinSource.SKIN_SOURCE_INSTANCE.get();
            this.updateSprite(member.getChampionId(), skinIndex.getSkin(member.getSkinId()).getId());
        }
    }

    private void updateSprite(int championId, int skinId) {
        manager.load(
                String.format(
                        SPRITE_PATH,
                        championId,
                        skinId
                ),
                bufferedImage -> {
                    BufferedImage crop = Scalr.crop(bufferedImage, 300, 50, 640, 400);
                    ChampSelectMemberElement.this.sprite = filter.filter(crop, null);
                    this.adjust();
                }
        );
    }

    private void updateSpellOne(ChampSelectTeamMember member) {
        if (this.spell1Id == member.getSpell1Id()) return;
        if (member.getSpell1Id() != 0) {
            this.spell1Id = member.getSpell1Id();
            SpellIndex spellIndex = SpellSource.SPELL_SOURCE_INSTANCE.get();
            manager.load(
                    spellIndex.getSpell(spell1Id).getIconPath(),
                    bufferedImage -> {
                        ChampSelectMemberElement.this.spellOne = Scalr.resize(
                                bufferedImage,
                                Scalr.Method.ULTRA_QUALITY,
                                Scalr.Mode.FIT_TO_HEIGHT,
                                SUMMONER_SPELL_DIMENSION.width,
                                SUMMONER_SPELL_DIMENSION.height
                        );
                    }
            );
        }
    }

    private void updateSpellTwo(ChampSelectTeamMember member) {
        if (this.spell2Id == member.getSpell2Id()) return;
        if (member.getSpell2Id() != 0) {
            this.spell2Id = member.getSpell2Id();
            SpellIndex spellIndex = SpellSource.SPELL_SOURCE_INSTANCE.get();
            manager.load(
                    spellIndex.getSpell(spell2Id).getIconPath(),
                    bufferedImage -> {
                        ChampSelectMemberElement.this.spellTwo = Scalr.resize(
                                bufferedImage,
                                Scalr.Method.ULTRA_QUALITY,
                                Scalr.Mode.FIT_TO_HEIGHT,
                                SUMMONER_SPELL_DIMENSION.width,
                                SUMMONER_SPELL_DIMENSION.height
                        );
                    }
            );
        }
    }

    public void update(ChampSelectMember member) {
        this.member = member;
        this.updateChamp(member);
        if (teamType == ChampSelectTeamType.ALLIED) {
            ChampSelectTeamMember teamMember = (ChampSelectTeamMember) member;
            this.updateChampIndicator(teamMember);
            this.updateSpellOne(teamMember);
            this.updateSpellTwo(teamMember);
            this.updateSkin(teamMember);
            if (index != null && isPicking()) {
                index.getCurrent()
                        .stream()
                        .filter(actionObject -> actionObject.getActorCellId() == member.getCellId())
                        .findAny()
                        .ifPresent(actionObject -> {
                            if (index.getCurrentActionSetIndex() > 0) {
                                updateChamp(actionObject.getChampionId());
                            }
                        });
            }
        }
        this.repaint();
    }

    private static final Color HIGHLIGHTER = new Color(0xFF, 0xFF, 0xFF, 0x4F);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (index == null) return;

        Dimension dimension = getSize();

        Color color = isPicking() ? Color.PINK : Color.ORANGE;
        g.setColor(isSelf() ? color.brighter() : color);

        //DRAW CHAMPION/SKIN IMAGE
        if (champion != null) {
            int imageX = (dimension.width >> 1) - (champion.getWidth() >> 1);
            int imageY = (dimension.height >> 1) - (champion.getHeight() >> 1);
            g.drawImage(champion, imageX, imageY, null);
        }

        //INDICATE STATUS NOT LOCKED IN
        if (!isLockedIn() || index.getCurrentActionSetIndex() <= 0) {
            g.setColor(HIGHLIGHTER);
            g.fillRect(0, 0, dimension.width, dimension.height);
        }

        //INDICATOR TO SHOW WHO WE ARE
        if (isSelf()) {
            int baseX = switch (team) {
                case BLUE -> dimension.width - (SUMMONER_SPELL_DIMENSION.width * 3) - (5 * 3);
                case PURPLE -> 5 + (SUMMONER_SPELL_DIMENSION.width << 1) + (5 << 1);
            };
            g.setColor(Color.BLACK);
            g.fillRect(baseX - 1, 5 - 1, SUMMONER_SPELL_DIMENSION.width + 2, SUMMONER_SPELL_DIMENSION.height + 2);
            g.setColor(!isPicking() ? Color.RED : Color.GREEN);
            g.fillRect(baseX, 5, SUMMONER_SPELL_DIMENSION.width, SUMMONER_SPELL_DIMENSION.height);
        }

        //DRAW SUMMONER SPELLS
        if (spellOne != null && spellTwo != null) paintSummonerSpells(g);

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics2D.setFont(new Font("Dialog", Font.BOLD, 20));
        graphics2D.setColor(Color.BLACK);
        FontMetrics metrics = graphics2D.getFontMetrics();

        //DRAW ROLES
        if (teamType == ChampSelectTeamType.ALLIED) {
            ChampSelectTeamMember teamMember = (ChampSelectTeamMember) member;

            String position = !"UTILITY".equals(teamMember.getAssignedPosition()) ? teamMember.getAssignedPosition() : "SUPPORT";
            int positionWidth = metrics.stringWidth(position);
            int positionX = switch (team) {
                case BLUE -> 5;
                case PURPLE -> dimension.width - 5 - positionWidth;
            };
            drawTextWithShadow(graphics2D, position, positionX, 5 + metrics.getAscent());
            String name = switch (member.getNameVisibilityType()) {
                case "HIDDEN" -> getHiddenName();
                default -> String.valueOf(this.name != null ? this.name : member.getSummonerId());
            };
            int nameX = switch (team) {
                case BLUE -> dimension.width - 5 - metrics.stringWidth(name);
                case PURPLE -> 5;
            };
            drawTextWithShadow(graphics2D, name, nameX, dimension.height - 7);
        }

        //DRAW CHAMPION NAME
        if (hero != null) {
            int heroX = switch (team) {
                case BLUE -> 5;
                case PURPLE -> dimension.width - 5 - metrics.stringWidth(hero);
            };
            drawTextWithShadow(graphics2D, hero, heroX, dimension.height - 7);
        }


        g.setColor(Color.BLACK);
        g.drawRect(0, 0, dimension.width - 1, dimension.height - 1);
    }

    public String getHiddenName() {
        return switch (member.getCellId() % 5) {
            case 0 -> "Raptor";
            case 1 -> "Krug";
            case 2 -> "Murk Wolf";
            case 3 -> "Gromp";
            default -> "Scuttle Crab";
        };
    }

    private void drawTextWithShadow(Graphics2D graphics2D, String text, int x, int y) {
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawString(text, x + 1, y + 1);
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString(text, x, y);
    }

    private void paintSummonerSpells(Graphics g) {
        Dimension dimension = getSize();
        g.setColor(Color.BLACK);
        switch (team) {
            case BLUE -> {
                int baseX = dimension.width - 5 - SUMMONER_SPELL_DIMENSION.width;
                g.drawImage(spellTwo, baseX, 5, null);
                g.drawRect(baseX, 5, SUMMONER_SPELL_DIMENSION.width, SUMMONER_SPELL_DIMENSION.height);
                g.drawImage(spellOne, baseX - 5 - SUMMONER_SPELL_DIMENSION.width, 5, null);
                g.drawRect(baseX - 5 - SUMMONER_SPELL_DIMENSION.width, 5, SUMMONER_SPELL_DIMENSION.width, SUMMONER_SPELL_DIMENSION.height);
            }
            case PURPLE -> {
                int baseX = 5;
                g.drawImage(spellOne, baseX, 5, null);
                g.drawRect(baseX, 5, SUMMONER_SPELL_DIMENSION.width, SUMMONER_SPELL_DIMENSION.height);
                g.drawImage(spellTwo, baseX + SUMMONER_SPELL_DIMENSION.width + 5, 5, null);
                g.drawRect(baseX + SUMMONER_SPELL_DIMENSION.width + 5, 5, SUMMONER_SPELL_DIMENSION.width, SUMMONER_SPELL_DIMENSION.height);
            }
        }
    }


    private boolean isSelf() {
        return member.getCellId() == index.getLocalPlayerCellId();
    }

    private boolean isLockedIn() {
        if (index.isFinalizing()) return true;
        if (index == null || index.getCurrentActionSetIndex() < 0) return false;
        return index.getActionSetMapping().values()
                .stream()
                .skip(1)
                .flatMap(Collection::stream)
                .anyMatch(actionObject -> actionObject.getActorCellId() == member.getCellId() && actionObject.isCompleted());
    }

    private boolean isPicking() {
        if (index.isFinalizing()) return false;
        if (index == null || index.getCurrentActionSetIndex() < 0) return false;
        return index.getActionSetMapping().get(index.getCurrentActionSetIndex())
                .stream()
                .anyMatch(actionObject -> actionObject.getActorCellId() == member.getCellId() && !actionObject.isCompleted());
    }

    public void setIndex(ChampSelectIndex index) {
        this.index = index;
    }

    @Override
    public BufferedImage apply(byte[] b) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(b));
    }
}
