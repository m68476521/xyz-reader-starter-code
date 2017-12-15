package com.example.xyzreader.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by mike on 12/15/17.
 */

public class ArticleDetailMainActivity extends SingleFragmentActivity {

    private static final String ARG_VALUE_ID = "value_id";
    public static final String ARG_ITEM = "image_url";
    public static final String ARG_IMAGE_TRANSITION_NAME = "image_transition_name";
    public static Intent newIntent(Context packageContext, long id, String sharedAnimation) {
        Intent intent = new Intent(packageContext, ArticleDetailMainActivity.class);
        intent.putExtra(ARG_VALUE_ID, id);
        intent.putExtra(ARG_IMAGE_TRANSITION_NAME, sharedAnimation);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
//        return new ArticleListFragment();
        long id = getIntent().getLongExtra(ARG_VALUE_ID, 0);
        String sharedPreferences= getIntent().getStringExtra(ARG_IMAGE_TRANSITION_NAME);
        return ArticleDetailActivity.newInstance(id, sharedPreferences);
    }
}