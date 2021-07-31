package com.zawpavel;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "block", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class BlockDependencyMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter
    private List<String> blockedDependencies;

    private ProjectBuildingRequest buildingRequest;
    private static final Logger log = LoggerFactory.getLogger(BlockDependencyMojo.class);

    public void execute() {
        if (project == null) {
            log.error("Maven project not found. Execution is terminated.");
            return;
        }

        if (blockedDependencies.isEmpty()) {
            return;
        }

        if (blockedDependencies.stream().anyMatch(dependency -> dependency.contains(";"))) {
            log.error("You should specify groupId:artifactId in blockedDependencies section");
            log.error("Execution is terminated");
            return;
        }

        System.out.println(blockedDependencies);
        final var projectArtifacts = project.getArtifacts().stream()
                .map(artifact -> artifact.getGroupId() + ":" + artifact.getArtifactId())
                .collect(Collectors.toUnmodifiableSet());

        final var blockedDependenciesInProject = blockedDependencies.stream()
                .filter(projectArtifacts::contains)
                .collect(Collectors.toUnmodifiableList());

        if (!blockedDependenciesInProject.isEmpty()) {
            throw new RuntimeException("Found some forbidden dependencies: " + blockedDependenciesInProject);
        }
    }
}

