package vn.com.vng.swipelistview;

/**
 * Created by NhaN on 2/6/2015.
 */
public enum TypeListView {
    EMAIL(0),
    SMS(1);

    private int value ;

    TypeListView(int value) {
        this.value = value ;
    }

    public int getValue(){
        return value;
    }
    
}
