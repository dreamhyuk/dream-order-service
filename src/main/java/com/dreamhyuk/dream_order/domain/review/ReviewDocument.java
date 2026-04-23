package com.dreamhyuk.dream_order.domain.review;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "reviews")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setting(settingPath = "/elasticsearch/settings.json")
public class ReviewDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String shopId;

    @Field(type = FieldType.Long)
    private Long customerId;

    @Field(type = FieldType.Keyword)
    private String nickname;

    @Field(type = FieldType.Double)
    private Double score;

    @Field(type = FieldType.Text)
    private String content;
}
