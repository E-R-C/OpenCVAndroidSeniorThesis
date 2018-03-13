package edu.hendrix.huynhem.buildingopencv.UI;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import edu.hendrix.huynhem.buildingopencv.Models.ListLabelTuple;
import edu.hendrix.huynhem.buildingopencv.R;

/**
 *
 */

public class TupleListAdapter extends ArrayAdapter<ListLabelTuple> implements View.OnClickListener {
    public TupleListAdapter(@NonNull Context context, int resource, List<ListLabelTuple> data) {
        super(context, resource, data);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final TupleListAdapter ref = this;
        final ListLabelTuple llt = getItem(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.tuple_list_row, null);
        }
        TextView tv1 = convertView.findViewById(R.id.ClassNameTextView);
        TextView tv2 = convertView.findViewById(R.id.NumImagesTextView);
        Button DeleteButton = convertView.findViewById(R.id.DeleteButton);

        tv1.setText(llt.getLabel());
        tv2.setText("" + llt.getFileNames().size());

        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref.remove(llt);
                ref.notifyDataSetChanged();
            }
        });
        return convertView;
    }

    @Override
    public void onClick(View view) {
        int position = (Integer) view.getTag();
        ListLabelTuple llt = getItem(position);


    }
}
