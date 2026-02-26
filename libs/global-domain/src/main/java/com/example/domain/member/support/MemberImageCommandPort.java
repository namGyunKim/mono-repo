package com.example.domain.member.support;

import com.example.domain.member.payload.dto.MemberImageDeleteCommand;
import com.example.domain.member.payload.dto.MemberImageRegisterCommand;

public interface MemberImageCommandPort {

    Long registerProfileImage(MemberImageRegisterCommand command);

    void deleteProfileImage(MemberImageDeleteCommand command);
}
