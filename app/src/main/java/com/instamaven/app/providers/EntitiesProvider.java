package com.instamaven.app.providers;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.instamaven.app.adapters.ListAdapter;
import com.instamaven.app.models.Entity;
import com.instamaven.app.utils.RequestClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public abstract class EntitiesProvider {

    private static final int SHOW_FOOTER_SPINNER = 0;
    private static final int ADD_DATA_TO_ADAPTER = 1;
    private static final int REPLACE_DATA_IN_ADAPTER = 2;
    private static final int HIDE_FOOTER_SPINNER = 3;
    private static final int SHOW_HEADER_SPINNER = 10;
    private static final int PREPEND_DATA_TO_ADAPTER = 11;
    private static final int HIDE_HEADER_SPINNER = 12;

    private static final int NO_NEED_DATA = 0;
    private static final int NEED_NEXT_DATA = 1;
    private static final int NEED_PREV_DATA = 2;
    private static final int NEED_NEXT_PAGE = 3;
    private static final int NEED_PREV_PAGE = 4;

    protected RequestClient.Builder client;
    protected View listView;
    protected Integer currentPage = 1;
    protected Integer lastPage = 1;

    private Handler mHandler;
    private boolean isLoading = false;
    private int whatDataNeeded = NO_NEED_DATA;

    EntitiesProvider(View view) {
        listView = view;
        mHandler = new MessageHandler();
    }

    public class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            View spinner = getSpinnerView();
            switch (msg.what) {
                case SHOW_FOOTER_SPINNER:
                    // Add loading view during search processing
//                    if (spinner != null) {
//                        listView.addFooterView(spinner);
//                    }
                    break;
                case ADD_DATA_TO_ADAPTER:
                    // Update data adapter and UI
//                    getAdapter().addListItemToAdapter((ArrayList<Entity>) msg.obj);
                    addListItemToAdapter((ArrayList<Entity>) msg.obj);
                case HIDE_FOOTER_SPINNER:
                    // Remove loading view after update listView
//                    if (spinner != null) {
//                        listView.removeFooterView(spinner);
//                    }
                    isLoading = false;
                    whatDataNeeded = NO_NEED_DATA;
                    break;
                case SHOW_HEADER_SPINNER:
                    // Add loading view during search processing
//                    if (spinner != null) {
//                        listView.addHeaderView(spinner);
//                    }
                    break;
                case PREPEND_DATA_TO_ADAPTER:
                    // Update data adapter and UI
                    //getAdapter().prependListItemToAdapter((ArrayList<Entity>) msg.obj);
                    prependListItemToAdapter((ArrayList<Entity>) msg.obj);
                case HIDE_HEADER_SPINNER:
                    // Remove loading view after update listView
//                    if (spinner != null) {
//                        listView.removeHeaderView(spinner);
//                    }
                    isLoading = false;
                    whatDataNeeded = NO_NEED_DATA;
                    break;
                case REPLACE_DATA_IN_ADAPTER:
                    // Update data adapter
                    replaceListItemInAdapter((ArrayList<Entity>) msg.obj);
                    isLoading = false;
                    whatDataNeeded = NO_NEED_DATA;
                    break;
            }
        }
    }

    public class ThreadGetNextData extends Thread {
        @Override
        public void run() {
            // Add footer view after get data
            mHandler.sendEmptyMessage(SHOW_FOOTER_SPINNER);
            // Search next page
            ArrayList<Entity> lstResult = getData(++currentPage);
            if (lstResult != null) {
                // Send the result to Handle
                Message msg = mHandler.obtainMessage(ADD_DATA_TO_ADAPTER, lstResult);
                mHandler.sendMessage(msg);
            } else {
                mHandler.sendEmptyMessage(HIDE_FOOTER_SPINNER);
            }
        }
    }

    public class ThreadGetPrevData extends Thread {
        @Override
        public void run() {
            // Add header view
            mHandler.sendEmptyMessage(SHOW_HEADER_SPINNER);
            // Search prev page
            ArrayList<Entity> lstResult = getData(--currentPage);
            if (lstResult != null) {
                // Send the result to Handle
                Message msg = mHandler.obtainMessage(PREPEND_DATA_TO_ADAPTER, lstResult);
                mHandler.sendMessage(msg);
            } else {
                mHandler.sendEmptyMessage(HIDE_HEADER_SPINNER);
            }
        }
    }

    public class ThreadGetNextPage extends Thread {
        @Override
        public void run() {
            // Search next page
            ArrayList<Entity> lstResult = getData(++currentPage);
            if (lstResult != null) {
                // Send the result to Handle
                Message msg = mHandler.obtainMessage(REPLACE_DATA_IN_ADAPTER, lstResult);
                mHandler.sendMessage(msg);
            }
        }
    }

    public class ThreadGetPrevPage extends Thread {
        @Override
        public void run() {
            // Search prev page
            ArrayList<Entity> lstResult = getData(--currentPage);
            if (lstResult != null) {
                // Send the result to Handle
                Message msg = mHandler.obtainMessage(REPLACE_DATA_IN_ADAPTER, lstResult);
                mHandler.sendMessage(msg);
            }
        }
    }

    public void checkDataLoading() {
        Thread thread;
        switch (whatDataNeeded) {
            case NEED_NEXT_DATA:
                isLoading = true;
                thread = new ThreadGetNextData();
                thread.start();
                break;
            case NEED_PREV_DATA:
                isLoading = true;
                thread = new ThreadGetPrevData();
                thread.start();
                break;
            case NEED_NEXT_PAGE:
                isLoading = true;
                thread = new ThreadGetNextPage();
                thread.start();
                break;
            case NEED_PREV_PAGE:
                isLoading = true;
                thread = new ThreadGetPrevPage();
                thread.start();
                break;
        }
    }

    // Check when scroll to last item in listView
    public boolean checkPosition(AbsListView view, int totalItemCount) {
        if (view.getLastVisiblePosition() == totalItemCount - 1 && currentPage < lastPage && !isLoading) {
            whatDataNeeded = NEED_NEXT_DATA;
        } else if (view.getFirstVisiblePosition() == 0 && currentPage > 1 && !isLoading) {
            // get page from entity
            //int page = getAdapter().getItem(0).getPage();
            int page = getTopPage();
            // ideally position should be 0
            // int position = adapter.getItem(0).getPosition();
            if (page > 1) {
                currentPage = page;
                whatDataNeeded = NEED_PREV_DATA;
            }
        }

        // return TRUE if we reach the last record, FALSE otherwise
        return (view.getLastVisiblePosition() == totalItemCount - 1 && currentPage.equals(lastPage));
    }

    public void getNextPage() {
        if (currentPage < lastPage && !isLoading) {
            whatDataNeeded = NEED_NEXT_PAGE;
            checkDataLoading();
        }
    }

    public void getPrevPage() {
        if (currentPage > 1 && !isLoading) {
            int page = getTopPage();
            // ideally position should be 0
            // int position = adapter.getItem(0).getPosition();
            if (page > 1) {
                currentPage = page;
                whatDataNeeded = NEED_PREV_DATA;
            }
            checkDataLoading();
        }
    }

    /**
     * Convert JSONArray (response from API server) into Entity List
     *
     * @param classCls Entity class
     * @param jsonObjects array of JSON objects
     * @param page current page number
     * @return ArrayList<Entity>
     */
    protected ArrayList<Entity> getEntityList(Class classCls, JSONArray jsonObjects, int page) {
        ArrayList<Entity> list = new ArrayList<>();
        Constructor constructor;
        try {
            Class cls = Class.forName(classCls.getName());
            constructor = cls.getConstructor(
                    new Class[] {
                            JSONObject.class,
                            Integer.class,
                            Integer.class
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                Entity entity = (Entity) constructor.newInstance(new Object[] {
                        jsonObjects.getJSONObject(i),
                        page,
                        i
                });
                list.add(entity);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        return list;
    }

    interface ListProvider {
        void setAdapter(ListAdapter adapter);

        ListAdapter getAdapter();

        ArrayList<Entity> getData(Integer page);

        View getSpinnerView();
    }

    void addListItemToAdapter(ArrayList<Entity> list) {
        getAdapter().addListItemToAdapter(list);
    }

    void prependListItemToAdapter(ArrayList<Entity> list) {
        getAdapter().prependListItemToAdapter(list);
    }

    void replaceListItemInAdapter(ArrayList<Entity> list) {
        getAdapter().replaceListItemInAdapter(list);
    }

    int getTopPage() {
        return getAdapter().getItem(0).getPage();
    }

    abstract ListAdapter getAdapter();

    abstract ArrayList<Entity> getData(Integer page);

    abstract View getSpinnerView();

}
