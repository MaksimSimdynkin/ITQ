package ru.myproject.itq.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.myproject.itq.dto.ConcurrentApproveRequest;
import ru.myproject.itq.dto.ConcurrentApproveResponse;
import ru.myproject.itq.entity.Document;
import ru.myproject.itq.enums.BatchItemResult;
import ru.myproject.itq.enums.DocumentStatus;
import ru.myproject.itq.exeption.RegistryWriteFailedRuntimeException;
import ru.myproject.itq.repository.ApprovalRegistryRepository;
import ru.myproject.itq.repository.DocumentRepository;
import ru.myproject.itq.service.DocumentConcurrencyService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class DocumentConcurrencyServiceImpl implements DocumentConcurrencyService {

    private final DocumentWorkService documentWorkService;
    private final DocumentRepository documentRepository;
    private final ApprovalRegistryRepository approvalRegistryRepository;

    @Override
    @Transactional
    public ConcurrentApproveResponse concurrentApprove(Long documentId, ConcurrentApproveRequest request) {

        int threads = request.threads();
        int attempts = request.attempts();

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger conflict = new AtomicInteger(0);
        AtomicInteger notFound = new AtomicInteger(0);
        AtomicInteger registryError = new AtomicInteger(0);
        AtomicInteger error = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        int base = attempts / threads;
        int rem = attempts % threads;

        for (int t = 0; t < threads; t++) {
            int myAttempts = base + (t < rem ? 1 : 0);

            pool.submit(() -> {
                try {
                    startLatch.await();

                    for (int i = 0; i < myAttempts; i++) {
                        try {
                            BatchItemResult res = documentWorkService.approveOne(
                                    documentId,
                                    request.initiator(),
                                    request.comment()
                            );

                            switch (res) {
                                case SUCCESS -> success.incrementAndGet();
                                case CONFLICT -> conflict.incrementAndGet();
                                case NOT_FOUND -> notFound.incrementAndGet();
                                case REGISTRY_ERROR -> registryError.incrementAndGet(); // на всякий случай
                                default -> error.incrementAndGet();
                            }

                        } catch (RegistryWriteFailedRuntimeException e) {
                            registryError.incrementAndGet();
                        } catch (ObjectOptimisticLockingFailureException e) {
                            conflict.incrementAndGet();
                        } catch (Exception e) {
                            error.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    error.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();

        try {
            boolean finished = doneLatch.await(60, TimeUnit.SECONDS);
            if (!finished) {
                error.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            error.incrementAndGet();
        } finally {
            pool.shutdownNow();
        }

        DocumentStatus finalStatus = documentRepository.findById(documentId)
                .map(Document::getStatus)
                .orElse(null);

        long registryCount = approvalRegistryRepository.countByDocument_DocumentId(documentId);

        return new ConcurrentApproveResponse(
                documentId,
                threads,
                attempts,
                success.get(),
                conflict.get(),
                notFound.get(),
                registryError.get(),
                error.get(),
                finalStatus,
                registryCount
        );
    }
}