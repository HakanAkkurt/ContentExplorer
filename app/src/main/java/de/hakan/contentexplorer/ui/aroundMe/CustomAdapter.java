package de.hakan.contentexplorer.ui.aroundMe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import de.hakan.contentexplorer.R;

public class CustomAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    private List<String> itemNames;
    private List<Integer> imgIDs;

    public CustomAdapter(Context context, List<String> itemNames, List<Integer> imgIDs) {
        super(context, R.layout.list_item, itemNames);
        this.mContext = context;
        this.itemNames = itemNames;
        this.imgIDs = imgIDs;
    }

    public void setItemNames(List<String> itemNames) {
        this.itemNames = itemNames;
        notifyDataSetChanged();
    }

    public void setImgIDs(List<Integer> imgIDs) {
        this.imgIDs = imgIDs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);

        TextView textView = rowView.findViewById(R.id.item_name);
        ImageView imageView = rowView.findViewById(R.id.item_image);

        textView.setText(itemNames.get(position));
        imageView.setImageResource(imgIDs.get(position));

        return rowView;
    }
}

