package com.hawolt.client.resources.platform.history.object;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created: 14/01/2023 23:10
 * Author: Twitter @hawolt
 **/

public class MatchOutcomeParticipant {
    private final short creepScore, championId, teamId, spell1Id, spell2Id, wards;
    private final byte level, participantId, kills, deaths, assists, champLevel;
    private final int goldEarned, totalDamageDealt;
    private final int[] items = new int[7];
    private final String puuid, summonerName;
    private final boolean win;
    private short primary, secondary;

    public MatchOutcomeParticipant(JSONObject o) {
        this.summonerName = o.getString("summonerName");
        this.puuid = o.getString("puuid");
        this.championId = (short) o.getInt("championId");
        this.teamId = (short) o.getInt("teamId");
        this.spell1Id = (short) o.getInt("spell1Id");
        this.spell2Id = (short) o.getInt("spell2Id");
        this.wards = (short) o.getInt("visionWardsBoughtInGame");
        this.creepScore = (short) (o.getInt("totalMinionsKilled") + o.getInt("neutralMinionsKilled"));
        this.level = (byte) o.getInt("champLevel");
        this.participantId = (byte) o.getInt("participantId");
        this.kills = (byte) o.getInt("kills");
        this.deaths = (byte) o.getInt("deaths");
        this.assists = (byte) o.getInt("assists");
        this.champLevel = (byte) o.getInt("champLevel");
        this.goldEarned = o.getInt("goldEarned");
        this.totalDamageDealt = o.getInt("totalDamageDealtToChampions");
        this.win = o.getBoolean("win");
        for (int i = 0; i < items.length; i++) {
            items[i] = o.getInt("item" + i);
        }
        JSONObject perks = o.getJSONObject("perks");
        JSONArray styles = perks.getJSONArray("styles");
        Map<Byte, JSONObject> map = new HashMap<>();
        for (int i = 0; i < styles.length(); i++) {
            JSONObject style = styles.getJSONObject(i);
            int index = style.getString("description").equals("primaryStyle") ? 0 : 1;
            map.put((byte) index, style);
        }
        if (map.containsKey((byte) 0))
            this.primary = (short) map.get((byte) 0).getJSONArray("selections").getJSONObject(0).getInt("perk");
        if (map.containsKey((byte) 1)) this.secondary = (short) map.get((byte) 1).getInt("style");
    }

    public int getItem(int i) {
        return items[i];
    }

    public short getCreepScore() {
        return creepScore;
    }

    public short getChampionId() {
        return championId;
    }

    public short getTeamId() {
        return teamId;
    }

    public short getSpell1Id() {
        return spell1Id;
    }

    public short getSpell2Id() {
        return spell2Id;
    }

    public short getWards() {
        return wards;
    }

    public short getPrimary() {
        return primary;
    }

    public short getSecondary() {
        return secondary;
    }

    public byte getLevel() {
        return level;
    }

    public byte getParticipantId() {
        return participantId;
    }

    public byte getKills() {
        return kills;
    }

    public byte getDeaths() {
        return deaths;
    }

    public byte getAssists() {
        return assists;
    }

    public byte getChampLevel() {
        return champLevel;
    }

    public int getGoldEarned() {
        return goldEarned;
    }

    public int getTotalDamageDealt() {
        return totalDamageDealt;
    }

    public int[] getItems() {
        return items;
    }

    public String getPUUID() {
        return puuid;
    }

    public boolean isWin() {
        return win;
    }

    public String getSummonerName() {
        return summonerName;
    }

    @Override
    public String toString() {
        return "MatchOutcomeParticipant{" +
                "creepScore=" + creepScore +
                ", championId=" + championId +
                ", teamId=" + teamId +
                ", spell1Id=" + spell1Id +
                ", spell2Id=" + spell2Id +
                ", wards=" + wards +
                ", primary=" + primary +
                ", secondary=" + secondary +
                ", level=" + level +
                ", participantId=" + participantId +
                ", kills=" + kills +
                ", deaths=" + deaths +
                ", assists=" + assists +
                ", champLevel=" + champLevel +
                ", goldEarned=" + goldEarned +
                ", totalDamageDealt=" + totalDamageDealt +
                ", items=" + Arrays.toString(items) +
                ", puuid='" + puuid + '\'' +
                ", summonerName='" + summonerName + '\'' +
                ", win=" + win +
                '}';
    }
}
