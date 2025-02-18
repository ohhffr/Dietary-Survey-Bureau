package com.example.quantacup.help;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.quantacup.R;

import java.util.ArrayList;
import java.util.List;


public class Carousel {

    private Context mContext;   //全局的Context用于加载图片
    private LinearLayout dotLinearLayout;   //用于加载标志点的LinearLayout
    private List<ImageView> mDotViewList = new ArrayList<>();//点视图集合 每个ImageView表示一个点

    private Handler autoScrollHandler;  //控制自动轮播的线程

    private List<Integer> originalImages = new ArrayList<>();   //存放这需要轮播的图片

    private ViewPager2 viewPager2;

    private long AUTO_SCROLL_INTERVAL = 1_500; // 设置自动滚动的间隔时间，单位为毫秒

    private boolean AUTO_SCROLL = false;    //是否设置自动播放


    public Carousel(Context mContext, LinearLayout dotLinearLayout, ViewPager2 viewPager2) {
        this.mContext = mContext;
        this.dotLinearLayout = dotLinearLayout;
        this.viewPager2 = viewPager2;

        autoScrollHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 用于启动轮播图效果
     *
     * @param resource 图片的资源ID
     */
    public void initViews(int[] resource) {

        //加载初始化绑定轮播图
        for (int id : resource) {
            originalImages.add(id);

            //制作标志点的ImageView，并且初始化加载第一张图片标志点
            ImageView dotImageView = new ImageView(mContext);
            if (originalImages.size() == 1) {
                dotImageView.setImageResource(R.drawable.red_dot);
            } else {
                dotImageView.setImageResource(R.drawable.grey_dot);
            }

            //设置标志点的布局参数
            LinearLayout.LayoutParams dotImageLayoutParams = new LinearLayout.LayoutParams(60, 60);
            dotImageLayoutParams.setMargins(5, 0, 5, 0);

            //将布局参数绑定到标志点视图
            dotImageView.setLayoutParams(dotImageLayoutParams);

            //保存标志点便于后续动态修改
            mDotViewList.add(dotImageView);

            //将标志点的视图绑定在Layout中
            dotLinearLayout.addView(dotImageView);
        }

        originalImages.add(0, originalImages.get(originalImages.size() - 1));  //将originalImages的最后一张照片插入到开头
        originalImages.add(originalImages.get(1));  //将originalImages的第2张照片插入到结尾

        ImageAdapter adapter = new ImageAdapter(originalImages);
        viewPager2.setAdapter(adapter);

        // 设置当前项为数据集的第一个元素，使其显示为轮播图的开始
        viewPager2.setCurrentItem(1, false);

        // 添加页面更改监听器，以实现循环滚动
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

                // 当滑动开始时停止自动滚动
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    autoScrollHandler.removeCallbacks(autoScrollRunnable);
                }
                // 当滑动结束时重新启动自动滚动
                else if (state == ViewPager2.SCROLL_STATE_IDLE && AUTO_SCROLL) {
                    autoScrollHandler.removeCallbacks(autoScrollRunnable); // 移除之前的回调,防止多次启动的情况
                    autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL);
                }
            }

            @Override
            public void onPageSelected(int position) {

                //动态设置图片的下标点位
                for (int i = 0; i < mDotViewList.size(); i++) {
                    //由于在第一张图片前添加一张过渡图，因此position比mDotViewList对于的标志点多一位
                    if (i == position - 1) {
                        mDotViewList.get(i).setImageResource(R.drawable.red_dot);
                    } else {
                        mDotViewList.get(i).setImageResource(R.drawable.grey_dot);
                    }
                }

                // 在滑动到最后一个元素时，跳转到第一个元素
                if (position == originalImages.size() - 1) {
                    viewPager2.setCurrentItem(1, false);
                }
                // 在滑动到第一个元素时，跳转到最后一个元素
                else if (position == 0) {
                    viewPager2.setCurrentItem(originalImages.size() - 2, false);
                }
            }
        });

    }

    /**
     * 启动自动滚动
     */
    public void startAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable); // 移除之前的回调,防止多次启动的情况
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL);
        AUTO_SCROLL = true;
    }

    /**
     * 停止自动滚动
     */
    public void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        AUTO_SCROLL = false;
    }

    // 定义自动滚动的 Runnable
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            // 在这里处理自动滚动逻辑
            int currentItem = viewPager2.getCurrentItem();

            //当自动轮播到最后一张图片时，又从头开始轮播
            if (currentItem == originalImages.size() - 2) {
                viewPager2.setCurrentItem(1);
            } else {
                viewPager2.setCurrentItem(currentItem + 1);
            }

            // 重新调度下一次自动滚动
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_INTERVAL);
        }
    };
}

class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;
    private List<Integer> images;

    public ImageAdapter(List<Integer> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        //使用Glide加载可以优化加载流程，Glide使用异步操作
        Glide.with(context).load(images.get(position)).into(holder.imageView);
//        holder.imageView.setImageResource(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}


