package vn.com.vng.swipelistview;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by NhaN on 1/22/2015.
 */
public class SwipeListViewTouchListener implements View.OnTouchListener {

    private static final int DISPLACE_CHOICE = 80;
    private static final float BORDER_ONE = 0.3f;
    private static final float BORDER_TWO = 0.8f;
    private static final float STANDARD_LIMIT = 0.2f ;
    private int swipeBackViewId;
    private int swipeFrontViewId;

    private float rightOffSet;
    private float leftOffSet;

    // View
    private View parentView;
    private View frontView;
    private View backView;

    // animation variable
    private int slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;
    private long configShortAnimationTime;
    private long animationTime;

    private int swipeMode;
    private int swipeAction;
    private int swipeActionLeft;
    private int swipeActionRight;
    private int swipeCurrentAction;
    private int viewWidth = 1;

    private int blankViewWidth = 0 ;

    private int dismissAnimationRefCount;
    private int downPosition;

    private List<Boolean> opened = new ArrayList<Boolean>();
    private List<Boolean> openedRight = new ArrayList<Boolean>();

    private ArrayList<Integer> pendingDismissPositions = new ArrayList<Integer>();
    private List<PendingDismissData> pendingDismisses = new ArrayList<PendingDismissData>();
    private Rect rect = new Rect();
    boolean listViewMoving;
    SwipeListView swipeListView;
    private VelocityTracker velocityTracker;

    private float downX;
    private boolean swiping;
    private boolean swipingRight;

    private boolean paused;
    private boolean dismissUponReveal = false;

    // boolean variable for set background action_todo
    private boolean isSetBGOneLeft = false;
    private boolean isSetBGTwoLeft = false;
    private boolean isSetBGOneRight = false;
    private boolean isSetBGTwoRight = false;

    private int lastDismissPosition = -1;
    // Reveal Action
    private Button btnUndoDismiss ;
    // Menu Action
    private ImageButton closeMenu ;
    private ImageButton editMenu ;
    private ImageButton deleteMenu ;
    // To_do action
    private ImageView imgTodo ;
    private int imgTodoDistance = 0;
    private boolean skipMove = false ;


    // constructor
    public SwipeListViewTouchListener(SwipeListView swipeListView, int swipeFrontViewId, int swipeBackViewId) {
        this.swipeBackViewId = swipeBackViewId;
        this.swipeFrontViewId = swipeFrontViewId;
        ViewConfiguration vc = ViewConfiguration.get(swipeListView.getContext());
        slop = vc.getScaledTouchSlop();
        minFlingVelocity = vc.getScaledMaximumFlingVelocity();
        maxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        configShortAnimationTime = swipeListView.getContext().getResources().getInteger(R.integer.config_shortAnimTime);
        animationTime = configShortAnimationTime;
        this.swipeListView = swipeListView;
    }

    // set parent view
    private void setParentView(View parentView) {
        this.parentView = parentView;
    }

    //set front view
    private void setFrontView(View frontView, final int childPosition) {
        this.frontView = frontView;
        frontView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pendingDismissPositions.size() > 0 ){
                    closeAllRevealView();
                } else {
                    swipeListView.onClickFrontView(childPosition);
                }
            }
        });

    }

    // set back view
    private void setBackView(final View backView) {
        this.backView = backView;

        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swipeAction == SwipeListView.SWIPE_ACTION_MENU_FILL_PART) {
                    closeAnimate(downPosition);
                }
                swipeListView.onClickBackView(downPosition);
            }
        });

        if (swipeAction == SwipeListView.SWIPE_ACTION_TODO){
            imgTodo = (ImageView) backView.findViewById(R.id.img_todo_id);
            int test = imgTodo.getWidth();
            imgTodoDistance = imgTodo != null ? imgTodo.getWidth()/2 + viewWidth/2 + 10 : 0 ;
        }

        if (swipeAction == SwipeListView.SWIPE_ACTION_REVEAL) {
            btnUndoDismiss = (Button) this.backView.findViewById(R.id.undo_dismiss);
            btnUndoDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAnimate(downPosition);
                }
            });
        }

        if (swipeAction == SwipeListView.SWIPE_ACTION_MENU_FILL_ALL || swipeAction == SwipeListView.SWIPE_ACTION_MENU_FILL_PART){
            TypeListView type = swipeListView.getTypeListView();
            if (swipeAction == SwipeListView.SWIPE_ACTION_MENU_FILL_PART){
                blankViewWidth = backView.findViewById(R.id.blank_view).getWidth();
            }
            switch(type){
                case EMAIL:
                    editMenu = (ImageButton) this.backView.findViewById(ItemMenuId.EDIT.getValue());
                    editMenu.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            swipeListView.onMenuEditClick(downPosition);
                        }
                    });

                    deleteMenu = (ImageButton) this.backView.findViewById(ItemMenuId.DELETE.getValue());
                    deleteMenu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            swipeListView.onMenuDeleteClick(downPosition);
                            deleteAnimate(downPosition);
                        }
                    });

                    closeMenu = (ImageButton) this.backView.findViewById(ItemMenuId.CLOSE.getValue());
                    closeMenu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            closeAnimate(downPosition);
                            swipeListView.onClosed(downPosition,false);
                        }
                    });

                    break;
                case SMS :
                    deleteMenu = (ImageButton) this.backView.findViewById(ItemMenuId.DELETE.getValue());
                    deleteMenu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            swipeListView.onMenuDeleteClick(downPosition);
                            deleteAnimate(downPosition);
                        }
                    });
                    break;
            }
        }

    }

    // return true if list in motion
    public boolean isListMoving() {
        return listViewMoving;
    }

    public void setEnabled(boolean enabled) {
        paused = !enabled;
    }

    protected boolean isSwipeEnabled() {
        return swipeMode != SwipeListView.SWIPE_MODE_NONE;
    }

    public void setSwipeAction(int swipeAction) {
        this.swipeAction = swipeAction;
    }

    // set time for animationTime
    public void setAnimationTime(long animationTime) {
        if (animationTime > 0) {
            this.animationTime = animationTime;
        } else {
            this.animationTime = configShortAnimationTime;
        }
    }

    public void setRightOffSet(float rightOffSet) {
        this.rightOffSet = rightOffSet;
    }

    public void setLeftOffSet(float leftOffSet) {
        this.leftOffSet = leftOffSet;
    }

    public void setSwipeMode(int swipeMode) {
        this.swipeMode = swipeMode;
    }

    public boolean isSwipeEnable() {
        return this.swipeMode != SwipeListView.SWIPE_MODE_NONE;
    }

    public int getSwipeActionLeft() {
        return swipeActionLeft;
    }

    public void setSwipeActionLeft(int swipeActionLeft) {
        this.swipeActionLeft = swipeActionLeft;
    }

    public int getSwipeActionRight() {
        return swipeActionRight;
    }

    public void setSwipeActionRight(int swipeActionRight) {
        this.swipeActionRight = swipeActionRight;
    }

    public void resetOpenedList(){
        if (opened != null ){
            for (int i = 0 ; i < opened.size(); i++){
                opened.set(i,false);
            }
        }
    }

    // add item when adapter is modified
    public void resetItems() {
        if (swipeListView.getAdapter() != null) {
            int count = swipeListView.getAdapter().getCount();
            for (int i = opened.size(); i <= count; i++) {
                opened.add(false);
                openedRight.add(false);
            }
        }
    }

    protected void deleteAnimate(int downPosition){
        if (swipeListView != null){
            View parentView = swipeListView.getChildAt(downPosition - swipeListView.getFirstVisiblePosition());
            performDismiss(parentView,downPosition,false);
        }
    }


    protected void openAnimate(int position) {
        if (swipeListView != null) {
            View child = swipeListView.getChildAt(position - swipeListView.getFirstVisiblePosition()).findViewById(swipeFrontViewId);
            if (child != null) {
                openAnimate(child, position);
            }
        }
    }

    protected void closeAnimate(int position) {
        if (swipeListView != null) {
            View child = swipeListView.getChildAt(position - swipeListView.getFirstVisiblePosition()).findViewById(swipeFrontViewId);
            if (child != null) {
                swipeListView.onStartClose(position,openedRight.get(position));
                closeAnimate(child, position);
            }
        }
    }

    // make onScrollListener
    public AbsListView.OnScrollListener makeScrollListener() {

        return new AbsListView.OnScrollListener() {
            private boolean isFirstItem = false;
            private boolean isLastItem = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                if(swipeAction == SwipeListView.SWIPE_ACTION_MENU_FILL_ALL || swipeAction == SwipeListView.SWIPE_ACTION_MENU_FILL_PART){
                    closeAllRevealView();
                }else {
                    dismissRevealedView();
                }
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    listViewMoving = true;
                    setEnabled(false);
                }
                if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_FLING && scrollState != SCROLL_STATE_TOUCH_SCROLL) {
                    listViewMoving = false;
                    downPosition = ListView.INVALID_POSITION;
                    swipeListView.resetScrolling();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            setEnabled(true);
                        }
                    }, 500);
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (isFirstItem) {
                    boolean onSecondItemList = firstVisibleItem == 1;
                    if (onSecondItemList) {
                        isFirstItem = false;
                    }
                } else {
                    boolean onFirstItemList = firstVisibleItem == 0;
                    if (onFirstItemList) {
                        isFirstItem = true;
                        swipeListView.onFirstListItem();
                    }
                }
                if (isLastItem) {
                    boolean onBeforeLastItemList = firstVisibleItem + visibleItemCount == totalItemCount - 1;
                    if (onBeforeLastItemList) {
                        isLastItem = false;
                    }
                } else {
                    boolean onLastItemList = firstVisibleItem + visibleItemCount >= totalItemCount;
                    if (onLastItemList) {
                        isLastItem = true;
                        swipeListView.onLastListItem();
                    }
                }
            }
        };
    }

    protected int dismiss(int position) {
        opened.remove(position);
        int start = swipeListView.getFirstVisiblePosition();
        int end = swipeListView.getLastVisiblePosition();
        View view = swipeListView.getChildAt(position - start);
        dismissAnimationRefCount++;
        if (position > start && position < end) {
            // TODO here
            return view.getHeight();
        } else {
            pendingDismisses.add(new PendingDismissData(position, null));
            return 0;
        }
    }

    private void closeAnimate(View view, final int position) {
        int moveTo = 0;

        ViewPropertyAnimator.animate(view)
                .translationX(moveTo)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeListView.resetScrolling();
                        swipeListView.onClosed(position,openedRight.get(position));
                        opened.set(position, false);
                        openedRight.set(position, false);
                        pendingDismissPositions.remove(Integer.valueOf(position));
                        resetCell();
                    }
                });
    }

    private void openAnimate(View view, int postion) {
        // TODO here
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (!isSwipeEnable()) {
            return false;
        }

        if (viewWidth < 2) {
            viewWidth = swipeListView.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (this.paused && downPosition != SwipeListView.INVALID_POSITION) {
                    return false;
                }

                this.swipeCurrentAction = SwipeListView.SWIPE_ACTION_NONE;
                int childCount = swipeListView.getChildCount();
                int[] listViewCoords = new int[2];
                swipeListView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];

                View child;

                for (int i = 0; i < childCount; i++) {
                    child = swipeListView.getChildAt(i);
                    child.getHitRect(rect);
                    int childPosition = swipeListView.getPositionForView(child);
                    // only allow swipe when list view enable
                    boolean allowSwipe = swipeListView.getAdapter().isEnabled(childPosition) && swipeListView.getAdapter().getItemViewType(childPosition) >= 0;
                    if (allowSwipe && rect.contains(x, y)) {
                        setParentView(child);
                        setFrontView(child.findViewById(swipeFrontViewId), childPosition);
                        this.downX = motionEvent.getX();

                        this.downPosition = childPosition;

                        frontView.setClickable(!opened.get(downPosition));
                        frontView.setLongClickable(!opened.get(downPosition));

                        velocityTracker = VelocityTracker.obtain();
                        velocityTracker.addMovement(motionEvent);

                        if (swipeBackViewId > 0 ) {
                            setBackView(child.findViewById(swipeBackViewId));
                        }
                        break;
                    }
                }

                if ( downPosition != SwipeListView.INVALID_POSITION && swipeAction == SwipeListView.SWIPE_ACTION_MENU_FILL_PART ){
                    if (opened.get(downPosition)){
                        setSwipeMode(SwipeListView.SWIPE_MODE_BOTH);
                    } else {
                        setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
                    }
                }

                if (downPosition != SwipeListView.INVALID_POSITION && swipeAction == SwipeListView.SWIPE_ACTION_MENU_FILL_ALL){
                    if (opened.get(downPosition)){
                        setSwipeMode(SwipeListView.SWIPE_MODE_BOTH);
                    } else {
                        setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
                    }
                }

                view.onTouchEvent(motionEvent);
                return true;

            }
            case MotionEvent.ACTION_UP: {
                if (velocityTracker == null || !swiping || downPosition == ListView.INVALID_POSITION) {
                    break;
                }
                float deltaX = motionEvent.getX() - downX;
                velocityTracker.addMovement(motionEvent);
                velocityTracker.computeCurrentVelocity(1000);
                float absVelocityX = Math.abs(velocityTracker.getXVelocity());

                if (!opened.get(downPosition)) {

                    if (swipeMode == SwipeListView.SWIPE_MODE_LEFT && velocityTracker.getXVelocity() > 0) {
                        absVelocityX = 0.0f;
                    }

                    if (swipeMode == SwipeListView.SWIPE_MODE_RIGHT && velocityTracker.getXVelocity() < 0) {
                        absVelocityX = 0.0f;
                    }
                }

                float absVelocityY = Math.abs(velocityTracker.getYVelocity());

                boolean swap = false;
                boolean swapRight = false;

                if (minFlingVelocity <= absVelocityX && absVelocityX <= maxFlingVelocity && absVelocityY * 2 < absVelocityX) {
                    swapRight = velocityTracker.getXVelocity() > 0;
                    if (swapRight != swipingRight && swipeActionLeft != swipeActionRight) {
                        swap = false;
                    } else if (opened.get(downPosition) && openedRight.get(downPosition) && swapRight) {
                        swap = false;
                    } else if (opened.get(downPosition) && !openedRight.get(downPosition) && !swapRight) {
                        swap = false;
                    } else {
                        swap = true;
                    }
                } else if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_MENU_FILL_ALL && Math.abs(deltaX) > (viewWidth*0.2)){
                    swap = true;
                    swapRight = deltaX > 0;
                } else if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_MENU_FILL_PART && Math.abs(deltaX) > (viewWidth - blankViewWidth)){
                    swap = true;
                    swapRight = deltaX > 0;
                } else if (Math.abs(deltaX) > viewWidth * STANDARD_LIMIT) {
                    swap = true;
                    swapRight = deltaX > 0;
                }

                generateAnimate(frontView, swap, swapRight, downPosition, deltaX);
                velocityTracker.recycle();
                velocityTracker = null;
                downX = 0;
                swiping = false;
                skipMove = false ;
                break;

            }
            case MotionEvent.ACTION_MOVE: {
                if (velocityTracker == null || this.paused || downPosition == ListView.INVALID_POSITION) {
                    break;
                }

                velocityTracker.addMovement(motionEvent);
                velocityTracker.computeCurrentVelocity(1000);
                float absVelocityX = Math.abs(velocityTracker.getXVelocity());
                float absVelocityY = Math.abs(velocityTracker.getYVelocity());
                float deltaX = motionEvent.getX() - downX;
                float deltaMode = Math.abs(deltaX);

                int swipeMode = this.swipeMode;
                int changeSwipeMode = swipeListView.changeSwipeMode(downPosition);

                if (changeSwipeMode >= 0) {
                    swipeMode = changeSwipeMode;
                }

                if (swipeMode == SwipeListView.SWIPE_MODE_NONE) {
                    deltaMode = 0;
                } else if (swipeMode != SwipeListView.SWIPE_MODE_BOTH) {
                        if (swipeMode == SwipeListView.SWIPE_MODE_LEFT && deltaX > 0) {
                            deltaMode = 0;
                        } else if (swipeMode == SwipeListView.SWIPE_MODE_RIGHT && deltaX < 0) {
                            deltaMode = 0;
                        }
                }

                if (deltaMode > slop && swipeCurrentAction == SwipeListView.SWIPE_ACTION_NONE && absVelocityY < absVelocityX) {
                    swiping = true;
                    swipingRight = (deltaX > 0);
                    if (opened.get(downPosition)) {
                        if (swipeAction == SwipeListView.SWIPE_ACTION_MENU_FILL_ALL){
                            if (!swipingRight ){
                                skipMove = true ;
                            } else {
                                skipMove = false ;
                            }
                            swipeCurrentAction = swipeAction;

                        }

                         else if (swipeAction == SwipeListView.SWIPE_ACTION_MENU_FILL_PART){
                            swipeCurrentAction = swipeAction;
                        } else
                             swipeCurrentAction = SwipeListView.SWIPE_ACTION_DISMISS;
                    } else {
                            swipeCurrentAction = swipeAction;
                            swipeListView.onStartOpen(downPosition, swipeCurrentAction, swipingRight);
                    }
                    swipeListView.requestDisallowInterceptTouchEvent(true);
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (MotionEventCompat.getActionIndex(motionEvent) << MotionEventCompat.ACTION_POINTER_INDEX_SHIFT));
                    swipeListView.onTouchEvent(cancelEvent);
                }

                if (swiping && downPosition != ListView.INVALID_POSITION) {
                    move(deltaX);
                    return true;
                }
                if (!swiping ){
                    swipeListView.resetScrolling();
//                    return true ;
                }
                break;
            }
        }

        return false;
    }


    private void generateAnimate(final View view, final boolean swap, final boolean swapRight, final int position, float deltaX) {
        if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_REVEAL) {
            generateRevealAnimate(view, swap, swapRight, position);
        }
        if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_DISMISS) {
            generateDismissAnimate(parentView, swap, swapRight, position);
        }
        if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_TODO) {
            generateTodoAnimate(view,swap,swapRight, position, deltaX);
        }
        if(swipeCurrentAction == SwipeListView.SWIPE_ACTION_MENU_FILL_ALL){
            generateMenuFillAllAnimate(view,swap,swapRight,position);
        }

        if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_MENU_FILL_PART){
            generateMenuFillPartAnimate(view,swap,swapRight,position, deltaX);
        }

    }

    private void generateMenuFillPartAnimate(View view, final boolean swap, final boolean swapRight,final int position, float deltaX){

       int moveTo = swap ? blankViewWidth - viewWidth : 0 ;
       if ( opened.get(position)){
           if (deltaX > 0){
               moveTo = 0 ;
           } else {
               moveTo = blankViewWidth - viewWidth ;
           }
       }

       final boolean isClose = moveTo == 0 ;
       if (!isClose){
           closeAllRevealView();
       }

       ViewPropertyAnimator.animate(view)
                .translationX(moveTo)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeListView.resetScrolling();

                        opened.set(position,!isClose);
                        if (!isClose) {
                            swipeListView.onOpened(position,false);
                            pendingDismissPositions.add(position);
                        } else {
                            swipeListView.onClosed(position, false);
                        }
                        resetCell();
                    }

                });
    }

    private void generateMenuFillAllAnimate(View view,final boolean swap,final boolean swapRight,final int position){
            if (skipMove){
                swipeListView.resetScrolling();
                return ;
            }
            int moveTo = 0;
            if (opened.get(position)){
                moveTo = swap ? 0 : (int) (-viewWidth + leftOffSet) ;
            } else {
                moveTo = swap ? (int) (-viewWidth + leftOffSet) : 0 ;
            }

        final boolean isOpen = !opened.get(position) && swap ;
        if (isOpen){
            closeAllRevealView();
        }

        ViewPropertyAnimator.animate(view)
                    .translationX(moveTo)
                    .setDuration(animationTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            swipeListView.resetScrolling();
                            if (swap) {
                                boolean aux = !opened.get(position);
                                opened.set(position, aux);
                                if (aux) {
                                    swipeListView.onOpened(position, swapRight);
                                    openedRight.set(position, swapRight);
                                    pendingDismissPositions.add(position);
                                } else {
                                    swipeListView.onClosed(position, openedRight.get(position));
                                    pendingDismissPositions.remove(Integer.valueOf(position));
                                }
                            }
                            resetCell();
                        }

                    });

            // new feature
            ViewPropertyAnimator.animate(backView)
                    .translationX(viewWidth + moveTo)
                    .setDuration(animationTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            swipeListView.resetScrolling();
                            resetCell();
                        }

                    });
    }

    private void generateTodoAnimate(final View view,boolean swap, boolean swapRight, final int position, float deltaX) {
        boolean temp = false ;
        int imgAnimatePosition = 0 ;
        float absDeltaX = Math.abs(deltaX);
        if (deltaX > 0) {
            if (absDeltaX >= viewWidth * BORDER_TWO) {
                generateDismissAnimate(parentView,swap,swapRight,position);
                return ;
            } else if (absDeltaX >= viewWidth * BORDER_ONE) {
                swipeListView.onPlusItem(position);
            }
            imgAnimatePosition = - imgTodoDistance ;
        } else {
            if (absDeltaX >= viewWidth * BORDER_TWO) {
                temp = true ;
            } else if (absDeltaX >= viewWidth * BORDER_ONE) {
                swipeListView.onSubItem(position);
            }
            imgAnimatePosition = imgTodoDistance ;
        }
//        swap = absDeltaX > viewWidth * BORDER_ONE ;

        final boolean isEdit = temp ;
        ViewPropertyAnimator.animate(view)
                .translationX(0)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if(isEdit){
                            swipeListView.onEditItem(position);
                        }
                        swipeListView.resetScrolling();
                        resetCell();
                        resetFlagBackground();
                    }
                });
//        if (swap) {
            ViewPropertyAnimator.animate(imgTodo)
                    .translationX(imgAnimatePosition)
                    .setDuration(animationTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });
//        }
    }


    private void generateDismissAnimate(final View view, final boolean swap, final boolean swapRight, final int position) {
        int moveTo = 0;
        int alpha = 1;

        if (swap) {
            moveTo = swapRight ? (int) (viewWidth - rightOffSet) : (int) (-viewWidth + leftOffSet);
            alpha = 0;
        }

        ViewPropertyAnimator.animate(view)
                .translationX(moveTo)
                .alpha(alpha)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        swipeListView.resetScrolling();
                        if (swap) {
                            performDismiss(view, position, false);
                        }
                        resetCell();
                    }
                });
    }

    void closeOpenedItems() {
        if (opened != null) {
            int start = swipeListView.getFirstVisiblePosition();
            int end = swipeListView.getLastVisiblePosition();
            for (int i = start; i <= end; i++) {
                if (opened.get(i)) {
                    closeAnimate(swipeListView.getChildAt(i - start).findViewById(swipeFrontViewId), i);
                }
            }
        }
    }


    protected void performDismiss(final View dismissView, final int dismissPosition, final boolean isDismisses) {
        enableDisableViewGroup((ViewGroup) dismissView, false);
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        final ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removePendingDismisses(originalHeight,isDismisses);
                if (!isDismisses) {
                    pendingDismissPositions.remove(Integer.valueOf(dismissPosition));
                    resetLastRevealedViews();
                }
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                enableDisableViewGroup((ViewGroup) dismissView, true);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lp.height = (Integer) animator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });


        pendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
        animator.start();
    }

    // reset last revealed view for recycle
    private void resetLastRevealedViews() {
        if (swipeAction == SwipeListView.SWIPE_ACTION_REVEAL){
            Collections.sort(pendingDismissPositions);
        }
        int size = pendingDismissPositions.size();
        int lastItem = swipeListView.getCount();
        int start = swipeListView.getFirstVisiblePosition();

        for (int i = 0; i < size; i++) {
            int position = pendingDismissPositions.get(i);
            dismissUponReveal = position > lastDismissPosition;
            if (dismissUponReveal) {
                if (position > 0 && !opened.get(position - 1)) {
                    View front = swipeListView.getChildAt(position - 1 - start).findViewById(swipeFrontViewId);
                    ViewHelper.setTranslationX(front, viewWidth);
                    opened.set(position - 1, true);
                }

                if (position <= lastItem && !opened.get(position + 1)) {
                    View front = swipeListView.getChildAt(position - start).findViewById(swipeFrontViewId);
                    ViewHelper.setTranslationX(front, 0.0f);
                    opened.set(position, false);
                }
                pendingDismissPositions.set(i, position - 1);
            }
        }

    }

    // remove all pending dismisses
    private void removePendingDismisses(int originalHeight, boolean isDismisses) {

        Collections.sort(pendingDismisses);
        int size = pendingDismisses.size();
        int[] dismissPositions = new int[size];
        for (int i = 0; i < size; i++) {
            dismissPositions[i] = pendingDismisses.get(i).position;
        }

        lastDismissPosition = size > 0 ? dismissPositions[size - 1] : -1;

        swipeListView.onDismiss(dismissPositions);

        ViewGroup.LayoutParams lp;
        for (PendingDismissData pendingDismissData : pendingDismisses) {
            if (pendingDismissData.view != null) {
                ViewHelper.setAlpha(pendingDismissData.view, 1.0f);
                ViewHelper.setTranslationX(pendingDismissData.view, 0.0f);
                lp = pendingDismissData.view.getLayoutParams();
                lp.height = originalHeight;
                pendingDismissData.view.setLayoutParams(lp);
                if (isDismisses) {
                    ViewHelper.setTranslationX(pendingDismissData.view.findViewById(swipeFrontViewId), 0.0f);
                    opened.set(pendingDismissData.position, false);
                } else if (!opened.get(pendingDismissData.position + 1)) {
                    ViewHelper.setTranslationX(pendingDismissData.view.findViewById(swipeFrontViewId), 0.0f);
                    opened.set(pendingDismissData.position, false);
                }

            }
        }
        resetPendingDismisses();
    }

    //reset list pending dismisses
    protected void resetPendingDismisses() {
        pendingDismisses.clear();
    }

    private void generateRevealAnimate(final View view, final boolean swap, final boolean swapRight, final int position) {
        int moveTo = 0;
        if (swap) {
            moveTo = swapRight ? (int) (viewWidth - rightOffSet) : (int) (-viewWidth + leftOffSet);
        }

        ViewPropertyAnimator.animate(view)
                .translationX(moveTo)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeListView.resetScrolling();
                        if (swap) {
                            boolean aux = !opened.get(position);
                            opened.set(position, aux);
                            if (aux) {
                                swipeListView.onOpened(position, swapRight);
                                openedRight.set(position, swapRight);
                            } else {
                                swipeListView.onClosed(position, openedRight.get(position));
                            }
                            pendingDismissPositions.add(position);
                        }
                        resetCell();
                    }

                });
    }

    // dismiss the first item revealed
    private void generateAnimatePendingDismiss() {
        int size = pendingDismissPositions.size();
        int start = swipeListView.getFirstVisiblePosition();
        for (int i = 0; i < size - 1; i++) {
            View parentView = swipeListView.getChildAt(pendingDismissPositions.get(i) - start);
            performDismiss(parentView, pendingDismissPositions.get(i), false);
        }
    }

    // dismiss all pending
    private void dismissRevealedView() {
        for (int i = 0; i < pendingDismissPositions.size(); i++) {
            int start = swipeListView.getFirstVisiblePosition();
            View parentView = swipeListView.getChildAt(pendingDismissPositions.get(i) - start);
            if (parentView != null) {
                performDismiss(parentView, pendingDismissPositions.get(i), true);
            }
        }
        pendingDismissPositions.clear();
    }

    private void closeAllRevealView(){
        for (int i = 0; i < pendingDismissPositions.size(); i++) {
            closeAnimate(pendingDismissPositions.get(i));
        }
        pendingDismissPositions.clear();
    }

    // reset cell when
    private void resetCell() {
        if (downPosition != ListView.INVALID_POSITION) {
            frontView.setClickable(true);
            frontView.setLongClickable(opened.get(downPosition));
            frontView = null;
            backView = null;
            downPosition = ListView.INVALID_POSITION;
        }
    }

    // reset boolean flag background
    private void resetFlagBackground(){
        isSetBGOneLeft = false ;
        isSetBGTwoLeft = false ;
        isSetBGOneRight = false ;
        isSetBGTwoRight = false ;
    }

    public void move(float deltaX) {
        if (skipMove){
            return ;
        }
        swipeListView.onMove(downPosition, deltaX);
        float imgTodoMoveTo = 0 ;
        if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_DISMISS) {
            ViewHelper.setTranslationX(parentView, deltaX);
            ViewHelper.setAlpha(parentView, Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / viewWidth)));

        } else if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_REVEAL ){
            ViewHelper.setTranslationX(frontView, deltaX);
        } else if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_MENU_FILL_PART){
            if (opened.get(downPosition)){
                deltaX = deltaX + blankViewWidth - viewWidth ;
                deltaX = deltaX > 0 ? 0 : deltaX ;
            }
            ViewHelper.setTranslationX(frontView, deltaX);
        } else if (swipeCurrentAction == SwipeListView.SWIPE_ACTION_MENU_FILL_ALL) {
            if (opened.get(downPosition)){
                if (deltaX >= 0){
                    ViewHelper.setTranslationX(frontView,-viewWidth + deltaX);
                    ViewHelper.setTranslationX(backView,deltaX);
                }
            } else if(deltaX <= 0)  {
                    ViewHelper.setTranslationX(frontView, deltaX);
                    ViewHelper.setTranslationX(backView, viewWidth + deltaX);
            }

        } else {
            if (deltaX > 0) {
                isSetBGTwoRight = true;
                isSetBGOneRight = false;
                if (Math.abs(deltaX) < viewWidth * BORDER_TWO && !isSetBGOneLeft) {
                    imgTodo.setImageResource(R.drawable.plus);
                    backView.setBackgroundColor(Color.YELLOW);
                    isSetBGOneLeft = true;
                    isSetBGTwoLeft = false;
                }
                if (Math.abs(deltaX) >= viewWidth * BORDER_TWO && !isSetBGTwoLeft) {
                    imgTodo.setImageResource(R.drawable.delete_menu);
                    backView.setBackgroundColor(Color.BLACK);
                    isSetBGTwoLeft = true;
                    isSetBGOneLeft = false;
                }
                imgTodoMoveTo = deltaX - imgTodoDistance ;
            } else {
                isSetBGTwoLeft = false;
                isSetBGOneLeft = false;
                if (Math.abs(deltaX) < viewWidth * BORDER_TWO && !isSetBGOneRight) {
                    imgTodo.setImageResource(R.drawable.sub);
                    backView.setBackgroundColor(Color.BLUE);
                    isSetBGOneRight = true;
                    isSetBGTwoRight = false;
                }
                if (Math.abs(deltaX) >= viewWidth * BORDER_TWO && !isSetBGTwoRight) {
                    imgTodo.setImageResource(R.drawable.edit_todo);
                    backView.setBackgroundColor(Color.GREEN);
                    isSetBGTwoRight = true;
                    isSetBGOneRight = false;
                }
                imgTodoMoveTo = deltaX + imgTodoDistance ;
            }
            ViewHelper.setTranslationX(frontView, deltaX);
//            if (Math.abs(deltaX) > viewWidth *BORDER_ONE) {
                 ViewHelper.setTranslationX(imgTodo, imgTodoMoveTo);
//            } else {
//                    ViewHelper.setTranslationX(imgTodo,0);
//                }

        }
    }

    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            return other.position - position;
        }
    }

    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup != null ? viewGroup.getChildCount() : 0;
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            child.setEnabled(enabled);
            if (child instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) child, enabled);
            }
        }
    }
}
