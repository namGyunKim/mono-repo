package com.example.domain.member.support;

import com.example.domain.member.payload.dto.MemberImageStorageDeleteCommand;
import com.example.domain.member.payload.dto.MemberImagesStorageDeleteCommand;

public interface MemberImageStoragePort {

    void deleteImage(MemberImageStorageDeleteCommand command);

    void deleteImages(MemberImagesStorageDeleteCommand command);
}
