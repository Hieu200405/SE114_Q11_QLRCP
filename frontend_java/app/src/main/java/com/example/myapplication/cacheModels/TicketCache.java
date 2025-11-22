package com.example.myapplication.cacheModels;

import com.example.myapplication.models.Ticket;

import java.util.List;

public class TicketCache {
    private static List<Ticket> cachedTickets;

    public static List<Ticket> getCachedTickets() {
        return cachedTickets;
    }

    public static void setCachedTickets(List<Ticket> tickets) {
        cachedTickets = tickets;
    }

    public static boolean isCached() {
        return cachedTickets != null && !cachedTickets.isEmpty();
    }

    public static void clearCache() {
        cachedTickets = null;
    }
}