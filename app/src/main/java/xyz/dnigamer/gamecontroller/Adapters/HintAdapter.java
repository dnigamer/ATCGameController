package xyz.dnigamer.gamecontroller.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class HintAdapter extends ArrayAdapter<CharSequence> {

    public HintAdapter(Context context, int resource, CharSequence[] objects) {
        super(context, resource, objects);
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;
        if (position == 0) {
            textView.setTextColor(getContext().getResources().getColor(android.R.color.darker_gray, getContext().getTheme()));
        } else {
            textView.setTextColor(getContext().getResources().getColor(android.R.color.black, getContext().getTheme()));
        }
        return view;
    }
}
