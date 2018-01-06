package net.capellari.showme;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by julien on 02/01/18.
 *
 * Rayon préference
 */

public class RayonPreference extends Preference {
    // Attributs
    private int m_seekBarValue;
    private boolean m_trackingTouch;
    private SeekBar m_seekBar;
    private TextView m_seekBarValueTextView;

    private int m_min = 0;
    private int m_max = 100000;
    private int m_factor = 1;
    private int m_seekBarIncrement = 0;
    private boolean m_adjustable = true; // whether the seekbar should respond to the left/right keys
    private boolean m_showSeekBarValue = true; // whether to show the seekbar value TextView next to the bar

    private static final String TAG = "RayonPreference";

    /**
     * Listener reacting to the SeekBar changing value by the user
     */
    private SeekBar.OnSeekBarChangeListener m_seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && !m_trackingTouch) {
                syncValueInternal(seekBar);
            }

            if (fromUser) {
                syncTextValue(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            m_trackingTouch = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            m_trackingTouch = false;
            if (seekBar.getProgress() + m_min != m_seekBarValue) {
                syncValueInternal(seekBar);
            }
        }
    };

    /**
     * Listener reacting to the user pressing DPAD left/right keys if {@code
     * adjustable} attribute is set to true; it transfers the key presses to the SeekBar
     * to be handled accordingly.
     */
    private View.OnKeyListener mSeekBarKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (!m_adjustable && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                // Right or left keys are pressed when in non-adjustable mode; Skip the keys.
                return false;
            }

            // We don't want to propagate the click keys down to the seekbar view since it will
            // create the ripple effect for the thumb.
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                return false;
            }

            if (m_seekBar == null) {
                Log.e(TAG, "SeekBar view is null and hence cannot be adjusted.");
                return false;
            }
            return m_seekBar.onKeyDown(keyCode, event);
        }
    };

    public RayonPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // Traitement des attributs
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RayonFragment, defStyleAttr, defStyleRes);

        m_factor = a.getInt(R.styleable.RayonFragment_rayon_fact, m_factor);
        m_max = a.getInt(R.styleable.RayonFragment_rayon_max, m_max * m_factor) / m_factor;
        m_min = a.getInt(R.styleable.RayonFragment_rayon_min, m_min * m_factor) / m_factor;
        m_seekBarValue = a.getInt(R.styleable.RayonFragment_rayon, m_min * m_factor) / m_factor;

        if (m_seekBarValue < m_min) m_seekBarValue = m_min;
        if (m_seekBarValue > m_max) m_seekBarValue = m_max;

        a.recycle();
    }
    public RayonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public RayonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarPreferenceStyle);
    }

    @SuppressWarnings("unused")
    public RayonPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);

        view.itemView.setOnKeyListener(mSeekBarKeyListener);

        m_seekBar = (SeekBar) view.findViewById(android.support.v7.preference.R.id.seekbar);
        m_seekBarValueTextView = (TextView) view.findViewById(android.support.v7.preference.R.id.seekbar_value);

        if (m_showSeekBarValue) {
            m_seekBarValueTextView.setVisibility(View.VISIBLE);
        } else {
            m_seekBarValueTextView.setVisibility(View.GONE);
            m_seekBarValueTextView = null;
        }

        if (m_seekBar == null) {
            Log.e(TAG, "SeekBar view is null in onBindViewHolder.");
            return;
        }

        m_seekBar.setOnSeekBarChangeListener(m_seekBarChangeListener);
        m_seekBar.setMax(m_max - m_min);

        // If the increment is not zero, use that. Otherwise, use the default mKeyProgressIncrement
        // in AbsSeekBar when it's zero. This default increment value is set by AbsSeekBar
        // after calling setMax. That's why it's important to call setKeyProgressIncrement after
        // calling setMax() since setMax() can change the increment value.
        if (m_seekBarIncrement != 0) {
            m_seekBar.setKeyProgressIncrement(m_seekBarIncrement);
        } else {
            m_seekBarIncrement = m_seekBar.getKeyProgressIncrement();
        }

        m_seekBar.setProgress(m_seekBarValue - m_min);
        syncTextValue();

        m_seekBar.setEnabled(isEnabled());
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(m_seekBarValue)
                : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @SuppressWarnings("unused")
    public void setMin(int min) {
        min /= m_factor;

        if (min > m_max) {
            min = m_max;
        }
        if (min != m_min) {
            m_min = min;
            notifyChanged();
        }
    }

    @SuppressWarnings("unused")
    public int getMin() {
        return m_min;
    }

    @SuppressWarnings("unused")
    public final void setMax(int max) {
        max /= m_factor;

        if (max < m_min) {
            max = m_min;
        }
        if (max != m_max) {
            m_max = max;
            notifyChanged();
        }
    }

    /**
     * Returns the amount of increment change via each arrow key click. This value is derived from
     * user's specified increment value if it's not zero. Otherwise, the default value is picked
     * from the default mKeyProgressIncrement value in {@link android.widget.AbsSeekBar}.
     * @return The amount of increment on the SeekBar performed after each user's arrow key press.
     */
    @SuppressWarnings("unused")
    public final int getSeekBarIncrement() {
        return m_seekBarIncrement;
    }

    /**
     * Sets the increment amount on the SeekBar for each arrow key press.
     * @param seekBarIncrement The amount to increment or decrement when the user presses an
     *                         arrow key.
     */
    @SuppressWarnings("unused")
    public final void setSeekBarIncrement(int seekBarIncrement) {
        if (seekBarIncrement != m_seekBarIncrement) {
            m_seekBarIncrement =  Math.min(m_max - m_min, Math.abs(seekBarIncrement));
            notifyChanged();
        }
    }

    @SuppressWarnings("unused")
    public int getMax() {
        return m_max;
    }

    @SuppressWarnings("unused")
    public int getFactor() {
        return m_factor;
    }

    @SuppressWarnings("unused")
    public void setFactor(int factor) {
        if (factor == m_factor) return;

        m_min *= m_factor;
        m_min /= factor;

        m_max *= m_factor;
        m_max /= factor;

        m_seekBarIncrement *= m_factor;
        m_seekBarIncrement /= factor;

        m_seekBarValue *= m_factor;
        m_seekBarValue /= factor;

        m_factor = factor;
        notifyChanged();
    }

    @SuppressWarnings("unused")
    public void setAdjustable(boolean adjustable) {
        m_adjustable = adjustable;
    }

    @SuppressWarnings("unused")
    public boolean isAdjustable() {
        return m_adjustable;
    }

    public void setValue(int seekBarValue) {
        setValueInternal(seekBarValue / m_factor, true);
    }

    private void setValueInternal(int seekBarValue, boolean notifyChanged) {
        if (seekBarValue < m_min) {
            seekBarValue = m_min;
        }

        if (seekBarValue > m_max) {
            seekBarValue = m_max;
        }

        if (seekBarValue != m_seekBarValue) {
            m_seekBarValue = seekBarValue;
            syncTextValue();

            persistInt(seekBarValue * m_factor);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    @SuppressWarnings("unused")
    public int getValue() {
        return m_seekBarValue;
    }

    public void syncTextValue() {
        syncTextValue(m_seekBarValue - m_min);
    }

    private void syncTextValue(int val) {
        val += m_min;
        val *= m_factor;

        if (m_seekBarValueTextView != null) {
            m_seekBarValueTextView.setText(String.format(Locale.getDefault(), "%d m", val));
        }
    }

    /**
     * Persist the seekBar's seekbar value if callChangeListener
     * returns true, otherwise set the seekBar's value to the stored value
     */
    private void syncValueInternal(SeekBar seekBar) {
        int seekBarValue = m_min + seekBar.getProgress();
        if (seekBarValue != m_seekBarValue) {
            if (callChangeListener(seekBarValue)) {
                setValueInternal(seekBarValue, false);
            } else {
                seekBar.setProgress(m_seekBarValue - m_min);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.seekBarValue = m_seekBarValue;
        myState.min = m_min;
        myState.max = m_max;
        myState.factor = m_factor;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        m_seekBarValue = myState.seekBarValue;
        m_min = myState.min;
        m_max = myState.max;
        m_factor = myState.factor;
        notifyChanged();
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        int seekBarValue;
        int min;
        int max;
        int factor;

        public SavedState(Parcel source) {
            super(source);

            // Restore the click counter
            seekBarValue = source.readInt();
            min = source.readInt();
            max = source.readInt();
            factor = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            // Save the click counter
            dest.writeInt(seekBarValue);
            dest.writeInt(min);
            dest.writeInt(max);
            dest.writeInt(factor);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
