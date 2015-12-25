package vn.com.vng.swipelistview;

/**
 * Created by NhaN on 1/22/2015.
 */
public class BaseSwipeListViewListener implements SwipeListViewListener {
    @Override
    public int onChangeSwipeMode(int position) {
        return -1;
    }

    @Override
    public void onDismiss(int[] positions) {

    }

    @Override
    public void onClosed(int position, boolean fromRight) {

    }

    @Override
    public void onOpened(int position, boolean toRight) {

    }

    @Override
    public void onMove(int position, float x) {

    }

    @Override
    public void onStartOpen(int position, int action, boolean right) {

    }

    @Override
    public void onStartClose(int position, boolean right) {

    }

    @Override
    public void onClickFrontView(int position) {

    }

    @Override
    public void onClickBackView(int position) {

    }

    @Override
    public void onFirstListItem() {

    }

    @Override
    public void onLastListItem() {

    }


    @Override
    public void onPlusItem(int position){

    }

    @Override
    public void onSubItem(int position) {

    }

    @Override
    public void onEditItem(int position) {

    }

    @Override
    public void onMenuCloseClick(int position) {

    }

    @Override
    public void onMenuEditClick(int position) {

    }

    @Override
    public void onMenuDeleteClick(int position) {

    }
}
