package com.alsfox.mall.view.fragment.base;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.alsfox.mall.R;
import com.alsfox.mall.adapter.BaseGridAdapter;
import com.alsfox.mall.adapter.BaseViewHolder;
import com.alsfox.mall.http.StatusCode;
import com.alsfox.mall.http.response.ResponseFinalAction;
import com.alsfox.mall.http.response.ResponseSuccessAction;
import com.alsfox.mall.presenter.base.BasePresenter;
import com.alsfox.mall.view.baseview.BaseGridview;
import com.alsfox.mall.view.baseview.FootView;
import com.alsfox.mall.view.baseview.HeaderView;
import com.alsfox.mall.view.baseview.MyGridView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Created by Luhao on 2016/8/22.
 * gridview在fragment中的基类
 */
public abstract class BaseGridFragment<T extends BasePresenter> extends BaseFragment<T> implements GridView.OnItemClickListener {

    private BaseGridview baseGridview;
    private MyGridView myGridView;
    protected List<Object> data = new ArrayList<>();
    protected BaseGridAdapter baseGridAdapter;
    protected boolean isRefresh = true, isLoadMore = true;
    protected int numColumns = 2;//默认gridview一行包含两个视图

    public BaseGridFragment() {
        super();
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    /**
     * 是否下拉刷新
     */
    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
        if (baseGridview != null) baseGridview.setRefresh(refresh);

    }

    public boolean isLoadMore() {
        return isLoadMore;
    }

    /**
     * 是否上拉加载
     */
    public void setLoadMore(boolean loadMore) {
        isLoadMore = loadMore;
        if (baseGridview != null) baseGridview.setLoadMore(loadMore);
        if (baseGridAdapter != null) baseGridAdapter.setFooterViewEnable(isLoadMore());
    }

    /**
     * 每一行item的数据
     *
     * @param position
     * @param baseViewHolder
     * @param itemType
     */
    public abstract void getItemData(int position, BaseViewHolder baseViewHolder, int itemType);

    /**
     * 返回一个提供重用的viewholder
     *
     * @param itemView
     * @param postion
     * @param itemType
     * @return
     */
    public abstract BaseViewHolder getViewHolder(View itemView, int postion, int itemType);

    /**
     * 返回一个item的视图
     *
     * @param position
     * @return
     */
    public abstract int getItemView(int position, int itemType);

    /**
     * 传入头view
     */
    protected int getHeaderView() {
        return 0;
    }

    /**
     * listview的头view是否可点击
     * 默认，false
     */
    protected boolean getIsHeaderViewClick() {
        return false;
    }

    //判断itemView类型,默认0
    public int getItemViewType(int position) {
        return 0;
    }

    // 种类+1。这里尤其要注意，必须+1.具体为什么我也不清楚
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    protected void initView(View parentView) {
        baseGridview = (BaseGridview) parentView.findViewById(R.id.default_grid_view);
        myGridView = baseGridview.myGridView;
        if (getHeaderView() > 0) {
            View view = View.inflate(getActivity(), getHeaderView(), null);
            if (view != null) {
                ViewGroup viewGroup = (ViewGroup) view.getRootView();
                viewGroup.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            }
        }
        baseGridAdapter = new BaseGridAdapter(data, baseGridview.footView, new OnAdapterStatus());
        myGridView.setAdapter(baseGridAdapter);
        myGridView.setOnItemClickListener(this);
        myGridView.setNumColumns(getNumColumns());
        baseGridview.setRefresh(isRefresh());
        baseGridview.setLoadMore(isLoadMore());
        baseGridAdapter.setFooterViewEnable(isLoadMore());
        defaultRefresh();
        defaultLoadMore();

    }

    private class OnAdapterStatus implements BaseGridAdapter.OnAdapterStatus {

        @Override
        public BaseViewHolder getViewHolders(View itemView, int position, int type) {
            return getViewHolder(itemView, position, type);
        }

        @Override
        public View getItemViews(int position, int type, ViewGroup parent) {
            return getActivity().getLayoutInflater().inflate(getItemView(position, type), null, false);
        }

        @Override
        public void getItemDatas(int position, BaseViewHolder baseViewHolder, int type) {
            getItemData(position, baseViewHolder, type);
        }

        @Override
        public int getItemViewTypes(int position) {
            return getItemViewType(position);
        }

        @Override
        public int getViewTypeCounts() {
            return getViewTypeCount();
        }
    }

    public int getNumColumns() {
        return numColumns;
    }

    /**
     * 重新设置gridview一行中view的个数
     *
     * @param numColumns
     */
    protected void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        if (myGridView != null) myGridView.setNumColumns(numColumns);
        notifyDataChange();
    }

    /**
     * 原来的点击事件做过处理
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == data.size() && isLoadMore()) return;//如果是最后一个item，则表明这个item是最后一个加载更多，则无法点击
        onGridViewItemClick(parent, view, position, id);
    }

    /**
     * 提供给外界调用的item点击事件
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    protected void onGridViewItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    protected void onRequestSuccess(ResponseSuccessAction success) {
        if (success.getRequestCode() == StatusCode.REQUEST_SUCCESS) {
            baseGridview.setOnExecuteScollSuccess();
            baseGridview.footView.onFootPrepare(baseGridview.isLoadComplete());
        }
        if (isRefresh())
            baseGridview.closeRefreshView();
    }

    @Override
    protected void onRequestFinal(ResponseFinalAction finals) {
        if (finals.getRequestCode() == StatusCode.NOT_MORE_DATA) {
            baseGridview.footView.onFootViewAll();
        }
        if (isRefresh()) {
            baseGridview.closeRefreshView();
        }
    }

    /**
     * 获得listview中的数据
     */
    protected List getData() {
        return data;
    }


    /**
     * 加载更多的事件，子类可以重写
     */
    public void loadMore() {

    }

    /**
     * 刷新数据
     */
    protected void notifyDataChange() {
        baseGridAdapter.notifyDataSetChanged();
    }

    /**
     * 添加数据
     */
    protected void addAll(Collection datas) {
        if (datas == null) return;
        data.addAll(datas);
    }

    /**
     * 删除数据
     */
    protected void clearAll() {
        if (data == null) return;
        data.clear();
    }

    /**
     * 下拉刷新监听事件
     */
    private void defaultRefresh() {
        baseGridview.setRefreshListener(new HeaderView.RefreshListener() {
            @Override
            public void onRefreshPrepare(boolean bl, PtrFrameLayout frame) {
                //准备刷新
            }

            @Override
            public void onRefreshBegin(boolean bl, PtrFrameLayout frame) {
                //刷新中
                if (isRefresh())
                    refresh();
            }

            @Override
            public void onRefreshComplete(boolean bl, PtrFrameLayout frame) {
                //刷新完成
            }
        });
    }

    /**
     * 上拉加载监听
     */
    private void defaultLoadMore() {
        baseGridview.setLoadMoreListener(new FootView.LoadMoreListener() {
            @Override
            public void onLoadMorePrepare(boolean bl) {
                //准备加载
            }

            @Override
            public void onLoadMoreBegin(boolean bl) {
                //开始加载
                if (isLoadMore())
                    loadMore();
            }

            @Override
            public void onLoadMoreComplete(boolean bl) {
                //加载完全部数据

            }
        });
    }

    protected void loadSuccess() {
        notifyDataChange();
        closeLoading();
        if (isRefresh())
            baseGridview.closeRefreshView();
    }

    private class finalOnclick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            refresh();
        }
    }

    protected void loadTextFinal(String errorStr) {
        showErrorLoadingByDefaultClick(errorStr);
        if (isRefresh())
            baseGridview.closeRefreshView();
    }


    protected void loadTextFinal(String errorStr, View.OnClickListener onClickListener) {
        showErrorLoading(errorStr, onClickListener);
        if (isRefresh())
            baseGridview.closeRefreshView();
    }

    protected void loadBtnFinal(String errorStr, String btnStr) {
        showErrorBtnLoadingByDefaultClick(errorStr, btnStr);
        if (isRefresh())
            baseGridview.closeRefreshView();
    }

    protected void loadBtnFinal(String errorStr, String btnStr, View.OnClickListener onClickListener) {
        showErrorBtnLoading(errorStr, btnStr, onClickListener);
        if (isRefresh())
            baseGridview.closeRefreshView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        baseGridAdapter.closeAdapter();
    }

}
