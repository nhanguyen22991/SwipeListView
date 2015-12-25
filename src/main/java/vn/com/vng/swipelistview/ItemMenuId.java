package vn.com.vng.swipelistview;

/**
 * Created by NhaN on 2/6/2015.
 */
public enum ItemMenuId {
    DELETE(R.id.edit_menu),
    EDIT(R.id.delete_menu),
    CLOSE(R.id.close_menu),
    BLANK_VIEW(R.id.blank_view);

    private int value ;

    ItemMenuId(int value) {
        this.value = value ;
    }

    public int getValue(){
        return this.value ;
    }

}
