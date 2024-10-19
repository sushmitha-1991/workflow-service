package com.dsp.workflow.levelbyabbraction.entity;

import com.dsp.workflow.entity.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity

@Table(name = "levelbyabbraction",schema = "workflow")
public class LevelByAbbrAction extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow.levelbyabbraction_seq_gen")
    @SequenceGenerator(name = "levelbyabbraction_seq_gen", sequenceName = "levelbyabbraction_seq", allocationSize = 1)
    private Long id;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "level", length = 50)
    private String level;

    @Column(name = "action_key", length = 100)
    private String actionKey;

    @Column(name = "abbr", length = 50)
    private String abbr;

    @Column(name = "step_type", length = 20)
    private String stepType;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "status_key", length = 50)
    private String statusKey;

    @Column(name = "application_id")
    private Long applicationId;
}
