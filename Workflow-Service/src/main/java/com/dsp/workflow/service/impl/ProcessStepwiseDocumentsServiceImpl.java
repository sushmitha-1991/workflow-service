///**
// * 
// * @author Mrinal Lakhanpal
// * @version 1.0
// */
//package com.dsp.workflow.service.impl;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.dsp.workflow.dto.ProcessStepwiseDocumentsDTO;
//import com.dsp.workflow.entity.NoteStepwiseDocuments;
//import com.dsp.workflow.exception.ResourceNotFoundException;
//import com.dsp.workflow.mapper.NotesheetStepwiseDocumentsMapper;
//import com.dsp.workflow.notesheet.entity.Notesheet;
//import com.dsp.workflow.notesheet.repository.NotesheetRepository;
//import com.dsp.workflow.repository.ProcessStepwiseDocumentsRepository;
//import com.dsp.workflow.service.ProcessStepwiseDocumentsService;
//
//@Service
//public class ProcessStepwiseDocumentsServiceImpl implements ProcessStepwiseDocumentsService {
//
//	private final ProcessStepwiseDocumentsRepository documentsRepository;
//	private final NotesheetStepwiseDocumentsMapper documentsMapper;
//
//	@Autowired
//	private NotesheetRepository notesheetRepository;
//
//	public ProcessStepwiseDocumentsServiceImpl(ProcessStepwiseDocumentsRepository documentsRepository,
//			NotesheetStepwiseDocumentsMapper documentsMapper) {
//		this.documentsRepository = documentsRepository;
//		this.documentsMapper = documentsMapper;
//	}
//
//	@Override
//	public ProcessStepwiseDocumentsDTO getDocumentById(Long id) {
//		NoteStepwiseDocuments document = documentsRepository.findById(id)
//				.orElseThrow(() -> new ResourceNotFoundException("Document not found"));
//		return documentsMapper.toDTO(document);
//	}
//
//	@Override
//	public ProcessStepwiseDocumentsDTO createDocument(ProcessStepwiseDocumentsDTO documentDTO) {
//		// Fetch the Notesheet entity based on the noteSheetId
//		Notesheet notesheet = notesheetRepository.findById(documentDTO.getNoteSheetId())
//				.orElseThrow(() -> new ResourceNotFoundException("Notesheet not found"));
//
//		NoteStepwiseDocuments document = documentsMapper.toEntity(documentDTO, notesheet);
//		document.setNotesheet(notesheet); // Set the notesheet reference
//
//		NoteStepwiseDocuments savedDocument = documentsRepository.save(document);
//		return documentsMapper.toDTO(savedDocument);
//	}
//
//	@Override
//	public ProcessStepwiseDocumentsDTO updateDocument(Long id, ProcessStepwiseDocumentsDTO documentDTO) {
//		NoteStepwiseDocuments existingDocument = documentsRepository.findById(id)
//				.orElseThrow(() -> new ResourceNotFoundException("Document not found"));
//
//		// Fetch the Notesheet using the noteSheetId from the DTO
//		Notesheet notesheet = notesheetRepository.findById(documentDTO.getNoteSheetId())
//				.orElseThrow(() -> new ResourceNotFoundException("Notesheet not found"));
//
//		// Update the existing document with the new details
//		documentsMapper.updateEntityFromDTO(documentDTO, existingDocument, notesheet);
//
//		// Save the updated document
//		NoteStepwiseDocuments updatedDocument = documentsRepository.save(existingDocument);
//		return documentsMapper.toDTO(updatedDocument);
//	}
//
//	@Override
//	public void deleteDocument(Long id) {
//		NoteStepwiseDocuments existingDocument = documentsRepository.findById(id)
//				.orElseThrow(() -> new ResourceNotFoundException("Document not found"));
//
//		// Set isDeleted to true for soft deletion
//		existingDocument.setIsDeleted(true);
//
//		// Save the updated document back to the repository
//		documentsRepository.save(existingDocument);
//	}
//
//	@Override
//	public List<ProcessStepwiseDocumentsDTO> getAllDocuments() {
//		List<NoteStepwiseDocuments> documents = documentsRepository.findAll(); // Fetch all documents
//		return documents.stream().map(documentsMapper::toDTO) // Corrected to use the right method name
//				.collect(Collectors.toList());
//	}
//
//}
