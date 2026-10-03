package org.akira.laptopstore.review.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReviewVisibilityRequest {
    @NotNull
    private Boolean visible;
}
