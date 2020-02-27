package me.sise.batch.application.service;

import lombok.extern.slf4j.Slf4j;
import me.sise.batch.hgnn.repository.ApartmentMatchTableRepository;
import me.sise.batch.infrastructure.NaverClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NaveSyncService {

    private final NaverClient naverClient;
    private final ApartmentMatchTableRepository apartmentMatchTableRepository;

    public NaveSyncService(NaverClient naverClient,
                           ApartmentMatchTableRepository apartmentMatchTableRepository) {
        this.naverClient = naverClient;
        this.apartmentMatchTableRepository = apartmentMatchTableRepository;
    }

    public void sync() {
    }
}
