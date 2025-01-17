package com.example.quantacup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quantacup.R;
import com.example.quantacup.adapter.PublishImageAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//底部导航栏中的 那个加号（发布帖子）
public class AddActivity extends AppCompatActivity {
    private PublishImageAdapter adapter;
    private List<Uri> imageUris = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView postTitle, postContent;
    private Button publish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        RecyclerView recyclerView = findViewById(R.id.image_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new PublishImageAdapter(imageUris);
        recyclerView.setAdapter(adapter);

        postContent = findViewById(R.id.post_content);
        postTitle = findViewById(R.id.post_title);
        publish = findViewById(R.id.publish);
        ImageView addImageButton = findViewById(R.id.add_image);


        // 请求读取外部存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
        }

        addImageButton.setOnClickListener(view -> openImageChooser());

        Toolbar toolbar = findViewById(R.id.z_btn);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

        toolbar.setNavigationOnClickListener(view -> finish());

        publish.setOnClickListener(v -> uploadImages()); // 设置上传按钮的点击事件

        // 添加 ItemTouchHelper 支持全方位拖拽
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // 交换图片的位置
                Collections.swap(imageUris, fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);

                // 检查是否到达 RecyclerView 的边缘
                if (toPosition == 0 || toPosition == adapter.getItemCount() - 1) {
                    showDeleteConfirmationDialog(toPosition);
                }

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 这部分可以留空，暂时不处理滑动删除
            }

            @Override
            public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    // 在拖拽时改变 Item 的背景颜色（可选）
                    if (viewHolder != null) {
                        viewHolder.itemView.setAlpha(0.5f);
                    }
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setAlpha(1.0f);
            }
        });

        // 将 ItemTouchHelper 附加到 RecyclerView
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("删除图片")
                .setMessage("您确定要删除这张图片吗？")
                .setPositiveButton("是", (dialog, which) -> {
                    imageUris.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton("否", null)
                .show();
    }


    // 打开图片选择器
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 允许多选
        startActivityForResult(Intent.createChooser(intent, "选择图片"), PICK_IMAGE_REQUEST);
    }

    // 处理选择的图片
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        if (imageUris.size() < 9) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            imageUris.add(imageUri);
                        }
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    imageUris.add(imageUri);
                }
                adapter.notifyDataSetChanged(); // 更新适配器以显示选择的图片
            }
        }
    }

    // 上传图片
    private void uploadImages() {
        String userId = getUserId(); // 获取 userId
        Log.e("TAG", "uploadImages: " + userId );
        String title = postTitle.getText().toString();
        String content = postContent.getText().toString();

        // 检查输入
        if (imageUris.isEmpty() || title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "请填写所有内容并选择图片", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", String.valueOf(userId))
                .addFormDataPart("title", title)
                .addFormDataPart("content", content);

        // 添加图片到请求
        for (Uri uri : imageUris) {
            File file = new File(getRealPathFromURI(uri)); // 根据 Uri 获取文件路径
            builder.addFormDataPart("images", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
        }

        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url("http://8.134.144.65:8082/cofood/community/Note")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("TAG", "onFailure: " + e.getMessage()  );
                runOnUiThread(() -> Toast.makeText(AddActivity.this, "上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(AddActivity.this, "上传成功", Toast.LENGTH_SHORT).show());
                } else {
                    Log.e("TAG", "onResponse: " + response );
                    runOnUiThread(() -> Toast.makeText(AddActivity.this, "上传失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // 获取文件真实路径的方法
    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return uri.getPath(); // 备用返回路径
    }

    // 获取用户 ID，您需要根据自己的逻辑来实现这个方法
    private String getUserId() {
        // 这里可以从 SharedPreferences 或者其他地方获取 userId
        SharedPreferences sharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
        return sharedPreferences.getString("user_id", null); // 默认值为 -1
    }
}
