package com.bookms.service.impl;

import com.bookms.config.BookmsProperties;
import com.bookms.dto.request.ReaderSaveRequest;
import com.bookms.dto.response.PageResponse;
import com.bookms.dto.response.ReaderResponse;
import com.bookms.entity.Reader;
import com.bookms.entity.User;
import com.bookms.exception.BusinessException;
import com.bookms.mapper.ReaderMapper;
import com.bookms.repository.ReaderRepository;
import com.bookms.repository.UserRepository;
import com.bookms.security.SecurityUtils;
import com.bookms.service.ReaderService;
import com.bookms.util.PageableUtils;
import com.bookms.util.ReaderCardNoGenerator;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {

    private final ReaderRepository readerRepository;
    private final UserRepository userRepository;
    private final ReaderMapper readerMapper;
    private final ReaderCardNoGenerator readerCardNoGenerator;
    private final BookmsProperties bookmsProperties;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReaderResponse> pageReaders(String keyword, String readerType, int page, int size) {
        Pageable pageable = PageableUtils.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReaderResponse> result = readerRepository.findAll(buildSpecification(keyword, readerType), pageable)
                .map(readerMapper::toResponse);
        return PageResponse.of(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ReaderResponse getReader(Long id) {
        return readerMapper.toResponse(getReaderEntity(id));
    }

    @Override
    @Transactional
    public ReaderResponse create(ReaderSaveRequest request) {
        if (readerRepository.existsByUserIdAndDeletedFalse(request.getUserId())) {
            throw new BusinessException(409, "该用户已关联读者信息");
        }
        User user = getUser(request.getUserId());
        Reader reader = readerMapper.toEntity(request);
        reader.setUser(user);
        applyDefaults(reader);
        return readerMapper.toResponse(readerRepository.save(reader));
    }

    @Override
    @Transactional
    public ReaderResponse update(Long id, ReaderSaveRequest request) {
        Reader reader = getReaderEntity(id);
        if (!reader.getUser().getId().equals(request.getUserId())) {
            throw new BusinessException(400, "不允许变更读者关联用户");
        }
        readerMapper.updateEntity(request, reader);
        if (reader.getValidDateStart() == null) {
            reader.setValidDateStart(LocalDate.now());
        }
        if (reader.getValidDateEnd() == null) {
            reader.setValidDateEnd(reader.getValidDateStart().plusYears(bookmsProperties.getBorrow().getReaderCardValidYears()));
        }
        return readerMapper.toResponse(readerRepository.save(reader));
    }

    @Override
    @Transactional
    public ReaderResponse issueCard(Long id) {
        Reader reader = getReaderEntity(id);
        if (!StringUtils.hasText(reader.getReaderCardNo())) {
            reader.setReaderCardNo(readerCardNoGenerator.next());
        }
        if (reader.getValidDateStart() == null) {
            reader.setValidDateStart(LocalDate.now());
        }
        if (reader.getValidDateEnd() == null || reader.getValidDateEnd().isBefore(reader.getValidDateStart())) {
            reader.setValidDateEnd(reader.getValidDateStart().plusYears(bookmsProperties.getBorrow().getReaderCardValidYears()));
        }
        return readerMapper.toResponse(readerRepository.save(reader));
    }

    @Override
    @Transactional(readOnly = true)
    public ReaderResponse currentReader() {
        Long userId = SecurityUtils.getCurrentUserId();
        Reader reader = readerRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(404, "未找到当前用户的读者信息"));
        return readerMapper.toResponse(reader);
    }

    @Override
    @Transactional
    public Reader createDefaultReader(User user) {
        Reader reader = new Reader();
        reader.setUser(user);
        reader.setReaderType("general");
        reader.setMaxBorrowCount(5);
        reader.setCurrentBorrowCount(0);
        reader.setCreditScore(100);
        reader.setBlacklist(Boolean.FALSE);
        reader.setValidDateStart(LocalDate.now());
        reader.setValidDateEnd(LocalDate.now().plusYears(bookmsProperties.getBorrow().getReaderCardValidYears()));
        return readerRepository.save(reader);
    }

    private Reader getReaderEntity(Long id) {
        return readerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "读者不存在"));
    }

    private User getUser(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    private void applyDefaults(Reader reader) {
        if (reader.getCreditScore() == null) {
            reader.setCreditScore(100);
        }
        if (reader.getCurrentBorrowCount() == null) {
            reader.setCurrentBorrowCount(0);
        }
        if (reader.getBlacklist() == null) {
            reader.setBlacklist(Boolean.FALSE);
        }
        if (reader.getValidDateStart() == null) {
            reader.setValidDateStart(LocalDate.now());
        }
        if (reader.getValidDateEnd() == null) {
            reader.setValidDateEnd(reader.getValidDateStart().plusYears(bookmsProperties.getBorrow().getReaderCardValidYears()));
        }
    }

    private Specification<Reader> buildSpecification(String keyword, String readerType) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            var userJoin = root.join("user", JoinType.LEFT);
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("readerCardNo")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("realName")), like),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("username")), like)));
            }
            if (StringUtils.hasText(readerType)) {
                predicates.add(criteriaBuilder.equal(root.get("readerType"), readerType));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}