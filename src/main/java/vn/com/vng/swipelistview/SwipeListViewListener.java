package vn.com.vng.swipelistview;

/**
 * Created by NhaN on 1/22/2015.
 */
public interface SwipeListViewListener {

    /**
     * Used when user want to change swipe list mode on some rows. Return SWIPE_MODE_DEFAULT
     * if you don't want to change swipe list mode
     * @param position position that you want to change
     * @return type
     */
    public int onChangeSwipeMode(int position);

    /**
     * Called when user dismisses items
     */
    public void onDismiss(int[] positions);

    /**
     * Called when close animation finishes
     * @param position of the view in the list
     * @param fromRight Close from right
     */
    public void onClosed(int position, boolean fromRight);

    /**
     * Called when open animation finishes
     * @param position of the view in the list
     * @param toRight Open to right
     */
    public void onOpened(int position, boolean toRight);

    /**
     * Called when user is moving an item
     * @param position of the view in the list
     * @param x Current position X
     */
    public void onMove(int position, float x);

    /**
     * Start open item
     * @param position of the view in the list
     * @param action current action
     * @param right to right
     */
    public void onStartOpen(int position, int action, boolean right);

    /**
     * Start close item
     * @param position of the view in the list
     * @param right
     */

    public void onStartClose(int position, boolean right);

    /**
     * Called when user clicks on the front view
     * @param position of the view in the list
     */
    public void onClickFrontView(int position);

    /**
     * Called when user clicks on the back view
     * @param position of the view in the list
     */
    public void onClickBackView(int position);


    /**
     * User is in first item of list
     */
    public void onFirstListItem();

    /**
     * User is in last item of list
     */
    public void onLastListItem();

    /**
     * User is in last item of list
     */
    public void onPlusItem(int position);

    /**
     * User is in last item of list
     */
    public void onSubItem(int position);

    /**
     * User is in last item of list
     */
    public void onEditItem(int position);

    public void onMenuCloseClick(int position);

    public void onMenuEditClick(int position);

    public void onMenuDeleteClick(int position);

}
