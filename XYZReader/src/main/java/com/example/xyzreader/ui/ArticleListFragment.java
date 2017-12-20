package com.example.xyzreader.ui;

import android.app.ActivityOptions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.databinding.ActivityArticleListBinding;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by mike on 12/12/17.
 */

public class ArticleListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM = "image_url";
    private static final String ARG_VALUE_ID = "value_id";
    public static final String ARG_IMAGE_TRANSITION_NAME = "image_transition_name";
    public static final String ARG_IMAGE_TRANSITION_NAME_IMAGE = "image_transition_name_image";


    private static final String TAG = ArticleListFragment.class.toString();

    private ActivityArticleListBinding Binding;
    private Context mContext;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        Log.d("MIKE", "OnCreateFragment");
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Binding = DataBindingUtil.inflate(inflater, R.layout.activity_article_list, container, false);

        Binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        if (savedInstanceState == null) {
            refresh();
        }

        Binding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    Binding.collapsingToolbarLayout.setTitle("xyzreader");
                    isShow = true;
                } else if (isShow) {
                    Binding.collapsingToolbarLayout.setTitle("xyzreader");
                }
            }
        });

        return Binding.getRoot();
    }

    private void refresh() {

        getActivity().startService(new Intent(getActivity(), UpdaterService.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mRefreshingReceiver);
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        Binding.swipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newAllArticlesInstance(getContext());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        ArticleListFragment.Adapter adapter = new ArticleListFragment.Adapter(cursor);
        adapter.setHasStableIds(true);

        Binding.recyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        Binding.recyclerView.setLayoutManager(sglm);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Binding.recyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ArticleListFragment.ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);

            Log.d("MIKE 23A:", Long.toString(mCursor.getLong(ArticleLoader.Query._ID)));
            Log.d("MIKE 23B:", mCursor.getString(ArticleLoader.Query.TITLE));
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public ArticleListFragment.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ArticleListFragment.ViewHolder vh = new ArticleListFragment.ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO, testing this animation remove it
//                    int finalRadius = (int)Math.hypot(view.getWidth()/2, view.getHeight()/2);
//                    Animator anim = ViewAnimationUtils.createCircularReveal(view, (int) view.getWidth()/2,
//                            (int) view.getHeight()/2, 0, finalRadius);
//                    anim.start();
                    ////TODO END, testing this animation remove it

                    long value = getItemId(vh.getAdapterPosition());

                    ImageView imageView = view.findViewById(R.id.thumbnail);

                    Log.d("MIKECLICKB", Long.toString(value));
                    Log.d("MIKECLICKB", imageView.toString());
                }
            });
            return vh;
        }

        private Date parsePublishedDate() {
            try {
                String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
                return dateFormat.parse(date);
            } catch (ParseException ex) {
                Log.e(TAG, ex.getMessage());
                Log.i(TAG, "passing today's date");
                return new Date();
            }
        }

        @Override
        public void onBindViewHolder(final ArticleListFragment.ViewHolder holder, final int position) {
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {

                holder.subtitleView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + "<br/>" + " by "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            } else {
                holder.subtitleView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate)
                                + "<br/>" + " by "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            }

            ViewCompat.setTransitionName(holder.thumbnailView, mCursor.getString(ArticleLoader.Query._ID));

            Picasso.with(getActivity())
                    .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                    .error(R.drawable.empty_detail)
                    .into(holder.thumbnailView);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    animalItemClickListener.onAnimalItemClick(position, animalItem, holder.thumbnailView);
                    Log.d("MIKECLICK", Integer.toString(position));
                    Log.d("MIKECLICK", mCursor.getString(ArticleLoader.Query._ID));
                    Log.d("MIKECLICK", holder.thumbnailView.toString());
                    Log.d("MIKECLICK", mCursor.getString(ArticleLoader.Query.TITLE));
                    //test(Long.parseLong(mCursor.getString(ArticleLoader.Query._ID)), holder.thumbnailView);
                    mCursor.moveToPosition(position);

                    Log.d("MIKECLICK :::", mCursor.getString(ArticleLoader.Query._ID));
                    Log.d("MIKECLICK :::", mCursor.getString(ArticleLoader.Query.TITLE));
                    test2(Long.parseLong(mCursor.getString(ArticleLoader.Query._ID)), holder.thumbnailView);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
    }

    public void test(long value, ImageView sharedImageView) {

        Log.d("MIKE TEST:::", String.valueOf(sharedImageView));
        Log.d("MIKE TESTB:::", sharedImageView.toString());
        Log.d("MIKE TESTC:::", ViewCompat.getTransitionName(sharedImageView));
        Log.d("MIKE TESTD:::", Long.toString(value));
//        Intent intent = ArticleDetailActivity.newIntent(mContext, value, ViewCompat.getTransitionName(sharedImageView));

//        Intent intent = ArticleDetailMainActivity.newIntent(mContext, value, ViewCompat.getTransitionName(sharedImageView));
//        intent.putExtra(ARG_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));
//
//        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                getActivity(),
//                sharedImageView,
//                ViewCompat.getTransitionName(sharedImageView));
//
//        startActivity(intent, options.toBundle());


        Fragment articleDetailFragment = ArticleDetailFragment.newInstance(value, ViewCompat.getTransitionName(sharedImageView));
        getFragmentManager()
                .beginTransaction()
                .addSharedElement(sharedImageView, ViewCompat.getTransitionName(sharedImageView))
                .addToBackStack(TAG)
                .replace(R.id.fragment_container, articleDetailFragment)
                .commit();
    }


    public void test2(long value, ImageView sharedImageView) {
        Log.d("MIKE TEST2:::", String.valueOf(sharedImageView));
        Log.d("MIKE TEST2:::", sharedImageView.toString());
        Log.d("MIKE TEST2:::", ViewCompat.getTransitionName(sharedImageView));

        Fragment articleDetailFragment = ArticleDetailFragment.newInstance(value, ViewCompat.getTransitionName(sharedImageView));
        getFragmentManager()
                .beginTransaction()
                .addSharedElement(sharedImageView, ViewCompat.getTransitionName(sharedImageView))
                .addToBackStack(TAG)
                .replace(R.id.fragment_container, articleDetailFragment)
                .commit();
    }
}
