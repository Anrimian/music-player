package com.github.anrimian.musicplayer.ui.common.dialogs.input;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutocompleteAdapter extends ArrayAdapter<String> {

    private final ListFilter listFilter = new ListFilter();

    private final String[] hints;

    private Callback<String> onItemClickListener;
    private List<String> filteredHints = new ArrayList<>();

    public AutocompleteAdapter(@NonNull Context context,
                               int res,
                               int tvRes,
                               String[] hints) {
        super(context, res, tvRes, hints);
        this.hints = hints;
        filteredHints.addAll(Arrays.asList(hints));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
//        if (v == null) {
//            v = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_autocomplete_password_title, parent, false);
//        }
//        ImageView ivServiceIcon = v.findViewById(R.id.iv_service_icon);
//        TextView tvServiceTitle = v.findViewById(R.id.service_title);
//        TextView tvServiceWebsite = v.findViewById(R.id.service_website);
//
//        String service = filteredHints.get(position);
//        tvServiceTitle.setText(service.getName());
//        tvServiceWebsite.setText(service.getUrl());
//        ivServiceIcon.setImageResource(Icon.getIconDrawableResId(ivServiceIcon.getContext(),
//                service.getIcon(),
//                false));
//
//        v.setOnClickListener(b -> onItemClickListener.call(service));
        return v;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return filteredHints.get(position);
    }

    @Override
    public int getCount() {
        return filteredHints.size();
    }

    public void setOnItemClickListener(Callback<String> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class ListFilter extends Filter {

        private int textSize = 0;

        @Override
        protected FilterResults performFiltering(CharSequence query) {
            FilterResults results = new FilterResults();

            if (TextUtils.isEmpty(query) || query.length() < textSize) {
                filteredHints.clear();
                filteredHints.addAll(Arrays.asList(hints));
            }

            if (TextUtils.isEmpty(query)) {
                results.values = hints;
                results.count = hints.length;
            } else {
                final String searchStr = query.toString().toLowerCase();

                ArrayList<String> matchValues = new ArrayList<>();

                for (String hint : filteredHints) {
                    String name = hint.toLowerCase();
                    if (name.startsWith(searchStr)) {
                        matchValues.add(hint);
                        continue;
                    }

                    String[] words = name.split(" ");
                    for (String word: words) {
                        if (word.startsWith(searchStr)) {
                            matchValues.add(hint);
                            break;
                        }
                    }
                }

                results.values = matchValues;
                results.count = matchValues.size();
                textSize = query.length();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            filteredHints = (ArrayList<String>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}
