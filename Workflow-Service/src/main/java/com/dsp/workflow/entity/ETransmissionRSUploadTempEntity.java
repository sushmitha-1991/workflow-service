package com.dsp.workflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "etransmission_rs_temp_new", schema = "migration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ETransmissionRSUploadTempEntity {

    @Id
    private Long id;

    @Column(name = "sno_dsp1")
    private Integer snoDsp1;

    @Column(name = "list1_eng")
    private String list1Eng;

    @Column(name = "list1_hindi")
    private String list1Hindi;

    @Column(name = "eng_errata")
    private String engErrata;

    @Column(name = "hindi_errata")
    private String hindiErrata;

    @Column(name = "list1_eng_url")
    private String list1EngUrl;

    @Column(name = "list1_hindi_url")
    private String list1HindiUrl;

    @Column(name = "eng_errata_url")
    private String engErrataUrl;

    @Column(name = "hindi_errata_url")
    private String hindiErrataUrl;

    @Column(name = "house_id")
    private Integer houseId;

    @Column(name = "list1_eng_uuid")
    private String list1EngUuid;

    @Column(name = "list1_hindi_uuid")
    private String list1HindiUuid;

    @Column(name = "eng_errata_uuid")
    private String engErrataUuid;

    @Column(name = "hindi_errata_uuid")
    private String hindiErrataUuid;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "is_updated_uuid")
    private Boolean isUpdatedAllUuid;
    
    @Column(name = "dateofcirculation")
    private LocalDate dateOfCirculation;
}
