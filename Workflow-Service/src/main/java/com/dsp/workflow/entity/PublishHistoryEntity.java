package com.dsp.workflow.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "publish_history", schema = "workflow")
public class PublishHistoryEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "publish_history_seq")
    @SequenceGenerator(name = "publish_history_seq", sequenceName = "publish_history_seq", allocationSize = 1)
    private Long id;

    private String lokSabhaNo;
    private String lokSabhaSessionNo;
    private String rajyaSabhaSessionNo;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false, length = 100)
    private String applicationName;

    @Column(nullable = false, length = 100)
    private String applicationNo;

    @Column(nullable = false)
    private UUID publishUuid;
    @Column(nullable = false)

    private String fileType;

    @Column(nullable = false, columnDefinition = "text")
    private String publishUrl;

    @Column(nullable = false)
    private LocalDate forDate;

    @Column(nullable = false)
    private Boolean publishStatus;

    @Column(nullable = false, length = 100)
    private String publishByUser;

    @Column(nullable = false)
    private LocalDate publishDate;

    private String unpublishByUser;
    private LocalDate unpublishDate;

    @Column(nullable = false)
    private Long officeId;

    @Column(nullable = false)
    private String locale;

}
