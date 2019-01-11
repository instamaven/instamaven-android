package com.instamaven.app.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.instamaven.app.models.Entity;

import java.util.ArrayList;
import java.util.Observable;

public abstract class ListAdapter extends ArrayAdapter<Entity> {

    protected final Context context;
    protected final ArrayList<Entity> items;

    public ListAdapter(Context context, int res, ArrayList<Entity> items) {
        super(context, res, items);
        this.context = context;
        this.items = items;
    }

    public void prependListItemToAdapter(ArrayList<Entity> list) {
        this.items.addAll(0, list);
        this.notifyDataSetChanged();
    }

    public void addListItemToAdapter(ArrayList<Entity> list) {
        this.items.addAll(list);
        this.notifyDataSetChanged();
    }

    public void replaceListItemInAdapter(ArrayList<Entity> list) {
        this.items.clear();
        this.items.addAll(list);
        this.notifyDataSetChanged();
    }

    public ArrayList<Entity> getAll() {
        return this.items;
    }
}
