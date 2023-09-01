package com.hawolt.ui.champselect.generic.impl;

import com.hawolt.client.resources.communitydragon.spell.Spell;
import com.hawolt.ui.champselect.data.ChampSelectType;

/**
 * Created: 29/08/2023 22:36
 * Author: Twitter @hawolt
 **/

public interface ChampSelectChoice {

    void onSummonerSubmission(Spell selectedSpellOne, Spell selectedSpellTwo);

    void onChoiceSubmission(ChampSelectType type, int championId, boolean completed);

    void onChoice(ChampSelectSelectionElement element);
}
