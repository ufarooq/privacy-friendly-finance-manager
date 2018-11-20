package org.secuso.privacyfriendlyfinance.activities.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.secuso.privacyfriendlyfinance.R;
import org.secuso.privacyfriendlyfinance.domain.model.Category;

import java.text.NumberFormat;
import java.util.List;

public class CategoryArrayAdapter extends ArrayAdapter<Category> {

    public CategoryArrayAdapter(Context context, List<Category> items) {
        super(context, 0, items);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item_category, parent, false);
        }


        Category category = getItem(position);

        TextView category_listItem_amount = convertView.findViewById(R.id.category_listItem_amount);
        TextView category_listItem_name = convertView.findViewById(R.id.category_listItem_name);

        NumberFormat format = NumberFormat.getCurrencyInstance();

        category_listItem_name.setText(category.getName());

        //TODO: Retrieve category balance
        long categoryBalance = 4242L;

        if (categoryBalance < 0) {
            category_listItem_amount.setTextColor(getContext().getResources().getColor(R.color.red));
        } else {
            category_listItem_amount.setTextColor(getContext().getResources().getColor(R.color.green));
        }
        category_listItem_amount.setText(format.format(categoryBalance));

        return convertView;
    }

}