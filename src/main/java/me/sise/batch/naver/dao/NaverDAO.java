package me.sise.batch.naver.dao;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class NaverDAO {

    public String getNaverTradeInfo(String portarId) throws IOException {
        Document doc = Jsoup.connect("https://m.land.naver.com/complex/info/111830?tradTpCd=A1:B1:B2:B3&ptpNo=&bildNo=&articleListYN=Y").get();
        Elements elements = doc.getElementsByClass("article_box").first().children();
        for (Element e: elements) {
            e.text();
        }
        return "";
    }

}
