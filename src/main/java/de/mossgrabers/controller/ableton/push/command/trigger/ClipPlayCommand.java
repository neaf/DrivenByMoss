// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.ableton.push.command.trigger;

import de.mossgrabers.controller.ableton.push.PushConfiguration;
import de.mossgrabers.controller.ableton.push.controller.PushControlSurface;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.data.ISlot;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.utils.ButtonEvent;

import java.util.Optional;


/**
 * When Werbel Live mode is on: plays or stops the selected clip slot; if the slot is recording,
 * stops recording and starts playback. When off: standard transport play/stop behaviour.
 *
 * @author Jürgen Moßgraber
 */
public class ClipPlayCommand extends AbstractTriggerCommand<PushControlSurface, PushConfiguration>
{
    private final ITransport transport;


    /**
     * Constructor.
     *
     * @param model The model
     * @param surface The surface
     */
    public ClipPlayCommand (final IModel model, final PushControlSurface surface)
    {
        super (model, surface);
        this.transport = model.getTransport ();
    }


    /** {@inheritDoc} */
    @Override
    public void execute (final ButtonEvent event, final int velocity)
    {
        if (event != ButtonEvent.DOWN)
            return;

        if (!this.surface.getConfiguration ().isWerbelLiveMode ())
        {
            if (this.transport.isPlaying ())
                this.transport.stop ();
            else
                this.transport.play ();
            return;
        }

        final Optional<ITrack> selectedTrack = this.model.getCurrentTrackBank ().getSelectedItem ();
        if (selectedTrack.isEmpty ())
            return;

        final Optional<ISlot> selectedSlot = selectedTrack.get ().getSlotBank ().getSelectedItem ();
        if (selectedSlot.isEmpty ())
            return;

        selectedSlot.get ().launch (true, false);
    }


    /**
     * Returns the LED state index: 1 when active (playing or recording in Werbel Live mode, or
     * transport playing otherwise), 0 when inactive.
     *
     * @return The LED state index
     */
    public int getLedState ()
    {
        if (!this.surface.getConfiguration ().isWerbelLiveMode ())
            return this.transport.isPlaying () ? 1 : 0;

        final Optional<ITrack> selectedTrack = this.model.getCurrentTrackBank ().getSelectedItem ();
        if (selectedTrack.isEmpty ())
            return 0;

        final Optional<ISlot> selectedSlot = selectedTrack.get ().getSlotBank ().getSelectedItem ();
        if (selectedSlot.isEmpty ())
            return 0;

        final ISlot slot = selectedSlot.get ();
        return slot.isPlaying () || slot.isRecording () ? 1 : 0;
    }
}
