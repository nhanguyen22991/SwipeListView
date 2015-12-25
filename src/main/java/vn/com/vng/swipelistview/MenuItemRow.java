package vn.com.vng.swipelistview;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by NhaN on 2/6/2015.
 */
public class MenuItemRow extends FrameLayout {

    public final static int FILL_ALL_TYPE = 1 ;
    public final static int FILL_PART_TYPE = 2 ;

    public MenuItemRow(Context context,TypeListView type, int typeMenu,int layout) {
        super(context);
        initLayout(context,type,typeMenu,layout);
    }

    private void initLayout(Context context, TypeListView type, int typeMenu, int layout){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View parentView = inflater.inflate(R.layout.layout_item_row_menu,this);
        View parentView = inflater.inflate(layout,this);
        ViewGroup backView = (ViewGroup) parentView.findViewById(R.id.back_view_id);
        // add blank text_view when menu type is fill part
        if (typeMenu == MenuItemRow.FILL_PART_TYPE){
            TextView textView = new TextView(context);
            textView.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3);
            textView.setLayoutParams(lp);
            textView.setId(ItemMenuId.BLANK_VIEW.getValue());
            backView.addView(textView);
        }

        switch (type){
            case EMAIL:
                ImageButton imgButton = new ImageButton(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                imgButton.setLayoutParams(lp);
                imgButton.setBackgroundResource(R.drawable.border_img_button);
                imgButton.setImageResource(R.drawable.edit_manu);
                imgButton.setId(ItemMenuId.EDIT.getValue());
                backView.addView(imgButton);

                imgButton = new ImageButton(context);
                lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                imgButton.setLayoutParams(lp);
                imgButton.setBackgroundResource(R.drawable.border_img_button);
                imgButton.setImageResource(R.drawable.delete_menu);
                imgButton.setId(ItemMenuId.DELETE.getValue());
                backView.addView(imgButton);

                imgButton = new ImageButton(context);
                lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                imgButton.setLayoutParams(lp);
                imgButton.setBackgroundResource(R.drawable.border_img_button);
                imgButton.setImageResource(R.drawable.close_menu);
                imgButton.setId(ItemMenuId.CLOSE.getValue());
                backView.addView(imgButton);
                break;

            case SMS:
                imgButton = new ImageButton(context);
                lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
                imgButton.setLayoutParams(lp);
                imgButton.setBackgroundResource(R.drawable.border_img_button);
                imgButton.setImageResource(R.drawable.delete_menu);
                imgButton.setId(ItemMenuId.DELETE.getValue());
                backView.addView(imgButton);
                break;
        }
    }

}
