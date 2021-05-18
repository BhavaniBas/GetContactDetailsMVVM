package com.example.contactutils;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactutils.databinding.ContactListItemBinding;

import java.util.ArrayList;
import java.util.List;

public class ContactDataAdapter extends RecyclerView.Adapter<ContactDataAdapter.ContactViewHolder> {

    private ArrayList<Contact> contactArrayList;
    private SparseBooleanArray selectedItems;
    private int selectedIndex = -1;
    private OnItemClick itemClick;

    public ContactDataAdapter() {
        selectedItems = new SparseBooleanArray();
    }

    void setItemClick(OnItemClick itemClick) {
        this.itemClick = itemClick;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactListItemBinding contactListItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.contact_list_item, parent, false);
        return new ContactViewHolder(contactListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, final int position) {
        Contact currentContact = contactArrayList.get(position);
        holder.contactListItemBinding.setContact(currentContact);

        //Changes the activated state of this view.
        holder.contactListItemBinding.lytParent.setActivated(selectedItems.get(position, false));

        holder.contactListItemBinding.lytParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClick == null) return;
                itemClick.onItemClick(view, contactArrayList.get(position), position);
            }
        });
        holder.contactListItemBinding.lytParent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (itemClick == null) {
                    return false;
                } else {
                    itemClick.onLongPress(view, contactArrayList.get(position), position);
                    return true;
                }
            }
        });
        toggleIcon(holder.contactListItemBinding, position);
    }

    /*
      This method will trigger when we we long press the item and it will change the icon of the item to check icon.
    */
    private void toggleIcon(ContactListItemBinding bi, int position) {
        if (selectedItems.get(position, false)) {
            bi.lytImage.setVisibility(View.GONE);
            bi.lytChecked.setVisibility(View.VISIBLE);
            if (selectedIndex == position) selectedIndex = -1;
        } else {
            bi.lytImage.setVisibility(View.VISIBLE);
            bi.lytChecked.setVisibility(View.GONE);
            if (selectedIndex == position) selectedIndex = -1;
        }
    }

    @Override
    public int getItemCount() {
        if (contactArrayList != null) {
            return contactArrayList.size();
        } else {
            return 0;
        }
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        private ContactListItemBinding contactListItemBinding;

        ContactViewHolder(@NonNull ContactListItemBinding contactListItemBinding) {
            super(contactListItemBinding.getRoot());
            this.contactListItemBinding = contactListItemBinding;
        }
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    int selectedItemCount() {
        return selectedItems.size();
    }

    void setContactList(ArrayList<Contact> contacts) {
        this.contactArrayList = contacts;
        notifyDataSetChanged();
    }

    void toggleSelection(int position) {
        selectedIndex = position;
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public interface OnItemClick {

        void onItemClick(View view, Contact inbox, int position);

        void onLongPress(View view, Contact inbox, int position);
    }
}
