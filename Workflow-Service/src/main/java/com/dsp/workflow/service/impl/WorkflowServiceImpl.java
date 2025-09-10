package com.dsp.workflow.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.dsp.workflow.dto.CorrectedDebatedDTO;
import com.dsp.workflow.dto.DebateTaggingDTO;
import com.dsp.workflow.dto.DraftDataDTO;
import com.dsp.workflow.dto.InProgressCountResponse;
import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;
import com.dsp.workflow.dto.SectionBulletin2AppHouseDTO;
import com.dsp.workflow.dto.SectionBulletin2DTO;
import com.dsp.workflow.dto.SerialNumberRequest;
import com.dsp.workflow.dto.SerialNumberResponse;
import com.dsp.workflow.dto.TaskHistoryDTO;
import com.dsp.workflow.dto.TaskQueryDTO;
import com.dsp.workflow.dto.TaskResponseDTO;
import com.dsp.workflow.dto.TaskVariableDto;
import com.dsp.workflow.dto.TranslationInputDTO;
import com.dsp.workflow.dto.UserDetailsDTO;
import com.dsp.workflow.entity.ApplicationInstance;
import com.dsp.workflow.entity.BaseUser;
import com.dsp.workflow.entity.DraftDocument;
import com.dsp.workflow.entity.WorkflowHistory;
import com.dsp.workflow.exception.CamundaException;
import com.dsp.workflow.exception.ResourceNotFoundException;
import com.dsp.workflow.exception.TaskNotFoundException;
import com.dsp.workflow.mapper.DocumentRowMapper;
import com.dsp.workflow.mapper.WorkflowMapper;
import com.dsp.workflow.notesheet.dto.NotesheetDTO;
import com.dsp.workflow.notesheet.entity.Notesheet;
import com.dsp.workflow.notesheet.mapper.NotesheetMapper;
import com.dsp.workflow.notesheet.repository.NotesheetRepository;
import com.dsp.workflow.repository.ApplicationInstanceRepository;
import com.dsp.workflow.repository.DraftDocumentRepository;
import com.dsp.workflow.repository.MyUserRepository;
import com.dsp.workflow.repository.TaskRepository;
import com.dsp.workflow.repository.WorkflowHistoryRepository;
import com.dsp.workflow.service.WorkflowService;
import com.dsp.workflow.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class WorkflowServiceImpl implements WorkflowService {

	@Autowired
	private NotesheetRepository notesheetRepository;

	@Autowired
	private ApplicationInstanceRepository applicationInstanceRepository;

	@Autowired
	private WorkflowHistoryRepository workflowHistoryRepository;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private Utils utils;

	private WorkflowMapper workflowMapper = new WorkflowMapper();

	@PersistenceContext
	private EntityManager entityManager;

	@Value("${camunda.bpm.rest.url}")
	private String camundaRestUrl;

	@Value("${camunda.bpm.admin-user.id}")
	private String camundaAdminId;

	@Value("${camunda.bpm.admin-user.password}")
	private String camundaAdminPassword;

	@Value("${translation.input.url}")
	private String translationInputUrl;

	@Value("${corrected.debate.url}")
	private String correctedDebateUrl;

	@Value("${debate.tagging.url}")
	private String debateTaggingUrl;

	@Value("${section.b2.rs.url}")
	private String sectionB2Url;

	@Value("${pdf.upload.url}")
	private String pdfUploadUrl;

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private MyUserRepository userRepository;
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	private DraftDocumentRepository draftDocumentRepository;

	private static final String CORRECTED_DEBATES_ABBR = "CD";

	private static final String CORRECTED_DEBATES_EN_ABBR = "CDE";

	private static final String CORRECTED_DEBATES_HI_ABBR = "CDH";

	private static final String B2_ABBR = "B2";

	private static final String BULLETIN_2_RS = "Bulletin - Part II";

	private static final String RS_HOUSE = "RS";

	private static final String PUBLISHED = "Published";

	@Value("${master.service.url}")
	private String masterServiceUrl;

	@Autowired
	private HttpServletRequest httpRequest;
	
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


	private final static String allDocumentsQuery = """

							SELECT applicationNo, documentName, "date", LSNo, SSNo, RSSNo, status, pendingAt, refId,
			applicationId, applicationinstanceId, taskName, taskId, actionPerformed, formUrl, actionUrl, draftId, publishedUrl, documentUUIDEn, documentUUIDHi, htmlContentEn, htmlContentHi, editorId, ma.translate_flag, ma.download_flag, ma.save_as_draft_url, uploaded_document_uuid
			FROM (
			                            select
			                    distinct on
			                    (ai.application_no) ai.application_no as applicationNo,
			                   case
			                        when ai.additional_details = 'Supplementary Synopsis' then 'Supplementary Synopsis'
			                        when (ai.translation_document_type_id is not null)
			        							then (select coalesce(app.document_display_name, app."name") from master.applications app
			        							where app.id = ai.translation_document_type_id)
			                		else coalesce(ai.document_display_name,  coalesce(a.document_display_name, a."name"))
			                	end
			                as documentName,
			                ai.document_date as date,
			                ai.lok_sabha_session as LSNo,
			                ai.lok_sabha_session_number as SSNo,
			                ai.rajya_sabha_session as RSSNo,
			                ai.current_status as status,
			                case
			                        when ai.current_status = 'Approved' then null
			                        else CONCAT(wh.pending_with_name)
			                end as pendingAt,
			                ai.ref_id as refId,
			                ai.application_id as applicationId,
			                ai.id as applicationinstanceId,
			                'IN-PROCESS' as taskName,
			                wh.next_task_id as taskId,
			                'True' as actionPerformed,
			                a.form_url as formUrl,
			                wh.action_url as actionUrl,
			                wh.draft_id as draftId,
			                a.published_url as publishedUrl,
			                ai.published_document_uuid_en as documentUUIDEn,
			                ai.published_document_uuid_hi as documentUUIDHi,
			                ai.html_content_en as htmlContentEn,
			                ai.html_content_hi as htmlContentHi,
			                null as editorId,
			                ai.uploaded_document_uuid as uploaded_document_uuid
			        from
			                workflow.workflow_history wh
			        join master.application_instance ai on
			                wh.application_instance_id = ai.id
			        join master.applications a on
			                ai.application_id = a.id
			        where
			                wh.pending_with = :userId
			                and wh.action_taken_by is null
			        and (ai.current_status != 'Approved' and ai.current_status != 'Published')
			        and a.id not in (select a.id from master.applications a, master.modules m
			                                        where a.module_id = m.id and a.abbr in ('SYN') and m.house_id = :houseId)
			union
			        select
			                distinct on
			                (ai.application_no) ai.application_no as applicationNo,
			                case
			                	when (ai.translation_document_type_id is not null)
			        							then (select coalesce(app.document_display_name, app."name") from master.applications app
			        							where app.id = ai.translation_document_type_id)
			                	else coalesce(ai.document_display_name,  coalesce(a.document_display_name, a."name"))
			        		end
			        		as documentName,
			                ai.document_date as date,
			                ai.lok_sabha_session as LSNo,
			                ai.lok_sabha_session_number as SSNo,
			                ai.rajya_sabha_session as RSSNo,
			                case
			                        when ai.current_status = 'Returned' then 'In Progress'
			                        else ai.current_status
			                end as status,
			                CONCAT(wh.pending_with_name) as pendingAt,
			                ai.ref_id as refId,
			                ai.application_id as applicationId,
			                ai.id as applicationinstanceId,
			                'IN-PROCESS' as taskName,
			                wh.next_task_id as taskId,
			                'False' as actionPerformed,
			                a.form_url as formUrl,
			                wh.action_url as actionUrl,
			                wh.draft_id as draftId,
			                a.published_url as publishedUrl,
			                ai.published_document_uuid_en as documentUUIDEn,
			                ai.published_document_uuid_hi as documentUUIDHi,
			                ai.html_content_en as htmlContentEn,
			                ai.html_content_hi as htmlContentHi,
			                null as editorId,
			                ai.uploaded_document_uuid as uploaded_document_uuid
			        from
			                (
			                select
			                        wh1.*
			                from
			                        workflow.workflow_history wh1
			                where
			                        wh1.id in (
			                        select
			                                max(id)
			                        from
			                                workflow.workflow_history wh2
			                        where
			                                wh2.application_instance_id in (
			                                select
			                                        application_instance_id
			                                from
			                                        workflow.workflow_history wh3
			                                where
			                                        wh3.action_taken_by = :userId)
			                        group by
			                                application_instance_id)
			                        and (wh1.pending_with != :userId
			                        or wh1.pending_with is null )) wh
			        join master.application_instance ai on
			                wh.application_instance_id = ai.id
			        join master.applications a on
			                ai.application_id = a.id
			                 where (ai.current_status != 'Approved' and ai.current_status != 'Published')
			                 and a.id not in (select a.id from master.applications a, master.modules m
			                        where a.module_id = m.id and a.abbr in ('SYN')  and m.house_id = :houseId)
			union
			        select
			                distinct on
			                (ii.application_no)
			                ii.application_no as applicationNo,
			                coalesce(a.document_display_name, a."name") as documentName,
			                ii.document_date as date,
			                ii.lok_sabha_session as LSNo,
			                ii.lok_sabha_session_number as SSNo,
			                ii.rajya_sabha_session as RSSNo,
			                case
			                        when ii.application_instance_id is not null
			                        and applicationInstance.id = ii.application_instance_id
			                        and applicationInstance.current_status not in ('Returned', 'Draft') then applicationInstance.current_status
			                        else ii.current_status
			                end as status,
			                case
			                        when ii.application_instance_id is not null
			                        and applicationInstance.id = ii.application_instance_id
			                        and applicationInstance.current_status in ('Approved', 'Published') then null
			                        else ii.pending_at
			                end as pendingAt,
			                ii.ref_id as refId,
			                ii.application_id as applicationId,
			                ii.id as applicationinstanceId,
			                'IN-PROCESS' as taskName,
			                null as taskId,
			                case
			                        when ii.current_status = 'Returned' then 'True'
			                        else 'False'
			                end as actionPerformed,
			                a.form_url as formUrl,
			                null as actionUrl,
			                null as draftId,
			                a.published_url as publishedUrl,
			                null as documentUUIDEn,
			                null as documentUUIDHi,
			                ii.html_content_en as htmlContentEn,
			                ii.html_content_hi as htmlContentHi,
			                null as editorId,
			                applicationInstance.uploaded_document_uuid as uploaded_document_uuid
			        from
			                master.input_instance ii
			        join master.applications a on
			                a.id = ii.application_id
			        join authentication.base_user u on
			                u.id = ii.created_by
			        left join master.application_instance applicationInstance on
			                applicationInstance.id = ii.application_instance_id
			        where
			                ii.created_by = :userId
			union
			        select
			                distinct on
			                (dd.application_no) dd.application_no as applicationNo,
			                coalesce(a.document_display_name, a."name") as documentName,
			                dd.for_date as date,
			                dd.lok_sabha_session as LSNo,
			                dd.lok_sabha_session_number as SSNo,
			                dd.rajya_sabha_session as RSSNo,
			                wh.status as status,
			                wh.pending_with_name as pendingAt,
			                null as refId,
			                dd.application_id as applicationId,
			                null as applicationinstanceId,
			                'IN-PROCESS' as taskName,
			                null as taskId,
			                'True' as actionPerformed,
			                a.form_url as formUrl,
			                wh.action_url as actionUrl,
			                wh.draft_id as draftId,
			                a.published_url as publishedUrl,
			                null as documentUUIDEn,
			                null as documentUUIDHi,
			                null as htmlContentEn,
			                null as htmlContentHi,
			                null as editorId,
			                null as uploaded_document_uuid
			        from
			                workflow.workflow_history wh
			        left join workflow.drafted_document dd on
			                dd.id = wh.draft_id
			        left join master.applications a on
			                a.id = dd.application_id
			        left join authentication.base_user u on
			                u.id = wh.pending_with
			        where
			                wh.pending_with = :userId
			                AND wh.draft_id IS NOT NULL
			union
			        select
			                distinct on
			                (ai.application_no) ai.application_no as applicationNo,

			                case
			                	when (ai.translation_document_type_id is not null)
			        							then (select coalesce(app.document_display_name, app."name") from master.applications app
			        							where app.id = ai.translation_document_type_id)
			                	else coalesce(ai.document_display_name,  coalesce(a.document_display_name, a."name"))
			        		end
			        		as documentName,
			                ai.document_date as date,
			                ai.lok_sabha_session as LSNo,
			                ai.lok_sabha_session_number as SSNo,
			                ai.rajya_sabha_session as RSSNo,
			                ai.current_status as status,
			                null as pendingAt,
			                ai.ref_id as refId,
			                ai.application_id as applicationId,
			                ai.id as applicationinstanceId,
			                'IN-PROCESS' as taskName,
			                wh.next_task_id as taskId,
			                'False' as actionPerformed,
			                a.form_url as formUrl,
			                wh.action_url as actionUrl,
			                wh.draft_id as draftId,
			                a.published_url as publishedUrl,
			                ai.published_document_uuid_en as documentUUIDEn,
			                ai.published_document_uuid_hi as documentUUIDHi,
			                ai.html_content_en as htmlContentEn,
			                ai.html_content_hi as htmlContentHi,
			                null as editorId,
			                ai.uploaded_document_uuid as uploaded_document_uuid
			        from
			                workflow.workflow_history wh
			        join master.application_instance ai on
			                wh.application_instance_id = ai.id
			        join master.applications a on
			                ai.application_id = a.id
			        where
			                (ai.current_status = 'Approved'
			                        or ai.current_status = 'Published')
			                    and a.primary_office_id in (
			                    select
			                            office_id
			                    from
			                            authentication.user_roles
			                    where
			                            user_id = :userId and is_active = true and is_deleted = false) and wh.is_active = true and wh.is_deleted = false

			union
			    SELECT
			                    r.ref_no as applicationNo,
			                    'Synopsis of Debates' as documentName,
			                r.roster_date as date,
			                lsm.lok_sabha_number as LSNo,
			                lssm.session_number as SSNo,
			                rssm.session_number as RSSNo,
			                'In Progress' as status,
			          		CONCAT(bu.first_name, ' ', bu.last_name, '-', d.abbr, ' (', o.office_name, ')') as pendingAt,
			                null as refId,
			                null as applicationId,
			                null as applicationinstanceId,
			                'IN-PROCESS' as taskName,
			                null as taskId,
			                'True' as actionPerformed,
			                '/synopsis/synopsis-create' as formUrl,
			                '/synopsis/synopsis-create' as actionUrl,
			                null as draftId,
			                null as publishedUrl,
			                null as documentUUIDEn,
			                null as documentUUIDHi,
			                null as htmlContentEn,
			                null as htmlContentHi,
			                CAST(rd.editor_id AS VARCHAR) as editorId,
			                null as uploaded_document_uuid
			FROM synopsis.roster_detail rd
			JOIN synopsis.roster r ON rd.roster_id = r.id
			left join master.lok_sabha_masters lsm on lsm.id = r.lok_sabha_no
			left join master.lok_sabha_session_masters lssm on lssm.id = r.lok_sabha_session_no
			left join master.rajya_sabha_session_master rssm on rssm.id = r.rajya_sabha_session_no
			left join authentication.base_user bu on bu.id = rd.editor_id

			left join master.designations d on bu.designation_id = d.id
				left join authentication.user_roles ur on bu.id = ur.user_id
				LEFT JOIN authentication.roles ar ON ur.role_id = ar.id
				LEFT JOIN master.offices o ON o.id = ur.office_id

			WHERE rd.editor_id = :userId
			AND r.roster_date IS NOT NULL
			AND r.roster_type = 'Daily'
			and rd.task_status <> 'Completed'
			         GROUP BY r.ref_no, r.roster_date, rd.editor_id, lsm.lok_sabha_number, lssm.session_number, rssm.session_number,
			        rd.supplementary_flag, bu.first_name, bu.last_name, d.abbr, o.office_name

			union
			                     SELECT
			                  ah.ref_no as applicationNo,
			                  ma.document_display_name as documentName,
			                  inp.document_date as date,
			                  lsm.lok_sabha_number as LSNo,
			                  lssm.session_number as SSNo,
			                  rssm.session_number as RSSNo,
			        				  case
								  	when (inp.status = 'Returned') then inp.status
								  	else 'In Progress'
							  end
			                  as status,
			          CONCAT(bu.first_name, ' ', bu.last_name, '-', d.abbr, ' (', o.office_name, ')') as pendingAt,
			          null as refId,
			          null as applicationId,
			          null as applicationinstanceId,
			          'IN-PROCESS' as taskName,
			          null as taskId,
			          'True' as actionPerformed,
			          '/translation-of-papers/createTranslation' as formUrl,
			          '/translation-of-papers/createTranslation' as actionUrl,
			              null as draftId,
			              null as publishedUrl,
			              null as documentUUIDEn,
			              null as documentUUIDHi,
			              null as htmlContentEn,
			              null as htmlContentHi,
			              CAST(ah.sent_to_user_id AS VARCHAR) as editorId,
			              null as uploaded_document_uuid
			      FROM translation.translation_input inp
			      JOIN translation.allocation_history ah ON inp.id = ah.translation_input_id
			      left join master.applications ma on ma.id = inp.document_type_id
			      left join master.lok_sabha_session_masters lssm on lssm.id = inp.lok_sabha_session_no
			      left join master.lok_sabha_masters lsm on lsm.id = inp.lok_sabha_no
			  left join master.rajya_sabha_session_master rssm on rssm.id = inp.rajya_sabha_session_no
			      left join authentication.base_user bu on bu.id = ah.sent_to_user_id

			      	left join master.designations d on bu.designation_id = d.id
			    	left join authentication.user_roles ur on bu.id = ur.user_id
					LEFT JOIN authentication.roles ar ON ur.role_id = ar.id
					LEFT JOIN master.offices o ON o.id = ur.office_id

			      WHERE ah.sent_to_user_id = :userId
			      AND inp.document_date IS NOT NULL
			      and ah.is_active = true
			      and ah.is_deleted = false

			    union

			SELECT
			            da.ref_no as applicationNo,
			            'Original Version of Debates' as documentName,
			        da.debate_date as date,
			        lsm.lok_sabha_number as LSNo,
			        lssm.session_number as SSNo,
			        rssm.session_number as RSSNo,
			        'In Progress' as status,
			        CONCAT(bu.first_name, ' ', bu.last_name) as pendingAt,
			        null as refId,
			        """;

	String allDocumentsQuery1 = """
				null as applicationinstanceId,
			        'IN-PROCESS' as taskName,
			        null as taskId,
			        'True' as actionPerformed,
			        '/corrected-debate-allocation/correct-debate-creation' as formUrl,
			        '/corrected-debate-allocation/correct-debate-creation' as actionUrl,
			        null as draftId,
			        null as publishedUrl,
			        null as documentUUIDEn,
			        null as documentUUIDHi,
			        null as htmlContentEn,
			        null as htmlContentHi,
			        CAST(da.allocateto AS VARCHAR) as editorId,
			        null as uploaded_document_uuid
			FROM corrected_debate.allocation da
			left join master.lok_sabha_masters lsm on lsm.id = da.lok_sabha_no
			left join master.lok_sabha_session_masters lssm on lssm.id = da.lok_sabha_session_no
			left join master.rajya_sabha_session_master rssm on rssm.id = da.rajya_sabha_session_no
			left join authentication.base_user bu on bu.id = da.allocateto
			WHERE da.allocateto = :userId
			and da.status <> 'Completed'

			union

			    SELECT
				            da.ref_no as applicationNo,
				            'Original Version of Debates' as documentName,
				    i.document_date as date,
				    lsm.lok_sabha_number as LSNo,
				    lssm.session_number as SSNo,
				    rssm.session_number as RSSNo,
					case
					  	when (da.status = 'Draft') then da.status
						when (da.status = 'Returned') then da.status
					  	else 'In Progress'
					end
					as status,
				    CONCAT(bu.first_name, ' ', bu.last_name) as pendingAt,
				    null as refId,
				    case
				   		when i."language" = 'EN' then (select a.id from master.applications a, master.modules m where a.abbr='CDE' and a.is_active = true and a.module_id = m.id and m.house_id = :houseId)
				   		when i."language" = 'HI' then (select a.id from master.applications a, master.modules m where a.abbr='CDH' and a.is_active = true and a.module_id = m.id and m.house_id = :houseId)
					end as applicationId,
				    null as applicationinstanceId,
				    'IN-PROCESS' as taskName,
				    null as taskId,
				    'True' as actionPerformed,
					case
				   		when i."language" = 'EN' then '/corrected-debate-translation/createTranslation'
				   		when i."language" = 'HI' then '/corrected-debate-translation/createTranslation?TranslationType=Hindi'
				    end as formUrl,
				    case
				   		when i."language" = 'EN' then '/corrected-debate-translation/createTranslation'
				   		when i."language" = 'HI' then '/corrected-debate-translation/createTranslation?TranslationType=Hindi'
				    end as actionUrl,
				    null as draftId,
				    null as publishedUrl,
				    null as documentUUIDEn,
				    null as documentUUIDHi,
				    null as htmlContentEn,
				    null as htmlContentHi,
				    CAST(da.sent_to_user_id AS VARCHAR) as editorId,
				    null as uploaded_document_uuid
				FROM corrected_debate.allocation_history da
				left join corrected_debate.translation_input i on i.id = da.translation_input_id
				left join master.lok_sabha_masters lsm on lsm.id = i.lok_sabha_no
				left join master.lok_sabha_session_masters lssm on lssm.id = i.lok_sabha_session_no
				left join master.rajya_sabha_session_master rssm on rssm.id = i.rajya_sabha_session_no
				left join authentication.base_user bu on bu.id = da.sent_to_user_id
				WHERE da.sent_to_user_id = :userId
				and da.status <> 'Completed'

			union
			-- ESummon
			SELECT
					       es.application_no as applicationNo,
					       'E-Summon for MPs' as documentName,
				       es.e_summon_issuance_date as date,
				       es.lok_sabha_no  as LSNo,
				       es.session_no as SSNo,
				       es.rajya_sabha_session_no  as RSSNo,
				       'Published' as status,
				       '' as pendingAt,
				       es.id as refId,
				       null as applicationId,
				       null as applicationinstanceId,
				       'IN-PROCESS' as taskName,
				       null as taskId,
				       'True' as actionPerformed,
				       '/workflow/mailsentinfo' as formUrl,
				       '/workflow/mailsentinfo' as actionUrl,
				       null as draftId,
				       null as publishedUrl,
				       null as documentUUIDEn,
				       null as documentUUIDHi,
				       null as htmlContentEn,
				       null as htmlContentHi,
				       null as editorId,
				       null as uploaded_document_uuid
				   FROM agenda_papers.e_summon es
				   where es.status = 'Published' and es.house_id = :houseId
				   and es.is_active = true and es.is_deleted = false
				and exists(select 1 from  authentication.user_roles ur  where ur.user_id = :userId
				and ur.office_id in (
				select  a.primary_office_id from master.applications a where a.abbr='ES'))

			union
			      select
			            distinct on
			    (ai.document_date, ai.application_id, rd.document_type) ai.application_no as applicationNo,
			            rd.document_type as documentName,
			            ai.document_date as date,
			            ai.lok_sabha_session as LSNo,
			            ai.lok_sabha_session_number as SSNo,
			            ai.rajya_sabha_session as RSSNo,
			            ai.current_status as status,
			            case
			                    when ai.current_status = 'Approved' then null
			                else CONCAT(wh.pending_with_name)
			        end as pendingAt,
			        ai.ref_id as refId,
			        ai.application_id as applicationId,
			        ai.id as applicationinstanceId,
			        'IN-PROCESS' as taskName,
			        wh.next_task_id as taskId,
			        'True' as actionPerformed,
			        a.form_url as formUrl,
			        wh.action_url as actionUrl,
			        wh.draft_id as draftId,
			        a.published_url as publishedUrl,
			        ai.published_document_uuid_en as documentUUIDEn,
			        ai.published_document_uuid_hi as documentUUIDHi,
			        ai.html_content_en as htmlContentEn,
			        ai.html_content_hi as htmlContentHi,
			        null as editorId,
			                null as uploaded_document_uuid
			from
			        workflow.workflow_history wh
			join master.application_instance ai on
			        wh.application_instance_id = ai.id
			join master.applications a on
			        ai.application_id = a.id
			join synopsis.synopsis s on s.id = ai.id
			join synopsis.roster_detail rd on rd.id = s.roster_detail_id
			where
			        wh.pending_with = :userId
			        and wh.action_taken_by is null
			and (ai.current_status != 'Approved' and ai.current_status != 'Published')
			 and a.id in (select a.id from master.applications a, master.modules m
			where a.module_id = m.id and a.abbr in ('SYN'))
			) A
			left join master.applications ma on
			        ma.id = A.applicationId

					""";

	private final static String orderByClause = """
			ORDER BY "date" DESC
			""";
	// @Override
	// public String startProcessInstance(String workflowId) {
	// ProcessInstance processInstance =
	// runtimeService.startProcessInstanceByKey(workflowId);
	// return processInstance.getProcessInstanceId();
	// }

	// @Override
	// public String getTaskIdByProcessInstanceId(String processInstanceId) {
	// Task task =
	// taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
	// return (task != null) ? task.getId() : null;
	// }
	@Override
	public String startProcessInstance(String workflowId) {
		String url = camundaRestUrl + "/process-definition/key/" + workflowId + "/start";
		Map<String, Object> requestBody = new HashMap<>();

		try {
			// Specify the type of response expected
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST,
					new HttpEntity<>(requestBody), new ParameterizedTypeReference<Map<String, Object>>() {
					});

			Map<String, Object> responseBody = response.getBody();
			return responseBody != null ? responseBody.get("id").toString() : null;

		} catch (HttpClientErrorException.NotFound e) {
			// Handle 404 Not Found
			String errorMessage = "No matching process definition found for workflow ID: " + workflowId;
			throw new CamundaException(errorMessage, e);

		} catch (HttpClientErrorException e) {
			String errorMessage = "Error while starting process instance: " + e.getResponseBodyAsString();
			throw new CamundaException(errorMessage, e);
		}
	}

	@Override
	public String getTaskIdByProcessInstanceId(String processInstanceId) {
		String url = camundaRestUrl + "/task?processInstanceId=" + processInstanceId;
		ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Map<String, Object>>>() {
				});
		List<Map<String, Object>> tasks = response.getBody();
		return (tasks != null && !tasks.isEmpty()) ? tasks.get(0).get("id").toString() : null;
	}

	// @Transactional
	// public String completeTask(String taskId, Map<String, Object> variables) {
	// // Fetch the task
	// Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
	// if (task != null) {
	// // Complete the task
	// taskService.complete(taskId, variables);
	//
	// // Create a new Notesheet entity using the mapper
	// Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId);
	// notesheetRepository.save(notesheet);
	// // Create and save ApplicationAuthority entity using the mapper
	// ApplicationAuthority applicationAuthority =
	// ApplicationAuthorityMapper.fromVariables(variables,
	// notesheet.getApplicationHistoryId());
	// applicationAuthorityRepository.save(applicationAuthority);
	//
	// // Create and save ApplicationHistory entity using the mapper
	// ApplicationHistory applicationHistory =
	// ApplicationHistoryMapper.fromVariables(variables, taskId);
	// applicationHistoryRepository.save(applicationHistory);
	//
	// // Fetch the next task ID if available
	// Task nextTask =
	// taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
	// String nextTaskId = (nextTask != null) ? nextTask.getId() : null;
	//
	// return nextTaskId;
	// } else {
	// return null;
	// }
	// }
	@Transactional
	public String completeTask(String taskId, Map<String, Object> variables,
			List<ProcessStepwiseDocumentsDTO> documentList, String actionUrl, String note) {
		try {
			System.out.println(">>> checkpoint 1");
			String url = camundaRestUrl + "/task/" + taskId + "/complete";
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(camundaAdminId, camundaAdminPassword);
			headers.setContentType(MediaType.APPLICATION_JSON);
			System.out.println(">>> checkpoint 2");

			// Convert variables to a format expected by Camunda REST API
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("variables", formatVariablesForCamunda(variables));
			System.out.println(">>> checkpoint 3");

			// Create an HttpEntity with headers and body
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
			System.out.println(">>> checkpoint 4");

			// Execute the REST API call to complete the task
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			System.out.println(">>> checkpoint 5");

			// Extract variables for further processing
			String userName = utils.getUserFullNameFromAuthentication();
			Long senderUserId = ((Number) variables.get("senderUserId")).longValue();
			Long receiverUserId = ((Number) variables.get("receiverUserId")).longValue();
			Long applicationInstanceId = ((Number) variables.get("applicationInstanceId")).longValue();
			String processInstanceId = (String) variables.get("processInstanceId");
			String application_no = (String) variables.get("application_no");
			String actionTaken = (String) variables.get("actionTaken");
			// String note = (String) variables.get("note");
			String statusKey = application_no + "." + actionTaken + ".PRESTATUS";
			String initalStatusKey = application_no + "." + actionTaken + ".POSTSTATUS";
			String receiverUserName = (String) variables.get("receiverUserName");
			String status = (String) variables.get("status");
			LocalDateTime actionTakenOn = LocalDateTime.now(); // Assuming the action is taken now

			// Fetch next task ID
			String nextTaskId = fetchNextTaskId(processInstanceId);

			// Check if the task was successfully completed
			if (response.getStatusCode().is2xxSuccessful()) {
				// Determine step type
				String stepType = (String) variables.get("stepType");

				if ("INIT".equals(stepType)) {
					// Entry to Notesheet Table
					try {
						Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId, documentList, note);
						notesheetRepository.save(notesheet);
					} catch (Exception e) {
						System.err.println("Unexpected error in INIT step: " + e.getMessage());
						e.printStackTrace();
						throw new RuntimeException("Unexpected error while processing INIT step", e);
					}

					System.out.println(">>> checkpoint 11");

					// First entry with status = "submitted"
					WorkflowHistory.WorkflowHistoryBuilder firstEntryBuilder = WorkflowHistory.builder()
							.processInstanceId(processInstanceId).subModuleId(application_no)
							.applicationInstanceId(applicationInstanceId).actionTakenBy(senderUserId)
							.actionTakenByName(userName).actionTaken(actionTaken).actionTakenOn(LocalDateTime.now())
							.status(status).statusKey(statusKey).actionUrl(actionUrl).pendingWith(receiverUserId)
							.pendingWithName(receiverUserName).pendingStartDate(LocalDateTime.now())
							.initalStatusKey(statusKey).note(note).currentTaskId(taskId);
					System.out.println(">>> checkpoint 12");

					// Second entry, for the next task or status
					WorkflowHistory.WorkflowHistoryBuilder secondEntryBuilder = WorkflowHistory.builder()
							.processInstanceId(processInstanceId).subModuleId(application_no)
							.applicationInstanceId(applicationInstanceId).actionTaken(null).actionTakenBy(null)
							.actionTakenByName(null).actionTakenOn(null).status(null).statusKey(null)
							.pendingWith(receiverUserId).pendingWithName(receiverUserName)
							.pendingStartDate(LocalDateTime.now()).initalStatusKey(initalStatusKey).note(note)
							.nextTaskId(nextTaskId).actionUrl(actionUrl);
					System.out.println(">>> checkpoint 13");

					// Entry to Translation Inputs
					TranslationInputDTO translationInputDTO = getTranslationInputDTO(applicationInstanceId);
					if (Boolean.TRUE.equals(translationInputDTO.getTranslationFlag())) {
						try {
							callTranslationAPI(translationInputDTO);
							secondEntryBuilder.translationSuccess(true);
							firstEntryBuilder.translationSuccess(true);

						} catch (Exception e) {
							// Log and continue
							secondEntryBuilder.translationSuccess(false);
							firstEntryBuilder.translationSuccess(false);
							e.printStackTrace();

						}
					}

					System.out.println(">>> checkpoint 14");

					WorkflowHistory firstEntry = firstEntryBuilder.build();
					workflowHistoryRepository.save(firstEntry);

					System.out.println(">>> checkpoint 15");

					WorkflowHistory secondEntry = secondEntryBuilder.build();
					workflowHistoryRepository.save(secondEntry);

					System.out.println(">>> checkpoint 16");

				} else if ("INTER".equals(stepType)) {
					updateApplicationInstance(applicationInstanceId, processInstanceId, taskId, status);

					// Entry to Notesheet Table
					Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId, documentList, note);
					notesheetRepository.save(notesheet);

					workflowHistoryRepository.updateWorkflowHistoryForStepType(processInstanceId, applicationInstanceId,
							senderUserId, userName, actionTakenOn, status, statusKey, taskId, receiverUserId,
							receiverUserName);

					WorkflowHistory entry = WorkflowHistory.builder().processInstanceId(processInstanceId)
							.subModuleId(application_no).applicationInstanceId(applicationInstanceId)
							.actionTakenBy(null).actionTakenByName(null).actionTakenOn(null).status(null)
							.statusKey(null).pendingWith(receiverUserId).pendingWithName(receiverUserName)
							.pendingStartDate(LocalDateTime.now()).initalStatusKey(initalStatusKey).note(note)
							.nextTaskId(nextTaskId).actionUrl(actionUrl).build();
					workflowHistoryRepository.save(entry);

				} else if ("FINAL".equals(stepType)) {
					updateApplicationInstance(applicationInstanceId, processInstanceId, taskId, status);

					// Entry to Notesheet Table
					Notesheet notesheet = NotesheetMapper.fromVariables(variables, taskId, documentList, note);
					notesheetRepository.save(notesheet);

					// Update existing workflow history for FINAL step
					workflowHistoryRepository.updateWorkflowHistoryForFinalStepType(processInstanceId,
							applicationInstanceId, senderUserId, userName, actionTakenOn, status, statusKey, taskId);

					// Translation API
					TranslationInputDTO translationInputDTO = getTranslationInputDTO(applicationInstanceId);
					if (Boolean.TRUE.equals(translationInputDTO.getTranslationFlag())) {
						try {
							callTranslationAPI(translationInputDTO);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					// Section Bulletin 2
					SectionBulletin2AppHouseDTO sectionBulletin2AppHouseDTO = getSectionB2AppHouse(
							applicationInstanceId);
					System.out.println(">>> sectionBulletin2AppHouseDTO - " + sectionBulletin2AppHouseDTO);

					if (sectionBulletin2AppHouseDTO != null
							&& BULLETIN_2_RS.equals(sectionBulletin2AppHouseDTO.getApplicationName())
							&& RS_HOUSE.equals(sectionBulletin2AppHouseDTO.getHouse())) {
						try {
							List<SectionBulletin2DTO> sectionBulletin2DTOs = getSectionBulletin2Data(
									applicationInstanceId);
							System.out.println(">>> sectionBulletin2DTOs - " + sectionBulletin2DTOs);
							if (!sectionBulletin2DTOs.isEmpty()) {
								callSectionB2RS(sectionBulletin2DTOs);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					// Corrected Debates API
					String appAbbr = workflowHistoryRepository.getApplicationAbbr(convertToLong(application_no));
					if (CORRECTED_DEBATES_ABBR.equals(appAbbr)) {
						CorrectedDebatedDTO correctedDebatedDTO = getCorrectedDebatedDTO(applicationInstanceId);
						DebateTaggingDTO debateTaggingDTO = getDebatetaggingDTO(applicationInstanceId);

						try {
							callCorrectedDebatesAPI(correctedDebatedDTO);

							try {
								// Calling Debate Tagging for DTAG
								debateTaggingDTO.setApplicationId(80L);
								callDebateTagging(debateTaggingDTO);
							} catch (Exception e) {
								System.err.println("Error in first call to callDebateTagging (DTAG):");
								e.printStackTrace();
							}

							try {
								// Calling Debate Tagging for Supplementary Tagging
								debateTaggingDTO.setApplicationId(81L);
								callDebateTagging(debateTaggingDTO);
							} catch (Exception e) {
								System.err.println("Error in second call to callDebateTagging (Supplementary):");
								e.printStackTrace();
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

//					// B2 - INPUT API for RS
//					if (B2_ABBR.equals(appAbbr) && "RS".equals(utils.getHouseAbbr())) {
//						callB2InputAPI();
//					}	

				}

				// Return next task ID or end message
				if (nextTaskId == null) {
					return "Workflow has ended";
				}
				return nextTaskId;

			} else {
				throw new RuntimeException("Failed to complete task with Camunda API");
			}

		} catch (

		Exception e) {
			e.printStackTrace();
			throw new CamundaException("Error completing task with external Camunda API: " + e.getMessage(), e);
		}
	}

//	/**
//	 * 
//	 */
//	private void callB2InputAPI() {
//		
//		CorrectedDebatedDTO correctedDebatedDTO = getCorrectedDebatedDTO(applicationInstanceId);
//		DebateTaggingDTO debateTaggingDTO = getDebatetaggingDTO(applicationInstanceId);
//
//		try {
//			callCorrectedDebatesAPI(correctedDebatedDTO);
//
//			try {
//				// Calling Debate Tagging for DTAG
//				debateTaggingDTO.setApplicationId(80L);
//				callDebateTagging(debateTaggingDTO);
//			} catch (Exception e) {
//				System.err.println("Error in first call to callDebateTagging (DTAG):");
//				e.printStackTrace();
//			}
//
//			try {
//				// Calling Debate Tagging for Supplementary Tagging
//				debateTaggingDTO.setApplicationId(81L);
//				callDebateTagging(debateTaggingDTO);
//			} catch (Exception e) {
//				System.err.println("Error in second call to callDebateTagging (Supplementary):");
//				e.printStackTrace();
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	private SectionBulletin2AppHouseDTO getSectionB2AppHouse(Long applicationInstanceId) {
		List<Object[]> result = applicationInstanceRepository.getSectionB2AppHouse(applicationInstanceId);

		if (result.isEmpty()) {
			return null;
		}

		Object[] row = result.get(0);

		return SectionBulletin2AppHouseDTO.builder().applicationName((String) row[0]).house((String) row[1]).build();
	}

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	private List<SectionBulletin2DTO> getSectionBulletin2Data(Long applicationInstanceId) {
		List<Object[]> result = applicationInstanceRepository
				.getSectionBulletin2ByApplicationInstanceId(applicationInstanceId);

		if (result.isEmpty()) {
			return List.of();
		}

		return result.stream()
				.map(row -> SectionBulletin2DTO.builder().rajyaSabhaSessionNo((String) row[0])
						.documentTypeId(convertToLong(row[1])).documentDate(convertToLocalDate(row[2]))
						.inputContent((String) row[3]).translatedContent(null) // always null initially
						.subject((String) row[4]).officeName("Legislative Branch") // fixed value
						.bulletinNo(convertToInteger(row[5])).build())
				.toList();
	}

	private Integer convertToInteger(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Integer) {
			return (Integer) obj;
		}
		if (obj instanceof Number) {
			return ((Number) obj).intValue();
		}
		try {
			return Integer.valueOf(obj.toString());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Cannot convert to Integer: " + obj, e);
		}
	}

	/**
	 * @param debateTaggingDTO
	 */
	private void callDebateTagging(DebateTaggingDTO debateTaggingDTO) {
		try {
			String token = getTokenFromRequest();

			// Step 2: Call translation API
			HttpHeaders jsonHeaders = new HttpHeaders();
			jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
			jsonHeaders.setBearerAuth(token);
			HttpEntity<DebateTaggingDTO> jsonRequest = new HttpEntity<>(debateTaggingDTO, jsonHeaders);
			ResponseEntity<String> response = restTemplate.postForEntity(debateTaggingUrl, jsonRequest, String.class);

			System.out.println("### Debate tagging API Response: " + response.getBody());

		} catch (HttpClientErrorException e) {
			System.err.println("HTTP error during Debates Tagging API call: " + e.getResponseBodyAsString());
			throw new RuntimeException("Error calling Debates Tagging API: " + e.getStatusCode(), e);
		} catch (Exception e) {
			System.err.println("Unexpected error during debates tagging process.");
			e.printStackTrace();
			throw new RuntimeException("Debates Tagging API call failed.", e);
		}
	}

	private void callSectionB2RS(List<SectionBulletin2DTO> sectionBulletin2DTOs) {
		try {
			String token = getTokenFromRequest();

			HttpHeaders jsonHeaders = new HttpHeaders();
			jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
			jsonHeaders.setBearerAuth(token);

			HttpEntity<List<SectionBulletin2DTO>> jsonRequest = new HttpEntity<>(sectionBulletin2DTOs, jsonHeaders);

			ResponseEntity<String> response = restTemplate.postForEntity(sectionB2Url, jsonRequest, String.class);

			System.out.println("### Section B2 API Response: " + response.getBody());

		} catch (HttpClientErrorException e) {
			System.err.println("HTTP error during Section B2 API call: " + e.getResponseBodyAsString());
			throw new RuntimeException("Error calling Section B2 API: " + e.getStatusCode(), e);
		} catch (Exception e) {
			System.err.println("Unexpected error during Section B2 process.");
			e.printStackTrace();
			throw new RuntimeException("Section B2 API call failed.", e);
		}
	}

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	private DebateTaggingDTO getDebatetaggingDTO(Long applicationInstanceId) {
		List<Object[]> result = applicationInstanceRepository
				.getDebateTaggingByApplicationInstanceId(applicationInstanceId);

		if (result.isEmpty()) {
			return null;
		}

		Object[] row = result.get(0);

		return DebateTaggingDTO.builder().lokSabhaNo(convertToLong(row[0])).lokSabhaSessionNo(convertToLong(row[1]))
				.rajyaSabhaSessionNo(convertToLong(row[2])).debateDate(convertToLocalDate(row[3]))
				.content((String) row[4]).status("DRAFT").build();
	}

	/**
	 * @param applicationInstanceId
	 * @return
	 */
	private CorrectedDebatedDTO getCorrectedDebatedDTO(Long applicationInstanceId) {
		List<Object[]> result = applicationInstanceRepository
				.getCorrectedDebatesByApplicationInstanceId(applicationInstanceId);

		if (result.isEmpty()) {
			return null;
		}

		Object[] row = result.get(0);

		return CorrectedDebatedDTO.builder().lokSabhaNo(convertToLong(row[0])).lokSabhaSessionNo(convertToLong(row[1]))
				.rajyaSabhaSessionNo(convertToLong(row[2])).documentTypeId(convertToLong(row[3]))
				.documentDate(convertToLocalDate(row[4])) // updated line
				.inputContent((String) row[5]).receivedDate(LocalDate.now()).build();
	}

	public TranslationInputDTO getTranslationInputDTO(Long applicationInstanceId) {
		System.out.println("### here");
		List<Object[]> result = applicationInstanceRepository
				.getTranslationDataByApplicationInstanceId(applicationInstanceId);

		if (result.isEmpty()) {
			return null;
		}

		Object[] row = result.get(0);

		return TranslationInputDTO.builder().lokSabhaNo(convertToLong(row[0])).lokSabhaSessionNo(convertToLong(row[1]))
				.rajyaSabhaSessionNo(convertToLong(row[2])).documentTypeId(convertToLong(row[3]))
				.documentDate(convertToLocalDate(row[4])) // updated line
				.inputContent((String) row[5]).translationFlag(row[6] != null ? (Boolean) row[6] : null)
				.contentUUID(convertToString(row[7]) != null ? UUID.fromString(convertToString(row[7])) : null)
				.build(); 
	}

	private LocalDate convertToLocalDate(Object dateObj) {
		if (dateObj instanceof java.sql.Date) {
			return ((java.sql.Date) dateObj).toLocalDate();
		} else if (dateObj instanceof java.util.Date) {
			return ((java.util.Date) dateObj).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		} else if (dateObj instanceof LocalDate) {
			return (LocalDate) dateObj;
		} else {
			return null;
		}
	}

	private Long convertToLong(Object obj) {
		if (obj instanceof Number) {
			return ((Number) obj).longValue();
		}
		if (obj instanceof String) {
			try {
				return Long.parseLong((String) obj);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	private String convertToString(Object obj) {
		if (obj instanceof String) {
			return obj.toString();
		}
		return null;
	}

	
	public void callTranslationAPI(TranslationInputDTO translationInputDTO) {
		try {
			
			String token = getTokenFromRequest(); // Ensure this method is implemented properly

			// Upload to OKM is HTML. If OnlyOffice, then no need to upload
			
			if (translationInputDTO.getContentUUID() != null) {
			
				// Step 1: Call upload-html-english API
				HttpHeaders formHeaders = new HttpHeaders();
				formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				formHeaders.setBearerAuth(token);
	
				// Check if startingPageNo is null, if so set to "1"
				String startingPageNo = (translationInputDTO.getStartingPageNo() != null)
						? translationInputDTO.getStartingPageNo().toString()
						: "1";
	
				MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
				formParams.add("htmlContentEn", translationInputDTO.getInputContent());
				formParams.add("startingPageNo", startingPageNo);
				formParams.add("applicationId", translationInputDTO.getDocumentTypeId().toString());
				formParams.add("locale", "en");
				
				System.out.println("PARAMS: " + formParams);
	
				HttpEntity<MultiValueMap<String, String>> formRequest = new HttpEntity<>(formParams, formHeaders);
	
				System.out.println(">>> upload failing - ");
				ResponseEntity<String> uploadResponse = restTemplate.postForEntity(pdfUploadUrl, formRequest, String.class);
	
				System.out.println("### upload completed");
				// Parse documentUuid from response
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode responseJson = objectMapper.readTree(uploadResponse.getBody());
	
				if (!responseJson.has("documentUuid") || responseJson.get("documentUuid").isNull()) {
					throw new RuntimeException("documentUuid not found in upload-html-english response");
				}
	
				UUID documentUuid = UUID.fromString(responseJson.get("documentUuid").asText());
	
				System.out.println("### doc - " + documentUuid);
	
				translationInputDTO.setContentUUID(documentUuid); // directly set it
			}
			
			// Step 2: Call translation API
			HttpHeaders jsonHeaders = new HttpHeaders();
			jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
			jsonHeaders.setBearerAuth(token);

			HttpEntity<TranslationInputDTO> jsonRequest = new HttpEntity<>(translationInputDTO, jsonHeaders);
			ResponseEntity<String> translationResponse = restTemplate.postForEntity(translationInputUrl, jsonRequest,
					String.class);

			System.out.println("### Translation API Response: " + translationResponse.getBody());

		} catch (HttpClientErrorException e) {
			System.err.println("HTTP error during Translation API call: " + e.getResponseBodyAsString());
			throw new RuntimeException("Error calling Translation API: " + e.getStatusCode(), e);
		} catch (Exception e) {
			System.err.println("Unexpected error during translation process.");
			e.printStackTrace();
			throw new RuntimeException("Translation API call failed.", e);
		}
	}

	public void callCorrectedDebatesAPI(CorrectedDebatedDTO correctedDebatedDTO) {
		try {
			String token = getTokenFromRequest(); // Ensure this method is implemented properly

			// Step 1: Call upload-html-english API
			HttpHeaders formHeaders = new HttpHeaders();
			formHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			formHeaders.setBearerAuth(token);

			// Check if startingPageNo is null, if so set to "1"
			String startingPageNo = "1";

			MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
			formParams.add("htmlContentEn", correctedDebatedDTO.getInputContent());
			formParams.add("startingPageNo", startingPageNo);
			formParams.add("applicationId", correctedDebatedDTO.getDocumentTypeId().toString());
			formParams.add("locale", "en");
			formParams.add("applicationAbbr", CORRECTED_DEBATES_ABBR);
//		    formParams.add("fileName", generateFileName(esummon, member, locale));

			HttpEntity<MultiValueMap<String, String>> formRequest = new HttpEntity<>(formParams, formHeaders);

			System.out.println(">>> calling");
			ResponseEntity<String> uploadResponse = restTemplate.postForEntity(pdfUploadUrl, formRequest, String.class);

			System.out.println(">>> success");
			// Parse documentUuid from response
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode responseJson = objectMapper.readTree(uploadResponse.getBody());

			if (!responseJson.has("documentUuid") || responseJson.get("documentUuid").isNull()) {
				throw new RuntimeException("documentUuid not found in upload-html-english response");
			}

			UUID documentUuid = UUID.fromString(responseJson.get("documentUuid").asText());

			correctedDebatedDTO.setContentUUID(documentUuid.toString()); // directly set it

			// Step 2: Call translation API
			HttpHeaders jsonHeaders = new HttpHeaders();
			jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
			jsonHeaders.setBearerAuth(token);
			System.out.println("### correctedDebateUrl - " + correctedDebateUrl);
			HttpEntity<CorrectedDebatedDTO> jsonRequest = new HttpEntity<>(correctedDebatedDTO, jsonHeaders);
			ResponseEntity<String> correctedResponse = restTemplate.postForEntity(correctedDebateUrl, jsonRequest,
					String.class);

			System.out.println("### Corrected Debates API Response: " + correctedResponse.getBody());

		} catch (HttpClientErrorException e) {
			System.err.println("HTTP error during Corrected Debates API call: " + e.getResponseBodyAsString());
			throw new RuntimeException("Error calling Corrected Debates API: " + e.getStatusCode(), e);
		} catch (Exception e) {
			System.err.println("Unexpected error during corrected debates process.");
			e.printStackTrace();
			throw new RuntimeException("Corrected Debates API call failed.", e);
		}
	}

	// New method to fetch the next task ID using the process instance ID
	private String fetchNextTaskId(String processInstanceId) {
		// Construct the query URL for tasks by process instance ID
		String queryUrl = camundaRestUrl + "/task?processInstanceId=" + processInstanceId;

		// Execute the REST API call to get tasks
		ResponseEntity<List<TaskDto>> tasksResponse = restTemplate.exchange(queryUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<TaskDto>>() {
				});

		List<TaskDto> tasks = tasksResponse.getBody();
		if (tasks != null && !tasks.isEmpty()) {
			// Return the ID of the first task in the list, which is considered the next
			// task
			return tasks.get(0).getId();
		}
		return null;
	}

	// Helper method to format the variables as Camunda expects
	private Map<String, Object> formatVariablesForCamunda(Map<String, Object> variables) {
		Map<String, Object> camundaVariables = new HashMap<>();

		System.out.println(">>> formatVariablesForCamunda");
		variables.forEach((key, value) -> {
			if (value instanceof Map) {
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					// Serialize the map to a JSON string
					String jsonValue = objectMapper.writeValueAsString(value);
					Map<String, Object> variableMap = new HashMap<>();
					variableMap.put("value", jsonValue);
					variableMap.put("type", "Json");
					camundaVariables.put(key, variableMap);
				} catch (JsonProcessingException e) {
					throw new RuntimeException("Error serializing map to JSON: " + key, e);
				}
			} else {
				Map<String, Object> variableMap = new HashMap<>();
				variableMap.put("value", value);
				variableMap.put("type", value.getClass().getSimpleName());
				camundaVariables.put(key, variableMap);
			}
		});
		System.out.println(">>> formatVariablesForCamunda completed");

		return camundaVariables;
	}
	// public List<TaskResponseDTO> getTasksByReceiverUserId(Long receiverUserId) {
	// try {
	// // Step 1: Retrieve tasks filtered by receiverUserId
	// String queryUrl = camundaRestUrl +
	// "/task?processVariableName=receiverUserId&processVariableValue="
	// + receiverUserId;
	// ResponseEntity<List<TaskDto>> response = restTemplate.exchange(queryUrl,
	// HttpMethod.GET, null,
	// new ParameterizedTypeReference<List<TaskDto>>() {
	// });
	//
	// // Check if tasks were successfully retrieved
	// if (response.getStatusCode().is2xxSuccessful()) {
	// List<TaskDto> taskDtos = response.getBody() != null ? response.getBody() :
	// Collections.emptyList();
	//
	// // Step 2: Filter tasks to include only those where receiverUserId matches
	// List<TaskResponseDTO> filteredTasks = taskDtos.stream().filter(taskDto -> {
	// // Retrieve the actual receiverUserId variable for the task
	// String variableUrl = camundaRestUrl + "/task/" + taskDto.getId() +
	// "/variables/receiverUserId";
	// ResponseEntity<TaskVariableDto> variableResponse =
	// restTemplate.getForEntity(variableUrl,
	// TaskVariableDto.class);
	//
	// // Verify that receiverUserId matches
	// return variableResponse.getStatusCode().is2xxSuccessful() &&
	// variableResponse.getBody() != null
	// && receiverUserId.equals(variableResponse.getBody().getValue());
	// }).map(taskDto -> {
	// // Step 3: Fetch applicationInstanceId variable if receiverUserId matches
	// String appInstanceUrl = camundaRestUrl + "/task/" + taskDto.getId()
	// + "/variables/applicationInstanceId";
	// ResponseEntity<TaskVariableDto> appInstanceResponse =
	// restTemplate.getForEntity(appInstanceUrl,
	// TaskVariableDto.class);
	//
	// Long applicationInstanceId = null;
	// if (appInstanceResponse.getStatusCode().is2xxSuccessful()
	// && appInstanceResponse.getBody() != null) {
	// applicationInstanceId = appInstanceResponse.getBody().getValue();
	// }
	//
	// return new TaskResponseDTO(taskDto.getId(), taskDto.getName(),
	// applicationInstanceId);
	// }).collect(Collectors.toList());
	//
	// // Step 4: Check if any tasks were found; if not, throw a custom exception
	// if (filteredTasks.isEmpty()) {
	// throw new ResourceNotFoundException(
	// "No pending tasks found for the provided receiverUserId: " + receiverUserId);
	// }
	//
	// return filteredTasks;
	// } else {
	// throw new ResourceNotFoundException("Failed to retrieve tasks: " +
	// response.getStatusCode());
	// }
	// } catch (ResourceNotFoundException e) {
	// throw e; // Preserve custom exception for resource not found
	// } catch (Exception e) {
	// throw new RuntimeException("An unexpected error occurred while retrieving
	// tasks: " + e.getMessage(), e);
	// }
	// }

	public List<TaskResponseDTO> getTasksByReceiverUserId(Long receiverUserId) {
		try {
			// Step 1: Retrieve tasks filtered by receiverUserId
			String queryUrl = camundaRestUrl + "/task?processVariableName=receiverUserId&processVariableValue="
					+ receiverUserId;
			ResponseEntity<List<TaskDto>> response = restTemplate.exchange(queryUrl, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<TaskDto>>() {
					});

			if (response.getStatusCode().is2xxSuccessful()) {
				List<TaskDto> taskDtos = response.getBody() != null ? response.getBody() : Collections.emptyList();
				List<TaskResponseDTO> filteredTasks = new ArrayList<>();

				for (TaskDto taskDto : taskDtos) {
					String variableUrl = camundaRestUrl + "/task/" + taskDto.getId() + "/variables/receiverUserId";
					ResponseEntity<TaskVariableDto> variableResponse;

					try {
						// Attempt to fetch receiverUserId
						variableResponse = restTemplate.getForEntity(variableUrl, TaskVariableDto.class);

						if (variableResponse.getStatusCode().is2xxSuccessful() && variableResponse.getBody() != null
								&& receiverUserId.equals(variableResponse.getBody().getValue())) {

							// Step 3: Fetch applicationInstanceId variable
							String appInstanceUrl = camundaRestUrl + "/task/" + taskDto.getId()
									+ "/variables/applicationInstanceId";
							ResponseEntity<TaskVariableDto> appInstanceResponse = restTemplate
									.getForEntity(appInstanceUrl, TaskVariableDto.class);

							Long applicationInstanceId = null;
							if (appInstanceResponse.getStatusCode().is2xxSuccessful()
									&& appInstanceResponse.getBody() != null) {
								applicationInstanceId = appInstanceResponse.getBody().getValue();
							}

							String processInstanceId = taskDto.getProcessInstanceId();

							// Step 4: Fetch task details from the repository
							List<Object[]> taskDetails = taskRepository
									.findTasksWithDetailsByReceiverUserIdAndApplicationInstanceId(receiverUserId,
											applicationInstanceId);

							if (!taskDetails.isEmpty()) {
								Object[] details = taskDetails.get(0);

								TaskResponseDTO taskResponseDTO = TaskResponseDTO.builder()
										.applicationNo((String) details[0]).documentName((String) details[1])
										.date((Date) details[2]).status((String) details[3])
										.pendingAt((String) details[4]).lsNo((String) details[5])
										.ssNo((String) details[6]).refId((Long) details[7])
										.applicationId((Long) details[8]).taskId(taskDto.getId())
										.taskName(taskDto.getName()).processInstanceId(processInstanceId) // Add
										// processInstanceId
										.applicationInstanceId(applicationInstanceId).build();
								filteredTasks.add(taskResponseDTO);
							} else {
								throw new TaskNotFoundException("No task details found for receiverUserId: "
										+ receiverUserId + " and applicationInstanceId: " + applicationInstanceId);
							}
						}
					} catch (HttpClientErrorException.NotFound ex) {
						// Handle the case where receiverUserId is missing for this task
						continue; // Skip to the next task
					}
				}

				if (filteredTasks.isEmpty()) {
					throw new TaskNotFoundException(
							"No pending tasks found for the provided receiverUserId: " + receiverUserId);
				}

				return filteredTasks;
			} else {
				throw new ResourceNotFoundException("Failed to retrieve tasks: " + response.getStatusCode());
			}
		} catch (TaskNotFoundException e) {
			throw e; // Preserve custom exception for resource not found
		} catch (Exception e) {
			throw new RuntimeException("An unexpected error occurred while retrieving tasks: " + e.getMessage(), e);
		}
	}

	// private void logTaskHistory(String taskId, String processInstanceId,
	// Map<String, Object> variables,
	// String nextTaskId) {
	// TaskHistory taskHistory = new TaskHistory();
	// taskHistory.setTaskId(taskId);
	// taskHistory.setProcessInstanceId(processInstanceId);
	// taskHistory.setUserId((String) variables.get("userId"));
	// taskHistory.setUserName((String) variables.get("userName"));
	// taskHistory.setAction((String) variables.get("sendTo"));
	// taskHistory.setComments((String) variables.get("comments"));
	//// taskHistory.setDocumentTitle((String) variables.get("documentTitle"));
	//// taskHistory.setDocDesc((String) variables.get("docDesc"));
	// taskHistory.setTimestamp(new Date());
	// taskHistory.setStatus("Completed");
	// taskHistory.setNextTaskId(nextTaskId);
	//
	// taskHistoryRepository.save(taskHistory);
	// }

	// public List<HistoricTaskInstance> getHistoricTasksByTaskId(String taskId) {
	// HistoricTaskInstanceQuery query =
	// historyService.createHistoricTaskInstanceQuery();
	//
	// if (taskId != null) {
	// query.taskId(taskId);
	// }
	//
	// return query.list();
	// }

	public List<TaskHistoryDTO> getTasksByProcessInstanceId(String processInstanceId) {
		try {
			// Construct the URL to fetch historic tasks for the specified process instance
			// ID
			String url = camundaRestUrl + "/history/task?processInstanceId=" + processInstanceId;

			// Create headers for basic authentication
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(camundaAdminId, camundaAdminPassword);
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Create an HttpEntity with headers
			HttpEntity<Void> entity = new HttpEntity<>(headers);

			// Make the GET request to Camunda's API
			ResponseEntity<List<HistoricTaskInstanceDto>> response = restTemplate.exchange(url, HttpMethod.GET, entity,
					new ParameterizedTypeReference<List<HistoricTaskInstanceDto>>() {
					});

			// Convert the response body to List<TaskHistoryDTO>
			if (response.getBody() != null) {
				return response.getBody().stream().map(workflowMapper::mapToTaskHistoryDTO)
						.collect(Collectors.toList());
			} else {
				return Collections.emptyList(); // Return an empty list if no tasks are found
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to retrieve task history: " + e.getMessage(), e);
		}
	}

	@Override
	public void saveNotesheet(NotesheetDTO notesheetDTO, String processInstanceId, String taskId) {
		// TODO Auto-generated method stub

	}

	public void updateApplicationInstance(long applicationInstanceId, String processInstanceId, String taskId,
			String status) {
		// Retrieve the application instance by ID
		Optional<ApplicationInstance> optionalInstance = applicationInstanceRepository.findById(applicationInstanceId);

		if (optionalInstance.isPresent()) {
			ApplicationInstance applicationInstance = optionalInstance.get();
			applicationInstance.setProcessInstanceId(processInstanceId);
			applicationInstance.setTaskId(taskId);
			applicationInstance.setCurrentStatus(status);
			// Save the updated instance
			applicationInstanceRepository.save(applicationInstance);
		} else {
			throw new ResourceNotFoundException("ApplicationInstance with ID " + applicationInstanceId + " not found.");
		}
	}

	@Override
	public WorkflowHistory getMostRecentWorkflowHistoryBySubModuleIdAndActionTakenBy(String subModuleId, Long userId) {
		return workflowHistoryRepository.findTopBySubModuleIdAndActionTakenByOrderByCreatedAtDesc(subModuleId, userId);
	}

	@Override
	public List<WorkflowHistory> getWorkflowHistoryBySubModuleIdAndActionTakenBy(String subModuleId,
			Long actionTakenBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WorkflowHistory> getWorkflowHistoryByApplicationInstanceId(Long applicationInstanceId) {
		return workflowHistoryRepository.findByApplicationInstanceId(applicationInstanceId);
	}

	public void updateApplicationInstance(NotesheetDTO notesheetDTO, String processInstanceId, String taskId) {
		// Retrieve the application instance by ID
		Optional<ApplicationInstance> optionalInstance = applicationInstanceRepository
				.findById(notesheetDTO.getApplicationInstanceId());

		if (optionalInstance.isPresent()) {
			ApplicationInstance applicationInstance = optionalInstance.get();

			// Update fields
			applicationInstance.setProcessInstanceId(processInstanceId);
			applicationInstance.setTaskId(taskId);
			applicationInstance.setCurrentStatus("In Progress"); // Hardcoding to "Initiated"

			// Save the updated instance
			applicationInstanceRepository.save(applicationInstance);
		} else {
			throw new ResourceNotFoundException(
					"ApplicationInstance with ID " + notesheetDTO.getApplicationInstanceId() + " not found.");
		}
	}

	@Override
	public List<Map<String, Object>> getallDocumentsTab(Long userId) {
		return workflowHistoryRepository.findAllDocumentsForUser(userId).stream().map(row -> {
			Map<String, Object> rowMap = new HashMap<>();
			rowMap.put("applicationNo", row[0]);
			rowMap.put("documentName", row[1]);
			rowMap.put("date", row[2] != null ? row[2].toString() : null); // Handle NULL date
			rowMap.put("lsNo", row[3]);
			rowMap.put("ssNo", row[4]);
			rowMap.put("rssNo", row[5]);
			rowMap.put("status", row[6]);
			rowMap.put("pendingAt", row[7]);
			rowMap.put("refId", row[8]);
			rowMap.put("applicationId", row[9]);
			rowMap.put("applicationInstanceId", row[10]);
			rowMap.put("taskName", row[11]);
			rowMap.put("taskId", row[12]);
			rowMap.put("actionPerformed", row[13]);
			rowMap.put("formUrl", row[14]);
			rowMap.put("actionUrl", row[15]);
			rowMap.put("translateFlag", row[16]);
			rowMap.put("downloadFlag", row[17]);

			return rowMap;
		}).collect(Collectors.toList());
	}

	@Override
	public List<Map<String, Object>> getallApprovedTab(Long userId) {
		return workflowHistoryRepository.findAllApprovedDocumentsForUser(userId).stream().map(row -> {
			Map<String, Object> rowMap = new HashMap<>();
			rowMap.put("applicationNo", row[0]);
			rowMap.put("documentName", row[1]);
			rowMap.put("date", row[2].toString()); // Format date as needed
			rowMap.put("lsNo", row[3]);
			rowMap.put("ssNo", row[4]);
			rowMap.put("status", row[5]);
			rowMap.put("pendingAt", row[6]);
			rowMap.put("refId", row[7]);
			rowMap.put("applicationId", row[8]);
			rowMap.put("applicationInstanceId", row[9]);
			return rowMap;
		}).collect(Collectors.toList());
	}

	@Override
	public List<Map<String, Object>> filterAllDocumentsTab(Long userId, TaskQueryDTO queryDto) {

		Long corrDebateId = utils.getApplicationId(CORRECTED_DEBATES_ABBR);
		System.out.println("Corrected Debate ID: " + corrDebateId);

		StringBuffer qry = new StringBuffer(allDocumentsQuery).append(corrDebateId).append(" as applicationId, ")
				.append(allDocumentsQuery1).append(" where 1 = 1 ");

		System.out.println("Final Query: " + qry);

		MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("userId", userId);

		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetailsDTO) {
			Long houseId = ((UserDetailsDTO) principal).getHouseId();
			parameters.addValue("houseId", houseId);
		}

		if (queryDto.getApplicationId() != null) {
			// qry = qry.append(" and applicationNo =
			// ").append(queryDto.getApplicationId());
			qry = qry.append(" and (ma.id = :applicationId or ma.parent_application_id = :applicationId) ");
			parameters.addValue("applicationId", queryDto.getApplicationId());
		}

		if (!Utils.isEmpty(queryDto.getLsNo())) {
			// qry = qry.append(" and LSNo = '").append(queryDto.getLsNo()).append("'");
			qry = qry.append(" and LSNo = :LsNo ");
			parameters.addValue("LsNo", queryDto.getLsNo());
		}

		if (!Utils.isEmpty(queryDto.getLsSessionNo())) {
			// qry = qry.append(" and SSNo =
			// '").append(queryDto.getLsSessionNo()).append("'");
			qry = qry.append(" and SSNo = :LsSessionNo ");
			parameters.addValue("LsSessionNo", queryDto.getLsSessionNo());
		}

		if (!Utils.isEmpty(queryDto.getRsSessionNo())) {
			// qry = qry.append(" and RSSNo =
			// '").append(queryDto.getRsSessionNo()).append("'");
			qry = qry.append(" and RSSNo = :RsSessionNo ");
			parameters.addValue("RsSessionNo", queryDto.getRsSessionNo());
		}

		if (!Utils.isEmpty(queryDto.getKeyword())) {
			qry = qry.append(" and pendingAt ILIKE '%").append(queryDto.getKeyword()).append("%'")
					.append(" or applicationNo ILIKE '%").append(queryDto.getKeyword()).append("%'");
		}

		if (queryDto.getFromDate() != null) {
			qry = qry.append(" and date >= :from_date ");
			parameters.addValue("from_date", queryDto.getFromDate());
		}

		if (queryDto.getToDate() != null) {
			qry = qry.append(" and date <= :to_date ");
			parameters.addValue("to_date", queryDto.getToDate());
		}

		qry.append(orderByClause);

		List<DocumentRowMapper> results = jdbcTemplate.query(qry.toString(), parameters,
				new BeanPropertyRowMapper<>(DocumentRowMapper.class));

		return results.stream().map(row -> {
			Map<String, Object> rowMap = new HashMap<>();
			rowMap.put("applicationNo", row.getApplicationNo());
			rowMap.put("documentName", row.getDocumentName());
			rowMap.put("date", row.getDate() != null ? row.getDate().toString() : null); // Handle NULL date
			rowMap.put("lsNo", row.getLSNo());
			rowMap.put("ssNo", row.getSSNo());
			rowMap.put("rssNo", row.getRSSNo());
			rowMap.put("status", row.getStatus());
			rowMap.put("pendingAt", row.getPendingAt());
			rowMap.put("refId", row.getRefId());
			rowMap.put("applicationId", row.getApplicationId());
			rowMap.put("applicationInstanceId", row.getApplicationInstanceId());
			rowMap.put("taskName", row.getTaskName());
			rowMap.put("taskId", row.getTaskId());
			rowMap.put("actionPerformed", row.getActionPerformed());
			rowMap.put("formUrl", row.getFormUrl());
			rowMap.put("actionUrl", row.getActionUrl());
			rowMap.put("draftId", row.getDraftId());
			rowMap.put("publishedUrl", row.getPublishedUrl());
			rowMap.put("documentUUIDEn", row.getDocumentUUIDEn());
			rowMap.put("documentUUIDHi", row.getDocumentUUIDHi());
			rowMap.put("htmlContentEn", row.getHtmlContentEn());
			rowMap.put("htmlContentHi", row.getHtmlContentHi());
			rowMap.put("editorId", row.getEditorId());
			rowMap.put("translateFlag", row.getTranslateFlag());
			rowMap.put("downloadFlag", row.getDownloadFlag());
			rowMap.put("saveAsDraftUrl", row.getSaveAsDraftUrl());
			rowMap.put("uploadedDocumentUUID", row.getUploadedDocumentUUID());
			return rowMap;
		}).collect(Collectors.toList());

	}

	@Transactional
	public Map<String, Object> saveAsDraft(Long userId, DraftDataDTO draftDataDTO) {
		String userName = utils.getUserFullNameFromAuthentication();
		String lokSabhaSession = null;
		String lokSabhaSessionNumber = null;
		String rajyaSabhaSession = null;
		Long houseId = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetailsDTO) {
			houseId = ((UserDetailsDTO) principal).getHouseId();
		}
		SerialNumberRequest serialNumberRequest = new SerialNumberRequest();
		serialNumberRequest.setAppId(draftDataDTO.getApplicationId());
		if (houseId == 1) {
			serialNumberRequest.setSessionId(draftDataDTO.getRajyaSabhaSession());
			rajyaSabhaSession = workflowHistoryRepository.getRajyaSabhaSession(draftDataDTO.getRajyaSabhaSession());
		}
		if (houseId == 2) {
			serialNumberRequest.setSessionId(draftDataDTO.getLokSabhaSessionNumber());
			lokSabhaSession = workflowHistoryRepository.getLokSabhaSession(draftDataDTO.getLokSabhaSession());
			lokSabhaSessionNumber = workflowHistoryRepository
					.getLokSabhaSessionNumber(draftDataDTO.getLokSabhaSessionNumber());

		}

		JsonNode draftData = draftDataDTO.getData();
		DraftDocument draftDocument;

		if (draftDataDTO.getDraftId() != null) {
			// Update existing draft
			draftDocument = draftDocumentRepository.findById(draftDataDTO.getDraftId()).orElseThrow(
					() -> new IllegalArgumentException("Draft not found for ID: " + draftDataDTO.getDraftId()));

			draftDocument.setDraftData(draftData);
			draftDocument.setLokSabhaSession(lokSabhaSession);
			draftDocument.setLokSabhaSessionNumber(lokSabhaSessionNumber);
			draftDocument.setRajyaSabhaSession(rajyaSabhaSession);
			draftDocument.setForDate(draftDataDTO.getForDate());
			draftDocument.setOnlyOfficeDsp2Key(draftDataDTO.getDsp2key());

			// Save updated draft document
			draftDocumentRepository.save(draftDocument);

		} else {
			// Create new draft
			draftDocument = new DraftDocument();
			draftDocument.setDraftData(draftData);
			draftDocument.setApplicationId(draftDataDTO.getApplicationId());

			// Use existing applicationNo if provided, else generate new one
			if (draftDataDTO.getApplicationNo() != null) {
				draftDocument.setApplicationNo(draftDataDTO.getApplicationNo());
			} else {
				SerialNumberResponse applicationNo = generateApplicationNo(serialNumberRequest);
				draftDocument.setApplicationNo(applicationNo.getId());
			}

			draftDocument.setLokSabhaSession(lokSabhaSession);
			draftDocument.setLokSabhaSessionNumber(lokSabhaSessionNumber);
			draftDocument.setRajyaSabhaSession(rajyaSabhaSession);
			draftDocument.setForDate(draftDataDTO.getForDate());

			// Save new draft document
			draftDocumentRepository.save(draftDocument);

			// Create and save WorkflowHistory
			WorkflowHistory workflowHistory = WorkflowHistory.builder()
					.subModuleId(draftDataDTO.getApplicationId().toString()).pendingWith(userId)
					.pendingWithName(userName).draftDocument(draftDocument) // Use the draftDocument entity directly
					.status("Draft").actionUrl(draftDataDTO.getActionUrl()).build();

			workflowHistoryRepository.save(workflowHistory);
		}

		// Prepare the response with the saved data
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("id", draftDocument.getId());
		responseMap.put("draftData", draftDocument.getDraftData());
		responseMap.put("applicationId", draftDocument.getApplicationId());
		responseMap.put("lokSabhaSession", lokSabhaSession);
		responseMap.put("rajyaSabhaSession", rajyaSabhaSession);
		responseMap.put("lokSabhaSessionNumber", lokSabhaSessionNumber);
		responseMap.put("actionUrl", draftDataDTO.getActionUrl());
		responseMap.put("applicationNo", draftDocument.getApplicationNo());

		return responseMap;
	}

	// Generate Application No Functionality
	public SerialNumberResponse generateApplicationNo(SerialNumberRequest request) {
		String url = masterServiceUrl + "/serial-number";
		String token = getTokenFromRequest();
		if (token == null) {
			throw new IllegalStateException("Authorization token is missing");
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<SerialNumberRequest> entity = new HttpEntity<>(request, headers);

		try {
			// RestTemplate restTemplate = new RestTemplate();
			return restTemplate.postForObject(url, entity, SerialNumberResponse.class);
		} catch (Exception e) {
			System.err.println("Error during API call: " + e.getMessage());
			throw e;
		}
	}

	public String getTokenFromRequest() {
		String authorizationHeader = httpRequest.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring(7);
		}
		return null;
	}

	public Map<String, Object> getSaveAsDraft(Long draftId) {
		// Call the repository method to fetch the draft data
		List<Object[]> draftDataList = draftDocumentRepository.findDraftDataByUserAndApplication(draftId);

		// If data is found, map the fields to a structured response for the first
		// record
		if (!draftDataList.isEmpty()) {
			Object[] draftDataRow = draftDataList.get(0);
			Map<String, Object> responseMap = new HashMap<>();
			responseMap.put("id", draftId);// draft ID
			responseMap.put("draftData", draftDataRow[0]); // draftData (JsonNode)
			responseMap.put("applicationNo", draftDataRow[1]); // applicationNo
			responseMap.put("lokSabhaSession", draftDataRow[2]); // lokSabhaSession
			responseMap.put("lokSabhaSessionNumber", draftDataRow[3]); // lokSabhaSessionNumber
			responseMap.put("rajyaSabhaSession", draftDataRow[4]); // rajyaSabhaSession
			responseMap.put("dsp2key", draftDataRow[5]); // dsp2Key
			return responseMap;
		}

		// Return an empty map if no data is found
		return new HashMap<>();
	}

	@Override
	public String deleteDraftedData(String applicationNo) {
		try {
			Long draftId = draftDocumentRepository.getIdByApplicationNo(applicationNo);
			if (draftId != null) {
				draftDocumentRepository.deleteById(draftId);
				workflowHistoryRepository.deleteByDraftId(draftId);
			}
			return "deleted Succefully";
		} catch (Exception e) {
			// e.printStackTrace();
			throw e;
		}
	}

	@Override
	public List<DraftDataDTO> getDraftByAppId(Long applicationId) {
		// TODO Auto-generated method stub
		// List<DraftDocument> draftDataList=draftDocumentRepository
		// .findByApplicationIdAndCreatedBy(applicationId,utils.getUserIdFromAuthentication().longValue());
		Long userId = utils.getUserIdFromAuthentication().longValue();
		List<Object[]> draftDataList = draftDocumentRepository.findByApplicationIdAndCreatedByData(applicationId,
				userId);
		System.out.println(draftDataList);
		List<DraftDataDTO> responseDtos = new ArrayList<>();
		for (Object[] draftData : draftDataList) {
			Long createbById = (Long) draftData[8];
			Optional<BaseUser> user = userRepository.findById(createbById);
			responseDtos.add(DraftDataDTO.builder().data((JsonNode) draftData[0]).applicationNo((String) draftData[1])
					.draftId((Long) draftData[5]).applicationId((Long) draftData[6])
					.createdAt((LocalDateTime) draftData[7])
					.createdAtWithoutTime(((LocalDateTime) draftData[7]).toLocalDate()).createdById((Long) draftData[8])
					.createdBy(user.get().getFirstName() + " " + user.get().getLastName()).build());
		}
		return responseDtos;
	}

	public boolean processTranslation(Long applicationInstanceId) {
		TranslationInputDTO translationInputDTO = getTranslationInputDTO(applicationInstanceId);

		System.out.println("TRANS DTO: " + translationInputDTO);

		if (translationInputDTO != null && Boolean.TRUE.equals(translationInputDTO.getTranslationFlag())) {
			callTranslationAPI(translationInputDTO);
			return true;
		}
		return false;
	}

	@Override
	public InProgressCountResponse getInProgressCount(Long userId, Long houseId) {
		return workflowHistoryRepository.getInProgressStatusCount(userId, houseId);
	}
	
	@Override
	public TaskResponseDTO getDocStatusForDate (long applicationId, 
								LocalDate docDate) {
		
		List<Object[]> result = applicationInstanceRepository.getDocStatusByDate(applicationId, docDate);

		if (result.isEmpty()) {
			return null;
		}

		return null;
		
//		Object[] row = result.get(0);
//
//	    Date dbDate = Date.parse((String)row[2]);
//	        
//		return TaskResponseDTO.builder()
//				.applicationId((Long) row[0])
//				.status((String) row[1])
//				.date(dbDate)
//				.build();
	}
	

}
