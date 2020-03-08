package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

    /**
     * A DTO for the {@link sa.tamkeentech.tbs.domain.Payment} entity.
     */
    @ApiModel(description = "Online Sadad refund Response DTO.")
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public class RefundStatusSadadResponseDTO implements Serializable {

        @JsonProperty("BillerSvcRs")
        private RefundResult refundResult ;


        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class RefundResult {
            @JsonProperty("Status")
            public Status status;

            @JsonProperty("RqUID")
            private String uid;

            @JsonProperty("RefundLoadRs")
            private ResponseBody response;
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Status {
            @JsonProperty("StatusCode")
            private String code;

            @JsonProperty("ShortDesc")
            private String description;

            @JsonProperty("Severity")
            private String severity;
        }


        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ResponseBody {
            @JsonProperty("Error")
            private ErrorResp error;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ErrorResp {

            @JsonProperty("ErrorCode")
            private String code;

            @JsonProperty("ErrorMsg")
            private String message;
        }


    }

