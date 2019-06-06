package org.wordpress.aztec.demo;

import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.PrecisionDescriber;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.action.Tapper;
import androidx.test.espresso.util.HumanReadables;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;

import org.hamcrest.Matcher;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static org.hamcrest.Matchers.allOf;

/**
 * Enables clicking on views with 65% or greater displaying.
 */
public final class BetterClickAction implements ViewAction {
    private final CoordinatesProvider coordinatesProvider;
    private final Tapper tapper;
    private final PrecisionDescriber precisionDescriber;
    private final ViewAction rollbackAction;
    public BetterClickAction(Tapper tapper, CoordinatesProvider coordinatesProvider,
                              PrecisionDescriber precisionDescriber) {
        this(tapper, coordinatesProvider, precisionDescriber, null);
    }
    public BetterClickAction(Tapper tapper, CoordinatesProvider coordinatesProvider,
                              PrecisionDescriber precisionDescriber, ViewAction rollbackAction) {
        this.coordinatesProvider = coordinatesProvider;
        this.tapper = tapper;
        this.precisionDescriber = precisionDescriber;
        this.rollbackAction = rollbackAction;
    }
    @Override
    @SuppressWarnings("unchecked")
    public Matcher<View> getConstraints() {
        // Check that at least 65% of the element is displayed (instead of default 90%)
        Matcher<View> standardConstraint = isDisplayingAtLeast(65);
        if (rollbackAction != null) {
            return allOf(standardConstraint, rollbackAction.getConstraints());
        } else {
            return standardConstraint;
        }
    }
    @Override
    public void perform(UiController uiController, View view) {
        float[] coordinates = coordinatesProvider.calculateCoordinates(view);
        float[] precision = precisionDescriber.describePrecision();
        Tapper.Status status = Tapper.Status.FAILURE;
        int loopCount = 0;
        // Native event injection is quite a tricky process. A tap is actually 2
        // seperate motion events which need to get injected into the system. Injection
        // makes an RPC call from our app under test to the Android system server, the
        // system server decides which window layer to deliver the event to, the system
        // server makes an RPC to that window layer, that window layer delivers the event
        // to the correct UI element, activity, or window object. Now we need to repeat
        // that 2x. for a simple down and up. Oh and the down event triggers timers to
        // detect whether or not the event is a long vs. short press. The timers are
        // removed the moment the up event is received (NOTE: the possibility of eventTime
        // being in the future is totally ignored by most motion event processors).
        //
        // Phew.
        //
        // The net result of this is sometimes we'll want to do a regular tap, and for
        // whatever reason the up event (last half) of the tap is delivered after long
        // press timeout (depending on system load) and the long press behaviour is
        // displayed (EG: show a context menu). There is no way to avoid or handle this more
        // gracefully. Also the longpress behavour is app/widget specific. So if you have
        // a seperate long press behaviour from your short press, you can pass in a
        // 'RollBack' ViewAction which when executed will undo the effects of long press.
        while (status != Tapper.Status.SUCCESS && loopCount < 3) {
            try {
                status = tapper.sendTap(uiController, coordinates, precision);
            } catch (RuntimeException re) {
                throw new PerformException.Builder()
                        .withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(re)
                        .build();
            }
            int duration = ViewConfiguration.getPressedStateDuration();
            // ensures that all work enqueued to process the tap has been run.
            if (duration > 0) {
                uiController.loopMainThreadForAtLeast(duration);
            }
            if (status == Tapper.Status.WARNING) {
                if (rollbackAction != null) {
                    rollbackAction.perform(uiController, view);
                } else {
                    break;
                }
            }
            loopCount++;
        }
        if (status == Tapper.Status.FAILURE) {
            throw new PerformException.Builder()
                    .withActionDescription(this.getDescription())
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(new RuntimeException(String.format("Couldn't "
                                    + "click at: %s,%s precision: %s, %s . Tapper: %s coordinate provider: %s precision " +
                                    "describer: %s. Tried %s times. With Rollback? %s", coordinates[0], coordinates[1],
                            precision[0], precision[1], tapper, coordinatesProvider, precisionDescriber, loopCount,
                            rollbackAction != null)))
                    .build();
        }
        if (tapper == Tap.SINGLE && view instanceof WebView) {
            // WebViews will not process click events until double tap
            // timeout. Not the best place for this - but good for now.
            uiController.loopMainThreadForAtLeast(ViewConfiguration.getDoubleTapTimeout());
        }
    }
    @Override
    public String getDescription() {
        return tapper.toString().toLowerCase() + " click";
    }
}
