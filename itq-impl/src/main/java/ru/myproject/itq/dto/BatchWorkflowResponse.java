package ru.myproject.itq.dto;

import java.util.List;

public record BatchWorkflowResponse(
        List<BatchItemResponse> result
) {
}
