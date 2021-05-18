package com.example.contactutils;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class ContactViewModel extends ViewModel {

    public MutableLiveData<List<Contact>> mutableLiveData = new MutableLiveData<List<Contact>>();

    public void getMutableLiveContactData(List<Contact> contactArray) {

        if (contactArray != null && !contactArray.isEmpty()) {
            mutableLiveData.setValue(contactArray);
        }
    }
}
