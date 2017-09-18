package com.hafidsousa.webcrawler.config;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Hafid Ferreira Sousa
 */
public final class Utils {

    public static <T> CompletableFuture<T> makeCompletableFuture(Future<T> future) {

        return CompletableFuture.supplyAsync(() -> {
            try
            {
                return future.get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    public static final class table {

        public static final String websites = "WEBSITES";
    }

    public static final class params {

        public static final String url = "url";

        public static final String status = "status";

        public static final String title = "title";

        public static final String nodes = "nodes";
    }

    public static final class error {

        public static final String failed_sqs = "Unable to Send Message {}";

        public static final String failed_dynamo_put = "Unable to Save Item {}";

        public static final String failed_dynamo_get = "Unable to Get Item {}";

        public static final String failed_get_website = "Unable to Get Website {}";
    }

    public static final class success {

        public static final String saved_dynamo = "Saved item {}";
        public static final String log_recursion= "Depth <%d> || Base URL <%s> || Child URL <%s>";
    }

    public static final class document {

        public static final String links = "a[href]";
        public static final String link_url = "abs:href";
        public static final String log_recursion= "Depth <%d> || Base URL <%s> || Child URL <%s>";
    }
}
