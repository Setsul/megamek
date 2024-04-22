/*
 * Copyright (c) 2023 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.client.ui.swing;

import megamek.client.ui.swing.boardview.TurnDetailsOverlay;
import megamek.client.ui.swing.util.KeyCommandBind;
import megamek.client.ui.swing.util.UIUtil;
import megamek.client.ui.swing.widget.MegamekButton;
import megamek.client.ui.swing.widget.SkinSpecification;
import megamek.common.annotations.Nullable;
import megamek.common.preference.PreferenceChangeEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import static megamek.client.ui.swing.util.UIUtil.guiScaledFontHTML;

public abstract class ActionPhaseDisplay extends StatusBarPhaseDisplay {
    protected MegamekButton butSkipTurn;
    private boolean isDoingAction = false;
    private boolean ignoreNoActionNag = false;

    protected ActionPhaseDisplay(ClientGUI cg) {
        super(cg);
    }

    @Override
    protected UIUtil.FixedXPanel setupDonePanel() {
        var donePanel = super.setupDonePanel();
        butSkipTurn = new MegamekButton("SKIP", SkinSpecification.UIComponents.PhaseDisplayDoneButton.getComp());
        butSkipTurn.setPreferredSize(new Dimension(UIUtil.scaleForGUI(DONE_BUTTON_WIDTH), MIN_BUTTON_SIZE.height * 1));
        String f = guiScaledFontHTML(UIUtil.uiLightViolet()) +  KeyCommandBind.getDesc(KeyCommandBind.DONE_NO_ACTION)+ "</FONT>";
        butSkipTurn.setToolTipText("<html><body>" + f + "</body></html>");
        addToDonePanel(donePanel, butSkipTurn);

        if (clientgui != null) {
            butSkipTurn.addActionListener(new AbstractAction() {
                private static final long serialVersionUID = -5034474968902280850L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isIgnoringEvents()) {
                        return;
                    }
                    if ((clientgui.getClient().isMyTurn())
                            || (clientgui.getClient().getGame().getTurn() == null)
                            || (clientgui.getClient().getGame().getPhase().isReport())) {
                        // act like Done button
                        performDoneNoAction();
                        // When the turn is ended, we could miss a key release event
                        // This will ensure no repeating keys are stuck down
                        clientgui.controller.stopAllRepeating();
                    }
                }
            });

            clientgui.controller.registerCommandAction(KeyCommandBind.DONE_NO_ACTION, this::shouldReceiveDoneKeyCommand,
                    this::performDoneNoAction);
        }

        updateDonePanel();
        return donePanel;
    }

    private void performDoneNoAction() {
        ignoreNoActionNag = true;
        ready();
    }

    public boolean shouldReceiveDoneKeyCommand() {
        return ((clientgui.getClient().isMyTurn()
                || (clientgui.getClient().getGame().getTurn() == null)
                || (clientgui.getClient().getGame().getPhase().isReport())))
                && !clientgui.getBoardView().getChatterBoxActive()
                && !isIgnoringEvents()
                && isVisible()
                && (butDone.isEnabled() || butSkipTurn.isEnabled());
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent e) {
        super.preferenceChange(e);
        updateDonePanel();
        adaptToGUIScale();
    }

    protected void initDonePanelForNewTurn() {
        ignoreNoActionNag = false;
        updateDonePanel();
    }

    /** called to reset, show, hide and relabel the Done panel buttons. Override to change button labels and states,
     * being sure to call {@link #updateDonePanelButtons(String,String,boolean) UpdateDonePanelButtons}
     * to set the button labels and states
     */
    abstract protected void updateDonePanel();

    /**
     * @return true if a nag dialog should be shown when there is no action given to current unit.
     * This is true if user option wants a nag
     * they have not preemptively checked @butIgnoreNag
     * the turn timer is not expired
     */
    protected boolean needNagForNoAction() {
        return GUIP.getNagForNoAction() && !ignoreNoActionNag && !isTimerExpired();
    }

    protected boolean needNagForOverheat() {
        return GUIP.getNagForOverheat() && !isTimerExpired();
    }

    protected boolean needNagForNoUnJamRAC() {
        return GUIP.getNagForNoUnJamRAC() && !isTimerExpired();
    }

    protected boolean needNagForMASC() {
        return GUIP.getNagForMASC() && !isTimerExpired();
    }

    protected boolean needNagForPSR() {
        return GUIP.getNagForPSR() && !isTimerExpired();
    }

    protected boolean needNagForMechanicalJumpFallDamage() {
        return GUIP.getNagForMechanicalJumpFallDamage() && !isTimerExpired();
    }

    protected boolean needNagForCrushingBuildings() {
        return GUIP.getNagForCrushingBuildings() && !isTimerExpired();
    }

    protected boolean needNagForWiGELanding() {
        return GUIP.getNagForWiGELanding() && !isTimerExpired();
    }

    protected boolean needNagForLaunchDoors() {
        return GUIP.getNagForLaunchDoors() && !isTimerExpired();
    }

    protected boolean needNagForSprint() {
        return GUIP.getNagForSprint() && !isTimerExpired();
    }

    protected boolean needNagForOther() {
        return !isTimerExpired();
    }

    protected boolean checkNagForNoAction(String title, String body) {
        ConfirmDialog nag = clientgui.doYesNoBotherDialog(title, body);
        if (nag.getAnswer()) {
            // do they want to be bothered again?
            if (!nag.getShowAgain()) {
                GUIP.setNagForNoAction(false);
            }
        } else {
            return true;
        }

        return false;
    }

    protected boolean checkNagForNoUnJamRAC(String title, String body) {
        ConfirmDialog nag = clientgui.doYesNoBotherDialog(title, body);
        if (nag.getAnswer()) {
            // do they want to be bothered again?
            if (!nag.getShowAgain()) {
                GUIP.setNagForNoUnJamRAC(false);
            }
        } else {
            return true;
        }

        return false;
    }

    protected boolean checkNagForMASC(String title, String body) {
        ConfirmDialog nag = clientgui.doYesNoBotherDialog(title, body);
        if (nag.getAnswer()) {
            // do they want to be bothered again?
            if (!nag.getShowAgain()) {
                GUIP.setNagForMASC(false);
            }
        } else {
            return true;
        }

        return false;
    }

    protected boolean checkNagForSprint(String title, String body) {
        ConfirmDialog nag = clientgui.doYesNoBotherDialog(title, body);
        if (nag.getAnswer()) {
            // do they want to be bothered again?
            if (!nag.getShowAgain()) {
                GUIP.setNagForSprint(false);
            }
        } else {
            return true;
        }

        return false;
    }

    protected boolean checkNagForPSR(String title, String body) {
        ConfirmDialog nag = clientgui.doYesNoBotherDialog(title, body);
        if (nag.getAnswer()) {
            // do they want to be bothered again?
            if (!nag.getShowAgain()) {
                GUIP.setNagForPSR(false);
            }
        } else {
            return true;
        }

        return false;
    }

    protected boolean checkNagForMechanicalJumpFallDamage(String title, String body) {
        ConfirmDialog nag = clientgui.doYesNoBotherDialog(title, body);
        if (nag.getAnswer()) {
            // do they want to be bothered again?
            if (!nag.getShowAgain()) {
                GUIP.setNagForMechanicalJumpFallDamage(false);
            }
        } else {
            return true;
        }

        return false;
    }

    protected boolean checkNagForCrushingBuildings(String title, String body) {
        ConfirmDialog nag = clientgui.doYesNoBotherDialog(title, body);
        if (nag.getAnswer()) {
            // do they want to be bothered again?
            if (!nag.getShowAgain()) {
                GUIP.setNagForCrushingBuildings(false);
            }
        } else {
            return true;
        }

        return false;
    }

    protected boolean checkNagForWiGELanding(String title, String body) {
        ConfirmDialog nag = clientgui.doYesNoBotherDialog(title, body);
        if (nag.getAnswer()) {
            // do they want to be bothered again?
            if (!nag.getShowAgain()) {
                GUIP.setNagForWiGELanding(false);
            }
        } else {
            return true;
        }

        return false;
    }

    protected boolean checkNagForOverheat(String title, String body) {
        ConfirmDialog nag = clientgui.doYesNoBotherDialog(title, body);
        if (nag.getAnswer()) {
            // do they want to be bothered again?
            if (!nag.getShowAgain()) {
                GUIP.setNagForOverheat(false);
            }
        } else {
            return true;
        }

        return false;
    }

    protected boolean checkNagLaunchDoors(String title, String body) {
        ConfirmDialog nag = clientgui.doYesNoBotherDialog(title, body);
        if (nag.getAnswer()) {
            // do they want to be bothered again?
            if (!nag.getShowAgain()) {
                GUIP.setNagForLaunchDoors(false);
            }
        } else {
            return true;
        }

        return false;
    }

    /** set labels and enables on the done and skip buttons depending on the GUIP getNagForNoAction option
     *
     * @param doneButtonLabel
     * @param skipButtonLabel
     * @param isDoingAction true if user has entered actions for this turn, false if not.
     */
    protected void updateDonePanelButtons(final String doneButtonLabel, final String skipButtonLabel, final boolean isDoingAction,
                                          @Nullable List<String> turnDetails) {
        if (isIgnoringEvents()) {
            return;
        }

        this.isDoingAction = isDoingAction;
        if (GUIP.getNagForNoAction()) {
            butDone.setText("<html><b>" + doneButtonLabel + "</b></html>");
            butSkipTurn.setText("<html><b>" + skipButtonLabel + "</b></html>");
        } else {
            // toggle the text on the done button, butIgnoreNag is not used
            butSkipTurn.setVisible(false);
            if (this.isDoingAction) {
                butDone.setText("<html><b>" + doneButtonLabel + "</b></html>");
            } else {
                butDone.setText("<html><b>" + skipButtonLabel + "</b></html>");
            }
        }
        butSkipTurn.setText("<html><b>" + skipButtonLabel + "</b></html>");

        // point blank shots don't have the "isMyTurn()" characteristic
        if (!clientgui.getClient().isMyTurn() && !clientgui.isProcessingPointblankShot()) {
            butDone.setEnabled(false);
            butSkipTurn.setEnabled(false);
        } else if (this.isDoingAction || ignoreNoActionNag) {
            butDone.setEnabled(true);
            butSkipTurn.setEnabled(false);
        } else {
            butDone.setEnabled(!GUIP.getNagForNoAction());
            butSkipTurn.setEnabled(true);
        }

        TurnDetailsOverlay turnDetailsOverlay = clientgui.getBoardView().getTurnDetailsOverlay();
        if (turnDetailsOverlay != null) {
            turnDetailsOverlay.setLines(turnDetails);
        }
    }

    private void adaptToGUIScale() {
        butSkipTurn.setPreferredSize(new Dimension(UIUtil.scaleForGUI(DONE_BUTTON_WIDTH), MIN_BUTTON_SIZE.height * 1));
    }
}
