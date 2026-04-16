package com.bookms.service.impl;

import com.bookms.dto.*;
import com.bookms.entity.ReaderInfo;
import com.bookms.entity.SysUser;
import com.bookms.exception.BusinessException;
import com.bookms.repository.ReaderInfoRepository;
import com.bookms.repository.SysUserRepository;
import com.bookms.service.ReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {

    private final ReaderInfoRepository readerInfoRepository;
    private final SysUserRepository userRepository;

    @Override
    public PageResponse<ReaderVO> listReaders(String keyword, Pageable pageable) {
        Page<ReaderInfo> page = readerInfoRepository.findByConditions(keyword, pageable);
        return PageResponse.of(page.map(this::toReaderVO));
    }

    @Override
    public ReaderVO getReaderByUserId(Long userId) {
        ReaderInfo reader = readerInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("读者信息不存在"));
        return toReaderVO(reader);
    }

    @Override
    @Transactional
    public ReaderVO updateReader(Long id, ReaderDTO dto) {
        ReaderInfo reader = readerInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("读者信息不存在"));
        if (dto.getReaderType() != null) reader.setReaderType(dto.getReaderType());
        if (dto.getMaxBorrowCount() != null) reader.setMaxBorrowCount(dto.getMaxBorrowCount());
        if (dto.getValidDateStart() != null) reader.setValidDateStart(dto.getValidDateStart());
        if (dto.getValidDateEnd() != null) reader.setValidDateEnd(dto.getValidDateEnd());
        readerInfoRepository.save(reader);
        return toReaderVO(reader);
    }

    @Override
    @Transactional
    public void toggleBlacklist(Long id, Boolean blacklist) {
        ReaderInfo reader = readerInfoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("读者信息不存在"));
        reader.setIsBlacklist(blacklist);
        readerInfoRepository.save(reader);
    }

    private ReaderVO toReaderVO(ReaderInfo reader) {
        ReaderVO.ReaderVOBuilder builder = ReaderVO.builder()
                .id(reader.getId())
                .userId(reader.getUserId())
                .readerCardNo(reader.getReaderCardNo())
                .readerType(reader.getReaderType())
                .maxBorrowCount(reader.getMaxBorrowCount())
                .currentBorrowCount(reader.getCurrentBorrowCount())
                .creditScore(reader.getCreditScore())
                .isBlacklist(reader.getIsBlacklist())
                .validDateStart(reader.getValidDateStart())
                .validDateEnd(reader.getValidDateEnd())
                .createdAt(reader.getCreatedAt());

        userRepository.findById(reader.getUserId()).ifPresent(u -> {
            builder.username(u.getUsername());
            builder.realName(u.getRealName());
        });

        return builder.build();
    }
}
