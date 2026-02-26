package com.example.domain.aws.service.query;

import com.example.domain.aws.payload.dto.S3ImageUrlQuery;

public interface S3QueryService {

    String getImageUrl(S3ImageUrlQuery query);
}
