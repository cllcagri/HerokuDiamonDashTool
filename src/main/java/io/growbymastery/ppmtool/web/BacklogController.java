package io.growbymastery.ppmtool.web;

import io.growbymastery.ppmtool.domain.ProjectTask;
import io.growbymastery.ppmtool.services.MapValidationErrorService;
import io.growbymastery.ppmtool.services.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/api/backlog")
@CrossOrigin
public class BacklogController {

  @Autowired private ProjectTaskService projectTaskService;

  @Autowired private MapValidationErrorService mapValidationErrorService;

  @PostMapping("/{backlogId}")
  public ResponseEntity<?> addPTtoBacklog(@Valid @RequestBody ProjectTask projectTask,BindingResult bindingResult,
                                          @PathVariable String backlogId, Principal principal) {
    ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationErrorService(bindingResult);
    if (errorMap != null) {
      return errorMap;
    }
    ProjectTask task =
        projectTaskService.addProjectTask(backlogId, projectTask, principal.getName());
    return new ResponseEntity<>(task, HttpStatus.CREATED);
  }

  @GetMapping("/{backlogId}")
  public Iterable<ProjectTask> getProjectBacklog(
      @PathVariable String backlogId, Principal principal) {
    return projectTaskService.findBacklogById(backlogId, principal.getName());
  }

  @GetMapping("/{backlogId}/{ptId}")
  public ResponseEntity<?> getProjectTask(@PathVariable String backlogId, @PathVariable String ptId, Principal principal) {
    ProjectTask projectTask = projectTaskService.findPTBySequence(backlogId, ptId, principal.getName());
    return new ResponseEntity<>(projectTask, HttpStatus.OK);
  }

  @PatchMapping("/{backlogId}/{ptId}")
  public ResponseEntity<?> updateProjectTask(
      @Valid @RequestBody ProjectTask projectTask,
      @PathVariable String backlogId,
      @PathVariable String ptId,
      BindingResult result,
      Principal principal) {
    ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationErrorService(result);
    if (errorMap != null) return errorMap;

    ProjectTask updatedProjectTask = projectTaskService.updateProjectSequence(projectTask, backlogId, ptId, principal.getName());
    return new ResponseEntity<>(updatedProjectTask, HttpStatus.OK);
  }

  @DeleteMapping("/{backlogId}/{ptId}")
  public ResponseEntity<?> deleteProjectTask(
      @PathVariable String backlogId, @PathVariable String ptId, Principal principal) {
    projectTaskService.deletePTByProjectSequence(backlogId, ptId, principal.getName());
    return new ResponseEntity<>("Project Task with ID " + ptId + " was deleted.", HttpStatus.OK);
  }
}
