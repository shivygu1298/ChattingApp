package com.example.shivamgupta.chatapp;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.shivamgupta.chatapp.fragments.ChatFragment;
import com.example.shivamgupta.chatapp.fragments.FriendsFragment;
import com.example.shivamgupta.chatapp.fragments.RequestFragment;

/**
 * Created by Shivam Gupta on 21-03-2018.
 */

class SectionPagerAdapter extends FragmentPagerAdapter{


    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        switch (position){
            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
            return "REQUESTS";

            case 1:
                return "CHATS";

            case 2:
                return "FRIENDS";

            default: return null;
        }
    }
}
