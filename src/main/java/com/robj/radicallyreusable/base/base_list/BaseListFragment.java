package com.robj.radicallyreusable.base.base_list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.robj.radicallyreusable.R;
import com.robj.radicallyreusable.R2;
import com.robj.radicallyreusable.base.mvp.fragment.BaseMvpFragment;
import com.robj.radicallyreusable.base.mvp.fragment.BaseMvpPresenter;

import java.util.Collection;

import butterknife.BindView;

/**
 * Created by jj on 05/02/17.
 */

public abstract class BaseListFragment<V extends BaseListView<T>, P extends BaseMvpPresenter<V>,
        A extends BaseListRecyclerAdapter, T extends Object> extends BaseMvpFragment<V, P> implements BaseListView<T>, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R2.id.list)
    RecyclerView list;
    @BindView(R2.id.progress_container)
    View progressContainer;
    @BindView(R2.id.progress)
    ProgressBar progress;
    @BindView(R2.id.progress_label)
    TextView progressLabel;
    @BindView(R2.id.swipe_to_refresh)
    SwipeRefreshLayout swipeToRefresh;

    private A adapter;
    private OnScrollToLoadListener onScrollToLoadListener;
    private boolean hasMoreToLoad;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViewsIfIncluded(view);
        initList();
        initSwipeToRefresh();
    }

    /**
     * I think because the library can't find R2 id's from an included layout
     * @param view
     */
    private void findViewsIfIncluded(View view) {
        if(list != null)
            return;
        list = (RecyclerView) view.findViewById(R.id.list);
        progressContainer = view.findViewById(R.id.progress_container);
        progress = (ProgressBar) view.findViewById(R.id.progress);
        progressLabel = (TextView) view.findViewById(R.id.progress_label);
        swipeToRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_to_refresh);
    }

    private void initSwipeToRefresh() {
        swipeToRefresh.setOnRefreshListener(this);
    }

    private void initList() {
        adapter = createAdapter();
        final LinearLayoutManager layoutManager = getLayoutManager();
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(hasMoreToLoad && dy >= 0) { //Check for scroll down
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                    if((visibleItemCount + pastVisibleItems) >= totalItemCount && onScrollToLoadListener != null) {
                        hasMoreToLoad = false;
                        onScrollToLoadListener.onLoadMore();
                        Log.d(TAG, "Scroll to load more triggered..");
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    protected LinearLayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_base_list;
    }

    public void showProgress() {
        if(swipeToRefresh.isRefreshing() && !adapter.isEmpty()) //Show refresh spinner if content is already there
            return;
        progressContainer.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        progressLabel.setVisibility(View.VISIBLE);
        progressLabel.setText(getSearchString());
        swipeToRefresh.setRefreshing(false);
        swipeToRefresh.setEnabled(false);
    }

    protected abstract String getSearchString();

    public boolean isRefreshing() {
        return swipeToRefresh.isRefreshing();
    }

    @Override
    public void addResults(Collection<T> results) {
        if(isRefreshing()) {//To work requires getProgress() to always be called after this addResults
            adapter.clear();
            adapter.addOrReplaceAll(results);
        } else
            adapter.addAll(results);
    }

    public void hideProgress() {
        progressContainer.setVisibility(View.GONE);
        swipeToRefresh.setRefreshing(false);
        swipeToRefresh.setEnabled(true);
    }

    @Override
    public void addResult(T result) {
        if(isRefreshing()) //To work requires getProgress() to always be called after this addResults
            adapter.addOrReplace(result);
        else
            adapter.add(result);
    }

    @Override
    public void clear() {
        adapter.clear();
    }

    @Override
    public void showError(int errorResId) {
        showErrorMsg(errorResId);
    }

    public void showErrorMsg(int errorResId) {
        hideProgress();
        progressContainer.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        progressLabel.setVisibility(View.VISIBLE);
        progressLabel.setText(errorResId);
        swipeToRefresh.setRefreshing(false);
        swipeToRefresh.setEnabled(true);
    }

    protected A getAdapter() {
        return adapter;
    }

    protected abstract A createAdapter();

    public void hasMoreToLoad(boolean hasMoreToLoad) {
        this.hasMoreToLoad = hasMoreToLoad;
        adapter.setHasMoreToLoad(hasMoreToLoad);
    }

    public void setOnScrollToLoadListener(OnScrollToLoadListener onScrollToLoadListener) {
        this.onScrollToLoadListener = onScrollToLoadListener;
    }

    protected RecyclerView getList() {
        return list;
    }

    public void removeOnScrollToLoadListener() {
        this.onScrollToLoadListener = null;
    }

    public interface OnScrollToLoadListener {
        void onLoadMore();
    }

}
