// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.ableton.push.command.trigger;

import de.mossgrabers.controller.ableton.push.PushConfiguration;
import de.mossgrabers.controller.ableton.push.controller.PushControlSurface;
import de.mossgrabers.framework.command.trigger.Direction;
import de.mossgrabers.framework.command.trigger.mode.CursorCommand;
import de.mossgrabers.framework.daw.IClipLauncherNavigator;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.bank.ISceneBank;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;
import de.mossgrabers.framework.featuregroup.IMode;
import de.mossgrabers.framework.view.Views;


/**
 * Command for cursor arrow keys.
 *
 * @author Jürgen Moßgraber
 */
public class PushCursorCommand extends CursorCommand<PushControlSurface, PushConfiguration>
{
    private final ISceneBank             sceneBank64;
    private final PushConfiguration      configuration;
    private final IClipLauncherNavigator clipLauncherNavigator;


    /**
     * Constructor.
     *
     * @param direction The direction of the pushed cursor arrow
     * @param model The model
     * @param surface The surface
     */
    public PushCursorCommand (final Direction direction, final IModel model, final PushControlSurface surface)
    {
        super (direction, model, surface, false);

        this.configuration = this.surface.getConfiguration ();
        this.sceneBank64 = model.getSceneBank (64);
        this.clipLauncherNavigator = model.getClipLauncherNavigator ();
    }


    /**
     * Scroll scenes up.
     */
    @Override
    protected void scrollUp ()
    {
        this.model.getHost ().println ("cursor UP: view=" + this.surface.getViewManager ().getActiveID () + " isNoteView=" + this.isNoteView () + " playMode=" + this.configuration.isWerbelLiveMode () + " | track=" + this.model.getCursorTrack ().getName () + " slot=" + this.model.getCursorTrack ().getSlotBank ().getSelectedItem ().map (s -> s.getIndex () + ":" + s.getName ()).orElse ("none") + " canBack=" + this.model.getCursorTrack ().getSlotBank ().canScrollBackwards () + " canFwd=" + this.model.getCursorTrack ().getSlotBank ().canScrollForwards ());
        if (this.isNoteView () && this.configuration.isWerbelLiveMode ())
        {
            final java.util.Optional<de.mossgrabers.framework.daw.data.ISlot> selUp = this.model.getCursorTrack ().getSlotBank ().getSelectedItem ();
            final int upIdx = selUp.isPresent () ? selUp.get ().getIndex () : -1;
            this.model.getHost ().println ("  -> prev slot: current=" + upIdx);
            if (upIdx > 0)
                this.model.getCursorTrack ().getSlotBank ().getItem (upIdx - 1).select ();
            else if (this.model.getCursorTrack ().getSlotBank ().canScrollPageBackwards ())
            {
                this.model.getCursorTrack ().getSlotBank ().scrollBackwards ();
                this.model.getHost ().scheduleTask (() -> this.model.getCursorTrack ().getSlotBank ().getItem (this.model.getCursorTrack ().getSlotBank ().getPageSize () - 1).select (), 75);
            }
            return;
        }

        final ISceneBank sceneBank = this.getSceneBank ();
        switch (this.surface.isShiftPressed () || this.isNoteView () ? this.configuration.getCursorKeysSceneShiftedOption () : this.configuration.getCursorKeysSceneOption ())
        {
            case PushConfiguration.CURSOR_KEYS_SCENE_OPTION_MOVE_BANK_BY_PAGE:
                sceneBank.selectPreviousPage ();
                break;
            case PushConfiguration.CURSOR_KEYS_TRACK_OPTION_MOVE_BANK_BY_1:
                sceneBank.scrollBackwards ();
                break;
            default:
                // Ignore
                break;
        }
    }


    /**
     * Scroll scenes down.
     */
    @Override
    protected void scrollDown ()
    {
        this.model.getHost ().println ("cursor DOWN: view=" + this.surface.getViewManager ().getActiveID () + " isNoteView=" + this.isNoteView () + " playMode=" + this.configuration.isWerbelLiveMode () + " | track=" + this.model.getCursorTrack ().getName () + " slot=" + this.model.getCursorTrack ().getSlotBank ().getSelectedItem ().map (s -> s.getIndex () + ":" + s.getName ()).orElse ("none") + " canBack=" + this.model.getCursorTrack ().getSlotBank ().canScrollBackwards () + " canFwd=" + this.model.getCursorTrack ().getSlotBank ().canScrollForwards ());
        if (this.isNoteView () && this.configuration.isWerbelLiveMode ())
        {
            final java.util.Optional<de.mossgrabers.framework.daw.data.ISlot> selDown = this.model.getCursorTrack ().getSlotBank ().getSelectedItem ();
            final int downIdx = selDown.isPresent () ? selDown.get ().getIndex () : -1;
            this.model.getHost ().println ("  -> next slot: current=" + downIdx);
            if (downIdx >= 0 && downIdx < this.model.getCursorTrack ().getSlotBank ().getPageSize () - 1)
                this.model.getCursorTrack ().getSlotBank ().getItem (downIdx + 1).select ();
            else if (this.model.getCursorTrack ().getSlotBank ().canScrollPageForwards ())
            {
                this.model.getCursorTrack ().getSlotBank ().scrollForwards ();
                this.model.getHost ().scheduleTask (() -> this.model.getCursorTrack ().getSlotBank ().getItem (0).select (), 75);
            }
            return;
        }

        final ISceneBank sceneBank = this.getSceneBank ();
        switch (this.surface.isShiftPressed () || this.isNoteView () ? this.configuration.getCursorKeysSceneShiftedOption () : this.configuration.getCursorKeysSceneOption ())
        {
            case PushConfiguration.CURSOR_KEYS_SCENE_OPTION_MOVE_BANK_BY_PAGE:
                sceneBank.selectNextPage ();
                break;
            case PushConfiguration.CURSOR_KEYS_TRACK_OPTION_MOVE_BANK_BY_1:
                sceneBank.scrollForwards ();
                break;
            default:
                // Ignore
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void scrollLeft ()
    {
        this.model.getHost ().println ("cursor LEFT: view=" + this.surface.getViewManager ().getActiveID () + " isNoteView=" + this.isNoteView () + " playMode=" + this.configuration.isWerbelLiveMode () + " | track=" + this.model.getCursorTrack ().getName () + " trackIdx=" + this.model.getCursorTrack ().getIndex () + " canBack=" + this.model.getCurrentTrackBank ().canScrollBackwards () + " canFwd=" + this.model.getCurrentTrackBank ().canScrollForwards ());
        if (this.isNoteView () && this.configuration.isWerbelLiveMode ())
        {
            final de.mossgrabers.framework.daw.data.bank.ITrackBank tb = this.model.getCurrentTrackBank ();
            final java.util.Optional<de.mossgrabers.framework.daw.data.ITrack> selLeft = tb.getSelectedItem ();
            final int leftIdx = selLeft.isPresent () ? selLeft.get ().getIndex () : -1;
            this.model.getHost ().println ("  -> prev track: current=" + leftIdx);
            final int currentSlotLeft = this.model.getCursorTrack ().getSlotBank ().getSelectedItem ().map (s -> s.getIndex ()).orElse (0);
            if (leftIdx > 0)
            {
                tb.getItem (leftIdx - 1).select ();
                this.model.getHost ().scheduleTask (() -> this.model.getCursorTrack ().getSlotBank ().getItem (currentSlotLeft).select (), 75);
            }
            else if (tb.canScrollPageBackwards ())
            {
                tb.scrollBackwards ();
                this.model.getHost ().scheduleTask (() -> { tb.getItem (tb.getPageSize () - 1).select (); this.model.getCursorTrack ().getSlotBank ().getItem (currentSlotLeft).select (); }, 75);
            }
            return;
        }

        final IMode activeMode = this.surface.getModeManager ().getActive ();
        if (activeMode != null)
            activeMode.selectPreviousItemPage ();
    }


    /** {@inheritDoc} */
    @Override
    protected void scrollRight ()
    {
        this.model.getHost ().println ("cursor RIGHT: view=" + this.surface.getViewManager ().getActiveID () + " isNoteView=" + this.isNoteView () + " playMode=" + this.configuration.isWerbelLiveMode () + " | track=" + this.model.getCursorTrack ().getName () + " trackIdx=" + this.model.getCursorTrack ().getIndex () + " canBack=" + this.model.getCurrentTrackBank ().canScrollBackwards () + " canFwd=" + this.model.getCurrentTrackBank ().canScrollForwards ());
        if (this.isNoteView () && this.configuration.isWerbelLiveMode ())
        {
            final de.mossgrabers.framework.daw.data.bank.ITrackBank tb2 = this.model.getCurrentTrackBank ();
            final java.util.Optional<de.mossgrabers.framework.daw.data.ITrack> selRight = tb2.getSelectedItem ();
            final int rightIdx = selRight.isPresent () ? selRight.get ().getIndex () : -1;
            this.model.getHost ().println ("  -> next track: current=" + rightIdx);
            final int currentSlotRight = this.model.getCursorTrack ().getSlotBank ().getSelectedItem ().map (s -> s.getIndex ()).orElse (0);
            if (rightIdx >= 0 && rightIdx < tb2.getPageSize () - 1)
            {
                tb2.getItem (rightIdx + 1).select ();
                this.model.getHost ().scheduleTask (() -> this.model.getCursorTrack ().getSlotBank ().getItem (currentSlotRight).select (), 75);
            }
            else if (tb2.canScrollPageForwards ())
            {
                tb2.scrollForwards ();
                this.model.getHost ().scheduleTask (() -> { tb2.getItem (0).select (); this.model.getCursorTrack ().getSlotBank ().getItem (currentSlotRight).select (); }, 75);
            }
            return;
        }

        final IMode activeMode = this.surface.getModeManager ().getActive ();
        if (activeMode != null)
            activeMode.selectNextItemPage ();
    }


    /** {@inheritDoc} */
    @Override
    protected ISceneBank getSceneBank ()
    {
        if (this.isNoteView ())
            return this.sceneBank64;
        return this.model.getCurrentTrackBank ().getSceneBank ();
    }


    /** {@inheritDoc} */
    @Override
    protected void updateArrowStates ()
    {
        if (this.isNoteView () && this.configuration.isWerbelLiveMode ())
        {
            this.scrollStates.setCanScrollUp (this.model.getCursorTrack ().getSlotBank ().canScrollBackwards ());
            this.scrollStates.setCanScrollDown (this.model.getCursorTrack ().getSlotBank ().canScrollForwards ());
            final ITrackBank trackBank = this.model.getCurrentTrackBank ();
            this.scrollStates.setCanScrollLeft (trackBank.canScrollBackwards ());
            this.scrollStates.setCanScrollRight (trackBank.canScrollForwards ());
            return;
        }

        final ISceneBank sceneBank = this.getSceneBank ();
        final IMode mode = this.surface.getModeManager ().getActive ();
        final boolean shiftPressed = this.surface.isShiftPressed ();

        switch (shiftPressed || this.isNoteView () ? this.configuration.getCursorKeysSceneShiftedOption () : this.configuration.getCursorKeysSceneOption ())
        {
            case PushConfiguration.CURSOR_KEYS_SCENE_OPTION_MOVE_BANK_BY_PAGE:
                this.scrollStates.setCanScrollUp (sceneBank.canScrollPageBackwards ());
                this.scrollStates.setCanScrollDown (sceneBank.canScrollPageForwards ());
                break;
            case PushConfiguration.CURSOR_KEYS_TRACK_OPTION_MOVE_BANK_BY_1:
                this.scrollStates.setCanScrollUp (sceneBank.canScrollBackwards ());
                this.scrollStates.setCanScrollDown (sceneBank.canScrollForwards ());
                break;
            default:
                // Ignore
                break;
        }

        this.scrollStates.setCanScrollLeft (mode != null && (shiftPressed ? mode.hasPreviousItem () : mode.hasPreviousItemPage ()));
        this.scrollStates.setCanScrollRight (mode != null && (shiftPressed ? mode.hasNextItem () : mode.hasNextItemPage ()));
    }


    private boolean isNoteView ()
    {
        return Views.isNoteView (this.surface.getViewManager ().getActiveID ());
    }
}
