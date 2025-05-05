package com.example.mypixel.service;

import com.google.common.collect.Iterators;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Component
public class BatchProcessor {

    private final Executor graphTaskExecutor;

    int batchSize = 50;

    public <T> void processBatches(Collection<T> input, Consumer<T> itemProcessor) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        Iterators.partition(input.iterator(), batchSize)
                .forEachRemaining(batch -> futures.add(CompletableFuture.runAsync(() -> {
                    for (T item : batch) {
                        itemProcessor.accept(item);
                    }
                }, graphTaskExecutor)));

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();
    }

    public <T> void processBatchesList(Collection<T> input, Consumer<List<T>> itemProcessor) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        Iterators.partition(input.iterator(), batchSize)
                .forEachRemaining(batch -> futures.add(CompletableFuture.runAsync(() ->
                                itemProcessor.accept(batch), graphTaskExecutor)));

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();
    }
}
