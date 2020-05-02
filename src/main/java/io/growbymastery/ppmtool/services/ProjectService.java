package io.growbymastery.ppmtool.services;

import io.growbymastery.ppmtool.domain.Backlog;
import io.growbymastery.ppmtool.domain.Project;
import io.growbymastery.ppmtool.domain.User;
import io.growbymastery.ppmtool.exceptions.ProjectIdException;
import io.growbymastery.ppmtool.exceptions.ProjectNotFoundException;
import io.growbymastery.ppmtool.repositories.BacklogRepository;
import io.growbymastery.ppmtool.repositories.ProjectRepository;
import io.growbymastery.ppmtool.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

  @Autowired private ProjectRepository projectRepository;

  @Autowired private BacklogRepository backlogRepository;

  @Autowired private UserRepository userRepository;

  public Project saveOrUpdateProject(Project project, String username) {

    if (project.getId() != null) {
      Project existingProject =
          projectRepository.findByProjectIdentifier(project.getProjectIdentifier());

      if (existingProject != null && !existingProject.getProjectLeader().equals(username)) {
        throw new ProjectNotFoundException("Project not found in your account");
      }else if(existingProject == null){
        throw new ProjectNotFoundException(
            "Project with ID "
                + project.getProjectIdentifier()
                + "is"
                + " not updated because it was deleted.");
      }
    }

    try {

      User user = userRepository.findByUsername(username);
      project.setUser(user);
      project.setProjectLeader(user.getUsername());
      project.setProjectIdentifier(project.getProjectIdentifier().toUpperCase());

      if (project.getId() == null) {
        Backlog backlog = new Backlog();
        project.setBacklog(backlog);
        backlog.setProject(project);
        backlog.setProjectIdentifier(project.getProjectIdentifier().toUpperCase());
      }

      if (project.getId() != null) {
        project.setBacklog(
            backlogRepository.findByProjectIdentifier(
                project.getProjectIdentifier().toUpperCase()));
      }

      return projectRepository.save(project);
    } catch (Exception e) {
      throw new ProjectIdException("Project ID" + project.getProjectIdentifier().toUpperCase() + "already exists");
    }
  }

  public Project findByProjectIdentifier(String projectId, String username) {
    Project project = projectRepository.findByProjectIdentifier(projectId.toUpperCase());

    if (project == null) {
      throw new ProjectIdException("Project does not exists");
    }

    if (!project.getProjectLeader().equals(username)) {
      throw new ProjectNotFoundException("Project not found in your account");
    }

    return project;
  }

  public Iterable<Project> findAllProjects(String username) {
    return projectRepository.findAllByProjectLeader(username);
  }

  public void deleteProjectByIdentifier(String projectId, String username) {
    Project project = projectRepository.findByProjectIdentifier(projectId);

    if (project == null) {
      throw new ProjectIdException("Project does not exists");
    }

    if (!project.getProjectLeader().equals(username)) {
      throw new ProjectNotFoundException("Project not found in your account");
    }

    projectRepository.delete(project);
  }
}
