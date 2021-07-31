package com.zawpavel;


import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.*;
import org.apache.maven.repository.RepositorySystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "licences", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class LicencesMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Component
    RepositorySystem repositorySystem;

    @Component
    protected ProjectBuilder projectBuilder;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(property = "localRepository", required = true, readonly = true)
    protected ArtifactRepository localRepository;

    @Parameter(property = "project.remoteArtifactRepositories")
    protected List<ArtifactRepository> remoteRepositories;

    @Parameter(property = "project.pluginArtifactRepositories")
    protected List<ArtifactRepository> pluginRepositories;

    private ProjectBuildingRequest buildingRequest;
    private static final Logger log = LoggerFactory.getLogger(LicencesMojo.class);

    public void execute() {
        if (project == null) {
            log.error("Maven project not found. Execution is terminated.");
            return;
        }

        buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setLocalRepository(localRepository);
        buildingRequest.setRemoteRepositories(remoteRepositories);
        buildingRequest.setPluginArtifactRepositories(pluginRepositories);

        var projectArtifacts = project.getArtifacts();
        var dependenciesInfo = projectArtifacts.stream()
                .map(artifact -> artifact.getGroupId() + ":"
                        + artifact.getArtifactId() + ":"
                        + artifact.getVersion() + "; with licences: "
                        + findLicenses(artifact))
                .collect(Collectors.joining("\n"));

        log.info("Project dependencies and their licenses:\n{}", dependenciesInfo);
    }

    private String findLicenses(final Artifact artifact) {
        Artifact projectArtifact = artifact;

        boolean allowStubModel = false;
        String artifactType = artifact.getType();
        if (!artifactType.equals("pom")) {
            projectArtifact = repositorySystem.createProjectArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
            allowStubModel = true;
        }

        MavenProject project;
        try {
            project = projectBuilder.build(projectArtifact, allowStubModel, buildingRequest).getProject();
        } catch (ProjectBuildingException exception) {
            log.info("Some problems with " + projectArtifact.getGroupId() + ":" + projectArtifact.getArtifactId());
            log.info(exception.getMessage());
            return "Unknown license";
        }

        final var licences = project.getLicenses();
        if (licences.isEmpty()) {
            return "Unknown license";
        }
        return licences.stream().map(License::getName).collect(Collectors.joining("; "));
    }
}
