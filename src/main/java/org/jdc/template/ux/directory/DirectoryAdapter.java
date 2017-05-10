package org.jdc.template.ux.directory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.devbrackets.android.recyclerext.adapter.RecyclerListAdapter;
import com.squareup.picasso.Picasso;

import org.jdc.template.R;
import org.jdc.template.inject.Injector;
import org.jdc.template.model.database.main.individual.Individual;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DirectoryAdapter extends RecyclerListAdapter<DirectoryAdapter.ViewHolder, Individual> {
    private final LayoutInflater inflater;

    private OnItemClickListener listener;
    private long lastSelectedItemId = 0;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text1)
        TextView text1TextView;
        @BindView(R.id.list_item)
        View listItemView;
        @BindView(R.id.profilePic)
        ImageView profilePic;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void setText(String text) {
            text1TextView.setText(text);
        }
    }

    public DirectoryAdapter(Context context) {
        Injector.get().inject(this);

        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Individual individual = getItem(position);
        final long itemId = individual.getId();

        if(individual.getProfilePicture() != null && !individual.getProfilePicture().isEmpty()) {
            Picasso.with(context).load(individual.getProfilePicture()).placeholder(R.drawable.default_image).into(holder.profilePic);
        }else{
            Picasso.with(context).load(R.drawable.default_image).placeholder(R.drawable.default_image).into(holder.profilePic);
        }
        holder.text1TextView.setText(individual.getFullName());

        holder.listItemView.setOnClickListener(v -> onItemClicked(itemId));
    }

    private void onItemClicked(long selectedItemId) {
        this.lastSelectedItemId = selectedItemId;
        if (listener != null) {
            listener.onItemClick(selectedItemId);
        }
    }

    public long getLastSelectedItemId() {
        return lastSelectedItemId;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(long selectedItemId);
    }
}
