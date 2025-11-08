package app.greenpoint.dto.kakaopay;

import lombok.Data;

@Data
public class KakaoPayApproveRequestDto {
    private String cid;
    private String cid_secret;
    private String tid;
    private String partner_order_id;
    private String partner_user_id;
    private String pg_token;
    private String payload;
    private Integer total_amount;
}
