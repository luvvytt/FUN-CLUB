package hk.edu.cuhk.ie.iems5722.a2_1155160950;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static hk.edu.cuhk.ie.iems5722.a2_1155160950.NavigationActivity.MyName;

public class InputTextAdapter extends ArrayAdapter<InputText> {
    // View lookup cache


    private static class ViewHolder {
        TextView input_text;
        TextView time;
        TextView name;

    }

    public InputTextAdapter(Context context, ArrayList<InputText> texts) {
        super(context, R.layout.item_text, texts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        InputText inputText = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag





            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            String temp = inputText.name;
            System.out.println("temp"+temp);
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_text, parent, false);
            if (temp.equals(MyName)) {
                viewHolder.input_text = (TextView) convertView.findViewById(R.id.tvText);
                viewHolder.time = (TextView) convertView.findViewById(R.id.tvTime);
                viewHolder.name = (TextView) convertView.findViewById(R.id.tvName);
                LinearLayout layout_right = convertView.findViewById(R.id.item_text_right);
                LinearLayout layout_left = convertView.findViewById(R.id.item_text_left);
                layout_left.setVisibility(View.GONE);
                String name = ("user:" + inputText.name);
                viewHolder.input_text.setText(inputText.input_text);
                String t = inputText.time.substring(inputText.time.length() - 5, inputText.time.length());
                viewHolder.time.setText(t);
                viewHolder.name.setText(name);

            } else {
                viewHolder.input_text = (TextView) convertView.findViewById(R.id.tvText_L);
                viewHolder.time = (TextView) convertView.findViewById(R.id.tvTime_L);
                viewHolder.name = (TextView) convertView.findViewById(R.id.tvName_L);
                LinearLayout layout_right = convertView.findViewById(R.id.item_text_right);
                LinearLayout layout_left = convertView.findViewById(R.id.item_text_left);
                layout_right.setVisibility(View.GONE);
                String name = ("user:" + inputText.name);
                viewHolder.input_text.setText(inputText.input_text);
                String t = inputText.time.substring(inputText.time.length() - 5, inputText.time.length());
                viewHolder.time.setText(t);
                viewHolder.name.setText(name);

            }
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);

        // Populate the data from the data object via the viewHolder object
        // into the template view.


        // Return the completed view to render on screen
        return convertView;
    }
}
