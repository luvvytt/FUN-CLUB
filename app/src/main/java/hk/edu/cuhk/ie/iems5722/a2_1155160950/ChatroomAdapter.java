package hk.edu.cuhk.ie.iems5722.a2_1155160950;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ChatroomAdapter extends ArrayAdapter<Chatroom> {
    // View lookup cache
    private static class ViewHolder {
        TextView names;
    }

    public ChatroomAdapter(Context context, List<Chatroom> chatroom) {
        super(context, R.layout.item_text, chatroom);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Chatroom chatroom = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.chatroom_item, parent, false);
            viewHolder.names = (TextView) convertView.findViewById(R.id.chatroom_text);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.assert chatroom != null;
        viewHolder.names.setText(chatroom.text);
        // Return the completed view to render on screen
        return convertView;
    }
}
