package com.example.myapplication.helper;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.cacheModels.CinemaCache;
import com.example.myapplication.models.BroadcastFilm;
import com.example.myapplication.models.Cinema;
import com.example.myapplication.models.RoomResponse;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiRoomService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper class to enrich BroadcastFilm objects with Cinema information
 * Loads Room data to get CinemaId, then gets Cinema info from cache
 */
public class BroadcastCinemaEnricher {
    private static final String TAG = "BroadcastCinemaEnricher";

    // STATIC GLOBAL CACHE để tránh race condition và tái sử dụng giữa các lần gọi
    private static final Map<Integer, RoomResponse> globalRoomCache = new HashMap<>();

    public interface EnrichmentCompleteListener {
        void onEnrichmentComplete(List<BroadcastFilm> enrichedBroadcasts);
        void onEnrichmentError(String error);
    }

    /**
     * Clear room cache - call when rooms are modified
     */
    public static void clearRoomCache() {
        synchronized (globalRoomCache) {
            globalRoomCache.clear();
        }
        Log.d(TAG, "Room cache cleared (" + globalRoomCache.size() + " items removed)");
    }

    /**
     * Enrich a list of broadcasts with cinema information
     * @param broadcasts List of broadcasts to enrich
     * @param listener Callback when enrichment is complete
     */
    public static void enrichBroadcastsWithCinemaInfo(
            List<BroadcastFilm> broadcasts,
            EnrichmentCompleteListener listener) {

        if (broadcasts == null || broadcasts.isEmpty()) {
            Log.d(TAG, "No broadcasts to enrich");
            if (listener != null) {
                listener.onEnrichmentComplete(broadcasts);
            }
            return;
        }

        Log.d(TAG, "Starting enrichment for " + broadcasts.size() + " broadcasts");

        // First ensure cinema cache is loaded
        CinemaCache.loadCinemas(new CinemaCache.CinemaLoadListener() {
            @Override
            public void onCinemasLoaded(List<Cinema> cinemas) {
                Log.d(TAG, "Cinema cache loaded with " + cinemas.size() + " cinemas");
                // Now load room information for each broadcast
                loadRoomInfoForBroadcasts(broadcasts, listener);
            }

            @Override
            public void onLoadError(String error) {
                Log.e(TAG, "Failed to load cinemas: " + error);
                if (listener != null) {
                    listener.onEnrichmentError(error);
                }
            }
        });
    }

    /**
     * Load room information for all broadcasts
     * FIX: Collect unique room IDs first, then load rooms one by one to avoid race condition
     */
    private static void loadRoomInfoForBroadcasts(
            List<BroadcastFilm> broadcasts,
            EnrichmentCompleteListener listener) {

        // Step 1: Collect unique room IDs that need to be loaded
        Set<Integer> roomIdsToLoad = new HashSet<>();
        for (BroadcastFilm broadcast : broadcasts) {
            int roomId = broadcast.getRoomID();
            if (!globalRoomCache.containsKey(roomId)) {
                roomIdsToLoad.add(roomId);
            }
        }

        Log.d(TAG, "Need to load " + roomIdsToLoad.size() + " unique rooms, " +
              globalRoomCache.size() + " already cached");

        // Step 2: If all rooms are cached, enrich immediately
        if (roomIdsToLoad.isEmpty()) {
            enrichAllBroadcasts(broadcasts);
            if (listener != null) {
                listener.onEnrichmentComplete(broadcasts);
            }
            return;
        }

        // Step 3: Load missing rooms from API
        final AtomicInteger completedCount = new AtomicInteger(0);
        final int totalToLoad = roomIdsToLoad.size();
        ApiRoomService apiRoomService = ApiClient.getRetrofit().create(ApiRoomService.class);

        for (Integer roomId : roomIdsToLoad) {
            Log.d(TAG, "Loading room ID: " + roomId);

            apiRoomService.getRoomById(roomId).enqueue(new Callback<RoomResponse>() {
                @Override
                public void onResponse(@NonNull Call<RoomResponse> call, @NonNull Response<RoomResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        RoomResponse room = response.body();
                        // Save to global cache
                        synchronized (globalRoomCache) {
                            globalRoomCache.put(roomId, room);
                        }
                        Log.d(TAG, "Room " + roomId + " loaded -> CinemaId: " + room.getCinemaId());
                    } else {
                        Log.w(TAG, "Failed to load room " + roomId + ": HTTP " + response.code());
                    }

                    // Check if all rooms loaded
                    if (completedCount.incrementAndGet() == totalToLoad) {
                        Log.d(TAG, "All rooms loaded, now enriching broadcasts");
                        enrichAllBroadcasts(broadcasts);
                        if (listener != null) {
                            listener.onEnrichmentComplete(broadcasts);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RoomResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error loading room " + roomId + ": " + t.getMessage());

                    // Check if all rooms loaded (even with failures)
                    if (completedCount.incrementAndGet() == totalToLoad) {
                        Log.d(TAG, "All room requests completed (some may have failed)");
                        enrichAllBroadcasts(broadcasts);
                        if (listener != null) {
                            listener.onEnrichmentComplete(broadcasts);
                        }
                    }
                }
            });
        }
    }

    /**
     * Enrich all broadcasts using cached room data
     */
    private static void enrichAllBroadcasts(List<BroadcastFilm> broadcasts) {
        int enrichedCount = 0;
        for (BroadcastFilm broadcast : broadcasts) {
            int roomId = broadcast.getRoomID();
            RoomResponse room;
            synchronized (globalRoomCache) {
                room = globalRoomCache.get(roomId);
            }

            if (room != null) {
                enrichBroadcastWithRoom(broadcast, room);
                enrichedCount++;
            } else {
                Log.w(TAG, "Room " + roomId + " not in cache, cannot enrich broadcast " + broadcast.getID());
            }
        }
        Log.d(TAG, "Enriched " + enrichedCount + "/" + broadcasts.size() + " broadcasts");
    }

    /**
     * Enrich a single broadcast with room and cinema information
     */
    private static void enrichBroadcastWithRoom(BroadcastFilm broadcast, RoomResponse room) {
        if (room == null) {
            Log.w(TAG, "Room is null for broadcast " + broadcast.getID());
            return;
        }

        if (room.getCinemaId() == null) {
            Log.w(TAG, "Room " + room.getId() + " has no cinema assigned");
            return;
        }

        // Get cinema from cache
        Cinema cinema = CinemaCache.getCinemaById(room.getCinemaId());
        if (cinema != null) {
            broadcast.setCinemaId(cinema.getId());
            broadcast.setCinemaName(cinema.getName());
            broadcast.setCinemaAddress(cinema.getAddress());
            broadcast.setCinemaLatitude(cinema.getLatitude());
            broadcast.setCinemaLongitude(cinema.getLongitude());

            // Set distance and duration if available
            if (cinema.getDistance() != null) {
                broadcast.setDistanceText(cinema.getDistance().getText());
            }
            if (cinema.getDuration() != null) {
                broadcast.setDurationText(cinema.getDuration().getText());
            }

            Log.d(TAG, "✓ Enriched broadcast " + broadcast.getID() +
                  " with cinema: " + cinema.getName() +
                  " (Room: " + room.getName() + ")");
        } else {
            Log.w(TAG, "✗ Cinema ID " + room.getCinemaId() + " not found in cache!");
            // Log all available cinema IDs for debugging
            if (CinemaCache.isCached()) {
                StringBuilder sb = new StringBuilder("Available cinema IDs: ");
                for (Cinema c : CinemaCache.getCachedCinemas()) {
                    sb.append(c.getId()).append(", ");
                }
                Log.d(TAG, sb.toString());
            }
        }
    }

    /**
     * Enrich a single broadcast with cinema information
     * @param broadcast Broadcast to enrich
     * @param listener Callback when enrichment is complete
     */
    public static void enrichSingleBroadcast(
            BroadcastFilm broadcast,
            EnrichmentCompleteListener listener) {

        List<BroadcastFilm> list = new ArrayList<>();
        list.add(broadcast);
        enrichBroadcastsWithCinemaInfo(list, new EnrichmentCompleteListener() {
            @Override
            public void onEnrichmentComplete(List<BroadcastFilm> enrichedBroadcasts) {
                if (listener != null && !enrichedBroadcasts.isEmpty()) {
                    List<BroadcastFilm> singleList = new ArrayList<>();
                    singleList.add(enrichedBroadcasts.get(0));
                    listener.onEnrichmentComplete(singleList);
                }
            }

            @Override
            public void onEnrichmentError(String error) {
                if (listener != null) {
                    listener.onEnrichmentError(error);
                }
            }
        });
    }
}

