# dephelp-maven-plugin
Dependency Helper Maven Plugin  
a small plugin to help you manage your dependencies. it has two goals:  
- licences  
  to show you all the used licenses of your dependencies (including transitive ones) 
- block  
  to forbid using specific version of some dependency  
  you have to provide information about the dependency in `blockedDependency` parameter in `groupId:artifactId` format  
  
example of configuration for `pom.xml` :
```
<plugins>
    <plugin>
        <groupId>com.zawpavel</groupId>
        <artifactId>dephelp-maven-plugin</artifactId>
        <version>1.0.0</version>
        <executions>
            <execution>
                <goals>
                    <goal>licences</goal>
                    <goal>block</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <blockedDependencies>
                <blockedDependency>info.picocli:picocli</blockedDependency>
            </blockedDependencies>
        </configuration>
    </plugin>
</plugins>
```
then it will work in `compile` maven phase, for example when running `mvn verify`  
if you are going to test this plugin, first of all install it to your local maven repository using `mvn clean install` command 
