package vn.com.vng.swipelistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by NhaN on 1/22/2015.
 */
public class SwipeListView extends ListView {

    public final static int SWIPE_MODE_DEFAULT = -1;

    /**
     * Disable swipe
     */
    public final static int SWIPE_MODE_NONE = 0;

    /**
     * Enables both left and right swipe
     */
    public final static int SWIPE_MODE_BOTH = 1;

    /**
     * Enables right swipe
     */
    public final static int SWIPE_MODE_RIGHT = 2;

    /**
     * Enables left swipe
     */
    public final static int SWIPE_MODE_LEFT = 3;


    /**
     * Reveal the cell when swiped over
     */
    public final static int SWIPE_ACTION_REVEAL = 0;

    /**
     * Dismisses the cell when swiped over
     */
    public final static int SWIPE_ACTION_DISMISS = 1;


    /**
     * To do some function the cell when swiped over
     */
    public final static int SWIPE_ACTION_TODO = 2;

    public final static int SWIPE_ACTION_MENU_FILL_ALL = 3 ;

    public final static int SWIPE_ACTION_MENU_FILL_PART = 4 ;
    /**
     * Do nothing when swiped over
     */
    public final static int SWIPE_ACTION_NONE = 3 ;


    private final static int TOUCH_STATE_REST = 0;

    private final static int TOUCH_STATE_SCROLLING_X = 1 ;

    private final static int TOUCH_STATE_SCROLLING_Y = 2 ;

    private final static String DEFAULT_BACK_VIEW_ID = "front_view_id";
    private final static String DEFAULT_FRONT_VIEW_ID = "back_view_id";
    private final static String TYPE_ID ="id";




    private TypeListView typeListView = TypeListView.EMAIL;

    int swipeBackViewId = 0;
    int swipeFrontViewId = 0 ;
    private int touchState = 0 ;

    private float lastMotionX;
    private float lastMotionY;
    private int touchSlop ;


    SwipeListViewTouchListener touchListener ;
    SwipeListViewListener  swipeListViewListener ;


    public SwipeListView(Context context, int swipeBackViewId, int swipeFrontViewId ){
        super(context);
        this.swipeBackViewId = swipeBackViewId ;
        this.swipeFrontViewId = swipeFrontViewId;
        init(null);
    }

    public SwipeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SwipeListView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attributeSet){
        int swipeMode =  SWIPE_MODE_BOTH;
//        boolean swipeOpenOnLongPress = true;
//        boolean swipeCloseAllItemsWhenMoveList = true;
        long swipeAnimationTime = 0;
        float swipeOffsetLeft = 0;
        float swipeOffsetRight = 0;
        int  swipeAction = SwipeListView.SWIPE_ACTION_REVEAL;


        int swipeActionLeft = SWIPE_ACTION_REVEAL;
        int swipeActionRight = SWIPE_ACTION_REVEAL;

        if (attributeSet != null) {
            TypedArray styled = getContext().obtainStyledAttributes(attributeSet, R.styleable.SwipeListView);
            swipeMode = styled.getInt(R.styleable.SwipeListView_swipeMode, SWIPE_MODE_BOTH);
            swipeOffsetLeft = styled.getDimension(R.styleable.SwipeListView_swipeOffsetLeft, 0.0f);
            swipeOffsetRight = styled.getDimension(R.styleable.SwipeListView_swipeOffsetRight, 0.0f);
            swipeAnimationTime = styled.getInteger(R.styleable.SwipeListView_swipeAnimationTime, 0);
            swipeAction = styled.getInt(R.styleable.SwipeListView_swipeAction, SWIPE_ACTION_REVEAL);
//            swipeOpenOnLongPress = styled.getBoolean(R.styleable.SwipeListView_swipeOpenOnLongPress, true);
            this.swipeFrontViewId = styled.getResourceId(R.styleable.SwipeListView_swipeFrontViewId, 0);
            this.swipeBackViewId = styled.getResourceId(R.styleable.SwipeListView_swipeBackViewId, 0);

        }

        if (swipeBackViewId == 0 && swipeFrontViewId == 0){
            this.swipeBackViewId = getContext().getResources().getIdentifier(DEFAULT_BACK_VIEW_ID,TYPE_ID, getContext().getPackageName());
            this.swipeFrontViewId = getContext().getResources().getIdentifier(DEFAULT_FRONT_VIEW_ID,TYPE_ID,getContext().getPackageName());
            if (this.swipeFrontViewId == 0 || this.swipeBackViewId == 0) {
                throw new RuntimeException(String.format("You forgot the attributes swipeFrontView or swipeBackView. You can add this attributes or use '%s' and '%s' identifiers", new Object[]{"backViewId", "FrontViewId"}));
            }

        }

        this.touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(getContext()));
        this.touchListener = new SwipeListViewTouchListener(this,this.swipeFrontViewId, this.swipeBackViewId);
        if(swipeAnimationTime > 0){
            touchListener.setAnimationTime(swipeAnimationTime);
        }
        // only allow swipe_mode_left when swipe_action_menu
        if(swipeAction == SWIPE_ACTION_MENU_FILL_ALL || swipeAction == SWIPE_ACTION_MENU_FILL_PART){
            swipeMode = SWIPE_MODE_LEFT ;
        }

        touchListener.setSwipeMode(swipeMode);
        touchListener.setLeftOffSet(swipeOffsetLeft);
        touchListener.setRightOffSet(swipeOffsetRight);
        touchListener.setSwipeActionLeft(swipeActionLeft);
        touchListener.setSwipeActionRight(swipeActionRight);
        touchListener.setSwipeAction(swipeAction);
        setOnTouchListener(this.touchListener);
        setOnScrollListener(this.touchListener.makeScrollListener());

    }

    public void resetScrolling() {
        touchState = TOUCH_STATE_REST;
    }


    public int getSwipeActionLeft() {
        return this.touchListener.getSwipeActionLeft();
    }

    public int getSwipeActionRight() {
        return this.touchListener.getSwipeActionRight();
    }


    public TypeListView getTypeListView() {
        return typeListView;
    }

    public void setTypeListView(TypeListView typeListView) {
        this.typeListView = typeListView;
    }

    // callback method handle when click on front view
    protected void onClickFrontView(int position) {
        if (swipeListViewListener != null && position != ListView.INVALID_POSITION) {
            swipeListViewListener.onClickFrontView(position);
        }
    }


    protected void onClickBackView(int position){
        if (swipeListViewListener != null && position != ListView.INVALID_POSITION){
            swipeListViewListener.onClickBackView(position);
        }
    }

    protected  int changeSwipeMode(int position){
        if (swipeListViewListener != null && position != ListView.INVALID_POSITION) {
            return swipeListViewListener.onChangeSwipeMode(position);
        }
        return SWIPE_MODE_DEFAULT;

    }

    protected void onStartClose(int position, boolean right){
        if (swipeListViewListener != null && position != ListView.INVALID_POSITION) {
             swipeListViewListener.onStartClose(position,right);
        }
    }

    protected void onStartOpen(int position, int action, boolean right) {
        if (swipeListViewListener != null && position != ListView.INVALID_POSITION) {
            swipeListViewListener.onStartOpen(position, action, right);
        }
    }

    protected void onMove(int position, float x) {
        if (swipeListViewListener != null && position != ListView.INVALID_POSITION) {
            swipeListViewListener.onMove(position, x);
        }
    }

    protected void onClosed(int position, boolean fromRight) {
        if (swipeListViewListener != null && position != ListView.INVALID_POSITION) {
            swipeListViewListener.onClosed(position, fromRight);
        }
    }

    protected void onOpened(int position, boolean toRight) {
        if (swipeListViewListener != null && position != ListView.INVALID_POSITION) {
            swipeListViewListener.onOpened(position, toRight);
        }
    }

    protected  void onDismiss(int[] positions){
        if (swipeListViewListener != null){
           swipeListViewListener.onDismiss(positions);
        }
    }

    protected void onFirstListItem(){
        if (swipeListViewListener != null){
            swipeListViewListener.onFirstListItem();
        }
    }

    protected void onLastListItem(){
        if (swipeListViewListener != null){
            swipeListViewListener.onLastListItem();
        }
    }

    protected void onPlusItem(int position){
        if (swipeListViewListener != null && position != SwipeListView.INVALID_POSITION) {
            swipeListViewListener.onPlusItem(position);
        }
    }

    protected void onSubItem(int position){
        if (swipeListViewListener != null && position != SwipeListView.INVALID_POSITION) {
            swipeListViewListener.onSubItem(position);
        }
    }

    protected void onEditItem(int position){
        if(swipeListViewListener != null && position != SwipeListView.INVALID_POSITION){
            swipeListViewListener.onEditItem(position);
        }
    }

    protected void onMenuCloseClick(int position){
        if(swipeListViewListener != null && position != SwipeListView.INVALID_POSITION){
            swipeListViewListener.onMenuCloseClick(position);
        }
    }


    protected void onMenuEditClick(int position){
        if(swipeListViewListener != null && position != SwipeListView.INVALID_POSITION){
            swipeListViewListener.onMenuEditClick(position);
        }
    }

    protected void onMenuDeleteClick(int position){
        if(swipeListViewListener != null && position != SwipeListView.INVALID_POSITION){
            swipeListViewListener.onMenuDeleteClick(position);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        int action = MotionEventCompat.getActionMasked(ev);
        final float x = ev.getX();
        final float y = ev.getY();

        if (isEnabled() && touchListener.isSwipeEnabled()) {

            if (touchState == TOUCH_STATE_SCROLLING_X) {
                return touchListener.onTouch(this, ev);
            }

            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    checkInMoving(x, y);
                    return touchState == TOUCH_STATE_SCROLLING_Y;
                case MotionEvent.ACTION_DOWN:
                    super.onInterceptTouchEvent(ev);
                    touchListener.onTouch(this, ev);
                    touchState = TOUCH_STATE_REST;
                    lastMotionX = x;
                    lastMotionY = y;
                    return false;
                case MotionEvent.ACTION_CANCEL:
                    touchState = TOUCH_STATE_REST;
                    break;
                case MotionEvent.ACTION_UP:
                    touchListener.onTouch(this, ev);
                    return touchState == TOUCH_STATE_SCROLLING_Y;
                default:
                    break;
            }
        }

        return super.onInterceptTouchEvent(ev);
    }


    private void checkInMoving(float x, float y) {
        final int xDiff = (int) Math.abs(x - lastMotionX);
        final int yDiff = (int) Math.abs(y - lastMotionY);

        final int touchSlop = this.touchSlop;
        boolean xMoved = xDiff > touchSlop;
        boolean yMoved = yDiff > touchSlop;

        if (xMoved) {
            touchState = TOUCH_STATE_SCROLLING_X;
            lastMotionX = x;
            lastMotionY = y;
        }

        if (yMoved) {
            touchState = TOUCH_STATE_SCROLLING_Y;
            lastMotionX = x;
            lastMotionY = y;
        }
    }



    public void setSwipeAction(int swipeAction){
        this.touchListener.resetOpenedList();
        this.touchListener.setSwipeAction(swipeAction);
    }

    public void setAnimationTime(long j) {
        this.touchListener.setAnimationTime(j);
    }

    public void setOffsetLeft(float f) {
        this.touchListener.setLeftOffSet(f);
    }

    public void setOffsetRight(float f) {
        this.touchListener.setRightOffSet(f);
    }

    public void setSwipeActionLeft(int i) {
        this.touchListener.setSwipeActionLeft(i);
    }

    public void setSwipeActionRight(int i) {
        this.touchListener.setSwipeActionRight(i);
    }

    public void setSwipeListViewListener(SwipeListViewListener swipeListViewListener) {
        this.swipeListViewListener = swipeListViewListener;
    }

    public void setSwipeMode(int i) {
        this.touchListener.setSwipeMode(i);
    }


    public void setAdapter(ListAdapter listAdapter) {
        super.setAdapter(listAdapter);
        this.touchListener.resetItems();
        listAdapter.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                super.onChanged();
//                SwipeListView.this.onListChanged();
                SwipeListView.this.touchListener.resetItems();
            }
        });
    }


}
