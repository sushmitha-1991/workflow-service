package com.dsp.workflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "member_photo_sign_temp_rs", schema = "migration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberPhotoSignTempRS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer mpcode;
    private String relationWithMember;
    private String photo;
    private String signature;
    private String photoUrl;
    private String signatureUrl;
    private String house;

    private UUID photoUuid;

    private UUID signatureUuid;

    private LocalDate createdAt;

    private String createdBy;

    private LocalDate updatedAt;

    private String updatedBy;
    private Boolean isPhotoUploaded;
    private Boolean isSignUploaded;
}
