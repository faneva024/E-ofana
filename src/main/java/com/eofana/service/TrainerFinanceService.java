package com.eofana.service;

import com.eofana.dto.TrainerFinanceSummaryResponse;
import com.eofana.dto.TrainerTransferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrainerFinanceService {
    TrainerFinanceSummaryResponse getFinanceSummaryByEmail(String email);
    Page<TrainerTransferResponse> getTransferHistoryByEmail(String email, Pageable pageable);
}