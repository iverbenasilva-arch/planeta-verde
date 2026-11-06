package com.example.plantgreencare.integration.meta;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.plantgreencare.data.entities.PlantEntity;

public class MetaRepository {

    private final MetaAuthManager authManager;
    private final MetaShareManager shareManager;
    private final MutableLiveData<Boolean> loginState = new MutableLiveData<>();

    public MetaRepository(Context context) {
        this.authManager = new MetaAuthManager(context);
        this.shareManager = new MetaShareManager(context);
        this.loginState.setValue(authManager.isLoggedIn());
    }

    public LiveData<Boolean> getLoginState() {
        return loginState;
    }

    public void shareToFacebook(PlantEntity plant, String shareText) {
        shareManager.shareToFacebook(plant, shareText);
    }

    public void shareToInstagram(Uri imageUri, String caption) {
        shareManager.shareToInstagram(imageUri, caption);
    }

    public void logout() {
        authManager.logout();
        loginState.setValue(false);
    }
}
