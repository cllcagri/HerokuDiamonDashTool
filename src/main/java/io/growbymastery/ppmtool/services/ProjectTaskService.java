package io.growbymastery.ppmtool.services;

import io.growbymastery.ppmtool.domain.Backlog;
import io.growbymastery.ppmtool.domain.Project;
import io.growbymastery.ppmtool.domain.ProjectTask;
import io.growbymastery.ppmtool.exceptions.ProjectNotFoundException;
import io.growbymastery.ppmtool.repositories.BacklogRepository;
import io.growbymastery.ppmtool.repositories.ProjectRepository;
import io.growbymastery.ppmtool.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectTaskService {
  @Autowired BacklogRepository backlogRepository;

  @Autowired ProjectTaskRepository projectTaskRepository;
  
  @Autowired private ProjectService projectService;

  public ProjectTask addProjectTask(
      String projectIdentifier, ProjectTask projectTask, String username) {
    try {
      // PTs to be added to a specific project , project != null, backlog exist
      Backlog backlog = projectService.findByProjectIdentifier(projectIdentifier, username).getBacklog();
      projectTask.setBacklog(backlog); // set the backlog to PT
      Integer BacklogSequence = backlog.getPTSequence(); // project sequence be like : IDPRO-1 , IDRPRO-2

      BacklogSequence = BacklogSequence + 1; // update the BL Sequence
      backlog.setPTSequence(BacklogSequence);
      projectTask.setProjectSequence(projectIdentifier + "-" + BacklogSequence); // Add Sequence to Project Task
      projectTask.setProjectIdentifer(projectIdentifier);
      // INITIAL priority when priority is null
      if (projectTask.getPriority() == null || projectTask.getPriority() == 0) {
        projectTask.setPriority(3);
      }
      // INITIAL status when status is null
      if (projectTask.getStatus() == "" || projectTask.getStatus() == null) {
        projectTask.setStatus("TO_DO");
      }
      return projectTaskRepository.save(projectTask);
    } catch (Exception e) {
      throw new ProjectNotFoundException("Project Not Found !!");
    }
  }

  public Iterable<ProjectTask> findBacklogById(String id, String username) {
    Project project = projectService.findByProjectIdentifier(id, username);
    if (project == null)
      throw new ProjectNotFoundException("Project with id :" + id + " is not exist !");

    return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
  }

  public ProjectTask findPTBySequence(String backlogId, String ptId, String username) {

    Project project = projectService.findByProjectIdentifier(backlogId, username);
    if (project == null)
      throw new ProjectNotFoundException("Project with id :" + ptId + " is not exist !");

    ProjectTask projectTask = projectTaskRepository.findByProjectSequence(ptId);
    if (projectTask == null)
      throw new ProjectNotFoundException("Project Task with ID " + ptId + " not found.");

    if (!projectTask.getProjectIdentifer().equals(backlogId)) {
      throw new ProjectNotFoundException("PT Id and Project does not match");
    }

    return projectTask;
  }

  public ProjectTask updateProjectSequence(
      ProjectTask updatedProjectTask, String backlogId, String ptId, String username) {
    ProjectTask projectTask = findPTBySequence(backlogId, ptId, username);
    projectTask = updatedProjectTask;
    return projectTaskRepository.save(projectTask);
  }

  public void deletePTByProjectSequence(String backlogId, String ptId, String username) {
    ProjectTask projectTask = findPTBySequence(backlogId, ptId, username);
    projectTaskRepository.delete(projectTask);
  }
}
