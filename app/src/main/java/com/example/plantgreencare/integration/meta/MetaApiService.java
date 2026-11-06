package com.example.plantgreencare.integration.meta;

/**
 * Interface preparada para comunicação com a Meta Graph API.
 * Estruturada para receber chamadas de perfil e publicação quando o backend intermediário
 * ou SDK oficial for ativado no projeto.
 */
public interface MetaApiService {

    interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    void getUserProfile(String accessToken, ApiCallback<String> callback);
    void publishPlantPost(String pageId, String message, String accessToken, ApiCallback<Boolean> callback);
}
