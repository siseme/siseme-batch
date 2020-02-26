package me.sise.batch.application.service;

import java.io.IOException;

public interface NaverService {
    String crawlNaver(String portalId) throws IOException;
}
