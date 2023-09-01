package com.hawolt.ui.champselect.impl.draft;

import com.hawolt.client.resources.communitydragon.spell.Spell;
import com.hawolt.client.resources.communitydragon.spell.SpellIndex;
import com.hawolt.client.resources.communitydragon.spell.SpellSource;
import com.hawolt.ui.champselect.LHintTextField;
import com.hawolt.ui.champselect.generic.ChampSelectUIComponent;
import com.hawolt.ui.impl.Debouncer;
import com.hawolt.util.ColorPalette;
import com.hawolt.util.panel.ChildUIComponent;
import com.hawolt.util.ui.FlatButton;
import com.hawolt.util.ui.HighlightType;
import com.hawolt.util.ui.TextAlign;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created: 31/08/2023 17:41
 * Author: Twitter @hawolt
 **/

public class DraftGameSettingUI extends ChampSelectUIComponent {
    private final Debouncer debouncer = new Debouncer();
    private final JComboBox<Spell> spellOne, spellTwo;
    private final FlatButton submit, runes;

    //TODO find a data source for this
    private static final List<Integer> temporaryWhiteList = Arrays.asList(1, 3, 4, 6, 7, 11, 12, 13, 14, 21);
    private static Spell[] allowed;

    static {
        SpellIndex spellIndex = SpellSource.SPELL_SOURCE_INSTANCE.get();
        Spell[] spells = spellIndex.getAvailableSpells();
        List<Spell> list = new ArrayList<>();
        for (Spell spell : spells) {
            if (!temporaryWhiteList.contains(spell.getId())) continue;
            list.add(spell);
        }
        DraftGameSettingUI.allowed = list.toArray(Spell[]::new);
    }

    public DraftGameSettingUI() {
        this.setLayout(new BorderLayout());
        this.setBackground(ColorPalette.BACKGROUND_COLOR);
        this.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Color.BLACK));
        ChildUIComponent spellUI = new ChildUIComponent(new GridLayout(0, 2, 5, 0));
        spellUI.add(spellOne = new JComboBox<>(allowed));
        spellUI.add(spellTwo = new JComboBox<>(allowed));
        spellUI.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(spellUI, BorderLayout.EAST);
        ChildUIComponent buttonUI = new ChildUIComponent(new GridLayout(0, 3, 5, 0));
        buttonUI.add(submit = new FlatButton("Submit Choice", TextAlign.CENTER, HighlightType.COMPONENT));
        buttonUI.add(runes = new FlatButton("Configure Runes", TextAlign.CENTER, HighlightType.COMPONENT));
        LHintTextField filter = new LHintTextField("Search...");
        filter.getDocument().addDocumentListener(new DocumentListener() {
            private void forward(String text) {
                debouncer.debounce(
                        "filter",
                        () -> index.filterChampion(text),
                        200L,
                        TimeUnit.MILLISECONDS
                );
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                forward(filter.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                forward(filter.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                forward(filter.getText());
            }
        });
        buttonUI.add(filter);
        add(buttonUI, BorderLayout.WEST);
    }

    public JComboBox<Spell> getSpellOne() {
        return spellOne;
    }

    public Spell getSelectedSpellOne() {
        return spellOne.getItemAt(spellOne.getSelectedIndex());
    }

    public JComboBox<Spell> getSpellTwo() {
        return spellTwo;
    }

    public Spell getSelectedSpellTwo() {
        return spellTwo.getItemAt(spellTwo.getSelectedIndex());
    }

    public FlatButton getSubmitButton() {
        return submit;
    }

    public FlatButton getRuneButton() {
        return runes;
    }
}
