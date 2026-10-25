package org.akira.laptopstore.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

//backend tra json
//{
//  "RspCode": "00",
//  "Message": "Confirm Success"
//}
public record VnPayIpnResponse(

        @JsonProperty("RspCode") String rspCode,

        @JsonProperty("Message") String message) {

    // 00 → xử lý thành công
    public static VnPayIpnResponse success() {
        return new VnPayIpnResponse(
                "00",
                "Confirm Success");
    }

    // 01 → không tìm thấy giao dịch
    public static VnPayIpnResponse notFound() {
        return new VnPayIpnResponse(
                "01",
                "Order not found");
    }

    // 02 → giao dịch đã xử lý rồi
    public static VnPayIpnResponse alreadyProcessed() {
        return new VnPayIpnResponse(
                "02",
                "Order already confirmed");
    }

    // 04 → số tiền không khớp
    public static VnPayIpnResponse invalidAmount() {
        return new VnPayIpnResponse(
                "04",
                "Invalid Amount");
    }

    // 97 → checksum sai
    public static VnPayIpnResponse invalidSignature() {
        return new VnPayIpnResponse(
                "97",
                "Invalid Checksum");
    }

    // 99 → lỗi hệ thống
    public static VnPayIpnResponse systemError() {
        return new VnPayIpnResponse(
                "99",
                "Unknown error");
    }
}