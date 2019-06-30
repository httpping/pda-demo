package com.olc.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ListView;

import com.olc.reader.R;

/**
 * Elastic ListView for pull-up and pull-down
 */
public class FlexibleListView extends ListView implements OnTouchListener{
    /**Initial pullable Y-axis direction distance*/
    private static final int MAX_Y_OVER_SCROLL_DISTANCE = 100;

    private Context mContext;

    /**Actually pull up and down the distance on the Y axis*/
    private int mMaxYOverScrollDistance;

    private float mStartY = -1;
    /**Whether the first or last item is visible when starting the calculation*/
    private boolean mCalcOnItemVisible = false;
    /**Whether to start calculating*/
    private boolean mStartCalc = false;

    /**User-defined OnTouchListener class*/
    private OnTouchListener mTouchListener;

    /**Pull-up and pull-down listening events*/
    private OnPullListener mPullListener;

    private int mScrollY = 0;
    private int mLastMotionY = 0;
    private int mDeltaY = 0;
    /**Is it animating?*/
    private boolean mIsAnimationRunning = false;
    /**Whether the finger leaves the screen*/
    private boolean mIsActionUp = false;
    /**Whether to support display pulldown refresh loading*/
    private boolean mEnableRefreshHeader = false;
    /**Whether to support loading more loading*/
    private boolean mEnableLoadingMoreHeader = false;

    private View mDefaultRefreshViewHeader;
    private View mDefaultLoadingMore;

    public FlexibleListView(Context context){
        this(context, null);
    }

    public FlexibleListView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public FlexibleListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        super.setOnTouchListener(this);
        initBounceListView();
    }

    private void initBounceListView(){
        final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        final float density = metrics.density;
        mMaxYOverScrollDistance = (int) (density * MAX_Y_OVER_SCROLL_DISTANCE);
    }

    /**
     * Override the method of the parent class, set the OnTouchListener listener object
     * */
    public void setOnTouchListener(OnTouchListener listener) {
        mTouchListener = listener;
    }

    /**
     * Set pull-up and drop-down listeners
     * @param listener Pull-up and drop-down listeners
     * */
    public void setOnPullListener(OnPullListener listener){
        mPullListener = listener;
    }

    /**
     * Set to load more or refresh the loading header
     * @param enableRefreshHeader  Pull down refresh header
     * @param enableLoadingMoreHeader Load more animations
     * */
    public void setHeaderEnable(boolean enableRefreshHeader, boolean enableLoadingMoreHeader) {
        mEnableRefreshHeader = enableRefreshHeader;
        mEnableLoadingMoreHeader = enableLoadingMoreHeader;

        if(mEnableRefreshHeader && mDefaultRefreshViewHeader == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDefaultRefreshViewHeader = inflater.inflate(R.layout.loading_view, null);
        }

        if(enableLoadingMoreHeader && mDefaultLoadingMore == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDefaultLoadingMore = inflater.inflate(R.layout.loading_view, null);
        }
    }

    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);

        mScrollY = y;
    }

    /**
     * The ACTION_DOWN event of onTouch may be lost during the sliding process, and the initial value setting is performed here.
     * */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsActionUp = false;
                resetStatus();
                if(getFirstVisiblePosition() == 0 || (getLastVisiblePosition() == getAdapter().getCount()-1)) {
                    mStartY = event.getY();
                    mStartCalc = true;
                    mCalcOnItemVisible = true;
                }else{
                    mStartCalc = false;
                    mCalcOnItemVisible = false;
                }

                mLastMotionY = (int)event.getY();
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /*If the user-defined touch monitor object consumes an event, the following pull-up and pull-down functions are not performed.*/
        if(mTouchListener!=null && mTouchListener.onTouch(v, event)) {
            return true;
        }

        /*Do not swipe the list while doing animation*/
        if(mIsAnimationRunning) {
            return true;//Need to consume the event, otherwise there will be a situation in which the continuous pull-down or pull-up cannot return to the initial position.
        }

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                mIsActionUp = false;
                resetStatus();
                if(getFirstVisiblePosition() == 0 || (getLastVisiblePosition() == getAdapter().getCount()-1)) {
                    mStartY = event.getY();
                    mStartCalc = true;
                    mCalcOnItemVisible = true;
                }else{
                    mStartCalc = false;
                    mCalcOnItemVisible = false;
                }

                mLastMotionY = (int)event.getY();
            }
            case MotionEvent.ACTION_MOVE:{
                if(!mStartCalc && (getFirstVisiblePosition() == 0|| (getLastVisiblePosition() == getAdapter().getCount()-1))) {
                    mStartCalc = true;
                    mCalcOnItemVisible = false;
                    mStartY = event.getY();
                }

                final int y = (int) event.getY();
                mDeltaY = mLastMotionY - y;
                mLastMotionY = y;

                if(Math.abs(mScrollY) >= mMaxYOverScrollDistance) {
                    if(mDeltaY * mScrollY > 0) {
                        mDeltaY = 0;
                    }
                }

                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:{
                mIsActionUp = true;
                float distance = event.getY() - mStartY;
                checkIfNeedRefresh(distance);

                startBoundAnimate();
            }
        }

        return false;
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
                                  boolean clampedY) {
        if(mDeltaY == 0 || mIsActionUp) {
            return;
        }
        scrollBy(0, mDeltaY/2);
    }

    private void startBoundAnimate() {
        mIsAnimationRunning = true;
        final int scrollY = mScrollY;
        int time = Math.abs(500*scrollY/mMaxYOverScrollDistance);
        ValueAnimator animator = ValueAnimator.ofInt(0,1).setDuration(time);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float fraction = animator.getAnimatedFraction();
                scrollTo(0, scrollY - (int) (scrollY * fraction));

                if((int)fraction == 1) {
                    scrollTo(0, 0);
                    resetStatus();
                }
            }
        });
        animator.start();
    }

    private void resetStatus() {
        mIsAnimationRunning = false;
        mStartCalc = false;
        mCalcOnItemVisible = false;
    }

    /**
     * Determine whether a pullback pull-up or pull-down event is required based on the distance of the slide
     * @param distance
     * */
    private void checkIfNeedRefresh(float distance) {
        int LastVisiblePosition = 0;
        if(getChildCount() > 0) {
            LastVisiblePosition = getAdapter().getCount()-1;
        }
        if(distance > 0 && getFirstVisiblePosition() == 0) { //下拉
            View view = getChildAt(0);
            int viewHeight = 0;
            if(view != null) {
                viewHeight = view.getHeight();
            }

            float realDistance = distance;
            if(!mCalcOnItemVisible) {
                realDistance = realDistance - viewHeight;//The height of the first item is not counted in the content
            }
            if(realDistance > mMaxYOverScrollDistance) {
                if(mPullListener != null){
                    mPullListener.onPullDown();
                }
            }
        } else if(distance < 0 && getLastVisiblePosition() == LastVisiblePosition) {//
            View view = getChildAt(getChildCount()-1);
            if(view == null) {
                return;
            }

            float realDistance = -distance;
            if(!mCalcOnItemVisible) {
                realDistance = realDistance - view.getHeight();//The height of the last item is not counted in the content
            }
            if(realDistance > mMaxYOverScrollDistance) {
                if(mPullListener != null){
                    mPullListener.onPullUp();
                }
            }
        }
    }

    public interface OnPullListener{

        void onPullDown();

        void onPullUp();
    }
}
