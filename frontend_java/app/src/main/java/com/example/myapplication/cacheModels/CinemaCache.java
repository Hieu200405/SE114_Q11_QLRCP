package com.example.myapplication.cacheModels;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.models.Cinema;
import com.example.myapplication.network.ApiCinemaService;
import com.example.myapplication.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Singleton cache for Cinema data to avoid multiple API calls.
 * Cinema data is relatively static (< 10 cinemas) so we cache it once
 * and refresh only when add/update/delete operations occur.
 */
public class CinemaCache {
    private static final String TAG = "CinemaCache";

    private static List<Cinema> cachedCinemas;
    private static boolean isLoading = false;
    private static final List<CinemaLoadListener> pendingListeners = new ArrayList<>();

    /**
     * Interface for cinema load callbacks
     */
    public interface CinemaLoadListener {
        void onCinemasLoaded(List<Cinema> cinemas);
        void onLoadError(String error);
    }

    /**
     * Get cached cinemas list
     * @return cached list or null if not cached
     */
    public static List<Cinema> getCachedCinemas() {
        return cachedCinemas;
    }

    /**
     * Set cached cinemas list
     * @param cinemas list of cinemas to cache
     */
    public static void setCachedCinemas(List<Cinema> cinemas) {
        cachedCinemas = cinemas;
        Log.d(TAG, "Cinema cache updated with " + (cinemas != null ? cinemas.size() : 0) + " cinemas");
    }

    /**
     * Check if cinemas are cached
     * @return true if cached and not empty
     */
    public static boolean isCached() {
        return cachedCinemas != null && !cachedCinemas.isEmpty();
    }

    /**
     * Clear the cache - call this when cinemas are added, updated, or deleted
     */
    public static void clearCache() {
        cachedCinemas = null;
        Log.d(TAG, "Cinema cache cleared");
    }

    /**
     * Invalidate and reload cache from API
     * @param listener callback for when load completes
     */
    public static void refreshCache(CinemaLoadListener listener) {
        clearCache();
        loadCinemas(listener);
    }

    /**
     * Get cinemas - returns from cache if available, otherwise loads from API
     * @param listener callback for when cinemas are available
     */
    public static void loadCinemas(CinemaLoadListener listener) {
        // Return cached data immediately if available
        if (isCached()) {
            Log.d(TAG, "Returning cached cinemas (" + cachedCinemas.size() + " items)");
            if (listener != null) {
                listener.onCinemasLoaded(new ArrayList<>(cachedCinemas));
            }
            return;
        }

        // Add listener to pending list if already loading
        if (isLoading) {
            Log.d(TAG, "Already loading cinemas, adding listener to queue");
            if (listener != null) {
                synchronized (pendingListeners) {
                    pendingListeners.add(listener);
                }
            }
            return;
        }

        // Start loading from API
        isLoading = true;
        if (listener != null) {
            synchronized (pendingListeners) {
                pendingListeners.add(listener);
            }
        }

        Log.d(TAG, "Loading cinemas from API...");
        ApiCinemaService apiCinemaService = ApiClient.getRetrofit().create(ApiCinemaService.class);
        apiCinemaService.getAllCinemas().enqueue(new Callback<List<Cinema>>() {
            @Override
            public void onResponse(@NonNull Call<List<Cinema>> call, @NonNull Response<List<Cinema>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    cachedCinemas = new ArrayList<>(response.body());
                    Log.d(TAG, "Cinemas loaded and cached: " + cachedCinemas.size() + " items");

                    // Notify all pending listeners
                    notifyListenersSuccess(new ArrayList<>(cachedCinemas));
                } else {
                    Log.e(TAG, "Failed to load cinemas: " + response.code());
                    notifyListenersError("Lỗi tải danh sách rạp: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Cinema>> call, @NonNull Throwable t) {
                isLoading = false;
                Log.e(TAG, "Error loading cinemas: " + t.getMessage());
                notifyListenersError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Notify all pending listeners of successful load
     */
    private static void notifyListenersSuccess(List<Cinema> cinemas) {
        synchronized (pendingListeners) {
            for (CinemaLoadListener listener : pendingListeners) {
                if (listener != null) {
                    listener.onCinemasLoaded(cinemas);
                }
            }
            pendingListeners.clear();
        }
    }

    /**
     * Notify all pending listeners of load error
     */
    private static void notifyListenersError(String error) {
        synchronized (pendingListeners) {
            for (CinemaLoadListener listener : pendingListeners) {
                if (listener != null) {
                    listener.onLoadError(error);
                }
            }
            pendingListeners.clear();
        }
    }

    /**
     * Find cinema by ID from cache
     * @param cinemaId ID to search for
     * @return Cinema object or null if not found
     */
    public static Cinema getCinemaById(int cinemaId) {
        if (cachedCinemas == null) {
            return null;
        }
        for (Cinema cinema : cachedCinemas) {
            if (cinema.getId() == cinemaId) {
                return cinema;
            }
        }
        return null;
    }

    /**
     * Get cinema name by ID
     * @param cinemaId ID to search for
     * @return Cinema name or "Unknown" if not found
     */
    public static String getCinemaNameById(int cinemaId) {
        Cinema cinema = getCinemaById(cinemaId);
        return cinema != null ? cinema.getName() : "Unknown";
    }

    /**
     * Add a new cinema to cache
     * @param cinema Cinema to add
     */
    public static void addCinemaToCache(Cinema cinema) {
        if (cachedCinemas == null) {
            cachedCinemas = new ArrayList<>();
        }
        cachedCinemas.add(cinema);
        Log.d(TAG, "Added cinema to cache: " + cinema.getName());
    }

    /**
     * Update a cinema in cache
     * @param updatedCinema Cinema with updated data
     */
    public static void updateCinemaInCache(Cinema updatedCinema) {
        if (cachedCinemas == null) return;

        for (int i = 0; i < cachedCinemas.size(); i++) {
            if (cachedCinemas.get(i).getId() == updatedCinema.getId()) {
                cachedCinemas.set(i, updatedCinema);
                Log.d(TAG, "Updated cinema in cache: " + updatedCinema.getName());
                return;
            }
        }
    }

    /**
     * Remove a cinema from cache by ID
     * @param cinemaId ID of cinema to remove
     */
    public static void removeCinemaFromCache(int cinemaId) {
        if (cachedCinemas == null) return;

        for (int i = 0; i < cachedCinemas.size(); i++) {
            if (cachedCinemas.get(i).getId() == cinemaId) {
                Cinema removed = cachedCinemas.remove(i);
                Log.d(TAG, "Removed cinema from cache: " + removed.getName());
                return;
            }
        }
    }
}

