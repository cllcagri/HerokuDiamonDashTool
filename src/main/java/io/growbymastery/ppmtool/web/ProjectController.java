package io.growbymastery.ppmtool.web;

import io.growbymastery.ppmtool.domain.Project;
import io.growbymastery.ppmtool.services.MapValidationErrorService;
import io.growbymastery.ppmtool.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/project")
@CrossOrigin
public class ProjectController {
  @Autowired private ProjectService projectService;

  @Autowired private MapValidationErrorService mapValidationErrorService;

  @PostMapping("")
  public ResponseEntity<?> createNewProject(
      @Valid @RequestBody Project project, BindingResult bindingResult, Principal principal) {
    // check the validations
    ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationErrorService(bindingResult);
    if (errorMap != null) return errorMap;

    Project projectMysql = projectService.saveOrUpdateProject(project, principal.getName());
    return new ResponseEntity<>(projectMysql, HttpStatus.CREATED);
  }

  @GetMapping("/{projectId}")
  public ResponseEntity<?> getProjectById(@PathVariable(value = "projectId") String projectId, Principal principal) {
    Project project = projectService.findByProjectIdentifier(projectId, principal.getName());
    return new ResponseEntity<>(project, HttpStatus.OK);
  }

  @GetMapping("/all")
  public Iterable<?> getAllProjects(Principal principal) {
    return projectService.findAllProjects(principal.getName());
  }

  @DeleteMapping("/{projectId}")
  public ResponseEntity<?> deleteProject(@PathVariable String projectId, Principal principal) {
    projectService.deleteProjectByIdentifier(projectId, principal.getName());

    return new ResponseEntity<String>(
        "Project with ID " + projectId + " was deleted.", HttpStatus.OK);
  }
}
