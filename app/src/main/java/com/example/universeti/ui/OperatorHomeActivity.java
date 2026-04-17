package com.example.universeti.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.universeti.R;
import com.example.universeti.data.ProductRepository;
import com.example.universeti.data.ShelfRepository;
import com.example.universeti.model.Product;
import com.example.universeti.model.Shelf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OperatorHomeActivity extends AppCompatActivity {

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
    private ProductAdapter adapter;
    private FrameLayout mapContainer;

    private List<Shelf> shelves = new ArrayList<>();
    private boolean arrangeMode = false;
    private Button btnArrange;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ImageView dialogIvPreview;
    private String[] pickedImagePathHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_home);
        setTitle(R.string.operator_home_title);

        productRepo = new ProductRepository(this);
        shelfRepo = new ShelfRepository(this);
        shelves = shelfRepo.loadAll();

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            tvWelcome.setText(getString(R.string.operator_home_title) + " · " + username);
        }

        mapContainer = findViewById(R.id.mapContainer);

        RecyclerView rv = findViewById(R.id.rvProducts);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(new ProductAdapter.Listener() {
            @Override public void onEdit(Product p) { showEditor(p); }
            @Override public void onDelete(Product p) { confirmDelete(p); }
        });
        rv.setAdapter(adapter);

        btnArrange = findViewById(R.id.btnArrangeShelves);
        btnArrange.setOnClickListener(v -> toggleArrangeMode());

        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            if (arrangeMode) { toggleArrangeMode(); }
            showEditor(null);
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null || dialogIvPreview == null) return;
                    String path = copyUriToLocal(uri);
                    if (path == null) {
                        Toast.makeText(this, R.string.err_load_image, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (pickedImagePathHolder != null) pickedImagePathHolder[0] = path;
                    Glide.with(dialogIvPreview).load(new File(path)).into(dialogIvPreview);
                });

        refresh();
    }

    private String copyUriToLocal(Uri uri) {
        try {
            File dir = new File(getFilesDir(), "images");
            if (!dir.exists() && !dir.mkdirs()) return null;
            File out = new File(dir, UUID.randomUUID().toString() + ".img");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(out)) {
                if (in == null) return null;
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
            }
            return out.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

    private void toggleArrangeMode() {
        arrangeMode = !arrangeMode;
        if (arrangeMode) {
            btnArrange.setText(R.string.action_done);
            Toast.makeText(this, R.string.arrange_hint, Toast.LENGTH_SHORT).show();
        } else {
            btnArrange.setText(R.string.action_arrange_shelves);
            shelfRepo.saveAll(shelves);
        }
        renderMap();
    }

    private void refresh() {
        List<Product> list = productRepo.loadAll();
        adapter.setItems(list);
        renderMap();
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
                addShelfView(shelves.get(i), i, w, h);
            }

            if (!arrangeMode) {
                for (Product p : productRepo.loadAll()) {
                    if (p.getMapX() < 0 || p.getMapY() < 0) continue;
                    addMarker(p.getMapX(), p.getMapY(), w, h, 24, p.getName(), 12);
                }
            }
        });
    }

    private void addFixedBox(int w, int h, float x, float y, float rw, float rh,
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

    private void addShelfView(Shelf shelf, int index, int w, int h) {
        FrameLayout box = new FrameLayout(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                (int) (shelf.getW() * w), (int) (shelf.getH() * h));
        lp.leftMargin = (int) (shelf.getX() * w);
        lp.topMargin = (int) (shelf.getY() * h);
        box.setLayoutParams(lp);
        box.setBackgroundResource(arrangeMode ? R.drawable.shelf_bg_selected : R.drawable.shelf_bg);
        TextView tv = new TextView(this);
        tv.setText(getString(R.string.shelf_label, index + 1));
        tv.setTextColor(Color.BLACK);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        tlp.gravity = Gravity.CENTER;
        tv.setLayoutParams(tlp);
        box.addView(tv);

        if (arrangeMode) {
            attachDragHandler(box, shelf, w, h);
        }
        mapContainer.addView(box);
    }

    private void attachDragHandler(View view, Shelf shelf, int mapW, int mapH) {
        final float[] touch = new float[2];
        final float[] origin = new float[2];
        final List<Product>[] attached = new List[] { new ArrayList<>() };
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    touch[0] = event.getRawX();
                    touch[1] = event.getRawY();
                    origin[0] = shelf.getX();
                    origin[1] = shelf.getY();
                    attached[0] = new ArrayList<>();
                    for (Product p : productRepo.loadAll()) {
                        if (p.getMapX() < 0 || p.getMapY() < 0) continue;
                        if (shelf.contains(p.getMapX(), p.getMapY())) {
                            attached[0].add(p);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    float dx = (event.getRawX() - touch[0]) / mapW;
                    float dy = (event.getRawY() - touch[1]) / mapH;
                    float nx = clamp(origin[0] + dx, 0f, 1f - shelf.getW());
                    float ny = clamp(origin[1] + dy, 0f, 1f - shelf.getH());
                    float shiftX = nx - shelf.getX();
                    float shiftY = ny - shelf.getY();
                    shelf.setX(nx);
                    shelf.setY(ny);
                    for (Product p : attached[0]) {
                        p.setMapX(clamp(p.getMapX() + shiftX, 0f, 1f));
                        p.setMapY(clamp(p.getMapY() + shiftY, 0f, 1f));
                    }
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
                    lp.leftMargin = (int) (nx * mapW);
                    lp.topMargin = (int) (ny * mapH);
                    v.setLayoutParams(lp);
                    return true;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    for (Product p : attached[0]) {
                        productRepo.upsert(p);
                    }
                    v.performClick();
                    return true;
            }
            return false;
        });
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private void addMarker(float px, float py, int w, int h, int dp,
                           String label, int labelSp) {
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (dp * density);
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
            tv.setTextSize(labelSp);
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
            tlp.topMargin = (int) (py * h) - (int) (labelSp * density / 2);
            tv.setLayoutParams(tlp);
            mapContainer.addView(tv);
        }
    }

    private Shelf shelfAt(float px, float py) {
        for (Shelf s : shelves) {
            if (s.contains(px, py)) return s;
        }
        return null;
    }

    private void confirmDelete(Product p) {
        new AlertDialog.Builder(this)
                .setTitle(p.getName())
                .setMessage("Удалить товар?")
                .setPositiveButton("Удалить", (d, w) -> {
                    productRepo.delete(p.getId());
                    refresh();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showEditor(final Product existing) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_product_edit, null);
        final EditText etName = dialogView.findViewById(R.id.etName);
        final EditText etPrice = dialogView.findViewById(R.id.etPrice);
        final ImageView ivPreview = dialogView.findViewById(R.id.ivPreview);
        final Button btnPickImage = dialogView.findViewById(R.id.btnPickImage);
        final FrameLayout miniMap = dialogView.findViewById(R.id.miniMap);
        final TextView tvCoords = dialogView.findViewById(R.id.tvCoords);

        final String[] localPath = new String[] { null };
        final float[] picked = new float[] {-1f, -1f};
        if (existing != null) {
            etName.setText(existing.getName());
            etPrice.setText(String.valueOf(existing.getPrice()));
            String existingImg = existing.getImageUrl();
            if (!TextUtils.isEmpty(existingImg)) {
                if (existingImg.startsWith("/")) {
                    localPath[0] = existingImg;
                    Glide.with(ivPreview).load(new File(existingImg)).into(ivPreview);
                } else {
                    Glide.with(ivPreview).load(existingImg).into(ivPreview);
                }
            }
            picked[0] = existing.getMapX();
            picked[1] = existing.getMapY();
            if (picked[0] >= 0 && picked[1] >= 0) {
                tvCoords.setText(String.format("x=%.2f, y=%.2f", picked[0], picked[1]));
            }
        }

        dialogIvPreview = ivPreview;
        pickedImagePathHolder = localPath;

        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        miniMap.post(() -> drawMiniMap(miniMap, picked));

        miniMap.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int w = v.getWidth();
                int h = v.getHeight();
                if (w > 0 && h > 0) {
                    float px = clamp(event.getX() / w, 0f, 1f);
                    float py = clamp(event.getY() / h, 0f, 1f);
                    if (shelfAt(px, py) != null) {
                        picked[0] = px;
                        picked[1] = py;
                        tvCoords.setText(String.format("x=%.2f, y=%.2f", picked[0], picked[1]));
                        drawMiniMap(miniMap, picked);
                    } else {
                        Toast.makeText(this, R.string.err_pick_inside_shelf, Toast.LENGTH_SHORT).show();
                    }
                }
                v.performClick();
                return true;
            }
            return false;
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.title_add_product : R.string.title_edit_product)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                        Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int price;
                    try {
                        price = Integer.parseInt(priceStr);
                        if (price < 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.err_invalid_price, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (picked[0] < 0 || picked[1] < 0 || shelfAt(picked[0], picked[1]) == null) {
                        Toast.makeText(this, R.string.err_pick_inside_shelf, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String imageRef = localPath[0] != null ? localPath[0] :
                            (existing != null ? existing.getImageUrl() : "");
                    Product toSave;
                    if (existing != null) {
                        existing.setName(name);
                        existing.setPrice(price);
                        existing.setImageUrl(imageRef);
                        existing.setMapX(picked[0]);
                        existing.setMapY(picked[1]);
                        toSave = existing;
                    } else {
                        toSave = new Product(productRepo.newId(), name, price, imageRef, picked[0], picked[1]);
                    }
                    productRepo.upsert(toSave);
                    refresh();
                    dialog.dismiss();
                }));

        dialog.setOnDismissListener(d -> {
            dialogIvPreview = null;
            pickedImagePathHolder = null;
        });

        dialog.show();
    }

    private void drawMiniMap(FrameLayout miniMap, float[] picked) {
        miniMap.removeAllViews();
        int w = miniMap.getWidth();
        int h = miniMap.getHeight();
        if (w == 0 || h == 0) return;

        addMiniBox(miniMap, w, h, ENTRANCE_X, ENTRANCE_Y, ENTRANCE_W, ENTRANCE_H,
                R.drawable.entrance_bg, getString(R.string.label_entrance));
        addMiniBox(miniMap, w, h, CASHIER_X, CASHIER_Y, CASHIER_W, CASHIER_H,
                R.drawable.cashier_bg, getString(R.string.label_cashier));

        for (int i = 0; i < shelves.size(); i++) {
            Shelf s = shelves.get(i);
            addMiniBox(miniMap, w, h, s.getX(), s.getY(), s.getW(), s.getH(),
                    R.drawable.shelf_bg, getString(R.string.shelf_label, i + 1));
        }

        if (picked[0] >= 0 && picked[1] >= 0) {
            View marker = new View(this);
            int size = (int) (20 * getResources().getDisplayMetrics().density);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
            lp.leftMargin = (int) (picked[0] * w) - size / 2;
            lp.topMargin = (int) (picked[1] * h) - size / 2;
            marker.setLayoutParams(lp);
            marker.setBackgroundResource(R.drawable.marker);
            miniMap.addView(marker);
        }
    }

    private void addMiniBox(FrameLayout container, int w, int h, float x, float y,
                            float rw, float rh, int bgRes, String label) {
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
        tv.setTextSize(10);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        tlp.gravity = Gravity.CENTER;
        tv.setLayoutParams(tlp);
        box.addView(tv);
        container.addView(box);
    }
}
