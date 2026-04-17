package com.example.universeti.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.universeti.R;
import com.example.universeti.model.Product;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface Listener {
        void onEdit(Product p);
        void onDelete(Product p);
    }

    private final List<Product> items = new ArrayList<>();
    private final Listener listener;

    public ProductAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Product> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final Product p = items.get(position);
        holder.tvName.setText(p.getName());
        holder.tvPrice.setText(holder.itemView.getContext()
                .getString(R.string.price_rub, p.getPrice()));
        String img = p.getImageUrl();
        if (img != null && !img.isEmpty()) {
            if (img.startsWith("/")) {
                Glide.with(holder.ivThumb).load(new File(img)).into(holder.ivThumb);
            } else {
                Glide.with(holder.ivThumb).load(img).into(holder.ivThumb);
            }
        } else {
            holder.ivThumb.setImageDrawable(null);
        }
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(p));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvName, tvPrice;
        Button btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
