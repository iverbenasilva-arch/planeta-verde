package com.example.plantgreencare.integration.meta;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.example.plantgreencare.data.entities.PlantEntity;

/**
 * Gerenciador de compartilhamento oficial para Facebook e Instagram (Meta).
 * Utiliza as rotas oficiais de Intents suportadas pelo sistema e pelos aplicativos da Meta,
 * garantindo compatibilidade sem uso de APIs privadas ou automações não autorizadas.
 */
public class MetaShareManager {

    private final Context context;

    public MetaShareManager(Context context) {
        this.context = context;
    }

    /**
     * Compartilha no Facebook através do aplicativo oficial ou via Web Share Dialog da Meta.
     */
    public void shareToFacebook(PlantEntity plant, String shareText) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        intent.setPackage("com.facebook.katana");

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Se o app do Facebook não estiver instalado, direciona para o navegador seguro
            String url = "https://www.facebook.com/sharer/sharer.php?quote=" + Uri.encode(shareText);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                context.startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(context, "Não foi possível abrir o Facebook.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Compartilha no Instagram Stories ou Feed usando o mecanismo oficial de Intent do Instagram.
     */
    public void shareToInstagram(Uri imageUri, String caption) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        if (imageUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.putExtra(Intent.EXTRA_TEXT, caption);
        intent.setPackage("com.instagram.android");

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Aplicativo do Instagram não instalado. Use o compartilhamento geral.", Toast.LENGTH_SHORT).show();
        }
    }
}
