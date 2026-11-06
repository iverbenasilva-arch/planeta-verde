package com.example.plantgreencare.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.integration.meta.MetaRepository;

public class SocialViewModel extends AndroidViewModel {

    private final MetaRepository metaRepository;

    public SocialViewModel(@NonNull Application application) {
        super(application);
        this.metaRepository = new MetaRepository(application);
    }

    public LiveData<Boolean> getMetaLoginState() {
        return metaRepository.getLoginState();
    }

    public void shareToFacebook(PlantEntity plant, String message) {
        metaRepository.shareToFacebook(plant, message);
    }

    public void shareToInstagram(Uri imageUri, String caption) {
        metaRepository.shareToInstagram(imageUri, caption);
    }
}
