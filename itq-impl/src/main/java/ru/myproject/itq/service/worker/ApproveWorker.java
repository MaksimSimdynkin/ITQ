package ru.myproject.itq.service.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
public class ApproveWorker {

    private final AppProperties properties;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    @Scheduled(fixedDelayString ="${app.workers.approve.fixedDelayMs:2000}")
    public void run(){
        if (!properties.getWorkers().getApprove().isEnabled())return;

        int batchSize = properties.getBatchSize();
        String initiation = properties.getWorkers().getApprove().getInitiator();

        List<Long> ids = documentRepository.findIdsByStatus(DocumentStatus.SUBMITTED, PageRequest.of(0, batchSize));

        if(ids.isEmpty()){
            return;
        }

        Long startTime = System.nanoTime();

        BatchWorkflowResponse approve = documentService.approve(new BatchWorkflowRequest(ids, initiation, "auto=approve"));

        Long finishTime = (System.nanoTime() - startTime) / 1_000_000;

        Map<BatchItemResult, Integer> stats = summarize(approve);

        Long remaining = documentRepository.countByStatus(DocumentStatus.SUBMITTED);

        log.info("[APPROVE-worker] batchSize={}, fetched={}, tookMs={}, stats={}, remainingSubmitted={}",
                batchSize, ids.size(), finishTime, stats, remaining);

    }
    private Map<BatchItemResult, Integer> summarize(BatchWorkflowResponse response) {
        Map<BatchItemResult, Integer> map = new EnumMap<>(BatchItemResult.class);

        response.result().forEach(item -> map.merge(item.result(), 1, Integer::sum));

        return map;
    }
}
