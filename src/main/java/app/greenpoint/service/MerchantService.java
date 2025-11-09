package app.greenpoint.service;

import app.greenpoint.domain.Merchant;
import app.greenpoint.dto.MerchantNameDto;
import app.greenpoint.dto.MerchantResponseDto;
import app.greenpoint.repository.MerchantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    @Transactional(readOnly = true)
    public MerchantResponseDto getMerchantById(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException("Merchant not found with id: " + merchantId));
        return new MerchantResponseDto(merchant.getId(), merchant.getName());
    }

    @Transactional(readOnly = true)
    public List<MerchantNameDto> getAllMerchantNames() {
        return merchantRepository.findAll().stream()
                .map(merchant -> new MerchantNameDto(merchant.getId(), merchant.getName()))
                .collect(Collectors.toList());
    }
}
