package it.gov.pagopa.merchant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "point_of_sales")
public class PointOfSale {

    @MongoId
    private ObjectId id;

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

}
