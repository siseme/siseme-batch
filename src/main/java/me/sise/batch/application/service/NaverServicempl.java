package me.sise.batch.application.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class NaverServicempl implements NaverService{
    private static final String URL_PREFIX = "https://m.land.naver.com/complex/info/";
    private static final String URL_POSTIFX = "?tradTpCd=A1:B1:B2:B3&ptpNo=&bildNo=&articleListYN=Y";

    @Override
    public String crawlNaver(String portalId) throws IOException {
        String url = URL_PREFIX + portalId + URL_POSTIFX;
        Document doc = Jsoup.connect(url).get();
        Elements imgs = doc.getElementsByClass("item_inner");
        return null;
    }
}
