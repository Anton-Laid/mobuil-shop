package com.example.universeti.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universeti.R;
import com.example.universeti.data.ProductRepository;
import com.example.universeti.data.ShelfRepository;
import com.example.universeti.model.Product;
import com.example.universeti.model.Shelf;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserHomeActivity extends AppCompatActivity {

    private static final float ENTRANCE_X = 0.02f;
    private static final float ENTRANCE_Y = 0.82f;
    private static final float ENTRANCE_W = 0.22f;
    private static final float ENTRANCE_H = 0.14f;

    private static final float CASHIER_X = 0.72f;
    private static final float CASHIER_Y = 0.82f;
    private static final float CASHIER_W = 0.26f;
    private static final float CASHIER_H = 0.14f;

    private ProductRepository productRepo;
    private ShelfRepository shelfRepo;
    private UserProductAdapter adapter;
    private FrameLayout mapContainer;
    private EditText etSearch;

    private List<Shelf> shelves = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filtered = new ArrayList<>();
    private Product selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        setTitle(R.string.user_home_title);

        productRepo = new ProductRepository(this);
        shelfRepo = new ShelfRepository(this);
        shelves = shelfRepo.loadAll();

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            tvWelcome.setText(getString(R.string.user_home_title) + " · " + username);
        }

        mapContainer = findViewById(R.id.mapContainer);
        etSearch = findViewById(R.id.etSearch);

        RecyclerView rv = findViewById(R.id.rvProducts);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserProductAdapter(p -> {
            selected = p;
            adapter.setSelectedId(p.getId());
            renderMap();
        });
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { applyFilter(s.toString()); }
        });

        reload();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        allProducts = productRepo.loadAll();
        shelves = shelfRepo.loadAll();
        applyFilter(etSearch.getText().toString());
    }

    private void applyFilter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (q.isEmpty() || p.getName().toLowerCase(Locale.ROOT).contains(q)) {
                filtered.add(p);
            }
        }
        if (selected != null) {
            boolean stillHere = false;
            for (Product p : filtered) {
                if (p.getId().equals(selected.getId())) { stillHere = true; break; }
            }
            if (!stillHere) selected = null;
        }
        if (selected == null && filtered.size() == 1) {
            selected = filtered.get(0);
        }
        adapter.setItems(filtered);
        adapter.setSelectedId(selected == null ? null : selected.getId());
        renderMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void renderMap() {
        mapContainer.post(() -> {
            mapContainer.removeAllViews();
            int w = mapContainer.getWidth();
            int h = mapContainer.getHeight();
            if (w == 0 || h == 0) return;

            addFixedBox(w, h, ENTRANCE_X, ENTRANCE_Y, ENTRANCE_W, ENTRANCE_H,
                    R.drawable.entrance_bg, getString(R.string.label_entrance));
            addFixedBox(w, h, CASHIER_X, CASHIER_Y, CASHIER_W, CASHIER_H,
                    R.drawable.cashier_bg, getString(R.string.label_cashier));

            for (int i = 0; i < shelves.size(); i++) {
                Shelf s = shelves.get(i);
                addBox(w, h, s.getX(), s.getY(), s.getW(), s.getH(),
                        R.drawable.shelf_bg, getString(R.string.shelf_label, i + 1));
            }

            if (selected != null && selected.getMapX() >= 0 && selected.getMapY() >= 0) {
                addMarker(selected.getMapX(), selected.getMapY(), w, h, selected.getName());
            }
        });
    }

    private void addFixedBox(int w, int h, float x, float y, float rw, float rh,
                             int bgRes, String label) {
        addBox(w, h, x, y, rw, rh, bgRes, label);
    }

    private void addBox(int w, int h, float x, float y, float rw, float rh,
                        int bgRes, String label) {
        FrameLayout box = new FrameLayout(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                (int) (rw * w), (int) (rh * h));
        lp.leftMargin = (int) (x * w);
        lp.topMargin = (int) (y * h);
        box.setLayoutParams(lp);
        box.setBackgroundResource(bgRes);
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.BLACK);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        tlp.gravity = Gravity.CENTER;
        tv.setLayoutParams(tlp);
        box.addView(tv);
        mapContainer.addView(box);
    }

    private void addMarker(float px, float py, int w, int h, String label) {
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (24 * density);
        View dot = new View(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        lp.leftMargin = (int) (px * w) - size / 2;
        lp.topMargin = (int) (py * h) - size / 2;
        dot.setLayoutParams(lp);
        dot.setBackgroundResource(R.drawable.marker);
        mapContainer.addView(dot);

        if (label != null && !label.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText(label);
            tv.setTextSize(12);
            tv.setTextColor(Color.BLACK);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setBackgroundColor(0xCCFFFFFF);
            int padH = (int) (4 * density);
            int padV = (int) (1 * density);
            tv.setPadding(padH, padV, padH, padV);
            tv.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.AT_MOST));
            int tw = tv.getMeasuredWidth();
            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            int cx = (int) (px * w);
            int gap = (int) (2 * density);
            int rightLeft = cx + size / 2 + gap;
            if (rightLeft + tw <= w) {
                tlp.leftMargin = rightLeft;
            } else {
                tlp.leftMargin = Math.max(0, cx - size / 2 - gap - tw);
            }
            tlp.topMargin = (int) (py * h) - (int) (12 * density / 2);
            tv.setLayoutParams(tlp);
            mapContainer.addView(tv);
        }
    }
}
