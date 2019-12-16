package ezviz.ezopensdkcommon.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;

public class CheckTextButton extends CompoundButton {

    private boolean mToggleEnable = true;

    public CheckTextButton(Context context) {
        this(context, null);
    }

    public CheckTextButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckTextButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    @TargetApi(14)
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(CheckTextButton.class.getName());
    }

    @Override
    @TargetApi(14)
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(CheckTextButton.class.getName());
    }

    public void setToggleEnable(boolean toggleEnable) {
        this.mToggleEnable = toggleEnable;
    }

    @Override
    public void toggle() {
        if (mToggleEnable)
            super.toggle();
    }
}