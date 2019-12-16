package ezviz.ezopensdkcommon.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TitleBar extends FrameLayout {

    private Drawable mBackground;
    private Drawable mBackButton;
    private int mTextColor;
    
    private Context mContext;
    private ViewGroup mTitleLayout;
    private TextView mTextView;
    private View mTitleView;
    private LinearLayout mLeftLayout;
    private LinearLayout mRightLayout;
    
    private static final int ID_TITLELAYOUT = 1;
    private static final int ID_TEXTVIEW = 2;
    private static final int ID_LEFTLAYOUT = 3;
    private static final int ID_RIGHTLAYOUT = 4;
    
    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = getContext();
        mTextColor = Color.rgb(51, 51, 51);
        mBackground = Utils.getDrawableFromAssetsFile(mContext, "common_title.9.png");
        
        Drawable normal = Utils.getDrawableFromAssetsFile(mContext, "common_title_back.png");
        Drawable pressed = Utils.getDrawableFromAssetsFile(mContext, "common_title_back_sel.png");
        mBackButton = Utils.newSelector(mContext, normal, pressed, normal, normal);

        init();
    }

    private void init() {   
        mTitleLayout = new RelativeLayout(mContext);
        mTitleLayout.setId(ID_TITLELAYOUT); 
        mTitleLayout.setBackgroundDrawable(mBackground);
        LayoutParams titleLayoutLp = new LayoutParams(LayoutParams.MATCH_PARENT, Utils.dip2px(mContext, 44));
        this.addView(mTitleLayout, titleLayoutLp);
        
        mLeftLayout = new LinearLayout(mContext);
        mLeftLayout.setId(ID_LEFTLAYOUT); 
        RelativeLayout.LayoutParams leftLayoutLp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        leftLayoutLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        leftLayoutLp.addRule(RelativeLayout.CENTER_VERTICAL);
        mLeftLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLeftLayout.setGravity(Gravity.CENTER_VERTICAL);
        mTitleLayout.addView(mLeftLayout, leftLayoutLp);
        
        mTextView = new TextView(mContext);
        mTextView.setId(ID_TEXTVIEW); 
        RelativeLayout.LayoutParams textViewLp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textViewLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mTextView.setEllipsize(TruncateAt.END);
        mTextView.setMaxWidth(Utils.dip2px(mContext, 200));
        mTextView.setSingleLine(true);
        mTextView.setTextColor(mTextColor);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mTitleLayout.addView(mTextView, textViewLp);
        
        mRightLayout = new LinearLayout(mContext);
        mRightLayout.setId(ID_RIGHTLAYOUT); 
        RelativeLayout.LayoutParams rightLayoutLp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rightLayoutLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightLayoutLp.addRule(RelativeLayout.CENTER_VERTICAL);
        mRightLayout.setOrientation(LinearLayout.HORIZONTAL);
        mRightLayout.setGravity(Gravity.CENTER_VERTICAL);
        mTitleLayout.addView(mRightLayout, rightLayoutLp);
    }
    
    public void setStyle(int textColor, Drawable background, Drawable backButton) {
        mTextColor = textColor;
        mBackground = background;
        if(backButton != null)
        	mBackButton = backButton;
        mTextView.setTextColor(mTextColor);
        mTitleLayout.setBackgroundDrawable(mBackground);
    }
    
    /**
     * 设置标题文本
     * 
     * @param resId
     */
    public TextView setTitle(int resId) {
        return setTitle(mContext.getText(resId));
    }

    /**
     * 设置标题文本
     * 
     * @param text
     */
    public TextView setTitle(CharSequence text) {
        mTextView.setText(text);
        mTextView.setVisibility((TextUtils.isEmpty(text) || mTitleView != null) ? View.GONE : View.VISIBLE);
        return mTextView;
    }

    /**
     * 设置标题控件
     * 
     * @param
     */
    public View setTitle(View view) {
        if (mTitleView != null) {
            mTitleLayout.removeView(mTitleView);
        }

        if (view != null) {
            RelativeLayout.LayoutParams layoutParams = null;
            if (view.getLayoutParams() == null) {
                layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
            } else {
                layoutParams = new RelativeLayout.LayoutParams(view.getLayoutParams());
            }
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mTitleLayout.addView(view, layoutParams);
            mTextView.setVisibility(View.GONE);
        } else {
            mTextView.setVisibility(View.VISIBLE);
        }
        mTitleView = view;
        return view;
    }

    /**
     * 设置标题背景颜色
     * 
     * @param
     */
    public void setBackgroundColor(int color) {
        mTitleLayout.setBackgroundColor(color);
    }

    /**
     * 标题文本点击事件
     * 
     * @param l
     */
    public void setOnTitleClickListener(OnClickListener l) {
        mTextView.setOnClickListener(l);
    }

    /**
     * 在标题文本侧添加一个按钮
     * 
     * @param resId
     * @param l
     * @return
     */
    public ImageView addTitleButton(int resId, OnClickListener l) {
        // return addTitleButton(resId, l);
        ImageView button = new ImageView(mContext);
        button.setImageResource(resId);
        button.setOnClickListener(l);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.RIGHT_OF, ID_TEXTVIEW);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        mTitleLayout.addView(button, layoutParams);

        return button;
    }

    /**
     * 在标题文本侧添加一个自定义View
     * 
     * @param v
     * @return
     */
    public void addTitleView(View v) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.RIGHT_OF, ID_TEXTVIEW);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

        mTitleLayout.addView(v, layoutParams);
    }

    /**
     * 在标题栏左侧添加一个按钮
     * 
     * @param resId
     * @param l
     * @return
     */
    public Button addLeftButton(int resId, OnClickListener l) {
        return addLeftButton(getResources().getDrawable(resId), l);
    }

    /**
     * 在标题栏左侧添加一个按钮
     * 
     * @param drawable
     * @param l
     * @return
     */
    public Button addLeftButton(Drawable drawable, OnClickListener l) {
        Button button = new Button(mContext);
        button.setBackgroundDrawable(drawable);
        button.setOnClickListener(l);
        addLeftView(button);
        return button;
    }

    /**
     * 在标题栏左侧添加一个自定义View
     * 
     * @param v
     * @return
     */
    public void addLeftView(View v) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Utils.dip2px(mContext, 60), Utils.dip2px(mContext, 44));
        mLeftLayout.addView(v, 0, layoutParams);
    }

    /**
     * 清除在标题栏左侧控件
     * 
     * @param v
     * @return
     */
    public void removeLeftView(View v) {
        mLeftLayout.removeView(v);
    }

    /**
     * 清除在标题栏左侧控件
     * 
     * @return
     */
    public void removeAllLeftView() {
        mLeftLayout.removeAllViews();
    }

    /**
     * 在标题栏左侧添加一个返回按钮
     * 
     * @param l
     * @return
     */
    public Button addBackButton(OnClickListener l) {
        return addLeftButton(mBackButton, l);
    }

    /**
     * 在标题栏右侧添加一个按钮
     * 
     * @param resId
     * @param l
     * @return
     */
    public Button addRightButton(int resId, OnClickListener l) {
        return addRightButton(getResources().getDrawable(resId), l);
    }

    /**
     * 在标题栏右侧添加一个按钮
     * 
     * @param drawable
     * @param l
     * @return
     */
    public Button addRightButton(Drawable drawable, OnClickListener l) {
        Button button = new Button(mContext);
        button.setBackgroundDrawable(drawable);
        button.setOnClickListener(l);
        addRightView(button);
        return button;
    }

    /**
     * 在标题栏右侧添加一个自定义View
     * 
     * @param v
     * @return
     */
    public void addRightView(View v) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Utils.dip2px(mContext, 60), Utils.dip2px(mContext, 44));
        mRightLayout.addView(v, 0, layoutParams);
    }

    /**
     * 清除在标题栏右侧控件
     * 
     * @param v
     * @return
     */
    public void removeRightView(View v) {
        mRightLayout.removeView(v);
    }

    /**
     * 清除在标题栏右侧控件
     * 
     * @return
     */
    public void removeAllRightView() {
        mRightLayout.removeAllViews();
    }

    /**
     * 在标题栏右侧添加一个进度条
     * 
     * @return
     */
    public ImageView addRightProgress() {        
        ImageView view = new ImageView(mContext);
        view.setImageBitmap(Utils.getImageFromAssetsFile(mContext, "common_title_refresh.png"));
        addRightView(view);      
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.rightMargin = Utils.dip2px(mContext, 13);
        layoutParams.width = Utils.dip2px(mContext, 30);
        layoutParams.height = Utils.dip2px(mContext, 30);
        
        return view;
    }

    /**
     * 在标题栏右侧添加一个按钮
     * 
     * @param text
     * @param l
     * @return
     */
    public Button addRightTextButton(CharSequence text, OnClickListener l) {
        Button button = new Button(mContext);
        Drawable normal = Utils.getDrawableFromAssetsFile(mContext, "tittel_button_bg.9.png");
        Drawable pressed = Utils.getDrawableFromAssetsFile(mContext, "tittel_button_press_bg.9.png");
        button.setBackgroundDrawable(Utils.newSelector(mContext, normal, pressed, normal, normal));
        button.setOnClickListener(l);
        button.setText(text);
        button.setGravity(Gravity.CENTER);
        button.setTextColor(Color.rgb(51, 51, 51));
        button.setPadding(Utils.dip2px(mContext, 5), 0, Utils.dip2px(mContext, 5), 0);
        addRightView(button);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) button.getLayoutParams();
        layoutParams.rightMargin = Utils.dip2px(mContext, 15);
        return button;
    }

    /**
     * 在标题栏右侧添加一个可选择文字
     * 
     * @param text
     * @param checkedText
     * @param l
     * @return
     */
    public CheckTextButton addRightCheckedText(final CharSequence text, final CharSequence checkedText,
            final OnCheckedChangeListener l) {
        CheckTextButton view = new CheckTextButton(mContext);
        int padding = Utils.dip2px(mContext, 5);
        view.setClickable(true);
        view.setPadding(padding, Utils.dip2px(mContext, 9), padding, padding);
        view.setText(text);
        view.setTextColor(Color.rgb(51, 51, 51));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        view.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setText(isChecked ? checkedText : text);
                if (l != null)
                    l.onCheckedChanged(buttonView, isChecked);
            }
        });

        addRightView(view);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.rightMargin = Utils.dip2px(mContext, 15) - padding;
        return view;
    }

    public void setBackButton(int resId) {
        mBackButton = getResources().getDrawable(resId);
    }
}