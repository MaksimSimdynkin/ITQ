package ru.myproject.itqgenerator.generet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentGeneratorRunner implements CommandLineRunner {

    private final GeneratorProperties properties;

    record CreateDocumentBody(String author, String title, String initiator) {}

    record DocumentResponse(Long documentId, Long number, String status) {}


    @Override
    public void run(String... args) throws Exception {
        RestClient client = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();

        int n = properties.getN();
        int logEvery = properties.getLogEvery();
        int parallelism = properties.getParallelism();

        log.info("[GEN] Start. baseUrl={}, n={}, parallelism={}, logEvery={}",
                properties.getBaseUrl(), n, parallelism, logEvery);

        Long startTime = System.nanoTime();

        AtomicInteger created = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        if (parallelism <= 1) {
            for (int i = 1; i <= n; i++) {
                boolean ok = createOne(client, i);
                if (ok) created.incrementAndGet(); else failed.incrementAndGet();

                int done = created.get() + failed.get();
                if (done % logEvery == 0 || done == n) {
                    log.info("[GEN] Progress: {}/{} (ok={}, failed={})", done, n, created.get(), failed.get());
                }
            }
        } else {
            ExecutorService pool = Executors.newFixedThreadPool(parallelism);
            CompletionService<Boolean> cs = new ExecutorCompletionService<>(pool);

            int submitted = 0;
            int completed = 0;
            int maxInFlight = parallelism * 4;

            try {
                while (completed < n) {
                    while (submitted < n && (submitted - completed) < maxInFlight) {
                        final int idx = submitted + 1;
                        cs.submit(() -> createOne(client, idx));
                        submitted++;
                    }

                    Future<Boolean> f = cs.take();
                    boolean ok = f.get();
                    if (ok) created.incrementAndGet(); else failed.incrementAndGet();
                    completed++;

                    if (completed % logEvery == 0 || completed == n) {
                        log.info("[GEN] Progress: {}/{} (ok={}, failed={})",
                                completed, n, created.get(), failed.get());
                    }
                }
            } finally {
                pool.shutdownNow();
            }
        }

        long tookMs = (System.nanoTime() - startTime) / 1_000_000;
        log.info("[GEN] Done. created={}, failed={}, tookMs={}", created.get(), failed.get(), tookMs);

        System.exit(failed.get() == 0 ? 0 : 2);
    }

    private boolean createOne(RestClient client, int i) {
        String author = properties.getAuthorPrefix() + "-" + i;
        String title = properties.getTitlePrefix() + "-" + i;

        try {
            DocumentResponse resp = client.post()
                    .uri("/api/v1/documents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateDocumentBody(author, title, properties.getInitiator()))
                    .retrieve()
                    .body(DocumentResponse.class);

            return resp != null && resp.documentId() != null;
        } catch (Exception e) {
            log.warn("[GEN] Failed to create doc #{}: {}", i, e.getMessage());
            return false;
        }
    }
}