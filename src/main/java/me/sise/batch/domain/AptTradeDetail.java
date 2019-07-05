package me.sise.batch.domain;

import com.google.common.collect.Lists;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class AptTradeDetail {
    private Header header = new Header();
    private Body body = new Body();

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Header {
        private String resultCode = "";
        private String resultMsg = "";
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Body {
        private Items items = new Items();

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Items {
            private List<Item> item = Lists.newArrayList();

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Item {
                @XmlElement(name = "거래금액")
                private String price;
                @XmlElement(name = "건축년도")
                private String since;
                @XmlElement(name = "년")
                private String year;
                @XmlElement(name = "도로명")
                private String road;
                @XmlElement(name = "도로명건물본번호코드")
                private String roadMainCode;
                @XmlElement(name = "도로명건물부번호코드")
                private String roadSubCode;
                @XmlElement(name = "도로명시군구코드")
                private String roadSigunguCode;
                @XmlElement(name = "도로명일련번호코드")
                private String roadSerialNumberCode;
                @XmlElement(name = "도로명지상지하코드")
                private String roadGroundCode;
                @XmlElement(name = "도로명코드")
                private String roadCode;
                @XmlElement(name = "법정동")
                private String dong;
                @XmlElement(name = "법정동본번코드")
                private String dongMainCode;
                @XmlElement(name = "법정동부번코드")
                private String dongSubCode;
                @XmlElement(name = "법정동시군구코드")
                private String dongSigunguCode;
                @XmlElement(name = "법정동읍면동코드")
                private String dongCode;
                @XmlElement(name = "법정동지번코드")
                private String dongLotNumberCode;
                @XmlElement(name = "아파트")
                private String aptName;
                @XmlElement(name = "월")
                private String month;
                @XmlElement(name = "일")
                private String day;
                @XmlElement(name = "일련번호")
                private String serialNumber;
                @XmlElement(name = "전용면적")
                private String area;
                @XmlElement(name = "지번")
                private String lotNumber;
                @XmlElement(name = "지역코드")
                private String regionCode;
                @XmlElement(name = "층")
                private String floor;
            }
        }
    }
}
