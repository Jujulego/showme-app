package net.capellari.showme;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by julien on 11/01/18.
 *
 * Gestion des requetes HTTPS faites au serveur
 */

public class RequestManager {
    // Attributs
    private static RequestManager m_instance; // Singleton !
    private static Context m_context;

    private RequestQueue m_requestQueue;
    private ImageLoader m_imageLoader;

    // Constructeur
    private RequestManager(Context context) {
        m_context = context;
        getRequestQueue();

        // Setup ImageLoader
        m_imageLoader = new ImageLoader(m_requestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String,Bitmap> m_cache = new LruCache<>(20);

            @Override
            public Bitmap getBitmap(String url) {
                return m_cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                m_cache.put(url, bitmap);
            }
        });
    }

    // Méthodes statiques
    public static synchronized RequestManager getInstance(Context context) {
        if (m_instance == null) {
            m_instance = new RequestManager(context);
        }

        return m_instance;
    }

    // Méthodes
    public RequestQueue getRequestQueue() {
        // Initialisation request queue
        if (m_requestQueue == null) {
            m_requestQueue = Volley.newRequestQueue(m_context.getApplicationContext());
        }

        return m_requestQueue;
    }

    public <T> void addRequest(Request<T> rq) {
        getRequestQueue().add(rq);
    }

    public ImageLoader getImageLoader() {
        return m_imageLoader;
    }
}
