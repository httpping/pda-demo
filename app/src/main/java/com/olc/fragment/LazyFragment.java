package com.olc.fragment;

import android.support.v4.app.Fragment;

public abstract class LazyFragment extends Fragment {
    protected boolean isVisible;

    /**
     * Here the slow loading of Fragment data is implemented.
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {//Perform operations when visible
            isVisible = true;
            onVisible();
        } else {//Perform the corresponding action when it is not visible
            isVisible = false;
            onInvisible();
        }
    }

    protected void onVisible() {
        lazyLoad();
    }

    protected abstract void lazyLoad();

    protected void onInvisible() {
    }

    protected void doDiscon(){
    }
}
