package it.gov.pagopa.merchant.model;

import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
@Document(collection = "point_of_sales")
public class PointOfSale {
    @MongoId
    private String id;

    private String type;
    private String franchiseName;
    private String region;
    private String province;
    private String city;
    private String zipCode;
    private String address;
    private String streetNumber;
    private String website;
    private String contactEmail;
    private String contactName;
    private String contactSurname;
    private List<Channel> channels;

    private String merchantId;

    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
}
