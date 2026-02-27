package ru.myproject.itq.service.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.myproject.itq.config.AppProperties;
import ru.myproject.itq.dto.BatchWorkflowRequest;
import ru.myproject.itq.dto.BatchWorkflowResponse;
import ru.myproject.itq.enums.BatchItemResult;
import ru.myproject.itq.enums.DocumentStatus;
import ru.myproject.itq.repository.DocumentRepository;
import ru.myproject.itq.service.DocumentService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitWorker {

    private final AppProperties properties;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    @Scheduled(fixedDelayString ="${app.workers.approve.fixedDelayMs:2000}")
    public void run(){
        if (!properties.getWorkers().getSubmit().isEnabled()) return;

        int batchSize = properties.getBatchSize();
        String initiation = properties.getWorkers().getSubmit().getInitiator();

        List<Long> ids = documentRepository.findIdsByStatus(DocumentStatus.DRAFT, PageRequest.of(0, batchSize));

        if (ids.isEmpty())return;

        Long startTime = System.nanoTime();

        BatchWorkflowResponse submit = documentService.submit(new BatchWorkflowRequest(ids, initiation, "auto-submit"));

        Long finishTime = (System.nanoTime() - startTime) / 1_000_000;

        Map<BatchItemResult, Integer> stats = summarize(submit);

        Long remaining = documentRepository.countByStatus(DocumentStatus.DRAFT);

        log.info("[SUBMIT-worker] batchSize={}, fetched={}, tookMs={}, stats={}, remainingDraft={}",
                batchSize, ids.size(), finishTime, stats, remaining);



    }

    private Map<BatchItemResult, Integer> summarize(BatchWorkflowResponse response) {
        Map<BatchItemResult, Integer> map = new EnumMap<>(BatchItemResult.class);

        response.result().forEach(item -> map.merge(item.result(), 1, Integer::sum));

        return map;
    }
}
