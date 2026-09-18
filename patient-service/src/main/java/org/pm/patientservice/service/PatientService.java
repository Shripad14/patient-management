package org.pm.patientservice.service;

import org.pm.patientservice.dto.PatientRequestDTO;
import org.pm.patientservice.dto.PatientResponseDTO;
import org.pm.patientservice.entity.Patient;
import org.pm.patientservice.exception.EmailAlreadyExistsException;
import org.pm.patientservice.exception.PatientNotFoundException;
import org.pm.patientservice.mapper.PatientMapper;
import org.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository){
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepository.findAll();

        List<PatientResponseDTO> patientResponseDTOS = patients
                .stream()
                .map(patient -> PatientMapper.toDTO(patient))
                .toList();
        return patientResponseDTOS;
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){

        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException(
                    "An email byname " + patientRequestDTO.getEmail()
                    + " already exists.");
        }
        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(
            UUID id,
            PatientRequestDTO patientRequestDTO
    ){
        Patient patient = patientRepository
                .findById(id)
                .orElseThrow(
                        () -> new PatientNotFoundException("Patient not found with id "+ id)
                );

        if(patientRepository.existsByEmailAndIdNot(patient.getEmail(),id)){
            throw new EmailAlreadyExistsException(
                    "An email by name " + patient.getEmail()
                    + "already exists"
            );
        }

        patient.setRegisteredDate(LocalDate.parse(patientRequestDTO.getRegisteredDate()));
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());

        Patient updatedPatient = patientRepository.save(patient);

        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id){
        patientRepository.deleteById(id);
    }
}
