package com.colisa.notekeeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import java.util.List;

public class ModuleStatusView extends View {

    private static final String TAG = ModuleStatusView.class.getSimpleName();
    public static final int EDIT_MODE_MODULE_COUNT = 7;
    public static final int INVALID_INDEX = -1;
    public static final int SHAPE_CIRCLE = 0;
    public static final float DEFAULT_OUTLINE_WIDTH_DP = 2f;
    private boolean[] mModuleStatus;
    private float mOutlineWidth;
    private float mShapeSize;
    private float mSpacing;
    private Rect[] mModuleRectangles;
    private int mOutlineColor;
    private Paint mPaintOutline;
    private int mFillColor;
    private Paint mPaintFill;
    private float mRadius;
    private int mMaxHorizontalModules;
    private int mModuleIndex;
    private int mShape;
    private ModuleStatusAccessibilityHelper mAccessibilityHelper;

    public ModuleStatusView(Context context) {
        super(context);
        init(null, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * View initialization
     *
     * @param attrs    attributes
     * @param defStyle styles
     */
    private void init(AttributeSet attrs, int defStyle) {

        if (isInEditMode())
            setupEditModeValues();

        setFocusable(true);
        mAccessibilityHelper = new ModuleStatusAccessibilityHelper(this);
        ViewCompat.setAccessibilityDelegate(this, mAccessibilityHelper);

        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        float displayDensity = dm.density;
        float defaultOutlineWidthPixels = displayDensity * DEFAULT_OUTLINE_WIDTH_DP;

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ModuleStatusView, defStyle, 0);

        mOutlineColor = a.getColor(R.styleable.ModuleStatusView_outlineColor, Color.BLACK);
        mShape = a.getInt(R.styleable.ModuleStatusView_shape, SHAPE_CIRCLE);
        mOutlineWidth = a.getDimension(R.styleable.ModuleStatusView_outlineWidth, defaultOutlineWidthPixels);
        a.recycle();


        mShapeSize = 100f;
        mSpacing = 30f;
        mRadius = (mShapeSize - mOutlineWidth) / 2;



        mPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOutline.setStyle(Paint.Style.STROKE);
        mPaintOutline.setStrokeWidth(mOutlineWidth);
        mPaintOutline.setColor(mOutlineColor);

        mFillColor = getContext().getResources().getColor(R.color.application_orange);
        mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(mFillColor);

    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        return mAccessibilityHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mAccessibilityHelper.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        mAccessibilityHelper.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    private void setupEditModeValues() {
        boolean[] exampleModuleValues = new boolean[EDIT_MODE_MODULE_COUNT];
        int middle = EDIT_MODE_MODULE_COUNT / 2;
        for (int i = 0; i < middle; i++)
            exampleModuleValues[i] = true;

        setModuleStatus(exampleModuleValues);
    }

    private void setupModuleRectangles(int width) {
        int availableWidth = width - getPaddingLeft() - getPaddingRight();
        int horizontalModulesThatCanFit = (int) (availableWidth / (mShapeSize + mSpacing));
        int maxHorizontalModules = Math.min(horizontalModulesThatCanFit, mModuleStatus.length);
        mModuleRectangles = new Rect[mModuleStatus.length];
        for (int moduleIndex = 0; moduleIndex < mModuleRectangles.length; moduleIndex++) {
            int column = moduleIndex % maxHorizontalModules;
            int row = moduleIndex / maxHorizontalModules;
            int x = getPaddingLeft() + (int) (column * (mShapeSize + mSpacing));
            int y = getPaddingTop() + (int) (row * (mShapeSize + mSpacing));
            mModuleRectangles[moduleIndex] = new Rect(x, y, x + (int) mShapeSize, y + (int) mShapeSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "Inside onDraw()");
        super.onDraw(canvas);

        for (int moduleIndex = 0; moduleIndex < mModuleRectangles.length; moduleIndex++) {
            if (mShape == SHAPE_CIRCLE){
                float x = mModuleRectangles[moduleIndex].centerX();
                float y = mModuleRectangles[moduleIndex].centerY();
                if (mModuleStatus[moduleIndex]) {
                    canvas.drawCircle(x, y, mRadius, mPaintFill);
                }
                canvas.drawCircle(x, y, mRadius, mPaintOutline);
            } else {
                drawSquare(canvas, moduleIndex);
            }

        }
    }

    private void drawSquare(Canvas canvas, int moduleIndex) {
        Rect moduleRectangle = mModuleRectangles[moduleIndex];

        if(mModuleStatus[moduleIndex])
            canvas.drawRect(moduleRectangle, mPaintFill);

        canvas.drawRect(moduleRectangle.left + (mOutlineWidth/2),
                moduleRectangle.top + (mOutlineWidth/2),
                moduleRectangle.right - (mOutlineWidth/2),
                moduleRectangle.bottom - (mOutlineWidth/2),
                mPaintOutline);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 0;
        int desiredHeight = 0;

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int availableWidth = specWidth - getPaddingLeft() - getPaddingRight();
        int horizontalModulesThatCanFit = (int) (availableWidth / (mShapeSize + mSpacing));
        mMaxHorizontalModules = Math.min(horizontalModulesThatCanFit, mModuleStatus.length);

        desiredWidth = (int) ((mMaxHorizontalModules * (mShapeSize + mSpacing)) - mSpacing);
        desiredWidth += getPaddingLeft() + getPaddingRight();

        int rows = ((mModuleStatus.length - 1) / mMaxHorizontalModules) + 1;
        desiredHeight = (int) ((rows * (mShapeSize + mSpacing)) - mSpacing);
        desiredHeight += getPaddingTop() + getPaddingBottom();

        int width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
        int height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        Log.d(TAG, "Executing onSizeChanged() with -> width: " + w + " height: " + h);
        setupModuleRectangles(w);
    }

    @Override
    public boolean performClick() {
        if (mModuleIndex == INVALID_INDEX) {
            return super.performClick();
        }
        mModuleStatus[mModuleIndex] = ! mModuleStatus[mModuleIndex];
        invalidate();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                mModuleIndex = findItemAtPosition(event.getX(), event.getY());
                performClick();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private int findItemAtPosition(float x, float y) {
        int moduleIndex = INVALID_INDEX;
        for (int i = 0; i < mModuleRectangles.length; i++) {
            if (mModuleRectangles[i].contains((int) x, (int) y)){
                moduleIndex = i;
            }
        }
        return moduleIndex;
    }

    public void setModuleStatus(boolean[] moduleStatus) {
        mModuleStatus = moduleStatus;
    }

    private void onModuleSelected(int moduleIndex) {
        if (moduleIndex == INVALID_INDEX)
            return;
        mModuleStatus[mModuleIndex] = ! mModuleStatus[mModuleIndex];
        invalidate();
        mAccessibilityHelper.invalidateVirtualView(moduleIndex);
        mAccessibilityHelper.sendEventForVirtualView(moduleIndex, AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    private class ModuleStatusAccessibilityHelper extends ExploreByTouchHelper {

        public ModuleStatusAccessibilityHelper(@NonNull View host) {
            super(host);
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            int moduleIndex = findItemAtPosition(x, y);
            return moduleIndex == INVALID_INDEX ? ExploreByTouchHelper.INVALID_ID : moduleIndex;

        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            if (mModuleRectangles == null)
                return;
            for(int moduleIndex = 0; moduleIndex < mModuleRectangles.length; moduleIndex++) {
                virtualViewIds.add(moduleIndex);
            }
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId, @NonNull AccessibilityNodeInfoCompat node) {
            node.setFocusable(true);
            node.setBoundsInParent(mModuleRectangles[virtualViewId]);
            node.setContentDescription("Module " + virtualViewId);
            node.setCheckable(true);
            node.setChecked(mModuleStatus[virtualViewId]);
            node.addAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, @Nullable Bundle arguments) {
            if (action == AccessibilityNodeInfo.ACTION_CLICK) {
                onModuleSelected(virtualViewId);
                return true;
            }
            return false;
        }
    }
}
