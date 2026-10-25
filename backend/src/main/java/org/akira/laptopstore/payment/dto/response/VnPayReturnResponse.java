package org.akira.laptopstore.payment.dto.response;

import lombok.Builder;
import lombok.Getter;

// {
//   "signatureValid": true,
//   "successful": true,
//   "transactionRef": "abc-123",
//   "responseCode": "00",
//   "transactionStatus": "00",
//   "transactionNo": "15002123",
//   "message": "Payment successful"
// }

// signatureValid
// → checksum VNPay có hợp lệ không

// successful
// → giao dịch có thành công không

// transactionRef
// → vnp_TxnRef
// → mã transaction do hệ thống mình tạo

// responseCode
// → vnp_ResponseCode

// transactionStatus
// → vnp_TransactionStatus

// transactionNo
// → vnp_TransactionNo
// → mã giao dịch do VNPay cấp

@Getter
@Builder
public class VnPayReturnResponse {

    private boolean signatureValid;

    private boolean successful;

    private String transactionRef;

    private String responseCode;

    private String transactionStatus;

    private String transactionNo;

    private String message;
}