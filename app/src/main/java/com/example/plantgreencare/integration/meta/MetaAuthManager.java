package com.example.plantgreencare.integration.meta;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gerenciador modular de Autenticação Meta (Facebook Login).
 * Preparado para conexão oficial com Meta Graph API / Facebook Login SDK.
 * Nunca armazena App Secrets no aplicativo cliente.
 */
public class MetaAuthManager {

    private static final String PREF_NAME = "meta_auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ID = "user_id";

    // Configuração oficial (valores reais injetados via backend seguro ou BuildConfig em produção)
    public static final String META_APP_ID = "PLACEHOLDER_APP_ID";
    public static final String META_REDIRECT_URI = "https://example.com/auth/callback";

    private final SharedPreferences prefs;

    public MetaAuthManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Usuário Conectado");
    }

    public void setSession(String userId, String userName) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, userName)
                .apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
